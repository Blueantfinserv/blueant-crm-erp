# V56 Production Read-Only DBA Assessment & Technical Evaluation

**Target Document:** Production Read-Only Assessment for `V56__optimize_role_based_query_indexes.sql`  
**Git Reference:** Branch `testing` at commit `b4cabca61a942fa39e946050a120cd2fb22b61e3` (`origin/testing`)  
**Scope:** Strict Read-Only Assessment for 16 Indexes across 9 Target Tables  
**Work Follow Status:** Strict No-Touch Boundary Maintained (4 protected tables excluded)  

---

## 1. Engine Parameters & Database Sizing Framework

### 1.1 Read-Only Telemetry Query Battery
The production DBA must execute the following non-blocking queries during off-peak hours to collect baseline instance telemetry:

```sql
-- Query 1.1: MySQL Instance Configuration & Engine Sizing Parameters
-- [PENDING PRODUCTION TELEMETRY]
SELECT 
    VERSION() AS mysql_version,
    @@version_comment AS version_comment,
    @@innodb_version AS innodb_version,
    @@sql_mode AS sql_mode,
    ROUND(@@innodb_buffer_pool_size / (1024*1024*1024), 2) AS buffer_pool_gb,
    ROUND(@@innodb_online_alter_log_max_size / (1024*1024), 2) AS online_alter_log_max_mb,
    ROUND(@@innodb_sort_buffer_size / (1024*1024), 2) AS sort_buffer_mb,
    @@tmpdir AS tmpdir;

-- Query 1.2: Storage Footprint of the 9 V56 Target Tables
-- [PENDING PRODUCTION TELEMETRY]
SELECT 
    table_name,
    engine,
    row_format,
    table_rows AS est_row_count,
    ROUND(data_length / (1024 * 1024), 2) AS data_mb,
    ROUND(index_length / (1024 * 1024), 2) AS index_mb,
    ROUND(data_free / (1024 * 1024), 2) AS data_free_mb,
    ROUND((data_length + index_length) / (1024 * 1024), 2) AS total_mb
FROM information_schema.tables
WHERE table_schema = DATABASE()
  AND table_name IN (
    'leads', 'clients', 'client_followups', 'client_family_members',
    'client_family_head_histories', 'meetings', 'crm_verifications',
    'client_import_jobs', 'audit_logs'
  )
ORDER BY (data_length + index_length) DESC;
```

### 1.2 Technical Engine Requirements vs. Operational Baselines
- **MySQL Version [TECHNICAL REQUIREMENT]:** MySQL **8.0 GA** is required for descending index support (`DESC` collation in B-tree), utilized by 4 V56 indexes (`idx_cl_followup_sp_date`, `idx_cl_famhist_family_changed_at`, `idx_client_import_created_by_date`, `idx_audit_entity_time`). In MySQL 5.7 and earlier, the `DESC` keyword was parsed but treated as ascending (`ASC`), which would fail to eliminate filesort for descending pagination.
- **Row Format & Key Length Assessment:**  
  While `DYNAMIC` is the standard default row format in MySQL 8.0, the composite indexes defined in V56 have modest key lengths:
  - The widest index is `idx_audit_entity_time` on `audit_logs (entity_name, entity_id, action_time DESC)`. Under `utf8mb4`, `entity_name` (VARCHAR(100)) requires up to 400 bytes, which combined with BIGINT (8 bytes) and DATETIME (8 bytes) yields $\approx 416\text{ bytes}$.
  - This comfortably fits well within both the legacy 767-byte limit (`COMPACT`/`REDUNDANT`) and the modern 3072-byte limit (`DYNAMIC`/`COMPRESSED`). A specific row format migration is therefore not a prerequisite for V56.

---

## 2. Existing Index Inventory & Duplicate/Redundant Index Analysis

### 2.1 Complete Structural Overlap Analysis (All 16 V56 Indexes)

| # | Target Table | Proposed V56 Index | Existing Overlapping Index | Structural Overlap Type | DBRE Governance Recommendation |
| :---: | :--- | :--- | :--- | :--- | :--- |
| **1** | `leads` | `idx_leads_sales_person_status`<br>`(assigned_sales_person_id, lead_status)` | `idx_lead_sales_person`<br>`(assigned_sales_person_id)` | Left-Prefix Subset | **RETAIN BOTH:** Keep existing single-column index to prevent regression on single-column foreign key validations. |
| **2** | `leads` | `idx_leads_leader_status`<br>`(assigned_leader_id, lead_status)` | `idx_lead_leader`<br>`(assigned_leader_id)` | Left-Prefix Subset | **RETAIN BOTH:** Preserves FK lookup index for leader assignments. |
| **3** | `leads` | `idx_leads_created_at`<br>`(created_at)` | *None* | Zero Overlap | **RETAIN:** Independent secondary index for date-range reporting. |
| **4** | `leads` | `idx_leads_sales_person_created_at`<br>`(assigned_sales_person_id, created_at)` | `idx_lead_sales_person`<br>`(assigned_sales_person_id)` | Left-Prefix Sibling | **RETAIN ALL:** Optimizes employee-scoped date ranges; does not conflict with status pipeline index #1. |
| **5** | `leads` | `idx_leads_physical_unassigned`<br>`(is_physical_lead, assigned_sales_person_id)` | `idx_lead_is_physical`<br>`(is_physical_lead)` | Left-Prefix Subset | **RETAIN BOTH:** Composite index accelerates unassigned physical lead dispatch queue. |
| **6** | `clients` | `idx_clients_sales_person_status_del_id`<br>`(sales_person_id, client_status, is_deleted, id)` | `idx_client_sales_person`<br>`(sales_person_id)` | Left-Prefix Subset | **RETAIN BOTH:** Composite index eliminates filesort on paginated client status lists. |
| **7** | `clients` | `idx_clients_rm_deleted`<br>`(relationship_manager_id, is_deleted)` | `fk_client_rm`<br>`(relationship_manager_id)` | Foreign Key Support | **INSPECT & RETAIN:** Composite index backs `CONSTRAINT fk_client_rm`. Single-column index may be absorbed by InnoDB. *Rollback safety requires pre-drop verification.* |
| **8** | `client_followups` | `idx_cl_followup_sp_date`<br>`(sales_person_id, followup_date DESC)` | `idx_client_followup_sp`<br>`(sales_person_id)` | Left-Prefix Subset | **RETAIN BOTH:** Composite provides index-sorted descending follow-up ordering. |
| **9** | `client_family_members` | `idx_cl_fammem_family_status`<br>`(family_id, membership_status)` | `idx_cl_fammem_family`<br>`(family_id)` | Left-Prefix Subset | **RETAIN BOTH:** Supports active family head lookups. |
| **10** | `client_family_members` | `idx_cl_fammem_client_status`<br>`(client_id, membership_status)` | `idx_cl_fammem_client`<br>`(client_id)` | Left-Prefix Subset | **RETAIN BOTH:** Accelerates client active membership status checks. |
| **11** | `client_family_head_histories` | `idx_cl_famhist_family_changed_at`<br>`(family_id, changed_at DESC, id DESC)` | `idx_cl_famhist_family`<br>`(family_id)` | Left-Prefix Subset | **RETAIN BOTH:** Eliminates filesort on audit log queries. |
| **12** | `meetings` | `idx_meetings_lead_status_seq`<br>`(lead_id, meeting_status, meeting_sequence)` | `uk_lead_meeting_sequence`<br>`(lead_id, meeting_sequence)` | Partial Overlap | **RETAIN BOTH:** `uk_` enforces uniqueness; composite optimizes status-filtered sequence retrieval. |
| **13** | `crm_verifications` | `idx_crm_verif_lead_status`<br>`(lead_id, verification_status)` | `idx_crm_verif_lead`<br>`(lead_id)` | Left-Prefix Subset | **RETAIN BOTH:** Supports verification queue queries. |
| **14** | `client_import_jobs` | `idx_client_import_created_by_date`<br>`(created_by, created_at DESC)` | `idx_client_import_created_by`<br>`(created_by)` | Left-Prefix Subset | **RETAIN BOTH:** Eliminates filesort on user import job pagination. |
| **15** | `client_import_jobs` | `idx_client_import_status_heartbeat`<br>`(status, heartbeat_at)` | `idx_client_import_status`<br>`(status)` | Left-Prefix Subset | **RETAIN BOTH:** Optimizes watchdog queries for stalled import jobs. |
| **16** | `audit_logs` | `idx_audit_entity_time`<br>`(entity_name, entity_id, action_time DESC)` | `idx_audit_entity`<br>`(entity_name, entity_id)` | Superset Extension | **RETAIN BOTH:** Covers multi-column entity audit history without removing base index. |

### 2.2 DBA Recommendation on Redundancy
**No existing index should be dropped in V56.**  
*Rationale:* Dropping pre-existing indexes concurrently with creating new ones introduces unnecessary deployment risk. Index pruning should only be undertaken in a future migration (V57) after production optimizer statistics (`sys.schema_unused_indexes`) have collected 30+ days of production metrics.

---

## 3. Concurrency, Metadata Locks & Transaction Inspection

### 3.1 Pre-Flight Concurrency Queries

```sql
-- Query 3.1A: All Active InnoDB Transactions (Unfiltered Inspection)
-- [PENDING PRODUCTION TELEMETRY]
SELECT 
    trx_id,
    trx_started,
    TIMESTAMPDIFF(SECOND, trx_started, NOW()) AS duration_seconds,
    trx_state,
    trx_mysql_thread_id AS connection_id,
    trx_tables_locked,
    trx_rows_locked,
    LEFT(trx_query, 80) AS active_query
FROM information_schema.innodb_trx
ORDER BY trx_started ASC;

-- Query 3.1B: Threshold-Filtered Long-Running Transactions (>= 30 Seconds)
-- [PENDING PRODUCTION TELEMETRY]
SELECT 
    trx_id,
    trx_started,
    TIMESTAMPDIFF(SECOND, trx_started, NOW()) AS duration_seconds,
    trx_state,
    trx_mysql_thread_id AS connection_id,
    trx_tables_locked,
    trx_rows_locked,
    LEFT(trx_query, 100) AS active_query
FROM information_schema.innodb_trx
WHERE TIMESTAMPDIFF(SECOND, trx_started, NOW()) >= 30
ORDER BY trx_started ASC;

-- Query 3.2: Target-Table Metadata Lock Inspection (Correlated to Processlist)
-- [PENDING PRODUCTION TELEMETRY]
SELECT
    ml.object_schema,
    ml.object_name AS table_name,
    ml.lock_type,
    ml.lock_duration,
    ml.lock_status,
    t.processlist_id AS connection_id,
    t.processlist_user AS user,
    t.processlist_host AS host,
    LEFT(t.processlist_info, 80) AS current_query
FROM performance_schema.metadata_locks ml
JOIN performance_schema.threads t 
  ON ml.owner_thread_id = t.thread_id
WHERE ml.object_schema = DATABASE()
  AND ml.lock_status IN ('PENDING', 'GRANTED')
  AND ml.object_name IN (
    'leads', 'clients', 'client_followups', 'client_family_members',
    'client_family_head_histories', 'meetings', 'crm_verifications',
    'client_import_jobs', 'audit_logs'
  )
ORDER BY ml.lock_status DESC, ml.object_name;

-- Query 3.3: Active Lock Wait Stalls (Direct Blocker Correlation)
-- [PENDING PRODUCTION TELEMETRY]
SELECT
    object_schema,
    object_name,
    waiting_thread_id,
    waiting_pid,
    waiting_account,
    waiting_lock_type,
    waiting_lock_duration,
    waiting_query_secs,
    waiting_query,
    blocking_thread_id,
    blocking_pid,
    blocking_account,
    blocking_lock_type,
    blocking_lock_duration,
    sql_kill_blocking_connection
FROM sys.schema_table_lock_waits
WHERE object_schema = DATABASE();
```

### 3.2 Metadata Lock Lifecycle & Governance (GRANTED vs. PENDING)
1. **Object-Level Lock Acquisition:**  
   Metadata locks are acquired statement-by-statement on the specific database objects referenced. For instance, executing `SELECT` on `clients` acquires a `SHARED_READ` metadata lock on `clients` only; other tables in the schema remain un-locked by that statement. Write statements (`INSERT`, `UPDATE`, `DELETE`) acquire `SHARED_WRITE` on their target tables.
2. **Two-Phase Lock Lifetime:**  
   Metadata locks acquired by statements within a transaction are retained until the transaction explicitly commits (`COMMIT`) or rolls back (`ROLLBACK`). They are not released when the individual statement finishes.
3. **`lock_status = 'GRANTED'` vs. `lock_status = 'PENDING'`:**
   - **`GRANTED`:** Indicates an active connection currently holds access to an object. In a live system, seeing `GRANTED` locks on target tables is normal operational behavior. **A `GRANTED` lock is never grounds for terminating a connection.**
   - **`PENDING`:** Indicates a thread is stalled in the lock queue waiting for an incompatible lock (such as Flyway requesting `EXCLUSIVE` metadata lock during DDL). In MySQL, waiting `MDL-X` requests block subsequent incoming read/write traffic on that table.
4. **DBA Intervention Protocol:**
   - Pre-flight Gate: Query 3.3 must return **0 rows** before initiating migration.
   - If Query 3.1B shows active transactions $\ge 30\text{ seconds}$ accessing target tables: The DBA must investigate the transaction origin, business criticality, and query execution state before taking any action.
   - The `sql_kill_blocking_connection` column in Query 3.3 is an **informational suggestion only**. It must never be executed automatically. Terminating a connection requires explicit DBA review and authorization.

---

## 4. Storage Capacity & Headroom Evaluation

### 4.1 Storage Space Decomposition
Storage requirements must not be generalized as a universal multiplier. They consist of four distinct components:

1. **Tablespace Growth (`.ibd` Extent Allocation):**  
   Secondary index pages are allocated inside the table's existing `.ibd` file. When existing allocated free pages (`DATA_FREE`) are exhausted, InnoDB extends the tablespace in chunks (typically 1 MB to 4 MB). Tablespaces do not automatically shrink.
2. **Temporary Merge Sort Space (`@@tmpdir`):**  
   During index builds, InnoDB extracts index keys, sorts them using memory buffers (`innodb_sort_buffer_size`), and writes intermediate sorted runs to disk in `@@tmpdir`. The required temporary space depends on row count, indexed column data lengths, character sets, and merge sort passes.
3. **Filesystem Free Space:**  
   The underlying volume hosting the MySQL data directory must have verified free space exceeding the estimated tablespace growth, plus an operational buffer approved by the DBA.
4. **Online Alter Log Headroom (`innodb_online_alter_log_max_size`):**  
   Specifies an **upper limit on the cumulative size of the temporary log files** on disk used to record concurrent DML modifications (`INSERT`, `UPDATE`, `DELETE`) that occur while an online index is being created. If concurrent write volume during the build exceeds this configured limit, the DDL aborts with `DB_ONLINE_LOG_TOO_BIG`.  
   *Assessment Policy:* Read-only inspection only. Do not execute `SET GLOBAL` configuration modifications during this assessment.

---

## 5. Online DDL Compatibility & Behavior Qualification

### 5.1 Syntax Analysis of `V56__optimize_role_based_query_indexes.sql`
The V56 migration contains standard SQL statements:
```sql
CREATE INDEX idx_leads_sales_person_status ON leads (assigned_sales_person_id, lead_status);
```
- **Syntax Characteristic:** Statements do **not** explicitly include `ALGORITHM=INPLACE` or `LOCK=NONE` clauses.
- **Engine Behavior:** In MySQL 8.0 InnoDB, adding a secondary index (`CREATE INDEX`) is natively supported as an `INPLACE` operation with `LOCK=NONE` (concurrent DML permitted). The presence of triggers, foreign keys, or full-text indexes does **not** force table-copy (`ALGORITHM=COPY`) for secondary index creation.
- **No Zero-Downtime Guarantee:** While InnoDB secondary index creation supports concurrent DML during the index build phase, **zero downtime cannot be guaranteed**. Metadata lock acquisition at the start (preparation) and completion (commit) of each statement requires momentary exclusive access (`MDL-X`). Any active transaction holding `MDL-S` on a target table will cause the DDL to stall in `PENDING`, queuing subsequent application requests.
- **DBA Precondition:** The DBA must confirm in production that the 9 tables are standard InnoDB tables and that active concurrency allows lock acquisition without connection pileups.

---

## 6. Execution Duration & Maintenance Window

### 6.1 Unverified Execution Duration Status
> [!NOTE]
> **DURATION STATUS: UNVERIFIED [PENDING PRODUCTION TELEMETRY]**  
> Concrete execution durations cannot be estimated in advance without production telemetry. True duration is governed by production hardware I/O throughput (IOPS, disk latency), row count, average row width, buffer pool efficiency, and concurrent workload.

### 6.2 Maintenance Window Recommendation
- **Execution Strategy:** Because metadata lock queues can impact application throughput, the migration must be scheduled during an **approved off-peak maintenance window**.
- **Recommended Schedule:** Standard low-traffic window: **01:30 – 03:00 IST**.
- **Window Allocation:** Total window of 45 minutes to include pre-flight checks, sequential DDL execution, post-migration validation, and buffer for rollback decision gates.

### 6.3 Rollback Safeguard Protocol (`clients.idx_clients_rm_deleted`)
In MySQL InnoDB, dropping the only index supporting a foreign key constraint triggers:
```text
ERROR 1553 (HY000): Cannot drop index 'idx_clients_rm_deleted': needed in a foreign key constraint 'fk_client_rm'
```
**Mandatory Pre-Drop Check:** If V56 must be rolled back, the DBA must verify whether an alternate index with `relationship_manager_id` as the leftmost column exists on `clients`. If none exists, the DBA must execute:
```sql
CREATE INDEX fk_client_rm ON clients (relationship_manager_id);
```
*prior* to executing `DROP INDEX idx_clients_rm_deleted ON clients;`.

---

## 7. Backup Readiness & Topology-Aware Replication Gates

### 7.1 Operational Policy vs. MySQL Technical Requirements

| Dimension | Policy-Based Operational Requirement (CAB) | MySQL Technical Requirement |
| :--- | :--- | :--- |
| **Backup Currency** | Full consistent backup taken $< 4\text{ hours}$ before window. | Binlog retention enabled with known coordinates (`SHOW BINARY LOG STATUS`). |
| **Restore Verification** | Documented evidence that backup was restored successfully in staging/DR. | Storage engine crash recovery enabled (`innodb_flush_log_at_trx_commit = 1`). |
| **Replication Health** | Replica lag `Seconds_Behind_Source = 0` at T-5 minutes. | GTID execution consistency (`GTID_MODE = ON`, zero errant transactions). |

### 7.2 Topology-Aware Replication Assessment
- **Replication Mechanism:** Online DDL replicates as a single atomic event **after** committing on the primary.
- **Replica Lag Consideration:** When the replica executes each `CREATE INDEX`, its SQL/applier thread will process the index build sequentially. In multi-threaded applier environments (`slave_parallel_workers > 0`), DDL execution can pause parallel worker queues for that schema.
- **Monitoring Gate:** Monitor `SHOW REPLICA STATUS` across all active read replicas during deployment. If replica lag exceeds application tolerance (e.g. $> 60\text{ seconds}$), pause before executing subsequent index statements.

---

## 8. Summary of Evidence Categories & CAB Recommendation

### 8.1 Evidence Status Classification

| Category | Item Description | Verification Status |
| :--- | :--- | :---: |
| **Isolated Environment Evidence** | 16 V56 indexes & 36 columns verified in MySQL 8.0.46 | **[VERIFIED]** |
| | 384 automated tests pass (unit, regression, integration) | **[VERIFIED]** |
| | Work Follow modules 100% untouched (37/37 tests pass) | **[VERIFIED]** |
| | Commit `b4cabca` pushed to `origin/testing` on authorized repo | **[VERIFIED]** |
| **Pending Production Telemetry** | Exact production table sizes & exact row counts | **[NOT RUN / PENDING]** |
| | Free disk space on production datadir & `@@tmpdir` | **[NOT RUN / PENDING]** |
| | Production metadata lock & transaction status | **[NOT RUN / PENDING]** |
| | Active replication topology & lag baseline | **[NOT RUN / PENDING]** |
| **Mandatory Approval Gates** | Pre-migration consistent backup & restore verification | **[PENDING CAB GATE]** |
| | Maintenance window approval (01:30–03:00 IST) | **[PENDING CAB GATE]** |
| | DBA sign-off on production telemetry query outputs | **[PENDING CAB GATE]** |

---

### 8.2 Final DBA Recommendation: **CONDITIONAL GO**

Production deployment of migration **V56** remains classified as **CONDITIONAL GO**.  
Execution is strictly blocked until the production DBA executes Section 1 and Section 3 telemetry queries, confirms adequate disk headroom, verifies backup/restore evidence, and receives formal Change Advisory Board authorization for the maintenance window.

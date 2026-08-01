package com.blueant_crm_erp.audit.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "entity_name", nullable = false, length = 100)
    private String entityName;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Column(name = "action_type", nullable = false, length = 20)
    private String actionType; // INSERT, UPDATE, DELETE

    @Column(name = "who", nullable = false, length = 100)
    private String who;

    @Column(name = "action_time", nullable = false)
    private LocalDateTime actionTime;

    @Column(name = "old_value", columnDefinition = "json")
    private String oldValue; // Storing as JSON String

    @Column(name = "new_value", columnDefinition = "json")
    private String newValue; // Storing as JSON String

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "browser", length = 200)
    private String browser;

    @Column(name = "device", length = 100)
    private String device;
}

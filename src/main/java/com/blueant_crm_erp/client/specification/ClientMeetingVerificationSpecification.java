package com.blueant_crm_erp.client.specification;

import com.blueant_crm_erp.client.dto.request.ClientMeetingVerificationSearchRequest;
import com.blueant_crm_erp.client.entity.*;
import com.blueant_crm_erp.client.enums.PcBusinessType;
import com.blueant_crm_erp.meeting.enums.SalesRole;
import com.blueant_crm_erp.role.entity.Role;
import com.blueant_crm_erp.user.entity.Designation;
import com.blueant_crm_erp.user.entity.User;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * =============================================================================
 * Client Meeting Verification Specification
 * =============================================================================
 *
 * Dynamic JPA Specification for advanced search and filtering on Client Meeting
 * Verifications.
 *
 * Critical Data Integrity Guarantees:
 * 1. Strictly filters isCurrent = true on every query to eliminate historical duplicate rows.
 * 2. Uses LEFT JOIN on all associations to prevent accidental exclusion of nullable relationships.
 * 3. Uses EXISTS subquery for businessType filtering to prevent row multiplication/Cartesian products.
 * 4. Applies role-based ownership scoping server-side.
 */
public final class ClientMeetingVerificationSpecification {

    private ClientMeetingVerificationSpecification() {
    }

    public static Specification<ClientMeetingVerification> build(
            ClientMeetingVerificationSearchRequest request,
            Long scopedEmployeeId) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Mandatory data integrity invariant: filter only current verification attempts
            predicates.add(cb.isTrue(root.get("isCurrent")));

            // Safe LEFT JOINs on parent associations
            Join<ClientMeetingVerification, ClientMeeting> meetingJoin = root.join("clientMeeting", JoinType.LEFT);
            Join<ClientMeeting, Client> clientJoin = meetingJoin.join("client", JoinType.LEFT);
            Join<ClientMeeting, User> employeeJoin = meetingJoin.join("assignedEmployee", JoinType.LEFT);

            // Server-side ownership scoping for non-elevated callers (RM / SC)
            if (scopedEmployeeId != null) {
                predicates.add(cb.equal(employeeJoin.get("id"), scopedEmployeeId));
            }

            if (request != null) {
                // 2. Verification Status (PENDING, VERIFIED, REJECTED)
                if (request.getVerificationStatus() != null) {
                    predicates.add(cb.equal(root.get("verificationStatus"), request.getVerificationStatus()));
                }

                // 3. Date Range (applied to ClientMeeting.meetingDate)
                if (request.getFromDate() != null && request.getToDate() != null) {
                    predicates.add(cb.between(meetingJoin.get("meetingDate"), request.getFromDate(), request.getToDate()));
                } else if (request.getFromDate() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(meetingJoin.get("meetingDate"), request.getFromDate()));
                } else if (request.getToDate() != null) {
                    predicates.add(cb.lessThanOrEqualTo(meetingJoin.get("meetingDate"), request.getToDate()));
                }

                // 4. Meeting Code
                if (StringUtils.hasText(request.getMeetingCode())) {
                    String pattern = "%" + request.getMeetingCode().trim().toLowerCase() + "%";
                    predicates.add(cb.like(cb.lower(meetingJoin.get("meetingCode")), pattern));
                }

                // 5. Client Name
                if (StringUtils.hasText(request.getClientName())) {
                    String pattern = "%" + request.getClientName().trim().toLowerCase() + "%";
                    predicates.add(cb.like(cb.lower(clientJoin.get("clientName")), pattern));
                }

                // 6. Client Mobile Number
                if (StringUtils.hasText(request.getMobileNumber())) {
                    String pattern = "%" + request.getMobileNumber().trim() + "%";
                    predicates.add(cb.like(clientJoin.get("mobileNumber"), pattern));
                }

                // 7. Employee Name
                if (StringUtils.hasText(request.getEmployeeName())) {
                    String empPattern = "%" + request.getEmployeeName().trim().toLowerCase() + "%";
                    Predicate firstMatch = cb.like(cb.lower(employeeJoin.get("firstName")), empPattern);
                    Predicate lastMatch = cb.like(cb.lower(employeeJoin.get("lastName")), empPattern);
                    Expression<String> concatName = cb.concat(cb.concat(cb.lower(employeeJoin.get("firstName")), " "), cb.lower(employeeJoin.get("lastName")));
                    Predicate fullMatch = cb.like(concatName, empPattern);
                    predicates.add(cb.or(firstMatch, lastMatch, fullMatch));
                }

                // 8. Employee Code
                if (StringUtils.hasText(request.getEmployeeCode())) {
                    predicates.add(cb.equal(cb.lower(cb.trim(employeeJoin.get("employeeCode"))), request.getEmployeeCode().trim().toLowerCase()));
                }

                // 9. Employee Role (RM, SC)
                if (request.getEmployeeRole() != null) {
                    if (request.getEmployeeRole() == SalesRole.SM) {
                        // By business rule: SM has NO Client Meetings
                        predicates.add(cb.disjunction());
                    } else if (request.getEmployeeRole() == SalesRole.RM) {
                        Join<User, Role> roleJoin = employeeJoin.join("role", JoinType.LEFT);
                        Join<User, Designation> designationJoin = employeeJoin.join("designation", JoinType.LEFT);
                        Predicate rmRole = cb.or(
                                cb.equal(cb.upper(roleJoin.get("code")), "RELATIONSHIP_MANAGER"),
                                cb.equal(cb.upper(roleJoin.get("code")), "ROLE_RELATIONSHIP_MANAGER")
                        );
                        Predicate rmDesig = cb.equal(cb.upper(designationJoin.get("code")), "RM");
                        predicates.add(cb.or(rmRole, rmDesig));
                    } else if (request.getEmployeeRole() == SalesRole.SC) {
                        Join<User, Role> roleJoin = employeeJoin.join("role", JoinType.LEFT);
                        Join<User, Designation> designationJoin = employeeJoin.join("designation", JoinType.LEFT);
                        Predicate scRole = cb.or(
                                cb.equal(cb.upper(roleJoin.get("code")), "SALES_COORDINATOR"),
                                cb.equal(cb.upper(roleJoin.get("code")), "ROLE_SALES_COORDINATOR"),
                                cb.equal(cb.upper(roleJoin.get("code")), "PC_COORDINATOR"),
                                cb.equal(cb.upper(roleJoin.get("code")), "ROLE_PC_COORDINATOR")
                        );
                        Predicate scDesig = cb.or(
                                cb.equal(cb.upper(designationJoin.get("code")), "SC"),
                                cb.equal(cb.upper(designationJoin.get("code")), "PC")
                        );
                        predicates.add(cb.or(scRole, scDesig));
                    } else {
                        predicates.add(cb.disjunction());
                    }
                }

                // 10. Business Generated (YES / NO)
                if (StringUtils.hasText(request.getBusinessGenerated())) {
                    String bgVal = request.getBusinessGenerated().trim().toUpperCase();
                    Predicate verifBg = cb.equal(cb.upper(root.get("businessGenerated")), bgVal);
                    Predicate meetingBg = cb.equal(cb.upper(meetingJoin.get("businessGenerated")), bgVal);
                    predicates.add(cb.or(verifBg, cb.and(cb.isNull(root.get("businessGenerated")), meetingBg)));
                }

                // 11. Business Type (using EXISTS subquery to prevent Cartesian duplicate rows)
                if (StringUtils.hasText(request.getBusinessType())) {
                    String rawType = request.getBusinessType().trim();
                    String normalizedType = PcBusinessType.normalize(rawType);
                    String rawLower = rawType.toLowerCase();

                    Subquery<Long> itemSubquery = query.subquery(Long.class);
                    Root<ClientMeetingVerificationBusinessItem> itemRoot = itemSubquery.from(ClientMeetingVerificationBusinessItem.class);
                    itemSubquery.select(itemRoot.get("id"));

                    Predicate correlation = cb.equal(itemRoot.get("verification"), root);
                    Predicate typeMatch = cb.or(
                            cb.equal(cb.upper(itemRoot.get("businessType")), normalizedType.toUpperCase()),
                            cb.like(cb.lower(itemRoot.get("businessType")), "%" + rawLower + "%")
                    );
                    itemSubquery.where(cb.and(correlation, typeMatch));

                    String likePattern = "%" + rawLower + "%";
                    Predicate legacyVerifTypes = cb.like(cb.lower(root.get("businessTypes")), likePattern);
                    Predicate legacyVerifInvest = cb.like(cb.lower(root.get("investmentType")), likePattern);
                    Predicate legacyMeetingTypes = cb.like(cb.lower(meetingJoin.get("businessTypes")), likePattern);

                    predicates.add(cb.or(cb.exists(itemSubquery), legacyVerifTypes, legacyVerifInvest, legacyMeetingTypes));
                }

                // 12. General Keyword Filter
                if (StringUtils.hasText(request.getKeyword())) {
                    String kwPattern = "%" + request.getKeyword().trim().toLowerCase() + "%";
                    Predicate kMeetingCode = cb.like(cb.lower(meetingJoin.get("meetingCode")), kwPattern);
                    Predicate kClientName = cb.like(cb.lower(clientJoin.get("clientName")), kwPattern);
                    Predicate kClientMobile = cb.like(clientJoin.get("mobileNumber"), kwPattern);
                    Predicate kEmpCode = cb.like(cb.lower(employeeJoin.get("employeeCode")), kwPattern);
                    Predicate kEmpFirst = cb.like(cb.lower(employeeJoin.get("firstName")), kwPattern);
                    Predicate kEmpLast = cb.like(cb.lower(employeeJoin.get("lastName")), kwPattern);
                    Expression<String> concatName = cb.concat(cb.concat(cb.lower(employeeJoin.get("firstName")), " "), cb.lower(employeeJoin.get("lastName")));
                    Predicate kEmpFull = cb.like(concatName, kwPattern);

                    predicates.add(cb.or(kMeetingCode, kClientName, kClientMobile, kEmpCode, kEmpFirst, kEmpLast, kEmpFull));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

package com.blueant_crm_erp.user.entity;

import com.blueant_crm_erp.common.base.BaseSoftDeleteEntity;
import com.blueant_crm_erp.common.enums.Gender;
import com.blueant_crm_erp.common.enums.Status;
import com.blueant_crm_erp.role.entity.Role;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * =============================================================================
 * User Entity
 * =============================================================================
 * Project : BlueAnt CRM ERP Platform
 * Module  : User Management
 */
@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_employee_code", columnNames = "employee_code"),
                @UniqueConstraint(name = "uk_user_email", columnNames = "email"),
                @UniqueConstraint(name = "uk_user_mobile", columnNames = "mobile_number")
        },
        indexes = {
                @Index(name = "idx_user_employee_code", columnList = "employee_code"),
                @Index(name = "idx_user_email", columnList = "email"),
                @Index(name = "idx_user_mobile", columnList = "mobile_number"),
                @Index(name = "idx_user_status", columnList = "status"),
                @Index(name = "idx_user_account_enabled", columnList = "account_enabled"),
                @Index(name = "idx_user_account_locked", columnList = "account_locked"),
                @Index(name = "idx_user_reporting_manager", columnList = "reporting_manager_id"),
                @Index(name = "idx_user_role", columnList = "role_id"),
                @Index(name = "idx_user_department", columnList = "department_id"),
                @Index(name = "idx_user_designation", columnList = "designation_id"),
                @Index(name = "idx_user_team", columnList = "team_id")
        }
)
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class User extends BaseSoftDeleteEntity {

    @Column(name = "employee_code", nullable = false, unique = true, length = 30)
    private String employeeCode;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "mobile_number", nullable = false, unique = true, length = 20)
    private String mobileNumber;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(name = "profile_image", length = 1000)
    private String profileImage;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false, length = 20)
    private Gender gender;

    @Column(name = "joining_date")
    private LocalDate joiningDate;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Status status = Status.ACTIVE;

    @Column(length = 500)
    private String remarks;

    // --- Relationships ---

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false, foreignKey = @ForeignKey(name = "fk_user_role"))
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false, foreignKey = @ForeignKey(name = "fk_user_department"))
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "designation_id", nullable = false, foreignKey = @ForeignKey(name = "fk_user_designation"))
    private Designation designation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false, foreignKey = @ForeignKey(name = "fk_user_team"))
    private Team team;

    // --- Security Fields (Using @Builder.Default) ---

    @Builder.Default
    @Column(name = "account_enabled", nullable = false)
    private Boolean accountEnabled = true;

    @Builder.Default
    @Column(name = "first_login", nullable = false)
    private Boolean firstLogin = true;

    @Builder.Default
    @Column(name = "account_locked", nullable = false)
    private Boolean accountLocked = false;

    @Builder.Default
    @Column(name = "account_non_expired", nullable = false)
    private Boolean accountNonExpired = true;

    @Builder.Default
    @Column(name = "credentials_non_expired", nullable = false)
    private Boolean credentialsNonExpired = true;

    @Builder.Default
    @Column(name = "email_verified", nullable = false)
    private Boolean emailVerified = false;

    @Builder.Default
    @Column(name = "mobile_verified", nullable = false)
    private Boolean mobileVerified = false;

    @Builder.Default
    @Column(name = "failed_login_attempts", nullable = false)
    private Integer failedLoginAttempts = 0;

    // --- Audit Fields ---

    private LocalDateTime lastLoginAt;
    private LocalDateTime lastLogoutAt;
    private LocalDateTime passwordChangedAt;
    private LocalDateTime passwordResetAt;
    private LocalDateTime passwordExpiryAt;
    private String lastLoginIp;
    private String lastLoginDevice;




    // --- Reporting ---

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporting_manager_id", foreignKey = @ForeignKey(name = "fk_user_reporting_manager"))
    private User reportingManager;

    @Builder.Default
    @OneToMany(mappedBy = "reportingManager", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<User> reportingUsers = new ArrayList<>();

    // --- Helper Methods ---

    public String getFullName() {
        return firstName + (lastName == null || lastName.isBlank() ? "" : " " + lastName);
    }

    public boolean isBusinessHead() { return isAtLevel(1); }
    public boolean isSalesManager() { return isAtLevel(2); }
    public boolean isTeamLeader() { return isAtLevel(3); }
    public boolean isSalesPerson() { return isAtLevel(4); }

    private boolean isAtLevel(Integer level) {
        return designation != null && level.equals(designation.getHierarchyLevel());
    }

    public boolean isLocked() { return Boolean.TRUE.equals(accountLocked); }
    public boolean isAccountEnabled() { return Boolean.TRUE.equals(accountEnabled); }
    public boolean isFirstLogin() { return Boolean.TRUE.equals(firstLogin); }

    public boolean isActive() {
        return Status.ACTIVE.equals(status)
                && Boolean.TRUE.equals(accountEnabled)
                && Boolean.FALSE.equals(accountLocked)
                && Boolean.FALSE.equals(getDeleted());
    }

}
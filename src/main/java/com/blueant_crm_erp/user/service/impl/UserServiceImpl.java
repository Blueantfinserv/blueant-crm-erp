package com.blueant_crm_erp.user.service.impl;

import com.blueant_crm_erp.common.dto.response.PageResponse;
import com.blueant_crm_erp.common.enums.Gender;
import com.blueant_crm_erp.common.enums.Status;
import com.blueant_crm_erp.exception.common.BadRequestException;
import com.blueant_crm_erp.exception.common.ResourceNotFoundException;
import com.blueant_crm_erp.role.repository.RoleRepository;
import com.blueant_crm_erp.user.dto.request.ChangePasswordRequest;
import com.blueant_crm_erp.user.dto.request.ChangeUserStatusRequest;
import com.blueant_crm_erp.user.dto.request.CreateUserRequest;
import com.blueant_crm_erp.user.dto.request.ResetPasswordRequest;
import com.blueant_crm_erp.user.dto.request.UpdateUserRequest;
import com.blueant_crm_erp.user.dto.request.UserSearchRequest;
import com.blueant_crm_erp.user.dto.response.UserDropdownResponse;
import com.blueant_crm_erp.user.dto.response.UserResponse;
import com.blueant_crm_erp.user.dto.response.UserSummaryResponse;
import com.blueant_crm_erp.user.entity.Designation;
import com.blueant_crm_erp.user.entity.User;
import com.blueant_crm_erp.user.event.UserCreatedEvent;
import com.blueant_crm_erp.user.event.UserDeletedEvent;
import com.blueant_crm_erp.user.event.UserStatusChangedEvent;
import com.blueant_crm_erp.user.event.UserUpdatedEvent;
import com.blueant_crm_erp.user.mapper.UserMapper;
import com.blueant_crm_erp.user.repository.UserRepository;
import com.blueant_crm_erp.user.service.UserService;
import com.blueant_crm_erp.user.specification.UserSpecification;
import com.blueant_crm_erp.user.validator.UserValidator;
import com.blueant_crm_erp.util.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository            userRepository;
    private final UserValidator             userValidator;
    private final UserMapper                userMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final PasswordEncoder           passwordEncoder;
    private final RoleRepository            roleRepository;

    // =========================================================================
    // Create
    // =========================================================================

    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        log.info("Creating user with employeeCode: {}", request.getEmployeeCode());
        userValidator.validateEmployeeCode(request.getEmployeeCode());
        userValidator.validateEmail(request.getEmail());
        userValidator.validateMobile(request.getMobileNumber());

        User user = userMapper.toEntity(request);
        user.setStatus(Status.ACTIVE);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPasswordChangedAt(LocalDateTime.now());
        user.setFailedLoginAttempts(0);
        user.setCredentialsNonExpired(true);
        if (user.getGender() == null) {
            user.setGender(Gender.MALE);
        }

        if (request.getRoleId() != null) {
            userValidator.validateRole(request.getRoleId());
            user.setRole(roleRepository.findById(request.getRoleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Role not found")));
        }
        if (request.getDepartmentId() != null) {
            user.setDepartment(userValidator.validateDepartment(request.getDepartmentId()));
            if (user.getDepartment() != null && !Status.ACTIVE.equals(user.getDepartment().getStatus())) {
                throw new BadRequestException("Cannot assign inactive department.");
            }
        }
        if (request.getDesignationId() != null) {
            user.setDesignation(userValidator.validateDesignation(request.getDesignationId()));
            if (user.getDesignation() != null && !Status.ACTIVE.equals(user.getDesignation().getStatus())) {
                throw new BadRequestException("Cannot assign inactive designation.");
            }
        }
        if (request.getTeamId() != null) {
            user.setTeam(userValidator.validateTeam(request.getTeamId()));
            if (user.getTeam() != null && !Status.ACTIVE.equals(user.getTeam().getStatus())) {
                throw new BadRequestException("Cannot assign inactive team.");
            }
        }
        if (request.getReportingManagerId() != null) {
            User manager = userValidator.validateReportingManager(request.getReportingManagerId());
            validateHierarchy(user.getDesignation(), manager.getDesignation());
            user.setReportingManager(manager);
        }

        user = userRepository.save(user);
        eventPublisher.publishEvent(new UserCreatedEvent(this, user, null));
        log.info("Successfully created user with id: {}", user.getId());
        return userMapper.toResponse(user);
    }

    // =========================================================================
    // Update
    // =========================================================================

    @Override
    @Transactional
    public UserResponse updateUser(Long userId, UpdateUserRequest request) {
        log.info("Updating user with id: {}", userId);
        User user = userValidator.validateUser(userId);

        if (!user.getEmail().equalsIgnoreCase(request.getEmail())) {
            userValidator.validateEmail(request.getEmail());
        }
        if (!user.getMobileNumber().equalsIgnoreCase(request.getMobileNumber())) {
            userValidator.validateMobile(request.getMobileNumber());
        }

        userMapper.updateEntity(request, user);

        if (request.getRoleId() != null) {
            userValidator.validateRole(request.getRoleId());
            user.setRole(roleRepository.findById(request.getRoleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Role not found")));
        }
        if (request.getDepartmentId() != null) {
            user.setDepartment(userValidator.validateDepartment(request.getDepartmentId()));
            if (user.getDepartment() != null && !Status.ACTIVE.equals(user.getDepartment().getStatus())) {
                throw new BadRequestException("Cannot assign inactive department.");
            }
        }
        if (request.getDesignationId() != null) {
            user.setDesignation(userValidator.validateDesignation(request.getDesignationId()));
            if (user.getDesignation() != null && !Status.ACTIVE.equals(user.getDesignation().getStatus())) {
                throw new BadRequestException("Cannot assign inactive designation.");
            }
        }
        if (request.getTeamId() != null) {
            user.setTeam(userValidator.validateTeam(request.getTeamId()));
            if (user.getTeam() != null && !Status.ACTIVE.equals(user.getTeam().getStatus())) {
                throw new BadRequestException("Cannot assign inactive team.");
            }
        }
        if (request.getReportingManagerId() != null) {
            userValidator.validateReportingManager(userId, request.getReportingManagerId());
            User manager = userValidator.validateReportingManager(request.getReportingManagerId());
            validateHierarchy(user.getDesignation(), manager.getDesignation());
            validateCyclicReporting(userId, manager);
            user.setReportingManager(manager);
        }

        user = userRepository.save(user);
        eventPublisher.publishEvent(new UserUpdatedEvent(this, user.getId(), null));
        log.info("Successfully updated user with id: {}", userId);
        return userMapper.toResponse(user);
    }

    // =========================================================================
    // Delete / Restore
    // =========================================================================

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        log.info("Deleting user with id: {}", userId);
        User user = userValidator.validateUser(userId);
        user.markAsDeleted(SecurityUtil.getCurrentUsername());
        userRepository.save(user);
        eventPublisher.publishEvent(new UserDeletedEvent(this, user, null));
        log.info("Successfully deleted user with id: {}", userId);
    }

    @Override
    @Transactional
    public void restoreUser(Long userId) {
        log.info("Restoring user with id: {}", userId);
        User user = userRepository.findByIdAndDeletedTrue(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found."));
        user.restore();
        userRepository.save(user);
        log.info("Successfully restored user with id: {}", userId);
    }

    // =========================================================================
    // Status
    // =========================================================================

    @Override
    @Transactional
    public UserResponse changeUserStatus(Long userId, ChangeUserStatusRequest request) {
        log.info("Changing status of user with id: {} to {}", userId, request.getStatus());
        User user = userValidator.validateUser(userId);
        Status oldStatus = user.getStatus();
        user.setStatus(request.getStatus());
        user = userRepository.save(user);
        eventPublisher.publishEvent(new UserStatusChangedEvent(this, userId, oldStatus, request.getStatus(), null));
        log.info("Successfully changed status of user with id: {}", userId);
        return userMapper.toResponse(user);
    }

    // =========================================================================
    // Query
    // =========================================================================

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long userId) {
        log.info("Fetching user with id: {}", userId);
        User user = userValidator.validateUser(userId);
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserByEmployeeCode(String employeeCode) {
        User user = userRepository.findByEmployeeCodeIgnoreCaseAndDeletedFalse(employeeCode)
            .orElseThrow(() -> new ResourceNotFoundException("User not found."));
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserSummaryResponse> searchUsers(UserSearchRequest request) {
        Sort sort = Sort.by(Sort.Direction.fromString(request.getSortDirection()), request.getSortBy());
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);
        Page<User> page = userRepository.findAll(UserSpecification.search(request), pageable);
        return PageResponse.of(page.map(userMapper::toSummary));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDropdownResponse> getUserDropdown() {
        return userMapper.toDropdownList(
                userRepository.findAllByStatusAndAccountEnabledTrueAndDeletedFalseOrderByFirstNameAsc(Status.ACTIVE));
    }

    // =========================================================================
    // Password Management
    // =========================================================================

    @Override
    @Transactional
    public void resetPassword(Long userId, ResetPasswordRequest request) {
        log.info("Resetting password for user id: {}", userId);
        User user = userValidator.validateUser(userId);
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new BadRequestException("New password cannot be the same as the old password.");
        }
        final LocalDateTime now = LocalDateTime.now();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordResetAt(now);
        user.setPasswordChangedAt(now);
        user.setFailedLoginAttempts(0);
        user.setCredentialsNonExpired(true);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        log.info("Changing password for user id: {}", userId);
        User user = userValidator.validateUser(userId);
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Old password does not match.");
        }
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new BadRequestException("New password cannot be the same as the old password.");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordChangedAt(LocalDateTime.now());
        user.setFailedLoginAttempts(0);
        user.setCredentialsNonExpired(true);
        userRepository.save(user);
    }

    // =========================================================================
    // Existence Checks
    // =========================================================================

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long userId) {
        return userRepository.existsByIdAndDeletedFalse(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmployeeCode(String employeeCode) {
        return userRepository.existsByEmployeeCodeIgnoreCaseAndDeletedFalse(employeeCode);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmailIgnoreCaseAndDeletedFalse(email);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByMobile(String mobile) {
        return userRepository.existsByMobileNumberAndDeletedFalse(mobile);
    }

    // =========================================================================
    // Count
    // =========================================================================

    @Override
    @Transactional(readOnly = true)
    public long countUsers() {
        return userRepository.count();
    }

    /**
     * {@inheritDoc}
     *
     * <p><strong>Bug fix:</strong> The previous implementation called
     * {@code countByDeletedFalse()}, which incorrectly included INACTIVE users.
     * This implementation correctly calls {@code countByStatusAndDeletedFalse(Status.ACTIVE)}.
     */
    @Override
    @Transactional(readOnly = true)
    public long countActiveUsers() {
        return userRepository.countByStatusAndDeletedFalse(Status.ACTIVE);
    }

    // =========================================================================
    // Private Helpers
    // =========================================================================

    /**
     * Validates that the reporting manager's designation hierarchy level is
     * strictly higher than the user's designation hierarchy level.
     * Skipped when either designation or hierarchy level is {@code null}.
     */
    private void validateHierarchy(Designation userDesignation, Designation managerDesignation) {
        if (userDesignation == null || managerDesignation == null) {
            return;
        }
        Integer userLevel    = userDesignation.getHierarchyLevel();
        Integer managerLevel = managerDesignation.getHierarchyLevel();
        if (userLevel != null && managerLevel != null && managerLevel >= userLevel) {
            throw new BadRequestException("Reporting manager must be at a higher hierarchy level.");
        }
    }

    /**
     * Validates that assigning the given manager does not create a cyclic
     * reporting chain (e.g. A → B → A).
     */
    private void validateCyclicReporting(Long userId, User manager) {
        User currentManager = manager;
        while (currentManager != null) {
            if (userId.equals(currentManager.getId())) {
                throw new BadRequestException("Cyclic reporting structure detected.");
            }
            currentManager = currentManager.getReportingManager();
        }
    }
}

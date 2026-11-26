package com.blueant_crm_erp.client.service.impl;

import com.blueant_crm_erp.bootstrap.constant.BootstrapConstants;
import com.blueant_crm_erp.client.dto.ClientBusinessItemDto;
import com.blueant_crm_erp.client.dto.request.ClientMeetingVerificationRequest;
import com.blueant_crm_erp.client.dto.request.ClientMeetingVerificationSearchRequest;
import com.blueant_crm_erp.client.dto.response.ClientMeetingVerificationResponse;
import com.blueant_crm_erp.client.entity.Client;
import com.blueant_crm_erp.client.entity.ClientMeeting;
import com.blueant_crm_erp.client.entity.ClientMeetingVerification;
import com.blueant_crm_erp.client.entity.ClientMeetingVerificationBusinessItem;
import com.blueant_crm_erp.client.enums.PcBusinessType;
import com.blueant_crm_erp.client.repository.ClientMeetingRepository;
import com.blueant_crm_erp.client.repository.ClientMeetingVerificationBusinessItemRepository;
import com.blueant_crm_erp.client.repository.ClientMeetingVerificationRepository;
import com.blueant_crm_erp.client.repository.ClientRepository;
import com.blueant_crm_erp.client.service.ClientVerificationService;
import com.blueant_crm_erp.client.specification.ClientMeetingVerificationSpecification;
import com.blueant_crm_erp.client.validator.ClientValidator;
import com.blueant_crm_erp.common.dto.response.PageResponse;
import com.blueant_crm_erp.exception.common.BadRequestException;
import com.blueant_crm_erp.exception.common.ResourceNotFoundException;
import com.blueant_crm_erp.meeting.enums.MeetingConductStatus;
import com.blueant_crm_erp.meeting.enums.MeetingStatus;
import com.blueant_crm_erp.meeting.enums.SalesRole;
import com.blueant_crm_erp.servicerequest.enums.VerificationStatus;
import com.blueant_crm_erp.user.entity.User;
import com.blueant_crm_erp.user.repository.UserRepository;
import com.blueant_crm_erp.util.meeting.SalesRoleResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientVerificationServiceImpl implements ClientVerificationService {

    private final ClientMeetingRepository clientMeetingRepository;
    private final ClientMeetingVerificationRepository clientMeetingVerificationRepository;
    private final ClientMeetingVerificationBusinessItemRepository clientMeetingVerificationBusinessItemRepository;
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final ClientValidator clientValidator;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ClientMeetingVerificationResponse> getPendingVerifications(Pageable pageable) {
        Page<ClientMeetingVerification> page = clientMeetingVerificationRepository
                .findByVerificationStatusAndIsCurrentTrue(VerificationStatus.PENDING, pageable);

        return PageResponse.of(page.map(this::mapToResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ClientMeetingVerificationResponse> getVerifiedVerifications(Pageable pageable) {
        Page<ClientMeetingVerification> page = clientMeetingVerificationRepository
                .findByVerificationStatusAndIsCurrentTrue(VerificationStatus.VERIFIED, pageable);

        return PageResponse.of(page.map(this::mapToResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ClientMeetingVerificationResponse> searchVerifications(
            ClientMeetingVerificationSearchRequest request,
            Pageable pageable,
            String currentUserIdentifier) {

        log.info("Searching client meeting verifications with filters by user: {}", currentUserIdentifier);

        // 1. Validate date ranges if both provided
        if (request != null && request.getFromDate() != null && request.getToDate() != null) {
            if (request.getFromDate().isAfter(request.getToDate())) {
                throw new BadRequestException("From date cannot be after to date.");
            }
        }

        // 2. Resolve caller user & check elevated access
        User caller = resolveUserOrNull(currentUserIdentifier);
        boolean isElevated = isAdminUser(caller) || isPcUser(caller) || hasElevatedSecurityRole();

        // Business Rule: SM has NO Client Meeting access
        if (isSalesManager(caller)) {
            log.info("Sales Manager {} attempted client meeting verification search. Returning empty result.", currentUserIdentifier);
            return PageResponse.of(new PageImpl<>(Collections.emptyList(), pageable, 0));
        }

        // Enforce server-side ownership scoping for non-elevated callers (RM / SC)
        Long scopedEmployeeId = null;
        if (!isElevated) {
            if (caller != null) {
                scopedEmployeeId = caller.getId();
            } else {
                return PageResponse.of(new PageImpl<>(Collections.emptyList(), pageable, 0));
            }
        }

        // 3. Ensure stable sorting with unique tie-breaker
        Sort sort = pageable.getSort();
        if (sort.isUnsorted()) {
            sort = Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by(Sort.Direction.DESC, "id"));
        } else if (sort.getOrderFor("id") == null) {
            sort = sort.and(Sort.by(Sort.Direction.DESC, "id"));
        }
        Pageable stablePageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

        // 4. Build Specification
        Specification<ClientMeetingVerification> spec = ClientMeetingVerificationSpecification.build(request, scopedEmployeeId);

        // 5. Query page
        Page<ClientMeetingVerification> page = clientMeetingVerificationRepository.findAll(spec, stablePageable);
        if (page.isEmpty()) {
            return PageResponse.of(new PageImpl<>(Collections.emptyList(), stablePageable, page.getTotalElements()));
        }

        // 6. Batch load business items for current page to eliminate N+1 queries
        List<Long> verifIds = page.getContent().stream().map(ClientMeetingVerification::getId).toList();
        List<ClientMeetingVerificationBusinessItem> items =
                clientMeetingVerificationBusinessItemRepository.findByVerificationIdIn(verifIds);

        Map<Long, List<ClientBusinessItemDto>> itemsByVerifId = items.stream()
                .collect(Collectors.groupingBy(
                        item -> item.getVerification().getId(),
                        Collectors.mapping(
                                item -> ClientBusinessItemDto.builder()
                                        .businessType(item.getBusinessType())
                                        .displayName(PcBusinessType.getDisplayName(item.getBusinessType()))
                                        .amount(item.getAmount())
                                        .build(),
                                Collectors.toList()
                        )
                ));

        // 7. Map to enriched response DTOs
        List<ClientMeetingVerificationResponse> responses = page.getContent().stream()
                .map(v -> mapToResponse(v, itemsByVerifId.get(v.getId())))
                .toList();

        return PageResponse.of(new PageImpl<>(responses, stablePageable, page.getTotalElements()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClientMeetingVerificationResponse> getVerificationHistory(String meetingCode) {
        ClientMeeting meeting = clientMeetingRepository.findByMeetingCode(meetingCode)
                .orElseThrow(() -> new ResourceNotFoundException("Client meeting not found: " + meetingCode));

        return clientMeetingVerificationRepository.findByClientMeetingIdOrderByAttemptNumberAsc(meeting.getId()).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional
    public ClientMeetingVerificationResponse verifyMeeting(
            String meetingCode, ClientMeetingVerificationRequest request, String currentUserIdentifier) {

        log.info("PC verifying client meeting: {}, by: {}", meetingCode, currentUserIdentifier);

        ClientMeeting meeting = clientMeetingRepository.findByMeetingCode(meetingCode)
                .orElseThrow(() -> new ResourceNotFoundException("Client meeting not found: " + meetingCode));

        ClientMeetingVerification verification = clientMeetingVerificationRepository
                .findByClientMeetingIdAndIsCurrentTrue(meeting.getId())
                .orElseThrow(() -> new BadRequestException("No active verification found for meeting: " + meetingCode));

        if (verification.getVerificationStatus() != VerificationStatus.PENDING) {
            throw new BadRequestException("Meeting verification is not in PENDING state. Current status: " + verification.getVerificationStatus());
        }

        // Validate conditional PC questions based on Conducted vs Not Conducted & Business Generated YES/NO
        clientValidator.validateVerification(meeting, request);

        boolean isConducted = meeting.getMeetingConducted() == MeetingConductStatus.CONDUCTED;

        verification.setVerificationStatus(VerificationStatus.VERIFIED);
        verification.setVerifiedBy(currentUserIdentifier);
        verification.setVerifiedAt(LocalDateTime.now());
        verification.setRejectionReason(null);

        if (isConducted) {
            verification.setNewMeetingDate(request.getNewMeetingDate());
            verification.setMeetingTime(request.getMeetingTime());
            verification.setBlueantAppUsed(request.getBlueantAppUsed() != null ? request.getBlueantAppUsed().trim().toUpperCase() : null);
            String aw = request.getAloneWith() != null ? request.getAloneWith().trim().toUpperCase() : null;
            verification.setAloneWith(aw);
            if ("SELF".equals(aw)) {
                verification.setPersonName(null);
                verification.setPosition(null);
            } else {
                verification.setPersonName(request.getPersonName() != null ? request.getPersonName().trim() : null);
                verification.setPosition(request.getPosition() != null ? request.getPosition().trim().toUpperCase() : null);
            }
            verification.setRemarks(request.getRemarks());
            verification.setNextFollowupDate(request.getNextFollowupDate());

            boolean bgYes = "YES".equalsIgnoreCase(meeting.getBusinessGenerated()) || "YES".equalsIgnoreCase(request.getBusinessGenerated());
            if (bgYes) {
                List<ClientMeetingVerificationBusinessItem> itemsToPersist = new ArrayList<>();
                List<String> distinctTypes = new ArrayList<>();
                BigDecimal totalMonetaryAmount = BigDecimal.ZERO;

                if (request.getBusinessItems() != null && !request.getBusinessItems().isEmpty()) {
                    for (ClientBusinessItemDto itemDto : request.getBusinessItems()) {
                        String normalized = PcBusinessType.normalize(itemDto.getBusinessType());
                        distinctTypes.add(normalized);
                        BigDecimal itemAmount = itemDto.getAmount();
                        if (itemAmount != null && itemAmount.compareTo(BigDecimal.ZERO) > 0) {
                            totalMonetaryAmount = totalMonetaryAmount.add(itemAmount);
                        }
                        itemsToPersist.add(ClientMeetingVerificationBusinessItem.builder()
                                .verification(verification)
                                .businessType(normalized)
                                .amount(itemAmount)
                                .createdAt(LocalDateTime.now())
                                .createdBy(currentUserIdentifier)
                                .build());
                    }
                } else {
                    List<String> rawTypes = new ArrayList<>();
                    if (request.getBusinessTypes() != null && !request.getBusinessTypes().isEmpty()) {
                        rawTypes.addAll(request.getBusinessTypes());
                    } else if (request.getInvestmentType() != null && !request.getInvestmentType().isBlank()) {
                        Arrays.stream(request.getInvestmentType().split(","))
                                .map(String::trim)
                                .filter(s -> !s.isEmpty())
                                .forEach(rawTypes::add);
                    }

                    distinctTypes = rawTypes.stream()
                            .map(PcBusinessType::normalize)
                            .distinct()
                            .collect(Collectors.toList());

                    BigDecimal legacyAmount = request.getInvestmentAmount();
                    if (legacyAmount != null && legacyAmount.compareTo(BigDecimal.ZERO) > 0) {
                        totalMonetaryAmount = legacyAmount;
                    }

                    long monetaryCount = distinctTypes.stream().filter(PcBusinessType::isMonetary).count();
                    for (String normalized : distinctTypes) {
                        boolean isMonetary = PcBusinessType.isMonetary(normalized);
                        BigDecimal itemAmount = (isMonetary && monetaryCount == 1) ? legacyAmount : null;
                        itemsToPersist.add(ClientMeetingVerificationBusinessItem.builder()
                                .verification(verification)
                                .businessType(normalized)
                                .amount(itemAmount)
                                .createdAt(LocalDateTime.now())
                                .createdBy(currentUserIdentifier)
                                .build());
                    }
                }

                verification.getBusinessItems().clear();
                verification.getBusinessItems().addAll(itemsToPersist);

                String businessTypesStr;
                if (request.getBusinessItems() != null && !request.getBusinessItems().isEmpty()) {
                    businessTypesStr = String.join(", ", distinctTypes);
                } else if (request.getBusinessTypes() != null && !request.getBusinessTypes().isEmpty()) {
                    businessTypesStr = String.join(", ", distinctTypes);
                } else if (request.getInvestmentType() != null && !request.getInvestmentType().isBlank()) {
                    businessTypesStr = request.getInvestmentType().trim();
                } else {
                    businessTypesStr = String.join(", ", distinctTypes);
                }

                if (businessTypesStr.length() > 500) {
                    businessTypesStr = businessTypesStr.substring(0, 500);
                }

                String investmentTypeStr = businessTypesStr;
                if (investmentTypeStr.length() > 100) {
                    investmentTypeStr = investmentTypeStr.substring(0, 100);
                }

                verification.setBusinessGenerated("YES");
                verification.setBusinessTypes(businessTypesStr);
                verification.setInvestmentType(investmentTypeStr);
                verification.setInvestmentAmount(totalMonetaryAmount.compareTo(BigDecimal.ZERO) > 0 ? totalMonetaryAmount : null);

                meeting.setBusinessGenerated("YES");
                meeting.setBusinessTypes(businessTypesStr);
            } else {
                verification.getBusinessItems().clear();
                verification.setBusinessGenerated("NO");
                verification.setBusinessTypes(null);
                verification.setInvestmentType(null);
                verification.setInvestmentAmount(null);

                meeting.setBusinessGenerated("NO");
                meeting.setBusinessTypes(null);
            }
        } else {
            LocalDate notConductedDate = request.getMeetingDate() != null ? request.getMeetingDate() : request.getNewMeetingDate();
            verification.getBusinessItems().clear();
            verification.setNewMeetingDate(notConductedDate);
            verification.setMeetingTime(request.getMeetingTime());
            verification.setRemarks(request.getRemarks());
            verification.setNextFollowupDate(request.getNextFollowupDate());
            verification.setBlueantAppUsed(null);
            verification.setAloneWith(null);
            verification.setPersonName(null);
            verification.setPosition(null);
            verification.setBusinessGenerated(null);
            verification.setInvestmentType(null);
            verification.setBusinessTypes(null);
            verification.setInvestmentAmount(null);
        }

        ClientMeetingVerification savedVerif = clientMeetingVerificationRepository.save(verification);

        // Update meeting state
        meeting.setVerifiedByPc(true);
        meeting.setMeetingStatus(isConducted ? MeetingStatus.COMPLETED : MeetingStatus.NOT_CONDUCTED);
        if (isConducted) {
            meeting.setAloneWith(verification.getAloneWith());
            meeting.setPersonName(verification.getPersonName());
            meeting.setPosition(verification.getPosition());
        } else {
            meeting.setAloneWith(null);
            meeting.setPersonName(null);
            meeting.setPosition(null);
        }
        clientMeetingRepository.save(meeting);

        // Update client next follow-up and investment type if verified
        Client client = meeting.getClient();
        if (client != null) {
            if (request.getNextFollowupDate() != null) {
                client.setNextFollowupDate(request.getNextFollowupDate());
            }
            if (StringUtils.hasText(savedVerif.getInvestmentType())) {
                client.setInvestmentType(savedVerif.getInvestmentType());
            }
            clientRepository.save(client);
        }

        log.info("Client meeting {} successfully verified by PC: {}. Attempt #{}",
                meetingCode, currentUserIdentifier, savedVerif.getAttemptNumber());

        return mapToResponse(savedVerif);
    }

    @Override
    @Transactional
    public ClientMeetingVerificationResponse rejectMeeting(
            String meetingCode, String reason, String currentUserIdentifier) {

        log.info("PC rejecting client meeting: {}, reason: {}, by: {}", meetingCode, reason, currentUserIdentifier);

        if (!StringUtils.hasText(reason)) {
            throw new BadRequestException("Rejection reason is required.");
        }

        ClientMeeting meeting = clientMeetingRepository.findByMeetingCode(meetingCode)
                .orElseThrow(() -> new ResourceNotFoundException("Client meeting not found: " + meetingCode));

        ClientMeetingVerification verification = clientMeetingVerificationRepository
                .findByClientMeetingIdAndIsCurrentTrue(meeting.getId())
                .orElseThrow(() -> new BadRequestException("No active verification found for meeting: " + meetingCode));

        if (verification.getVerificationStatus() != VerificationStatus.PENDING) {
            throw new BadRequestException("Meeting verification is not in PENDING state. Current status: " + verification.getVerificationStatus());
        }

        verification.setVerificationStatus(VerificationStatus.REJECTED);
        verification.setRejectionReason(reason.trim());
        verification.setVerifiedBy(currentUserIdentifier);
        verification.setVerifiedAt(LocalDateTime.now());

        ClientMeetingVerification savedVerif = clientMeetingVerificationRepository.save(verification);

        // Mark meeting verifiedByPc as false so RM/SC can correct and resubmit
        meeting.setVerifiedByPc(false);
        clientMeetingRepository.save(meeting);

        log.info("Client meeting {} rejected by PC: {}. Attempt #{}. Historical record preserved.",
                meetingCode, currentUserIdentifier, savedVerif.getAttemptNumber());

        return mapToResponse(savedVerif);
    }

    @Override
    public List<String> getBusinessOptions() {
        return PcBusinessType.DISPLAY_OPTIONS;
    }

    private ClientMeetingVerificationResponse mapToResponse(ClientMeetingVerification v) {
        return mapToResponse(v, null);
    }

    private ClientMeetingVerificationResponse mapToResponse(
            ClientMeetingVerification v, List<ClientBusinessItemDto> preloadedItems) {
        ClientMeeting m = v.getClientMeeting();
        String bg = v.getBusinessGenerated() != null ? v.getBusinessGenerated() : (m != null ? m.getBusinessGenerated() : null);

        List<ClientBusinessItemDto> businessItems;
        if (preloadedItems != null) {
            businessItems = preloadedItems;
        } else if (v.getBusinessItems() != null && !v.getBusinessItems().isEmpty()) {
            businessItems = v.getBusinessItems().stream()
                    .map(item -> ClientBusinessItemDto.builder()
                            .businessType(item.getBusinessType())
                            .displayName(PcBusinessType.getDisplayName(item.getBusinessType()))
                            .amount(item.getAmount())
                            .build())
                    .toList();
        } else if (v.getBusinessTypes() != null || v.getInvestmentType() != null) {
            List<String> types = parseBusinessTypes(v.getBusinessTypes() != null ? v.getBusinessTypes() : v.getInvestmentType());
            long monetaryCount = types.stream().filter(PcBusinessType::isMonetary).count();
            businessItems = types.stream()
                    .map(t -> ClientBusinessItemDto.builder()
                            .businessType(t)
                            .displayName(PcBusinessType.getDisplayName(t))
                            .amount(PcBusinessType.isMonetary(t) && monetaryCount == 1 ? v.getInvestmentAmount() : null)
                            .build())
                    .toList();
        } else {
            businessItems = Collections.emptyList();
        }

        Client client = m != null ? m.getClient() : null;
        User employee = m != null ? m.getAssignedEmployee() : null;
        String employeeName = null;
        if (employee != null) {
            String first = employee.getFirstName() != null ? employee.getFirstName() : "";
            String last = employee.getLastName() != null ? employee.getLastName() : "";
            employeeName = (first + " " + last).trim();
            if (employeeName.isEmpty()) {
                employeeName = null;
            }
        }

        return ClientMeetingVerificationResponse.builder()
                .id(v.getId())
                .clientMeetingId(m != null ? m.getId() : null)
                .meetingCode(m != null ? m.getMeetingCode() : null)
                .attemptNumber(v.getAttemptNumber())
                .isCurrent(v.getIsCurrent())
                .verificationStatus(v.getVerificationStatus() != null ? v.getVerificationStatus().name() : null)
                .verifiedBy(v.getVerifiedBy())
                .verifiedAt(v.getVerifiedAt())
                .rejectionReason(v.getRejectionReason())
                .newMeetingDate(v.getNewMeetingDate())
                .meetingTime(v.getMeetingTime())
                .blueantAppUsed(v.getBlueantAppUsed())
                .aloneWith(v.getAloneWith())
                .personName(v.getPersonName())
                .position(v.getPosition())
                .businessGenerated(bg)
                .investmentType(v.getInvestmentType())
                .businessTypes(parseBusinessTypes(v.getBusinessTypes() != null ? v.getBusinessTypes() : v.getInvestmentType()))
                .businessItems(businessItems)
                .investmentAmount(v.getInvestmentAmount())
                .remarks(v.getRemarks())
                .nextFollowupDate(v.getNextFollowupDate())
                .createdAt(v.getCreatedAt())
                .createdBy(v.getCreatedBy())
                .clientId(client != null ? client.getId() : null)
                .clientCode(client != null ? client.getClientCode() : null)
                .clientName(client != null ? client.getClientName() : null)
                .clientMobileNumber(client != null ? client.getMobileNumber() : null)
                .assignedEmployeeId(employee != null ? employee.getId() : null)
                .assignedEmployeeCode(employee != null ? employee.getEmployeeCode() : null)
                .assignedEmployeeName(employeeName)
                .employeeRole(employee != null ? SalesRoleResolver.resolveRole(employee) : null)
                .meetingNumber(m != null ? m.getMeetingNumber() : null)
                .meetingMode(m != null && m.getMeetingMode() != null ? m.getMeetingMode().name() : null)
                .meetingDate(m != null ? m.getMeetingDate() : null)
                .meetingTimeOfDay(m != null ? m.getMeetingTime() : null)
                .meetingLocation(m != null ? m.getMeetingLocation() : null)
                .meetingConducted(m != null && m.getMeetingConducted() != null ? m.getMeetingConducted().name() : null)
                .clientMeetingStatus(m != null ? m.getClientMeetingStatus() : null)
                .build();
    }

    private User resolveUserOrNull(String identifier) {
        if (!StringUtils.hasText(identifier) || "system".equalsIgnoreCase(identifier)) {
            return null;
        }
        return userRepository.findByEmployeeCodeIgnoreCaseAndDeletedFalse(identifier.trim())
                .orElseGet(() -> userRepository.findByEmailIgnoreCaseAndDeletedFalse(identifier.trim()).orElse(null));
    }

    private boolean isAdminUser(User user) {
        if (user == null || user.getRole() == null || user.getRole().getCode() == null) {
            return false;
        }
        String code = user.getRole().getCode().trim().toUpperCase();
        return code.equals(BootstrapConstants.ROLE_ADMIN) ||
               code.equals("ROLE_" + BootstrapConstants.ROLE_ADMIN) ||
               code.equals("SUPER_ADMIN") ||
               code.equals("ROLE_SUPER_ADMIN");
    }

    private boolean isPcUser(User user) {
        if (user == null) {
            return false;
        }
        String roleCode = user.getRole() != null && user.getRole().getCode() != null
                ? user.getRole().getCode().trim().toUpperCase()
                : null;
        String desigCode = user.getDesignation() != null && user.getDesignation().getCode() != null
                ? user.getDesignation().getCode().trim().toUpperCase()
                : null;

        // Explicit safeguard: SALES_COORDINATOR or SC is NEVER PC
        if (roleCode != null && (roleCode.equals(BootstrapConstants.ROLE_SALES_COORDINATOR) ||
                                 roleCode.equals("ROLE_" + BootstrapConstants.ROLE_SALES_COORDINATOR))) {
            return false;
        }
        if (desigCode != null && desigCode.equals(BootstrapConstants.DESIG_SC)) {
            return false;
        }

        // Strict PC Coordinator role and designation matching
        boolean matchesRole = roleCode != null && (
                roleCode.equals(BootstrapConstants.ROLE_PC_COORDINATOR) ||
                roleCode.equals("ROLE_" + BootstrapConstants.ROLE_PC_COORDINATOR)
        );
        boolean matchesDesig = desigCode != null && desigCode.equals(BootstrapConstants.DESIG_PC);

        return matchesRole || matchesDesig;
    }

    private boolean isSalesManager(User user) {
        if (user == null) {
            return false;
        }
        return SalesRoleResolver.resolveRole(user) == SalesRole.SM;
    }

    private boolean hasElevatedSecurityRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return false;
        }
        return auth.getAuthorities().stream().anyMatch(a -> {
            String authName = a.getAuthority().trim().toUpperCase();
            return authName.equals("ROLE_ADMIN") ||
                   authName.equals("ADMIN") ||
                   authName.equals("ROLE_SUPER_ADMIN") ||
                   authName.equals("SUPER_ADMIN") ||
                   authName.equals("ROLE_" + BootstrapConstants.ROLE_PC_COORDINATOR) ||
                   authName.equals(BootstrapConstants.ROLE_PC_COORDINATOR) ||
                   authName.equals("MEETING_VERIFY");
        });
    }

    private List<String> parseBusinessTypes(String str) {
        if (str == null || str.isBlank()) {
            return List.of();
        }
        return Arrays.stream(str.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}

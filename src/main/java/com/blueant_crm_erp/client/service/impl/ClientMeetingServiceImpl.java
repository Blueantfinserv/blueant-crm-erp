package com.blueant_crm_erp.client.service.impl;

import com.blueant_crm_erp.client.dto.request.ClientMeetingUpdateRequest;
import com.blueant_crm_erp.client.dto.request.CreateClientMeetingRequest;
import com.blueant_crm_erp.client.dto.response.ClientMeetingResponse;
import com.blueant_crm_erp.client.dto.response.ClientMeetingUpdateResponse;
import com.blueant_crm_erp.client.dto.response.ClientWorkFollowResponse;
import com.blueant_crm_erp.client.entity.Client;
import com.blueant_crm_erp.client.entity.ClientMeeting;
import com.blueant_crm_erp.client.entity.ClientMeetingUpdate;
import com.blueant_crm_erp.client.entity.ClientMeetingVerification;
import com.blueant_crm_erp.client.enums.ClientStatus;
import com.blueant_crm_erp.client.repository.ClientMeetingRepository;
import com.blueant_crm_erp.client.repository.ClientMeetingUpdateRepository;
import com.blueant_crm_erp.client.repository.ClientMeetingVerificationRepository;
import com.blueant_crm_erp.client.repository.ClientRepository;
import com.blueant_crm_erp.client.service.ClientMeetingCodeGeneratorService;
import com.blueant_crm_erp.client.service.ClientMeetingService;
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
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientMeetingServiceImpl implements ClientMeetingService {

    private final ClientRepository clientRepository;
    private final ClientMeetingRepository clientMeetingRepository;
    private final ClientMeetingUpdateRepository clientMeetingUpdateRepository;
    private final ClientMeetingVerificationRepository clientMeetingVerificationRepository;
    private final UserRepository userRepository;
    private final ClientValidator clientValidator;
    private final ClientMeetingCodeGeneratorService clientMeetingCodeGeneratorService;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ClientWorkFollowResponse> getClientWorkFollow(
            String currentUserIdentifier, Pageable pageable, String search, String status, String salesPersonCode) {

        User currentUser = resolveUserOrThrow(currentUserIdentifier);
        boolean isAdmin = isAdminUser(currentUser);

        Long filterUserId;
        if (isAdmin) {
            if (StringUtils.hasText(salesPersonCode)) {
                User targetUser = userRepository.findByEmployeeCodeIgnoreCaseAndDeletedFalse(salesPersonCode.trim())
                        .orElseThrow(() -> new ResourceNotFoundException("User not found with code: " + salesPersonCode));
                filterUserId = targetUser.getId();
            } else {
                filterUserId = null; // Admin can see all
            }
        } else {
            // Strictly scoped to logged-in user
            filterUserId = currentUser.getId();
        }

        Specification<Client> spec = (root, query, cb) -> {
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch("salesPerson", jakarta.persistence.criteria.JoinType.LEFT);
            }
            List<Predicate> predicates = new ArrayList<>();

            if (filterUserId != null) {
                predicates.add(cb.equal(root.get("salesPerson").get("id"), filterUserId));
            }

            if (StringUtils.hasText(status)) {
                try {
                    ClientStatus clientStatus = ClientStatus.valueOf(status.trim().toUpperCase());
                    predicates.add(cb.equal(root.get("clientStatus"), clientStatus));
                } catch (IllegalArgumentException ignored) {}
            }

            if (StringUtils.hasText(search)) {
                String pattern = "%" + search.trim().toLowerCase() + "%";
                Predicate nameMatch = cb.like(cb.lower(root.get("clientName")), pattern);
                Predicate codeMatch = cb.like(cb.lower(root.get("clientCode")), pattern);
                Predicate mobileMatch = cb.like(root.get("mobileNumber"), pattern);
                predicates.add(cb.or(nameMatch, codeMatch, mobileMatch));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Client> page = clientRepository.findAll(spec, pageable);
        List<Client> clients = page.getContent();
        if (clients.isEmpty()) {
            return PageResponse.of(new org.springframework.data.domain.PageImpl<>(
                    java.util.Collections.emptyList(), pageable, page.getTotalElements()));
        }

        List<Long> clientIds = clients.stream().map(Client::getId).toList();
        List<ClientMeeting> activeMeetings = clientMeetingRepository
                .findByClientIdInAndMeetingStatus(clientIds, MeetingStatus.SCHEDULED);

        java.util.Map<Long, ClientMeeting> activeMeetingsByClientId = activeMeetings.stream()
                .collect(java.util.stream.Collectors.toMap(
                        m -> m.getClient().getId(),
                        java.util.function.Function.identity(),
                        (m1, m2) -> (m1.getMeetingNumber() != null && m2.getMeetingNumber() != null && m1.getMeetingNumber() > m2.getMeetingNumber()) ? m1 : m2
                ));

        List<ClientWorkFollowResponse> responses = clients.stream()
                .map(client -> mapToWorkFollowResponse(client, activeMeetingsByClientId.get(client.getId())))
                .toList();

        return PageResponse.of(new org.springframework.data.domain.PageImpl<>(
                responses, pageable, page.getTotalElements()));
    }

    private ClientWorkFollowResponse mapToWorkFollowResponse(Client client, ClientMeeting activeMeeting) {
        User sp = client.getSalesPerson();

        return ClientWorkFollowResponse.builder()
                .clientId(client.getId())
                .clientCode(client.getClientCode())
                .clientName(client.getClientName())
                .mobileNumber(client.getMobileNumber())
                .alternateMobileNumber(client.getAlternateMobileNumber())
                .email(client.getEmail())
                .speciality(client.getSpeciality())
                .location(client.getLocation())
                .clinicAddress(client.getClinicAddress())
                .clientStatus(client.getClientStatus() != null ? client.getClientStatus().name() : null)
                .assignmentDate(client.getAssignmentDate())
                .bestTimeToMeet(client.getBestTimeToMeet())
                .assignedSalesPersonId(sp != null ? sp.getId() : null)
                .assignedSalesPersonCode(sp != null ? sp.getEmployeeCode() : null)
                .assignedSalesPersonName(sp != null ? sp.getFullName() : null)
                .activeMeetingCode(activeMeeting != null ? activeMeeting.getMeetingCode() : null)
                .activeMeetingStatus(activeMeeting != null && activeMeeting.getMeetingStatus() != null ? activeMeeting.getMeetingStatus().name() : null)
                .activeMeetingDate(activeMeeting != null ? activeMeeting.getMeetingDate() : null)
                .activeMeetingTime(activeMeeting != null ? activeMeeting.getMeetingTime() : null)
                .activeMeetingNumber(activeMeeting != null ? activeMeeting.getMeetingNumber() : null)
                .build();
    }

    @Override
    @Transactional
    public ClientMeetingResponse scheduleMeeting(CreateClientMeetingRequest request, String currentUserIdentifier) {
        log.info("Scheduling client meeting for client: {}, by: {}", request.getClientCode(), currentUserIdentifier);

        User currentUser = resolveUserOrThrow(currentUserIdentifier);
        boolean isAdmin = isAdminUser(currentUser);

        Client client = resolveClient(request.getClientId(), request.getClientCode());

        // Validate client ownership
        if (!isAdmin && (client.getSalesPerson() == null || !client.getSalesPerson().getId().equals(currentUser.getId()))) {
            throw new AccessDeniedException("You are not authorized to schedule meetings for client: " + client.getClientCode());
        }

        // Validate meeting date
        if (request.getMeetingDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("Meeting date cannot be in the past.");
        }

        // Acquire pessimistic write lock on Client row to strictly serialize concurrent scheduling requests per client
        Client lockedClient = clientRepository.findByIdWithPessimisticLock(client.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + client.getId()));

        // Prevent duplicate active scheduled meetings under lock
        if (clientMeetingRepository.existsByClientIdAndMeetingStatus(lockedClient.getId(), MeetingStatus.SCHEDULED)) {
            throw new BadRequestException("An active scheduled meeting already exists for client: " + lockedClient.getClientCode());
        }

        int nextSeq = clientMeetingRepository.findTopByClientIdOrderByMeetingNumberDesc(lockedClient.getId())
                .map(m -> m.getMeetingNumber() + 1)
                .orElse(1);

        String meetingCode = clientMeetingCodeGeneratorService.generateNextMeetingCode();

        User assignedEmployee = lockedClient.getSalesPerson() != null ? lockedClient.getSalesPerson() : currentUser;

        ClientMeeting meeting = ClientMeeting.builder()
                .meetingCode(meetingCode)
                .meetingNumber(nextSeq)
                .client(lockedClient)
                .assignedEmployee(assignedEmployee)
                .meetingMode(request.getMeetingMode())
                .meetingDate(request.getMeetingDate())
                .meetingTime(request.getMeetingTime())
                .meetingLocation(request.getMeetingLocation())
                .remarks(request.getRemarks())
                .meetingStatus(MeetingStatus.SCHEDULED)
                .meetingConducted(MeetingConductStatus.NOT_CONDUCTED)
                .verifiedByPc(false)
                .build();

        ClientMeeting savedMeeting = clientMeetingRepository.save(meeting);
        log.info("Client meeting scheduled successfully: {}, sequence: #{}", meetingCode, nextSeq);

        return mapToMeetingResponse(savedMeeting, null);
    }

    @Override
    @Transactional
    public ClientMeetingResponse updateMeeting(String meetingCode, ClientMeetingUpdateRequest request, String currentUserIdentifier) {
        log.info("Updating client meeting: {}, conducted: {}, by: {}", meetingCode, request.getMeetingConducted(), currentUserIdentifier);

        User currentUser = resolveUserOrThrow(currentUserIdentifier);
        boolean isAdmin = isAdminUser(currentUser);

        ClientMeeting meeting = clientMeetingRepository.findByMeetingCode(meetingCode)
                .orElseThrow(() -> new ResourceNotFoundException("Client meeting not found with code: " + meetingCode));

        // Validate ownership
        if (!isAdmin && (meeting.getAssignedEmployee() == null || !meeting.getAssignedEmployee().getId().equals(currentUser.getId()))) {
            throw new AccessDeniedException("You are not authorized to update meeting: " + meetingCode);
        }

        // Check state transitions: only SCHEDULED or REJECTED meetings can be updated/submitted
        if (meeting.getMeetingStatus() == MeetingStatus.COMPLETED && !Boolean.TRUE.equals(meeting.getVerifiedByPc())) {
            // Check if there is a pending verification
            Optional<ClientMeetingVerification> currentVerif = clientMeetingVerificationRepository
                    .findByClientMeetingIdAndIsCurrentTrue(meeting.getId());
            if (currentVerif.isPresent() && currentVerif.get().getVerificationStatus() == VerificationStatus.PENDING) {
                throw new BadRequestException("Meeting update is already submitted and pending PC verification.");
            }
        }

        SalesRole role = SalesRoleResolver.resolveRole(meeting.getAssignedEmployee());
        clientValidator.validateMeetingUpdate(meeting, request, role);

        boolean isConducted = request.getMeetingConducted() == MeetingConductStatus.CONDUCTED;
        MeetingStatus newStatus = isConducted ? MeetingStatus.COMPLETED : MeetingStatus.NOT_CONDUCTED;

        meeting.setMeetingConducted(request.getMeetingConducted());
        meeting.setMeetingStatus(newStatus);
        if (request.getMeetingMode() != null) {
            meeting.setMeetingMode(request.getMeetingMode());
        }
        meeting.setMeetingDate(request.getMeetingDate());
        if (request.getMeetingTime() != null) {
            meeting.setMeetingTime(request.getMeetingTime());
        }
        meeting.setClientMeetingStatus(request.getClientMeetingStatus());
        meeting.setAloneWith(request.getAloneWith());
        meeting.setPersonName(request.getPersonName());
        meeting.setPosition(request.getPosition());
        meeting.setBusinessGenerated(request.getBusinessGenerated());
        meeting.setRemarks(request.getRemarks());
        meeting.setNextPlanDate(request.getNextPlanDate());
        meeting.setNextPlanTime(request.getNextPlanTime());
        meeting.setLatitude(request.getLatitude());
        meeting.setLongitude(request.getLongitude());
        meeting.setLocationAccuracy(request.getAccuracy());
        meeting.setLocationCapturedAt(LocalDateTime.now());
        meeting.setVerifiedByPc(false);

        ClientMeeting savedMeeting = clientMeetingRepository.save(meeting);

        // Record immutable Meeting Update audit entry
        int nextUpdateNum = clientMeetingUpdateRepository.countByClientMeetingId(savedMeeting.getId()) + 1;
        ClientMeetingUpdate updateAudit = ClientMeetingUpdate.builder()
                .clientMeeting(savedMeeting)
                .updateNumber(nextUpdateNum)
                .meetingMode(savedMeeting.getMeetingMode())
                .meetingDate(savedMeeting.getMeetingDate())
                .meetingTime(savedMeeting.getMeetingTime())
                .meetingConducted(savedMeeting.getMeetingConducted())
                .clientMeetingStatus(savedMeeting.getClientMeetingStatus())
                .aloneWith(savedMeeting.getAloneWith())
                .personName(savedMeeting.getPersonName())
                .position(savedMeeting.getPosition())
                .businessGenerated(savedMeeting.getBusinessGenerated())
                .remarks(savedMeeting.getRemarks())
                .nextPlanDate(savedMeeting.getNextPlanDate())
                .nextPlanTime(savedMeeting.getNextPlanTime())
                .latitude(savedMeeting.getLatitude())
                .longitude(savedMeeting.getLongitude())
                .locationAccuracy(savedMeeting.getLocationAccuracy())
                .build();
        clientMeetingUpdateRepository.save(updateAudit);

        // Handle verification attempt lifecycle (Immutable history)
        Optional<ClientMeetingVerification> currentVerifOpt = clientMeetingVerificationRepository
                .findByClientMeetingIdAndIsCurrentTrue(savedMeeting.getId());

        int attemptNumber = 1;
        if (currentVerifOpt.isPresent()) {
            ClientMeetingVerification prevVerif = currentVerifOpt.get();
            prevVerif.setIsCurrent(false);
            clientMeetingVerificationRepository.save(prevVerif);
            attemptNumber = prevVerif.getAttemptNumber() + 1;
        }

        ClientMeetingVerification newAttempt = ClientMeetingVerification.builder()
                .clientMeeting(savedMeeting)
                .attemptNumber(attemptNumber)
                .isCurrent(true)
                .verificationStatus(VerificationStatus.PENDING)
                .remarks(null)
                .build();
        ClientMeetingVerification savedVerif = clientMeetingVerificationRepository.save(newAttempt);

        log.info("Client meeting updated and submitted for PC verification. Meeting: {}, Attempt: #{}",
                meetingCode, attemptNumber);

        return mapToMeetingResponse(savedMeeting, savedVerif.getVerificationStatus().name());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClientMeetingUpdateResponse> getMeetingUpdateHistory(String meetingCode, String currentUserIdentifier) {
        ClientMeeting meeting = clientMeetingRepository.findByMeetingCode(meetingCode)
                .orElseThrow(() -> new ResourceNotFoundException("Client meeting not found: " + meetingCode));

        return clientMeetingUpdateRepository.findByClientMeetingIdOrderByUpdateNumberAsc(meeting.getId()).stream()
                .map(u -> ClientMeetingUpdateResponse.builder()
                        .id(u.getId())
                        .clientMeetingId(meeting.getId())
                        .updateNumber(u.getUpdateNumber())
                        .meetingMode(u.getMeetingMode() != null ? u.getMeetingMode().name() : null)
                        .meetingDate(u.getMeetingDate())
                        .meetingTime(u.getMeetingTime())
                        .meetingConducted(u.getMeetingConducted() != null ? u.getMeetingConducted().name() : null)
                        .clientMeetingStatus(u.getClientMeetingStatus())
                        .aloneWith(u.getAloneWith())
                        .personName(u.getPersonName())
                        .position(u.getPosition())
                        .businessGenerated(u.getBusinessGenerated())
                        .remarks(u.getRemarks())
                        .nextPlanDate(u.getNextPlanDate())
                        .nextPlanTime(u.getNextPlanTime())
                        .latitude(u.getLatitude())
                        .longitude(u.getLongitude())
                        .locationAccuracy(u.getLocationAccuracy())
                        .createdAt(u.getCreatedAt())
                        .createdBy(u.getCreatedBy())
                        .build())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClientMeetingResponse> getClientMeetings(String clientCode, String currentUserIdentifier) {
        Client client = clientRepository.findByClientCode(clientCode)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found: " + clientCode));

        return clientMeetingRepository.findByClientId(client.getId(), Pageable.unpaged()).getContent().stream()
                .map(m -> mapToMeetingResponse(m, null))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ClientMeetingResponse getMeetingByCode(String meetingCode, String currentUserIdentifier) {
        ClientMeeting meeting = clientMeetingRepository.findByMeetingCode(meetingCode)
                .orElseThrow(() -> new ResourceNotFoundException("Client meeting not found: " + meetingCode));
        return mapToMeetingResponse(meeting, null);
    }

    private Client resolveClient(Long clientId, String clientCode) {
        if (clientId != null) {
            return clientRepository.findById(clientId)
                    .orElseThrow(() -> new ResourceNotFoundException("Client not found with ID: " + clientId));
        }
        if (StringUtils.hasText(clientCode)) {
            return clientRepository.findByClientCode(clientCode.trim())
                    .orElseThrow(() -> new ResourceNotFoundException("Client not found with code: " + clientCode));
        }
        throw new BadRequestException("Either clientId or clientCode must be provided.");
    }

    private User resolveUserOrThrow(String identifier) {
        return userRepository.findByEmployeeCodeIgnoreCaseAndDeletedFalse(identifier)
                .orElseGet(() -> userRepository.findByEmailIgnoreCaseAndDeletedFalse(identifier)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found: " + identifier)));
    }

    private boolean isAdminUser(User user) {
        if (user.getRole() == null) return false;
        String code = user.getRole().getCode().toUpperCase();
        return code.contains("ADMIN") || code.contains("SUPER_ADMIN");
    }

    private ClientMeetingResponse mapToMeetingResponse(ClientMeeting meeting, String verificationStatus) {
        Client client = meeting.getClient();
        User employee = meeting.getAssignedEmployee();

        return ClientMeetingResponse.builder()
                .id(meeting.getId())
                .meetingCode(meeting.getMeetingCode())
                .meetingNumber(meeting.getMeetingNumber())
                .clientId(client != null ? client.getId() : null)
                .clientCode(client != null ? client.getClientCode() : null)
                .clientName(client != null ? client.getClientName() : null)
                .assignedEmployeeId(employee != null ? employee.getId() : null)
                .assignedEmployeeCode(employee != null ? employee.getEmployeeCode() : null)
                .assignedEmployeeName(employee != null ? employee.getFullName() : null)
                .meetingMode(meeting.getMeetingMode() != null ? meeting.getMeetingMode().name() : null)
                .meetingDate(meeting.getMeetingDate())
                .meetingTime(meeting.getMeetingTime())
                .meetingLocation(meeting.getMeetingLocation())
                .meetingStatus(meeting.getMeetingStatus() != null ? meeting.getMeetingStatus().name() : null)
                .meetingConducted(meeting.getMeetingConducted() != null ? meeting.getMeetingConducted().name() : null)
                .clientMeetingStatus(meeting.getClientMeetingStatus())
                .aloneWith(meeting.getAloneWith())
                .personName(meeting.getPersonName())
                .position(meeting.getPosition())
                .businessGenerated(meeting.getBusinessGenerated())
                .remarks(meeting.getRemarks())
                .nextPlanDate(meeting.getNextPlanDate())
                .nextPlanTime(meeting.getNextPlanTime())
                .latitude(meeting.getLatitude())
                .longitude(meeting.getLongitude())
                .locationAccuracy(meeting.getLocationAccuracy())
                .verifiedByPc(meeting.getVerifiedByPc())
                .verificationStatus(verificationStatus)
                .createdAt(meeting.getCreatedAt())
                .build();
    }
}

package com.blueant_crm_erp.client.service.impl;

import com.blueant_crm_erp.client.dto.request.AddFamilyMemberRequest;
import com.blueant_crm_erp.client.dto.request.ChangeFamilyHeadRequest;
import com.blueant_crm_erp.client.dto.request.CreateFamilyRequest;
import com.blueant_crm_erp.client.dto.response.*;
import com.blueant_crm_erp.client.entity.Client;
import com.blueant_crm_erp.client.entity.ClientFamily;
import com.blueant_crm_erp.client.entity.ClientFamilyHeadHistory;
import com.blueant_crm_erp.client.entity.ClientFamilyMember;
import com.blueant_crm_erp.client.enums.FamilyMembershipStatus;
import com.blueant_crm_erp.client.enums.FamilyRelationship;
import com.blueant_crm_erp.client.enums.FamilyStatus;
import com.blueant_crm_erp.client.repository.ClientFamilyHeadHistoryRepository;
import com.blueant_crm_erp.client.repository.ClientFamilyMemberRepository;
import com.blueant_crm_erp.client.repository.ClientFamilyRepository;
import com.blueant_crm_erp.client.repository.ClientRepository;
import com.blueant_crm_erp.client.service.ActiveInvestmentEligibilityService;
import com.blueant_crm_erp.client.service.FamilyCodeGeneratorService;
import com.blueant_crm_erp.client.service.FamilyService;
import com.blueant_crm_erp.exception.client.ClientNotFoundException;
import com.blueant_crm_erp.exception.client.FamilyConflictException;
import com.blueant_crm_erp.exception.client.FamilyNotFoundException;
import com.blueant_crm_erp.exception.client.IneligibleFamilyHeadException;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FamilyServiceImpl implements FamilyService {

    private final ClientFamilyRepository clientFamilyRepository;
    private final ClientFamilyMemberRepository clientFamilyMemberRepository;
    private final ClientFamilyHeadHistoryRepository clientFamilyHeadHistoryRepository;
    private final ClientRepository clientRepository;
    private final FamilyCodeGeneratorService familyCodeGeneratorService;
    private final ActiveInvestmentEligibilityService activeInvestmentEligibilityService;

    @Override
    @Transactional
    public FamilyDetailResponse createFamily(CreateFamilyRequest request, String authenticatedUsername) {
        String actor = StringUtils.hasText(authenticatedUsername) ? authenticatedUsername : "SYSTEM";
        String familyCode = familyCodeGeneratorService.generateNextFamilyCode();

        ClientFamily family = ClientFamily.builder()
                .familyCode(familyCode)
                .familyName(request.getFamilyName().trim())
                .status(FamilyStatus.ACTIVE)
                .build();

        if (request.getInitialHeadClientId() != null) {
            Client candidateHead = clientRepository.findById(request.getInitialHeadClientId())
                    .orElseThrow(() -> new ClientNotFoundException("Client not found with id: " + request.getInitialHeadClientId()));

            // Check if client is already part of another active family
            Optional<ClientFamilyMember> existingActive = clientFamilyMemberRepository
                    .findByClientIdAndMembershipStatus(candidateHead.getId(), FamilyMembershipStatus.ACTIVE);
            if (existingActive.isPresent()) {
                throw new FamilyConflictException(
                        "Client " + candidateHead.getClientCode() + " already belongs to active family "
                                + existingActive.get().getFamily().getFamilyCode()
                );
            }

            // Verify active investment eligibility
            ActiveInvestmentEligibilityService.EligibilityResult eligibility = activeInvestmentEligibilityService.evaluateEligibility(candidateHead);
            if (!eligibility.eligible()) {
                throw new IneligibleFamilyHeadException(
                        "Candidate head " + candidateHead.getClientCode() + " is ineligible: " + eligibility.reason()
                );
            }

            family.setFamilyHead(candidateHead);
            ClientFamily savedFamily = clientFamilyRepository.save(family);

            FamilyRelationship rel = request.getInitialHeadRelationship() != null
                    ? request.getInitialHeadRelationship()
                    : FamilyRelationship.SELF;

            ClientFamilyMember member = ClientFamilyMember.builder()
                    .family(savedFamily)
                    .client(candidateHead)
                    .relationshipToHead(rel)
                    .membershipStatus(FamilyMembershipStatus.ACTIVE)
                    .joinedAt(LocalDateTime.now())
                    .joinedBy(actor)
                    .build();
            clientFamilyMemberRepository.save(member);

            candidateHead.setFamily(savedFamily);
            clientRepository.save(candidateHead);

            // Record initial Family Head History
            ClientFamilyHeadHistory history = ClientFamilyHeadHistory.builder()
                    .family(savedFamily)
                    .previousHead(null)
                    .newHead(candidateHead)
                    .changedByUser(actor)
                    .changedAt(LocalDateTime.now())
                    .reason("Initial family creation")
                    .build();
            clientFamilyHeadHistoryRepository.save(history);

            log.info("Created family {} with head {} by {}", savedFamily.getFamilyCode(), candidateHead.getClientCode(), actor);
            return mapToDetailResponse(savedFamily);
        } else {
            ClientFamily savedFamily = clientFamilyRepository.save(family);
            log.info("Created family {} without initial head by {}", savedFamily.getFamilyCode(), actor);
            return mapToDetailResponse(savedFamily);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FamilySummaryResponse> getFamilies(Pageable pageable, String search, FamilyStatus status) {
        Specification<ClientFamily> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("isDeleted"), false));

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (StringUtils.hasText(search)) {
                String pattern = "%" + search.trim().toLowerCase() + "%";
                Predicate codeMatch = cb.like(cb.lower(root.get("familyCode")), pattern);
                Predicate nameMatch = cb.like(cb.lower(root.get("familyName")), pattern);
                predicates.add(cb.or(codeMatch, nameMatch));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return clientFamilyRepository.findAll(spec, pageable).map(this::mapToSummaryResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public FamilyDetailResponse getFamilyByCode(String familyCode) {
        ClientFamily family = clientFamilyRepository.findByFamilyCode(familyCode)
                .orElseThrow(() -> new FamilyNotFoundException("Family not found with code: " + familyCode));
        return mapToDetailResponse(family);
    }

    @Override
    @Transactional
    public FamilyMemberResponse addMember(String familyCode, AddFamilyMemberRequest request, String authenticatedUsername) {
        String actor = StringUtils.hasText(authenticatedUsername) ? authenticatedUsername : "SYSTEM";
        ClientFamily family = clientFamilyRepository.findByFamilyCode(familyCode)
                .orElseThrow(() -> new FamilyNotFoundException("Family not found with code: " + familyCode));

        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new ClientNotFoundException("Client not found with id: " + request.getClientId()));

        // Verify client is not active in another family
        Optional<ClientFamilyMember> existingActive = clientFamilyMemberRepository
                .findByClientIdAndMembershipStatus(client.getId(), FamilyMembershipStatus.ACTIVE);
        if (existingActive.isPresent()) {
            if (existingActive.get().getFamily().getId().equals(family.getId())) {
                throw new FamilyConflictException("Client " + client.getClientCode() + " is already an active member of this family");
            } else {
                throw new FamilyConflictException(
                        "Client " + client.getClientCode() + " already belongs to active family "
                                + existingActive.get().getFamily().getFamilyCode()
                                + ". Cross-family transfers require an explicit approved transfer workflow."
                );
            }
        }

        // If client was previously an inactive member of this family, reactivate
        Optional<ClientFamilyMember> existingInactive = clientFamilyMemberRepository
                .findByFamilyIdAndClientId(family.getId(), client.getId());

        ClientFamilyMember member;
        if (existingInactive.isPresent()) {
            member = existingInactive.get();
            member.setMembershipStatus(FamilyMembershipStatus.ACTIVE);
            member.setRelationshipToHead(request.getRelationshipToHead());
            member.setJoinedAt(LocalDateTime.now());
            member.setJoinedBy(actor);
            member.setLeftAt(null);
            member.setLeftBy(null);
        } else {
            member = ClientFamilyMember.builder()
                    .family(family)
                    .client(client)
                    .relationshipToHead(request.getRelationshipToHead())
                    .membershipStatus(FamilyMembershipStatus.ACTIVE)
                    .joinedAt(LocalDateTime.now())
                    .joinedBy(actor)
                    .build();
        }

        ClientFamilyMember savedMember = clientFamilyMemberRepository.save(member);
        client.setFamily(family);
        clientRepository.save(client);

        // Optional head designation during member linking
        if (Boolean.TRUE.equals(request.getDesignateAsHead())) {
            ActiveInvestmentEligibilityService.EligibilityResult eligibility = activeInvestmentEligibilityService.evaluateEligibility(client);
            if (!eligibility.eligible()) {
                throw new IneligibleFamilyHeadException(
                        "Cannot designate client " + client.getClientCode() + " as Family Head: " + eligibility.reason()
                );
            }

            Client prevHead = family.getFamilyHead();
            family.setFamilyHead(client);
            clientFamilyRepository.save(family);

            ClientFamilyHeadHistory history = ClientFamilyHeadHistory.builder()
                    .family(family)
                    .previousHead(prevHead)
                    .newHead(client)
                    .changedByUser(actor)
                    .changedAt(LocalDateTime.now())
                    .reason("Designated as head during member linking")
                    .build();
            clientFamilyHeadHistoryRepository.save(history);
        }

        log.info("Added client {} to family {} by {}", client.getClientCode(), family.getFamilyCode(), actor);
        return mapToMemberResponse(savedMember, family.getFamilyHead() != null && family.getFamilyHead().getId().equals(client.getId()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EligibleFamilyHeadResponse> getEligibleHeads(String familyCode) {
        ClientFamily family = clientFamilyRepository.findByFamilyCode(familyCode)
                .orElseThrow(() -> new FamilyNotFoundException("Family not found with code: " + familyCode));

        List<ClientFamilyMember> activeMembers = clientFamilyMemberRepository
                .findByFamilyIdAndMembershipStatus(family.getId(), FamilyMembershipStatus.ACTIVE);

        Long currentHeadId = family.getFamilyHead() != null ? family.getFamilyHead().getId() : null;

        return activeMembers.stream().map(member -> {
            Client c = member.getClient();
            ActiveInvestmentEligibilityService.EligibilityResult result = activeInvestmentEligibilityService.evaluateEligibility(c);
            boolean isCurrentHead = Objects.equals(c.getId(), currentHeadId);
            return EligibleFamilyHeadResponse.builder()
                    .clientId(c.getId())
                    .clientCode(c.getClientCode())
                    .clientName(c.getClientName())
                    .isCurrentHead(isCurrentHead)
                    .eligible(result.eligible())
                    .eligibilityReason(result.reason())
                    .qualifyingDetails(result.qualifyingDetails())
                    .build();
        }).toList();
    }

    @Override
    @Transactional
    public FamilyDetailResponse changeFamilyHead(String familyCode, ChangeFamilyHeadRequest request, String authenticatedUsername) {
        String actor = StringUtils.hasText(authenticatedUsername) ? authenticatedUsername : "SYSTEM";

        // Concurrency-safe pessimistic write lock
        ClientFamily family = clientFamilyRepository.findByFamilyCodeWithLock(familyCode)
                .orElseThrow(() -> new FamilyNotFoundException("Family not found with code: " + familyCode));

        Client candidateHead = clientRepository.findById(request.getNewHeadClientId())
                .orElseThrow(() -> new ClientNotFoundException("Client not found with id: " + request.getNewHeadClientId()));

        // Rule: The selected head must be an active member of that same family
        boolean isActiveMember = clientFamilyMemberRepository
                .existsByFamilyIdAndClientIdAndMembershipStatus(family.getId(), candidateHead.getId(), FamilyMembershipStatus.ACTIVE);
        if (!isActiveMember) {
            throw new IneligibleFamilyHeadException(
                    "Selected client " + candidateHead.getClientCode() + " is not an active member of family " + familyCode
            );
        }

        // Rule: The selected head must satisfy the authoritative active SIP/investment eligibility rule
        ActiveInvestmentEligibilityService.EligibilityResult eligibility = activeInvestmentEligibilityService.evaluateEligibility(candidateHead);
        if (!eligibility.eligible()) {
            throw new IneligibleFamilyHeadException(
                    "Selected client " + candidateHead.getClientCode() + " does not meet active investment eligibility: "
                            + eligibility.reason()
            );
        }

        Client previousHead = family.getFamilyHead();

        // Atomically update head and record immutable history
        family.setFamilyHead(candidateHead);
        clientFamilyRepository.save(family);

        ClientFamilyHeadHistory history = ClientFamilyHeadHistory.builder()
                .family(family)
                .previousHead(previousHead)
                .newHead(candidateHead)
                .changedByUser(actor)
                .changedAt(LocalDateTime.now())
                .reason(StringUtils.hasText(request.getReason()) ? request.getReason().trim() : "CRM family head update")
                .build();
        clientFamilyHeadHistoryRepository.save(history);

        log.info("Changed family {} head from {} to {} by {}",
                familyCode,
                previousHead != null ? previousHead.getClientCode() : "NONE",
                candidateHead.getClientCode(),
                actor
        );

        return mapToDetailResponse(family);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FamilyHeadHistoryResponse> getFamilyHeadHistory(String familyCode) {
        ClientFamily family = clientFamilyRepository.findByFamilyCode(familyCode)
                .orElseThrow(() -> new FamilyNotFoundException("Family not found with code: " + familyCode));

        List<ClientFamilyHeadHistory> histories = clientFamilyHeadHistoryRepository
                .findByFamilyIdOrderByChangedAtDescIdDesc(family.getId());

        return histories.stream().map(h -> FamilyHeadHistoryResponse.builder()
                .id(h.getId())
                .familyId(family.getId())
                .familyCode(family.getFamilyCode())
                .previousHeadClientId(h.getPreviousHead() != null ? h.getPreviousHead().getId() : null)
                .previousHeadClientCode(h.getPreviousHead() != null ? h.getPreviousHead().getClientCode() : null)
                .previousHeadClientName(h.getPreviousHead() != null ? h.getPreviousHead().getClientName() : null)
                .newHeadClientId(h.getNewHead().getId())
                .newHeadClientCode(h.getNewHead().getClientCode())
                .newHeadClientName(h.getNewHead().getClientName())
                .changedByUser(h.getChangedByUser())
                .changedAt(h.getChangedAt())
                .reason(h.getReason())
                .build()
        ).toList();
    }

    private FamilySummaryResponse mapToSummaryResponse(ClientFamily family) {
        List<ClientFamilyMember> activeMembers = clientFamilyMemberRepository
                .findByFamilyIdAndMembershipStatus(family.getId(), FamilyMembershipStatus.ACTIVE);

        return FamilySummaryResponse.builder()
                .id(family.getId())
                .familyCode(family.getFamilyCode())
                .familyName(family.getFamilyName())
                .familyHeadClientId(family.getFamilyHead() != null ? family.getFamilyHead().getId() : null)
                .familyHeadClientCode(family.getFamilyHead() != null ? family.getFamilyHead().getClientCode() : null)
                .familyHeadClientName(family.getFamilyHead() != null ? family.getFamilyHead().getClientName() : null)
                .activeMemberCount(activeMembers.size())
                .status(family.getStatus())
                .createdAt(family.getCreatedAt())
                .createdBy(family.getCreatedBy())
                .build();
    }

    private FamilyDetailResponse mapToDetailResponse(ClientFamily family) {
        List<ClientFamilyMember> activeMembers = clientFamilyMemberRepository
                .findByFamilyIdAndMembershipStatus(family.getId(), FamilyMembershipStatus.ACTIVE);

        Long currentHeadId = family.getFamilyHead() != null ? family.getFamilyHead().getId() : null;

        List<FamilyMemberResponse> memberResponses = activeMembers.stream()
                .map(m -> mapToMemberResponse(m, Objects.equals(m.getClient().getId(), currentHeadId)))
                .toList();

        return FamilyDetailResponse.builder()
                .id(family.getId())
                .familyCode(family.getFamilyCode())
                .familyName(family.getFamilyName())
                .familyHeadClientId(family.getFamilyHead() != null ? family.getFamilyHead().getId() : null)
                .familyHeadClientCode(family.getFamilyHead() != null ? family.getFamilyHead().getClientCode() : null)
                .familyHeadClientName(family.getFamilyHead() != null ? family.getFamilyHead().getClientName() : null)
                .status(family.getStatus())
                .createdAt(family.getCreatedAt())
                .createdBy(family.getCreatedBy())
                .updatedAt(family.getUpdatedAt())
                .updatedBy(family.getUpdatedBy())
                .members(memberResponses)
                .build();
    }

    private FamilyMemberResponse mapToMemberResponse(ClientFamilyMember member, boolean isFamilyHead) {
        Client c = member.getClient();
        return FamilyMemberResponse.builder()
                .memberId(member.getId())
                .clientId(c.getId())
                .clientCode(c.getClientCode())
                .clientName(c.getClientName())
                .mobileNumber(c.getMobileNumber())
                .relationshipToHead(member.getRelationshipToHead())
                .membershipStatus(member.getMembershipStatus())
                .isFamilyHead(isFamilyHead)
                .joinedAt(member.getJoinedAt())
                .joinedBy(member.getJoinedBy())
                .build();
    }
}

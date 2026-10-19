package com.blueant_crm_erp.client.service.impl;

import com.blueant_crm_erp.client.entity.Client;
import com.blueant_crm_erp.client.entity.CrmOnboarding;
import com.blueant_crm_erp.client.enums.ClientPaymentStatus;
import com.blueant_crm_erp.client.enums.ClientStatus;
import com.blueant_crm_erp.client.repository.ClientMeetingRepository;
import com.blueant_crm_erp.client.repository.CrmOnboardingRepository;
import com.blueant_crm_erp.client.service.ActiveInvestmentEligibilityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActiveInvestmentEligibilityServiceImpl implements ActiveInvestmentEligibilityService {

    private final ClientMeetingRepository clientMeetingRepository;
    private final CrmOnboardingRepository crmOnboardingRepository;

    @Override
    @Transactional(readOnly = true)
    public boolean isEligibleForFamilyHead(Client client) {
        return evaluateEligibility(client).eligible();
    }

    @Override
    @Transactional(readOnly = true)
    public EligibilityResult evaluateEligibility(Client client) {
        if (client == null) {
            return new EligibilityResult(false, "Client record does not exist", null);
        }

        if (client.getClientStatus() != ClientStatus.ACTIVE) {
            return new EligibilityResult(
                    false,
                    "Client is not ACTIVE (current status: " + client.getClientStatus() + ")",
                    null
            );
        }

        // Criterion 1: Confirmed payment status with positive payment amount and investment type or first investment
        if (client.getPaymentStatus() == ClientPaymentStatus.YES
                && client.getPaymentAmount() != null
                && client.getPaymentAmount().compareTo(BigDecimal.ZERO) > 0) {
            boolean hasInvestmentType = StringUtils.hasText(client.getInvestmentType());
            boolean hasFirstInvestment = client.getFirstInvestmentAmount() != null
                    && client.getFirstInvestmentAmount().compareTo(BigDecimal.ZERO) > 0;
            if (hasInvestmentType || hasFirstInvestment) {
                String detail = "Confirmed payment of " + client.getPaymentAmount()
                        + (hasInvestmentType ? " (" + client.getInvestmentType() + ")" : "");
                return new EligibilityResult(true, "Qualifying active investment verified via confirmed client payment", detail);
            }
        }

        // Criterion 2: Active portfolio registered in Investwell with positive first investment amount
        if (StringUtils.hasText(client.getInvestwellId())
                && client.getFirstInvestmentAmount() != null
                && client.getFirstInvestmentAmount().compareTo(BigDecimal.ZERO) > 0) {
            String detail = "Active Investwell portfolio (" + client.getInvestwellId() + ") with initial investment of "
                    + client.getFirstInvestmentAmount();
            return new EligibilityResult(true, "Qualifying active investment verified via Investwell portfolio", detail);
        }

        // Criterion 3: Verified Client Meeting with business generated
        if (client.getId() != null && clientMeetingRepository.hasVerifiedBusinessGeneratedMeeting(client.getId())) {
            return new EligibilityResult(
                    true,
                    "Qualifying active investment verified via PC-verified client meeting with business generated",
                    "PC-verified client meeting business outcome"
            );
        }

        // Criterion 4: Converted Lead with CRM Onboarding verified payment and first investment
        if (client.getLead() != null && client.getLead().getId() != null) {
            Optional<CrmOnboarding> onboardingOpt = crmOnboardingRepository.findByLeadId(client.getLead().getId());
            if (onboardingOpt.isPresent()) {
                CrmOnboarding onboarding = onboardingOpt.get();
                if (Boolean.TRUE.equals(onboarding.getPaymentDone())
                        && onboarding.getFirstInvestmentAmount() != null
                        && onboarding.getFirstInvestmentAmount().compareTo(BigDecimal.ZERO) > 0) {
                    String detail = "CRM Onboarding verified payment of " + onboarding.getFirstInvestmentAmount()
                            + (StringUtils.hasText(onboarding.getInvestmentType()) ? " (" + onboarding.getInvestmentType() + ")" : "");
                    return new EligibilityResult(true, "Qualifying active investment verified via CRM onboarding", detail);
                }
            }
        }

        // Rejection explanation: specifically check if only an expectedMaxSIP exists
        if (client.getExpectedMaxSIP() != null && client.getExpectedMaxSIP().compareTo(BigDecimal.ZERO) > 0) {
            return new EligibilityResult(
                    false,
                    "Client only has an expected SIP amount (" + client.getExpectedMaxSIP()
                            + ") without authoritative confirmed active investment or payment",
                    null
            );
        }

        return new EligibilityResult(
                false,
                "No authoritative active investment, confirmed payment, or PC-verified business meeting found for client",
                null
        );
    }
}

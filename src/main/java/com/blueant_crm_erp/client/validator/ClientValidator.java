package com.blueant_crm_erp.client.validator;

import com.blueant_crm_erp.client.dto.request.ClientMeetingUpdateRequest;
import com.blueant_crm_erp.client.dto.request.ClientMeetingVerificationRequest;
import com.blueant_crm_erp.client.dto.request.CreateClientRegistrationRequest;
import com.blueant_crm_erp.client.dto.request.UpdateClientRequest;
import com.blueant_crm_erp.client.entity.ClientMeeting;
import com.blueant_crm_erp.exception.common.BadRequestException;
import com.blueant_crm_erp.meeting.enums.MeetingConductStatus;
import com.blueant_crm_erp.meeting.enums.SalesRole;
import com.blueant_crm_erp.user.entity.User;
import com.blueant_crm_erp.util.meeting.SalesRoleResolver;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@Component
public class ClientValidator {

    private static final Set<String> ALLOWED_BEST_TIME_TO_MEET = Set.of(
            "9:00 AM - 12:00 PM",
            "12:00 PM - 3:00 PM",
            "3:00 PM - 6:00 PM",
            "6:00 PM - 9:00 PM"
    );

    private static final Set<String> ALLOWED_VERIFICATION_POSITIONS = Set.of(
            "SM",
            "RM",
            "TL",
            "ADMIN",
            "SUPER ADMIN"
    );

    private static final Set<String> ALLOWED_INVESTMENT_TYPES = Set.of(
            "New SIP",
            "Lumpsum",
            "New Broker Change"
    );

    public void validateUpdateRequest(UpdateClientRequest request) {
        // Business logic validation for updates
    }

    public void validateRegistration(CreateClientRegistrationRequest request) {
        if (request == null) {
            throw new BadRequestException("Registration request cannot be null.");
        }

        if (request.getBestTimeToMeet() != null && !request.getBestTimeToMeet().isBlank()) {
            String trimmed = request.getBestTimeToMeet().trim();
            if (!ALLOWED_BEST_TIME_TO_MEET.contains(trimmed)) {
                throw new BadRequestException("Invalid bestTimeToMeet: '" + trimmed + "'. Allowed values: " + ALLOWED_BEST_TIME_TO_MEET);
            }
        }
    }

    public void validateSalesPersonEligibility(User user) {
        if (user == null || Boolean.TRUE.equals(user.getDeleted())) {
            throw new BadRequestException("Selected Sales Person is not found or has been deleted.");
        }

        if (!user.isActive()) {
            throw new BadRequestException("Selected Sales Person (" + user.getFullName() + ") is inactive.");
        }

        SalesRole role = SalesRoleResolver.resolveRole(user);
        if (role != SalesRole.RM && role != SalesRole.SC) {
            throw new BadRequestException("Selected Sales Person (" + user.getFullName() + ") is ineligible for client assignment. Only active Relationship Managers (RM) or Sales Coordinators (SC) can be assigned.");
        }
    }

    public void validateMeetingUpdate(ClientMeeting meeting, ClientMeetingUpdateRequest request, SalesRole role) {
        if (request == null) {
            throw new BadRequestException("Meeting update request cannot be null.");
        }

        if (request.getMeetingConducted() == null) {
            throw new BadRequestException("Meeting conducted status is required.");
        }

        if (request.getMeetingConducted() == MeetingConductStatus.CONDUCTED) {
            validateConductedUpdate(request, role);
        } else {
            validateNotConductedUpdate(request, role);
        }
    }

    private void validateConductedUpdate(ClientMeetingUpdateRequest request, SalesRole role) {
        if (request.getMeetingMode() == null) {
            throw new BadRequestException("Meeting mode is required for conducted meetings.");
        }

        if (request.getMeetingDate() == null) {
            throw new BadRequestException("Meeting date is required.");
        }

        if (request.getAloneWith() == null || request.getAloneWith().isBlank()) {
            throw new BadRequestException("Alone with is required for conducted meetings.");
        }

        String aw = request.getAloneWith().trim().toUpperCase();
        if (!"SELF".equals(aw) && !"SOMEONE".equals(aw)) {
            throw new BadRequestException("Alone with must be either 'SELF' or 'SOMEONE'.");
        }

        if ("SOMEONE".equals(aw)) {
            if (request.getPersonName() == null || request.getPersonName().isBlank()) {
                throw new BadRequestException("Person name is required when aloneWith is SOMEONE.");
            }
            if (request.getPosition() == null || request.getPosition().isBlank()) {
                throw new BadRequestException("Position is required when aloneWith is SOMEONE.");
            }
        } else {
            if ((request.getPersonName() != null && !request.getPersonName().isBlank()) ||
                (request.getPosition() != null && !request.getPosition().isBlank())) {
                throw new BadRequestException("Person name and position must not be provided when aloneWith is SELF.");
            }
        }

        if (request.getBusinessGenerated() == null || request.getBusinessGenerated().isBlank()) {
            throw new BadRequestException("Business generated is required (YES or NO).");
        }

        String bg = request.getBusinessGenerated().trim().toUpperCase();
        if (!"YES".equals(bg) && !"NO".equals(bg)) {
            throw new BadRequestException("Business generated must be either 'YES' or 'NO'.");
        }

        if (request.getRemarks() == null || request.getRemarks().isBlank()) {
            throw new BadRequestException("Remarks are required.");
        }

        if (request.getNextPlanDate() != null) {
            validateFollowUpDateLimit(request.getNextPlanDate(), role);
        }

        validateGeoLocation(request.getLatitude(), request.getLongitude(), request.getAccuracy(), role, true);
    }

    private void validateNotConductedUpdate(ClientMeetingUpdateRequest request, SalesRole role) {
        if (request.getMeetingDate() == null) {
            throw new BadRequestException("Meeting date is required.");
        }

        if (request.getMeetingTime() == null) {
            throw new BadRequestException("Meeting time is required when meeting is not conducted.");
        }

        if (request.getRemarks() == null || request.getRemarks().isBlank()) {
            throw new BadRequestException("Remarks are required when meeting is not conducted.");
        }

        if (request.getNextPlanDate() == null) {
            throw new BadRequestException("Next follow-up date is required when meeting is not conducted.");
        }

        validateFollowUpDateLimit(request.getNextPlanDate(), role);
        validateGeoLocation(request.getLatitude(), request.getLongitude(), request.getAccuracy(), role, false);
    }

    private void validateFollowUpDateLimit(LocalDate date, SalesRole role) {
        if (date.isBefore(LocalDate.now())) {
            throw new BadRequestException("Follow-up date cannot be in the past.");
        }

        if (role == SalesRole.RM || role == SalesRole.SC) {
            LocalDate maxDate = LocalDate.now().plusMonths(1);
            if (date.isAfter(maxDate)) {
                throw new BadRequestException(role.name() + " follow-up date cannot exceed one calendar month (" + maxDate + ").");
            }
        }
    }

    private void validateGeoLocation(BigDecimal lat, BigDecimal lon, Double accuracy, SalesRole role, boolean conducted) {
        if (role == SalesRole.RM) {
            // RM requires geo for conducted and not conducted meetings
            if (lat == null || lon == null || accuracy == null) {
                throw new BadRequestException("Location coordinates (latitude, longitude) and accuracy are mandatory for RM.");
            }
            validateCoordinatesRange(lat, lon, accuracy);
        } else {
            // SC geo is optional for both statuses, but if present must be valid
            if (lat != null || lon != null) {
                if (lat == null || lon == null) {
                    throw new BadRequestException("Both latitude and longitude must be provided together.");
                }
                validateCoordinatesRange(lat, lon, accuracy);
            }
            if (accuracy != null && accuracy < 0) {
                throw new BadRequestException("Location accuracy cannot be negative.");
            }
        }
    }

    private void validateCoordinatesRange(BigDecimal lat, BigDecimal lon, Double accuracy) {
        if (lat.compareTo(BigDecimal.valueOf(-90)) < 0 || lat.compareTo(BigDecimal.valueOf(90)) > 0) {
            throw new BadRequestException("Latitude must be between -90 and +90 degrees.");
        }
        if (lon.compareTo(BigDecimal.valueOf(-180)) < 0 || lon.compareTo(BigDecimal.valueOf(180)) > 0) {
            throw new BadRequestException("Longitude must be between -180 and +180 degrees.");
        }
        if (accuracy != null && accuracy < 0) {
            throw new BadRequestException("Location accuracy cannot be negative.");
        }
    }

    public void validateVerification(ClientMeeting meeting, ClientMeetingVerificationRequest request) {
        if (request == null) {
            throw new BadRequestException("Verification request cannot be null.");
        }

        boolean isConducted = meeting.getMeetingConducted() == MeetingConductStatus.CONDUCTED;

        if (isConducted) {
            validateConductedVerification(meeting, request);
        } else {
            validateNotConductedVerification(request);
        }
    }

    private void validateConductedVerification(ClientMeeting meeting, ClientMeetingVerificationRequest request) {
        if (request.getMeetingTime() == null) {
            throw new BadRequestException("Meeting time is required for verification.");
        }

        if (request.getBlueantAppUsed() == null || request.getBlueantAppUsed().isBlank()) {
            throw new BadRequestException("BlueAnt App Used? (YES/NO) is required.");
        }
        String appUsed = request.getBlueantAppUsed().trim().toUpperCase();
        if (!"YES".equals(appUsed) && !"NO".equals(appUsed)) {
            throw new BadRequestException("BlueAnt App Used must be either 'YES' or 'NO'.");
        }

        if (request.getAloneWith() == null || request.getAloneWith().isBlank()) {
            throw new BadRequestException("Alone with is required.");
        }
        String aw = request.getAloneWith().trim().toUpperCase();
        if (!"SELF".equals(aw) && !"SOMEONE".equals(aw)) {
            throw new BadRequestException("Alone with must be either 'SELF' or 'SOMEONE'.");
        }

        if ("SOMEONE".equals(aw)) {
            if (request.getPersonName() == null || request.getPersonName().isBlank()) {
                throw new BadRequestException("Person name is required when Alone With is SOMEONE.");
            }
            if (request.getPosition() == null || request.getPosition().isBlank()) {
                throw new BadRequestException("Position is required when Alone With is SOMEONE.");
            }
            String pos = request.getPosition().trim().toUpperCase();
            if (!ALLOWED_VERIFICATION_POSITIONS.contains(pos)) {
                throw new BadRequestException("Position must be one of: " + ALLOWED_VERIFICATION_POSITIONS);
            }
        }

        if (request.getRemarks() == null || request.getRemarks().isBlank()) {
            throw new BadRequestException("Verification remarks are required.");
        }

        boolean businessGenerated = "YES".equalsIgnoreCase(meeting.getBusinessGenerated()) ||
                "YES".equalsIgnoreCase(request.getBusinessGenerated());

        if (businessGenerated) {
            if (request.getInvestmentType() == null || request.getInvestmentType().isBlank()) {
                throw new BadRequestException("Investment Type is required when Business Generated is YES.");
            }
            String invType = request.getInvestmentType().trim();
            if (!ALLOWED_INVESTMENT_TYPES.contains(invType)) {
                throw new BadRequestException("Investment Type must be one of: " + ALLOWED_INVESTMENT_TYPES);
            }
            if (request.getInvestmentAmount() == null || request.getInvestmentAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BadRequestException("Investment Amount must be greater than zero when Business Generated is YES.");
            }
        }
    }

    private void validateNotConductedVerification(ClientMeetingVerificationRequest request) {
        LocalDate date = request.getMeetingDate() != null ? request.getMeetingDate() : request.getNewMeetingDate();
        if (date == null) {
            throw new BadRequestException("Meeting date is required.");
        }

        if (request.getMeetingTime() == null) {
            throw new BadRequestException("Meeting time is required.");
        }

        if (request.getRemarks() == null || request.getRemarks().isBlank()) {
            throw new BadRequestException("Remarks are required.");
        }
    }
}

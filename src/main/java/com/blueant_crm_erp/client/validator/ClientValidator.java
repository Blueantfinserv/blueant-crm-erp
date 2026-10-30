package com.blueant_crm_erp.client.validator;

import com.blueant_crm_erp.client.dto.ClientBusinessItemDto;
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
import com.blueant_crm_erp.util.validation.MobileValidator;
import com.blueant_crm_erp.util.validation.PinCodeValidator;
import org.springframework.stereotype.Component;

import com.blueant_crm_erp.client.enums.ClientBestTimeToMeet;
import com.blueant_crm_erp.client.enums.ClientDocumentType;
import com.blueant_crm_erp.client.enums.PcBusinessType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Component
public class ClientValidator {

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
        if (request == null) {
            return;
        }

        // 1. Mandatory Field Protection: Prevent blank strings from wiping non-nullable columns
        if (request.getClientName() != null) {
            if (request.getClientName().isBlank()) {
                throw new BadRequestException("Client name cannot be blank.");
            }
            request.setClientName(request.getClientName().trim());
        }
        if (request.getMobileNumber() != null) {
            if (request.getMobileNumber().isBlank()) {
                throw new BadRequestException("Mobile number cannot be blank.");
            }
            String normalizedMobile = MobileValidator.normalize(request.getMobileNumber());
            if (!MobileValidator.isValid(normalizedMobile) || !MobileValidator.hasValidLength(normalizedMobile)) {
                throw new BadRequestException("Invalid mobile number. Must be a 10-digit Indian mobile number.");
            }
            request.setMobileNumber(normalizedMobile);
        }

        // 2. Accidental Blank Erase Protection: Normalize whitespace-only optional strings to null
        // This ensures MapStruct's IGNORE strategy skips them, preserving existing DB values
        request.setSpeciality(sanitizeBlank(request.getSpeciality()));
        request.setLocation(sanitizeBlank(request.getLocation()));
        request.setClinicAddress(sanitizeBlank(request.getClinicAddress()));
        request.setAlternateMobileNumber(sanitizeBlank(request.getAlternateMobileNumber()));
        request.setOccupation(sanitizeBlank(request.getOccupation()));
        request.setMotherName(sanitizeBlank(request.getMotherName()));
        request.setPlaceOfBirth(sanitizeBlank(request.getPlaceOfBirth()));
        request.setResidentialAddress(sanitizeBlank(request.getResidentialAddress()));
        request.setCorrespondenceAddress(sanitizeBlank(request.getCorrespondenceAddress()));
        request.setOfficeAddress(sanitizeBlank(request.getOfficeAddress()));
        request.setCity(sanitizeBlank(request.getCity()));
        request.setState(sanitizeBlank(request.getState()));
        request.setPinCode(sanitizeBlank(request.getPinCode()));
        request.setClientSource(sanitizeBlank(request.getClientSource()));
        request.setSourceDescription(sanitizeBlank(request.getSourceDescription()));
        request.setInvestwellId(sanitizeBlank(request.getInvestwellId()));
        request.setRemarks(sanitizeBlank(request.getRemarks()));
        request.setEmail(sanitizeBlank(request.getEmail()));
        request.setPanNumber(sanitizeBlank(request.getPanNumber()));
        request.setAmc(sanitizeBlank(request.getAmc()));
        request.setScheme(sanitizeBlank(request.getScheme()));
        request.setInvestmentType(sanitizeBlank(request.getInvestmentType()));

        // 3. Date Integrity Validations
        if (request.getDateOfBirth() != null && !request.getDateOfBirth().isBefore(LocalDate.now())) {
            throw new BadRequestException("Date of birth must be in the past.");
        }
        if (request.getActualDateOfBirth() != null && !request.getActualDateOfBirth().isBefore(LocalDate.now())) {
            throw new BadRequestException("Actual date of birth must be in the past.");
        }
        if (request.getAssignmentDate() != null && request.getAssignmentDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("Assignment date cannot be in the past. It must be today or a future date.");
        }

        // 4. Enumeration & Form Display Name Validation for bestTimeToMeet
        if (request.getBestTimeToMeet() != null && !request.getBestTimeToMeet().isBlank()) {
            String trimmed = request.getBestTimeToMeet().trim();
            if (!ClientBestTimeToMeet.isValid(trimmed)) {
                throw new BadRequestException("Invalid bestTimeToMeet: '" + trimmed + "'. Allowed values: " + ClientBestTimeToMeet.SUPPORTED_DISPLAY_NAMES);
            }
            request.setBestTimeToMeet(ClientBestTimeToMeet.normalizeToDisplayName(trimmed));
        } else {
            request.setBestTimeToMeet(null);
        }

        // 5. Alternate Mobile & PIN Validations
        if (request.getAlternateMobileNumber() != null) {
            String normAlt = MobileValidator.normalize(request.getAlternateMobileNumber());
            if (!MobileValidator.isValid(normAlt) || !MobileValidator.hasValidLength(normAlt)) {
                throw new BadRequestException("Invalid alternate mobile number. Must be a 10-digit Indian mobile number.");
            }
            if (request.getMobileNumber() != null && normAlt.equals(request.getMobileNumber())) {
                throw new BadRequestException("Alternate mobile number cannot be identical to primary mobile number.");
            }
            request.setAlternateMobileNumber(normAlt);
        }
        if (request.getPinCode() != null) {
            if (!PinCodeValidator.isValid(request.getPinCode())) {
                throw new BadRequestException("Invalid PIN code. Expected 6-digit postal code.");
            }
            request.setPinCode(PinCodeValidator.normalize(request.getPinCode()));
        }

        // 6. PAN format validation if provided
        if (request.getPanNumber() != null) {
            String panUpper = request.getPanNumber().trim().toUpperCase(Locale.ENGLISH);
            if (!panUpper.matches("^[A-Z]{5}[0-9]{4}[A-Z]{1}$")) {
                throw new BadRequestException("Invalid PAN format. Expected format: ABCDE1234F.");
            }
            request.setPanNumber(panUpper);
        }

        // 7. Non-negative numeric amounts
        if (request.getExpectedMaxSIP() != null && request.getExpectedMaxSIP().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Expected max SIP must be non-negative.");
        }
        if (request.getFirstInvestmentAmount() != null && request.getFirstInvestmentAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("First investment amount must be non-negative.");
        }
    }

    private String sanitizeBlank(String val) {
        return (val != null && !val.isBlank()) ? val.trim() : null;
    }

    public void validateRegistration(CreateClientRegistrationRequest request) {
        if (request == null) {
            throw new BadRequestException("Registration request cannot be null.");
        }

        if (request.getAssignmentDate() != null && request.getAssignmentDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("Assignment date cannot be in the past. It must be today or a future date.");
        }

        if (request.getBestTimeToMeet() != null && !request.getBestTimeToMeet().isBlank()) {
            String trimmed = request.getBestTimeToMeet().trim();
            if (!ClientBestTimeToMeet.isValid(trimmed)) {
                throw new BadRequestException("Invalid bestTimeToMeet: '" + trimmed + "'. Allowed values: " + ClientBestTimeToMeet.SUPPORTED_DISPLAY_NAMES);
            }
        }

        if (request.getDateOfBirth() != null && !request.getDateOfBirth().isBefore(LocalDate.now())) {
            throw new BadRequestException("Date of birth must be in the past.");
        }

        if (request.getActualDateOfBirth() != null && !request.getActualDateOfBirth().isBefore(LocalDate.now())) {
            throw new BadRequestException("Actual date of birth must be in the past.");
        }

        if (request.getFirstInvestmentAmount() != null && request.getFirstInvestmentAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("First investment amount must be non-negative.");
        }

        if (request.getExpectedMaxSIP() != null && request.getExpectedMaxSIP().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Expected max SIP must be non-negative.");
        }

        if (request.getPaymentAmount() != null && request.getPaymentAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Payment amount must be non-negative.");
        }

        if (request.getNominees() != null) {
            for (com.blueant_crm_erp.client.dto.request.ClientNomineeRequest nominee : request.getNominees()) {
                if (nominee.getNomineeName() == null || nominee.getNomineeName().isBlank()) {
                    throw new BadRequestException("Nominee name is required.");
                }
                if (nominee.getRelationship() == null || nominee.getRelationship().isBlank()) {
                    throw new BadRequestException("Nominee relationship is required.");
                }
                if (nominee.getDateOfBirth() != null && !nominee.getDateOfBirth().isBefore(LocalDate.now())) {
                    throw new BadRequestException("Nominee date of birth must be in the past.");
                }
                if (Boolean.TRUE.equals(nominee.getIsMinor())) {
                    if (nominee.getGuardianName() == null || nominee.getGuardianName().isBlank()) {
                        throw new BadRequestException("Guardian name is required for minor nominee: " + nominee.getNomineeName());
                    }
                    if (nominee.getGuardianRelationship() == null || nominee.getGuardianRelationship().isBlank()) {
                        throw new BadRequestException("Guardian relationship is required for minor nominee: " + nominee.getNomineeName());
                    }
                }
            }
        }

        if (request.getDocuments() != null && !request.getDocuments().isEmpty()) {
            Set<ClientDocumentType> distinctTypes = new HashSet<>();
            for (com.blueant_crm_erp.client.dto.request.ClientDocumentRequest doc : request.getDocuments()) {
                if (doc.getDocumentType() == null) {
                    throw new BadRequestException("Document type is required.");
                }
                if (!ClientDocumentType.isSupported(doc.getDocumentType())) {
                    throw new BadRequestException("Unsupported document type: '" + doc.getDocumentType() + "'. Supported types: " + ClientDocumentType.SUPPORTED_TYPES);
                }
                if (doc.getReceivedDate() != null && doc.getReceivedDate().isAfter(LocalDate.now())) {
                    throw new BadRequestException("Document received date cannot be in the future.");
                }
                distinctTypes.add(doc.getDocumentType().toCanonical());
            }
            if (distinctTypes.size() < 2) {
                throw new BadRequestException("Any TWO distinct supported document types are mandatory for client registration document submission. Found distinct types: " + distinctTypes.size());
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

    public void validateFollowUpDateLimit(LocalDate date, SalesRole role) {
        validateFollowUpDateLimit(date, role, LocalDate.now());
    }

    public void validateFollowUpDateLimit(LocalDate date, SalesRole role, LocalDate referenceDate) {
        if (date.isBefore(referenceDate)) {
            throw new BadRequestException("Follow-up date cannot be in the past.");
        }

        if (role == SalesRole.RM || role == SalesRole.SC) {
            LocalDate maxDate = referenceDate.plusMonths(2);
            if (date.isAfter(maxDate)) {
                throw new BadRequestException(role.name() + " follow-up date cannot exceed two calendar months (" + maxDate + ").");
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
        } else {
            if ((request.getPersonName() != null && !request.getPersonName().isBlank()) ||
                (request.getPosition() != null && !request.getPosition().isBlank())) {
                throw new BadRequestException("Person name and position must not be provided when aloneWith is SELF.");
            }
        }

        if (request.getRemarks() == null || request.getRemarks().isBlank()) {
            throw new BadRequestException("Verification remarks are required.");
        }

        boolean businessGenerated = "YES".equalsIgnoreCase(meeting.getBusinessGenerated()) ||
                "YES".equalsIgnoreCase(request.getBusinessGenerated());

        if (businessGenerated) {
            if (request.getBusinessItems() != null && !request.getBusinessItems().isEmpty()) {
                Set<String> seenTypes = new HashSet<>();
                for (ClientBusinessItemDto item : request.getBusinessItems()) {
                    if (item.getBusinessType() == null || item.getBusinessType().isBlank()) {
                        throw new BadRequestException("Business type is required for each business item.");
                    }
                    if (!PcBusinessType.isValid(item.getBusinessType())) {
                        throw new BadRequestException("Unsupported business/service type: '" + item.getBusinessType() + "'. Supported types: " + PcBusinessType.SUPPORTED_TYPES);
                    }
                    String normalized = PcBusinessType.normalize(item.getBusinessType());
                    if (!seenTypes.add(normalized)) {
                        throw new BadRequestException("Duplicate business type is not allowed: " + item.getBusinessType());
                    }

                    boolean isMonetary = PcBusinessType.isMonetary(normalized);
                    if (isMonetary) {
                        if (item.getAmount() == null || item.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                            throw new BadRequestException("Amount must be greater than zero for category: " + item.getBusinessType());
                        }
                    } else {
                        if (item.getAmount() != null && item.getAmount().compareTo(BigDecimal.ZERO) < 0) {
                            throw new BadRequestException("Amount cannot be negative for category: " + item.getBusinessType());
                        }
                    }
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

                if (rawTypes.isEmpty()) {
                    throw new BadRequestException("Investment Type is required when Business Generated is YES.");
                }

                for (String rawType : rawTypes) {
                    if (rawType == null || rawType.isBlank() || !PcBusinessType.isValid(rawType)) {
                        throw new BadRequestException("Unsupported business/service type: '" + rawType + "'. Supported types: " + PcBusinessType.SUPPORTED_TYPES);
                    }
                }

                boolean hasMonetary = rawTypes.stream().anyMatch(PcBusinessType::isMonetary);
                if (hasMonetary) {
                    if (request.getInvestmentAmount() == null || request.getInvestmentAmount().compareTo(BigDecimal.ZERO) <= 0) {
                        throw new BadRequestException("Investment Amount must be greater than zero when Business Generated is YES.");
                    }
                } else {
                    if (request.getInvestmentAmount() != null && request.getInvestmentAmount().compareTo(BigDecimal.ZERO) < 0) {
                        throw new BadRequestException("Investment Amount cannot be negative.");
                    }
                }
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

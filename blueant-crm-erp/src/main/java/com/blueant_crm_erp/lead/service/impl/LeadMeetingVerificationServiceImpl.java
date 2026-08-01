package com.blueant_crm_erp.lead.service.impl;

import com.blueant_crm_erp.lead.service.LeadMeetingVerificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class LeadMeetingVerificationServiceImpl implements LeadMeetingVerificationService {

    @Override
    public void verifyLeadMeeting(String uniqueLeadId, String pcUserEmail) {
        log.warn("Meeting Verification module is not yet implemented.");
        throw new UnsupportedOperationException("Meeting Verification module is not yet implemented.");
    }
    
    public int getVerifiedMeetingsCount() {
        log.warn("Meeting Verification module is not yet implemented. Returning 0 verified meetings.");
        return 0;
    }
}

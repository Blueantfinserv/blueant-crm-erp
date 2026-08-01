package com.blueant_crm_erp.lead.service.impl;

import com.blueant_crm_erp.lead.service.DuplicateTransferApprovalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class DuplicateTransferApprovalServiceImpl implements DuplicateTransferApprovalService {

    @Override
    public void approveDuplicateTransfer(String uniqueLeadId, String approverEmail) {
        log.warn("Transfer Approval module is not yet implemented.");
        throw new UnsupportedOperationException("Transfer Approval module is not yet implemented.");
    }

    @Override
    public boolean isTransferApproved(String uniqueLeadId) {
        log.warn("Transfer Approval module is not yet implemented. Returning false.");
        return false;
    }
}

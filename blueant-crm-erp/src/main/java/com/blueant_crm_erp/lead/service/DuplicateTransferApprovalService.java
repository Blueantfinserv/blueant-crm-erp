package com.blueant_crm_erp.lead.service;

public interface DuplicateTransferApprovalService {

    /**
     * Approves the transfer of a duplicate Lead by the previous owner.
     *
     * @param uniqueLeadId the unique lead ID
     * @param approverEmail the email of the previous owner approving the transfer
     */
    void approveDuplicateTransfer(String uniqueLeadId, String approverEmail);

    /**
     * Checks if a transfer has been explicitly approved for this lead.
     * 
     * @param uniqueLeadId the unique lead ID
     * @return true if approved
     */
    boolean isTransferApproved(String uniqueLeadId);
}

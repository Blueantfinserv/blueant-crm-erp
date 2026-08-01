package com.blueant_crm_erp.proposal.event;

import com.blueant_crm_erp.proposal.entity.Proposal;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * ============================================================================
 * Proposal Workflow Event
 * ============================================================================
 * Base class for all Proposal domain events.
 */
@Getter
public abstract class ProposalWorkflowEvent extends ApplicationEvent {

    private final Proposal proposal;
    private final String eventType;
    private final String description;
    private final String triggeredBy;

    public ProposalWorkflowEvent(Object source, Proposal proposal, String eventType, String description, String triggeredBy) {
        super(source);
        this.proposal = proposal;
        this.eventType = eventType;
        this.description = description;
        this.triggeredBy = triggeredBy;
    }
}

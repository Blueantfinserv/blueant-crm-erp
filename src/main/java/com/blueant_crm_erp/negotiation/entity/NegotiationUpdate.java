package com.blueant_crm_erp.negotiation.entity;

import com.blueant_crm_erp.negotiation.enums.NegotiationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "negotiation_updates")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NegotiationUpdate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "negotiation_id", nullable = false)
    private Negotiation negotiation;

    @Column(name = "update_number", nullable = false)
    private Integer updateNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "negotiation_status", nullable = false, length = 50)
    private NegotiationStatus negotiationStatus;

    @Column(name = "discussion", length = 2000)
    private String discussion;

    @Column(name = "agreed_amount")
    private BigDecimal agreedAmount;

    @Column(name = "submitted_by", nullable = false, length = 100)
    private String submittedBy;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}

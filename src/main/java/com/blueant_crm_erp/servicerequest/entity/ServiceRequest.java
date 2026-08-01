package com.blueant_crm_erp.servicerequest.entity;

import com.blueant_crm_erp.client.entity.Client;
import com.blueant_crm_erp.common.base.BaseVersionEntity;
import com.blueant_crm_erp.servicerequest.enums.ServiceRequestStatus;
import com.blueant_crm_erp.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Entity
@Table(name = "service_requests")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceRequest extends BaseVersionEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Column(name = "sr_code", nullable = false, unique = true, length = 50)
    private String srCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "sr_status", nullable = false, length = 50)
    private ServiceRequestStatus srStatus;

    @Column(name = "request_type", nullable = false, length = 50)
    private String requestType;

    @Column(name = "investment_amount")
    private BigDecimal investmentAmount;

    @Column(name = "product_type", length = 100)
    private String productType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_crm_id")
    private User assignedCrm;
}

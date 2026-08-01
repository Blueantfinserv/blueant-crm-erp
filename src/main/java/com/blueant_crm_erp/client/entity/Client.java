package com.blueant_crm_erp.client.entity;

import com.blueant_crm_erp.client.enums.ClientStatus;
import com.blueant_crm_erp.common.base.BaseVersionEntity;
import com.blueant_crm_erp.lead.entity.Lead;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "clients")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Client extends BaseVersionEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lead_id", nullable = false, unique = true)
    private Lead lead;

    @Column(name = "client_code", nullable = false, unique = true, length = 50)
    private String clientCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "client_status", nullable = false, length = 50)
    private ClientStatus clientStatus;

    @Column(name = "client_name", nullable = false, length = 150)
    private String clientName;

    @Column(name = "mobile_number", nullable = false, length = 20)
    private String mobileNumber;

    @Column(name = "email", length = 150)
    private String email;

    @Column(name = "pan_number", length = 20)
    private String panNumber;

    @Column(name = "amc", length = 100)
    private String amc;

    @Column(name = "scheme", length = 150)
    private String scheme;

    @Column(name = "investment_type", length = 50)
    private String investmentType;

    @Column(name = "client_since")
    private java.time.LocalDate clientSince;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "relationship_manager_id", foreignKey = @ForeignKey(name = "fk_client_rm"))
    private com.blueant_crm_erp.user.entity.User relationshipManager;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crm_owner_id", foreignKey = @ForeignKey(name = "fk_client_crm_owner"))
    private com.blueant_crm_erp.user.entity.User crmOwner;

}

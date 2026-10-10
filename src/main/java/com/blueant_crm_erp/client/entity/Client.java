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
    @JoinColumn(name = "lead_id", nullable = true, unique = true)
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

    @Column(name = "speciality", length = 100)
    private String speciality;

    @Column(name = "location", length = 150)
    private String location;

    @Column(name = "clinic_address", length = 500)
    private String clinicAddress;

    @Column(name = "alternate_mobile_number", length = 20)
    private String alternateMobileNumber;

    @Column(name = "remarks", length = 1000)
    private String remarks;

    @Column(name = "assignment_date")
    private java.time.LocalDate assignmentDate;

    @Column(name = "best_time_to_meet", length = 50)
    private String bestTimeToMeet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by_id", foreignKey = @ForeignKey(name = "fk_client_assigned_by"))
    private com.blueant_crm_erp.user.entity.User assignedBy;

    @Column(name = "assigned_at")
    private java.time.LocalDateTime assignedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "relationship_manager_id", foreignKey = @ForeignKey(name = "fk_client_rm"))
    private com.blueant_crm_erp.user.entity.User relationshipManager;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crm_owner_id", foreignKey = @ForeignKey(name = "fk_client_crm_owner"))
    private com.blueant_crm_erp.user.entity.User crmOwner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_sales_person_id", foreignKey = @ForeignKey(name = "fk_client_created_by_sales_person"))
    private com.blueant_crm_erp.user.entity.User createdBySalesPerson;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_person_id", foreignKey = @ForeignKey(name = "fk_client_sales_person"))
    private com.blueant_crm_erp.user.entity.User salesPerson;

    @Column(name = "next_followup_date")
    private java.time.LocalDate nextFollowupDate;

}

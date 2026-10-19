package com.blueant_crm_erp.client.entity;

import com.blueant_crm_erp.client.enums.FamilyMembershipStatus;
import com.blueant_crm_erp.client.enums.FamilyRelationship;
import com.blueant_crm_erp.common.base.BaseVersionEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Table(name = "client_family_members")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ClientFamilyMember extends BaseVersionEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id", nullable = false, foreignKey = @ForeignKey(name = "fk_cl_fammem_family"))
    private ClientFamily family;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false, foreignKey = @ForeignKey(name = "fk_cl_fammem_client"))
    private Client client;

    @Enumerated(EnumType.STRING)
    @Column(name = "relationship_to_head", length = 50)
    private FamilyRelationship relationshipToHead;

    @Enumerated(EnumType.STRING)
    @Column(name = "membership_status", nullable = false, length = 30)
    @Builder.Default
    private FamilyMembershipStatus membershipStatus = FamilyMembershipStatus.ACTIVE;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    @Column(name = "joined_by", nullable = false, length = 100)
    private String joinedBy;

    @Column(name = "left_at")
    private LocalDateTime leftAt;

    @Column(name = "left_by", length = 100)
    private String leftBy;

    /**
     * Stored generated column in MySQL enforcing single active family membership per client.
     */
    @Column(name = "active_client_id", insertable = false, updatable = false)
    private Long activeClientId;
}

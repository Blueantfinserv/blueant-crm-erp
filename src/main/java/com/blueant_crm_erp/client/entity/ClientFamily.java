package com.blueant_crm_erp.client.entity;

import com.blueant_crm_erp.client.enums.FamilyStatus;
import com.blueant_crm_erp.common.base.BaseVersionEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "client_families")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ClientFamily extends BaseVersionEntity {

    @Column(name = "family_code", nullable = false, unique = true, length = 50)
    private String familyCode;

    @Column(name = "family_name", nullable = false, length = 150)
    private String familyName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_head_client_id", foreignKey = @ForeignKey(name = "fk_cl_family_head"))
    private Client familyHead;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private FamilyStatus status = FamilyStatus.ACTIVE;

    @Builder.Default
    @OneToMany(mappedBy = "family", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ClientFamilyMember> members = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "family", cascade = CascadeType.ALL)
    private List<ClientFamilyHeadHistory> headHistories = new ArrayList<>();
}

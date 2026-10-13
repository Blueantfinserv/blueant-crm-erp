package com.blueant_crm_erp.client.entity;

import com.blueant_crm_erp.common.base.BaseVersionEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Entity
@Table(name = "client_nominees",
        indexes = {
                @Index(name = "idx_cl_nominee_client", columnList = "client_id")
        }
)
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ClientNominee extends BaseVersionEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false, foreignKey = @ForeignKey(name = "fk_cl_nominee_client"))
    private Client client;

    @Column(name = "nominee_name", nullable = false, length = 150)
    private String nomineeName;

    @Column(name = "relationship", nullable = false, length = 50)
    private String relationship;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "contact_number", length = 20)
    private String contactNumber;

    @Column(name = "pan_number", length = 20)
    private String panNumber;

    @Builder.Default
    @Column(name = "is_minor", nullable = false)
    private Boolean isMinor = Boolean.FALSE;

    @Column(name = "guardian_name", length = 150)
    private String guardianName;

    @Column(name = "guardian_relationship", length = 50)
    private String guardianRelationship;

    @Column(name = "guardian_contact", length = 20)
    private String guardianContact;

    @Column(name = "guardian_pan", length = 20)
    private String guardianPan;
}

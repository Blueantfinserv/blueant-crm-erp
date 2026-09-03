package com.blueant_crm_erp.lead.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "lead_code_sequences")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadCodeSequence {

    @Id
    @Column(name = "sequence_name", nullable = false, length = 50)
    private String sequenceName;

    @Column(name = "current_value", nullable = false)
    private Long currentValue;
}

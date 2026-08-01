package com.blueant_crm_erp.common.base;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * Base entity for all JPA entities.
 *
 * Contains only the primary key.
 * All project entities should extend this class.
 *
 * Example:
 * Role
 * User
 * Lead
 * Meeting
 * Client
 * Transaction
 */
@Getter
@Setter
@MappedSuperclass
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor

@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Primary Key
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    @Setter(AccessLevel.NONE)
    private Long id;

    /**
     * Two entities are considered equal
     * if they have the same database id.
     */
    @Override
    public boolean equals(Object object) {

        if (this == object) {
            return true;
        }

        if (object == null || getClass() != object.getClass()) {
            return false;
        }

        BaseEntity that = (BaseEntity) object;

        return id != null && Objects.equals(id, that.id);
    }

    /**
     * HashCode based on entity id.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Useful while debugging.
     */
    @Override
    public String toString() {
        return getClass().getSimpleName() +
                "{" +
                "id=" + id +
                '}';
    }
}
package com.sparrowx.internal.data.postgres.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;

import java.time.Instant;

@Getter
@Entity
@Table(
        name = "engineers",
        indexes = {
                @Index(
                        name = "idx_engineers_tenant_email",
                        columnList = "tenant_id,email",
                        unique = true
                ),
                @Index(
                        name = "idx_engineers_tenant_role",
                        columnList = "tenant_id,role"
                )
        }
)
public class EngineerEntity {

    @Id
    @Column(name = "engineer_id", nullable = false, updatable = false)
    private String engineerId;

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private String tenantId;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "role", nullable = false)
    private String role;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected EngineerEntity() {
    }

    public EngineerEntity(
            String engineerId,
            String tenantId,
            String fullName,
            String email,
            String role,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.engineerId = engineerId;
        this.tenantId = tenantId;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

}
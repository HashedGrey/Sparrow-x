package buildingblocks.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.time.Instant;
import java.util.Objects;

//@EnableJpaAuditing
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@FilterDef(name = "softDeleteFilter", parameters = @ParamDef(name = "isDeleted", type = boolean.class))
@Filter(name = "softDeleteFilter", condition = "is_deleted = :isDeleted")
@NoArgsConstructor
@AllArgsConstructor
@Getter
public abstract class BaseEntity<T> {

    @Id
    protected T id;

    @CreatedDate
    protected Instant createdAt;

    @CreatedBy
    protected Long createdBy;

    @LastModifiedDate
    protected Instant lastModified;

    @LastModifiedBy
    protected Long lastModifiedBy;

    @Version
    protected Long version;

    protected boolean isDeleted = false;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseEntity<?> that = (BaseEntity<?>) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
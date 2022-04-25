package gov.healthit.chpl.sharedstore;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "shared_store", schema = "shared_store")
public class SharedStoreEntity implements Serializable {
    private static final long serialVersionUID = -9211908180405673195L;

    @EmbeddedId
    private SharedStorePrimaryKey primaryKey;

    @Column(name = "value")
    private String value;

    @Column(name = "put_date")
    private LocalDateTime putDate;

    public SharedStore toDomain() {
        return SharedStore.builder()
                .domain(this.getPrimaryKey().getDomain())
                .key(this.getPrimaryKey().getKey())
                .value(this.getValue())
                .putDate(this.getPutDate())
                .build();
    }
}

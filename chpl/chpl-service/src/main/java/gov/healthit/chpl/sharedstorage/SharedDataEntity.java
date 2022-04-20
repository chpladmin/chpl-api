package gov.healthit.chpl.sharedstorage;

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
@Table(name = "shared_data", schema = "shared_data")
public class SharedDataEntity implements Serializable {
    private static final long serialVersionUID = -9211908180405673195L;

    @EmbeddedId
    private SharedDataPrimaryKey primaryKey;

    @Column(name = "value")
    private String value;

    @Column(name = "put_date")
    private LocalDateTime putDate;

    public SharedData toDomain() {
        return SharedData.builder()
                .domain(this.getPrimaryKey().getDomain())
                .key(this.getPrimaryKey().getKey())
                .value(this.getValue())
                .putDate(this.getPutDate())
                .build();
    }
}

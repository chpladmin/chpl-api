package gov.healthit.chpl.sharedstorage;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "shared_data")
public class SharedDataEntity implements Serializable {
    private static final long serialVersionUID = -9211908180405673195L;

    private SharedDataPrimaryKey primaryKey;
    private String value;
    private LocalDateTime putDate;

    public SharedData toDomain() {
        return SharedData.builder()
                .type(this.getPrimaryKey().getType())
                .key(this.getPrimaryKey().getKey())
                .value(this.getValue())
                .putDate(this.getPutDate())
                .build();
    }
}

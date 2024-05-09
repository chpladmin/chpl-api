package gov.healthit.chpl.entity.developer;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.annotations.Type;

import gov.healthit.chpl.domain.DeveloperStatus;
import gov.healthit.chpl.entity.EntityAudit;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@ToString
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "vendor_status")
public class DeveloperStatusEntity extends EntityAudit {
    private static final long serialVersionUID = 1730728043307135377L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "vendor_status_id", nullable = false)
    private Long id;

    @Column(name = "name")
    @Type(type = "gov.healthit.chpl.entity.developer.PostgresDeveloperStatusType", parameters = {
            @org.hibernate.annotations.Parameter(name = "enumClassName",
                    value = "gov.healthit.chpl.entity.developer.DeveloperStatusType")
    })
    private DeveloperStatusType name;

    public DeveloperStatus toDomain() {
        return DeveloperStatus.builder()
                .id(this.getId())
                .status(this.getName().getName())
                .build();
    }
}

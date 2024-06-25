package gov.healthit.chpl.listing.measure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import gov.healthit.chpl.domain.MeasureDomain;
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
@Table(name = "measure_domain")
public class MeasureDomainEntity extends EntityAudit {
    private static final long serialVersionUID = -9220228565137094298L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "domain")
    private String domain;

    public MeasureDomain convert() {
        MeasureDomain measureDomain = new MeasureDomain();
        measureDomain.setId(getId());
        measureDomain.setName(getDomain());
        return measureDomain;
    }
}

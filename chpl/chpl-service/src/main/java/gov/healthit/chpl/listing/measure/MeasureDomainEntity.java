package gov.healthit.chpl.listing.measure;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

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

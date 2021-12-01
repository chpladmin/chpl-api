package gov.healthit.chpl.listing.measure;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import gov.healthit.chpl.domain.MeasureDomain;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "measure_domain")
public class MeasureDomainEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "domain")
    private String domain;

    @Column(name = "creation_date", nullable = false, updatable = false, insertable = false)
    private Date creationDate;

    @Column(nullable = false)
    private Boolean deleted;

    @Column(name = "last_modified_date", nullable = false, updatable = false, insertable = false)
    private Date lastModifiedDate;

    @Column(name = "last_modified_user", nullable = false)
    private Long lastModifiedUser;

    public MeasureDomain convert() {
        MeasureDomain measureDomain = new MeasureDomain();
        measureDomain.setId(getId());
        measureDomain.setName(getDomain());
        return measureDomain;
    }
}

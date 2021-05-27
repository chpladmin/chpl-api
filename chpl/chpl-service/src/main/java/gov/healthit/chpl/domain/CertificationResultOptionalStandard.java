package gov.healthit.chpl.domain;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.entity.listing.CertificationResultOptionalStandardEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * An optional standard used to meet a certification criterion. You can find a list of
 * potential values in the 2015 Functionality and Standards Reference
 * Tables.
 *
 */
@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@AllArgsConstructor
public class CertificationResultOptionalStandard implements Serializable {
    private static final long serialVersionUID = -9182555768595891414L;

    /**
     * Optional standard to certification result mapping internal ID
     */
    @XmlElement(required = true)
    private Long id;

    /**
     * The Optional Standard used to test the associated criteria.
     */
    @XmlElement(required = false, nillable = true)
    private OptionalStandard optionalStandard;

    public CertificationResultOptionalStandard() {
        super();
    }

    public CertificationResultOptionalStandard(CertificationResultOptionalStandardEntity entity) {
        this.id = entity.getId();
        OptionalStandard os = new OptionalStandard();
        if (entity.getOptionalStandard() != null) {
            os.setId(entity.getOptionalStandard().getId());
            os.setName(entity.getOptionalStandard().getName());
            os.setDescription(entity.getOptionalStandard().getDescription());
        }
        this.optionalStandard = os;
    }

    public boolean matches(CertificationResultOptionalStandard anotherStd) {
        boolean result = false;
        if (this.getOptionalStandard() != null && anotherStd.getOptionalStandard() != null
                && this.getOptionalStandard().getId().longValue() == anotherStd.getOptionalStandard().getId().longValue()) {
            result = true;
        }
        return result;
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public OptionalStandard getOptionalStandard() {
        return optionalStandard;
    }

    public void setOptionalStandard(OptionalStandard optionalStandard) {
        this.optionalStandard = optionalStandard;
    }

}

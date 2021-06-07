package gov.healthit.chpl.optionalStandard.domain;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
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
     * Optional standard to certification result mapping internal ID.
     */
    @XmlElement(required = true)
    private Long id;

    @XmlTransient
    private OptionalStandard optionalStandard;

    /**
     * The Optional Standard internal identifier.
     */
    @XmlElement(required = true)
    private Long optionalStandardId;

    /**
     * The Optional Standard used to test the associated criteria.
     */
    @XmlElement(required = true)
    private String standard;

    public CertificationResultOptionalStandard() {
        super();
    }

    public CertificationResultOptionalStandard(CertificationResultOptionalStandardEntity entity) {
        this.id = entity.getId();
        OptionalStandard os = new OptionalStandard();
        if (entity.getOptionalStandard() != null) {
            os.setId(entity.getOptionalStandard().getId());
            os.setOptionalStandard(entity.getOptionalStandard().getOptionalStandard());
        }
        this.optionalStandard = os;
        this.standard = os.getOptionalStandard();
        this.optionalStandardId = os.getId();
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

    public void setId(Long id) {
        this.id = id;
    }

    public OptionalStandard getOptionalStandard() {
        return optionalStandard;
    }

    public void setOptionalStandard(OptionalStandard optionalStandard) {
        this.optionalStandard = optionalStandard;
    }

    public Long getOptionalStandardId() {
        return optionalStandardId;
    }

    public void setOptionalStandardId(Long optionalStandardId) {
        this.optionalStandardId = optionalStandardId;
    }

    public String getStandard() {
        return standard;
    }

    public void setStandard(String standard) {
        this.standard = standard;
    }
}

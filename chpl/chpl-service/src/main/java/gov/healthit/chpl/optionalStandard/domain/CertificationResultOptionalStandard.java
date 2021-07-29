package gov.healthit.chpl.optionalStandard.domain;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.entity.listing.CertificationResultOptionalStandardEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.ToString;

/**
 * An optional standard used to meet a certification criterion. You can find a list of
 * potential values in the 2015 Functionality and Standards Reference
 * Tables.
 *
 */
@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@Builder
@ToString
public class CertificationResultOptionalStandard implements Serializable {
    private static final long serialVersionUID = -9182555768595891414L;

    /**
     * Optional standard to certification result mapping internal ID.
     */
    @XmlElement(required = true)
    private Long id;

    /**
     * The Optional Standard internal identifier.
     */
    @XmlElement(required = true)
    private Long optionalStandardId;

    /**
     * The citation for the Optional Standard used to test the associated criteria.
     */
    @XmlElement(required = true)
    private String citation;

    /**
     * The description of the Optional Standard used to test the associated criteria.
     */
    @XmlElement(required = true)
    private String description;

    public CertificationResultOptionalStandard() {
        super();
    }

    public CertificationResultOptionalStandard(CertificationResultOptionalStandardEntity entity) {
        this.id = entity.getId();
        if (entity.getOptionalStandard() != null) {
            this.optionalStandardId = entity.getOptionalStandard().getId();
            this.citation = entity.getOptionalStandard().getCitation();
            this.description = entity.getOptionalStandard().getDescription();
        }
    }

    public boolean matches(CertificationResultOptionalStandard existingItem) {
        return this.optionalStandardId.longValue() == existingItem.getOptionalStandardId().longValue()
                || this.citation.equalsIgnoreCase(existingItem.getCitation());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOptionalStandardId() {
        return optionalStandardId;
    }

    public void setOptionalStandardId(Long optionalStandardId) {
        this.optionalStandardId = optionalStandardId;
    }

    public String getCitation() {
        return citation;
    }

    public void setCitation(String citation) {
        this.citation = citation;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

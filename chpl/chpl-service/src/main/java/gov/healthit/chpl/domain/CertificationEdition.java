package gov.healthit.chpl.domain;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.api.deprecatedUsage.DeprecatedResponseField;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.xml.bind.annotation.XmlTransient;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * The certification edition. It takes a value of 2011, 2014 or 2015.
 */
@AllArgsConstructor
@Builder
@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CertificationEdition implements Serializable {
    private static final long serialVersionUID = 5732322243572571895L;

    @XmlTransient
    @JsonIgnore
    public static final String CURES_SUFFIX = " Cures Update";

    @Deprecated
    @DeprecatedResponseField(message = "Please use the 'id' field", removalDate = "2024-01-01")
    @XmlTransient
    private Long certificationEditionId;

    /**
     * The internal ID of the edition.
     */
    @Schema(description = "The internal ID of the edition.")
    @XmlElement(required = true, nillable = false)
    private Long id;

    @Deprecated
    @DeprecatedResponseField(message = "Please use the 'name' field", removalDate = "2024-01-01")
    @XmlTransient
    private String year;

    /**
     * The name of the edition.
     */
    @Schema(description = "The name of the edition.")
    @XmlElement(required = true, nillable = false)
    private String name;

    /**
     * Whether or not the edition has been retired.
     */
    @Schema(description = "Whether or not the edition has been retired.")
    @XmlElement(required = true)
    private boolean retired;

    public CertificationEdition() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Deprecated
    public Long getCertificationEditionId() {
        return certificationEditionId;
    }

    @Deprecated
    public void setCertificationEditionId(final Long certificationEditionId) {
        this.certificationEditionId = certificationEditionId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Deprecated
    public String getYear() {
        return year;
    }

    @Deprecated
    public void setYear(final String year) {
        this.year = year;
    }

    public boolean isRetired() {
        return retired;
    }

    public void setRetired(final boolean retired) {
        this.retired = retired;
    }
}

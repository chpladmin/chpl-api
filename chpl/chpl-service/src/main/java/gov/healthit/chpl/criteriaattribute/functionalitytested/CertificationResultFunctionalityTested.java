package gov.healthit.chpl.criteriaattribute.functionalitytested;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorOrder;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.api.deprecatedUsage.DeprecatedResponseField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Any optional, alternative, ambulatory (2015 only), or inpatient (2015 only) capabilities within a certification
 * criterion to which the Health IT module was tested and certified. For example, within the 2015 certification criteria
 * 170.315(a), the optional functionality to include a "reason for order" field should be denoted as "(a)(1)(ii)". You
 * can find a list of potential values in the 2014 or 2015 Functionality and Standards Reference Tables.
 *
 */
@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlAccessorOrder(value = XmlAccessOrder.ALPHABETICAL)
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class CertificationResultFunctionalityTested implements Serializable {
    private static final long serialVersionUID = -1647645050538126758L;

    /**
     * Functionality tested to certification result mapping internal ID
     */
    @Schema(description = "Functionality tested to certification result mapping internal ID")
    @XmlElement(required = true)
    private Long id;

    /**
     * Functionality Tested
     */
    @Schema(description = "Functionality tested internal ID")
    @XmlElement(required = true)
    private FunctionalityTested functionalityTested;

    @Deprecated
    @DeprecatedResponseField(message = "This field is deprecated and will be removed. This data can be found functionalityTested.id",
            removalDate = "2024-01-01")
    @XmlTransient
    private Long functionalityTestedId;

    @Deprecated
    @DeprecatedResponseField(message = "This field is deprecated and will be removed. This data can be found functionalityTested.value",
            removalDate = "2024-01-01")
    @XmlTransient
    private String description;

    @Deprecated
    @DeprecatedResponseField(message = "This field is deprecated and will be removed. This data can be found functionalityTested.regulatoryTextCitation",
            removalDate = "2024-01-01")
    @XmlTransient
    private String name;

    @XmlTransient
    @JsonIgnore
    private Long certificationResultId;

    public boolean matches(CertificationResultFunctionalityTested anotherFunc) {
        boolean result = false;
        if (this.getFunctionalityTested().getId() != null && anotherFunc.getFunctionalityTested().getId() != null
                && this.getFunctionalityTested().getId().longValue() == anotherFunc.getFunctionalityTested().getId().longValue()) {
            result = true;
        } else if (!StringUtils.isEmpty(this.getFunctionalityTested().getRegulatoryTextCitation())
                    && !StringUtils.isEmpty(anotherFunc.getFunctionalityTested().getRegulatoryTextCitation())
                && this.getFunctionalityTested().getRegulatoryTextCitation().equalsIgnoreCase(anotherFunc.getFunctionalityTested().getRegulatoryTextCitation())) {
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

    @Deprecated
    public Long getFunctionalityTestedId() {
        return functionalityTestedId;
    }

    @Deprecated
    public void setFunctionalityTestedId(Long functionalityTestedId) {
        this.functionalityTestedId = functionalityTestedId;
    }

    @Deprecated
    public String getDescription() {
        return description;
    }

    @Deprecated
    public void setDescription(final String description) {
        this.description = description;
    }

    @Deprecated
    public String getName() {
        return name;
    }

    @Deprecated
    public void setName(final String name) {
        this.name = name;
    }

    public Long getCertificationResultId() {
        return this.certificationResultId;
    }

    public void setCertificationResultId(Long certificationResultId) {
        this.certificationResultId = certificationResultId;
    }

    public FunctionalityTested getFunctionalityTested() {
        return functionalityTested;
    }

    public void setFunctionalityTested(FunctionalityTested functionalityTested) {
        this.functionalityTested = functionalityTested;
    }

}

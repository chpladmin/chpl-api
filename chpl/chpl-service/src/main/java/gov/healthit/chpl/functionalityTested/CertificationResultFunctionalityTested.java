package gov.healthit.chpl.functionalityTested;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
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
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@ToString
@Builder
public class CertificationResultFunctionalityTested implements Serializable {
    private static final long serialVersionUID = -1647645050538126758L;

    /**
     * Functionality tested to certification result mapping internal ID
     */
    @XmlElement(required = true)
    private Long id;

    /**
     * Functionality tested internal ID
     */
    @XmlElement(required = true)
    private Long functionalityTestedId;

    /**
     * Description of functionality tested
     */
    @XmlElement(required = false, nillable = true)
    private String description;

    /**
     * Name of functionality tested
     */
    @XmlElement(required = true)
    private String name;

    @XmlTransient
    @JsonIgnore
    private Long certificationResultId;

    public CertificationResultFunctionalityTested() {
    }

    public boolean matches(CertificationResultFunctionalityTested anotherFunc) {
        boolean result = false;
        if (this.getFunctionalityTestedId() != null && anotherFunc.getFunctionalityTestedId() != null
                && this.getFunctionalityTestedId().longValue() == anotherFunc.getFunctionalityTestedId().longValue()) {
            result = true;
        } else if (!StringUtils.isEmpty(this.getName()) && !StringUtils.isEmpty(anotherFunc.getName())
                && this.getName().equalsIgnoreCase(anotherFunc.getName())) {
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

    public Long getFunctionalityTestedId() {
        return functionalityTestedId;
    }

    public void setFunctionalityTestedId(Long functionalityTestedId) {
        this.functionalityTestedId = functionalityTestedId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Long getCertificationResultId() {
        return this.certificationResultId;
    }

    public void setCertificationResultId(Long certificationResultId) {
        this.certificationResultId = certificationResultId;
    }

}

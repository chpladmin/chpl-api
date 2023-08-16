package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.LinkedHashSet;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang3.ObjectUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.dto.CertificationResultUcdProcessDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * The user-centered design (UCD) process applied for the corresponding
 * certification criteria
 *
 */
@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@AllArgsConstructor
public class CertifiedProductUcdProcess implements Serializable {
    private static final long serialVersionUID = 7248865611086710891L;

    /**
     * The internal ID of the UCD process.
     */
    @XmlElement(required = true)
    private Long id;

    /**
     * The name of the user-centered design (UCD) process applied for the corresponding
     * certification criteria
     */
    @XmlElement(required = true)
    private String name;

    @XmlTransient
    @JsonIgnore
    private String userEnteredName;

    /**
     * A description of the UCD process used. This variable is applicable for
     * 2014 and 2015 Edition, and a string variable that does not take any
     * restrictions on formatting or values.
     */
    @XmlElement(required = false, nillable = true)
    private String details;

    /**
     * The set of criteria within a listing to which this UCD process is
     * applied.
     */
    @XmlElementWrapper(name = "criteriaList", nillable = true, required = false)
    @XmlElement(required = false, nillable = true)
    @Builder.Default
    private LinkedHashSet<CertificationCriterion> criteria = new LinkedHashSet<CertificationCriterion>();

    public CertifiedProductUcdProcess() {
        super();
        this.criteria = new LinkedHashSet<CertificationCriterion>();
    }

    public CertifiedProductUcdProcess(CertificationResultUcdProcessDTO dto) {
        this();
        this.id = dto.getUcdProcessId();
        this.name = dto.getUcdProcessName();
        this.details = dto.getUcdProcessDetails();
    }

    public boolean matches(CertifiedProductUcdProcess anotherUcd) {
        boolean result = false;
        if (ObjectUtils.allNotNull(this.getId(), anotherUcd.getId())
                && this.getId().equals(anotherUcd.getId())) {
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

    public String getName() {
        return name;
    }

    public void setName(String ucdProcessName) {
        this.name = ucdProcessName;
    }

    public String getUserEnteredName() {
        return userEnteredName;
    }

    public void setUserEnteredName(String userEnteredName) {
        this.userEnteredName = userEnteredName;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String ucdProcessDetails) {
        this.details = ucdProcessDetails;
    }

    public LinkedHashSet<CertificationCriterion> getCriteria() {
        return criteria;
    }

    public void setCriteria(LinkedHashSet<CertificationCriterion> criteria) {
        this.criteria = criteria;
    }
}

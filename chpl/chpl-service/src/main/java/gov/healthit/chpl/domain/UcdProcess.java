package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.dto.CertificationResultUcdProcessDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Singular;

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
public class UcdProcess implements Serializable {
    private static final long serialVersionUID = 7248865611086710891L;

    /**
     * UCD process to certification result internal mapping ID
     */
    @XmlElement(required = true)
    private Long id;

    /**
     * The user-centered design (UCD) process applied for the corresponding
     * certification criteria
     */
    @XmlElement(required = true)
    private String name;

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
    @Singular("criterion")
    private Set<CertificationCriterion> criteria;

    public UcdProcess() {
        super();
        this.criteria = new HashSet<CertificationCriterion>();
    }

    public UcdProcess(CertificationResultUcdProcessDTO dto) {
        this();
        this.id = dto.getUcdProcessId();
        this.name = dto.getUcdProcessName();
        this.details = dto.getUcdProcessDetails();
    }

    public boolean matches(final UcdProcess anotherUcd) {
        boolean result = false;
        if (this.getId() != null && anotherUcd.getId() != null
                && this.getId().longValue() == anotherUcd.getId().longValue()) {
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

    public String getName() {
        return name;
    }

    public void setName(final String ucdProcessName) {
        this.name = ucdProcessName;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(final String ucdProcessDetails) {
        this.details = ucdProcessDetails;
    }

    public Set<CertificationCriterion> getCriteria() {
        return criteria;
    }

    public void setCriteria(final Set<CertificationCriterion> criteria) {
        this.criteria = criteria;
    }
}

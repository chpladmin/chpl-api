package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang3.ObjectUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.ToString;

@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@ToString
@AllArgsConstructor
public class Measure implements Serializable {
    private static final long serialVersionUID = 3070401446291821552L;

    /**
     * An internal ID for each valid measure
     */
    @XmlElement(required = true)
    private Long id;

    @XmlElement(required = true)
    private MeasureDomain domain;

    /**
     * Abbreviation of the Required Test. Examples are "RT7" or "RT9"
     */
    @XmlElement(required = true)
    private String abbreviation;

    /**
     * The required test associated with each measure. For example, "Required Test 10: Stage 2 Objective 3 Measure 1 and
     * Stage 3 Objective 4 Measure 1"
     */
    @XmlElement(required = true)
    private String requiredTest;

    /**
     * The name of the measure. For example, "Computerized Provider Order Entry -
     * Medications: Eligible Hospital/Critical"
     */
    @XmlElement(required = true)
    private String name;

    /**
     * Whether or not this measure requires criteria to be designated as associated with it.
     */
    @XmlElement(required = true)
    private Boolean requiresCriteriaSelection;

    /**
     * A flag indicating whether or not the measure has been marked as removed.
     */
    @XmlElement(required = true)
    private Boolean removed;

    @XmlElementWrapper(name = "allowedCriteria", nillable = true, required = false)
    @XmlElement(required = true, name = "criteria")
    @Builder.Default
    private Set<CertificationCriterion> allowedCriteria = new LinkedHashSet<CertificationCriterion>();

    public Measure() {
        super();
    }

    // not overriding equals on purpose
    // this is meant to determine if a user would think two measures
    // are the same, not as thorough as equals
    public boolean matches(Measure anotherMeasure) {
        if (this.id == null && anotherMeasure.id == null) {
            return false;
        } else if (this.id == null && anotherMeasure.id != null || this.id != null && anotherMeasure.id == null) {
            return false;
        } else if (ObjectUtils.allNotNull(this.id, anotherMeasure.id)
                && this.id.longValue() != anotherMeasure.id.longValue()) {
            return false;
        }

        return true;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getRemoved() {
        return removed;
    }

    public void setRemoved(Boolean removed) {
        this.removed = removed;
    }

    public String getRequiredTest() {
        return requiredTest;
    }

    public void setRequiredTest(String requiredTest) {
        this.requiredTest = requiredTest;
    }

    public Boolean getRequiresCriteriaSelection() {
        return requiresCriteriaSelection;
    }

    public void setRequiresCriteriaSelection(Boolean requiresCriteriaSelection) {
        this.requiresCriteriaSelection = requiresCriteriaSelection;
    }

    public MeasureDomain getDomain() {
        return domain;
    }

    public void setDomain(MeasureDomain domain) {
        this.domain = domain;
    }

    public Set<CertificationCriterion> getAllowedCriteria() {
        return allowedCriteria;
    }

    public void setAllowedCriteria(Set<CertificationCriterion> allowedCriteria) {
        this.allowedCriteria = allowedCriteria;
    }
}

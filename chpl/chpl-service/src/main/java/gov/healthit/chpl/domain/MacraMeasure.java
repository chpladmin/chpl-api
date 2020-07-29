package gov.healthit.chpl.domain;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorOrder;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.dto.MacraMeasureDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;

@XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@AllArgsConstructor
public class MacraMeasure implements Serializable {
    private static final long serialVersionUID = 3070401446291821552L;

    /**
     * An internal ID for each valid measure
     */
    @XmlElement(required = false, nillable = true)
    private Long id;

    /**
     * The criteria for which a given measure is valid.
     */
    @XmlElement(required = false, nillable = true)
    private CertificationCriterion criteria;

    @XmlElement(required = true)
    private String abbreviation;

    /**
     * The name of the measure that was successfully tested. For example, "Computerized Provider Order Entry -
     * Medications: Eligible Hospital/Critical"
     */
    @XmlElement(required = false, nillable = true)
    private String name;

    /**
     * The required test associated with each measure. For example, "Required Test 10: Stage 2 Objective 3 Measure 1 and
     * Stage 3 Objective 4 Measure 1"
     */
    @XmlElement(required = false, nillable = true)
    private String description;

    /**
     * A flag indicating whether or not the measure has been marked as removed.
     */
    @XmlElement(required = true, nillable = false)
    private Boolean removed;

    public MacraMeasure() {
    }

    public MacraMeasure(final MacraMeasureDTO dto) {
        this.id = dto.getId();
        if (dto.getCriteria() != null) {
            this.criteria = new CertificationCriterion(dto.getCriteria());
        } else {
            this.criteria = new CertificationCriterion();
            this.criteria.setId(dto.getCriteriaId());
        }
        this.abbreviation = dto.getValue();
        this.name = dto.getName();
        this.description = dto.getDescription();
        this.removed = dto.getRemoved();
    }

    // not overriding equals on purpose
    // this is meant to determine if a user would think two macra measures
    // are the same, not as thorough as equals
    public boolean matches(MacraMeasure anotherMeasure) {
        if (!ObjectUtils.allNotNull(this.getId(), anotherMeasure.getId())) {
            return this.getId().equals(anotherMeasure.getId());
        } else {
            return !StringUtils.isEmpty(this.getAbbreviation()) && !StringUtils.isEmpty(anotherMeasure.getAbbreviation())
                && this.getAbbreviation().equalsIgnoreCase(anotherMeasure.getAbbreviation())
                && !StringUtils.isEmpty(this.getName()) && !StringUtils.isEmpty(anotherMeasure.getName())
                && this.getName().equalsIgnoreCase(anotherMeasure.getName());
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public CertificationCriterion getCriteria() {
        return criteria;
    }

    public void setCriteria(final CertificationCriterion criteria) {
        this.criteria = criteria;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(final String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public Boolean getRemoved() {
        return removed;
    }

    public void setRemoved(final Boolean removed) {
        this.removed = removed;
    }
}

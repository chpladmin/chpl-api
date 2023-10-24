package gov.healthit.chpl.standard;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorOrder;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.criteriaattribute.rule.Rule;
import gov.healthit.chpl.util.LocalDateAdapter;
import gov.healthit.chpl.util.LocalDateDeserializer;
import gov.healthit.chpl.util.LocalDateSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

//TODO OCD-4333 - NEED TEXT TO DESCRIBE THIS OBJECT
@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlAccessorOrder(value = XmlAccessOrder.ALPHABETICAL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Standard implements Serializable {
    private static final long serialVersionUID = -5058475461826923683L;

    /**
     * Criteria Attribute internal ID
     */
    @XmlElement(required = true)
    private Long id;

    /**
     * A string value to represent the value to be used for the Standard.
     */
    @XmlElement(required = true)
    private String value;

    /**
     * A string value representing a law and section (e.g., 170.202(a)).
     */
    @XmlElement(required = false, nillable = true)
    private String regulatoryTextCitation;


    /**
     * A date value representing the date by which the Standard became available.
     */
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    @XmlElement(required = false, nillable = true)
    @XmlJavaTypeAdapter(value = LocalDateAdapter.class)
    private LocalDate startDay;

    /**
     * A date value representing the date by which the Standard can no longer be used.
     */
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    @XmlElement(required = false, nillable = true)
    @XmlJavaTypeAdapter(value = LocalDateAdapter.class)
    private LocalDate endDay;

    /**
     * A date value representing the date by which the Standard is required for selected criteria.
     */
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    @XmlElement(required = false, nillable = true)
    @XmlJavaTypeAdapter(value = LocalDateAdapter.class)
    private LocalDate requiredDay;

    // Do not include this property if the value is "empty". It will be empty when generating listing details
    // and will be non-empty (this included) when doing CRUD operations on Standards
    @JsonInclude(value = Include.NON_EMPTY)
    @XmlTransient
    @Builder.Default
    private List<CertificationCriterion> criteria = new ArrayList<CertificationCriterion>();

    /**
     * The rule which this Criteria Attrbute is associated with.
     */
    @XmlElement(required = false, nillable = true)
    private Rule rule;

    @XmlTransient
    public Boolean isRetired() {
        LocalDate end = endDay != null ? endDay : LocalDate.MAX;
        return end.isBefore(LocalDate.now());
    }

    /*
     * TODO: OCD-4333 NEED THIS TEXT
     */
    @XmlElement(required = false)
    private String additionalInformation;

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (getClass() != o.getClass()) {
            return false;
        }
        Standard functionalityTested = (Standard) o;
        return Objects.equals(getId(), functionalityTested.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getId());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getRegulatoryTextCitation() {
        return regulatoryTextCitation;
    }

    public void setRegulatoryTextCitation(String regulatoryTextCitation) {
        this.regulatoryTextCitation = regulatoryTextCitation;
    }

    public LocalDate getStartDay() {
        return startDay;
    }

    public void setStartDay(LocalDate startDay) {
        this.startDay = startDay;
    }

    public LocalDate getEndDay() {
        return endDay;
    }

    public void setEndDay(LocalDate endDay) {
        this.endDay = endDay;
    }

    public LocalDate getRequiredDay() {
        return requiredDay;
    }

    public void setRequiredDay(LocalDate requiredDay) {
        this.requiredDay = requiredDay;
    }

    public List<CertificationCriterion> getCriteria() {
        return criteria;
    }

    public void setCriteria(List<CertificationCriterion> criteria) {
        this.criteria = criteria;
    }

    public Rule getRule() {
        return rule;
    }

    public void setRule(Rule rule) {
        this.rule = rule;
    }

}

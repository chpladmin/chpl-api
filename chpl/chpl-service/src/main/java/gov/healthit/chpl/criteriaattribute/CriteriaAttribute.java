package gov.healthit.chpl.criteriaattribute;

import java.io.Serializable;
import java.time.LocalDate;

import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorOrder;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import gov.healthit.chpl.util.LocalDateAdapter;
import gov.healthit.chpl.util.LocalDateDeserializer;
import gov.healthit.chpl.util.LocalDateSerializer;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
public class CriteriaAttribute implements Serializable {
    private static final long serialVersionUID = 2856878300304895096L;

    /**
     * TODO: OCD-4242 - Need this text
     */
    @XmlElement(required = true)
    private Long id;

    /**
     * This is the text assoc with the element
     */
    @XmlElement(required = true)
    private String value;

    /**
     * TODO: OCD-4242 - Need this text
     */
    @XmlElement(required = false, nillable = true)
    private String regulationTextCitation;


    /**
     * TODO: OCD-4242 - Need this text
     */
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    @XmlElement(required = false, nillable = true)
    @XmlJavaTypeAdapter(value = LocalDateAdapter.class)
    private LocalDate startDay;

    /**
     * TODO: OCD-4242 - Need this text
     */
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    @XmlElement(required = false, nillable = true)
    @XmlJavaTypeAdapter(value = LocalDateAdapter.class)
    private LocalDate endDay;

    /**
     * TODO: OCD-4242 - Need this text
     */
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    @XmlElement(required = false, nillable = true)
    @XmlJavaTypeAdapter(value = LocalDateAdapter.class)
    private LocalDate requiredDay;

    /**
     * TODO: OCD-4242 - Need this text
     */
    @XmlElement(required = false, nillable = true)
    private Rule rule;

    @JsonIgnore
    @XmlTransient
    public Boolean isRetired() {
        LocalDate start = startDay != null ? startDay : LocalDate.MIN;
        LocalDate end = endDay != null ? endDay : LocalDate.MAX;
        return LocalDate.now().compareTo(start) >= 0 && LocalDate.now().compareTo(end) <= 0;
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

    public String getRegulationTextCitation() {
        return regulationTextCitation;
    }

    public void setRegulationTextCitation(String regulationTextCitation) {
        this.regulationTextCitation = regulationTextCitation;
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

    public Rule getRule() {
        return rule;
    }

    public void setRule(Rule rule) {
        this.rule = rule;
    }
}

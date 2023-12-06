package gov.healthit.chpl.criteriaattribute;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.criteriaattribute.rule.Rule;
import gov.healthit.chpl.util.LocalDateDeserializer;
import gov.healthit.chpl.util.LocalDateSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CriteriaAttribute implements Serializable {
    private static final long serialVersionUID = 2856878300304895096L;

    @Schema(description = "Criteria Attribute internal ID")
    private Long id;

    @Schema(description = "A string value to represent the value to be used for the Criteria Attribute.")
    private String value;

    @Schema(description = "A string value representing a law and section (e.g., 170.202(a)).")
    private String regulatoryTextCitation;


    @Schema(description = "A date value representing the date by which the Criteria Attribute became available.")
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate startDay;

    @Schema(description = "A date value representing the date by which the Criteria Attribute can no longer be used.")
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate endDay;

    @Schema(description = "A date value representing the date by which the Criteria Attribute is required for selected criteria.")
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate requiredDay;

    private List<CertificationCriterion> criteria;

    @Schema(description = "The rule which this Criteria Attrbute is associated with.")
    private Rule rule;

    public Boolean isRetired() {
        LocalDate end = endDay != null ? endDay : LocalDate.MAX;
        return end.isBefore(LocalDate.now());
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

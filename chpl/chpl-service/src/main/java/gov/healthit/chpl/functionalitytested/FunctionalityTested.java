package gov.healthit.chpl.functionalitytested;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import gov.healthit.chpl.api.deprecatedUsage.DeprecatedResponseField;
import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.criteriaattribute.rule.Rule;
import gov.healthit.chpl.domain.PracticeType;
import gov.healthit.chpl.util.LocalDateDeserializer;
import gov.healthit.chpl.util.LocalDateSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


// TODO OCD-4288 - NEED TEXT TO DESCRIBE THIS OBJECT
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class FunctionalityTested implements Serializable {
    private static final long serialVersionUID = 620315627813874301L;

    private Long id;

    @Deprecated
    @DeprecatedResponseField(message = "This field is deprecated and will be removed. This data can be found value",
            removalDate = "2024-01-01")
    private String name;

    @Deprecated
    @DeprecatedResponseField(message = "This field is deprecated and will be removed. This data can be found regulatoryTextCitation",
            removalDate = "2024-01-01")
    private String description;

    @Schema(description = "A string value to represent the value to be used for the Criteria Attribute.")
    private String value;

    @Schema(description = "A string value representing a law and section (e.g., 170.202(a))..")
    private String regulatoryTextCitation;


    @Schema(description = " A date value representing the date by which the Criteria Attribute became available.")
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

    // Do not include this property if the value is "empty". It will be empty when generating listing details
    // and will be non-empty (this included) when doing CRUD operations on functionality tested
    @JsonInclude(value = Include.NON_EMPTY)
    @Builder.Default
    private List<CertificationCriterion> criteria = new ArrayList<CertificationCriterion>();

    @Schema(description = "The rule which this Criteria Attrbute is associated with.")
    private Rule rule;

    public Boolean isRetired() {
        LocalDate end = endDay != null ? endDay : LocalDate.MAX;
        return end.isBefore(LocalDate.now());
    }

    /*
     * TODO: OCD-4288 NEED THIS TEXT
     */
    private String additionalInformation;

    /*
     * TODO: OCD-4288 NEED THIS TEXT
     */
    private PracticeType practiceType;

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
        FunctionalityTested functionalityTested = (FunctionalityTested) o;
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

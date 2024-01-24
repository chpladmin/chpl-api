package gov.healthit.chpl.standard;

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

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.criteriaattribute.rule.Rule;
import gov.healthit.chpl.util.LocalDateDeserializer;
import gov.healthit.chpl.util.LocalDateSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Schema(description = "A unique identifier for a health IT standard, ensuring consistent technical and interoperability compliance across systems")
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Standard implements Serializable {
    private static final long serialVersionUID = -5058475461826923683L;

    @Schema(description = "Criteria Attribute internal ID")
    private Long id;

    @Schema(description = "A string value to represent the value to be used for the Standard.")
    private String value;

    @Schema(description = "A string value representing a law and section (e.g., 170.202(a)).")
    private String regulatoryTextCitation;


    @Schema(description = "A date value representing the date by which the Standard became available.")
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate startDay;

    @Schema(description = "A date value representing the date by which the Standard can no longer be used.")
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate endDay;

    @Schema(description = "A date value representing the date by which the Standard is required for selected criteria.")
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate requiredDay;

    // Do not include this property if the value is "empty". It will be empty when generating listing details
    // and will be non-empty (this included) when doing CRUD operations on Standards
    @JsonInclude(value = Include.NON_EMPTY)
    @Builder.Default
    private List<CertificationCriterion> criteria = new ArrayList<CertificationCriterion>();

    @Schema(description = "The rule which this Criteria Attrbute is associated with.")
    private Rule rule;

    private String groupName;

    public Boolean isRetired() {
        LocalDate end = endDay != null ? endDay : LocalDate.MAX;
        return end.isBefore(LocalDate.now());
    }

    /*
     * TODO: OCD-4333 NEED THIS TEXT
     */
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
}

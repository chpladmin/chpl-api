package gov.healthit.chpl.certificationCriteria;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

import org.apache.commons.lang3.tuple.Pair;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import gov.healthit.chpl.api.deprecatedUsage.DeprecatedResponseField;
import gov.healthit.chpl.criteriaattribute.rule.Rule;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.concept.CertificationEditionConcept;
import gov.healthit.chpl.util.DateUtil;
import gov.healthit.chpl.util.LocalDateDeserializer;
import gov.healthit.chpl.util.LocalDateSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@JsonIgnoreProperties(ignoreUnknown = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class CertificationCriterion implements Serializable {
    private static final long serialVersionUID = 5732322243572571895L;

    private Long id;
    private String number;
    private String title;
    private Long certificationEditionId;
    private String certificationEdition;

    @Schema(description = "A date value representing the date by which the Criteria Attribute became available.")
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate startDay;

    @Schema(description = "A date value representing the date by which the Criteria Attribute can no longer be used.")
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate endDay;

    private String description;

    @Schema(description = "The rule which this criterion is associated with.")
    private Rule rule;

    private String companionGuideLink;

    @Deprecated
    @DeprecatedResponseField(message = "This property will be removed. It can be derived based on the endDay.",
        removalDate = "2024-01-01")
    private Boolean removed;

    @JsonProperty(access = Access.READ_ONLY)
    public CriterionStatus getStatus() {
        if (certificationEdition != null
                && (certificationEdition.equals(CertificationEditionConcept.CERTIFICATION_EDITION_2011.getYear())
                        || certificationEdition.equals(CertificationEditionConcept.CERTIFICATION_EDITION_2014.getYear()))) {
            return CriterionStatus.RETIRED;
        } else {
            LocalDate end = endDay != null ? endDay : LocalDate.MAX;
            if (end.isBefore(LocalDate.now())) {
                return CriterionStatus.REMOVED;
            }
            return CriterionStatus.ACTIVE;
        }
    }

    @JsonProperty(access = Access.READ_ONLY)
    public Boolean isRemoved() {
        return getStatus().equals(CriterionStatus.REMOVED);
    }

    public boolean isAvailableToListing(CertifiedProductSearchDetails listing) {
        return DateUtil.datesOverlap(Pair.of(listing.getCertificationDay(), listing.getDecertificationDay()),
                Pair.of(getStartDay(), getEndDay()));
    }

    public boolean isEditable() {
        LocalDate today = LocalDate.now();
        LocalDate startDayLocal = (this.startDay == null ? LocalDate.MIN : this.startDay);
        return (startDayLocal.isEqual(today) || startDayLocal.isBefore(today))
                && (this.endDay == null ? true : this.endDay.plusYears(1).isAfter(LocalDate.now()));
    }

    @Override
    public int hashCode() {
        return Objects.hash(certificationEdition, certificationEditionId, description, endDay, id, number, rule, startDay, title);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        CertificationCriterion other = (CertificationCriterion) obj;
        return Objects.equals(certificationEdition, other.certificationEdition)
                && Objects.equals(certificationEditionId, other.certificationEditionId)
                && Objects.equals(description, other.description)
                && Objects.equals(endDay, other.endDay)
                && Objects.equals(id, other.id)
                && Objects.equals(number, other.number)
                && Objects.equals(rule, other.rule)
                && Objects.equals(startDay, other.startDay)
                && Objects.equals(title, other.title);
    }
}

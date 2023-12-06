package gov.healthit.chpl.domain.surveillance;

import java.time.LocalDate;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import gov.healthit.chpl.certificationCriteria.CriterionStatus;
import gov.healthit.chpl.domain.CertificationEdition;
import gov.healthit.chpl.domain.concept.CertificationEditionConcept;
import gov.healthit.chpl.util.LocalDateDeserializer;
import gov.healthit.chpl.util.LocalDateSerializer;
import gov.healthit.chpl.util.NullSafeEvaluator;
import gov.healthit.chpl.util.Util;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequirementType {
    private Long id;
    private String number;
    private String title;

    @Schema(description = "A date value representing the date by which the Requirement Type became available.")
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate startDay;

    @Schema(description = "A date value representing the date by which the Requirement Type can no longer be used.")
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate endDay;

    private CertificationEdition certificationEdition;

    @JsonIgnore
    private String edition;

    private RequirementGroupType requirementGroupType;

    public String getEdition() {
        return NullSafeEvaluator.eval(() -> certificationEdition.getName(), null);
    }

    @JsonIgnore
    public String getFormattedTitle() {
        if (StringUtils.isNotEmpty(number)) {
            return Util.formatCriteriaNumber(this);
        } else {
            return title;
        }
    }

    @JsonIgnore
    public String getFormattedTitleForReport() {
        String formattedTitleForReport = "";
        if (StringUtils.isNotEmpty(number)) {
            CriterionStatus status = getStatus();
            if (status != null && status.equals(CriterionStatus.REMOVED)) {
                formattedTitleForReport = "Removed | ";
            }
            formattedTitleForReport += Util.formatCriteriaNumber(this);
        } else {
            formattedTitleForReport = title;
        }
        return formattedTitleForReport;
    }

    @JsonProperty(access = Access.READ_ONLY)
    public CriterionStatus getStatus() {
        if (certificationEdition != null && certificationEdition.getName() != null
                && (certificationEdition.getName().equals(CertificationEditionConcept.CERTIFICATION_EDITION_2011.getYear())
                        || certificationEdition.getName().equals(CertificationEditionConcept.CERTIFICATION_EDITION_2014.getYear()))) {
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
}

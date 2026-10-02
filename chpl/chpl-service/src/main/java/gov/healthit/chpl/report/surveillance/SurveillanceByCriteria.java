package gov.healthit.chpl.report.surveillance;

import java.time.LocalDate;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.certificationCriteria.CriterionStatus;
import gov.healthit.chpl.domain.concept.CertificationEditionConcept;
import gov.healthit.chpl.util.LocalDateDeserializer;
import gov.healthit.chpl.util.LocalDateSerializer;
import lombok.Builder;
import lombok.Data;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

@Data
@Builder
public class SurveillanceByCriteria {

    private CertificationCriterion criterion;
    private Integer criterionSortOrder;
    private Long surveillanceId;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate surveillanceStartDate;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate surveillanceEndDate;

    @JsonProperty(access = Access.READ_ONLY)
    public CriterionStatus getStatus() {
        if (!StringUtils.isEmpty(criterion.getCertificationEdition())
                && (criterion.getCertificationEdition().equals(CertificationEditionConcept.CERTIFICATION_EDITION_2011.getYear())
                        || criterion.getCertificationEdition().equals(CertificationEditionConcept.CERTIFICATION_EDITION_2014.getYear()))) {
            return CriterionStatus.RETIRED;
        } else {
            LocalDate end = criterion.getEndDay() != null ? criterion.getEndDay() : LocalDate.MAX;
            if (end.isBefore(LocalDate.now())) {
                return CriterionStatus.REMOVED;
            }
            return CriterionStatus.ACTIVE;
        }
    }

}

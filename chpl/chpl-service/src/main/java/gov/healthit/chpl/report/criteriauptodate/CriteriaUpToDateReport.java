package gov.healthit.chpl.report.criteriauptodate;

import java.time.LocalDate;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.util.LocalDateDeserializer;
import gov.healthit.chpl.util.LocalDateSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Defines the data for the criteria up-to-date report")
public class CriteriaUpToDateReport {

    @Schema(description = "The date the data for this report was gathered. It could be the current day or a recent prior day.")
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate accurateAsOfDate;

    @Schema(description = "The criterion being checked")
    private CertificationCriterion criterion;

    @Schema(description = "The total number of listings attesting to the criterion")
    private Long activeListingsAttestingToCriterionCount;

    @Schema(description = "The number of listings attesting to this criterion where the criterion is considered up-to-date. "
            + "This is expected to be equal to or less than listingsAttestingToCriterionCount.")
    private Long activeListingsUpToDateOnCriterionCount;
}

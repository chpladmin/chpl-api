package gov.healthit.chpl.report.criteriamigrationreport;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import gov.healthit.chpl.util.LocalDateDeserializer;
import gov.healthit.chpl.util.LocalDateSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Defines the counts for a CriteriaMigrationDefinition on a particular date")
public class CriteriaMigrationCount {

    @Schema(description = "Internal ID for instance of CriteriaMigrationCount")
    private Long id;

    @JsonIgnore
    private CriteriaMigrationDefinition criteriaMigrationDefinition;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    @Schema(description = "Date which the counts were calculated")
    private LocalDate reportDate;

    @Schema(description = "Number of listings attesting the original criteria as of the reportDate")
    private Integer originalCriterionCount;

    @Schema(description = "Number of listings attesting the original criteria as of the reportDate")
    private Integer updatedCriterionCount;

    @Schema(description = "Number of listings where the original criteria was attested to, then unattested to, and trhe updated criteria has been attested to")
    private Integer originalToUpdatedCriterionCount;
}

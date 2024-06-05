package gov.healthit.chpl.report.criteriamigrationreport;

import java.util.List;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Defines the original and updated criteria for a CriteriaMigrationReport")
public class CriteriaMigrationDefinition {

    @Schema(description = "Internal ID for instance of CriteriaMigrationDefinition")
    private Long id;

    @Schema(description = "The criterion that is being migrated from")
    private CertificationCriterion originalCriterion;

    @Schema(description = "The criterion that is being migrated to")
    private CertificationCriterion updatedCriterion;

    @Schema(description = "List of CriteriaMigrationCount objects")
    private List<CriteriaMigrationCount> criteriaMigrationCounts;

}

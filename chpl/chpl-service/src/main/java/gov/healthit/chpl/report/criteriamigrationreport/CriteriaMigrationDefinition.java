package gov.healthit.chpl.report.criteriamigrationreport;

import java.util.List;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CriteriaMigrationDefinition {
    private Long id;
    private CertificationCriterion originalCriterion;
    private CertificationCriterion updatedCriterion;
    private List<CriteriaMigrationCount> criteriaMigrationCounts;

}

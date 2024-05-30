package gov.healthit.chpl.report.criteriamigrationreport;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CriteriaMigrationReport {
    private Long id;
    private String reportName;
    private List<CriteriaMigrationDefinition> criteriaMigrationDefinitions;
}

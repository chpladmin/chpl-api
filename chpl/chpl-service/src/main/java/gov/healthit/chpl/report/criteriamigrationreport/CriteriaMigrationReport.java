package gov.healthit.chpl.report.criteriamigrationreport;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import gov.healthit.chpl.util.LocalDateDeserializer;
import gov.healthit.chpl.util.LocalDateSerializer;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CriteriaMigrationReport {
    private Long id;
    private String reportName;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate startDate;
    private List<CriteriaMigrationDefinition> criteriaMigrationDefinitions;
}

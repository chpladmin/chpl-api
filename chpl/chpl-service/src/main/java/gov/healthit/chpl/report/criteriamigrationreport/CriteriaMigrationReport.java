package gov.healthit.chpl.report.criteriamigrationreport;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import gov.healthit.chpl.util.LocalDateDeserializer;
import gov.healthit.chpl.util.LocalDateSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Defines the report and asssociated criteria migrations for the report")
public class CriteriaMigrationReport {

    @Schema(description = "Internal ID for instance of CriteriaMigrationReport")
    private Long id;

    @Schema(description = "The name of the report used to group a series to criteria migrations")
    private String reportName;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    @Schema(description = "The date used as the maximum date to look back to determine if a criteria was upgraded.  "
            + "Typically, this date is something like the Cures implementation date or HTI-1 implemenation date")
    private LocalDate startDate;

    @Schema(description = "Collection of CriteriaMigrationDefinition objects that define the criteria migrations "
            + "associated with a report")
    private List<CriteriaMigrationDefinition> criteriaMigrationDefinitions;
}

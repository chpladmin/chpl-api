package gov.healthit.chpl.report.criteriamigrationreport;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import gov.healthit.chpl.util.LocalDateDeserializer;
import gov.healthit.chpl.util.LocalDateSerializer;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CriteriaMigrationCount {
    private Long id;

    @JsonIgnore
    private CriteriaMigrationDefinition criteriaMigrationDefinition;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate reportDate;

    private Integer originalCriterionCount;
    private Integer updatedCriterionCount;
    private Integer originalToUpdatedCriterionCount;
}

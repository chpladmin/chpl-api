package gov.healthit.chpl.report.realworldtesting;

import java.time.LocalDate;

import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.util.LocalDateDeserializer;
import gov.healthit.chpl.util.LocalDateSerializer;
import lombok.Builder;
import lombok.Data;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

@Deprecated
@Data
@Builder
public class RealWorldTestingSummaryByAcbReportPreHti5 {
    private Long id;

    private Long realWorldTestingYear;
    private CertificationBody certificationBody;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate checkedDate;

    private Long checkedCount;
    private Long requiresCheckCount;
}

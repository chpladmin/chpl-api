package gov.healthit.chpl.surveillance.report.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SurveillanceSummaryDTO {

    private Long reactiveCount = 0L;
    private Long randomizedCount = 0L;
}

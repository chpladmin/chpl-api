package gov.healthit.chpl.surveillance.report.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class SurveillanceSummary {

    private Long reactiveCount = 0L;
    private Long randomizedCount  = 0L;
}

package gov.healthit.chpl.surveillance.report.domain;

import java.time.MonthDay;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Quarter {

    private Long id;
    private String name;
    private Integer startMonth;
    private Integer startDay;
    private Integer endMonth;
    private Integer endDay;
    private MonthDay start;
    private MonthDay end;
}

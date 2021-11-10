package gov.healthit.chpl.surveillance.report.domain;

import gov.healthit.chpl.surveillance.report.entity.QuarterEntity;
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

    public Quarter(QuarterEntity entity) {
        this.id = entity.getId();
        this.name = entity.getName();
        this.startMonth = entity.getQuarterBeginMonth();
        this.startDay = entity.getQuarterBeginDay();
        this.endMonth = entity.getQuarterEndMonth();
        this.endDay = entity.getQuarterEndDay();
    }
}

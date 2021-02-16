package gov.healthit.chpl.surveillance.report.dto;

import gov.healthit.chpl.surveillance.report.entity.QuarterEntity;
import lombok.Data;

@Data
public class QuarterDTO {

    private Long id;
    private String name;
    private Integer startMonth;
    private Integer startDay;
    private Integer endMonth;
    private Integer endDay;

    public QuarterDTO() {}

    public QuarterDTO(QuarterEntity entity) {
        this.id = entity.getId();
        this.name = entity.getName();
        this.startMonth = entity.getQuarterBeginMonth();
        this.startDay = entity.getQuarterBeginDay();
        this.endMonth = entity.getQuarterEndMonth();
        this.endDay = entity.getQuarterEndDay();
    }
}

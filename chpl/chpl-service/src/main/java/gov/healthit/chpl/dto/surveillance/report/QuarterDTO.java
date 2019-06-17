package gov.healthit.chpl.dto.surveillance.report;

import gov.healthit.chpl.entity.surveillance.report.QuarterEntity;

public class QuarterDTO {

    private Long id;
    private String name;
    private Integer startMonth;
    private Integer startDay;
    private Integer endMonth;
    private Integer endDay;

    public QuarterDTO() {}

    public QuarterDTO(final QuarterEntity entity) {
        this.id = entity.getId();
        this.name = entity.getName();
        this.startMonth = entity.getQuarterBeginMonth();
        this.startDay = entity.getQuarterBeginDay();
        this.endMonth = entity.getQuarterEndMonth();
        this.endDay = entity.getQuarterEndDay();
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Integer getStartMonth() {
        return startMonth;
    }

    public void setStartMonth(final Integer startMonth) {
        this.startMonth = startMonth;
    }

    public Integer getStartDay() {
        return startDay;
    }

    public void setStartDay(final Integer startDay) {
        this.startDay = startDay;
    }

    public Integer getEndMonth() {
        return endMonth;
    }

    public void setEndMonth(final Integer endMonth) {
        this.endMonth = endMonth;
    }

    public Integer getEndDay() {
        return endDay;
    }

    public void setEndDay(final Integer endDay) {
        this.endDay = endDay;
    }
}

package gov.healthit.chpl.dto.surveillance.report;

public class SurveillanceSummaryDTO {

    private Integer reactiveCount;
    private Integer randomizedCount;

    public Integer getReactiveCount() {
        return reactiveCount;
    }

    public void setReactiveCount(final Integer reactiveCount) {
        this.reactiveCount = reactiveCount;
    }

    public Integer getRandomizedCount() {
        return randomizedCount;
    }

    public void setRandomizedCount(final Integer randomizedCount) {
        this.randomizedCount = randomizedCount;
    }

    public SurveillanceSummaryDTO() {}
}

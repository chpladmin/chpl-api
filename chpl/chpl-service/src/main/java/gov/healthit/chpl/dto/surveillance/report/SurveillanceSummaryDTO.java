package gov.healthit.chpl.dto.surveillance.report;

public class SurveillanceSummaryDTO {

    private Long reactiveCount = 0L;
    private Long randomizedCount = 0L;

    public Long getReactiveCount() {
        return reactiveCount;
    }

    public void setReactiveCount(final Long reactiveCount) {
        this.reactiveCount = reactiveCount;
    }

    public Long getRandomizedCount() {
        return randomizedCount;
    }

    public void setRandomizedCount(final Long randomizedCount) {
        this.randomizedCount = randomizedCount;
    }
}

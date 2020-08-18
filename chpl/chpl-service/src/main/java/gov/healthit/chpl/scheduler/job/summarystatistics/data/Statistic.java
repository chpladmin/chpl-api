package gov.healthit.chpl.scheduler.job.summarystatistics.data;

public abstract class Statistic {
    private Integer year;
    private String name;

    public Integer getYear() {
        return year;
    };

    public void setYear(final Integer year) {
        this.year = year;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }
}

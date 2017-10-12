package gov.healthit.chpl.domain.statistics;

public abstract class Statistic {
    protected Integer year;
    protected String name;

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

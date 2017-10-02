package gov.healthit.chpl.domain.statistics;

public abstract class Statistic {
	protected Integer year;
	protected String name;

	public Integer getYear() {
		return year;
		};
	public void setYear(Integer year) {
		this.year = year;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}

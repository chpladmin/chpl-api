package gov.healthit.chpl.domain;

public class CertifiedBodyStatistics {
	private String name;
	private Long count;
	private Integer year;
	
	public CertifiedBodyStatistics(){}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Long getCount() {
		return count;
	}
	public void setCount(Long count) {
		this.count = count;
	}
	public Integer getYear() {
		return year;
	}
	public void setYear(Integer year) {
		this.year = year;
	}
}

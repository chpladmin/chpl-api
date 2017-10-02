package gov.healthit.chpl.domain.statistics;

public class CertifiedBodyStatistics extends Statistic {
	private String certificationStatusName;
	private Long totalDevelopersWithListings;
	private Long totalListings;

	public CertifiedBodyStatistics(){}

	public String getCertificationStatusName() {
		return certificationStatusName;
	}
	public void setCertificationStatusName(String certificationStatusName) {
		this.certificationStatusName = certificationStatusName;
	}
	public Long getTotalDevelopersWithListings() {
		return totalDevelopersWithListings;
	}
	public void setTotalDevelopersWithListings(Long totalDevelopersWithListings) {
		this.totalDevelopersWithListings = totalDevelopersWithListings;
	}
	public Long getTotalListings() {
		return totalListings;
	}
	public void setTotalListings(Long totalListings) {
		this.totalListings = totalListings;
	}
}

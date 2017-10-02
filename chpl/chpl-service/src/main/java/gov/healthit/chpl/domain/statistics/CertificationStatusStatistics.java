package gov.healthit.chpl.domain.statistics;

public class CertificationStatusStatistics {
	private Integer id;
	private String name;
	private CertifiedBodyStatistics certifiedBodyStatistics;

	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public CertifiedBodyStatistics getCertifiedBodyStatistics() {
		return certifiedBodyStatistics;
	}
	public void setCertifiedBodyStatistics(CertifiedBodyStatistics certifiedBodyStatistics) {
		this.certifiedBodyStatistics = certifiedBodyStatistics;
	}

}

package gov.healthit.chpl.domain;


public class AdditionalSoftware {

	private Long additionalSoftwareid;
	private Long certifiedProductId;
	private String justification;
	private String name;
	private String version;
	
	public Long getAdditionalSoftwareid() {
		return additionalSoftwareid;
	}
	public void setAdditionalSoftwareid(Long additionalSoftwareid) {
		this.additionalSoftwareid = additionalSoftwareid;
	}
	public Long getCertifiedProductId() {
		return certifiedProductId;
	}
	public void setCertifiedProductId(Long certifiedProductId) {
		this.certifiedProductId = certifiedProductId;
	}
	public String getJustification() {
		return justification;
	}
	public void setJustification(String justification) {
		this.justification = justification;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
}

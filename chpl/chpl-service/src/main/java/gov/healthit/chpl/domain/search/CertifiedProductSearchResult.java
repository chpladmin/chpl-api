package gov.healthit.chpl.domain.search;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonView;

public class CertifiedProductSearchResult implements Serializable {
	private static final long serialVersionUID = -2547390525592841034L;
	
	@JsonView({SearchViews.Default.class})
	protected Long id;
	
	@JsonView({SearchViews.Default.class})
	protected String chplProductNumber;
	
	@JsonView({SearchViews.Default.class})
	protected String edition;
	
	@JsonView({SearchViews.Default.class})
	protected String atl;
	
	@JsonView({SearchViews.Default.class})
	protected String acb;
	
	@JsonView({SearchViews.Default.class})
	protected String acbCertificationId;
	
	@JsonView({SearchViews.Default.class})
	protected String practiceType;
	
	@JsonView({SearchViews.Default.class})
	protected String developer;
	
	@JsonView({SearchViews.Default.class})
	protected String product;
	
	@JsonView({SearchViews.Default.class})
	protected String version;
	
	@JsonView({SearchViews.Default.class})
	protected Long certificationDate;
	
	@JsonView({SearchViews.Default.class})
	protected String certificationStatus;
	
	@JsonView({SearchViews.Default.class})
	protected Long surveillanceCount;
	
	@JsonView({SearchViews.Default.class})
	protected Long openNonconformityCount;
	
	@JsonView({SearchViews.Default.class})
	protected Long closedNonconformityCount;
	
	public CertifiedProductSearchResult() {
	}
	
	public CertifiedProductSearchResult(CertifiedProductSearchResult other) {
		this.id = other.getId();
		this.chplProductNumber = other.getChplProductNumber();
		this.edition = other.getEdition();
		this.atl = other.getAtl();
		this.acb = other.getAcb();
		this.acbCertificationId = other.getAcbCertificationId();
		this.practiceType = other.getPracticeType();
		this.developer = other.getDeveloper();
		this.product = other.getProduct();
		this.version = other.getVersion();
		this.certificationDate = other.getCertificationDate();
		this.certificationStatus = other.getCertificationStatus();
		this.surveillanceCount = other.getSurveillanceCount();
		this.openNonconformityCount = other.getOpenNonconformityCount();
		this.closedNonconformityCount = other.getClosedNonconformityCount();
	}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getChplProductNumber() {
		return chplProductNumber;
	}
	public void setChplProductNumber(String chplProductNumber) {
		this.chplProductNumber = chplProductNumber;
	}
	public String getEdition() {
		return edition;
	}
	public void setEdition(String edition) {
		this.edition = edition;
	}
	public String getAtl() {
		return atl;
	}
	public void setAtl(String atl) {
		this.atl = atl;
	}
	public String getAcb() {
		return acb;
	}
	public void setAcb(String acb) {
		this.acb = acb;
	}
	public String getPracticeType() {
		return practiceType;
	}
	public void setPracticeType(String practiceType) {
		this.practiceType = practiceType;
	}
	public String getDeveloper() {
		return developer;
	}
	public void setDeveloper(String developer) {
		this.developer = developer;
	}
	public String getProduct() {
		return product;
	}
	public void setProduct(String product) {
		this.product = product;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public Long getCertificationDate() {
		return certificationDate;
	}
	public void setCertificationDate(Long certificationDate) {
		this.certificationDate = certificationDate;
	}
	public String getCertificationStatus() {
		return certificationStatus;
	}
	public void setCertificationStatus(String certificationStatus) {
		this.certificationStatus = certificationStatus;
	}
	public Long getSurveillanceCount() {
		return surveillanceCount;
	}
	public void setSurveillanceCount(Long surveillanceCount) {
		this.surveillanceCount = surveillanceCount;
	}
	public Long getOpenNonconformityCount() {
		return openNonconformityCount;
	}
	public void setOpenNonconformityCount(Long openNonconformityCount) {
		this.openNonconformityCount = openNonconformityCount;
	}

	public Long getClosedNonconformityCount() {
		return closedNonconformityCount;
	}
	public void setClosedNonconformityCount(Long closedNonconformityCount) {
		this.closedNonconformityCount = closedNonconformityCount;
	}
	
	public String getAcbCertificationId() {
		return acbCertificationId;
	}

	public void setAcbCertificationId(String acbCertificationId) {
		this.acbCertificationId = acbCertificationId;
	}
}

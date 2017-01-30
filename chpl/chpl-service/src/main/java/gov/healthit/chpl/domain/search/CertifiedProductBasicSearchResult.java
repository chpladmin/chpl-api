package gov.healthit.chpl.domain.search;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CertifiedProductBasicSearchResult implements Serializable {
	private static final long serialVersionUID = -2547390525592841034L;
	private Long id;
	private String chplProductNumber;
	private String edition;
	private String atl;
	private String acb;
	private String acbCertificationId;
	private String practiceType;
	private String developer;
	private String product;
	private String version;
	private Long certificationDate;
	private String certificationStatus;
	private boolean hasHadSurveillance;
	private boolean hasOpenSurveillance;
	private boolean hasOpenNonconformities;
	
	private List<String> criteriaMet; //list of criteria numbers
	private List<String> cqmsMet; //map of cqm number to list of versions that were met
	
	public CertifiedProductBasicSearchResult() {
		criteriaMet = new ArrayList<String>();
		cqmsMet = new ArrayList<String>();
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
	public boolean isHasHadSurveillance() {
		return hasHadSurveillance;
	}
	public void setHasHadSurveillance(boolean hasHadSurveillance) {
		this.hasHadSurveillance = hasHadSurveillance;
	}
	public boolean isHasOpenSurveillance() {
		return hasOpenSurveillance;
	}
	public void setHasOpenSurveillance(boolean hasOpenSurveillance) {
		this.hasOpenSurveillance = hasOpenSurveillance;
	}
	public boolean isHasOpenNonconformities() {
		return hasOpenNonconformities;
	}
	public void setHasOpenNonconformities(boolean hasOpenNonconformities) {
		this.hasOpenNonconformities = hasOpenNonconformities;
	}
	public List<String> getCriteriaMet() {
		return criteriaMet;
	}
	public void setCriteriaMet(List<String> criteriaMet) {
		this.criteriaMet = criteriaMet;
	}
	public List<String> getCqmsMet() {
		return cqmsMet;
	}
	public void setCqmsMet(List<String> cqmsMet) {
		this.cqmsMet = cqmsMet;
	}

	public String getAcbCertificationId() {
		return acbCertificationId;
	}

	public void setAcbCertificationId(String acbCertificationId) {
		this.acbCertificationId = acbCertificationId;
	}
}

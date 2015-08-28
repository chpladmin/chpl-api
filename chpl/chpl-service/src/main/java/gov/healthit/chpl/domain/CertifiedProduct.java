package gov.healthit.chpl.domain;

import gov.healthit.chpl.dto.CertifiedProductDTO;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CertifiedProduct {
	
	private Long id;
	private Long productVersionId;
	private String chplNum;
	private String practiceType;
	private String classification;
	private String certifyingBody;
	private String certificationEdition;
	private Date certDate;
	private Date lastModifiedDate;
	private List<String> additionalSoftware = new ArrayList<String>();
	private String certsAndCQMs;
	private List<CertificationResult> certs;
	private List<CQMResult> cqms;
	
	public CertifiedProduct(CertifiedProductDTO dto) {
		this.id = dto.getId();
		this.productVersionId = dto.getProductVersionId();
		this.chplNum = dto.getChplProductNumber();
		this.practiceType = dto.getPracticeTypeId().toString();
		this.classification = dto.getProductClassificationTypeId().toString();
		this.certifyingBody = dto.getCertificationBodyId().toString();
		this.certificationEdition = dto.getCertificationEditionId().toString();
		this.certDate = dto.getCreationDate();
		this.lastModifiedDate = dto.getLastModifiedDate();
//		this.additionalSoftware.addAll(dto.getAdditionalSoftware());
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getProductVersionId() {
		return productVersionId;
	}
	public void setProductVersionId(Long productVersionId) {
		this.productVersionId = productVersionId;
	}
	public String getChplNum() {
		return chplNum;
	}
	public void setChplNum(String chplNum) {
		this.chplNum = chplNum;
	}
	public String getPracticeType() {
		return practiceType;
	}
	public void setPracticeType(String practiceType) {
		this.practiceType = practiceType;
	}
	public String getClassification() {
		return classification;
	}
	public void setClassification(String classification) {
		this.classification = classification;
	}
	public String getCertifyingBody() {
		return certifyingBody;
	}
	public void setCertifyingBody(String certifyingBody) {
		this.certifyingBody = certifyingBody;
	}
	public String getCertificationEdition() {
		return certificationEdition;
	}
	public void setCertificationEdition(String certificationEdition) {
		this.certificationEdition = certificationEdition;
	}
	public Date getCertDate() {
		return certDate;
	}
	public void setCertDate(Date certDate) {
		this.certDate = certDate;
	}
	public Date getLastModifiedDate() {
		return lastModifiedDate;
	}
	public void setLastModifiedDate(Date lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}
	public List<String> getAdditionalSoftware() {
		return additionalSoftware;
	}
	public void setAdditionalSoftware(List<String> additionalSoftware) {
		this.additionalSoftware = additionalSoftware;
	}
	public String getCertsAndCQMs() {
		return certsAndCQMs;
	}
	public void setCertsAndCQMs(String certsAndCQMs) {
		this.certsAndCQMs = certsAndCQMs;
	}
	public List<CertificationResult> getCerts() {
		return certs;
	}
	public void setCerts(List<CertificationResult> certs) {
		this.certs = certs;
	}
	public List<CQMResult> getCqms() {
		return cqms;
	}
	public void setCqms(List<CQMResult> cqms) {
		this.cqms = cqms;
	}
	
}

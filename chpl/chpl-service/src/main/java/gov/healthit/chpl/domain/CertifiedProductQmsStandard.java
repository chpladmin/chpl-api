package gov.healthit.chpl.domain;

import gov.healthit.chpl.dto.CertifiedProductQmsStandardDTO;

public class CertifiedProductQmsStandard {
	private Long id;
	private Long qmsStandardId;
	private String qmsStandardName;
	private String qmsModification;
	private String applicableCriteria;

	public CertifiedProductQmsStandard() {
		super();
	}
	
	public CertifiedProductQmsStandard(CertifiedProductQmsStandardDTO dto) {
		this.id = dto.getId();
		this.qmsStandardId = dto.getQmsStandardId();
		this.qmsStandardName = dto.getQmsStandardName();
		this.qmsModification = dto.getQmsModification();
		this.applicableCriteria = dto.getApplicableCriteria();
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getQmsStandardId() {
		return qmsStandardId;
	}

	public void setQmsStandardId(Long qmsStandardId) {
		this.qmsStandardId = qmsStandardId;
	}

	public String getQmsStandardName() {
		return qmsStandardName;
	}

	public void setQmsStandardName(String qmsStandardName) {
		this.qmsStandardName = qmsStandardName;
	}

	public String getQmsModification() {
		return qmsModification;
	}

	public void setQmsModification(String qmsModification) {
		this.qmsModification = qmsModification;
	}

	public String getApplicableCriteria() {
		return applicableCriteria;
	}

	public void setApplicableCriteria(String applicableCriteria) {
		this.applicableCriteria = applicableCriteria;
	}
}

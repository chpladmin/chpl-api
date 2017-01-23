package gov.healthit.chpl.domain;

import java.io.Serializable;

import gov.healthit.chpl.dto.CertificationCriterionDTO;

public class CertificationCriterion implements Serializable {
	private static final long serialVersionUID = 5732322243572571895L;
	
	private Long id;
	private String number;
	private String title;
	private Long certificationEditionId;
	private String description;
	
	public CertificationCriterion() {}
	
	public CertificationCriterion(CertificationCriterionDTO dto) {
		this.id = dto.getId();
		this.certificationEditionId = dto.getCertificationEditionId();
		this.description = dto.getDescription();
		this.number = dto.getNumber();
		this.title = dto.getTitle();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	public Long getCertificationEditionId() {
		return certificationEditionId;
	}

	public void setCertificationEditionId(Long certificationEditionId) {
		this.certificationEditionId = certificationEditionId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}

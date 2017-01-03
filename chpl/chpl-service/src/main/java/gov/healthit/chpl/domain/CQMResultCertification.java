package gov.healthit.chpl.domain;

import java.io.Serializable;

import gov.healthit.chpl.dto.CQMResultCriteriaDTO;

public class CQMResultCertification implements Serializable {
	private static final long serialVersionUID = 2547864525772721622L;
	private Long id;
	private Long certificationId;
	private String certificationNumber;

	public CQMResultCertification(){
		
	}
	
	public CQMResultCertification(CQMResultCriteriaDTO dto){
		this.id = dto.getId();
		this.certificationId = dto.getCriterionId();
		if(dto.getCriterion() != null) {
			this.certificationNumber = dto.getCriterion().getNumber();
		}
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getCertificationId() {
		return certificationId;
	}

	public void setCertificationId(Long criteriaId) {
		this.certificationId = criteriaId;
	}

	public String getCertificationNumber() {
		return certificationNumber;
	}

	public void setCertificationNumber(String criteriaNumber) {
		this.certificationNumber = criteriaNumber;
	}
}

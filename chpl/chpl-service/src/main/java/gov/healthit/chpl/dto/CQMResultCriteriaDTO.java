package gov.healthit.chpl.dto;

import gov.healthit.chpl.entity.CQMResultCriteriaEntity;
import gov.healthit.chpl.entity.CQMResultEntity;

import java.util.Date;

public class CQMResultCriteriaDTO {
	
	private Long id;
	private Long cqmResultId;
	private Long criterionId;
	private CertificationCriterionDTO criterion;
	
	public CQMResultCriteriaDTO(){}
	
	public CQMResultCriteriaDTO(CQMResultCriteriaEntity entity){
		
		this.id = entity.getId();
		this.cqmResultId = entity.getCqmResultId();
		this.criterionId = entity.getCertificationCriterionId();
		if(entity.getCertCriteria() != null) {
			this.criterion = new CertificationCriterionDTO(entity.getCertCriteria());
		}
	}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	public Long getCqmResultId() {
		return cqmResultId;
	}

	public void setCqmResultId(Long cqmResultId) {
		this.cqmResultId = cqmResultId;
	}

	public Long getCriterionId() {
		return criterionId;
	}

	public void setCriterionId(Long criterionId) {
		this.criterionId = criterionId;
	}

	public CertificationCriterionDTO getCriterion() {
		return criterion;
	}

	public void setCriterion(CertificationCriterionDTO criterion) {
		this.criterion = criterion;
	}
}

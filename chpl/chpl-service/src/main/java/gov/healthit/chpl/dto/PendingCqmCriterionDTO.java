package gov.healthit.chpl.dto;

import gov.healthit.chpl.entity.PendingCqmCriterionEntity;

public class PendingCqmCriterionDTO {
	private Long id;
	private Long cqmCriterionId;
	private Long pendingCertifiedProductId;
	private boolean meetsCriteria;
	
	public PendingCqmCriterionDTO() {} 
	
	public PendingCqmCriterionDTO(PendingCqmCriterionEntity entity) {
		this.setId(entity.getId());
		this.setCqmCriterionId(entity.getCqmCriterionId());
		this.setPendingCertifiedProductId(entity.getPendingCertifiedProductId());
		this.setMeetsCriteria(entity.getMeetsCriteria().booleanValue());
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getCqmCriterionId() {
		return cqmCriterionId;
	}

	public void setCqmCriterionId(Long cqmCriterionId) {
		this.cqmCriterionId = cqmCriterionId;
	}

	public Long getPendingCertifiedProductId() {
		return pendingCertifiedProductId;
	}

	public void setPendingCertifiedProductId(Long pendingCertifiedProductId) {
		this.pendingCertifiedProductId = pendingCertifiedProductId;
	}

	public boolean isMeetsCriteria() {
		return meetsCriteria;
	}

	public void setMeetsCriteria(boolean meetsCriteria) {
		this.meetsCriteria = meetsCriteria;
	}
}

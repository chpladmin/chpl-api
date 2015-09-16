package gov.healthit.chpl.dto;

import gov.healthit.chpl.entity.PendingCertificationCriterionEntity;

public class PendingCertificationCriterionDTO {
	private Long id;
	private Long certificationCriterionId;
	private Long pendingCertifiedProductId;
	private boolean meetsCriteria;
	
	public PendingCertificationCriterionDTO() {}
	
	public PendingCertificationCriterionDTO(PendingCertificationCriterionEntity entity) {
		this.setId(entity.getId());
		this.setCertificationCriterionId(entity.getCertificationCriterionId());
		this.setPendingCertifiedProductId(entity.getPendingCertifiedProductId());
		this.setMeetsCriteria(entity.getMeetsCriteria().booleanValue());
	}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getCertificationCriterionId() {
		return certificationCriterionId;
	}
	public void setCertificationCriterionId(Long certificationCriterionId) {
		this.certificationCriterionId = certificationCriterionId;
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

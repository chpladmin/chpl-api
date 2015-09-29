package gov.healthit.chpl.dto;

import gov.healthit.chpl.entity.PendingCertificationCriterionEntity;

public class PendingCertificationCriterionDTO {
	private Long id;
	private String number;
	private String title;
	private Long certificationCriterionId;
	private Long pendingCertifiedProductId;
	private boolean meetsCriteria;
	
	public PendingCertificationCriterionDTO() {}
	
	public PendingCertificationCriterionDTO(PendingCertificationCriterionEntity entity) {
		this.setId(entity.getId());
				
		if(entity.getMappedCriterion() != null) {
			this.setCertificationCriterionId(entity.getMappedCriterion().getId());
			this.setNumber(entity.getMappedCriterion().getNumber());
			this.setTitle(entity.getMappedCriterion().getTitle());
		}
		
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
	
	
}

package gov.healthit.chpl.dto;

import gov.healthit.chpl.entity.PendingCertifiedProductQmsStandardEntity;

public class PendingCertifiedProductQmsStandardDTO {
	private Long id;
	private Long pendingCertifiedProductId;
	private Long qmsStandardId;
	private String name;
	private String modification;
	private String applicableCriteria;
	
	public PendingCertifiedProductQmsStandardDTO() {}
	
	public PendingCertifiedProductQmsStandardDTO(PendingCertifiedProductQmsStandardEntity entity) {
		this.setId(entity.getId());
				
		if(entity.getMappedProduct() != null) {
			this.setPendingCertifiedProductId(entity.getMappedProduct().getId());
		}
		this.setQmsStandardId(entity.getQmsStandardId());
		this.setName(entity.getName());
		this.setModification(entity.getModification());
		this.setApplicableCriteria(entity.getApplicableCriteria());
	}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getPendingCertifiedProductId() {
		return pendingCertifiedProductId;
	}
	public void setPendingCertifiedProductId(Long pendingCertifiedProductId) {
		this.pendingCertifiedProductId = pendingCertifiedProductId;
	}

	public Long getQmsStandardId() {
		return qmsStandardId;
	}

	public void setQmsStandardId(Long qmsStandardId) {
		this.qmsStandardId = qmsStandardId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getModification() {
		return modification;
	}

	public void setModification(String modification) {
		this.modification = modification;
	}

	public String getApplicableCriteria() {
		return applicableCriteria;
	}

	public void setApplicableCriteria(String applicableCriteria) {
		this.applicableCriteria = applicableCriteria;
	}
}

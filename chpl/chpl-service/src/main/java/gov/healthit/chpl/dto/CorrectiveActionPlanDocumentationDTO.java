package gov.healthit.chpl.dto;

import gov.healthit.chpl.entity.CorrectiveActionPlanDocumentationEntity;

public class CorrectiveActionPlanDocumentationDTO {
	private Long id;
	private Long correctiveActionPlanId;
	private String fileName;
	private String fileType;
	private byte[] fileData;
	
	public CorrectiveActionPlanDocumentationDTO() {} 
	public CorrectiveActionPlanDocumentationDTO(CorrectiveActionPlanDocumentationEntity entity) {
		this.fileData = entity.getFileData();
		this.fileName = entity.getFileName();
		this.fileType = entity.getFileType();
		this.id = entity.getId();
		if(entity.getCorrectiveActionPlan() != null) {
			this.correctiveActionPlanId = entity.getCorrectiveActionPlan().getId();
		}
	}
	
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getFileType() {
		return fileType;
	}
	public void setFileType(String fileType) {
		this.fileType = fileType;
	}
	public byte[] getFileData() {
		return fileData;
	}
	public void setFileData(byte[] fileData) {
		this.fileData = fileData;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getCorrectiveActionPlanId() {
		return correctiveActionPlanId;
	}
	public void setCorrectiveActionPlanId(Long correctiveActionPlanId) {
		this.correctiveActionPlanId = correctiveActionPlanId;
	}
	
	
}

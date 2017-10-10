package gov.healthit.chpl.dto;

import java.io.Serializable;

import gov.healthit.chpl.entity.CorrectiveActionPlanDocumentationEntity;

public class CorrectiveActionPlanDocumentationDTO implements Serializable {
    private static final long serialVersionUID = -2772628293164277707L;
    private Long id;
    private Long correctiveActionPlanId;
    private String fileName;
    private String fileType;
    private byte[] fileData;

    public CorrectiveActionPlanDocumentationDTO() {
    }

    public CorrectiveActionPlanDocumentationDTO(CorrectiveActionPlanDocumentationEntity entity) {
        this.fileData = entity.getFileData();
        this.fileName = entity.getFileName();
        this.fileType = entity.getFileType();
        this.id = entity.getId();
        if (entity.getCorrectiveActionPlan() != null) {
            this.correctiveActionPlanId = entity.getCorrectiveActionPlan().getId();
        }
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(final String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(final String fileType) {
        this.fileType = fileType;
    }

    public byte[] getFileData() {
        return fileData;
    }

    public void setFileData(final byte[] fileData) {
        this.fileData = fileData;
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Long getCorrectiveActionPlanId() {
        return correctiveActionPlanId;
    }

    public void setCorrectiveActionPlanId(final Long correctiveActionPlanId) {
        this.correctiveActionPlanId = correctiveActionPlanId;
    }

}

package gov.healthit.chpl.domain;

import java.io.Serializable;

import gov.healthit.chpl.dto.CorrectiveActionPlanDocumentationDTO;

public class CorrectiveActionPlanDocumentation implements Serializable {
    private static final long serialVersionUID = -2571420590662242822L;
    private Long id;
    private String fileName;
    private String fileType;

    public CorrectiveActionPlanDocumentation() {
    }

    public CorrectiveActionPlanDocumentation(CorrectiveActionPlanDocumentationDTO dto) {
        this.id = dto.getId();
        this.fileName = dto.getFileName();
        this.fileType = dto.getFileType();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
}

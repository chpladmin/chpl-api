package gov.healthit.chpl.dto;

import java.util.Date;

import gov.healthit.chpl.entity.FilesEntity;

public class FilesDTO {
    private Long id;
    private FileTypeDTO fileType;
    private String fileName;
    private String contentType;
    private byte[] fileData;
    private Date associatedDate;
    private Date creationDate;
    private Date lastModifiedDate;
    private Long lastModifiedUser;
    private Boolean deleted;

    public FilesDTO() { }

    public FilesDTO(FilesEntity entity) {
        this.id = entity.getId();
        this.fileType = new FileTypeDTO(entity.getFileType());
        this.fileName = entity.getFileName();
        this.contentType = entity.getContentType();
        this.fileData = entity.getFileData();
        this.associatedDate = entity.getAssociatedDate();
        this.creationDate = entity.getCreationDate();
        this.lastModifiedDate = entity.getLastModifiedDate();
        this.lastModifiedUser = entity.getLastModifiedUser();
        this.deleted = entity.getDeleted();
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(final Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(final Date lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public Long getLastModifiedUser() {
        return lastModifiedUser;
    }

    public void setLastModifiedUser(final Long lastModifiedUser) {
        this.lastModifiedUser = lastModifiedUser;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(final Boolean deleted) {
        this.deleted = deleted;
    }

    public FileTypeDTO getFileType() {
        return fileType;
    }

    public void setFileType(final FileTypeDTO fileType) {
        this.fileType = fileType;
    }

    public byte[] getFileData() {
        return fileData;
    }

    public void setFileData(final byte[] fileData) {
        this.fileData = fileData;
    }

    public Date getAssociatedDate() {
        return associatedDate;
    }

    public void setAssociatedDate(final Date associatedDate) {
        this.associatedDate = associatedDate;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(final String fileName) {
        this.fileName = fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(final String contentType) {
        this.contentType = contentType;
    }

}

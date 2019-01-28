package gov.healthit.chpl.dto;

import java.util.Date;

import gov.healthit.chpl.entity.CHPLFileEntity;
import gov.healthit.chpl.util.Util;

public class CHPLFileDTO {
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

    public CHPLFileDTO() { }

    public CHPLFileDTO(CHPLFileEntity entity) {
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
        return Util.getNewDate(creationDate);
    }

    public void setCreationDate(final Date creationDate) {
        this.creationDate = Util.getNewDate(creationDate);
    }

    public Date getLastModifiedDate() {
        return Util.getNewDate(lastModifiedDate);
    }

    public void setLastModifiedDate(final Date lastModifiedDate) {
        this.lastModifiedDate = Util.getNewDate(lastModifiedDate);
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
        return Util.getNewDate(associatedDate);
    }

    public void setAssociatedDate(final Date associatedDate) {
        this.associatedDate = Util.getNewDate(associatedDate);
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

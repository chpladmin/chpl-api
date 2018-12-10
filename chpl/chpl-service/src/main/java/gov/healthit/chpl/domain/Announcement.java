package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.Date;

import gov.healthit.chpl.dto.AnnouncementDTO;

public class Announcement implements Serializable {
    private static final long serialVersionUID = -7647761708813529969L;
    private Long id;
    public String title;
    private String text;
    private Date startDate;
    private Date endDate;
    private Boolean isPublic;
    private Date creationDate;
    private Boolean deleted;
    private Date lastModifiedDate;
    private Long lastModifiedUser;

    public Announcement() {
    }

    public Announcement(AnnouncementDTO dto) {
        this.id = dto.getId();
        this.title = dto.getTitle();
        this.text = dto.getText();
        this.startDate = dto.getStartDate();
        this.endDate = dto.getEndDate();
        this.isPublic = dto.getIsPublic();
        this.deleted = dto.getDeleted();
        this.lastModifiedDate = dto.getLastModifiedDate();
        this.lastModifiedUser = dto.getLastModifiedUser();
        this.creationDate = dto.getCreationDate();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(final String text) {
        this.text = text;
    }

    public Date getStartDate() {
        return new Date(startDate.getTime());
    }

    public void setStartDate(final Date startDate) {
        this.startDate = new Date(startDate.getTime());
    }

    public Date getEndDate() {
        return new Date(endDate.getTime());
    }

    public void setEndDate(final Date endDate) {
        this.endDate = new Date(endDate.getTime());
    }

    public Date getCreationDate() {
        return new Date(creationDate.getTime());
    }

    public void setCreationDate(final Date creationDate) {
        this.creationDate = new Date(creationDate.getTime());
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(final Boolean deleted) {
        this.deleted = deleted;
    }

    public Date getLastModifiedDate() {
        return new Date(lastModifiedDate.getTime());
    }

    public void setLastModifiedDate(final Date lastModifiedDate) {
        this.lastModifiedDate = new Date(lastModifiedDate.getTime());
    }

    public Long getLastModifiedUser() {
        return lastModifiedUser;
    }

    public void setLastModifiedUser(final Long lastModifiedUser) {
        this.lastModifiedUser = lastModifiedUser;
    }

    public Boolean getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(final Boolean isPublic) {
        this.isPublic = isPublic;
    }

}

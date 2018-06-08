package gov.healthit.chpl.domain;

import java.util.Date;

import gov.healthit.chpl.dto.PracticeTypeDTO;

public class PracticeType {
    private Long id;
    private Date creationDate;
    private Boolean deleted;
    private String description;
    private Date lastModifiedDate;
    private Long lastModifiedUser;
    private String name;

    public PracticeType() {
    }

    public PracticeType(PracticeTypeDTO dto) {

        this.id = dto.getId();
        this.creationDate = dto.getCreationDate();
        this.deleted = dto.getDeleted();
        this.description = dto.getDescription();
        this.lastModifiedDate = dto.getLastModifiedDate();
        this.lastModifiedUser = dto.getLastModifiedUser();
        this.name = dto.getName();

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

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(final Boolean deleted) {
        this.deleted = deleted;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
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

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

}

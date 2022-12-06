package gov.healthit.chpl.dto;

import java.util.Date;

import gov.healthit.chpl.entity.FilterTypeEntity;

@Deprecated
public class FilterTypeDTO {
    private Long id;
    private String name;
    private Date creationDate;
    private Date lastModifiedDate;
    private Long lastModifiedUser;
    private Boolean deleted;

    public FilterTypeDTO() {

    }

    public FilterTypeDTO(FilterTypeEntity entity) {
        this.id = entity.getId();
        this.name = entity.getName();
        this.creationDate = entity.getCreationDate();
        this.lastModifiedDate = entity.getLastModifiedDate();
        this.lastModifiedUser = entity.getLastModifiedUser();
        this.deleted = entity.getDeleted();
    }

    public final Long getId() {
        return id;
    }

    public final void setId(final Long id) {
        this.id = id;
    }

    public final String getName() {
        return name;
    }

    public final void setName(final String name) {
        this.name = name;
    }

    public final Date getCreationDate() {
        return creationDate;
    }

    public final void setCreationDate(final Date creationDate) {
        this.creationDate = creationDate;
    }

    public final Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    public final void setLastModifiedDate(final Date lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public final Long getLastModifiedUser() {
        return lastModifiedUser;
    }

    public final void setLastModifiedUser(final Long lastModifiedUser) {
        this.lastModifiedUser = lastModifiedUser;
    }

    public final Boolean getDeleted() {
        return deleted;
    }

    public final void setDeleted(final Boolean deleted) {
        this.deleted = deleted;
    }

    @Override
    public String toString() {
        return "FilterTypeDTO [id=" + id + ", name=" + name + ", creationDate=" + creationDate + ", lastModifiedDate="
                + lastModifiedDate + ", lastModifiedUser=" + lastModifiedUser + ", deleted=" + deleted + "]";
    }
}

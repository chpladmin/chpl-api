package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.Date;

import gov.healthit.chpl.dto.PracticeTypeDTO;
import gov.healthit.chpl.util.Util;

public class PracticeType implements Serializable {
    private static final long serialVersionUID = 8826782928545744059L;

    private Long id;
    private Date creationDate;
    private Boolean deleted;
    private String description;
    private Date lastModifiedDate;
    private Long lastModifiedUser;
    private String name;

    public PracticeType() { }

    public PracticeType(PracticeTypeDTO dto) {
        this.id = dto.getId();
        this.creationDate = dto.getCreationDate();
        this.deleted = dto.getDeleted();
        this.description = dto.getDescription();
        this.lastModifiedDate = dto.getLastModifiedDate();
        this.lastModifiedUser = dto.getLastModifiedUser();
        this.name = dto.getName();
    }

    public final Long getId() {
        return id;
    }
    public final void setId(final Long id) {
        this.id = id;
    }
    public final Date getCreationDate() {
        return Util.getNewDate(creationDate);
    }
    public final void setCreationDate(final Date creationDate) {
        this.creationDate = Util.getNewDate(creationDate);
    }
    public final Boolean getDeleted() {
        return deleted;
    }
    public final void setDeleted(final Boolean deleted) {
        this.deleted = deleted;
    }
    public final String getDescription() {
        return description;
    }
    public final void setDescription(final String description) {
        this.description = description;
    }
    public Date getLastModifiedDate() {
        return Util.getNewDate(lastModifiedDate);
    }

    public void setLastModifiedDate(final Date lastModifiedDate) {
        this.lastModifiedDate = Util.getNewDate(lastModifiedDate);
    }

    public final Long getLastModifiedUser() {
        return lastModifiedUser;
    }
    public final void setLastModifiedUser(final Long lastModifiedUser) {
        this.lastModifiedUser = lastModifiedUser;
    }
    public final String getName() {
        return name;
    }
    public final void setName(final  String name) {
        this.name = name;
    }
}

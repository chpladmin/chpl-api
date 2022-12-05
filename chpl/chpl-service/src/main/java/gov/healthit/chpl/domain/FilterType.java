package gov.healthit.chpl.domain;

import java.util.Date;

import gov.healthit.chpl.dto.FilterTypeDTO;

@Deprecated
public class FilterType {
    private Long id;
    private String name;
    private Date creationDate;
    private Date lastModifiedDate;
    private Long lastModifiedUser;

    public FilterType() {

    }

    public FilterType(FilterTypeDTO dto) {
        this.id = dto.getId();
        this.name = dto.getName();
        this.creationDate = dto.getCreationDate();
        this.lastModifiedDate = dto.getLastModifiedDate();
        this.lastModifiedUser = dto.getLastModifiedUser();
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

}

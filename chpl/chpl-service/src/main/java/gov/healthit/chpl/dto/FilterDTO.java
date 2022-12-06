package gov.healthit.chpl.dto;

import java.util.Date;

import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.entity.FilterEntity;

@Deprecated
public class FilterDTO {
    private Long id;
    private UserDTO user;
    private FilterTypeDTO filterType;
    private String name;
    private String filter;
    private Date creationDate;
    private Date lastModifiedDate;
    private Long lastModifiedUser;
    private Boolean deleted;

    public FilterDTO() {
    }

    public FilterDTO(FilterEntity entity) {
        this.id = entity.getId();
        this.setFilterType(new FilterTypeDTO(entity.getFilterType()));
        this.name = entity.getName();
        this.filter = entity.getFilter();
        this.creationDate = entity.getCreationDate();
        this.lastModifiedDate = entity.getLastModifiedDate();
        this.deleted = entity.getDeleted();
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public UserDTO getUser() {
        return user;
    }

    public void setUser(final UserDTO user) {
        this.user = user;
    }

    public FilterTypeDTO getFilterType() {
        return filterType;
    }

    public void setFilterType(FilterTypeDTO filterType) {
        this.filterType = filterType;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(final String filter) {
        this.filter = filter;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "FilterDTO [id=" + id + ", user=" + user + ", filterType=" + filterType + ", name=" + name + ", filter="
                + filter + ", creationDate=" + creationDate + ", lastModifiedDate=" + lastModifiedDate
                + ", lastModifiedUser=" + lastModifiedUser + ", deleted=" + deleted + "]";
    }
}

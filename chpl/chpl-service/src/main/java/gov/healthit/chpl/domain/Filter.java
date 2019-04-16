package gov.healthit.chpl.domain;

import java.util.Date;

import gov.healthit.chpl.dto.FilterDTO;

public class Filter {
    private Long id;
    private FilterType filterType;
    private String filter;
    private Date creationDate;
    private Date lastModifiedDate;
    private Long lastModifiedUser;

    public Filter() {
    }

    public Filter(FilterDTO dto) {
        this.id = dto.getId();
        this.filterType = new FilterType(dto.getFilterType());
        this.filter = dto.getFilter();
        this.creationDate = dto.getCreationDate();
        this.lastModifiedDate = dto.getLastModifiedDate();
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public FilterType getFilterType() {
        return filterType;
    }

    public void setFilterType(FilterType filterType) {
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
}

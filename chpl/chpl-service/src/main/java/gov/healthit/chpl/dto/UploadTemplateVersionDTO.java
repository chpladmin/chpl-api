package gov.healthit.chpl.dto;

import java.io.Serializable;
import java.util.Date;

import gov.healthit.chpl.entity.listing.pending.UploadTemplateVersionEntity;
import gov.healthit.chpl.util.Util;

public class UploadTemplateVersionDTO implements Serializable {
    private static final long serialVersionUID = -7841449230766088264L;
    private Long id;
    private String name;
    private Date availableAsOf;
    private Boolean deprecated;
    private String headerCsv;

    public UploadTemplateVersionDTO() {
    }

    public UploadTemplateVersionDTO(UploadTemplateVersionEntity entity) {
        this.id = entity.getId();
        this.name = entity.getName();
        this.availableAsOf = entity.getAvailableAsOfDate();
        this.deprecated = entity.getDeprecated();
        this.headerCsv = entity.getHeaderCsv();
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Date getAvailableAsOf() {
        return Util.getNewDate(availableAsOf);
    }

    public void setAvailableAsOf(final Date availableAsOf) {
        this.availableAsOf = Util.getNewDate(availableAsOf);
    }

    public Boolean getDeprecated() {
        return deprecated;
    }

    public void setDeprecated(final Boolean deprecated) {
        this.deprecated = deprecated;
    }

    public String getHeaderCsv() {
        return headerCsv;
    }

    public void setHeaderCsv(final String headerCsv) {
        this.headerCsv = headerCsv;
    }
}

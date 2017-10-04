package gov.healthit.chpl.domain;

import java.io.Serializable;

import gov.healthit.chpl.dto.UploadTemplateVersionDTO;

public class UploadTemplateVersion implements Serializable {
    private static final long serialVersionUID = -6175366628840736413L;
    private Long id;
    private String name;
    private String description;
    private boolean deprecated;
    private long availableAsOf;

    public UploadTemplateVersion() {
    }

    public UploadTemplateVersion(UploadTemplateVersionDTO dto) {
        this.id = dto.getId();
        this.name = dto.getName();
        this.description = dto.getDescription();
        this.deprecated = dto.getDeprecated();
        this.availableAsOf = dto.getAvailableAsOf().getTime();
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

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public boolean isDeprecated() {
        return deprecated;
    }

    public void setDeprecated(final boolean deprecated) {
        this.deprecated = deprecated;
    }

    public long getAvailableAsOf() {
        return availableAsOf;
    }

    public void setAvailableAsOf(final long availableAsOf) {
        this.availableAsOf = availableAsOf;
    }
}

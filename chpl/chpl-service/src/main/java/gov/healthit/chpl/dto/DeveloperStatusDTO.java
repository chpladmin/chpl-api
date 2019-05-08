package gov.healthit.chpl.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.entity.developer.DeveloperStatusEntity;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DeveloperStatusDTO implements Serializable {
    private static final long serialVersionUID = 6227999632663396485L;
    private Long id;
    private String statusName;

    public DeveloperStatusDTO() {
    }

    public DeveloperStatusDTO(DeveloperStatusEntity entity) {
        this.id = entity.getId();
        this.statusName = entity.getName().toString();
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getStatusName() {
        return statusName;
    }

    public void setStatusName(final String statusName) {
        this.statusName = statusName;
    }
}

package gov.healthit.chpl.dto;

import java.io.Serializable;

import gov.healthit.chpl.entity.UcdProcessEntity;

public class UcdProcessDTO implements Serializable {
    private static final long serialVersionUID = -7841496230766088264L;
    private Long id;
    private String name;

    public UcdProcessDTO() {
    }

    public UcdProcessDTO(UcdProcessEntity entity) {
        this.id = entity.getId();
        this.name = entity.getName();
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
}

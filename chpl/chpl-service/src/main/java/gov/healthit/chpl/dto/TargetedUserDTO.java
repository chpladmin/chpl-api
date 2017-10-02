package gov.healthit.chpl.dto;

import java.io.Serializable;

import gov.healthit.chpl.entity.TargetedUserEntity;

public class TargetedUserDTO implements Serializable {
    private static final long serialVersionUID = 6819005018143479705L;
    private Long id;
    private String name;

    public TargetedUserDTO() {
    }

    public TargetedUserDTO(TargetedUserEntity entity) {
        this.id = entity.getId();
        this.name = entity.getName();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

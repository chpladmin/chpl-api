package gov.healthit.chpl.dto;

import gov.healthit.chpl.entity.RoleEntity;

public class RoleDTO {

    private Long id;

    private String name;

    private String description;

    private String authority;

    private Boolean deleted;

    public RoleDTO() {

    }

    public RoleDTO(RoleEntity entity) {
        this.id = entity.getId();
        this.name = entity.getName();
        this.authority = entity.getName();
        this.deleted = entity.getDeleted();
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAuthority() {
        return authority;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

}

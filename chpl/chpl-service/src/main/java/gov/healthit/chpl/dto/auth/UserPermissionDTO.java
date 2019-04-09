package gov.healthit.chpl.dto.auth;

import gov.healthit.chpl.auth.entity.UserPermissionEntity;
import gov.healthit.chpl.auth.permission.GrantedPermission;

public class UserPermissionDTO {
    private Long id;
    private String authority;
    private String name;
    private String description;

    public UserPermissionDTO() {
    }

    public UserPermissionDTO(final UserPermissionEntity entity) {
        this.id = entity.getId();
        this.authority = entity.getAuthority();
        this.name = entity.getName();
        this.description = entity.getDescription();
    }

    public GrantedPermission getGrantedPermission() {
        return new GrantedPermission(authority);
    }

    public String getAuthority() {
        return authority;
    }

    public void setAuthority(final String authority) {
        this.authority = authority;
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

    public String toString() {
        return authority;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}

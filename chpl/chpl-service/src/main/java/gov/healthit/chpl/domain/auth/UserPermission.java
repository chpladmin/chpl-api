package gov.healthit.chpl.domain.auth;

import gov.healthit.chpl.dto.auth.UserPermissionDTO;

public class UserPermission {
    private Long id;
    private String authority;
    private String name;
    private String description;

    public UserPermission() {
    }

    public UserPermission(final UserPermissionDTO dto) {
        this.id = dto.getId();
        this.authority = dto.getAuthority();
        this.name = dto.getName();
        this.description = dto.getDescription();
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

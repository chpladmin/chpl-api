package gov.healthit.chpl.dto;

import gov.healthit.chpl.auth.dto.UserDTO;
import gov.healthit.chpl.entity.UserRoleMapEntity;

public class UserRoleMapDTO {
    private Long id;

    private UserDTO user;

    private RoleDTO role;

    private Boolean deleted;

    public UserRoleMapDTO() {

    }

    public UserRoleMapDTO(UserRoleMapEntity entity) {
        this.id = entity.getId();
        this.role = new RoleDTO(entity.getRole());
        this.user = new UserDTO(entity.getUser());
        this.deleted = entity.getDeleted();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }

    public RoleDTO getRole() {
        return role;
    }

    public void setRole(RoleDTO role) {
        this.role = role;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }
}

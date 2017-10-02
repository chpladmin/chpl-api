package gov.healthit.chpl.dto.notification;

import java.util.ArrayList;
import java.util.List;

import gov.healthit.chpl.auth.dto.UserPermissionDTO;
import gov.healthit.chpl.entity.notification.NotificationPermissionEntity;
import gov.healthit.chpl.entity.notification.NotificationTypeEntity;

public class NotificationTypeDTO {
    private Long id;
    private String name;
    private String description;
    private Boolean requiresAcb;
    private List<UserPermissionDTO> permissions;

    public NotificationTypeDTO() {
        permissions = new ArrayList<UserPermissionDTO>();
    }

    public NotificationTypeDTO(NotificationTypeEntity entity) {
        this();
        this.id = entity.getId();
        this.name = entity.getName();
        this.description = entity.getDescription();
        this.requiresAcb = entity.getRequiresAcb();
        if (entity.getPermissions() != null && entity.getPermissions().size() > 0) {
            for (NotificationPermissionEntity notifPerm : entity.getPermissions()) {
                UserPermissionDTO perm = new UserPermissionDTO(notifPerm.getPermission());
                this.permissions.add(perm);
            }
        }
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

    public List<UserPermissionDTO> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<UserPermissionDTO> permissions) {
        this.permissions = permissions;
    }

    public Boolean getRequiresAcb() {
        return requiresAcb;
    }

    public void setRequiresAcb(Boolean requiresAcb) {
        this.requiresAcb = requiresAcb;
    }
}

package gov.healthit.chpl.domain;

import java.io.Serializable;

public class PermissionDeletedResponse implements Serializable {
    private static final long serialVersionUID = -5021860424039007381L;

    private boolean permissionDeleted;

    public boolean isPermissionDeleted() {
        return permissionDeleted;
    }

    public void setPermissionDeleted(final boolean permissionDeleted) {
        this.permissionDeleted = permissionDeleted;
    }
}

package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.List;

import gov.healthit.chpl.domain.auth.User;

public class PermittedUser implements Serializable {
    private static final long serialVersionUID = -7978555260304001452L;
    private User user;
    private List<String> roles;
    private List<String> permissions;

    /** Default constructor. */
    public PermittedUser() {
    }

    public User getUser() {
        return user;
    }

    public void setUser(final User user) {
        this.user = user;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(final List<String> roles) {
        this.roles = roles;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(final List<String> permissions) {
        this.permissions = permissions;
    }
}

package gov.healthit.chpl.auth.permission;

import org.springframework.security.core.GrantedAuthority;

public class GrantedPermission implements GrantedAuthority {
    private static final long serialVersionUID = -6187373552693311470L;
    private String authority;

    public GrantedPermission(final String authority) {
        this.authority = authority;
    }

    @Override
    public String getAuthority() {
        return authority;
    }

    @Override
    public String toString() {
        return authority;
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof GrantedPermission)) {
            return false;
        }
        GrantedPermission claim = (GrantedPermission) obj;
        return claim.getAuthority() == this.getAuthority() || claim.getAuthority().equals(this.getAuthority());
    }

    @Override
    public int hashCode() {
        return getAuthority() == null ? 0 : getAuthority().hashCode();
    }

}

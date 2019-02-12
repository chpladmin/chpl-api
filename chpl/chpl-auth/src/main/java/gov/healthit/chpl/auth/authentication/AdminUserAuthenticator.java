package gov.healthit.chpl.auth.authentication;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;

import gov.healthit.chpl.auth.domain.Authority;
import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.auth.user.User;

/**
 * This class is for simulating a user with ROLE_ADMIN in our system.
 * It should be used sparingly, if ever.
 * @author kekey
 *
 */
public class AdminUserAuthenticator extends JWTAuthenticatedUser {
    private static final long serialVersionUID = -8874289831576797927L;

    @Override
    public Long getId() {
        return User.ADMIN_USER_ID;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> auths = new ArrayList<GrantedAuthority>();
        auths.add(new GrantedPermission(Authority.ROLE_ADMIN));
        return auths;
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getDetails() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return getName();
    }

    @Override
    public String getSubjectName() {
        return this.getName();
    }

    @Override
    public boolean isAuthenticated() {
        return true;
    }

    @Override
    public void setAuthenticated(final boolean arg0) throws IllegalArgumentException {
    }

    @Override
    public String getName() {
        return "admin";
    }
}

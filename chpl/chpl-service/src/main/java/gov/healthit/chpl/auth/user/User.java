package gov.healthit.chpl.auth.user;

import java.util.Collection;
import java.util.Set;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import gov.healthit.chpl.auth.permission.GrantedPermission;

public interface User extends UserDetails, Authentication {

    Long ADMIN_USER_ID = -2L;
    Long SYSTEM_USER_ID = -3L;

    Long getId();
    String getSubjectName();
    void setSubjectName(String subject);

    void setFullName(String fullName);
    String getFullName();
    void setFriendlyName(String friendlyName);
    String getFriendlyName();
    boolean getPasswordResetRequired();
    void setPasswordResetRequired(boolean setPasswordResetRequired);
    Set<GrantedPermission> getPermissions();
    void addPermission(GrantedPermission permission);
    void removePermission(String permissionValue);

    // UserDetails interface
    @Override String getPassword();

    @Override String getUsername();

    @Override boolean isAccountNonExpired();

    @Override boolean isAccountNonLocked();

    @Override boolean isCredentialsNonExpired();

    @Override boolean isEnabled();

    // Authentication Interface
    @Override Collection<? extends GrantedAuthority> getAuthorities();

    @Override Object getCredentials();

    @Override Object getDetails();

    @Override Object getPrincipal();

    @Override boolean isAuthenticated();

    @Override void setAuthenticated(boolean arg0) throws IllegalArgumentException;

    @Override String getName();
}

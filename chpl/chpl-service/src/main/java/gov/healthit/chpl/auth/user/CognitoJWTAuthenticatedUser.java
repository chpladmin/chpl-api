package gov.healthit.chpl.auth.user;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.springframework.security.core.GrantedAuthority;

import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.dto.auth.UserDTO;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CognitoJWTAuthenticatedUser implements User {
    private static final long serialVersionUID = 2847351904166625725L;

    private Long id;
    private UUID ssoId;
    private String email;
    private String fullName;
    private String friendlyName;

    @Builder.Default
    private Set<GrantedPermission> permissions = new HashSet<GrantedPermission>();

    @Builder.Default
    private boolean accountExpired = false;

    @Builder.Default
    private boolean accountLocked = false;

    @Builder.Default
    private boolean credentialsExpired = false;

    @Builder.Default
    private boolean accountEnabled = true;

    @Builder.Default
    private boolean passwordResetRequired = false;

    @Builder.Default
    private boolean authenticated = true;

    private UserDTO impersonatingUser;

    @Override
    public void addPermission(GrantedPermission permission) {
        this.permissions.add(permission);
    }

    public void addPermission(String permissionValue) {
        GrantedPermission permission = new GrantedPermission(permissionValue);
        this.permissions.add(permission);
    }

    @Override
    public void removePermission(String permissionValue) {
        this.permissions.remove(new GrantedPermission(permissionValue));
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.getPermissions();
    }

    @Override
    public Object getCredentials() {
        return this.getPassword();
    }

    @Override
    public Object getDetails() {
        return this;
    }

    @Override
    public Object getPrincipal() {
        return this.getName();
    }

    @Override
    public boolean isAuthenticated() {
        return this.authenticated;
    }

    @Override
    public void setAuthenticated(boolean arg0) throws IllegalArgumentException {
        this.authenticated = arg0;
    }

    @Override
    public String getName() {
        return email;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return !accountExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !accountLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return !credentialsExpired;
    }

    @Override
    public boolean isEnabled() {
        return accountEnabled;
    }

    @Override
    public boolean getPasswordResetRequired() {
        return this.passwordResetRequired;
    }

    @Override
    public String getSubjectName() {
        return email;
    }

    @Override
    public void setSubjectName(String subject) {
        this.email = subject;
    }

}

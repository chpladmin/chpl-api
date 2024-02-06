package gov.healthit.chpl.auth.user;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import gov.healthit.chpl.dto.auth.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JWTAuthenticatedUser implements UserDetails, Authentication {
    private static final long serialVersionUID = -7558546038256722930L;

    private AuthenticationSystem authenticationSystem;
    private UUID cognitoId;
    private Long id;
    private List<Long> organizationIds;
    private String subjectName;
    private String fullName;
    private String friendlyName;
    private String email;
    private String passwordResetRequired;
    //@Builder.Default
    //private Set<GrantedPermission> permissions = new HashSet<GrantedPermission>();
    private UserDTO impersonatingUser;

    // UserDetails interface
    private String password;
    private String username;
    private boolean accountNonExpired;
    private boolean accountNonLocked;
    private boolean credentialsNonExpired;
    private boolean enabled;

    // Authentication Interface
    @Builder.Default
    private Collection<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
    private Object credentials;
    private Object details;
    private Object principal;
    private boolean authenticated;
    private String name;

    //public void addPermission(GrantedPermission permission) {
    //    permissions.add(permission);
    //}
}

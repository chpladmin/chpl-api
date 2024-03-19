package gov.healthit.chpl.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import gov.healthit.chpl.auth.user.AuthenticationSystem;
import gov.healthit.chpl.auth.user.ChplSystemUsers;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.domain.auth.Authority;

public class AuthUtil {
    public static String getUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth instanceof JWTAuthenticatedUser) {
            return ((JWTAuthenticatedUser) auth).getSubjectName();
        } else {
            throw new RuntimeException("Canot determine the auth user type.");
        }
    }

    public static JWTAuthenticatedUser getCurrentUser() {
        JWTAuthenticatedUser user = null;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JWTAuthenticatedUser) {
            user = (JWTAuthenticatedUser) auth;
        }
        return user;
    }

    /**
     * Get the ID of the active user. If the active user is being impersonated, get the id of the impersonating user instead.
     * @return the user's audit-ready id
     */
    public static long getAuditId() {
        JWTAuthenticatedUser user = null;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth instanceof JWTAuthenticatedUser) {
            user = (JWTAuthenticatedUser) auth;
            if (user.getImpersonatingUser() != null) {
                return user.getImpersonatingUser().getId();
            } else {
                return user.getId();
            }
        }
        return ChplSystemUsers.DEFAULT_USER_ID;
    }


    public static Authentication getCurrentAuthentication() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth;
    }

    public static String fromInt(final Integer toStr) {
        return toStr.toString();
    }

    public static JWTAuthenticatedUser getInvitedUserAuthenticator(final Long id) {
        JWTAuthenticatedUser authenticator = new JWTAuthenticatedUser() {

            @Override
            public Long getId() {
                return id == null ? Long.valueOf(ChplSystemUsers.ADMIN_USER_ID) : id;
            }

            @Override
            public Collection<GrantedAuthority> getAuthorities() {
                List<GrantedAuthority> auths = new ArrayList<GrantedAuthority>();
                auths.add(new SimpleGrantedAuthority(Authority.ROLE_INVITED_USER_CREATOR));
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

            @Override
            public AuthenticationSystem getAuthenticationSystem() {
                return AuthenticationSystem.CHPL;
            }

        };
        return authenticator;
    }
}

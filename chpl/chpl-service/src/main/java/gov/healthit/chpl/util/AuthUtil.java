package gov.healthit.chpl.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.auth.user.SystemUsers;
import gov.healthit.chpl.auth.user.User;

public class AuthUtil {
    public static String getUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth.getPrincipal() instanceof UserDetails) {
            return ((UserDetails) auth.getPrincipal()).getUsername();
        } else {
            return auth.getPrincipal().toString();
        }
    }

    public static User getCurrentUser() {
        User user = null;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof User) {
            user = (User) auth;
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
        return SystemUsers.DEFAULT_USER_ID;
    }


    //TODO - OCD-4377 - Need to figure out how to get this
    public static UUID getAuditSsoUser() {
        //JWTAuthenticatedUser user = null;
        //Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        //if (auth instanceof JWTAuthenticatedUser) {
        //    user = (JWTAuthenticatedUser) auth;
        //    if (user.getImpersonatingUser() != null) {
        //        return user.getImpersonatingUser().getId();
        //    } else {
        //        return user.getId();
        //    }
        //}
        //return User.DEFAULT_USER_ID;

        return UUID.fromString("c5fcfa4d-5557-405d-a9e6-1ba73a469bc3");
    }

    public static Authentication getCurrentAuthentication() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth;
    }

    public static String fromInt(final Integer toStr) {
        return toStr.toString();
    }

    public static Authentication getInvitedUserAuthenticator(final Long id) {
        JWTAuthenticatedUser authenticator = new JWTAuthenticatedUser() {

            @Override
            public Long getId() {
                return id == null ? Long.valueOf(SystemUsers.ADMIN_USER_ID) : id;
            }

            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                List<GrantedAuthority> auths = new ArrayList<GrantedAuthority>();
                auths.add(new GrantedPermission("ROLE_INVITED_USER_CREATOR"));
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

        };
        return authenticator;
    }
}

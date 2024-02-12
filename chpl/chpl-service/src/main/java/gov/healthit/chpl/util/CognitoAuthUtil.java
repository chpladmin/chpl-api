package gov.healthit.chpl.util;

import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import gov.healthit.chpl.auth.user.ChplSystemUsers;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;

public final class CognitoAuthUtil {
    private CognitoAuthUtil() { }

    public static String getUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth.getPrincipal() instanceof UserDetails) {
            return ((UserDetails) auth.getPrincipal()).getUsername();
        } else {
            return auth.getPrincipal().toString();
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


    //TODO - OCD-4377 - Need to figure out how to get this
    public static UUID getAuditCognitoUser() {
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

}

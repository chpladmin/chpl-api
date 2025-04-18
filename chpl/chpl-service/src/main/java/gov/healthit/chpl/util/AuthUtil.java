package gov.healthit.chpl.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;

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

    public static Authentication getCurrentAuthentication() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth;
    }
}

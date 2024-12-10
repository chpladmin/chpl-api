package gov.healthit.chpl.scheduler;

import org.springframework.security.core.context.SecurityContextHolder;

import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;

public class AuthenticatedUserAwareJob {

    public void setSecurityContext(JWTAuthenticatedUser user) {
        SecurityContextHolder.getContext().setAuthentication(user);
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
    }
}

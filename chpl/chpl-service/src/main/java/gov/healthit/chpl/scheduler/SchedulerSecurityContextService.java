package gov.healthit.chpl.scheduler;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.auth.user.CognitoSystemUserService;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.domain.auth.CognitoGroups;
import gov.healthit.chpl.domain.auth.User;
import gov.healthit.chpl.user.cognito.CognitoApiWrapper;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class SchedulerSecurityContextService {

    private CognitoApiWrapper cognitoApiWrapper;
    private CognitoSystemUserService cognitoSystemUserService;

    @Autowired
    public SchedulerSecurityContextService(CognitoApiWrapper cognitoApiWrapper,
            CognitoSystemUserService cognitoSystemUserService) {
        this.cognitoApiWrapper = cognitoApiWrapper;
        this.cognitoSystemUserService = cognitoSystemUserService;
    }

    public void setAdminSecurityContext() {
        User user = null;
        try {
            user = cognitoApiWrapper.getUserInfo(cognitoSystemUserService.getSystemUserUuId());
        } catch (Exception ex) {
            LOGGER.error("Unable to get Cognito user with UUID " + cognitoSystemUserService.getSystemUserUuId(), ex);
        }

        SecurityContextHolder.getContext().setAuthentication(JWTAuthenticatedUser.builder()
                .authenticated(true)
                .fullName(user.getFullName())
                .cognitoId(user.getCognitoId())
                .subjectName(user.getSubjectName())
                .authorities(List.of(new SimpleGrantedAuthority(CognitoGroups.CHPL_ADMIN)))
                .build());
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
    }

    public void setSecurityContext(User user) {
        SecurityContextHolder.getContext().setAuthentication(JWTAuthenticatedUser.builder()
                .authenticated(true)
                .fullName(user.getFullName())
                .cognitoId(user.getCognitoId())
                .subjectName(user.getEmail())
                .authorities(List.of(new SimpleGrantedAuthority(user.getRole())))
                .build());
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
    }
}

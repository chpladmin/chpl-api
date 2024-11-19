package gov.healthit.chpl.scheduler;

import java.util.List;

import org.ff4j.FF4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.auth.user.AuthenticationSystem;
import gov.healthit.chpl.auth.user.ChplSystemUsers;
import gov.healthit.chpl.auth.user.CognitoSystemUserService;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.domain.auth.Authority;
import gov.healthit.chpl.domain.auth.CognitoGroups;
import gov.healthit.chpl.domain.auth.User;
import gov.healthit.chpl.user.cognito.CognitoApiWrapper;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class SchedulerSecurityContextService {

    private CognitoApiWrapper cognitoApiWrapper;
    private CognitoSystemUserService cognitoSystemUserService;
    private FF4j ff4j;

    @Autowired
    public SchedulerSecurityContextService(CognitoApiWrapper cognitoApiWrapper,
            CognitoSystemUserService cognitoSystemUserService,
            FF4j ff4j) {
        this.cognitoApiWrapper = cognitoApiWrapper;
        this.cognitoSystemUserService = cognitoSystemUserService;
        this.ff4j = ff4j;
    }

    public void setAdminSecurityContext() {
        User user = null;
        if (ff4j.check(FeatureList.SSO)) {
            try {
                user = cognitoApiWrapper.getUserInfo(cognitoSystemUserService.getSystemUserUuId());
            } catch (Exception ex) {
                LOGGER.error("Unable to get Cognito user with UUID " + cognitoSystemUserService.getSystemUserUuId(), ex);
            }
        } else {
            user = new User();
            user.setFullName("Administrator");
            user.setFriendlyName("Admin");
            user.setSubjectName("admin");
        }

        SecurityContextHolder.getContext().setAuthentication(JWTAuthenticatedUser.builder()
                .authenticated(true)
                .authenticationSystem(ff4j.check(FeatureList.SSO) ? AuthenticationSystem.COGNITO : AuthenticationSystem.CHPL)
                .fullName(user.getFullName())
                .id(ff4j.check(FeatureList.SSO) ? null : ChplSystemUsers.ADMIN_USER_ID)
                .cognitoId(ff4j.check(FeatureList.SSO) ? user.getCognitoId() : null)
                .friendlyName(user.getFriendlyName())
                .subjectName(user.getSubjectName())
                .authorities(List.of(
                        new SimpleGrantedAuthority(ff4j.check(FeatureList.SSO) ? CognitoGroups.CHPL_ADMIN : Authority.ROLE_ADMIN)))
                .build());
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
    }

    public void setSecurityContext(User user) {
        SecurityContextHolder.getContext().setAuthentication(JWTAuthenticatedUser.builder()
                .authenticated(true)
                .authenticationSystem(ff4j.check(FeatureList.SSO) ? AuthenticationSystem.COGNITO : AuthenticationSystem.CHPL)
                .fullName(user.getFullName())
                .id(ff4j.check(FeatureList.SSO) ? null : user.getUserId())
                .cognitoId(ff4j.check(FeatureList.SSO) ? user.getCognitoId() : null)
                .friendlyName(user.getFriendlyName())
                .subjectName(user.getEmail())
                .authorities(List.of(new SimpleGrantedAuthority(user.getRole())))
                .build());
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
    }
}

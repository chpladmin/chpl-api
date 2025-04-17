package gov.healthit.chpl;

import java.util.UUID;

import org.mockito.Mockito;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.domain.auth.CognitoGroups;
import gov.healthit.chpl.permissions.ResourcePermissions;

public class TestingUsers {
    public void setupForAdminUser(ResourcePermissions resourcePermissions) {
        SecurityContextHolder.getContext().setAuthentication(getAdminUser());
        Mockito.when(resourcePermissions.isUserRoleAdmin()).thenReturn(true);
    }

    private JWTAuthenticatedUser getAdminUser() {
        JWTAuthenticatedUser adminUser = new JWTAuthenticatedUser();
        adminUser.setFullName("Administrator");
        adminUser.setCognitoId(UUID.randomUUID());
        adminUser.setFriendlyName("Administrator");
        adminUser.setSubjectName("admin");
        adminUser.getAuthorities().add(new SimpleGrantedAuthority(CognitoGroups.CHPL_ADMIN));
        return adminUser;
    }

    public void setupForAcbUser(ResourcePermissions resourcePermissions) {
        SecurityContextHolder.getContext().setAuthentication(getAcbUser());
        Mockito.when(resourcePermissions.isUserRoleAcbAdmin()).thenReturn(true);
    }

    private JWTAuthenticatedUser getAcbUser() {
        JWTAuthenticatedUser acbUser = new JWTAuthenticatedUser();
        acbUser.setFullName("Test");
        acbUser.setCognitoId(UUID.randomUUID());
        acbUser.setFriendlyName("User3");
        acbUser.setSubjectName("testUser3");
        acbUser.getAuthorities().add(new SimpleGrantedAuthority(CognitoGroups.CHPL_ACB));
        return acbUser;
    }

    public void setupForCmsUser(ResourcePermissions resourcePermissions) {
        SecurityContextHolder.getContext().setAuthentication(getCmsUser());
        Mockito.when(resourcePermissions.isUserRoleCmsStaff()).thenReturn(true);
    }

    public void setupForStartupUser(ResourcePermissions resourcePermissions) {
        SecurityContextHolder.getContext().setAuthentication(getStartupUser());
        Mockito.when(resourcePermissions.isUserRoleStartup()).thenReturn(true);
    }

    private JWTAuthenticatedUser getCmsUser() {
        JWTAuthenticatedUser cmsUser = new JWTAuthenticatedUser();
        cmsUser.setFullName("CMS");
        cmsUser.setCognitoId(UUID.randomUUID());
        cmsUser.setFriendlyName("User");
        cmsUser.setSubjectName("cmsUser");
        cmsUser.getAuthorities().add(new SimpleGrantedAuthority(CognitoGroups.CHPL_CMS_STAFF));

        return cmsUser;
    }

    private JWTAuthenticatedUser getStartupUser() {
        JWTAuthenticatedUser startupUser = new JWTAuthenticatedUser();
        startupUser.setFullName("Startup User");
        startupUser.setCognitoId(UUID.randomUUID());
        startupUser.setFriendlyName("Startup");
        startupUser.setSubjectName("startpUser");
        startupUser.getAuthorities().add(new SimpleGrantedAuthority(CognitoGroups.CHPL_STARTUP));
        return startupUser;
    }

    public void setupForOncUser(ResourcePermissions resourcePermissions) {
        SecurityContextHolder.getContext().setAuthentication(getOncUser());
        Mockito.when(resourcePermissions.isUserRoleOnc()).thenReturn(true);
    }

    private JWTAuthenticatedUser getOncUser() {
        JWTAuthenticatedUser oncUser = new JWTAuthenticatedUser();
        oncUser.setFullName("ONC");
        oncUser.setCognitoId(UUID.randomUUID());
        oncUser.setFriendlyName("User");
        oncUser.setSubjectName("oncUser");
        oncUser.getAuthorities().add(new SimpleGrantedAuthority(CognitoGroups.CHPL_ONC));
        return oncUser;
    }

    public void setupForDeveloperUser(ResourcePermissions resourcePermissions) {
        SecurityContextHolder.getContext().setAuthentication(getDeveloperUser());
        Mockito.when(resourcePermissions.isUserRoleDeveloperAdmin()).thenReturn(true);
    }

    private JWTAuthenticatedUser getDeveloperUser() {
        JWTAuthenticatedUser developerUser = new JWTAuthenticatedUser();
        developerUser.setFullName("Developer");
        developerUser.setCognitoId(UUID.randomUUID());
        developerUser.setFriendlyName("User");
        developerUser.setSubjectName("developerUser");
        developerUser.getAuthorities().add(new SimpleGrantedAuthority(CognitoGroups.CHPL_DEVELOPER));
        return developerUser;
    }

}

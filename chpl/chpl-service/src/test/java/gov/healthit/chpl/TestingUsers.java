package gov.healthit.chpl;

import org.mockito.Mockito;
import org.springframework.security.core.context.SecurityContextHolder;

import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.permissions.ResourcePermissions;

public class TestingUsers {
    public void setupForAdminUser(ResourcePermissions resourcePermissions) {
        SecurityContextHolder.getContext().setAuthentication(getAdminUser());
        Mockito.when(resourcePermissions.isUserRoleAdmin()).thenReturn(true);
    }

    private JWTAuthenticatedUser getAdminUser() {
        JWTAuthenticatedUser adminUser = new JWTAuthenticatedUser();
        adminUser.setFullName("Administrator");
        adminUser.setId(-2L);
        adminUser.setFriendlyName("Administrator");
        adminUser.setSubjectName("admin");
        adminUser.getPermissions().add(new GrantedPermission("ROLE_ADMIN"));
        return adminUser;
    }

    public void setupForAcbUser(ResourcePermissions resourcePermissions) {
        SecurityContextHolder.getContext().setAuthentication(getAcbUser());
        Mockito.when(resourcePermissions.isUserRoleAcbAdmin()).thenReturn(true);
    }

    private JWTAuthenticatedUser getAcbUser() {
        JWTAuthenticatedUser acbUser = new JWTAuthenticatedUser();
        acbUser.setFullName("Test");
        acbUser.setId(3L);
        acbUser.setFriendlyName("User3");
        acbUser.setSubjectName("testUser3");
        acbUser.getPermissions().add(new GrantedPermission("ROLE_ACB"));
        return acbUser;
    }

    public void setupForAtlUser(ResourcePermissions resourcePermissions) {
        SecurityContextHolder.getContext().setAuthentication(getAtlUser());
        Mockito.when(resourcePermissions.isUserRoleAtlAdmin()).thenReturn(true);
    }

    private JWTAuthenticatedUser getAtlUser() {
        JWTAuthenticatedUser atlUser = new JWTAuthenticatedUser();
        atlUser.setFullName("ATL");
        atlUser.setId(3L);
        atlUser.setFriendlyName("User");
        atlUser.setSubjectName("atlUser");
        atlUser.getPermissions().add(new GrantedPermission("ROLE_ATL"));
        return atlUser;
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
        cmsUser.setId(3L);
        cmsUser.setFriendlyName("User");
        cmsUser.setSubjectName("cmsUser");
        cmsUser.getPermissions().add(new GrantedPermission("ROLE_CMS_STAFF"));
        return cmsUser;
    }

    private JWTAuthenticatedUser getStartupUser() {
        JWTAuthenticatedUser startupUser = new JWTAuthenticatedUser();
        startupUser.setFullName("Startup User");
        startupUser.setId(-4L);
        startupUser.setFriendlyName("Startup");
        startupUser.setSubjectName("startpUser");
        startupUser.getPermissions().add(new GrantedPermission("ROLE_STARTUP"));
        return startupUser;
    }

    public void setupForOncUser(ResourcePermissions resourcePermissions) {
        SecurityContextHolder.getContext().setAuthentication(getOncUser());
        Mockito.when(resourcePermissions.isUserRoleOnc()).thenReturn(true);
    }

    private JWTAuthenticatedUser getOncUser() {
        JWTAuthenticatedUser oncUser = new JWTAuthenticatedUser();
        oncUser.setFullName("ONC");
        oncUser.setId(3L);
        oncUser.setFriendlyName("User");
        oncUser.setSubjectName("oncUser");
        oncUser.getPermissions().add(new GrantedPermission("ROLE_ONC"));
        return oncUser;
    }

    public void setupForOncStaffUser(ResourcePermissions resourcePermissions) {
        SecurityContextHolder.getContext().setAuthentication(getOncStaffUser());
        Mockito.when(resourcePermissions.isUserRoleOncStaff()).thenReturn(true);
    }

    private JWTAuthenticatedUser getOncStaffUser() {
        JWTAuthenticatedUser oncUser = new JWTAuthenticatedUser();
        oncUser.setFullName("ONC Staff");
        oncUser.setId(3L);
        oncUser.setFriendlyName("User");
        oncUser.setSubjectName("oncStaffUser");
        oncUser.getPermissions().add(new GrantedPermission("ROLE_ONC_STAFF"));
        return oncUser;
    }

    public void setupForDeveloperUser(ResourcePermissions resourcePermissions) {
        SecurityContextHolder.getContext().setAuthentication(getDeveloperUser());
        Mockito.when(resourcePermissions.isUserRoleDeveloperAdmin()).thenReturn(true);
    }

    private JWTAuthenticatedUser getDeveloperUser() {
        JWTAuthenticatedUser oncUser = new JWTAuthenticatedUser();
        oncUser.setFullName("Developer");
        oncUser.setId(3L);
        oncUser.setFriendlyName("User");
        oncUser.setSubjectName("developerUser");
        oncUser.getPermissions().add(new GrantedPermission("ROLE_DEVELOPER"));
        return oncUser;
    }

}

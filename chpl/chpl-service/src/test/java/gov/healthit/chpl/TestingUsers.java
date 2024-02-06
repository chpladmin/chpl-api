package gov.healthit.chpl;

import org.mockito.Mockito;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.domain.auth.Authority;
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
        //adminUser.getPermissions().add(new GrantedPermission("ROLE_ADMIN"));
        adminUser.getAuthorities().add(new SimpleGrantedAuthority(Authority.ROLE_ADMIN));
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
        //acbUser.getPermissions().add(new GrantedPermission("ROLE_ACB"));
        acbUser.getAuthorities().add(new SimpleGrantedAuthority(Authority.ROLE_ACB));
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
        cmsUser.setId(3L);
        cmsUser.setFriendlyName("User");
        cmsUser.setSubjectName("cmsUser");
        //cmsUser.getPermissions().add(new GrantedPermission("ROLE_CMS_STAFF"));
        cmsUser.getAuthorities().add(new SimpleGrantedAuthority(Authority.ROLE_CMS_STAFF));

        return cmsUser;
    }

    private JWTAuthenticatedUser getStartupUser() {
        JWTAuthenticatedUser startupUser = new JWTAuthenticatedUser();
        startupUser.setFullName("Startup User");
        startupUser.setId(-4L);
        startupUser.setFriendlyName("Startup");
        startupUser.setSubjectName("startpUser");
        //startupUser.getPermissions().add(new GrantedPermission("ROLE_STARTUP"));
        startupUser.getAuthorities().add(new SimpleGrantedAuthority(Authority.ROLE_STARTUP));
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
        //oncUser.getPermissions().add(new GrantedPermission("ROLE_ONC"));
        oncUser.getAuthorities().add(new SimpleGrantedAuthority(Authority.ROLE_ONC));
        return oncUser;
    }

    public void setupForDeveloperUser(ResourcePermissions resourcePermissions) {
        SecurityContextHolder.getContext().setAuthentication(getDeveloperUser());
        Mockito.when(resourcePermissions.isUserRoleDeveloperAdmin()).thenReturn(true);
    }

    private JWTAuthenticatedUser getDeveloperUser() {
        JWTAuthenticatedUser developerUser = new JWTAuthenticatedUser();
        developerUser.setFullName("Developer");
        developerUser.setId(3L);
        developerUser.setFriendlyName("User");
        developerUser.setSubjectName("developerUser");
        //oncUser.getPermissions().add(new GrantedPermission("ROLE_DEVELOPER"));
        developerUser.getAuthorities().add(new SimpleGrantedAuthority(Authority.ROLE_DEVELOPER));
        return developerUser;
    }

}

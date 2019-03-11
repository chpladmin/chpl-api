package gov.healthit.chpl.app.permissions.domain;

import java.util.ArrayList;
import java.util.List;

import org.mockito.Mockito;
import org.springframework.security.core.context.SecurityContextHolder;

import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.dto.TestingLabDTO;
import gov.healthit.chpl.permissions.ResourcePermissions;

public abstract class ActionPermissionsBaseTest {
    public final static Long ROLE_ONC_ID = 8l;
    public final static Long ROLE_ADMIN_ID = -2l;
    public final static Long ROLE_ACB_ID = 2l;

    public abstract void hasAccess_Admin() throws Exception;

    public abstract void hasAccess_Onc() throws Exception;

    public abstract void hasAccess_Acb() throws Exception;

    public abstract void hasAccess_Atl() throws Exception;

    public abstract void hasAccess_Cms() throws Exception;

    public abstract void hasAccess_Anon() throws Exception;

    public List<CertificationBodyDTO> getAllAcbForUser(Long... acbIds) {
        List<CertificationBodyDTO> dtos = new ArrayList<CertificationBodyDTO>();

        for (Long acbId : acbIds) {
            CertificationBodyDTO dto = new CertificationBodyDTO();
            dto.setId(acbId);
            dtos.add(dto);
        }

        return dtos;
    }

    public List<TestingLabDTO> getAllAtlForUser(Long... atlIds) {
        List<TestingLabDTO> dtos = new ArrayList<TestingLabDTO>();

        for (Long atlId : atlIds) {
            TestingLabDTO dto = new TestingLabDTO();
            dto.setId(atlId);
            dtos.add(dto);
        }

        return dtos;
    }

    public CertifiedProductDTO getCertifiedProduct(Long id, Long certificationBodyId) {
        CertifiedProductDTO dto = new CertifiedProductDTO();
        dto.setId(id);
        dto.setCertificationBodyId(certificationBodyId);

        return dto;
    }

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

    private JWTAuthenticatedUser getCmsUser() {
        JWTAuthenticatedUser cmsUser = new JWTAuthenticatedUser();
        cmsUser.setFullName("CMS");
        cmsUser.setId(3L);
        cmsUser.setFriendlyName("User");
        cmsUser.setSubjectName("cmsUser");
        cmsUser.getPermissions().add(new GrantedPermission("ROLE_CMS_STAFF"));
        return cmsUser;
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

    public void setupForAnonUser(ResourcePermissions resourcePermissions) {
        SecurityContextHolder.getContext().setAuthentication(null);
        Mockito.when(resourcePermissions.isUserRoleAdmin()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleOnc()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleAcbAdmin()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleAtlAdmin()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleCmsStaff()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleUserCreator()).thenReturn(false);

    }
}

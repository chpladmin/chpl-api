package gov.healthit.chpl.app.permissions.domain.pendingsurveillance;

import java.util.ArrayList;
import java.util.List;

import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.CertifiedProductDTO;

public abstract class ActionPermissionsBaseTest {
    public abstract void hasAccess_Admin() throws Exception;
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

    public CertifiedProductDTO getCertifiedProduct(Long certifiectionBodyId) {
        CertifiedProductDTO dto = new CertifiedProductDTO();
        dto.setId(1l);
        dto.setCertificationBodyId(certifiectionBodyId);

        return dto;
    }

    public JWTAuthenticatedUser getAdminUser() {
        JWTAuthenticatedUser adminUser = new JWTAuthenticatedUser();
        adminUser.setFullName("Administrator");
        adminUser.setId(-2L);
        adminUser.setFriendlyName("Administrator");
        adminUser.setSubjectName("admin");
        adminUser.getPermissions().add(new GrantedPermission("ROLE_ADMIN"));
        return adminUser;
    }

    public JWTAuthenticatedUser getAcbUser() {
        JWTAuthenticatedUser acbUser = new JWTAuthenticatedUser();
        acbUser.setFullName("Test");
        acbUser.setId(3L);
        acbUser.setFriendlyName("User3");
        acbUser.setSubjectName("testUser3");
        acbUser.getPermissions().add(new GrantedPermission("ROLE_ACB"));
        return acbUser;
    }

    public JWTAuthenticatedUser getAtlUser() {
        JWTAuthenticatedUser atlUser = new JWTAuthenticatedUser();
        atlUser.setFullName("ATL");
        atlUser.setId(3L);
        atlUser.setFriendlyName("User");
        atlUser.setSubjectName("atlUser");
        atlUser.getPermissions().add(new GrantedPermission("ROLE_ATL"));
        return atlUser;
    }

    public JWTAuthenticatedUser getCmsUser() {
        JWTAuthenticatedUser cmsUser = new JWTAuthenticatedUser();
        cmsUser.setFullName("CMS");
        cmsUser.setId(3L);
        cmsUser.setFriendlyName("User");
        cmsUser.setSubjectName("cmsUser");
        cmsUser.getPermissions().add(new GrantedPermission("ROLE_CMS_STAFF"));
        return cmsUser;
    }

}

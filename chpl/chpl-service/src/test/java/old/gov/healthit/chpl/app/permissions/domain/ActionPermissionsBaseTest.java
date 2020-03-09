package old.gov.healthit.chpl.app.permissions.domain;

import java.util.ArrayList;
import java.util.List;

import org.mockito.Mockito;
import org.springframework.security.core.context.SecurityContextHolder;

import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.TestingLabDTO;
import gov.healthit.chpl.permissions.ResourcePermissions;
import old.gov.healthit.chpl.TestingUsers;

public abstract class ActionPermissionsBaseTest extends TestingUsers {
    public final static Long ROLE_ONC_ID = 8l;
    public final static Long ROLE_ADMIN_ID = -2l;
    public final static Long ROLE_ACB_ID = 2l;

    public abstract void hasAccess_Admin() throws Exception;

    public abstract void hasAccess_Onc() throws Exception;

    public abstract void hasAccess_Acb() throws Exception;

    public abstract void hasAccess_Atl() throws Exception;

    public abstract void hasAccess_Cms() throws Exception;

    public abstract void hasAccess_Anon() throws Exception;

    public void hasAccess_Developer() throws Exception {
        // Do nothing - just Override if necessary
    }

    public List<CertificationBodyDTO> getAllAcbForUser(Long... acbIds) {
        List<CertificationBodyDTO> dtos = new ArrayList<CertificationBodyDTO>();

        for (Long acbId : acbIds) {
            CertificationBodyDTO dto = new CertificationBodyDTO();
            dto.setId(acbId);
            dtos.add(dto);
        }

        return dtos;
    }

    public List<DeveloperDTO> getAllDeveloperForUser(Long... developerIds) {
        List<DeveloperDTO> dtos = new ArrayList<DeveloperDTO>();

        for (Long acbId : developerIds) {
            DeveloperDTO dto = new DeveloperDTO();
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

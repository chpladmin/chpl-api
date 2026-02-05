package gov.healthit.chpl.permissions.domain;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.context.SecurityContextHolder;

import gov.healthit.chpl.TestingUsers;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.permissions.ResourcePermissions;

public abstract class ActionPermissionsBaseTest extends TestingUsers {
    private AutoCloseable closeable;

    @BeforeEach
    public void openMocks() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    public void releaseMocks() {
        if (closeable == null) {
            return;
        }

        try {
            closeable.close();
        } catch (Exception ex) {
            System.out.println("Error closing mocks");
            ex.printStackTrace();
        }
    }

    public abstract void hasAccess_Admin() throws Exception;

    public abstract void hasAccess_Onc() throws Exception;

    public abstract void hasAccess_Acb() throws Exception;

    public abstract void hasAccess_Cms() throws Exception;

    public abstract void hasAccess_Anon() throws Exception;

    public void hasAccess_Developer() throws Exception {
        // Do nothing - just Override if necessary
    }

    public List<CertificationBody> getAllAcbForUser(Long... acbIds) {
        List<CertificationBody> dtos = new ArrayList<CertificationBody>();

        for (Long acbId : acbIds) {
            CertificationBody dto = new CertificationBody();
            dto.setId(acbId);
            dtos.add(dto);
        }

        return dtos;
    }

    public List<Developer> getAllDeveloperForUser(Long... developerIds) {
        List<Developer> devs = new ArrayList<Developer>();

        for (Long devId : developerIds) {
            Developer dev = new Developer();
            dev.setId(devId);
            devs.add(dev);
        }
        return devs;
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
        Mockito.when(resourcePermissions.isUserRoleCmsStaff()).thenReturn(false);
    }
}

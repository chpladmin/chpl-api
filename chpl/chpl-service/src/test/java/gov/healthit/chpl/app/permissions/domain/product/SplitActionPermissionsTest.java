package gov.healthit.chpl.app.permissions.domain.product;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import gov.healthit.chpl.app.permissions.domain.ActionPermissionsBaseTest;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.DeveloperStatusDTO;
import gov.healthit.chpl.dto.DeveloperStatusEventDTO;
import gov.healthit.chpl.dto.ProductDTO;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.permissions.domains.product.SplitActionPermissions;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        gov.healthit.chpl.CHPLTestConfig.class
})
public class SplitActionPermissionsTest extends ActionPermissionsBaseTest {

    @Mock
    private ResourcePermissions resourcePermissions;

    @Mock
    private DeveloperDAO developerDAO;

    @Mock
    private CertifiedProductDAO certifiedProductDAO;

    @InjectMocks
    private SplitActionPermissions permissions;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        Mockito.when(resourcePermissions.getAllAcbsForCurrentUser()).thenReturn(getAllAcbForUser(2l, 4l));
    }

    @Override
    @Test
    public void hasAccess_Admin() throws Exception {
        setupForAdminUser(resourcePermissions);

        assertFalse(permissions.hasAccess());
        assertTrue(permissions.hasAccess(new ProductDTO()));
    }

    @Override
    @Test
    public void hasAccess_Onc() throws Exception {
        setupForOncUser(resourcePermissions);

        assertFalse(permissions.hasAccess());
        assertTrue(permissions.hasAccess(new ProductDTO()));
    }

    @Override
    @Test
    public void hasAccess_Acb() throws Exception {
        setupForAcbUser(resourcePermissions);

        assertFalse(permissions.hasAccess());

        ProductDTO dto = new ProductDTO();
        dto.setId(1l);
        dto.setDeveloperId(2l);

        // Non Active Developer
        Mockito.when(developerDAO.getById(ArgumentMatchers.anyLong())).thenReturn(getNonActiveDeveloper());
        assertFalse(permissions.hasAccess(dto));

        // User has access to associated certified products
        Mockito.when(developerDAO.getById(ArgumentMatchers.anyLong())).thenReturn(getActiveDeveloper());
        Mockito.when(certifiedProductDAO.findByDeveloperId(ArgumentMatchers.anyLong()))
                .thenReturn(getAcbMatchingCertifiedProducts());
        assertTrue(permissions.hasAccess(dto));

        // User does not have access to associated certified products
        Mockito.when(developerDAO.getById(ArgumentMatchers.anyLong())).thenReturn(getActiveDeveloper());
        Mockito.when(certifiedProductDAO.findByDeveloperId(ArgumentMatchers.anyLong()))
                .thenReturn(getAcbNotMatchingCertifiedProducts());
        assertFalse(permissions.hasAccess(dto));
    }

    @Override
    @Test
    public void hasAccess_Atl() throws Exception {
        setupForAtlUser(resourcePermissions);

        assertFalse(permissions.hasAccess());
        assertFalse(permissions.hasAccess(new Object()));
    }

    @Override
    @Test
    public void hasAccess_Cms() throws Exception {
        setupForCmsUser(resourcePermissions);

        assertFalse(permissions.hasAccess());
        assertFalse(permissions.hasAccess(new Object()));
    }

    @Override
    @Test
    public void hasAccess_Anon() throws Exception {
        setupForAnonUser(resourcePermissions);

        assertFalse(permissions.hasAccess());
        assertFalse(permissions.hasAccess(new Object()));
    }

    private DeveloperDTO getActiveDeveloper() {
        DeveloperDTO dto = new DeveloperDTO();
        dto.setId(1l);
        DeveloperStatusEventDTO statusEvent = new DeveloperStatusEventDTO();
        statusEvent.setDeveloperId(1l);
        DeveloperStatusDTO status = new DeveloperStatusDTO();
        status.setStatusName("Active");
        statusEvent.setStatus(status);
        dto.getStatusEvents().add(statusEvent);
        return dto;
    }

    private DeveloperDTO getNonActiveDeveloper() {
        DeveloperDTO dto = new DeveloperDTO();
        dto.setId(1l);
        DeveloperStatusEventDTO statusEvent = new DeveloperStatusEventDTO();
        statusEvent.setDeveloperId(1l);
        DeveloperStatusDTO status = new DeveloperStatusDTO();
        status.setStatusName("Suspended by ONC");
        statusEvent.setStatus(status);
        dto.getStatusEvents().add(statusEvent);
        return dto;
    }

    private List<CertifiedProductDetailsDTO> getAcbMatchingCertifiedProducts() {
        List<CertifiedProductDetailsDTO> cps = new ArrayList<CertifiedProductDetailsDTO>();
        CertifiedProductDetailsDTO cp = new CertifiedProductDetailsDTO();
        cp.setCertificationBodyId(2l);
        cps.add(cp);
        return cps;
    }

    private List<CertifiedProductDetailsDTO> getAcbNotMatchingCertifiedProducts() {
        List<CertifiedProductDetailsDTO> cps = new ArrayList<CertifiedProductDetailsDTO>();
        CertifiedProductDetailsDTO cp = new CertifiedProductDetailsDTO();
        cp.setCertificationBodyId(5l);
        cps.add(cp);
        return cps;
    }
}

package gov.healthit.chpl.permissions.domain.surveillance;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import gov.healthit.chpl.changerequest.dao.DeveloperCertificationBodyMapDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.permissions.ResourcePermissionsFactory;
import gov.healthit.chpl.permissions.domain.ActionPermissionsBaseTest;
import gov.healthit.chpl.permissions.domains.surveillance.CreateActionPermissions;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class CreateActionPermissionsTest extends ActionPermissionsBaseTest {
    @Mock
    private ResourcePermissions resourcePermissions;

    @Mock
    private DeveloperCertificationBodyMapDAO developerCertificationBodyMapDao;

    @Mock
    private CertifiedProductDAO certifiedProductDao;

    @Mock
    private ResourcePermissionsFactory resourcePermissionsFactory;

    @Mock
    private ErrorMessageUtil msgUtil;

    @InjectMocks
    private CreateActionPermissions permissions;

    @BeforeEach
    public void setup() {
        permissions = new CreateActionPermissions(resourcePermissionsFactory, certifiedProductDao, developerCertificationBodyMapDao, msgUtil);
        Mockito.when(resourcePermissionsFactory.get()).thenReturn(resourcePermissions);
        Mockito.when(resourcePermissions.getAllAcbsForCurrentUser()).thenReturn(getAllAcbForUser(2L, 4L));
        try {
            CertifiedProductDTO listingNoAccess = new CertifiedProductDTO();
            listingNoAccess.setCertificationBodyId(1L);
            Mockito.when(certifiedProductDao.getById(ArgumentMatchers.eq(1L))).thenReturn(listingNoAccess);
            CertifiedProductDTO listingWithAccess = new CertifiedProductDTO();
            listingWithAccess.setCertificationBodyId(2L);
            Mockito.when(certifiedProductDao.getById(ArgumentMatchers.eq(2L))).thenReturn(listingWithAccess);
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    @Override
    @Test
    public void hasAccess_Admin() throws Exception {
        setupForAdminUser(resourcePermissions);

        // This should always be false
        assertFalse(permissions.hasAccess());

        // Since it is admin it has access to all - param value does not matter.
        assertTrue(permissions.hasAccess(1L));
    }

    @Override
    @Test
    public void hasAccess_Onc() throws Exception {
        setupForOncUser(resourcePermissions);

        // This should always be false
        assertFalse(permissions.hasAccess());
        assertFalse(permissions.hasAccess(1L));
    }

    @Override
    @Test
    @Disabled
    public void hasAccess_Acb() throws Exception {
        setupForAcbUser(resourcePermissions);

        // This should always be false
        assertFalse(permissions.hasAccess());

        Mockito.when(certifiedProductDao.getById(ArgumentMatchers.anyLong())).thenReturn(getListing(1L));
        assertFalse(permissions.hasAccess(1L));

        Mockito.when(certifiedProductDao.getById(ArgumentMatchers.anyLong())).thenReturn(getListing(2L));
        assertTrue(permissions.hasAccess(1L));
    }

    @Override
    @Test
    public void hasAccess_Cms() throws Exception {
        setupForCmsUser(resourcePermissions);

        // This should always be false
        assertFalse(permissions.hasAccess());

        // Cms has no access - the param shouldn't even matter
        assertFalse(permissions.hasAccess(new Surveillance()));
    }

    @Override
    @Test
    public void hasAccess_Anon() throws Exception {
        setupForAnonUser(resourcePermissions);

        // This should always be false
        assertFalse(permissions.hasAccess());

        // Anon has no access - the param shouldn't even matter
        assertFalse(permissions.hasAccess(new Surveillance()));
    }

    private CertifiedProductDTO getListing(Long acbId) {
        CertifiedProductDTO dto = new CertifiedProductDTO();
        dto.setCertificationBodyId(acbId);
        return dto;
    }
}

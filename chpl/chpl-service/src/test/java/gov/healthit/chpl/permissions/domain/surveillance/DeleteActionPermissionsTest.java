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
import org.mockito.MockitoAnnotations;

import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.permissions.ResourcePermissionsFactory;
import gov.healthit.chpl.permissions.domain.ActionPermissionsBaseTest;
import gov.healthit.chpl.permissions.domains.surveillance.DeleteActionPermissions;

public class DeleteActionPermissionsTest extends ActionPermissionsBaseTest {
    @Mock
    private ResourcePermissions resourcePermissions;

    @Mock
    private CertifiedProductDAO certifiedProductDAO;

    @Mock
    private ResourcePermissionsFactory resourcePermissionsFactory;

    @InjectMocks
    private DeleteActionPermissions permissions;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        Mockito.when(resourcePermissionsFactory.get()).thenReturn(resourcePermissions);
        Mockito.when(resourcePermissions.getAllAcbsForCurrentUser()).thenReturn(getAllAcbForUser(2L, 4L));
        try {
            CertifiedProductDTO listingNoAccess = new CertifiedProductDTO();
            listingNoAccess.setCertificationBodyId(1L);
            Mockito.when(certifiedProductDAO.getById(ArgumentMatchers.eq(1L))).thenReturn(listingNoAccess);
            CertifiedProductDTO listingWithAccess = new CertifiedProductDTO();
            listingWithAccess.setCertificationBodyId(2L);
            Mockito.when(certifiedProductDAO.getById(ArgumentMatchers.eq(2L))).thenReturn(listingWithAccess);
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    @Override
    @Test
    @Disabled
    public void hasAccess_Admin() throws Exception {
        setupForAdminUser(resourcePermissions);

        // This should always be false
        assertFalse(permissions.hasAccess());

        // Since it is admin it has access to all - param value does not matter.
        assertTrue(permissions.hasAccess(1L));
    }

    @Override
    @Test
    @Disabled
    public void hasAccess_Onc() throws Exception {
        setupForOncUser(resourcePermissions);

        // This should always be false
        assertFalse(permissions.hasAccess());

        // Since it is onc it has access to all - param value does not matter.
        assertTrue(permissions.hasAccess(1L));
    }

    @Override
    @Test
    @Disabled
    public void hasAccess_Acb() throws Exception {
        setupForAcbUser(resourcePermissions);

        // This should always be false
        assertFalse(permissions.hasAccess());

        assertFalse(permissions.hasAccess(1L));

        assertTrue(permissions.hasAccess(2L));
    }

    @Override
    @Test
    public void hasAccess_Cms() throws Exception {
        setupForCmsUser(resourcePermissions);

        // This should always be false
        assertFalse(permissions.hasAccess());

        // Cms has no access - the param shouldn't even matter
        assertFalse(permissions.hasAccess(1L));
    }

    @Override
    @Test
    public void hasAccess_Anon() throws Exception {
        setupForAnonUser(resourcePermissions);

        // This should always be false
        assertFalse(permissions.hasAccess());

        // Anon has no access - the param shouldn't even matter
        assertFalse(permissions.hasAccess(1L));
    }
}

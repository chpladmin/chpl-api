package gov.healthit.chpl.permissions.domain.pendingcertifiedproduct;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import gov.healthit.chpl.domain.PendingCertifiedProductDetails;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.permissions.domain.ActionPermissionsBaseTest;
import gov.healthit.chpl.permissions.domains.pendingcertifiedproduct.GetDetailsByIdActionPermissions;

public class GetDetailsByIdActionPermissionsTest extends ActionPermissionsBaseTest {
    @Mock
    private ResourcePermissions resourcePermissions;

    @InjectMocks
    private GetDetailsByIdActionPermissions permissions;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        Mockito.when(resourcePermissions.getAllAcbsForCurrentUser()).thenReturn(getAllAcbForUser(2L, 4L));
    }

    @Override
    @Test
    public void hasAccess_Admin() throws Exception {
        setupForAdminUser(resourcePermissions);

        // Not used
        assertFalse(permissions.hasAccess());

        // ROLE_ADMIN can access any pending listing
        assertTrue(permissions.hasAccess(new PendingCertifiedProductDetails()));
    }

    @Override
    @Test
    public void hasAccess_Onc() throws Exception {
        setupForOncUser(resourcePermissions);

        // Not used
        assertFalse(permissions.hasAccess());

        // Only ROLE_ADMIN and ROLE_ACB has access
        assertFalse(permissions.hasAccess(new PendingCertifiedProductDetails()));
    }

    @Override
    @Test
    public void hasAccess_OncStaff() throws Exception {
        setupForOncStaffUser(resourcePermissions);

        assertFalse(permissions.hasAccess());
        assertFalse(permissions.hasAccess(new PendingCertifiedProductDetails()));
    }

    @Override
    @Test
    public void hasAccess_Acb() throws Exception {
        setupForAcbUser(resourcePermissions);

        // Not used
        assertFalse(permissions.hasAccess());

        // ROLE_ACB can only see pending listing on their own ACB
        PendingCertifiedProductDetails pendingListing = new PendingCertifiedProductDetails();
        pendingListing.getCertifyingBody().put("id", 1L);
        assertFalse(permissions.hasAccess(pendingListing));

        pendingListing = new PendingCertifiedProductDetails();
        pendingListing.getCertifyingBody().put("id", 2L);
        assertTrue(permissions.hasAccess(pendingListing));
    }

    @Override
    @Test
    public void hasAccess_Atl() throws Exception {
        setupForAtlUser(resourcePermissions);

        // Not used
        assertFalse(permissions.hasAccess());

        // Only ROLE_ADMIN and ROLE_ACB has access
        assertFalse(permissions.hasAccess(new Object()));
    }

    @Override
    @Test
    public void hasAccess_Cms() throws Exception {
        setupForCmsUser(resourcePermissions);

        // Not used
        assertFalse(permissions.hasAccess());

        // Only ROLE_ADMIN and ROLE_ACB has access
        assertFalse(permissions.hasAccess(new Object()));
    }

    @Override
    @Test
    public void hasAccess_Anon() throws Exception {
        setupForAnonUser(resourcePermissions);

        // Not used
        assertFalse(permissions.hasAccess());

        // Only ROLE_ADMIN and ROLE_ACB has access
        assertFalse(permissions.hasAccess(new Object()));
    }
}

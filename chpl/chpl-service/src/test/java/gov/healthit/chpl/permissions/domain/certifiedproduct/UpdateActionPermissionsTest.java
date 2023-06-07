package gov.healthit.chpl.permissions.domain.certifiedproduct;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.ListingUpdateRequest;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.permissions.domain.ActionPermissionsBaseTest;
import gov.healthit.chpl.permissions.domains.certifiedproduct.UpdateActionPermissions;

public class UpdateActionPermissionsTest extends ActionPermissionsBaseTest {

    @Mock
    private ResourcePermissions resourcePermissions;

    @InjectMocks
    private UpdateActionPermissions permissions;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        Mockito.when(resourcePermissions.getAllAcbsForCurrentUser()).thenReturn(getAllAcbForUser(2L, 4L));
    }

    @Override
    @Test
    @Ignore
    public void hasAccess_Admin() throws Exception {
        setupForAdminUser(resourcePermissions);

        // This should always be false
        assertFalse(permissions.hasAccess());

        // Since it is admin it has access to all - parm value does not matter.
        assertTrue(permissions.hasAccess(new ListingUpdateRequest()));
    }

    @Override
    @Test
    @Ignore
    public void hasAccess_Onc() throws Exception {
        setupForOncUser(resourcePermissions);

        // This should always be false
        assertFalse(permissions.hasAccess());

        // Since it is ONC it has access to all - parm value does not matter.
        assertTrue(permissions.hasAccess(new ListingUpdateRequest()));
    }

    @Override
    @Test
    @Ignore
    public void hasAccess_Acb() throws Exception {
        setupForAcbUser(resourcePermissions);

        // This should always be false
        assertFalse(permissions.hasAccess());

        ListingUpdateRequest request = new ListingUpdateRequest();
        request.setListing(new CertifiedProductSearchDetails());
        request.getListing().getCertifyingBody().put(CertifiedProductSearchDetails.ACB_ID_KEY, 1L);
        assertFalse(permissions.hasAccess(request));

        request = new ListingUpdateRequest();
        request.setListing(new CertifiedProductSearchDetails());
        request.getListing().getCertifyingBody().put(CertifiedProductSearchDetails.ACB_ID_KEY, 2L);
        assertTrue(permissions.hasAccess(request));
    }

    @Override
    @Test
    @Ignore
    public void hasAccess_Cms() throws Exception {
        setupForCmsUser(resourcePermissions);

        // This should always be false
        assertFalse(permissions.hasAccess());

        // Cms has no access - the param shouldn't even matter
        assertFalse(permissions.hasAccess(1L));
    }

    @Override
    @Test
    @Ignore
    public void hasAccess_Anon() throws Exception {
        setupForAnonUser(resourcePermissions);

        // This should always be false
        assertFalse(permissions.hasAccess());

        // Anon has no access - the param shouldn't even matter
        assertFalse(permissions.hasAccess(1L));
    }
}

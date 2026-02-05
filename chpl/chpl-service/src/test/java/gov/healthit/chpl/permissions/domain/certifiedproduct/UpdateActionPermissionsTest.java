package gov.healthit.chpl.permissions.domain.certifiedproduct;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import gov.healthit.chpl.changerequest.dao.DeveloperCertificationBodyMapDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.ListingUpdateRequest;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.permissions.ResourcePermissionsFactory;
import gov.healthit.chpl.permissions.domain.ActionPermissionsBaseTest;
import gov.healthit.chpl.permissions.domains.certifiedproduct.UpdateActionPermissions;

public class UpdateActionPermissionsTest extends ActionPermissionsBaseTest {

    @Mock
    private ResourcePermissions resourcePermissions;

    @Mock
    private DeveloperCertificationBodyMapDAO developerCertificationBodyMapDao;

    @Mock
    private CertifiedProductDAO certifiedProductDao;

    @Mock
    private ResourcePermissionsFactory resourcePermissionsFactory;

    @InjectMocks
    private UpdateActionPermissions permissions;

    @BeforeEach
    public void setup() {
        permissions = new UpdateActionPermissions(resourcePermissionsFactory, certifiedProductDao, developerCertificationBodyMapDao);
        Mockito.when(resourcePermissionsFactory.get()).thenReturn(resourcePermissions);
        Mockito.when(resourcePermissions.getAllAcbsForCurrentUser()).thenReturn(getAllAcbForUser(2L, 4L));
    }

    @Override
    @Test
    @Disabled
    public void hasAccess_Admin() throws Exception {
        setupForAdminUser(resourcePermissions);

        // This should always be false
        assertFalse(permissions.hasAccess());

        // Since it is admin it has access to all - parm value does not matter.
        assertTrue(permissions.hasAccess(new ListingUpdateRequest()));
    }

    @Override
    @Test
    @Disabled
    public void hasAccess_Onc() throws Exception {
        setupForOncUser(resourcePermissions);

        // This should always be false
        assertFalse(permissions.hasAccess());

        // Since it is ONC it has access to all - parm value does not matter.
        assertTrue(permissions.hasAccess(new ListingUpdateRequest()));
    }

    @Override
    @Test
    @Disabled
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
    @Disabled
    public void hasAccess_Cms() throws Exception {
        setupForCmsUser(resourcePermissions);

        // This should always be false
        assertFalse(permissions.hasAccess());

        // Cms has no access - the param shouldn't even matter
        assertFalse(permissions.hasAccess(1L));
    }

    @Override
    @Test
    @Disabled
    public void hasAccess_Anon() throws Exception {
        setupForAnonUser(resourcePermissions);

        // This should always be false
        assertFalse(permissions.hasAccess());

        // Anon has no access - the param shouldn't even matter
        assertFalse(permissions.hasAccess(1L));
    }
}

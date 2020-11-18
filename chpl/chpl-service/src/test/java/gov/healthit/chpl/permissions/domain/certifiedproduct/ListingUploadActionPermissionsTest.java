package gov.healthit.chpl.permissions.domain.certifiedproduct;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.ListingUpload;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.permissions.domain.ActionPermissionsBaseTest;
import gov.healthit.chpl.permissions.domains.listingUpload.CreateActionPermissions;

public class ListingUploadActionPermissionsTest extends ActionPermissionsBaseTest {
    private static final Long USER_ACB_ID = 2L;
    private static final Long OTHER_ACB_ID = 4L;

    @Mock
    private ResourcePermissions resourcePermissions;

    @InjectMocks
    private CreateActionPermissions permissions;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        Mockito.when(resourcePermissions.getAllAcbsForCurrentUser()).thenReturn(getAllAcbForUser(USER_ACB_ID));
    }

    @Override
    @Test
    public void hasAccess_Admin() throws Exception {
        setupForAdminUser(resourcePermissions);
        assertTrue(permissions.hasAccess());
        assertTrue(permissions.hasAccess(buildUploadMetadata(USER_ACB_ID)));
        assertTrue(permissions.hasAccess(buildUploadMetadata(OTHER_ACB_ID)));
    }

    @Override
    @Test
    public void hasAccess_Onc() throws Exception {
        setupForOncUser(resourcePermissions);
        assertTrue(permissions.hasAccess());
        assertTrue(permissions.hasAccess(buildUploadMetadata(USER_ACB_ID)));
        assertTrue(permissions.hasAccess(buildUploadMetadata(OTHER_ACB_ID)));
    }

    @Override
    @Test
    public void hasAccess_Acb() throws Exception {
        setupForAcbUser(resourcePermissions);
        assertTrue(permissions.hasAccess());
        assertTrue(permissions.hasAccess(buildUploadMetadata(USER_ACB_ID)));
        assertFalse(permissions.hasAccess(buildUploadMetadata(OTHER_ACB_ID)));
    }

    @Override
    @Test
    public void hasAccess_Atl() throws Exception {
        setupForAtlUser(resourcePermissions);
        assertFalse(permissions.hasAccess());
        assertFalse(permissions.hasAccess(buildUploadMetadata(USER_ACB_ID)));
        assertFalse(permissions.hasAccess(buildUploadMetadata(OTHER_ACB_ID)));
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
        assertFalse(permissions.hasAccess(buildUploadMetadata(USER_ACB_ID)));
        assertFalse(permissions.hasAccess(buildUploadMetadata(OTHER_ACB_ID)));
    }

    private ListingUpload buildUploadMetadata(Long acbId) {
        ListingUpload uploadMetadata = new ListingUpload();
        uploadMetadata.setChplProductNumber("12.45");
        CertificationBody acb = new CertificationBody();
        acb.setId(acbId);
        uploadMetadata.setAcb(acb);
        return uploadMetadata;
    }
}

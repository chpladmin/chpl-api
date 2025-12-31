package gov.healthit.chpl.permissions.domain.listingUpload;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import gov.healthit.chpl.changerequest.dao.DeveloperCertificationBodyMapDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.ListingUpload;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.permissions.ResourcePermissionsFactory;
import gov.healthit.chpl.permissions.domain.ActionPermissionsBaseTest;
import gov.healthit.chpl.permissions.domains.listingUpload.DeleteActionPermissions;
import gov.healthit.chpl.upload.listing.ListingUploadDao;

public class DeleteActionPermissionsTest extends ActionPermissionsBaseTest {
    @Mock
    private ResourcePermissions resourcePermissions;

    @Mock
    private DeveloperCertificationBodyMapDAO developerCertificationBodyMapDao;

    @Mock
    private CertifiedProductDAO certifiedProductDao;

    @Mock
    private ResourcePermissionsFactory resourcePermissionsFactory;

    @Mock
    private ListingUploadDao listingUploadDao;

    @InjectMocks
    private DeleteActionPermissions permissions;


    @BeforeEach
    public void setup() throws EntityRetrievalException {
        permissions = new DeleteActionPermissions(resourcePermissionsFactory, certifiedProductDao, developerCertificationBodyMapDao, listingUploadDao);
        Mockito.when(resourcePermissionsFactory.get()).thenReturn(resourcePermissions);
        Mockito.when(resourcePermissions.getAllAcbsForCurrentUser()).thenReturn(getAllAcbForUser(2L));
        Mockito.when(listingUploadDao.getById(ArgumentMatchers.anyLong()))
            .thenAnswer(i -> buildListingUpload(i.getArgument(0)));
    }

    @Override
    @Test
    public void hasAccess_Admin() throws Exception {
        setupForAdminUser(resourcePermissions);
        assertFalse(permissions.hasAccess());
        assertTrue(permissions.hasAccess(1L));
    }

    @Override
    @Test
    public void hasAccess_Onc() throws Exception {
        setupForOncUser(resourcePermissions);
        assertFalse(permissions.hasAccess());
        assertFalse(permissions.hasAccess(1L));
    }

    @Override
    @Test
    public void hasAccess_Acb() throws Exception {
        setupForAcbUser(resourcePermissions);
        assertFalse(permissions.hasAccess());
        assertFalse(permissions.hasAccess(1L));
        assertTrue(permissions.hasAccess(2L));
    }

    @Test
    public void invalidId_Acb_NoAccess() throws Exception {
        setupForAcbUser(resourcePermissions);
        Mockito.when(listingUploadDao.getById(ArgumentMatchers.anyLong()))
            .thenThrow(EntityRetrievalException.class);
        assertFalse(permissions.hasAccess(-1L));
    }

    @Override
    @Test
    public void hasAccess_Cms() throws Exception {
        setupForCmsUser(resourcePermissions);
        assertFalse(permissions.hasAccess());
        assertFalse(permissions.hasAccess(1L));
    }

    @Override
    @Test
    public void hasAccess_Anon() throws Exception {
        setupForAnonUser(resourcePermissions);
        assertFalse(permissions.hasAccess());
        assertFalse(permissions.hasAccess(1L));
    }

    private ListingUpload buildListingUpload(Long acbId) {
        return ListingUpload.builder()
            .acb(CertificationBody.builder()
                    .id(acbId)
                    .build())
            .certificationDate(LocalDate.now())
            .chplProductNumber("14.05.05.XXXX")
            .developer("Dev name")
            .product("Prod name")
            .version("Ver name")
            .build();
    }
}

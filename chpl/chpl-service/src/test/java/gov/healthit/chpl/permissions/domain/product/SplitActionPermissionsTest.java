package gov.healthit.chpl.permissions.domain.product;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.security.access.AccessDeniedException;

import gov.healthit.chpl.changerequest.dao.DeveloperCertificationBodyMapDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.Product;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.permissions.ResourcePermissionsFactory;
import gov.healthit.chpl.permissions.domain.ActionPermissionsBaseTest;
import gov.healthit.chpl.permissions.domains.product.SplitActionPermissions;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class SplitActionPermissionsTest extends ActionPermissionsBaseTest {
    @Mock
    private ErrorMessageUtil msgUtil;

    @Mock
    private ResourcePermissions resourcePermissions;

    @Mock
    private DeveloperCertificationBodyMapDAO developerCertificationBodyMapDao;

    @Mock
    private CertifiedProductDAO certifiedProductDao;

    @Mock
    private ResourcePermissionsFactory resourcePermissionsFactory;

    @InjectMocks
    private SplitActionPermissions permissions;

    @BeforeEach
    public void setup() {
        permissions = new SplitActionPermissions(resourcePermissionsFactory, certifiedProductDao, developerCertificationBodyMapDao, msgUtil);
        Mockito.when(resourcePermissionsFactory.get()).thenReturn(resourcePermissions);
        Mockito.when(resourcePermissions.getAllAcbsForCurrentUser()).thenReturn(getAllAcbForUser(2L, 4L));
        Mockito.when(msgUtil.getMessage(
                ArgumentMatchers.eq("product.split.notAllowedMultipleAcbs"),
                ArgumentMatchers.any())).thenReturn("AnyMessage1");
    }

    @Override
    @Test
    public void hasAccess_Admin() throws Exception {
        setupForAdminUser(resourcePermissions);

        assertFalse(permissions.hasAccess());
        assertTrue(permissions.hasAccess(new Product()));
    }

    @Override
    @Test
    public void hasAccess_Onc() throws Exception {
        setupForOncUser(resourcePermissions);

        assertFalse(permissions.hasAccess());
        assertTrue(permissions.hasAccess(new Product()));
    }

    @Override
    @Test
    public void hasAccess_Acb() throws Exception {
        setupForAcbUser(resourcePermissions);

        SplitActionPermissions spyPermissions = Mockito.spy(permissions);
        assertFalse(permissions.hasAccess());

        Product product = new Product();
        product.setId(1L);
        product.setOwner(Developer.builder()
                .id(2L)
                .build());

        // Non Active Developer
        Mockito.when(resourcePermissions.isDeveloperNotBannedOrSuspended(ArgumentMatchers.anyLong())).thenReturn(false);
        assertFalse(permissions.hasAccess(product));

        // User has access to associated certified products
        Mockito.when(resourcePermissions.isDeveloperNotBannedOrSuspended(ArgumentMatchers.anyLong())).thenReturn(true);
        Mockito.doReturn(true).when(spyPermissions)
                .doesCurrentUserHaveAccessToAllOfDevelopersListings(ArgumentMatchers.anyLong(),
                        ArgumentMatchers.any());
        assertTrue(spyPermissions.hasAccess(product));

        // User does not have access to associated certified products
        Mockito.when(resourcePermissions.isDeveloperNotBannedOrSuspended(ArgumentMatchers.anyLong())).thenReturn(true);
        Mockito.doReturn(false).when(spyPermissions)
                .doesCurrentUserHaveAccessToAllOfDevelopersListings(ArgumentMatchers.anyLong(), ArgumentMatchers.any());

        Exception exception = assertThrows(AccessDeniedException.class, () -> {
            spyPermissions.hasAccess(product);
        });
        assertNotNull(exception);
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

}

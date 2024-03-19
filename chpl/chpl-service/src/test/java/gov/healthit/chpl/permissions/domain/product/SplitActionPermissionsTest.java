package gov.healthit.chpl.permissions.domain.product;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.security.access.AccessDeniedException;

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
    private ResourcePermissionsFactory resourcePermissionsFacotry;

    @InjectMocks
    private SplitActionPermissions permissions;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        Mockito.when(resourcePermissionsFacotry.get()).thenReturn(resourcePermissions);
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
    @Test(expected = AccessDeniedException.class)
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
        Mockito.when(resourcePermissions.isDeveloperActive(ArgumentMatchers.anyLong())).thenReturn(false);
        assertFalse(permissions.hasAccess(product));

        // User has access to associated certified products
        Mockito.when(resourcePermissions.isDeveloperActive(ArgumentMatchers.anyLong())).thenReturn(true);
        Mockito.doReturn(true).when(spyPermissions)
                .doesCurrentUserHaveAccessToAllOfDevelopersListings(ArgumentMatchers.anyLong(),
                        ArgumentMatchers.any());
        assertTrue(spyPermissions.hasAccess(product));

        // User does not have access to associated certified products
        Mockito.when(resourcePermissions.isDeveloperActive(ArgumentMatchers.anyLong())).thenReturn(true);
        Mockito.doReturn(false).when(spyPermissions)
                .doesCurrentUserHaveAccessToAllOfDevelopersListings(ArgumentMatchers.anyLong(), ArgumentMatchers.any());
        spyPermissions.hasAccess(product);
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

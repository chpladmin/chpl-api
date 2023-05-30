package gov.healthit.chpl.permissions.domain.surveillance;

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

import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.permissions.domain.ActionPermissionsBaseTest;
import gov.healthit.chpl.permissions.domains.surveillance.UpdateActionPermissions;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class UpdateActionPermissionsTest extends ActionPermissionsBaseTest {
    @Mock
    private ResourcePermissions resourcePermissions;

    @Mock
    private ErrorMessageUtil errorMessageUtil;

    @Mock
    private CertifiedProductDAO cpDAO;

    @InjectMocks
    private UpdateActionPermissions permissions;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        Mockito.when(resourcePermissions.getAllAcbsForCurrentUser()).thenReturn(getAllAcbForUser(2L, 4L));
    }

    @Override
    @Test
    public void hasAccess_Admin() throws Exception {
        setupForAdminUser(resourcePermissions);

        // This should always be false
        assertFalse(permissions.hasAccess());

        // Since it is admin it has access to all - param value does not matter.
        Surveillance surv = new Surveillance();
        assertTrue(permissions.hasAccess(surv));
    }

    @Override
    @Test
    public void hasAccess_Onc() throws Exception {
        setupForOncUser(resourcePermissions);

        // This should always be false
        assertFalse(permissions.hasAccess());

        // Since it is onc it has access to all - param value does not matter.
        Surveillance surv = new Surveillance();
        assertFalse(permissions.hasAccess(surv));
    }

    @Override
    @Test
    public void hasAccess_Acb() throws Exception {
        setupForAcbUser(resourcePermissions);

        // This should always be false
        assertFalse(permissions.hasAccess());

        Surveillance surv = new Surveillance();
        surv.setCertifiedProduct(new CertifiedProduct());
        surv.getCertifiedProduct().setId(1L);

        Mockito.when(cpDAO.getById(ArgumentMatchers.anyLong()))
                .thenReturn(getCertifiedProductWithAcbAndEdition(1L, 3L));
        assertFalse(permissions.hasAccess(surv));

        Mockito.when(cpDAO.getById(ArgumentMatchers.anyLong()))
                .thenReturn(getCertifiedProductWithAcbAndEdition(2L, 3L));
        assertTrue(permissions.hasAccess(surv));
    }

    @Test(expected = AccessDeniedException.class)
    public void hasAccess_Acb_2014Listing() throws Exception {
        setupForAcbUser(resourcePermissions);

        // This should always be false
        assertFalse(permissions.hasAccess());

        Surveillance surv = new Surveillance();
        surv.setCertifiedProduct(new CertifiedProduct());
        surv.getCertifiedProduct().setId(1L);

        Mockito.when(cpDAO.getById(ArgumentMatchers.anyLong()))
                .thenReturn(getCertifiedProductWithAcbAndEdition(3L, 2L));

        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.anyString()))
                .thenReturn("message");

        permissions.hasAccess(surv);
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

    private CertifiedProductDTO getCertifiedProductWithAcbAndEdition(Long acbId, Long editionId) {
        return CertifiedProductDTO.builder()
                .certificationBodyId(acbId)
                .certificationEditionId(editionId)
                .build();
    }
}

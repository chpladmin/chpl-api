package gov.healthit.chpl.permissions.domain.surveillance;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.ff4j.FF4j;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.permissions.domain.ActionPermissionsBaseTest;
import gov.healthit.chpl.permissions.domains.surveillance.UpdateActionPermissions;

public class UpdateActionPermissionsTest extends ActionPermissionsBaseTest {
    @Mock
    private ResourcePermissions resourcePermissions;

    @Mock
    private CertifiedProductDAO cpDAO;

    @Autowired
    private FF4j ff4j;

    @InjectMocks
    private UpdateActionPermissions permissions;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        Mockito.when(resourcePermissions.getAllAcbsForCurrentUser()).thenReturn(getAllAcbForUser(2l, 4l));
        Mockito.doReturn(false).when(ff4j).check(FeatureList.EFFECTIVE_RULE_DATE_PLUS_ONE_WEEK);
    }

    @Override
    @Test
    @Ignore
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
    @Ignore
    public void hasAccess_Onc() throws Exception {
        setupForOncUser(resourcePermissions);

        // This should always be false
        assertFalse(permissions.hasAccess());

        // Since it is onc it has access to all - param value does not matter.
        Surveillance surv = new Surveillance();
        assertTrue(permissions.hasAccess(surv));
    }

    @Override
    @Test
    @Ignore
    public void hasAccess_Acb() throws Exception {
        setupForAcbUser(resourcePermissions);

        // This should always be false
        assertFalse(permissions.hasAccess());

        Surveillance surv = new Surveillance();
        surv.setCertifiedProduct(new CertifiedProduct());
        surv.getCertifiedProduct().setId(1l);

        Mockito.when(cpDAO.getById(ArgumentMatchers.anyLong()))
                .thenReturn(getCertifiedProduct(1l));
        assertFalse(permissions.hasAccess(surv));

        Mockito.when(cpDAO.getById(ArgumentMatchers.anyLong()))
                .thenReturn(getCertifiedProduct(2l));

        assertTrue(permissions.hasAccess(surv));
    }

    @Override
    @Test
    @Ignore
    public void hasAccess_Atl() throws Exception {
        setupForAtlUser(resourcePermissions);

        // This should always be false
        assertFalse(permissions.hasAccess());

        // Atl has no access - the param shouldn't even matter
        assertFalse(permissions.hasAccess(new Surveillance()));
    }

    @Override
    @Test
    @Ignore
    public void hasAccess_Cms() throws Exception {
        setupForCmsUser(resourcePermissions);

        // This should always be false
        assertFalse(permissions.hasAccess());

        // Cms has no access - the param shouldn't even matter
        assertFalse(permissions.hasAccess(new Surveillance()));
    }

    @Override
    @Test
    @Ignore
    public void hasAccess_Anon() throws Exception {
        setupForAnonUser(resourcePermissions);

        // This should always be false
        assertFalse(permissions.hasAccess());

        // Anon has no access - the param shouldn't even matter
        assertFalse(permissions.hasAccess(new Surveillance()));
    }

    private CertifiedProductDTO getCertifiedProduct(Long acbId) {
        CertifiedProductDTO dto = new CertifiedProductDTO();
        dto.setCertificationBodyId(acbId);
        return dto;
    }
}

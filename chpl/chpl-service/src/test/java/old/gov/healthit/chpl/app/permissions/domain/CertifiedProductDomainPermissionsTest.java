package old.gov.healthit.chpl.app.permissions.domain;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import gov.healthit.chpl.permissions.domains.CertifiedProductDomainPermissions;
import gov.healthit.chpl.permissions.domains.certifiedproduct.CleanDataActionPermissions;
import gov.healthit.chpl.permissions.domains.certifiedproduct.CreateFromPendingActionPermissions;
import gov.healthit.chpl.permissions.domains.certifiedproduct.UpdateActionPermissions;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        old.gov.healthit.chpl.CHPLTestConfig.class
})
public class CertifiedProductDomainPermissionsTest {
    @Autowired
    private CertifiedProductDomainPermissions permissions;

    @Test
    public void setupTest() {
        assertTrue(permissions.getActionPermissions().size() == 3);

        assertTrue(permissions.getActionPermissions()
                .get(CertifiedProductDomainPermissions.CLEAN_DATA) instanceof CleanDataActionPermissions);

        assertTrue(permissions.getActionPermissions().get(
                CertifiedProductDomainPermissions.CREATE_FROM_PENDING) instanceof CreateFromPendingActionPermissions);

        assertTrue(permissions.getActionPermissions()
                .get(CertifiedProductDomainPermissions.UPDATE) instanceof UpdateActionPermissions);
    }
}

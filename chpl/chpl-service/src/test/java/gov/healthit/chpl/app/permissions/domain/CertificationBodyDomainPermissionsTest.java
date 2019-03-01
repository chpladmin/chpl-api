package gov.healthit.chpl.app.permissions.domain;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import gov.healthit.chpl.permissions.domains.CertificationBodyDomainPermissions;
import gov.healthit.chpl.permissions.domains.certificationbody.CreateActionPermissions;
import gov.healthit.chpl.permissions.domains.certificationbody.GetByIdActionPermissions;
import gov.healthit.chpl.permissions.domains.certificationbody.RetireActionPermissions;
import gov.healthit.chpl.permissions.domains.certificationbody.UnretireActionPermissions;
import gov.healthit.chpl.permissions.domains.certificationbody.UpdateActionPermissions;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        gov.healthit.chpl.CHPLTestConfig.class
})
public class CertificationBodyDomainPermissionsTest {
    @Autowired
    private CertificationBodyDomainPermissions permissions;

    @Test
    public void setupTest() {
        assertTrue(permissions.getActionPermissions().size() == 5);

        assertTrue(permissions.getActionPermissions()
                .get(CertificationBodyDomainPermissions.CREATE) instanceof CreateActionPermissions);

        assertTrue(permissions.getActionPermissions()
                .get(CertificationBodyDomainPermissions.GET_BY_ID) instanceof GetByIdActionPermissions);

        assertTrue(permissions.getActionPermissions()
                .get(CertificationBodyDomainPermissions.RETIRE) instanceof RetireActionPermissions);

        assertTrue(permissions.getActionPermissions()
                .get(CertificationBodyDomainPermissions.UNRETIRE) instanceof UnretireActionPermissions);

        assertTrue(permissions.getActionPermissions()
                .get(CertificationBodyDomainPermissions.UPDATE) instanceof UpdateActionPermissions);
    }
}

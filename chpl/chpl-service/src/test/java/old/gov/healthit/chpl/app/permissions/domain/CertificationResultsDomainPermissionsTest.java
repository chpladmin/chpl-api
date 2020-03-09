package old.gov.healthit.chpl.app.permissions.domain;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import gov.healthit.chpl.permissions.domains.CertificationResultsDomainPermissions;
import gov.healthit.chpl.permissions.domains.certificationresults.UpdatePermissions;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        old.gov.healthit.chpl.CHPLTestConfig.class
})
public class CertificationResultsDomainPermissionsTest {

    @Autowired
    private CertificationResultsDomainPermissions permissions;

    @Test
    public void setupTest() {
        assertTrue(permissions.getActionPermissions().size() == 1);
        assertTrue(permissions.getActionPermissions()
                .get(CertificationResultsDomainPermissions.UPDATE) instanceof UpdatePermissions);
    }
}

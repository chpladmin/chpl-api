package old.gov.healthit.chpl.app.permissions.domain;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import gov.healthit.chpl.permissions.domains.TestingLabDomainPermissions;
import gov.healthit.chpl.permissions.domains.testinglab.CreateActionPermissions;
import gov.healthit.chpl.permissions.domains.testinglab.GetAllActionPermissions;
import gov.healthit.chpl.permissions.domains.testinglab.RetireActionPermissions;
import gov.healthit.chpl.permissions.domains.testinglab.UnretireActionPermissions;
import gov.healthit.chpl.permissions.domains.testinglab.UpdateActionPermissions;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        old.gov.healthit.chpl.CHPLTestConfig.class
})
public class TestingLabDomainPermissionsTest {
    @Autowired
    private TestingLabDomainPermissions permissions;

    @Test
    public void setupTest() {
        assertTrue(permissions.getActionPermissions().size() == 5);

        assertTrue(permissions.getActionPermissions()
                .get(TestingLabDomainPermissions.CREATE) instanceof CreateActionPermissions);

        assertTrue(permissions.getActionPermissions()
                .get(TestingLabDomainPermissions.UPDATE) instanceof UpdateActionPermissions);

        assertTrue(permissions.getActionPermissions()
                .get(TestingLabDomainPermissions.RETIRE) instanceof RetireActionPermissions);

        assertTrue(permissions.getActionPermissions()
                .get(TestingLabDomainPermissions.UNRETIRE) instanceof UnretireActionPermissions);

        assertTrue(permissions.getActionPermissions()
                .get(TestingLabDomainPermissions.GET_ALL) instanceof GetAllActionPermissions);
    }
}

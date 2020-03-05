package old.gov.healthit.chpl.app.permissions.domain;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import gov.healthit.chpl.permissions.domains.ComplaintDomainPermissions;
import gov.healthit.chpl.permissions.domains.complaint.CreateActionPermissions;
import gov.healthit.chpl.permissions.domains.complaint.DeleteActionPermissions;
import gov.healthit.chpl.permissions.domains.complaint.GetAllActionPermissions;
import gov.healthit.chpl.permissions.domains.complaint.UpdateActionPermissions;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        old.gov.healthit.chpl.CHPLTestConfig.class
})
public class ComplaintDomainPermissionsTest {
    @Autowired
    private ComplaintDomainPermissions permissions;

    @Test
    public void setupTest() {
        assertTrue(permissions.getActionPermissions().size() == 4);

        assertTrue(permissions.getActionPermissions()
                .get(ComplaintDomainPermissions.CREATE) instanceof CreateActionPermissions);

        assertTrue(permissions.getActionPermissions()
                .get(ComplaintDomainPermissions.DELETE) instanceof DeleteActionPermissions);

        assertTrue(permissions.getActionPermissions()
                .get(ComplaintDomainPermissions.UPDATE) instanceof UpdateActionPermissions);

        assertTrue(permissions.getActionPermissions()
                .get(ComplaintDomainPermissions.GET_ALL) instanceof GetAllActionPermissions);
    }
}

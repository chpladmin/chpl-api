package gov.healthit.chpl.app.permissions.domain;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import gov.healthit.chpl.permissions.domains.UserPermissionsDomainPermissions;
import gov.healthit.chpl.permissions.domains.userpermissions.AddAcbActionPermissions;
import gov.healthit.chpl.permissions.domains.userpermissions.DeleteAcbActionPermissions;
import gov.healthit.chpl.permissions.domains.userpermissions.DeleteAllAcbPermissionsForUserActionPermissions;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        gov.healthit.chpl.CHPLTestConfig.class
})
public class UserPermissionsDomainPermissionsTest {
    @Autowired
    private UserPermissionsDomainPermissions permissions;

    @Test
    public void setupTest() {
        assertTrue(permissions.getActionPermissions().size() == 3);

        assertTrue(permissions.getActionPermissions()
                .get(UserPermissionsDomainPermissions.ADD_ACB) instanceof AddAcbActionPermissions);

        assertTrue(permissions.getActionPermissions()
                .get(UserPermissionsDomainPermissions.DELETE_ACB) instanceof DeleteAcbActionPermissions);

        assertTrue(permissions.getActionPermissions().get(
                UserPermissionsDomainPermissions.DELETE_ALL_ACBS_FOR_USER) instanceof DeleteAllAcbPermissionsForUserActionPermissions);
    }
}

package old.gov.healthit.chpl.app.permissions.domain;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import gov.healthit.chpl.permissions.domains.UserPermissionsDomainPermissions;
import gov.healthit.chpl.permissions.domains.secureduser.ImpersonateUserActionPermissions;
import gov.healthit.chpl.permissions.domains.userpermissions.AddAcbActionPermissions;
import gov.healthit.chpl.permissions.domains.userpermissions.AddAtlActionPermissions;
import gov.healthit.chpl.permissions.domains.userpermissions.AddDeveloperActionPermissions;
import gov.healthit.chpl.permissions.domains.userpermissions.DeleteAcbActionPermissions;
import gov.healthit.chpl.permissions.domains.userpermissions.DeleteAtlActionPermissions;
import gov.healthit.chpl.permissions.domains.userpermissions.DeleteDeveloperActionPermissions;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        old.gov.healthit.chpl.CHPLTestConfig.class
})
public class UserPermissionsDomainPermissionsTest {
    @Autowired
    private UserPermissionsDomainPermissions permissions;

    @Test
    public void setupTest() {
        assertTrue(permissions.getActionPermissions().size() == 7);

        assertTrue(permissions.getActionPermissions()
                .get(UserPermissionsDomainPermissions.ADD_ACB) instanceof AddAcbActionPermissions);

        assertTrue(permissions.getActionPermissions()
                .get(UserPermissionsDomainPermissions.DELETE_ACB) instanceof DeleteAcbActionPermissions);

        assertTrue(permissions.getActionPermissions()
                .get(UserPermissionsDomainPermissions.ADD_ATL) instanceof AddAtlActionPermissions);

        assertTrue(permissions.getActionPermissions()
                .get(UserPermissionsDomainPermissions.DELETE_ATL) instanceof DeleteAtlActionPermissions);

        assertTrue(permissions.getActionPermissions()
                .get(UserPermissionsDomainPermissions.ADD_DEVELOPER) instanceof AddDeveloperActionPermissions);

        assertTrue(permissions.getActionPermissions()
                .get(UserPermissionsDomainPermissions.DELETE_DEVELOPER) instanceof DeleteDeveloperActionPermissions);

        assertTrue(permissions.getActionPermissions()
                .get(UserPermissionsDomainPermissions.IMPERSONATE_USER) instanceof ImpersonateUserActionPermissions);
    }
}

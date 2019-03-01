package gov.healthit.chpl.app.permissions.domain;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import gov.healthit.chpl.permissions.domains.InvitationDomainPermissions;
import gov.healthit.chpl.permissions.domains.invitation.InviteAcbActionPermissions;
import gov.healthit.chpl.permissions.domains.invitation.InviteAcbAtlActionPermissions;
import gov.healthit.chpl.permissions.domains.invitation.InviteAdminActionPermissions;
import gov.healthit.chpl.permissions.domains.invitation.InviteOncActionPermissions;
import gov.healthit.chpl.permissions.domains.invitation.InviteRoleNoAccessActionPermissions;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        gov.healthit.chpl.CHPLTestConfig.class
})
public class InvitationDomainPermissionsTest {
    @Autowired
    private InvitationDomainPermissions permissions;

    @Test
    public void setupTest() {
        assertTrue(permissions.getActionPermissions().size() == 5);

        assertTrue(permissions.getActionPermissions()
                .get(InvitationDomainPermissions.INVITE_ACB) instanceof InviteAcbActionPermissions);

        assertTrue(permissions.getActionPermissions()
                .get(InvitationDomainPermissions.INVITE_ACB_ATL) instanceof InviteAcbAtlActionPermissions);

        assertTrue(permissions.getActionPermissions()
                .get(InvitationDomainPermissions.INVITE_ADMIN) instanceof InviteAdminActionPermissions);

        assertTrue(permissions.getActionPermissions()
                .get(InvitationDomainPermissions.INVITE_ONC) instanceof InviteOncActionPermissions);

        assertTrue(permissions.getActionPermissions()
                .get(InvitationDomainPermissions.INVITE_ROLE_NO_ACCESS) instanceof InviteRoleNoAccessActionPermissions);

    }
}

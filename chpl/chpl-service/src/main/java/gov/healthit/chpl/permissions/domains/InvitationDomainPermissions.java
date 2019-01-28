package gov.healthit.chpl.permissions.domains;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.invitation.InviteAcbActionPermissions;
import gov.healthit.chpl.permissions.domains.invitation.InviteAcbAtlActionPermissions;
import gov.healthit.chpl.permissions.domains.invitation.InviteAdminActionPermissions;
import gov.healthit.chpl.permissions.domains.invitation.InviteOncActionPermissions;
import gov.healthit.chpl.permissions.domains.invitation.InviteRoleNoAccessActionPermissions;

@Component
public class InvitationDomainPermissions extends DomainPermissions {
    public static final String INVITE_ADMIN = "INVITE_ADMIN";
    public static final String INVITE_ONC = "INVITE_ONC";
    public static final String INVITE_ROLE_NO_ACCESS = "INVITE_ROLE_NO_ACCESS";
    public static final String INVITE_ACB = "INVITE_ACB";
    public static final String INVITE_ACB_ATL = "INVITE_ACB_ATL";

    public InvitationDomainPermissions(
            @Qualifier("invitationInviteAdminActionPermissions") InviteAdminActionPermissions inviteAdminActionPermissions,
            @Qualifier("invitationInviteOncActionPermissions") InviteOncActionPermissions inviteOncActionPermissions,
            @Qualifier("invitationInviteRoleNoAccessActionPermissions") InviteRoleNoAccessActionPermissions inviteRoleNoAccessActionPermissions,
            @Qualifier("invitationInviteAcbActionPermissions") InviteAcbActionPermissions inviteAcbActionPermissions,
            @Qualifier("invitationInviteAcbAtlActionPermissions") InviteAcbAtlActionPermissions inviteAcbAtlActionPermissions) {

        getActionPermissions().put(INVITE_ADMIN, inviteAdminActionPermissions);
        getActionPermissions().put(INVITE_ONC, inviteOncActionPermissions);
        getActionPermissions().put(INVITE_ROLE_NO_ACCESS, inviteRoleNoAccessActionPermissions);
        getActionPermissions().put(INVITE_ACB, inviteAcbActionPermissions);
        getActionPermissions().put(INVITE_ACB_ATL, inviteAcbAtlActionPermissions);
    }
}

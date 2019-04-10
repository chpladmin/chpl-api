package gov.healthit.chpl.permissions.domains;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.invitation.InviteAcbActionPermissions;
import gov.healthit.chpl.permissions.domains.invitation.InviteAdminActionPermissions;
import gov.healthit.chpl.permissions.domains.invitation.InviteAtlActionPermissions;
import gov.healthit.chpl.permissions.domains.invitation.InviteCmsActionPermissions;
import gov.healthit.chpl.permissions.domains.invitation.InviteOncActionPermissions;

@Component
public class InvitationDomainPermissions extends DomainPermissions {
    public static final String INVITE_ADMIN = "INVITE_ADMIN";
    public static final String INVITE_ONC = "INVITE_ONC";
    public static final String INVITE_ACB = "INVITE_ACB";
    public static final String INVITE_ATL = "INVITE_ATL";
    public static final String INVITE_CMS = "INVITE_CMS";

    @Autowired
    public InvitationDomainPermissions(
            @Qualifier("invitationInviteAdminActionPermissions") InviteAdminActionPermissions inviteAdminActionPermissions,
            @Qualifier("invitationInviteOncActionPermissions") InviteOncActionPermissions inviteOncActionPermissions,
            @Qualifier("invitationInviteCmsActionPermissions") InviteCmsActionPermissions inviteCmsActionPermissions,
            @Qualifier("invitationInviteAcbActionPermissions") InviteAcbActionPermissions inviteAcbActionPermissions,
            @Qualifier("invitationInviteAtlActionPermissions") InviteAtlActionPermissions inviteAtlActionPermissions) {

        getActionPermissions().put(INVITE_ADMIN, inviteAdminActionPermissions);
        getActionPermissions().put(INVITE_ONC, inviteOncActionPermissions);
        getActionPermissions().put(INVITE_CMS, inviteCmsActionPermissions);
        getActionPermissions().put(INVITE_ACB, inviteAcbActionPermissions);
        getActionPermissions().put(INVITE_ATL, inviteAtlActionPermissions);
    }
}

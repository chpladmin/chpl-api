package gov.healthit.chpl.permissions.domains;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.invitation.InviteAcbActionPermissions;
import gov.healthit.chpl.permissions.domains.invitation.InviteAdminActionPermissions;
import gov.healthit.chpl.permissions.domains.invitation.InviteAtlActionPermissions;
import gov.healthit.chpl.permissions.domains.invitation.InviteCmsActionPermissions;
import gov.healthit.chpl.permissions.domains.invitation.InviteDeveloperActionPermissions;
import gov.healthit.chpl.permissions.domains.invitation.InviteOncActionPermissions;
import gov.healthit.chpl.permissions.domains.invitation.UpdateFromInvitationActionPermissions;

@Component
public class InvitationDomainPermissions extends DomainPermissions {
    public static final String INVITE_ADMIN = "INVITE_ADMIN";
    public static final String INVITE_ONC = "INVITE_ONC";
    public static final String INVITE_ACB = "INVITE_ACB";
    public static final String INVITE_ATL = "INVITE_ATL";
    public static final String INVITE_CMS = "INVITE_CMS";
    public static final String INVITE_DEVELOPER = "INVITE_DEVELOPER";
    public static final String UPDATE_FROM_INVITATION = "UPDATE_FROM_INVITATION";

    @Autowired
    public InvitationDomainPermissions(
            @Qualifier("invitationInviteAdminActionPermissions") final InviteAdminActionPermissions inviteAdminActionPermissions,
            @Qualifier("invitationInviteOncActionPermissions") final InviteOncActionPermissions inviteOncActionPermissions,
            @Qualifier("invitationInviteCmsActionPermissions") final InviteCmsActionPermissions inviteCmsActionPermissions,
            @Qualifier("invitationInviteAcbActionPermissions") final InviteAcbActionPermissions inviteAcbActionPermissions,
            @Qualifier("invitationInviteAtlActionPermissions") final InviteAtlActionPermissions inviteAtlActionPermissions,
            @Qualifier("invitationInviteDeveloperActionPermissions") final InviteDeveloperActionPermissions inviteDeveloperActionPermissions,
            @Qualifier("invitationUpdateFromInvitationActionPermissions")
                final UpdateFromInvitationActionPermissions updateFromInvitationActionPermissions) {

        getActionPermissions().put(INVITE_ADMIN, inviteAdminActionPermissions);
        getActionPermissions().put(INVITE_ONC, inviteOncActionPermissions);
        getActionPermissions().put(INVITE_CMS, inviteCmsActionPermissions);
        getActionPermissions().put(INVITE_ACB, inviteAcbActionPermissions);
        getActionPermissions().put(INVITE_ATL, inviteAtlActionPermissions);
        getActionPermissions().put(INVITE_DEVELOPER, inviteDeveloperActionPermissions);
        getActionPermissions().put(UPDATE_FROM_INVITATION, updateFromInvitationActionPermissions);
    }
}

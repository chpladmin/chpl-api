package gov.healthit.chpl.auth.user;

import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.domain.CreateUserFromInvitationRequest;
import gov.healthit.chpl.exception.EmailNotSentException;
import gov.healthit.chpl.exception.UserCreationException;
import gov.healthit.chpl.exception.UserPermissionRetrievalException;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.auth.CognitoUserService;
import gov.healthit.chpl.service.InvitationEmailer;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class CognitoUserManager {

    private CognitoUserInvitationDAO userInvitationDAO;
    private CognitoUserCreationValidator userCreationValidator;
    private InvitationEmailer invitationEmailer;
    private CognitoConfirmEmailEmailer cognitoConfirmEmailEmailer;
    private CognitoUserService cognitoUserService;


    @Autowired
    public CognitoUserManager(CognitoUserInvitationDAO userInvitationDAO, CognitoUserCreationValidator userCreationValidator,
            InvitationEmailer invitationEmailer, CognitoConfirmEmailEmailer cognitoConfirmEmailEmailer, CognitoUserService cognitoUserService) {

        this.userInvitationDAO = userInvitationDAO;
        this.userCreationValidator = userCreationValidator;
        this.invitationEmailer = invitationEmailer;
        this.cognitoConfirmEmailEmailer = cognitoConfirmEmailEmailer;
        this.cognitoUserService = cognitoUserService;
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).INVITATION, "
            + "T(gov.healthit.chpl.permissions.domains.InvitationDomainPermissions).INVITE_ADMIN)")
    public CognitoUserInvitation inviteAdmin(String email)
            throws UserCreationException, UserRetrievalException, UserPermissionRetrievalException {
        return createUserInvitation(email);
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SECURED_USER, "
            + "T(gov.healthit.chpl.permissions.domains.SecuredUserDomainPermissions).CREATE)")
    public Boolean createUser(CreateUserFromInvitationRequest userInfo)
            throws ValidationException, UserCreationException, EmailNotSentException {

        Set<String> errors = userCreationValidator.validate(userInfo);
        if (errors.size() > 0) {
            throw new ValidationException(errors, null);
        }

        // Need to be able to rollback this whole thing if there is an error...
        CognitoCredentials credentials = null;
        try {
            credentials = cognitoUserService.createUser(userInfo.getUser());
            cognitoUserService.addUserToAdminGroup(userInfo.getUser().getEmail());
            userInvitationDAO.deleteByToken(UUID.fromString(userInfo.getHash()));
            cognitoConfirmEmailEmailer.sendConfirmationEmail(credentials);
        } catch (EmailNotSentException e) {
            //Invitation deletion should roll back due to @Transactional
            if (credentials != null) {
                cognitoUserService.deleteUser(credentials.getCognitoId());
            }
            throw e;
        }

        return true;
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).INVITATION, "
            + "T(gov.healthit.chpl.permissions.domains.InvitationDomainPermissions).GET)")
    public CognitoUserInvitation getInvitation(UUID token) {
        return userInvitationDAO.getByToken(token);
    }

    private CognitoUserInvitation createUserInvitation(String email) {
        CognitoUserInvitation invitation = userInvitationDAO.create(CognitoUserInvitation.builder()
                .email(email)
                .invitationToken(UUID.randomUUID())
                .build());

        invitationEmailer.emailInvitedUser(invitation);
        return invitation;
    }

}

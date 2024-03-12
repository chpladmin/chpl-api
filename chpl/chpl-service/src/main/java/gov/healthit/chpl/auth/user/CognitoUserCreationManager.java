package gov.healthit.chpl.auth.user;

import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.domain.CreateUserFromInvitationRequest;
import gov.healthit.chpl.domain.auth.LoginCredentials;
import gov.healthit.chpl.domain.auth.User;
import gov.healthit.chpl.exception.UserCreationException;
import gov.healthit.chpl.exception.UserPermissionRetrievalException;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.auth.CognitoUserService;
import gov.healthit.chpl.service.InvitationEmailer;
import gov.healthit.chpl.util.ErrorMessageUtil;
import lombok.extern.log4j.Log4j2;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminAddUserToGroupResponse;

@Log4j2
@Component
public class CognitoUserCreationManager {

    private CognitoUserInvitationDAO userInvitationDAO;
    private CognitoUserCreationValidator userCreationValidator;
    private InvitationEmailer invitationEmailer;
    private CognitoConfirmEmailEmailer cognitoConfirmEmailEmailer;
    private CognitoUserService cognitoUserService;
    private ErrorMessageUtil msgUtil;


    @Autowired
    public CognitoUserCreationManager(CognitoUserInvitationDAO userInvitationDAO, CognitoUserCreationValidator userCreationValidator,
            InvitationEmailer invitationEmailer, CognitoConfirmEmailEmailer cognitoConfirmEmailEmailer, CognitoUserService cognitoUserService,
            ErrorMessageUtil msgUtil) {

        this.userInvitationDAO = userInvitationDAO;
        this.userCreationValidator = userCreationValidator;
        this.invitationEmailer = invitationEmailer;
        this.cognitoConfirmEmailEmailer = cognitoConfirmEmailEmailer;
        this.cognitoUserService = cognitoUserService;
        this.msgUtil = msgUtil;

    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).INVITATION, "
            + "T(gov.healthit.chpl.permissions.domains.InvitationDomainPermissions).INVITE_ADMIN)")
    public CognitoUserInvitation inviteAdmin(String email)
            throws UserCreationException, UserRetrievalException, UserPermissionRetrievalException {
        return createUserInvitation(email);
    }

    //TODO Need to add security
    @Transactional
    public User createUser(CreateUserFromInvitationRequest userInfo) throws ValidationException {
        Set<String> errors = userCreationValidator.validate(userInfo);
        if (errors.size() > 0) {
            throw new ValidationException(errors, null);
        }

        LoginCredentials credentials = cognitoUserService.createUser(userInfo.getUser());

        AdminAddUserToGroupResponse adminAddUserToGroupResponse = cognitoUserService.addUserToAdminGroup(userInfo.getUser().getEmail());

        LOGGER.info("AdminAddUserToGroupResponse: {}", adminAddUserToGroupResponse.toString());

        cognitoConfirmEmailEmailer.sendConfirmationEmail(credentials);

        userInvitationDAO.deleteByToken(UUID.fromString(userInfo.getHash()));

        return null;
    }


    //TODO Does this make sense?  Can it be left w/o security??
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

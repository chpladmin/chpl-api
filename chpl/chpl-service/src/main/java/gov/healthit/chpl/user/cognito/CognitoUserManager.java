package gov.healthit.chpl.user.cognito;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.domain.CreateUserFromInvitationRequest;
import gov.healthit.chpl.domain.auth.User;
import gov.healthit.chpl.exception.EmailNotSentException;
import gov.healthit.chpl.exception.UserCreationException;
import gov.healthit.chpl.exception.UserPermissionRetrievalException;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.service.InvitationEmailer;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class CognitoUserManager {

    private CognitoUserInvitationDAO userInvitationDAO;
    private CognitoUserCreationValidator userCreationValidator;
    private InvitationEmailer invitationEmailer;
    private CognitoConfirmEmailEmailer cognitoConfirmEmailEmailer;
    private CognitoApiWrapper cognitoApiWrapper;


    @Autowired
    public CognitoUserManager(CognitoUserInvitationDAO userInvitationDAO, CognitoUserCreationValidator userCreationValidator,
            InvitationEmailer invitationEmailer, CognitoConfirmEmailEmailer cognitoConfirmEmailEmailer, CognitoApiWrapper cognitoApiWrapper) {

        this.userInvitationDAO = userInvitationDAO;
        this.userCreationValidator = userCreationValidator;
        this.invitationEmailer = invitationEmailer;
        this.cognitoConfirmEmailEmailer = cognitoConfirmEmailEmailer;
        this.cognitoApiWrapper = cognitoApiWrapper;
    }

    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SECURED_USER, "
            + "T(gov.healthit.chpl.permissions.domains.SecuredUserDomainPermissions).GET_BY_USER_NAME)")
    @PostAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SECURED_USER, "
            + "T(gov.healthit.chpl.permissions.domains.SecuredUserDomainPermissions).GET_BY_USER_NAME, returnObject)")
    public User getUserInfo(UUID cognitoId) throws UserRetrievalException {
        return cognitoApiWrapper.getUserInfo(cognitoId);
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).INVITATION, "
            + "T(gov.healthit.chpl.permissions.domains.InvitationDomainPermissions).INVITE_ADMIN)")
    public CognitoUserInvitation inviteAdminUser(CognitoUserInvitation invitation)
            throws UserCreationException, UserRetrievalException, UserPermissionRetrievalException, ValidationException {

        validateUserInvitation(invitation, List.of(
                CognitoInvitationValidator.InvitationValidationRules.EmailRequired,
                CognitoInvitationValidator.InvitationValidationRules.EmailValid,
                CognitoInvitationValidator.InvitationValidationRules.GroupNameRequired,
                CognitoInvitationValidator.InvitationValidationRules.GroupNameValid));

        return createUserInvitation(invitation);
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).INVITATION, "
            + "T(gov.healthit.chpl.permissions.domains.InvitationDomainPermissions).INVITE_ONC)")
    public CognitoUserInvitation inviteOncUser(CognitoUserInvitation invitation)
            throws UserCreationException, UserRetrievalException, UserPermissionRetrievalException, ValidationException {

        validateUserInvitation(invitation, List.of(
                CognitoInvitationValidator.InvitationValidationRules.EmailRequired,
                CognitoInvitationValidator.InvitationValidationRules.EmailValid,
                CognitoInvitationValidator.InvitationValidationRules.GroupNameRequired,
                CognitoInvitationValidator.InvitationValidationRules.GroupNameValid));

        return createUserInvitation(invitation);
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).INVITATION, "
            + "T(gov.healthit.chpl.permissions.domains.InvitationDomainPermissions).INVITE_ACB, #invitation.organizationId)")
    public CognitoUserInvitation inviteOncAcbUser(CognitoUserInvitation invitation)
            throws UserCreationException, UserRetrievalException, UserPermissionRetrievalException, ValidationException {

        validateUserInvitation(invitation, List.of(
                CognitoInvitationValidator.InvitationValidationRules.EmailRequired,
                CognitoInvitationValidator.InvitationValidationRules.EmailValid,
                CognitoInvitationValidator.InvitationValidationRules.GroupNameRequired,
                CognitoInvitationValidator.InvitationValidationRules.GroupNameValid,
                CognitoInvitationValidator.InvitationValidationRules.OrganizationRequired));

        return createUserInvitation(invitation);
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).INVITATION, "
            + "T(gov.healthit.chpl.permissions.domains.InvitationDomainPermissions).INVITE_DEVELOPER, #invitation.organizationId)")
    public CognitoUserInvitation inviteDeveloperUser(CognitoUserInvitation invitation)
            throws UserCreationException, UserRetrievalException, UserPermissionRetrievalException, ValidationException {

        validateUserInvitation(invitation, List.of(
                CognitoInvitationValidator.InvitationValidationRules.EmailRequired,
                CognitoInvitationValidator.InvitationValidationRules.EmailValid,
                CognitoInvitationValidator.InvitationValidationRules.GroupNameRequired,
                CognitoInvitationValidator.InvitationValidationRules.GroupNameValid,
                CognitoInvitationValidator.InvitationValidationRules.OrganizationRequired));

        return createUserInvitation(invitation);
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
            credentials = cognitoApiWrapper.createUser(userInfo.getUser());
            cognitoApiWrapper.addUserToAdminGroup(userInfo.getUser().getEmail());
            userInvitationDAO.deleteByToken(UUID.fromString(userInfo.getHash()));
            cognitoConfirmEmailEmailer.sendConfirmationEmail(credentials);
        } catch (EmailNotSentException e) {
            //Invitation deletion should roll back due to @Transactional
            if (credentials != null) {
                cognitoApiWrapper.deleteUser(credentials.getCognitoId());
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

    private CognitoUserInvitation createUserInvitation(CognitoUserInvitation origInvitation) {
        origInvitation.setInvitationToken(UUID.randomUUID());

        CognitoUserInvitation invitation = userInvitationDAO.create(origInvitation);

        invitationEmailer.emailInvitedUser(invitation);
        return invitation;
    }

    private void validateUserInvitation(CognitoUserInvitation invitation, List<CognitoInvitationValidator.InvitationValidationRules> rules)
            throws ValidationException {

        List<String> errors = CognitoInvitationValidator.validate(invitation, rules);
        if (CollectionUtils.isNotEmpty(errors)) {
            throw new ValidationException(errors);
        }
    }
}

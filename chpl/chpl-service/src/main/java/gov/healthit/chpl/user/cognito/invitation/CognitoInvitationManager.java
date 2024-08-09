package gov.healthit.chpl.user.cognito.invitation;

import java.util.List;
import java.util.UUID;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.exception.UserCreationException;
import gov.healthit.chpl.exception.UserPermissionRetrievalException;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.service.InvitationEmailer;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class CognitoInvitationManager {

    private CognitoUserInvitationDAO userInvitationDAO;
    private InvitationEmailer invitationEmailer;
    private CognitoInvitationValidator cognitoInvitationValidator;

    @Autowired
    public CognitoInvitationManager(CognitoUserInvitationDAO userInvitationDAO, InvitationEmailer invitationEmailer,
            CognitoInvitationValidator cognitoInvitationValidator) {

        this.userInvitationDAO = userInvitationDAO;
        this.invitationEmailer = invitationEmailer;
        this.cognitoInvitationValidator = cognitoInvitationValidator;
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
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).INVITATION, "
            + "T(gov.healthit.chpl.permissions.domains.InvitationDomainPermissions).INVITE_CMS)")
    public CognitoUserInvitation inviteCmsUser(CognitoUserInvitation invitation)
            throws UserCreationException, UserRetrievalException, UserPermissionRetrievalException, ValidationException {

        validateUserInvitation(invitation, List.of(
                CognitoInvitationValidator.InvitationValidationRules.EmailRequired,
                CognitoInvitationValidator.InvitationValidationRules.EmailValid,
                CognitoInvitationValidator.InvitationValidationRules.GroupNameRequired,
                CognitoInvitationValidator.InvitationValidationRules.GroupNameValid));

        return createUserInvitation(invitation);
    }

    public void deleteToken(UUID invitationToken) {
        userInvitationDAO.deleteByToken(invitationToken);
    }

    public CognitoUserInvitation getByToken(UUID invitationToken) {
        return userInvitationDAO.getByToken(invitationToken);
    }

    private CognitoUserInvitation createUserInvitation(CognitoUserInvitation origInvitation) {
        origInvitation.setInvitationToken(UUID.randomUUID());

        CognitoUserInvitation invitation = userInvitationDAO.create(origInvitation);

        invitationEmailer.emailInvitedUser(invitation);
        return invitation;
    }

    private void validateUserInvitation(CognitoUserInvitation invitation, List<CognitoInvitationValidator.InvitationValidationRules> rules)
            throws ValidationException {

        List<String> errors = cognitoInvitationValidator.validate(invitation, rules);
        if (CollectionUtils.isNotEmpty(errors)) {
            throw new ValidationException(errors);
        }
    }

}

package gov.healthit.chpl.user.cognito;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.domain.CreateUserFromInvitationRequest;
import gov.healthit.chpl.domain.auth.User;
import gov.healthit.chpl.exception.EmailNotSentException;
import gov.healthit.chpl.exception.UserCreationException;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.user.cognito.invitation.CognitoInvitationManager;
import gov.healthit.chpl.user.cognito.invitation.CognitoUserInvitation;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class CognitoUserManager {

    private CognitoUserCreationValidator userCreationValidator;
    private CognitoConfirmEmailEmailer cognitoConfirmEmailEmailer;
    private CognitoApiWrapper cognitoApiWrapper;
    private CognitoInvitationManager cognitoInvitationManager;
    private String groupNameForEnvironment;


    @Autowired
    public CognitoUserManager(CognitoUserCreationValidator userCreationValidator, CognitoConfirmEmailEmailer cognitoConfirmEmailEmailer,
            CognitoApiWrapper cognitoApiWrapper, CognitoInvitationManager cognitoInvitationManager,
            @Value("${cognito.environment.groupName}") String groupNameForEnvironment) {

        this.userCreationValidator = userCreationValidator;
        this.cognitoConfirmEmailEmailer = cognitoConfirmEmailEmailer;
        this.cognitoApiWrapper = cognitoApiWrapper;
        this.cognitoInvitationManager = cognitoInvitationManager;
        this.groupNameForEnvironment = groupNameForEnvironment;
    }

    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SECURED_USER, "
            + "T(gov.healthit.chpl.permissions.domains.SecuredUserDomainPermissions).GET_BY_USER_NAME)")
    @PostAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SECURED_USER, "
            + "T(gov.healthit.chpl.permissions.domains.SecuredUserDomainPermissions).GET_BY_USER_NAME, returnObject)")
    public User getUserInfo(UUID cognitoId) throws UserRetrievalException {
        return cognitoApiWrapper.getUserInfo(cognitoId);
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
            CognitoUserInvitation invitation = cognitoInvitationManager.getByToken(UUID.fromString(userInfo.getHash()));
            if (invitation.getOrganizationId() != null) {
                userInfo.getUser().setOrganizationId(invitation.getOrganizationId());
            }
            credentials = cognitoApiWrapper.createUser(userInfo.getUser());
            cognitoApiWrapper.addUserToGroup(userInfo.getUser().getEmail(), invitation.getGroupName());
            cognitoApiWrapper.addUserToGroup(userInfo.getUser().getEmail(), groupNameForEnvironment);
            cognitoInvitationManager.deleteToken(UUID.fromString(userInfo.getHash()));
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
    @PostFilter("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SECURED_USER, "
            + "T(gov.healthit.chpl.permissions.domains.SecuredUserDomainPermissions).GET_ALL, filterObject)")
    public List<User> getAll() {
        return cognitoApiWrapper.getAllUsers();
    }
}

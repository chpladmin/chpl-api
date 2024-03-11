package gov.healthit.chpl.auth.user;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CreateUserFromInvitationRequest;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component
public class CognitoUserCreationValidator {

    private CognitoUserInvitationDAO userInvitationDAO;
    private ErrorMessageUtil msgUtil;
    private long invitationLengthInDays;


    @Autowired
    public CognitoUserCreationValidator(CognitoUserInvitationDAO userInvitationDAO, ErrorMessageUtil msgUtil, @Value("${invitationLengthInDays}") Long invitationLengthDays) {
        this.userInvitationDAO = userInvitationDAO;
        this.msgUtil = msgUtil;
        this.invitationLengthInDays = invitationLengthDays;
    }

    public Set<String> validate(CreateUserFromInvitationRequest userInfo) {
        Set<String> messages = new HashSet<String>();


        if (isInvitationExpired(UUID.fromString(userInfo.getHash()))) {
            messages.add(msgUtil.getMessage("user.invitation.expired",
                    invitationLengthInDays + "",
                    invitationLengthInDays == 1 ? "" : "s"));
        }


        if (userInfo.getUser() == null || userInfo.getUser().getEmail() == null) {
            messages.add(msgUtil.getMessage("user.email.required"));
            return messages;
        }

        if (doesUserExistInCognito(userInfo.getUser().getEmail())) {
            messages.add(msgUtil.getMessage("user.accountAlreadyExists", userInfo.getUser().getEmail()));
        }

        Set<String> errors = validateCreateUserFromInvitationRequest(userInfo);
        if (errors.size() > 0) {
            messages.addAll(errors);
        }

        return messages;
    }

    private Boolean isInvitationExpired(UUID token) {
        CognitoUserInvitation invitation = userInvitationDAO.getByToken(token);
        return invitation == null || invitation.isOlderThan(invitationLengthInDays);
    }

    private Set<String> validateCreateUserFromInvitationRequest(CreateUserFromInvitationRequest request) {
        Set<String> validationErrors = new HashSet<String>();

        if (request.getUser().getFullName().length() > msgUtil.getMessageAsInteger("maxLength.fullName")) {
            validationErrors.add(msgUtil.getMessage("user.fullName.maxlength",
                    msgUtil.getMessageAsInteger("maxLength.fullName")));
        }
        if (!StringUtils.isEmpty(request.getUser().getFriendlyName())
                && request.getUser().getFriendlyName().length() > msgUtil.getMessageAsInteger("maxLength.friendlyName")) {
            validationErrors.add(msgUtil.getMessage("user.friendlyName.maxlength",
                    msgUtil.getMessageAsInteger("maxLength.friendlyName")));
        }
        if (!StringUtils.isEmpty(request.getUser().getTitle())
                && request.getUser().getTitle().length() > msgUtil.getMessageAsInteger("maxLength.title")) {
            validationErrors.add(msgUtil.getMessage("user.title.maxlength",
                    msgUtil.getMessageAsInteger("maxLength.title")));
        }
        if (request.getUser().getEmail().length() > msgUtil.getMessageAsInteger("maxLength.email")) {
            validationErrors.add(msgUtil.getMessage("user.email.maxlength",
                    msgUtil.getMessageAsInteger("maxLength.email")));
        }
        if (!StringUtils.isEmpty(request.getUser().getPhoneNumber())
                && request.getUser().getPhoneNumber().length() > msgUtil.getMessageAsInteger("maxLength.phoneNumber")) {
            validationErrors.add(msgUtil.getMessage("user.phoneNumber.maxlength",
                    msgUtil.getMessageAsInteger("maxLength.phoneNumber")));
        }
        return validationErrors;
    }

    //TODO Check if user with this email already exists in Cognito
    private Boolean doesUserExistInCognito(String email) {
        return false;
    }

}

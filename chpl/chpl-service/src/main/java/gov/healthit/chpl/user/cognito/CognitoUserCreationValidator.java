package gov.healthit.chpl.user.cognito;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CreateUserFromInvitationRequest;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.user.cognito.invitation.CognitoUserInvitation;
import gov.healthit.chpl.user.cognito.invitation.CognitoUserInvitationDAO;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class CognitoUserCreationValidator {

    private CognitoUserInvitationDAO userInvitationDAO;
    private CognitoApiWrapper cognitoApiWrapper;
    private ErrorMessageUtil msgUtil;
    private long invitationLengthInDays;


    @Autowired
    public CognitoUserCreationValidator(CognitoUserInvitationDAO userInvitationDAO, CognitoApiWrapper cognitoApiWrapper,
            ErrorMessageUtil msgUtil, @Value("${invitationLengthInDays}") Long invitationLengthDays) {
        this.userInvitationDAO = userInvitationDAO;
        this.cognitoApiWrapper = cognitoApiWrapper;
        this.msgUtil = msgUtil;
        this.invitationLengthInDays = invitationLengthDays;
    }

    public Set<String> validate(CreateUserFromInvitationRequest userInfo) {
        normalizeSpacesInUserInfo(userInfo);
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

        try {
            if (doesUserExistInCognito(userInfo.getUser().getEmail())) {
                messages.add(msgUtil.getMessage("user.accountAlreadyExists", userInfo.getUser().getEmail()));
            }
        } catch (UserRetrievalException e) {
            LOGGER.error("Could not validate if the user exists in store.", e);
        }
        Set<String> errors = validateCreateUserFromInvitationRequest(userInfo);
        if (errors.size() > 0) {
            messages.addAll(errors);
        }

        return messages;
    }

    private void normalizeSpacesInUserInfo(CreateUserFromInvitationRequest userInfo) {
        userInfo.getUser().setEmail(StringUtils.normalizeSpace(userInfo.getUser().getEmail()));
        userInfo.getUser().setFriendlyName(StringUtils.normalizeSpace(userInfo.getUser().getFriendlyName()));
        userInfo.getUser().setFullName(StringUtils.normalizeSpace(userInfo.getUser().getFullName()));
        userInfo.getUser().setPhoneNumber(StringUtils.normalizeSpace(userInfo.getUser().getPhoneNumber()));
        userInfo.getUser().setTitle(StringUtils.normalizeSpace(userInfo.getUser().getTitle()));
    }

    private Boolean isInvitationExpired(UUID token) {
        CognitoUserInvitation invitation = userInvitationDAO.getByToken(token);
        return invitation == null || invitation.isOlderThan(invitationLengthInDays);
    }

    private Set<String> validateCreateUserFromInvitationRequest(CreateUserFromInvitationRequest request) {
        Set<String> validationErrors = new HashSet<String>();

        if (StringUtils.isEmpty(request.getUser().getFullName())) {
            validationErrors.add(msgUtil.getMessage("cognito.user.create.fullName.empty"));
        }

        if (StringUtils.isEmpty(request.getUser().getPhoneNumber())) {
            validationErrors.add(msgUtil.getMessage("cognito.user.create.phoneNumber.empty"));
        } else if (!isPhoneNumberValid(request.getUser().getPhoneNumber())) {
            validationErrors.add(msgUtil.getMessage("cognito.user.create.phoneNumber.invalid"));
        }

        if (StringUtils.isEmpty(request.getUser().getEmail())) {
            validationErrors.add(msgUtil.getMessage("cognito.user.create.email.empty"));
        } else if (!Util.isEmailAddressValidFormat(request.getUser().getEmail())) {
            validationErrors.add(msgUtil.getMessage("cognito.user.create.email.invalid"));
        }

        return validationErrors;
    }

    private Boolean doesUserExistInCognito(String email) throws UserRetrievalException {
        return cognitoApiWrapper.getUserInfo(email) != null;
    }

    private Boolean isPhoneNumberValid(String phoneNumber) {
        return phoneNumber.matches("\\(?\\d{3}\\)?-? *\\d{3}-? *-?\\d{4}");
    }
}
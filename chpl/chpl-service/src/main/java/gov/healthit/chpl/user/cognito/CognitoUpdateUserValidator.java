package gov.healthit.chpl.user.cognito;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.auth.User;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class CognitoUpdateUserValidator {

    private CognitoApiWrapper cognitoApiWrapper;
    private ErrorMessageUtil msgUtil;


    @Autowired
    public CognitoUpdateUserValidator(CognitoApiWrapper cognitoApiWrapper, ErrorMessageUtil msgUtil) {
        this.cognitoApiWrapper = cognitoApiWrapper;
        this.msgUtil = msgUtil;
    }

    public Set<String> validate(User user) {
        Set<String> messages = new HashSet<String>();

        Set<String> errors = validateUser(user);
        if (errors.size() > 0) {
            messages.addAll(errors);
        }

        return messages;
    }

    private Set<String> validateUser(User user) {
        Set<String> validationErrors = new HashSet<String>();

        if (!doesUserExistInCognito(user.getCognitoId())) {
            validationErrors.add(msgUtil.getMessage("user.notFound"));
        }

        if (StringUtils.isEmpty(user.getFullName())) {
            validationErrors.add(msgUtil.getMessage("cognito.user.create.fullName.empty"));
        }

        if (StringUtils.isEmpty(user.getPhoneNumber())) {
            validationErrors.add(msgUtil.getMessage("cognito.user.create.phoneNumber.empty"));
        } else if (!isPhoneNumberValid(user.getPhoneNumber())) {
            validationErrors.add(msgUtil.getMessage("cognito.user.create.phoneNumber.invalid"));
        }

        if (StringUtils.isEmpty(user.getEmail())) {
            validationErrors.add(msgUtil.getMessage("cognito.user.create.email.empty"));
        } else if (!Util.isEmailAddressValidFormat(user.getEmail())) {
            validationErrors.add(msgUtil.getMessage("cognito.user.create.email.invalid"));
        }

        return validationErrors;
    }

    private Boolean doesUserExistInCognito(UUID cognitoId) {
        try {
            return cognitoApiWrapper.getUserInfo(cognitoId) != null;
        } catch (UserRetrievalException e) {
            return false;
        }
    }

    private Boolean isPhoneNumberValid(String phoneNumber) {
        return phoneNumber.matches("\\(?\\d{3}\\)?-? *\\d{3}-? *-?\\d{4}");
    }

}

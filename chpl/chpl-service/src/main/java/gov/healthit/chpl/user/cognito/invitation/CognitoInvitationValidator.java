package gov.healthit.chpl.user.cognito.invitation;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.auth.CognitoGroups;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;

@Component
public final class CognitoInvitationValidator {
    enum InvitationValidationRules {
        EmailRequired,
        EmailValid,
        GroupNameRequired,
        GroupNameValid,
        OrganizationRequired,
    }

    private ErrorMessageUtil errorMessageUtil;

    public CognitoInvitationValidator(ErrorMessageUtil errorMessageUtil) {
        this.errorMessageUtil = errorMessageUtil;
    }

    public List<String> validate(CognitoUserInvitation invitation, List<InvitationValidationRules> rules) {
        List<String> errorMessages = new ArrayList<String>();
        if (rules.contains(InvitationValidationRules.EmailRequired)
                && isEmailEmpty(invitation.getEmail())) {
            errorMessages.add(errorMessageUtil.getMessage("user.invitation.emailRequired"));
        }

        if (rules.contains(InvitationValidationRules.EmailValid)
                && !isEmailValid(invitation.getEmail())) {
            errorMessages.add(errorMessageUtil.getMessage("user.invitation.emailNotValid", invitation.getEmail()));
        }

        if (rules.contains(InvitationValidationRules.GroupNameRequired)
                && isGroupNameEmpty(invitation.getGroupName())) {
            errorMessages.add(errorMessageUtil.getMessage("user.invitation.groupNameRequired"));
        }

        if (rules.contains(InvitationValidationRules.GroupNameValid)
                && !isGroupNameValid(invitation.getGroupName())) {
            errorMessages.add(errorMessageUtil.getMessage("user.invitation.groupNameNotValid", invitation.getGroupName()));
        }

        if (rules.contains(InvitationValidationRules.OrganizationRequired)
                && isOrganizationIdNull(invitation.getOrganizationId())) {
            errorMessages.add(errorMessageUtil.getMessage("user.invitation.organizationIdRequired"));
        }

        return errorMessages;
    }

    private static Boolean isEmailEmpty(String email) {
        return StringUtils.isEmpty(email);
    }

    private static Boolean isEmailValid(String email) {
        return Util.isEmailAddressValidFormat(email);
    }

    private static Boolean isGroupNameValid(String groupName) {
        return Boolean.valueOf(CognitoGroups.getAll().contains(groupName));
    }

    private static Boolean isGroupNameEmpty(String groupName) {
        return StringUtils.isEmpty(groupName);
    }

    private static Boolean isOrganizationIdNull(Long organizationId) {
        return organizationId == null;
    }

}

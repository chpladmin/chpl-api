package gov.healthit.chpl.user.cognito;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import gov.healthit.chpl.domain.auth.CognitoGroups;
import gov.healthit.chpl.util.Util;

public final class CognitoInvitationValidator {
    enum InvitationValidationRules {
        EmailRequired,
        EmailValid,
        GroupNameRequired,
        GroupNameValid,
        OrganizationRequired,
    }

    private CognitoInvitationValidator() { }

    public static List<String> validate(CognitoUserInvitation invitation, List<InvitationValidationRules> rules) {
        List<String> errorMessages = new ArrayList<String>();
        if (rules.contains(InvitationValidationRules.EmailRequired)
                && isEmailEmpty(invitation.getEmail())) {
            errorMessages.add("Email is reuired to create invitation");
        }

        if (rules.contains(InvitationValidationRules.EmailValid)
                && !isEmailValid(invitation.getEmail())) {
            errorMessages.add(String.format("'%s' is not a valid email address", invitation.getEmail()));
        }

        if (rules.contains(InvitationValidationRules.GroupNameRequired)
                && isGroupNameEmpty(invitation.getGroupName())) {
            errorMessages.add("Group Name is reuired to create invitation");
        }

        if (rules.contains(InvitationValidationRules.GroupNameValid)
                && !isGroupNameValid(invitation.getGroupName())) {
            errorMessages.add(String.format("'%s' is not a valid Group Name", invitation.getGroupName()));
        }

        if (rules.contains(InvitationValidationRules.OrganizationRequired)
                && isOrganizationIdNull(invitation.getOrganizationId())) {
            errorMessages.add("Organization Id is reuired to create invitation");
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

package gov.healthit.chpl.changerequest.validation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.rules.ValidationRule;

public class CurrentStatusValidation extends ValidationRule<ChangeRequestValidationContext> {

    @Override
    public boolean isValid(ChangeRequestValidationContext context) {
        // It's fine if it's not set... We aren't going to do anything with it.
        if (!doesCurrentStatusExist(context.getNewChangeRequest())) {
            return true;
        }

        // Make sure the current status type is is valid
        // Does it exist in the DB?
        try {
            context.getValidationDAOs().getChangeRequestStatusTypeDAO().getChangeRequestStatusTypeById(
                    context.getNewChangeRequest().getCurrentStatus().getChangeRequestStatusType().getId());
        } catch (EntityRetrievalException e) {
            getMessages().add(getErrorMessage("changeRequest.statusType.notExists"));
            return false;
        }


        Long statusTypeId = context.getNewChangeRequest().getCurrentStatus().getChangeRequestStatusType().getId();
        // Is this a valid status change based on the user's role?
        if (context.getResourcePermissions().isUserRoleDeveloperAdmin()
                && !getValidStatusesForDeveloper(context).contains(statusTypeId)) {

            getMessages().add(getErrorMessage("changeRequest.statusType.invalid"));
            return false;
        } else if ((context.getResourcePermissions().isUserRoleAcbAdmin()
                || context.getResourcePermissions().isUserRoleOnc()
                || context.getResourcePermissions().isUserRoleAdmin())
                && !getValidStatusesForChangeRequestAdmin(context).contains(statusTypeId)) {

            getMessages().add(getErrorMessage("changeRequest.statusType.invalid"));
            return false;
        }
        return true;
    }

    private List<Long> getValidStatusesForDeveloper(ChangeRequestValidationContext context) {
        return new ArrayList<Long>(Arrays.asList(
                context.getChangeRequestStatusIds().getCancelledStatus(),
                context.getChangeRequestStatusIds().getPendingAcbActionStatus()));
    }

    private List<Long> getValidStatusesForChangeRequestAdmin(ChangeRequestValidationContext context) {
        return new ArrayList<Long>(Arrays.asList(
                context.getChangeRequestStatusIds().getAcceptedStatus(),
                context.getChangeRequestStatusIds().getRejectedStatus(),
                context.getChangeRequestStatusIds().getPendingDeveloperActionStatus()));
    }

    private Boolean doesCurrentStatusExist(ChangeRequest cr) {
        return cr.getCurrentStatus() != null
                && cr.getCurrentStatus().getChangeRequestStatusType() != null
                && cr.getCurrentStatus().getChangeRequestStatusType().getId() != null;
    }
}

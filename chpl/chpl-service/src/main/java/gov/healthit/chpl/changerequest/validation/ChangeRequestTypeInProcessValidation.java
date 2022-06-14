package gov.healthit.chpl.changerequest.validation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.rules.ValidationRule;

public class ChangeRequestTypeInProcessValidation extends ValidationRule<ChangeRequestValidationContext> {

    @Override
    public boolean isValid(ChangeRequestValidationContext context) {
        try {
            List<ChangeRequest> crs = context.getValidationDAOs().getChangeRequestDAO().getByDeveloper(context.getNewChangeRequest().getDeveloper().getId()).stream()
                    .filter(cr -> cr.getChangeRequestType().getId().equals(context.getNewChangeRequest().getChangeRequestType().getId()))
                    .filter(cr -> getInProcessStatuses(context).stream()
                            .anyMatch(status -> cr.getCurrentStatus().getChangeRequestStatusType().getId()
                                    .equals(status)))
                    .collect(Collectors.toList());
            if (crs.size() > 0) {
                getMessages().add(getErrorMessage("changeRequest.inProcess"));
                return false;
            }
        } catch (EntityRetrievalException e) {
            // Not sure what happened here, but we'll assume that another
            // validator catches it
            return true;
        }
        return true;
    }

    private List<Long> getInProcessStatuses(ChangeRequestValidationContext context) {
        return new ArrayList<Long>(Arrays.asList(
                context.getChangeRequestStatusIds().getPendingAcbActionStatus(),
                context.getChangeRequestStatusIds().getPendingDeveloperActionStatus()));
    }
}

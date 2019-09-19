package gov.healthit.chpl.changerequest.validation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.rules.ValidationRule;

@Component
public class ChangeRequestTypeInProcessValidation extends ValidationRule<ChangeRequestValidationContext> {

    @Value("${changerequest.status.pendingacbaction}")
    private Long pendingAcbAction;

    @Value("${changerequest.status.pendingdeveloperaction}")
    private Long pendingDeveloperAction;

    @Override
    public boolean isValid(ChangeRequestValidationContext context) {
        try {
            List<ChangeRequest> crs = context.getChangeRequestDAO()
                    .getByDeveloper(context.getChangeRequest().getDeveloper().getDeveloperId()).stream()
                    .filter(cr -> getInProcessStatuses().stream()
                            .anyMatch(status -> cr.getCurrentStatus().getChangeRequestStatusType().getId()
                                    .equals(status)))
                    .collect(Collectors.<ChangeRequest> toList());
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

    private List<Long> getInProcessStatuses() {
        return new ArrayList<Long>(Arrays.asList(pendingAcbAction, pendingDeveloperAction));
    }
}

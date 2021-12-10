package gov.healthit.chpl.changerequest.validation;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.changerequest.domain.ChangeRequestAttestation;
import gov.healthit.chpl.changerequest.domain.ChangeRequestDeveloperDetails;
import gov.healthit.chpl.changerequest.domain.ChangeRequestWebsite;
import gov.healthit.chpl.manager.rules.ValidationRule;

@Component
public class ChangeRequestModificationValidation extends ValidationRule<ChangeRequestValidationContext> {

    @Override
    public boolean isValid(ChangeRequestValidationContext context) {
        if (isStatusUpdated(context) || !changeRequestDetailsEquals(context)) {
            return true;
        } else {
            getMessages().add(getErrorMessage("changeRequest.noChanges"));
            return false;
        }
    }

    private Boolean isStatusUpdated(ChangeRequestValidationContext context) {
        return context.getOrigChangeRequest().getCurrentStatus().getChangeRequestStatusType().getId().equals(
                context.getNewChangeRequest().getCurrentStatus().getChangeRequestStatusType().getId());
    }

    private Boolean changeRequestDetailsEquals(ChangeRequestValidationContext context) {
        if (context.getNewChangeRequest().getDetails() instanceof ChangeRequestWebsite) {
            return (((ChangeRequestWebsite) context.getNewChangeRequest().getDetails()).isEqual(
                    (context.getOrigChangeRequest().getDetails())));
        } else if (context.getNewChangeRequest().getDetails() instanceof ChangeRequestDeveloperDetails) {
            return (((ChangeRequestDeveloperDetails) context.getNewChangeRequest().getDetails()).isEqual(
                    (context.getOrigChangeRequest().getDetails())));
        } else if (context.getNewChangeRequest().getDetails() instanceof ChangeRequestAttestation) {
            return (((ChangeRequestDeveloperDetails) context.getNewChangeRequest().getDetails()).isEqual(
                    (context.getOrigChangeRequest().getDetails())));
        } else {
            return false;
        }
    }
}

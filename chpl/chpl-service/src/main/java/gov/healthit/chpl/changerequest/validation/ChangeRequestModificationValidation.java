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
        if (isCurrentStatusEqual(context) && changeRequestDetailsEquals(context)) {
            getMessages().add(getErrorMessage("changeRequest.noChanges"));
            return false;
        } else {
            return true;
        }
    }

    private Boolean isCurrentStatusEqual(ChangeRequestValidationContext context) {
        return context.getOrigChangeRequest().getCurrentStatus().getChangeRequestStatusType().getId().equals(
                context.getNewChangeRequest().getCurrentStatus().getChangeRequestStatusType().getId());
    }

    private Boolean changeRequestDetailsEquals(ChangeRequestValidationContext context) {
        if (context.getNewChangeRequest().getDetails() instanceof ChangeRequestWebsite) {
            return ChangeRequestWebsite.cast(context.getNewChangeRequest().getDetails()).matches(
                    ChangeRequestWebsite.cast(context.getOrigChangeRequest().getDetails()));
        } else if (context.getNewChangeRequest().getDetails() instanceof ChangeRequestDeveloperDetails) {
            return ChangeRequestDeveloperDetails.cast(context.getNewChangeRequest().getDetails()).matches(
                    ChangeRequestDeveloperDetails.cast(context.getOrigChangeRequest().getDetails()));
        } else if (context.getNewChangeRequest().getDetails() instanceof ChangeRequestAttestation) {
            //return ChangeRequestAttestation.cast(context.getNewChangeRequest().getDetails()).matches(
            //        ChangeRequestAttestation.cast(context.getOrigChangeRequest().getDetails()));
            return false;
        } else {
            return false;
        }
    }
}

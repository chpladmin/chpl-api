package gov.healthit.chpl.changerequest.validation;

import gov.healthit.chpl.changerequest.domain.ChangeRequestListingUrl;
import gov.healthit.chpl.manager.rules.ValidationRule;

public class CheckDatePresentOnApprovalValidation extends ValidationRule<ChangeRequestValidationContext> {

    @Override
    public boolean isValid(ChangeRequestValidationContext context) {
        if (context.getNewChangeRequest().getCurrentStatus().getChangeRequestStatusType().getId().equals(getAcceptedStatusId(context))) {
            ChangeRequestListingUrl details = (ChangeRequestListingUrl) context.getNewChangeRequest().getDetails();
            if (details.getCheckDate() == null) {
                getMessages().add(getErrorMessage("changeRequest.listingUrl.checkDateRequired"));
                return false;
            }
        }
        return true;
    }

    private Long getAcceptedStatusId(ChangeRequestValidationContext context) {
        return context.getChangeRequestStatusIds().getAcceptedStatus();
    }

}

package gov.healthit.chpl.changerequest.validation;

import gov.healthit.chpl.changerequest.domain.ChangeRequestDeveloperDemographics;
import gov.healthit.chpl.manager.rules.ValidationRule;

public class ChangeRequestCreateValidation extends ValidationRule<ChangeRequestValidationContext> {

    @Override
    public boolean isValid(ChangeRequestValidationContext context) {
        if (context.getNewChangeRequest().getChangeRequestType().getId().equals(context.getChangeRequestTypeIds().getDeveloperDemographicsChangeRequestTypeId())) {
            ChangeRequestDeveloperDemographics details = (ChangeRequestDeveloperDemographics) context.getNewChangeRequest().getDetails();
            if (context.getNewChangeRequest().getDetails() == null) {
                getMessages().add(getErrorMessage("changeRequest.demographics.invalid"));
                return false;
            } else {
                boolean areDetailsValid = true;
                if (!isChangeRequestSelfDevloperValid(details)) {
                    getMessages().add(getErrorMessage("changeRequest.demographics.selfDeveloper.missing"));
                    areDetailsValid = false;
                } else if (!isChangeRequestAddressValid(details)) {
                    getMessages().add(getErrorMessage("changeRequest.demographics.address.missing"));
                    areDetailsValid = false;
                } else if (!isChangeRequestContactValid(details)) {
                    getMessages().add(getErrorMessage("changeRequest.demographics.contact.missing"));
                    areDetailsValid = false;
                } else if (!isChangeRequestContactValid(details)) {
                    getMessages().add(getErrorMessage("changeRequest.demographics..missing"));
                    areDetailsValid = false;
                }
                return areDetailsValid;
            }
        }
        return true;
    }

    private boolean isChangeRequestSelfDevloperValid(ChangeRequestDeveloperDemographics details) {
        return details.getSelfDeveloper() != null;
    }

    private boolean isChangeRequestAddressValid(ChangeRequestDeveloperDemographics details) {
        return details.getAddress() != null;
    }

    private boolean isChangeRequestContactValid(ChangeRequestDeveloperDemographics details) {
        return details.getContact() != null;
    }
}

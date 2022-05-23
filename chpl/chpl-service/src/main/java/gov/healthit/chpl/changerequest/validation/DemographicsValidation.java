package gov.healthit.chpl.changerequest.validation;

import org.apache.commons.lang3.StringUtils;

import gov.healthit.chpl.changerequest.domain.ChangeRequestDeveloperDemographics;
import gov.healthit.chpl.domain.Address;
import gov.healthit.chpl.manager.rules.ValidationRule;

public class DemographicsValidation extends ValidationRule<ChangeRequestValidationContext> {

    @Override
    public boolean isValid(ChangeRequestValidationContext context) {
        if (context.getResourcePermissions().isUserRoleDeveloperAdmin()) {
            ChangeRequestDeveloperDemographics details = (ChangeRequestDeveloperDemographics) context.getNewChangeRequest().getDetails();
            if (details.getAddress() != null) {
                boolean addressComponentsValid = true;
                if (!isStreetPopulated(details.getAddress())) {
                    getMessages().add(getErrorMessage("developer.address.streetRequired"));
                    addressComponentsValid = false;
                }
                if (!isCityPopulated(details.getAddress())) {
                    getMessages().add(getErrorMessage("developer.address.cityRequired"));
                    addressComponentsValid = false;
                }
                if (!isStatePopulated(details.getAddress())) {
                    getMessages().add(getErrorMessage("developer.address.stateRequired"));
                    addressComponentsValid = false;
                }
                if (!isZipPopulated(details.getAddress())) {
                    getMessages().add(getErrorMessage("developer.address.zipRequired"));
                    addressComponentsValid = false;
                }
                return addressComponentsValid;
            }
        }
        return true;
    }

    private boolean isStreetPopulated(Address address) {
        return StringUtils.isNotEmpty(address.getLine1());
    }

    private boolean isCityPopulated(Address address) {
        return StringUtils.isNotEmpty(address.getCity());
    }

    private boolean isStatePopulated(Address address) {
        return StringUtils.isNotEmpty(address.getState());
    }

    private boolean isZipPopulated(Address address) {
        return StringUtils.isNotEmpty(address.getZipcode());
    }
}

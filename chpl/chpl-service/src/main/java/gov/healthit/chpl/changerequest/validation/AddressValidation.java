package gov.healthit.chpl.changerequest.validation;

import org.apache.commons.lang3.StringUtils;

import gov.healthit.chpl.changerequest.domain.ChangeRequestDeveloperDemographic;
import gov.healthit.chpl.domain.Address;
import gov.healthit.chpl.manager.rules.ValidationRule;

public class AddressValidation extends ValidationRule<ChangeRequestValidationContext> {

    @Override
    public boolean isValid(ChangeRequestValidationContext context) {
        if (context.getResourcePermissions().isUserRoleDeveloperAdmin()) {
            ChangeRequestDeveloperDemographic details = (ChangeRequestDeveloperDemographic) context.getNewChangeRequest().getDetails();
            if (details.getAddress() != null) {
                boolean addressComponentsValid = true;
                if (!isStreetValid(details.getAddress())) {
                    getMessages().add(getErrorMessage("developer.address.streetRequired"));
                    addressComponentsValid = false;
                }
                if (!isCityValid(details.getAddress())) {
                    getMessages().add(getErrorMessage("developer.address.cityRequired"));
                    addressComponentsValid = false;
                }
                if (!isStateValid(details.getAddress())) {
                    getMessages().add(getErrorMessage("developer.address.stateRequired"));
                    addressComponentsValid = false;
                }
                if (!isZipValid(details.getAddress())) {
                    getMessages().add(getErrorMessage("developer.address.zipRequired"));
                    addressComponentsValid = false;
                }
                return addressComponentsValid;
            }
        }
        return true;
    }

    private boolean isStreetValid(Address address) {
        return StringUtils.isNotEmpty(address.getLine1());
    }

    private boolean isCityValid(Address address) {
        return StringUtils.isNotEmpty(address.getCity());
    }

    private boolean isStateValid(Address address) {
        return StringUtils.isNotEmpty(address.getState());
    }

    private boolean isZipValid(Address address) {
        return StringUtils.isNotEmpty(address.getZipcode());
    }
}

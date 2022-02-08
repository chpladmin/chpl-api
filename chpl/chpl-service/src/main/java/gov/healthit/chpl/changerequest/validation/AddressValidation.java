package gov.healthit.chpl.changerequest.validation;

import java.util.HashMap;

import org.springframework.util.StringUtils;

import gov.healthit.chpl.domain.Address;
import gov.healthit.chpl.manager.rules.ValidationRule;

public class AddressValidation extends ValidationRule<ChangeRequestValidationContext> {

    @Override
    public boolean isValid(ChangeRequestValidationContext context) {
        if (context.getResourcePermissions().isUserRoleDeveloperAdmin()) {
            HashMap<String, Object> details = (HashMap) context.getNewChangeRequest().getDetails();
            if (details.containsKey("address")) {
                Address crAddress = new Address((HashMap<String, Object>) details.get("address"));
                boolean addressComponentsValid = true;
                if (!isStreetValid(crAddress)) {
                    getMessages().add(getErrorMessage("developer.address.streetRequired"));
                    addressComponentsValid = false;
                }
                if (!isCityValid(crAddress)) {
                    getMessages().add(getErrorMessage("developer.address.cityRequired"));
                    addressComponentsValid = false;
                }
                if (!isStateValid(crAddress)) {
                    getMessages().add(getErrorMessage("developer.address.stateRequired"));
                    addressComponentsValid = false;
                }
                if (!isZipValid(crAddress)) {
                    getMessages().add(getErrorMessage("developer.address.zipRequired"));
                    addressComponentsValid = false;
                }
                return addressComponentsValid;
            }
        }
        return true;
    }

    private boolean isStreetValid(Address address) {
        return !StringUtils.isEmpty(address.getLine1());
    }

    private boolean isCityValid(Address address) {
        return !StringUtils.isEmpty(address.getCity());
    }

    private boolean isStateValid(Address address) {
        return !StringUtils.isEmpty(address.getState());
    }

    private boolean isZipValid(Address address) {
        return !StringUtils.isEmpty(address.getZipcode());
    }
}

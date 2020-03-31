package gov.healthit.chpl.changerequest.validation;

import java.io.IOException;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.domain.Address;
import gov.healthit.chpl.manager.rules.ValidationRule;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.JSONUtils;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class AddressValidation extends ValidationRule<ChangeRequestValidationContext> {

    private ResourcePermissions resourcePermissions;

    @Autowired
    public AddressValidation(final ResourcePermissions resourcePermissions) {
        this.resourcePermissions = resourcePermissions;
    }

    @Override
    public boolean isValid(ChangeRequestValidationContext context) {
        if (resourcePermissions.isUserRoleDeveloperAdmin()) {
            HashMap<String, Object> details = (HashMap) context.getChangeRequest().getDetails();
            if (details.containsKey("address")) {
                Address crAddress = null;
                try {
                    crAddress = JSONUtils.fromJSON(details.get("address").toString(), Address.class);
                } catch (IOException ex) {
                    LOGGER.error("Could not parse " + details.get("address") + " as an Address object.", ex);
                    return false;
                }
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

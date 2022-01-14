package gov.healthit.chpl.changerequest.validation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.domain.Address;
import gov.healthit.chpl.manager.rules.ValidationRule;
import gov.healthit.chpl.permissions.ResourcePermissions;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class AddressValidation extends ValidationRule<ChangeRequestValidationContext> {

    private ResourcePermissions resourcePermissions;

    @Autowired
    public AddressValidation(ResourcePermissions resourcePermissions) {
        this.resourcePermissions = resourcePermissions;
    }

    @Override
    public List<String> getErrorMessages(ChangeRequestValidationContext context) {
        List<String> errorMessages = new ArrayList<String>();

        if (resourcePermissions.isUserRoleDeveloperAdmin()) {
            HashMap<String, Object> details = (HashMap) context.getNewChangeRequest().getDetails();
            if (details.containsKey("address")) {
                Address crAddress = new Address((HashMap<String, Object>) details.get("address"));
                if (!isStreetValid(crAddress)) {
                    errorMessages.add(getErrorMessage("developer.address.streetRequired"));
                }
                if (!isCityValid(crAddress)) {
                    errorMessages.add(getErrorMessage("developer.address.cityRequired"));
                }
                if (!isStateValid(crAddress)) {
                    errorMessages.add(getErrorMessage("developer.address.stateRequired"));
                }
                if (!isZipValid(crAddress)) {
                    errorMessages.add(getErrorMessage("developer.address.zipRequired"));
                }
            }
        }
        return errorMessages;
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

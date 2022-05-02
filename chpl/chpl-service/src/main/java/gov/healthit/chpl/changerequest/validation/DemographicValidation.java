package gov.healthit.chpl.changerequest.validation;

import org.apache.commons.lang3.StringUtils;

import gov.healthit.chpl.changerequest.domain.ChangeRequestDeveloperDemographic;
import gov.healthit.chpl.domain.Address;
import gov.healthit.chpl.manager.rules.ValidationRule;
import gov.healthit.chpl.util.ValidationUtils;

public class DemographicValidation extends ValidationRule<ChangeRequestValidationContext> {

    @Override
    public boolean isValid(ChangeRequestValidationContext context) {
        if (context.getResourcePermissions().isUserRoleDeveloperAdmin()) {
            ChangeRequestDeveloperDemographic details = (ChangeRequestDeveloperDemographic) context.getNewChangeRequest().getDetails();
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
                if (!isWebsitePopulated(details.getWebsite())) {
                    getMessages().add(getErrorMessage("developer.websiteRequired"));
                    addressComponentsValid = false;
                } else if (!isWebsiteFormatValid(context.getValidationUtils(), details.getWebsite())) {
                    getMessages().add(getErrorMessage("developer.websiteIsInvalid"));
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

    private boolean isWebsitePopulated(String website) {
        return StringUtils.isNotEmpty(website);
    }

    private boolean isWebsiteFormatValid(ValidationUtils utils, String website) {
        return utils.isWellFormedUrl(website);
    }
}

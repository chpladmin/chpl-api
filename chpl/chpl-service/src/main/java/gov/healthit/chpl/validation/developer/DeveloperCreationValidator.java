package gov.healthit.chpl.validation.developer;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.ValidationUtils;

/**
 * Validate fields used when creating a new developer.
 */
@Component("developerCreationValidator")
public class DeveloperCreationValidator {

    private ErrorMessageUtil msgUtil;

    public DeveloperCreationValidator() {}

    @Autowired
    public DeveloperCreationValidator(final ErrorMessageUtil msgUtil) {
        this.msgUtil = msgUtil;
    }

    /**
     * Looks for missing or invalid fields in the developer object.
     * @param developer the developer to validate
     * @return a list of error messages generated from problems found with the developer
     */
    public Set<String> validate(final DeveloperDTO developer) {
        Set<String> errorMessages = new HashSet<String>();
        //developer name is required
        if (StringUtils.isEmpty(developer.getName())) {
            errorMessages.add(msgUtil.getMessage("developer.nameRequired"));
        }

        //if website is provided it must be a valid URL
        if (!StringUtils.isEmpty(developer.getWebsite())
                && !ValidationUtils.isWellFormedUrl(developer.getWebsite())) {
            errorMessages.add(msgUtil.getMessage("developer.websiteIsInvalid"));
        }

        //contact is required
        if (developer.getContact() == null) {
            errorMessages.add(msgUtil.getMessage("developer.contactRequired"));
        } else {
            if (StringUtils.isEmpty(developer.getContact().getEmail())) {
                errorMessages.add(msgUtil.getMessage("developer.contact.emailRequired"));
            }
            if (StringUtils.isEmpty(developer.getContact().getPhoneNumber())) {
                errorMessages.add(msgUtil.getMessage("developer.contact.phoneRequired"));
            }
            if (StringUtils.isEmpty(developer.getContact().getFullName())) {
                errorMessages.add(msgUtil.getMessage("developer.contact.nameRequired"));
            }
        }

        //address is required when creating a developer
        if (developer.getAddress() == null) {
            errorMessages.add(msgUtil.getMessage("developer.addressRequired"));
        } else {
            if (StringUtils.isEmpty(developer.getAddress().getStreetLineOne())) {
                errorMessages.add(msgUtil.getMessage("developer.address.streetRequired"));
            }
            if (StringUtils.isEmpty(developer.getAddress().getCity())) {
                errorMessages.add(msgUtil.getMessage("developer.address.cityRequired"));
            }
            if (StringUtils.isEmpty(developer.getAddress().getState())) {
                errorMessages.add(msgUtil.getMessage("developer.address.stateRequired"));
            }
            if (StringUtils.isEmpty(developer.getAddress().getZipcode())) {
                errorMessages.add(msgUtil.getMessage("developer.address.zipRequired"));
            }
        }

        //no status is required; new developer will default to active status if none is specified
        return errorMessages;
    }
}

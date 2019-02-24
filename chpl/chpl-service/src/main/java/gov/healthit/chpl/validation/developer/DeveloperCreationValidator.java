package gov.healthit.chpl.validation.developer;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.util.ErrorMessageUtil;

/**
 * Validate fields used when creating a new developer.
 */
@Component("developerCreationValidator")
public class DeveloperCreationValidator extends DeveloperUpdateValidator {

    @Autowired
    public DeveloperCreationValidator(final ErrorMessageUtil msgUtil) {
        super(msgUtil);
    }

    /**
     * Looks for missing or invalid fields in the developer object.
     * @param developer the developer to validate
     * @return a list of error messages generated from problems found with the developer
     */
    public Set<String> validate(final Developer developer) {
        Set<String> errorMessages = super.validate(developer);

        if (developer.getAddress() == null) {
            errorMessages.add(getMsgUtil().getMessage("developer.addressRequired"));
        } else {
            if (StringUtils.isEmpty(developer.getAddress().getLine1())) {
                errorMessages.add(getMsgUtil().getMessage("developer.address.streetRequired"));
            }
            if (StringUtils.isEmpty(developer.getAddress().getCity())) {
                errorMessages.add(getMsgUtil().getMessage("developer.address.cityRequired"));
            }
            if (StringUtils.isEmpty(developer.getAddress().getState())) {
                errorMessages.add(getMsgUtil().getMessage("developer.address.stateRequired"));
            }
            if (StringUtils.isEmpty(developer.getAddress().getZipcode())) {
                errorMessages.add(getMsgUtil().getMessage("developer.address.zipRequired"));
            }
        }
        return errorMessages;
    }
}

package gov.healthit.chpl.changerequest.validation;

import java.util.Objects;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.Strings;

import gov.healthit.chpl.changerequest.domain.ChangeRequestDeveloperDemographics;
import gov.healthit.chpl.domain.Address;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.contact.PointOfContact;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.rules.ValidationRule;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class DemographicsChangedValidation extends ValidationRule<ChangeRequestValidationContext> {

    @Override
    public boolean isValid(ChangeRequestValidationContext context) {
        Long crDeveloperId = context.getNewChangeRequest().getDeveloper().getId();
        ChangeRequestDeveloperDemographics details = (ChangeRequestDeveloperDemographics) context.getNewChangeRequest().getDetails();
        Developer existingDeveloper = null;
        try {
            existingDeveloper = context.getValidationDAOs().getDeveloperDAO().getById(crDeveloperId);
        } catch (EntityRetrievalException ex) {
            LOGGER.error("Could not find developer with ID " + crDeveloperId + " from change requeset with ID " + context.getNewChangeRequest().getId(), ex);
            return false;
        }

        boolean anyFieldUpdated = false;
        Address updatedAddress = details.getAddress();
        Address existingAddress = existingDeveloper.getAddress();
        if (ObjectUtils.allNotNull(updatedAddress, existingAddress)) {
            anyFieldUpdated = anyFieldUpdated
                    || !Strings.CS.equals(updatedAddress.getLine1(), existingAddress.getLine1())
                    || !Strings.CS.equals(updatedAddress.getLine2(), existingAddress.getLine2())
                    || !Strings.CS.equals(updatedAddress.getCity(), existingAddress.getCity())
                    || !Strings.CS.equals(updatedAddress.getState(), existingAddress.getState())
                    || !Strings.CS.equals(updatedAddress.getZipcode(), existingAddress.getZipcode())
                    || !Strings.CS.equals(updatedAddress.getCountry(), existingAddress.getCountry());
        } else {
            anyFieldUpdated = true;
        }

        PointOfContact updatedPoc = details.getContact();
        PointOfContact existingPoc = existingDeveloper.getContact();
        if (ObjectUtils.allNotNull(updatedPoc, existingPoc)) {
            anyFieldUpdated = anyFieldUpdated
                    || !Strings.CS.equals(updatedPoc.getFullName(), existingPoc.getFullName())
                    || !Strings.CS.equals(updatedPoc.getPhoneNumber(), existingPoc.getPhoneNumber())
                    || !Strings.CS.equals(updatedPoc.getEmail(), existingPoc.getEmail());
        } else {
            anyFieldUpdated = true;
        }

        anyFieldUpdated = anyFieldUpdated
                || !Objects.equals(details.getSelfDeveloper(), existingDeveloper.getSelfDeveloper())
                || !Strings.CS.equals(details.getWebsite(), existingDeveloper.getWebsite());

        if (!anyFieldUpdated) {
            getMessages().add(getErrorMessage("changeRequest.demographics.noChange"));
        }
        return anyFieldUpdated;
    }
}

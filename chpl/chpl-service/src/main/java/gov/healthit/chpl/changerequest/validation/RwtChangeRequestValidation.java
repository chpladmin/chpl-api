package gov.healthit.chpl.changerequest.validation;

import org.apache.commons.lang3.StringUtils;

import gov.healthit.chpl.changerequest.domain.ChangeRequestListingUrl;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.rules.ValidationRule;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class RwtChangeRequestValidation extends ValidationRule<ChangeRequestValidationContext> {

    @Override
    public boolean isValid(ChangeRequestValidationContext context) {

        //rwt must have a listing associated
        //rwt may have the same url than the listing currently has
        ChangeRequestListingUrl crDetails = (ChangeRequestListingUrl) context.getNewChangeRequest().getDetails();
        if (crDetails == null || crDetails.getListing() == null || crDetails.getListing().getId() == null) {
            getMessages().add(getErrorMessage("changeRequest.missingDetails"));
            return false;
        }
        try {
            CertifiedProductSearchDetails existingListing = context.getCpdManager().getCertifiedProductDetails(crDetails.getListing().getId());
            if (StringUtils.isEmpty(crDetails.getUrl())) {
                getMessages().add(context.getMsgUtil().getMessage("changeRequest.listingUrl.rwtUrl.missing", existingListing.getChplProductNumber()));
                return false;
            }
        } catch (EntityRetrievalException ex) {
            LOGGER.error("Unable to look up listing with ID " + crDetails.getListing().getId());
            getMessages().add(getErrorMessage("changeRequest.missingDetails"));
            return false;
        }
        return true;
    }
}

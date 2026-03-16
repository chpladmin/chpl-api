package gov.healthit.chpl.changerequest.validation;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.changerequest.domain.ChangeRequestListingUrl;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.rules.ValidationRule;
import gov.healthit.chpl.service.CertificationCriterionService.Criteria2015;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class SbulChangeRequestValidation extends ValidationRule<ChangeRequestValidationContext> {

    @Override
    public boolean isValid(ChangeRequestValidationContext context) {

        //sbul must have a listing associated
        //sbul must have a different url than the listing currently has
        ChangeRequestListingUrl crDetails = (ChangeRequestListingUrl) context.getNewChangeRequest().getDetails();
        if (crDetails == null || crDetails.getListing() == null || crDetails.getListing().getId() == null) {
            getMessages().add(context.getMsgUtil().getMessage("changeRequest.missingDetails"));
            return false;
        }

        try {
            CertifiedProductSearchDetails existingListing = context.getCpdManager().getCertifiedProductDetails(crDetails.getListing().getId());
            CertificationCriterion g10 = context.getCriteriaService().get(Criteria2015.G_10);
            CertificationResult g10Result = existingListing.getCertificationResults().stream()
                    .filter(certResult -> certResult.getCriterion().getId().equals(g10.getId()))
                    .findAny()
                    .orElse(null);
            if (g10Result == null) {
                getMessages().add(context.getMsgUtil().getMessage("changeRequest.listingUrl.serviceBaseUrlList.noG10", existingListing.getChplProductNumber()));
                return false;
            }
            if (StringUtils.isEmpty(crDetails.getUrl())) {
                getMessages().add(context.getMsgUtil().getMessage("changeRequest.listingUrl.serviceBaseUrlList.missing", existingListing.getChplProductNumber()));
                return false;
            }
            if (Strings.CS.equals(g10Result.getServiceBaseUrlList(), crDetails.getUrl())) {
                getMessages().add(context.getMsgUtil().getMessage("changeRequest.listingUrl.serviceBaseUrlList.sameUrl", existingListing.getChplProductNumber()));
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

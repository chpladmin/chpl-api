package gov.healthit.chpl.validation.pendingListing.reviewer;

import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dto.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.PendingCertifiedProductDTO;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.ValidationUtils;

/**
 * Validate URLs have no new lines and otherwise look like URLs in Pending Listings.
 * @author alarned
 *
 */
@Component("pendingUrlReviewer")
public class UrlReviewer implements Reviewer {

    @Autowired private ErrorMessageUtil msgUtil;

    public void review(final PendingCertifiedProductDTO listing) {
        //check all string fields at the listing level
        if (listing.getDeveloperWebsite() != null) {
            addListingErrorIfNotValid(listing, listing.getDeveloperWebsite(),
                    "Developer's Website '" + listing.getDeveloperWebsite() + "'");
        }
        addListingErrorIfNotValid(listing, listing.getReportFileLocation(),
                "Report File Location '" + listing.getReportFileLocation() + "'");
        addListingErrorIfNotValid(listing, listing.getSedReportFileLocation(),
                "SED Report File Location '" + listing.getSedReportFileLocation() + "'");
        addListingErrorIfNotValid(listing, listing.getTransparencyAttestationUrl(),
                "Transparency Attestation URL '" + listing.getTransparencyAttestationUrl() + "'");

        //check all criteria fields
        for (PendingCertificationResultDTO cert : listing.getCertificationCriterion()) {
            if (cert.getMeetsCriteria() != null && cert.getMeetsCriteria().booleanValue()) {
                addCriteriaErrorIfNotValid(listing, cert, cert.getApiDocumentation(), "API Documentation");
            }
        }
    }

    private void addListingErrorIfNotValid(final PendingCertifiedProductDTO listing,
            final String input, final String fieldName) {
        if (ValidationUtils.hasNewline(input)) {
            listing.getErrorMessages().add(
                    msgUtil.getMessage("listing.invalidUrlFound", fieldName));
        } else {
            UrlValidator validator = new UrlValidator();
            if (!validator.isValid(input)) {
                listing.getErrorMessages().add(
                        msgUtil.getMessage("listing.invalidUrlFound", fieldName));
            }
        }
    }

    private void addCriteriaErrorIfNotValid(final PendingCertifiedProductDTO listing,
            final PendingCertificationResultDTO cert, final String input, final String fieldName) {
        if (ValidationUtils.hasNewline(input)) {
            listing.getErrorMessages().add(
                    msgUtil.getMessage("listing.criteria.invalidUrlFound", fieldName, cert.getNumber()));
        } else {
            UrlValidator validator = new UrlValidator();
            if (!validator.isValid(input)) {
                listing.getErrorMessages().add(
                        msgUtil.getMessage("listing.criteria.invalidUrlFound", fieldName, cert.getNumber()));
            }
        }
    }
}

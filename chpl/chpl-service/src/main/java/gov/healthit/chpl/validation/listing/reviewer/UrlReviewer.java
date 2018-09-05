package gov.healthit.chpl.validation.listing.reviewer;

import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.ValidationUtils;

/**
 * Validate URLs have no new lines and otherwise look like URLs.
 * @author alarned
 *
 */
@Component("urlReviewer")
public class UrlReviewer implements Reviewer {

    @Autowired private ErrorMessageUtil msgUtil;

    public void review(final CertifiedProductSearchDetails listing) {
        //check all string fields at the listing level
        if (listing.getDeveloper() != null) {
            addListingErrorIfNotValid(listing, listing.getDeveloper().getWebsite(),
                    "Developer's Website '" + listing.getDeveloper().getWebsite() + "'");
        }
        addListingErrorIfNotValid(listing, listing.getReportFileLocation(),
                "Report File Location '" + listing.getReportFileLocation() + "'");
        addListingErrorIfNotValid(listing, listing.getSedReportFileLocation(),
                "SED Report File Location '" + listing.getSedReportFileLocation() + "'");
        addListingErrorIfNotValid(listing, listing.getTransparencyAttestationUrl(),
                "Transparency Attestation URL '" + listing.getTransparencyAttestationUrl() + "'");

        //check all criteria fields
        for (CertificationResult cert : listing.getCertificationResults()) {
            if (cert.isSuccess() != null && cert.isSuccess().booleanValue()) {
                addCriteriaErrorIfNotValid(listing, cert, cert.getApiDocumentation(), "API Documentation");
            }
        }
    }

    private void addListingErrorIfNotValid(final CertifiedProductSearchDetails listing,
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

    private void addCriteriaErrorIfNotValid(final CertifiedProductSearchDetails listing,
            final CertificationResult criteria, final String input, final String fieldName) {
        if (ValidationUtils.hasNewline(input)) {
            listing.getErrorMessages().add(
                    msgUtil.getMessage("listing.criteria.invalidUrlFound", fieldName, criteria.getNumber()));
        } else {
            UrlValidator validator = new UrlValidator();
            if (!validator.isValid(input)) {
                listing.getErrorMessages().add(
                        msgUtil.getMessage("listing.criteria.invalidUrlFound", fieldName, criteria.getNumber()));
            }
        }
    }
}

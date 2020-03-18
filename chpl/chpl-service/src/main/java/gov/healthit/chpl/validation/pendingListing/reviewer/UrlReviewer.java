package gov.healthit.chpl.validation.pendingListing.reviewer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import gov.healthit.chpl.util.ValidationUtils;

/**
 * Validate URLs have no new lines and otherwise look like URLs in Pending Listings.
 * @author alarned
 *
 */
@Component("pendingUrlReviewer")
public class UrlReviewer extends PermissionBasedReviewer {

    @Autowired
    public UrlReviewer(ErrorMessageUtil msgUtil, ResourcePermissions resourcePermissions) {
        super(msgUtil, resourcePermissions);
    }

    @Override
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
            if (cert.getMeetsCriteria() != null && cert.getMeetsCriteria().equals(Boolean.TRUE)) {
                addCriteriaErrorIfNotValid(listing, cert, cert.getApiDocumentation(), "API Documentation");
                addCriteriaErrorIfNotValid(listing, cert, cert.getExportDocumentation(), "Export Documentation");
                addCriteriaErrorIfNotValid(listing, cert, cert.getDocumentationUrl(), "Documentation Url");
                addCriteriaErrorIfNotValid(listing, cert, cert.getUseCases(), "Use Cases");
            }
        }
    }

    private void addListingErrorIfNotValid(final PendingCertifiedProductDTO listing,
            final String input, final String fieldName) {
        if (!StringUtils.isEmpty(input)) {
            if (ValidationUtils.hasNewline(input)) {
                listing.getErrorMessages().add(
                        msgUtil.getMessage("listing.invalidUrlFound", fieldName));
            }  else if (!ValidationUtils.isWellFormedUrl(input)) {
                listing.getErrorMessages().add(
                        msgUtil.getMessage("listing.invalidUrlFound", fieldName));
            }
        }
    }

    private void addCriteriaErrorIfNotValid(final PendingCertifiedProductDTO listing,
            final PendingCertificationResultDTO cert, final String input, final String fieldName) {
        if (!StringUtils.isEmpty(input)) {
            if (ValidationUtils.hasNewline(input)
                    || !ValidationUtils.isWellFormedUrl(input)) {
                addErrorOrWarningByPermission(listing, cert,
                        "listing.criteria.invalidUrlFound", fieldName,
                        Util.formatCriteriaNumber(cert.getCriterion()));
            }
        }
    }
}

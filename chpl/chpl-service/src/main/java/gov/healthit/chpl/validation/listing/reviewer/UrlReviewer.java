package gov.healthit.chpl.validation.listing.reviewer;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import gov.healthit.chpl.util.ValidationUtils;

@Component("urlReviewer")
public class UrlReviewer extends PermissionBasedReviewer {
    private ValidationUtils validationUtils;

    @Autowired
    public UrlReviewer(ValidationUtils validationUtils, ErrorMessageUtil msgUtil,
            ResourcePermissions resourcePermissions) {
        super(msgUtil, resourcePermissions);
        this.validationUtils = validationUtils;
    }

    @Override
    public void review(CertifiedProductSearchDetails listing) {
        // check all string fields at the listing level
        addListingErrorIfNotValid(listing, listing.getReportFileLocation(),
                "Report File Location '" + listing.getReportFileLocation() + "'");
        addListingErrorIfNotValid(listing, listing.getSedReportFileLocation(),
                "SED Report File Location '" + listing.getSedReportFileLocation() + "'");
        addListingErrorIfNotValid(listing, listing.getMandatoryDisclosures(),
                "Mandatory Disclosures '" + listing.getMandatoryDisclosures() + "'");
        addListingErrorIfNotValid(listing, listing.getSvapNoticeUrl(),
                "SVAP Notice URL '" + listing.getSvapNoticeUrl() + "'");

        // check all criteria fields
        for (CertificationResult cert : listing.getCertificationResults()) {
            if (validationUtils.isEligibleForErrors(cert)) {
                addCriteriaErrorIfNotValid(listing, cert, cert.getApiDocumentation(), "API Documentation");
                addCriteriaErrorIfNotValid(listing, cert, cert.getExportDocumentation(), "Export Documentation");
                addCriteriaErrorIfNotValid(listing, cert, cert.getDocumentationUrl(), "Documentation Url");
                addCriteriaErrorIfNotValid(listing, cert, cert.getUseCases(), "Use Cases");
                addCriteriaErrorIfNotValid(listing, cert, cert.getServiceBaseUrlList(), "Service Base URL List");
                addCriteriaErrorIfNotValid(listing, cert, cert.getRiskManagementSummaryInformation(), "Risk Management Summary Information");
            }
        }
    }

    private void addListingErrorIfNotValid(CertifiedProductSearchDetails listing, String input, String fieldName) {
        if (!StringUtils.isEmpty(input)) {
            if (validationUtils.hasNewline(input)) {
                listing.addBusinessErrorMessage(
                        msgUtil.getMessage("listing.invalidUrlFound", fieldName));
            } else if (!validationUtils.isWellFormedUrl(input)) {
                listing.addBusinessErrorMessage(
                        msgUtil.getMessage("listing.invalidUrlFound", fieldName));
            }
        }
    }

    private void addCriteriaErrorIfNotValid(CertifiedProductSearchDetails listing, CertificationResult cert, String input, String fieldName) {
        if (!StringUtils.isEmpty(input)) {
            if (validationUtils.hasNewline(input) || !validationUtils.isWellFormedUrl(input)) {
                addBusinessCriterionError(listing, cert,
                        "listing.criteria.invalidUrlFound", fieldName,
                        Util.formatCriteriaNumber(cert.getCriterion()));
            }
        }
    }
}

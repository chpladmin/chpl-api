package gov.healthit.chpl.validation.listing.reviewer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

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
        //check all string fields at the listing level
        addListingErrorIfNotValid(listing, listing.getReportFileLocation(),
                "Report File Location '" + listing.getReportFileLocation() + "'");
        addListingErrorIfNotValid(listing, listing.getSedReportFileLocation(),
                "SED Report File Location '" + listing.getSedReportFileLocation() + "'");
        addListingErrorIfNotValid(listing, listing.getTransparencyAttestationUrl(),
                "Mandatory Disclosures '" + listing.getTransparencyAttestationUrl() + "'");
        addListingErrorIfNotValid(listing, listing.getSvapNoticeUrl(),
                "SVAP Notice URL '" + listing.getSvapNoticeUrl() + "'");

        //check all criteria fields
        for (CertificationResult cert : listing.getCertificationResults()) {
            if (cert.isSuccess() != null && cert.isSuccess().equals(Boolean.TRUE)) {
                addCriteriaErrorIfNotValid(listing, cert, cert.getApiDocumentation(), "API Documentation");
                addCriteriaErrorIfNotValid(listing, cert, cert.getExportDocumentation(), "Export Documentation");
                addCriteriaErrorIfNotValid(listing, cert, cert.getDocumentationUrl(), "Documentation Url");
                addCriteriaErrorIfNotValid(listing, cert, cert.getUseCases(), "Use Cases");
                addCriteriaErrorIfNotValid(listing, cert, cert.getServiceBaseUrlList(), "Service Base URL List");
            }
        }
    }

    private void addListingErrorIfNotValid(CertifiedProductSearchDetails listing, String input, String fieldName) {
        if (!StringUtils.isEmpty(input)) {
            if (validationUtils.hasNewline(input)) {
                listing.getErrorMessages().add(
                        msgUtil.getMessage("listing.invalidUrlFound", fieldName));
            } else if (!validationUtils.isWellFormedUrl(input)) {
                listing.getErrorMessages().add(
                        msgUtil.getMessage("listing.invalidUrlFound", fieldName));
            }
        }
    }

    private void addCriteriaErrorIfNotValid(CertifiedProductSearchDetails listing, CertificationResult cert, String input, String fieldName) {
        if (!StringUtils.isEmpty(input)) {
            if (validationUtils.hasNewline(input) || !validationUtils.isWellFormedUrl(input)) {
                addCriterionErrorOrWarningByPermission(listing, cert,
                        "listing.criteria.invalidUrlFound", fieldName,
                        Util.formatCriteriaNumber(cert.getCriterion()));
            }
        }
    }
}

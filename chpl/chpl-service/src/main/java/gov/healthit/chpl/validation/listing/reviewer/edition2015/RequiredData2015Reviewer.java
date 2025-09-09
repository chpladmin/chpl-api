package gov.healthit.chpl.validation.listing.reviewer.edition2015;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.permissions.ResourcePermissionsFactory;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import gov.healthit.chpl.validation.listing.reviewer.PermissionBasedReviewer;

@Component("requiredData2015Reviewer")
public class RequiredData2015Reviewer extends PermissionBasedReviewer {

    private CertificationResultRules certRules;

    @Autowired
    @SuppressWarnings("checkstyle:parameternumber")
    public RequiredData2015Reviewer(CertificationResultRules certRules,
            ErrorMessageUtil msgUtil,
            ResourcePermissionsFactory resourcePermissionsFactory) {
        super(msgUtil, resourcePermissionsFactory);
        this.certRules = certRules;
    }

    @Override
    public void review(CertifiedProductSearchDetails listing) {
        reviewRequiredFieldsCommonToAllListings(listing);

        if (listing.getIcs() == null || listing.getIcs().getInherits() == null) {
            listing.addDataErrorMessage(msgUtil.getMessage("listing.missingIcs"));
        }

        for (CertificationResult cert : listing.getCertificationResults()) {
            if (cert.getSuccess() != null && cert.getSuccess()) {
                if (certRules.hasCertOption(cert.getCriterion().getId(), CertificationResultRules.ATTESTATION_ANSWER)
                        && cert.getAttestationAnswer() == null) {
                    addBusinessCriterionError(listing, cert,
                            "listing.criteria.missingAttestationAnswer", Util.formatCriteriaNumber(cert.getCriterion()));
                }

                if (certRules.hasCertOption(cert.getCriterion().getId(), CertificationResultRules.API_DOCUMENTATION)
                        && StringUtils.isEmpty(cert.getApiDocumentation())) {
                    addDataCriterionError(listing, cert, "listing.criteria.missingApiDocumentation",
                            Util.formatCriteriaNumber(cert.getCriterion()));
                }
                if (certRules.hasCertOption(cert.getCriterion().getId(), CertificationResultRules.EXPORT_DOCUMENTATION)
                        && StringUtils.isEmpty(cert.getExportDocumentation())) {
                    addBusinessCriterionError(listing, cert, "listing.criteria.missingExportDocumentation",
                            Util.formatCriteriaNumber(cert.getCriterion()));
                }

                if (certRules.hasCertOption(cert.getCriterion().getId(), CertificationResultRules.USE_CASES)
                        && StringUtils.isEmpty(cert.getUseCases())
                        && cert.getAttestationAnswer() != null && cert.getAttestationAnswer().equals(Boolean.TRUE)) {
                    addBusinessCriterionError(listing, cert, "listing.criteria.missingUseCases",
                            Util.formatCriteriaNumber(cert.getCriterion()));
                } else if (certRules.hasCertOption(cert.getCriterion().getId(), CertificationResultRules.USE_CASES)
                        && !StringUtils.isEmpty(cert.getUseCases())
                        && (cert.getAttestationAnswer() == null || cert.getAttestationAnswer().equals(Boolean.FALSE))) {
                    listing.addWarningMessage(
                            msgUtil.getMessage("listing.criteria.useCasesWithoutAttestation",
                                    Util.formatCriteriaNumber(cert.getCriterion())));
                }

                if (certRules.hasCertOption(cert.getCriterion().getId(), CertificationResultRules.SERVICE_BASE_URL_LIST)
                        && StringUtils.isEmpty(cert.getServiceBaseUrlList())) {
                    addDataCriterionError(listing, cert, "listing.criteria.missingServiceBaseUrlList",
                            Util.formatCriteriaNumber(cert.getCriterion()));
                }

                if ((certRules.hasCertOption(cert.getCriterion().getId(), CertificationResultRules.TEST_PROCEDURE)
                                && !certRules.hasCertOption(cert.getCriterion().getId(), CertificationResultRules.CONFORMANCE_METHOD))
                        && (cert.getTestProcedures() == null || cert.getTestProcedures().size() == 0)) {
                    addBusinessCriterionError(listing, cert, "listing.criteria.missingTestProcedure",
                            Util.formatCriteriaNumber(cert.getCriterion()));
                }
            }
        }
    }

    private void reviewRequiredFieldsCommonToAllListings(CertifiedProductSearchDetails listing) {
        if (StringUtils.isEmpty(listing.getAcbCertificationId())) {
            listing.addWarningMessage("CHPL certification ID was not found.");
        }
        if (listing.getCertificationDate() == null) {
            listing.addBusinessErrorMessage("Certification date was not found.");
        }
        if (listing.getDeveloper() == null) {
            listing.addBusinessErrorMessage("A developer is required.");
        }
        if (listing.getProduct() == null || StringUtils.isEmpty(listing.getProduct().getName())) {
            listing.addBusinessErrorMessage("A product name is required.");
        }
        if (listing.getVersion() == null || StringUtils.isEmpty(listing.getVersion().getVersion())) {
            listing.addBusinessErrorMessage("A product version is required.");
        }
    }
}

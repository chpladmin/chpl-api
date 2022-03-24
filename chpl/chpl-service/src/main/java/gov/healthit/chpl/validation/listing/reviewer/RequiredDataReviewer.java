package gov.healthit.chpl.validation.listing.reviewer;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;

@Component("requiredDataReviewer")
public class RequiredDataReviewer extends PermissionBasedReviewer {
    protected CertificationResultRules certRules;

    @Autowired
    public RequiredDataReviewer(CertificationResultRules certRules, ErrorMessageUtil msgUtil,
            ResourcePermissions resourcePermissions) {
        super(msgUtil, resourcePermissions);
        this.certRules = certRules;
    }

    @Override
    public void review(final CertifiedProductSearchDetails listing) {
        if (listing.getCertificationEdition() == null
                || listing.getCertificationEdition().get(CertifiedProductSearchDetails.EDITION_ID_KEY) == null) {
            listing.getErrorMessages().add("Certification edition is required but was not found.");
        }
        if (StringUtils.isEmpty(listing.getAcbCertificationId())) {
            listing.getWarningMessages().add("CHPL certification ID was not found.");
        }
        if (listing.getCertificationDate() == null) {
            listing.getErrorMessages().add("Certification date was not found.");
        }
        if (listing.getDeveloper() == null) {
            listing.getErrorMessages().add("A developer is required.");
        }
        if (listing.getProduct() == null || StringUtils.isEmpty(listing.getProduct().getName())) {
            listing.getErrorMessages().add("A product name is required.");
        }
        if (listing.getVersion() == null || StringUtils.isEmpty(listing.getVersion().getVersion())) {
            listing.getErrorMessages().add("A product version is required.");
        }
        if (listing.getOldestStatus() == null) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.noStatusProvided"));
        }

        for (CertificationResult cert : listing.getCertificationResults()) {
            if (cert.isSuccess() != null && cert.isSuccess().equals(Boolean.TRUE)
                    && certRules.hasCertOption(cert.getCriterion().getNumber(), CertificationResultRules.GAP)
                    && cert.isGap() == null) {
                addCriterionErrorOrWarningByPermission(listing, cert, "listing.criteria.missingGap",
                        Util.formatCriteriaNumber(cert.getCriterion()));
            }
        }
    }
}

package gov.healthit.chpl.validation.listing.reviewer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component("requiredDataReviewer")
public class RequiredDataReviewer implements Reviewer {
    protected ErrorMessageUtil msgUtil;
    protected CertificationResultRules certRules;

    @Autowired
    public RequiredDataReviewer(CertificationResultRules certRules, ErrorMessageUtil msgUtil) {
        this.certRules = certRules;
        this.msgUtil = msgUtil;
    }

    @Override
    public void review(final CertifiedProductSearchDetails listing) {
        if (listing.getCertificationEdition() == null || listing.getCertificationEdition().get("id") == null) {
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
            if (cert.isSuccess() != null && cert.isSuccess().booleanValue()) {
                if (certRules.hasCertOption(cert.getNumber(), CertificationResultRules.GAP) && cert.isGap() == null) {
                    listing.getErrorMessages().add(
                            msgUtil.getMessage("listing.criteria.missingGap", cert.getNumber()));
                }
            }
        }
    }
}

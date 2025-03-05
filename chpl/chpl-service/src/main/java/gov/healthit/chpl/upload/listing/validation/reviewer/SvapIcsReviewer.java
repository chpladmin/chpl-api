package gov.healthit.chpl.upload.listing.validation.reviewer;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.svap.domain.CertificationResultSvap;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.ValidationUtils;
import lombok.extern.log4j.Log4j2;

//This reviewer is only relevant to new listings - there is a validation rule
//that newly created listings must have ICS if they are using any replaced SVAPs.
//The rules are different when editing a listing.
@Component("listingUploadSvapIcsReviewer")
@Log4j2
public class SvapIcsReviewer {
    private CertificationResultRules certResultRules;
    private ValidationUtils validationUtils;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public SvapIcsReviewer(CertificationResultRules certResultRules,
            ValidationUtils validationUtils,
            ErrorMessageUtil msgUtil) {
        this.certResultRules = certResultRules;
        this.validationUtils = validationUtils;
        this.msgUtil = msgUtil;
    }

    public void review(CertifiedProductSearchDetails listing) {
        listing.getCertificationResults().stream()
            .filter(certResult -> validationUtils.isEligibleForErrors(certResult))
            .filter(certResult -> certResultRules.hasCertOption(certResult.getCriterion().getId(), CertificationResultRules.SVAP))
            .forEach(certResult -> reviewCertificationResult(listing, certResult));
    }

    private void reviewCertificationResult(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        if (!CollectionUtils.isEmpty(certResult.getSvaps())) {
            certResult.getSvaps().stream()
                .forEach(svap -> reviewSvapMarkedAsReplaced(listing, certResult, svap));
        }
    }

    private void reviewSvapMarkedAsReplaced(CertifiedProductSearchDetails listing,
            CertificationResult certResult, CertificationResultSvap svap) {
        if (svap.getSvapId() != null
                && BooleanUtils.isTrue(svap.isReplaced())
                && !doesListingHaveIcs(listing)) {
            listing.addDataErrorMessage(msgUtil.getMessage("listing.criteria.svap.replacedWithIcs",
                    svap.getRegulatoryTextCitation(), certResult.getCriterion().getNumber()));
        }
    }

    private boolean doesListingHaveIcs(CertifiedProductSearchDetails listing) {
        return listing.getIcs().getInherits();
    }
}

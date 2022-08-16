package gov.healthit.chpl.upload.listing.validation.reviewer;

import java.util.Iterator;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.svap.domain.CertificationResultSvap;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import gov.healthit.chpl.util.ValidationUtils;
import lombok.extern.log4j.Log4j2;

@Component("listingUploadSvapReviewer")
@Log4j2
public class SvapReviewer {
    private CertificationResultRules certResultRules;
    private ValidationUtils validationUtils;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public SvapReviewer(CertificationResultRules certResultRules,
            ValidationUtils validationUtils,
            ErrorMessageUtil msgUtil) {
        this.certResultRules = certResultRules;
        this.validationUtils = validationUtils;
        this.msgUtil = msgUtil;
    }

    public void review(CertifiedProductSearchDetails listing) {
        listing.getCertificationResults().stream()
            .filter(certResult -> validationUtils.isEligibleForErrors(certResult))
            .forEach(certResult -> reviewCertificationResult(listing, certResult));
        listing.getCertificationResults().stream()
            .forEach(certResult -> removeSvapIfNotApplicable(certResult));
    }

    private void reviewCertificationResult(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        reviewCriteriaCanHaveSvaps(listing, certResult);
        if (!CollectionUtils.isEmpty(certResult.getSvaps())) {
            removeSvapsWithoutIds(listing, certResult);
            certResult.getSvaps().stream()
                .forEach(svap -> reviewSvapFields(listing, certResult, svap));
        }
    }

    private void reviewCriteriaCanHaveSvaps(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        if (!certResultRules.hasCertOption(certResult.getCriterion().getNumber(), CertificationResultRules.SVAP)) {
            if (!CollectionUtils.isEmpty(certResult.getSvaps())) {
                listing.getWarningMessages().add(msgUtil.getMessage(
                    "listing.criteria.svapsNotApplicable", Util.formatCriteriaNumber(certResult.getCriterion())));
            }
            certResult.setSvaps(null);
        }
    }

    private void removeSvapIfNotApplicable(CertificationResult certResult) {
        if (!certResultRules.hasCertOption(certResult.getCriterion().getNumber(), CertificationResultRules.SVAP)) {
            certResult.setSvaps(null);
        }
    }

    private void removeSvapsWithoutIds(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        if (CollectionUtils.isEmpty(certResult.getSvaps())) {
            return;
        }
        Iterator<CertificationResultSvap> svapIter = certResult.getSvaps().iterator();
        while (svapIter.hasNext()) {
            CertificationResultSvap svap = svapIter.next();
            if (svap.getSvapId() == null) {
                svapIter.remove();
                listing.getWarningMessages().add(msgUtil.getMessage(
                        "listing.criteria.svap.invalidCriteriaAndRemoved",
                        svap.getRegulatoryTextCitation(),
                        Util.formatCriteriaNumber(certResult.getCriterion())));
            }
        }
    }

    private void reviewSvapFields(CertifiedProductSearchDetails listing,
            CertificationResult certResult, CertificationResultSvap svap) {
        reviewRegulatoryTextCitationRequired(listing, certResult, svap);
        reviewSvapMarkedAsReplaced(listing, certResult, svap);
    }

    private void reviewRegulatoryTextCitationRequired(CertifiedProductSearchDetails listing,
            CertificationResult certResult, CertificationResultSvap svap) {
        if (StringUtils.isEmpty(svap.getRegulatoryTextCitation())) {
            listing.getErrorMessages().add(
                    msgUtil.getMessage("listing.criteria.svap.missingCitation",
                    Util.formatCriteriaNumber(certResult.getCriterion())));
        }
    }

    private void reviewSvapMarkedAsReplaced(CertifiedProductSearchDetails listing,
            CertificationResult certResult, CertificationResultSvap svap) {
        if (svap.getSvapId() != null
                && BooleanUtils.isTrue(svap.getReplaced())
                && !doesListingHaveIcs(listing)) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.criteria.svap.replacedWithIcs",
                    svap.getRegulatoryTextCitation(), certResult.getCriterion().getNumber()));
        }
    }

    private boolean doesListingHaveIcs(CertifiedProductSearchDetails listing) {
        return listing.getIcs().getInherits();
    }
}

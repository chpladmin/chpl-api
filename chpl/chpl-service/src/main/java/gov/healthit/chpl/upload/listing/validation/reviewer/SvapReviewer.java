package gov.healthit.chpl.upload.listing.validation.reviewer;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.svap.domain.CertificationResultSvap;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import gov.healthit.chpl.validation.listing.reviewer.Reviewer;
import lombok.extern.log4j.Log4j2;

@Component("listingUploadSvapReviewer")
@Log4j2
public class SvapReviewer implements Reviewer {
    private CertificationResultRules certResultRules;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public SvapReviewer(CertificationResultRules certResultRules,
            ErrorMessageUtil msgUtil) {
        this.certResultRules = certResultRules;
        this.msgUtil = msgUtil;
    }

    @Override
    public void review(CertifiedProductSearchDetails listing) {
        listing.getCertificationResults().stream()
            .filter(certResult -> BooleanUtils.isTrue(certResult.isSuccess()))
            .forEach(certResult -> reviewCertificationResult(listing, certResult));
    }

    private void reviewCertificationResult(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        reviewCriteriaCanHaveSvaps(listing, certResult);
        if (!CollectionUtils.isEmpty(certResult.getSvaps())) {
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

    private void reviewSvapFields(CertifiedProductSearchDetails listing,
            CertificationResult certResult, CertificationResultSvap svap) {
        reviewIdRequired(listing, certResult, svap);
        reviewRegulatoryTextCitationRequired(listing, certResult, svap);
    }

    private void reviewIdRequired(CertifiedProductSearchDetails listing,
            CertificationResult certResult, CertificationResultSvap svap) {
        if (svap.getSvapId() == null
                && !StringUtils.isEmpty(svap.getRegulatoryTextCitation())) {
            listing.getErrorMessages().add(
                    msgUtil.getMessage("listing.criteria.svap.invalidCriteria",
                    svap.getRegulatoryTextCitation(),
                    Util.formatCriteriaNumber(certResult.getCriterion())));
        }
    }

    private void reviewRegulatoryTextCitationRequired(CertifiedProductSearchDetails listing,
            CertificationResult certResult, CertificationResultSvap svap) {
        if (StringUtils.isEmpty(svap.getRegulatoryTextCitation())) {
            listing.getErrorMessages().add(
                    msgUtil.getMessage("listing.criteria.svap.missingCitation",
                    Util.formatCriteriaNumber(certResult.getCriterion())));
        }
    }
}

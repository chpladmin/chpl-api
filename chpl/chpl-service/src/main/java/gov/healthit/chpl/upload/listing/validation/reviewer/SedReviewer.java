package gov.healthit.chpl.upload.listing.validation.reviewer;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import gov.healthit.chpl.util.ValidationUtils;

@Component("listingUploadSedReviewer")
public class SedReviewer {

    private UcdProcessReviewer ucdProcessReviewer;
    private TestTaskReviewer testTaskReviewer;
    private TestParticipantReviewer testParticipantReviewer;
    private CertificationResultRules certResultRules;
    private ValidationUtils validationUtils;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public SedReviewer(@Qualifier("ucdProcessReviewer") UcdProcessReviewer ucdProcessReviewer,
            @Qualifier("listingUploadTestTaskReviewer") TestTaskReviewer testTaskReviewer,
            @Qualifier("listingUploadTestParticipantReviewer") TestParticipantReviewer testParticipantReviewer,
            CertificationResultRules certResultRules,
            ValidationUtils validationUtils,
            ErrorMessageUtil msgUtil) {
        this.ucdProcessReviewer = ucdProcessReviewer;
        this.testTaskReviewer = testTaskReviewer;
        this.testParticipantReviewer = testParticipantReviewer;
        this.certResultRules = certResultRules;
        this.validationUtils = validationUtils;
        this.msgUtil = msgUtil;
    }

    public void review(CertifiedProductSearchDetails listing) {
        reviewUnusedTasksAndParticipants(listing);
        ucdProcessReviewer.review(listing);
        testTaskReviewer.review(listing);
        testParticipantReviewer.review(listing);

        listing.getCertificationResults().stream()
            .filter(certResult -> certResult.getCriterion() != null && certResult.getCriterion().getId() != null
                && validationUtils.isEligibleForErrors(certResult))
            .forEach(certResult -> reviewCriteriaCanHaveSed(listing, certResult));
        listing.getCertificationResults().stream()
            .forEach(certResult -> removeSedIfNotApplicable(certResult));
    }

    private void reviewCriteriaCanHaveSed(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        if (!certResultRules.hasCertOption(certResult.getCriterion().getId(), CertificationResultRules.SED)) {
            if (BooleanUtils.isTrue(certResult.getSed())) {
                listing.addWarningMessage(msgUtil.getMessage(
                    "listing.criteria.sedNotApplicable", Util.formatCriteriaNumber(certResult.getCriterion())));
            }
        }
    }

    private void removeSedIfNotApplicable(CertificationResult certResult) {
        if (!certResultRules.hasCertOption(certResult.getCriterion().getId(), CertificationResultRules.SED)) {
            certResult.setSed(null);
        }
    }

    private void reviewUnusedTasksAndParticipants(CertifiedProductSearchDetails listing) {
        if (listing.getSed() != null
                && !CollectionUtils.isEmpty(listing.getSed().getUnusedTestTaskUniqueIds())) {
            listing.getSed().getUnusedTestTaskUniqueIds().stream()
                .forEach(unusedTestTask -> listing.addWarningMessage(msgUtil.getMessage("listing.sed.unusedTestTask", unusedTestTask)));
        }
        if (listing.getSed() != null
                && !CollectionUtils.isEmpty(listing.getSed().getUnusedTestParticipantUniqueIds())) {
            listing.getSed().getUnusedTestParticipantUniqueIds().stream()
                .forEach(unusedTestParticipant -> listing.addWarningMessage(msgUtil.getMessage("listing.sed.unusedTestParticipant", unusedTestParticipant)));
        }
    }
}

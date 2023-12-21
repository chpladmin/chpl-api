package gov.healthit.chpl.upload.listing.validation.reviewer;

import java.time.LocalDate;
import java.util.Iterator;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.testtool.CertificationResultTestTool;
import gov.healthit.chpl.testtool.TestTool;
import gov.healthit.chpl.testtool.TestToolDAO;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import gov.healthit.chpl.util.ValidationUtils;
import gov.healthit.chpl.validation.listing.reviewer.Reviewer;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component("listingUploadTestToolReviewer")
public class TestToolReviewer implements Reviewer {
    private CertificationResultRules certResultRules;
    private ValidationUtils validationUtils;
    private ErrorMessageUtil msgUtil;
    private ChplProductNumberUtil chplProductNumberUtil;
    private TestToolDAO testToolDao;

    @Autowired
    public TestToolReviewer(CertificationResultRules certResultRules,
            ValidationUtils validationUtils,
            ChplProductNumberUtil chplProductNumberUtil,
            ErrorMessageUtil msgUtil, TestToolDAO testToolDAO) throws EntityRetrievalException {
        this.certResultRules = certResultRules;
        this.validationUtils = validationUtils;
        this.chplProductNumberUtil = chplProductNumberUtil;
        this.msgUtil = msgUtil;
        this.testToolDao = testToolDAO;
    }

    @Override
    public void review(CertifiedProductSearchDetails listing) {
        listing.getCertificationResults().stream()
                .filter(certResult -> validationUtils.isEligibleForErrors(certResult))
                .forEach(certResult -> review(listing, certResult));
        listing.getCertificationResults().stream()
                .forEach(certResult -> removeTestToolsIfNotApplicable(certResult));
    }

    private void review(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        reviewCriteriaCanHaveTestToolData(listing, certResult);
        removeTestToolsWithoutIds(listing, certResult);
        reviewTestToolsRequiredWhenCertResultIsNotGap(listing, certResult);
        if (!CollectionUtils.isEmpty(certResult.getTestToolsUsed())) {
            certResult.getTestToolsUsed().stream()
                    .forEach(testTool -> reviewTestToolFields(listing, certResult, testTool));
        }
    }

    private void reviewCriteriaCanHaveTestToolData(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        if (!certResultRules.hasCertOption(certResult.getCriterion().getId(), CertificationResultRules.TEST_TOOLS_USED)) {
            if (!CollectionUtils.isEmpty(certResult.getTestToolsUsed())) {
                listing.addWarningMessage(msgUtil.getMessage(
                        "listing.criteria.testToolsNotApplicable", Util.formatCriteriaNumber(certResult.getCriterion())));
            }
            certResult.setTestToolsUsed(null);
        }
    }

    private void removeTestToolsIfNotApplicable(CertificationResult certResult) {
        if (!certResultRules.hasCertOption(certResult.getCriterion().getId(), CertificationResultRules.TEST_TOOLS_USED)) {
            certResult.setTestToolsUsed(null);
        }
    }

    private void removeTestToolsWithoutIds(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        if (CollectionUtils.isEmpty(certResult.getTestToolsUsed())) {
            return;
        }
        Iterator<CertificationResultTestTool> testToolIter = certResult.getTestToolsUsed().iterator();
        while (testToolIter.hasNext()) {
            CertificationResultTestTool testTool = testToolIter.next();
            if (testTool.getTestTool().getId() == null) {
                testToolIter.remove();
                listing.addWarningMessage(msgUtil.getMessage("listing.criteria.testToolNotFoundAndRemoved",
                        Util.formatCriteriaNumber(certResult.getCriterion()), testTool.getTestTool().getValue()));
            }
        }
    }

    private void reviewTestToolsRequiredWhenCertResultIsNotGap(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        if (!isGapEligibileAndHasGap(certResult)
                && certResultRules.hasCertOption(certResult.getCriterion().getId(), CertificationResultRules.TEST_TOOLS_USED)
                && CollectionUtils.isEmpty(certResult.getTestToolsUsed())) {
            listing.addDataErrorMessage(msgUtil.getMessage(
                    "listing.criteria.missingTestTool",
                    Util.formatCriteriaNumber(certResult.getCriterion())));
        }
    }

    private boolean isGapEligibileAndHasGap(CertificationResult certResult) {
        boolean result = false;
        if (certResultRules.hasCertOption(certResult.getCriterion().getId(), CertificationResultRules.GAP)
                && certResult.getGap() != null && certResult.getGap()) {
            result = true;
        }
        return result;
    }

    private void reviewTestToolFields(CertifiedProductSearchDetails listing,
            CertificationResult certResult, CertificationResultTestTool testTool) {
        reviewNameAndVersionRequired(listing, certResult, testTool);
        reviewTestToolRetiredBeforeListingActiveDatesUnlessIcs(listing, certResult, testTool);
        reviewTestToolAvailabilityAfterListingActiveDates(listing, certResult, testTool);
        reviewTestToolValidForCriteria(listing, certResult, testTool);
    }

    private void reviewNameAndVersionRequired(CertifiedProductSearchDetails listing,
            CertificationResult certResult, CertificationResultTestTool testTool) {
        if (StringUtils.isEmpty(testTool.getTestTool().getValue())) {
            listing.addDataErrorMessage(msgUtil.getMessage("listing.criteria.missingTestToolName",
                    Util.formatCriteriaNumber(certResult.getCriterion())));
        } else if (!StringUtils.isEmpty(testTool.getTestTool().getValue())
                && StringUtils.isEmpty(testTool.getVersion())) {
            // require test tool version if a test tool name was entered
            listing.addDataErrorMessage(msgUtil.getMessage("listing.criteria.missingTestToolVersion",
                    testTool.getTestTool().getValue(), Util.formatCriteriaNumber(certResult.getCriterion())));
        }
    }

    private void reviewTestToolRetiredBeforeListingActiveDatesUnlessIcs(CertifiedProductSearchDetails listing,
            CertificationResult certResult, CertificationResultTestTool testTool) {
        if (testTool.getTestTool().getId() != null
                && isTestToolRetiredBeforeListingActiveDates(listing, testTool.getTestTool())
                && (!hasIcs(listing) || hasIcsMismatch(listing))) {
            listing.addDataErrorMessage(msgUtil.getMessage(
                    "listing.criteria.retiredTestToolNoIcsNotAllowed",
                    testTool.getTestTool().getValue(),
                    Util.formatCriteriaNumber(certResult.getCriterion())));
        }
    }

    private Boolean hasIcs(CertifiedProductSearchDetails listing) {
        Integer icsCodeInteger = chplProductNumberUtil.getIcsCode(listing.getChplProductNumber());
        return listing.getIcs() != null && listing.getIcs().getInherits() != null
                && listing.getIcs().getInherits() && !icsCodeInteger.equals(0);
    }

    private Boolean hasIcsMismatch(CertifiedProductSearchDetails listing) {
        boolean icsBoolean = false;
        if (listing.getIcs() != null && listing.getIcs().getInherits() != null
                && listing.getIcs().getInherits()) {
            icsBoolean = true;
        }
        return chplProductNumberUtil.hasIcsConflict(listing.getChplProductNumber(), icsBoolean);
    }

    private void reviewTestToolValidForCriteria(CertifiedProductSearchDetails listing, CertificationResult certResult, CertificationResultTestTool testTool) {
        if (!isTestToolValidForCriteria(certResult.getCriterion(), testTool)) {
            listing.addDataErrorMessage(msgUtil.getMessage("listing.criteria.testToolCriterionMismatch",
                    testTool.getTestTool().getValue(), Util.formatCriteriaNumber(certResult.getCriterion())));
        }
    }

    private Boolean isTestToolValidForCriteria(CertificationCriterion criterion, CertificationResultTestTool certResultTestTool) {
        try {
            return testToolDao.getAllTestToolCriteriaMaps().stream()
                    .filter(ttcm -> ttcm.getCriterion().getId().equals(criterion.getId())
                            && ttcm.getTestTool().getId().equals(certResultTestTool.getTestTool().getId()))
                    .findAny()
                    .isPresent();
        } catch (EntityRetrievalException e) {
            LOGGER.error("Could not validate Test Tool: {}", certResultTestTool.getTestTool().getValue());
            return false;
        }
    }

    private void reviewTestToolAvailabilityAfterListingActiveDates(CertifiedProductSearchDetails listing, CertificationResult certResult, CertificationResultTestTool testTool) {
        if (isTestToolActiveAfterListingActiveDates(listing, testTool.getTestTool())) {
            listing.addBusinessErrorMessage(msgUtil.getMessage("listing.criteria.testToolUnavailable",
                    testTool.getTestTool().getValue(),
                    Util.formatCriteriaNumber(certResult.getCriterion())));
        }
    }

    private boolean isTestToolRetiredBeforeListingActiveDates(CertifiedProductSearchDetails listing, TestTool testTool) {
        LocalDate listingStartDay = listing.getCertificationDay();
        LocalDate testToolEndDay = testTool.getEndDay() == null ? LocalDate.MAX : testTool.getEndDay();
        return testToolEndDay.isBefore(listingStartDay);
    }

    private boolean isTestToolActiveAfterListingActiveDates(CertifiedProductSearchDetails listing, TestTool testTool) {
        LocalDate listingEndDay = listing.getDecertificationDay() == null ? LocalDate.now() : listing.getDecertificationDay();
        LocalDate testToolStartDay = testTool.getStartDay() == null ? LocalDate.MIN : testTool.getStartDay();
        return testToolStartDay.isAfter(listingEndDay);
    }
}

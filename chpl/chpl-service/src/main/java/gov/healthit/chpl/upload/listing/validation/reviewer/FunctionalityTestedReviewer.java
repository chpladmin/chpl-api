package gov.healthit.chpl.upload.listing.validation.reviewer;

import java.time.LocalDate;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.functionalitytested.CertificationResultFunctionalityTested;
import gov.healthit.chpl.functionalitytested.FunctionalityTested;
import gov.healthit.chpl.functionalitytested.FunctionalityTestedDAO;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import gov.healthit.chpl.util.ValidationUtils;

@Component("listingUploadFunctionalityTestedReviewer")
public class FunctionalityTestedReviewer {
    private CertificationResultRules certResultRules;
    private ValidationUtils validationUtils;
    private FunctionalityTestedDAO functionalityTestedDao;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public FunctionalityTestedReviewer(CertificationResultRules certResultRules,
            ValidationUtils validationUtils,
            FunctionalityTestedDAO functionalityTestedDao, ErrorMessageUtil msgUtil) {
        this.certResultRules = certResultRules;
        this.validationUtils = validationUtils;
        this.functionalityTestedDao = functionalityTestedDao;
        this.msgUtil = msgUtil;
    }

    public void review(CertifiedProductSearchDetails listing) {
        listing.getCertificationResults().stream()
                .filter(certResult -> validationUtils.isEligibleForErrors(certResult))
                .forEach(certResult -> review(listing, certResult));
        listing.getCertificationResults().stream()
                .forEach(certResult -> removeFunctionalitiesTestedIfNotApplicable(certResult));
    }

    private void review(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        reviewCriteriaCanHaveFunctionalitiesTested(listing, certResult);
        removeFunctionalitiesTestedWithoutIds(listing, certResult);
        removeFunctionalitiesTestedMismatchedToCriteria(listing, certResult);
        if (certResult.getFunctionalitiesTested() != null && certResult.getFunctionalitiesTested().size() > 0) {
            certResult.getFunctionalitiesTested().stream()
                    .forEach(functionalityTested -> reviewFunctionalityTestedFields(listing, certResult, functionalityTested));
        }
    }

    private void reviewCriteriaCanHaveFunctionalitiesTested(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        if (!certResultRules.hasCertOption(certResult.getCriterion().getId(), CertificationResultRules.FUNCTIONALITY_TESTED)) {
            if (!CollectionUtils.isEmpty(certResult.getFunctionalitiesTested())) {
                listing.addWarningMessage(msgUtil.getMessage(
                        "listing.criteria.functionalityTestedNotApplicable", Util.formatCriteriaNumber(certResult.getCriterion())));
            }
            certResult.setFunctionalitiesTested(null);
        }
    }

    private void removeFunctionalitiesTestedIfNotApplicable(CertificationResult certResult) {
        if (!certResultRules.hasCertOption(certResult.getCriterion().getId(), CertificationResultRules.FUNCTIONALITY_TESTED)) {
            certResult.setFunctionalitiesTested(null);
        }
    }

    private void removeFunctionalitiesTestedWithoutIds(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        if (CollectionUtils.isEmpty(certResult.getFunctionalitiesTested())) {
            return;
        }
        Iterator<CertificationResultFunctionalityTested> functionalitiesTestedIter = certResult.getFunctionalitiesTested().iterator();
        while (functionalitiesTestedIter.hasNext()) {
            CertificationResultFunctionalityTested functionalityTested = functionalitiesTestedIter.next();
            if (functionalityTested.getFunctionalityTested().getId() == null) {
                functionalitiesTestedIter.remove();
                listing.addWarningMessage(msgUtil.getMessage(
                        "listing.criteria.functionalityTestedNotFoundAndRemoved",
                        Util.formatCriteriaNumber(certResult.getCriterion()), functionalityTested.getFunctionalityTested().getRegulatoryTextCitation()));
            }
        }
    }

    private void removeFunctionalitiesTestedMismatchedToCriteria(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        if (CollectionUtils.isEmpty(certResult.getFunctionalitiesTested())) {
            return;
        }
        Iterator<CertificationResultFunctionalityTested> functionalitiesTestedIter = certResult.getFunctionalitiesTested().iterator();
        while (functionalitiesTestedIter.hasNext()) {
            CertificationResultFunctionalityTested functionalityTested = functionalitiesTestedIter.next();
            if (!isFunctionalityTestedCritierionValid(certResult.getCriterion().getId(),
                    functionalityTested.getFunctionalityTested().getId())) {
                functionalitiesTestedIter.remove();
                listing.addWarningMessage(msgUtil.getMessage("listing.criteria.functionalityTestedCriterionMismatch",
                        Util.formatCriteriaNumber(certResult.getCriterion()),
                        functionalityTested.getFunctionalityTested().getRegulatoryTextCitation(),
                        getDelimitedListOfValidCriteriaNumbers(functionalityTested),
                        Util.formatCriteriaNumber(certResult.getCriterion())));
            }
        }
    }

    private boolean isFunctionalityTestedCritierionValid(Long criteriaId, Long functionalityTestedId) {
        List<FunctionalityTested> validFunctionalitiesTestedForCriteria = functionalityTestedDao.getFunctionalitiesTestedCriteriaMaps().get(criteriaId);
        if (validFunctionalitiesTestedForCriteria == null) {
            return false;
        } else {
            return validFunctionalitiesTestedForCriteria.stream().filter(validTf -> validTf.getId().equals(functionalityTestedId)).count() > 0;
        }
    }

    private String getDelimitedListOfValidCriteriaNumbers(CertificationResultFunctionalityTested crft) {
        FunctionalityTested functionalityTested = null;
        functionalityTested = functionalityTestedDao.getById(crft.getFunctionalityTested().getId());

        List<String> criteriaNumbers = functionalityTested.getCriteria().stream()
                .map(criterion -> Util.formatCriteriaNumber(criterion))
                .collect(Collectors.toList());
        return Util.joinListGrammatically(criteriaNumbers);
    }

    private void reviewFunctionalityTestedFields(CertifiedProductSearchDetails listing,
            CertificationResult certResult, CertificationResultFunctionalityTested functionalityTested) {
        reviewFunctionalityTestedName(listing, certResult, functionalityTested);
        reviewFunctionalityTestedRetiredBeforeListingActiveDates(listing, certResult, functionalityTested);
        reviewFunctionalityTestedAvailabilityAfterListingActiveDates(listing, certResult, functionalityTested);
    }

    private void reviewFunctionalityTestedName(CertifiedProductSearchDetails listing, CertificationResult certResult, CertificationResultFunctionalityTested functionalityTested) {
        if (StringUtils.isEmpty(functionalityTested.getFunctionalityTested().getRegulatoryTextCitation())) {
            listing.addDataErrorMessage(msgUtil.getMessage("listing.criteria.missingFunctionalityTestedName",
                    Util.formatCriteriaNumber(certResult.getCriterion())));
        }
    }

    private void reviewFunctionalityTestedRetiredBeforeListingActiveDates(CertifiedProductSearchDetails listing,
            CertificationResult certResult, CertificationResultFunctionalityTested functionalityTested) {
        if (isFunctionalityTestedRetiredBeforeListingActiveDates(listing, functionalityTested.getFunctionalityTested())) {
            listing.addBusinessErrorMessage(msgUtil.getMessage("listing.criteria.functionalityTestedUnavailable",
                    functionalityTested.getFunctionalityTested().getValue(),
                    Util.formatCriteriaNumber(certResult.getCriterion())));
        }
    }

    private void reviewFunctionalityTestedAvailabilityAfterListingActiveDates(CertifiedProductSearchDetails listing,
            CertificationResult certResult, CertificationResultFunctionalityTested functionalityTested) {
        if (isFunctionalityTestedActiveAfterListingActiveDates(listing, functionalityTested.getFunctionalityTested())) {
            listing.addBusinessErrorMessage(msgUtil.getMessage("listing.criteria.functionalityTestedUnavailable",
                    functionalityTested.getFunctionalityTested().getValue(),
                    Util.formatCriteriaNumber(certResult.getCriterion())));
        }
    }

    private boolean isFunctionalityTestedRetiredBeforeListingActiveDates(CertifiedProductSearchDetails listing, FunctionalityTested functionalityTested) {
        LocalDate listingStartDay = listing.getCertificationDay();
        LocalDate funcTestedEndDay = functionalityTested.getEndDay() == null ? LocalDate.MAX : functionalityTested.getEndDay();
        return funcTestedEndDay.isBefore(listingStartDay);
    }

    private boolean isFunctionalityTestedActiveAfterListingActiveDates(CertifiedProductSearchDetails listing, FunctionalityTested functionalityTested) {
        LocalDate listingEndDay = listing.getDecertificationDay() == null ? LocalDate.now() : listing.getDecertificationDay();
        LocalDate funcTestedStartDay = functionalityTested.getStartDay() == null ? LocalDate.MIN : functionalityTested.getStartDay();
        return funcTestedStartDay.isAfter(listingEndDay);
    }
}

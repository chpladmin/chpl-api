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
import gov.healthit.chpl.standard.CertificationResultStandard;
import gov.healthit.chpl.standard.Standard;
import gov.healthit.chpl.standard.StandardDAO;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import gov.healthit.chpl.util.ValidationUtils;

@Component("listingUploadStandardReviewer")
public class StandardReviewer {
    private CertificationResultRules certResultRules;
    private ValidationUtils validationUtils;
    private StandardDAO standardDao;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public StandardReviewer(CertificationResultRules certResultRules,
            ValidationUtils validationUtils,
            StandardDAO standardDao, ErrorMessageUtil msgUtil) {
        this.certResultRules = certResultRules;
        this.validationUtils = validationUtils;
        this.standardDao = standardDao;
        this.msgUtil = msgUtil;
    }

    public void review(CertifiedProductSearchDetails listing) {
        listing.getCertificationResults().stream()
                .filter(certResult -> validationUtils.isEligibleForErrors(certResult))
                .forEach(certResult -> review(listing, certResult));
        listing.getCertificationResults().stream()
                .forEach(certResult -> removeStandardIfNotApplicable(certResult));
    }

    private void review(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        reviewCriteriaCanHaveStandard(listing, certResult);
        removeStandardsWithoutIds(listing, certResult);
        removeStandardMismatchedToCriteria(listing, certResult);
        if (certResult.getStandards() != null && certResult.getStandards().size() > 0) {
            certResult.getStandards().stream()
                    .forEach(standard -> reviewStandardFields(listing, certResult, standard));
        }
    }

    private void reviewCriteriaCanHaveStandard(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        if (!certResultRules.hasCertOption(certResult.getCriterion().getId(), CertificationResultRules.STANDARD)) {
            if (!CollectionUtils.isEmpty(certResult.getStandards())) {
                listing.addWarningMessage(msgUtil.getMessage(
                        "listing.criteria.standardNotApplicable", Util.formatCriteriaNumber(certResult.getCriterion())));
            }
            certResult.setStandards(null);
        }
    }

    private void removeStandardIfNotApplicable(CertificationResult certResult) {
        if (!certResultRules.hasCertOption(certResult.getCriterion().getId(), CertificationResultRules.STANDARD)) {
            certResult.setStandards(null);
        }
    }

    private void removeStandardsWithoutIds(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        if (CollectionUtils.isEmpty(certResult.getStandards())) {
            return;
        }
        Iterator<CertificationResultStandard> standardsIter = certResult.getStandards().iterator();
        while (standardsIter.hasNext()) {
            CertificationResultStandard standard = standardsIter.next();
            if (standard.getStandard().getId() == null) {
                standardsIter.remove();
                listing.addWarningMessage(msgUtil.getMessage(
                        "listing.criteria.standardNotFoundAndRemoved",
                        Util.formatCriteriaNumber(certResult.getCriterion()), standard.getStandard().getRegulatoryTextCitation()));
            }
        }
    }

    private void removeStandardMismatchedToCriteria(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        if (CollectionUtils.isEmpty(certResult.getFunctionalitiesTested())) {
            return;
        }
        Iterator<CertificationResultStandard> standardIter = certResult.getStandards().iterator();
        while (standardIter.hasNext()) {
            CertificationResultStandard standard = standardIter.next();
            if (!isStandardCritierionValid(certResult.getCriterion().getId(),
                    standard.getStandard().getId())) {
                standardIter.remove();
                listing.addWarningMessage(msgUtil.getMessage("listing.criteria.standardCriterionMismatch",
                        Util.formatCriteriaNumber(certResult.getCriterion()),
                        standard.getStandard().getRegulatoryTextCitation(),
                        getDelimitedListOfValidCriteriaNumbers(standard),
                        Util.formatCriteriaNumber(certResult.getCriterion())));
            }
        }
    }

    private boolean isStandardCritierionValid(Long criteriaId, Long standardId) {
        List<Standard> validStandardForCriteria = standardDao.getStandardCriteriaMaps().get(criteriaId);
        if (validStandardForCriteria == null) {
            return false;
        } else {
            return validStandardForCriteria.stream().filter(validTf -> validTf.getId().equals(standardId)).count() > 0;
        }
    }

    private String getDelimitedListOfValidCriteriaNumbers(CertificationResultStandard crs) {
        Standard standard = null;
        standard = standardDao.getById(crs.getStandard().getId());

        List<String> criteriaNumbers = standard.getCriteria().stream()
                .map(criterion -> Util.formatCriteriaNumber(criterion))
                .collect(Collectors.toList());
        return Util.joinListGrammatically(criteriaNumbers);
    }

    private void reviewStandardFields(CertifiedProductSearchDetails listing,
            CertificationResult certResult, CertificationResultStandard standard) {
        reviewStandardName(listing, certResult, standard);
        reviewStandardRetiredBeforeListingActiveDates(listing, certResult, standard);
        reviewStandardAvailabilityAfterListingActiveDates(listing, certResult, standard);
    }

    private void reviewStandardName(CertifiedProductSearchDetails listing, CertificationResult certResult, CertificationResultStandard standard) {
        if (StringUtils.isEmpty(standard.getStandard().getRegulatoryTextCitation())) {
            listing.addDataErrorMessage(msgUtil.getMessage("listing.criteria.missingStandardName",
                    Util.formatCriteriaNumber(certResult.getCriterion())));
        }
    }

    private void reviewStandardRetiredBeforeListingActiveDates(CertifiedProductSearchDetails listing,
            CertificationResult certResult, CertificationResultStandard standard) {
        if (isStandardRetiredBeforeListingActiveDates(listing, standard.getStandard())) {
            listing.addBusinessErrorMessage(msgUtil.getMessage("listing.criteria.standardUnavailable",
                    standard.getStandard().getValue(),
                    Util.formatCriteriaNumber(certResult.getCriterion())));
        }
    }

    private void reviewStandardAvailabilityAfterListingActiveDates(CertifiedProductSearchDetails listing,
            CertificationResult certResult, CertificationResultStandard standard) {
        if (isStandardActiveAfterListingActiveDates(listing, standard.getStandard())) {
            listing.addBusinessErrorMessage(msgUtil.getMessage("listing.criteria.standardUnavailable",
                    standard.getStandard().getValue(),
                    Util.formatCriteriaNumber(certResult.getCriterion())));
        }
    }

    private boolean isStandardRetiredBeforeListingActiveDates(CertifiedProductSearchDetails listing, Standard standard) {
        LocalDate listingStartDay = listing.getCertificationDay();
        LocalDate standardEndDay = standard.getEndDay() == null ? LocalDate.MAX : standard.getEndDay();
        return standardEndDay.isBefore(listingStartDay);
    }

    private boolean isStandardActiveAfterListingActiveDates(CertifiedProductSearchDetails listing, Standard standard) {
        LocalDate listingEndDay = listing.getDecertificationDay() == null ? LocalDate.now() : listing.getDecertificationDay();
        LocalDate standardStartDay = standard.getStartDay() == null ? LocalDate.MIN : standard.getStartDay();
        return standardStartDay.isAfter(listingEndDay);
    }

}

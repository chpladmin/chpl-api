package gov.healthit.chpl.upload.listing.validation.reviewer;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.functionalityTested.CertificationResultFunctionalityTested;
import gov.healthit.chpl.functionalityTested.FunctionalityTested;
import gov.healthit.chpl.functionalityTested.FunctionalityTestedCriteriaMap;
import gov.healthit.chpl.functionalityTested.FunctionalityTestedDAO;
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
        if (!certResultRules.hasCertOption(certResult.getCriterion().getNumber(), CertificationResultRules.FUNCTIONALITY_TESTED)) {
            if (!CollectionUtils.isEmpty(certResult.getFunctionalitiesTested())) {
                listing.getWarningMessages().add(msgUtil.getMessage(
                    "listing.criteria.functionalityTestedNotApplicable", Util.formatCriteriaNumber(certResult.getCriterion())));
            }
            certResult.setFunctionalitiesTested(null);
        }
    }

    private void removeFunctionalitiesTestedIfNotApplicable(CertificationResult certResult) {
        if (!certResultRules.hasCertOption(certResult.getCriterion().getNumber(), CertificationResultRules.FUNCTIONALITY_TESTED)) {
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
            if (functionalityTested.getFunctionalityTestedId() == null) {
                functionalitiesTestedIter.remove();
                listing.getWarningMessages().add(msgUtil.getMessage(
                        "listing.criteria.functionalityTestedNotFoundAndRemoved",
                        Util.formatCriteriaNumber(certResult.getCriterion()), functionalityTested.getName()));
            }
        }
    }

    private void removeFunctionalitiesTestedMismatchedToCriteria(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        if (CollectionUtils.isEmpty(certResult.getFunctionalitiesTested())) {
            return;
        }
        String year = MapUtils.getString(listing.getCertificationEdition(), CertifiedProductSearchDetails.EDITION_NAME_KEY);
        Iterator<CertificationResultFunctionalityTested> functionalitiesTestedIter = certResult.getFunctionalitiesTested().iterator();
        while (functionalitiesTestedIter.hasNext()) {
            CertificationResultFunctionalityTested functionalityTested = functionalitiesTestedIter.next();
            if (!isFunctionalityTestedCritierionValid(certResult.getCriterion().getId(),
                    functionalityTested.getFunctionalityTestedId(), year)) {
                functionalitiesTestedIter.remove();
                listing.getWarningMessages().add(msgUtil.getMessage("listing.criteria.functionalityTestedCriterionMismatch",
                            Util.formatCriteriaNumber(certResult.getCriterion()),
                            functionalityTested.getName(),
                            getDelimitedListOfValidCriteriaNumbers(functionalityTested.getFunctionalityTestedId(), year),
                            Util.formatCriteriaNumber(certResult.getCriterion())));
            }
        }
    }

    private boolean isFunctionalityTestedCritierionValid(Long criteriaId, Long functionalityTestedId, String year) {
        List<FunctionalityTested> validFunctionalitiesTestedForCriteria =
                functionalityTestedDao.getFunctionalitiesTestedCriteriaMaps(year).get(criteriaId);
        if (validFunctionalitiesTestedForCriteria == null) {
            return false;
        } else {
            return validFunctionalitiesTestedForCriteria.stream().filter(validTf -> validTf.getId().equals(functionalityTestedId)).count() > 0;
        }
    }

    private String getDelimitedListOfValidCriteriaNumbers(Long functionalityTestedId, String year) {
        List<FunctionalityTestedCriteriaMap> functionalityTestedMaps = functionalityTestedDao.getFunctionalitiesTestedCritieriaMaps();
        return functionalityTestedMaps.stream().
            filter(functionalityTestedMap -> functionalityTestedMap.getCriterion().getCertificationEdition().equals(year)
                    && functionalityTestedId.equals(functionalityTestedMap.getFunctionalityTested().getId()))
            .map(functionalityTestedMap -> Util.formatCriteriaNumber(functionalityTestedMap.getCriterion()))
            .collect(Collectors.joining(","));
    }

    private void reviewFunctionalityTestedFields(CertifiedProductSearchDetails listing,
            CertificationResult certResult, CertificationResultFunctionalityTested functionalityTested) {
        reviewFunctionalityTestedName(listing, certResult, functionalityTested);
    }

    private void reviewFunctionalityTestedName(CertifiedProductSearchDetails listing,
            CertificationResult certResult, CertificationResultFunctionalityTested FunctionalityTested) {
        if (StringUtils.isEmpty(FunctionalityTested.getName())) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.criteria.missingFunctionalityTestedName",
                    Util.formatCriteriaNumber(certResult.getCriterion())));
        }
    }
}

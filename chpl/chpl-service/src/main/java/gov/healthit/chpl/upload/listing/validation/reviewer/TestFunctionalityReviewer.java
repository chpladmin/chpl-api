package gov.healthit.chpl.upload.listing.validation.reviewer;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.dao.TestFunctionalityDAO;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultTestFunctionality;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.TestFunctionalityCriteriaMapDTO;
import gov.healthit.chpl.dto.TestFunctionalityDTO;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import gov.healthit.chpl.util.ValidationUtils;

@Component("listingUploadTestFunctionalityReviewer")
public class TestFunctionalityReviewer {
    private CertificationResultRules certResultRules;
    private ValidationUtils validationUtils;
    private TestFunctionalityDAO testFunctionalityDao;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public TestFunctionalityReviewer(CertificationResultRules certResultRules,
            ValidationUtils validationUtils,
            TestFunctionalityDAO testFunctionalityDao, ErrorMessageUtil msgUtil) {
        this.certResultRules = certResultRules;
        this.validationUtils = validationUtils;
        this.testFunctionalityDao = testFunctionalityDao;
        this.msgUtil = msgUtil;
    }

    public void review(CertifiedProductSearchDetails listing) {
        listing.getCertificationResults().stream()
            .filter(certResult -> validationUtils.isEligibleForErrors(certResult))
            .forEach(certResult -> review(listing, certResult));
    }

    private void review(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        reviewCriteriaCanHaveTestFunctionalityData(listing, certResult);
        removeTestFunctionalityWithoutIds(listing, certResult);
        removeTestFunctionalityMismatchedToCriteria(listing, certResult);
        if (certResult.getTestFunctionality() != null && certResult.getTestFunctionality().size() > 0) {
            certResult.getTestFunctionality().stream()
                .forEach(testFunc -> reviewTestFunctionalityFields(listing, certResult, testFunc));
        }
    }

    private void reviewCriteriaCanHaveTestFunctionalityData(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        if (!certResultRules.hasCertOption(certResult.getCriterion().getNumber(), CertificationResultRules.FUNCTIONALITY_TESTED)) {
            if (!CollectionUtils.isEmpty(certResult.getTestFunctionality())) {
                listing.getWarningMessages().add(msgUtil.getMessage(
                    "listing.criteria.testFunctionalityNotApplicable", Util.formatCriteriaNumber(certResult.getCriterion())));
            }
            certResult.setTestFunctionality(null);
        }
    }

    private void removeTestFunctionalityWithoutIds(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        if (CollectionUtils.isEmpty(certResult.getTestFunctionality())) {
            return;
        }
        Iterator<CertificationResultTestFunctionality> testFunctionalityIter = certResult.getTestFunctionality().iterator();
        while (testFunctionalityIter.hasNext()) {
            CertificationResultTestFunctionality testFunctionality = testFunctionalityIter.next();
            if (testFunctionality.getTestFunctionalityId() == null) {
                testFunctionalityIter.remove();
                listing.getWarningMessages().add(msgUtil.getMessage(
                        "listing.criteria.testFunctionalityNotFoundAndRemoved",
                        Util.formatCriteriaNumber(certResult.getCriterion()), testFunctionality.getName()));
            }
        }
    }

    private void removeTestFunctionalityMismatchedToCriteria(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        if (CollectionUtils.isEmpty(certResult.getTestFunctionality())) {
            return;
        }
        String year = MapUtils.getString(listing.getCertificationEdition(), CertifiedProductSearchDetails.EDITION_NAME_KEY);
        Iterator<CertificationResultTestFunctionality> testFunctionalityIter = certResult.getTestFunctionality().iterator();
        while (testFunctionalityIter.hasNext()) {
            CertificationResultTestFunctionality testFunctionality = testFunctionalityIter.next();
            if (!isTestFunctionalityCritierionValid(certResult.getCriterion().getId(),
                    testFunctionality.getTestFunctionalityId(), year)) {
                testFunctionalityIter.remove();
                listing.getWarningMessages().add(msgUtil.getMessage("listing.criteria.testFunctionalityCriterionMismatch",
                            Util.formatCriteriaNumber(certResult.getCriterion()),
                            testFunctionality.getName(),
                            getDelimitedListOfValidCriteriaNumbers(testFunctionality.getTestFunctionalityId(), year),
                            Util.formatCriteriaNumber(certResult.getCriterion())));
            }
        }
    }

    private boolean isTestFunctionalityCritierionValid(Long criteriaId, Long testFunctionalityId, String year) {
        List<TestFunctionalityDTO> validTestFunctionalityForCriteria =
                testFunctionalityDao.getTestFunctionalityCriteriaMaps(year).get(criteriaId);
        if (validTestFunctionalityForCriteria == null) {
            return false;
        } else {
            return validTestFunctionalityForCriteria.stream().filter(validTf -> validTf.getId().equals(testFunctionalityId)).count() > 0;
        }
    }

    private String getDelimitedListOfValidCriteriaNumbers(Long testFunctionalityId, String year) {
        List<TestFunctionalityCriteriaMapDTO> testFunctionalityMaps = testFunctionalityDao.getTestFunctionalityCritieriaMaps();
        return testFunctionalityMaps.stream().
            filter(testFunctionalityMap -> testFunctionalityMap.getCriteria().getCertificationEdition().equals(year)
                    && testFunctionalityId.equals(testFunctionalityMap.getTestFunctionality().getId()))
            .map(testFunctionalityMap -> Util.formatCriteriaNumber(testFunctionalityMap.getCriteria()))
            .collect(Collectors.joining(","));
    }

    private void reviewTestFunctionalityFields(CertifiedProductSearchDetails listing,
            CertificationResult certResult, CertificationResultTestFunctionality testFunctionality) {
        reviewTestFunctionalityName(listing, certResult, testFunctionality);
    }

    private void reviewTestFunctionalityName(CertifiedProductSearchDetails listing,
            CertificationResult certResult, CertificationResultTestFunctionality testFunctionality) {
        if (StringUtils.isEmpty(testFunctionality.getName())) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.criteria.missingTestFunctionalityName",
                    Util.formatCriteriaNumber(certResult.getCriterion())));
        }
    }
}

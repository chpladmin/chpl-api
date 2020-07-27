package gov.healthit.chpl.validation.listing.reviewer.duplicate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultTestData;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import gov.healthit.chpl.validation.DuplicateReviewResult;

@Component("testDataDuplicateReviewer")
public class TestDataDuplicateReviewer {
    private ErrorMessageUtil errorMessageUtil;

    @Autowired
    public TestDataDuplicateReviewer(ErrorMessageUtil errorMessageUtil) {
        this.errorMessageUtil = errorMessageUtil;
    }

    public void review(CertifiedProductSearchDetails listing, CertificationResult certificationResult) {

        DuplicateReviewResult<CertificationResultTestData> testDataDuplicateResults =
                new DuplicateReviewResult<CertificationResultTestData>(duplicatePredicate());
        if (certificationResult.getTestDataUsed() != null) {
            for (CertificationResultTestData dto : certificationResult.getTestDataUsed()) {
                testDataDuplicateResults.addObject(dto);
            }
        }
        if (testDataDuplicateResults.duplicatesExist()) {
            listing.getWarningMessages().addAll(
                    getWarnings(testDataDuplicateResults.getDuplicateList(),
                            Util.formatCriteriaNumber(certificationResult.getCriterion())));
            certificationResult.setTestDataUsed(testDataDuplicateResults.getUniqueList());
        }

        DuplicateReviewResult<CertificationResultTestData> testDataDuplicateIdResults =
                new DuplicateReviewResult<CertificationResultTestData>(duplicateIdPredicate());
        if (certificationResult.getTestDataUsed() != null) {
            for (CertificationResultTestData dto : certificationResult.getTestDataUsed()) {
                testDataDuplicateIdResults.addObject(dto);
            }
        }
        if (testDataDuplicateIdResults.duplicatesExist()) {
            listing.getErrorMessages().addAll(
                    getErrors(testDataDuplicateIdResults.getDuplicateList(),
                            Util.formatCriteriaNumber(certificationResult.getCriterion())));
        }
    }

    private List<String> getErrors(List<CertificationResultTestData> duplicates, String criteria) {
        List<String> errors = new ArrayList<String>();
        for (CertificationResultTestData duplicate : duplicates) {
            String error = errorMessageUtil.getMessage("listing.criteria.duplicateTestDataName",
                    criteria, duplicate.getTestData().getName());
            errors.add(error);
        }
        return errors;
    }

    private List<String> getWarnings(List<CertificationResultTestData> duplicates, String criteria) {
        List<String> warnings = new ArrayList<String>();
        for (CertificationResultTestData duplicate : duplicates) {
            String warning = errorMessageUtil.getMessage("listing.criteria.duplicateTestDataNameAndVersion",
                        criteria, duplicate.getTestData().getName(),
                        duplicate.getVersion() == null ? "" : duplicate.getVersion());
            warnings.add(warning);
        }
        return warnings;
    }

    private BiPredicate<CertificationResultTestData, CertificationResultTestData> duplicatePredicate() {
        return new BiPredicate<CertificationResultTestData, CertificationResultTestData>() {
            @Override
            public boolean test(CertificationResultTestData dto1,
                    CertificationResultTestData dto2) {
                return Objects.equals(dto1.getTestData().getId(), dto2.getTestData().getId())
                        && Objects.equals(dto1.getVersion(), dto2.getVersion());
            }
        };
    }

    private BiPredicate<CertificationResultTestData, CertificationResultTestData> duplicateIdPredicate() {
        return new BiPredicate<CertificationResultTestData, CertificationResultTestData>() {
            @Override
            public boolean test(CertificationResultTestData dto1,
                    CertificationResultTestData dto2) {
                return Objects.equals(dto1.getTestData().getId(), dto2.getTestData().getId())
                        && !Objects.equals(dto1.getVersion(), dto2.getVersion());
            }
        };
    }
}

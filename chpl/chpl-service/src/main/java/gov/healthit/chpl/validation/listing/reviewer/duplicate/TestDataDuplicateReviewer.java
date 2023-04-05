package gov.healthit.chpl.validation.listing.reviewer.duplicate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiPredicate;

import org.apache.commons.lang3.ObjectUtils;
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

        DuplicateReviewResult<CertificationResultTestData> testDataDuplicateResults = new DuplicateReviewResult<CertificationResultTestData>(duplicatePredicate());
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

        DuplicateReviewResult<CertificationResultTestData> testDataDuplicateIdResults = new DuplicateReviewResult<CertificationResultTestData>(duplicateIdPredicate());
        if (certificationResult.getTestDataUsed() != null) {
            for (CertificationResultTestData dto : certificationResult.getTestDataUsed()) {
                testDataDuplicateIdResults.addObject(dto);
            }
        }
        if (testDataDuplicateIdResults.duplicatesExist()) {
            listing.addAllBusinessErrorMessages(
                    getErrors(testDataDuplicateIdResults.getDuplicateList(),
                            Util.formatCriteriaNumber(certificationResult.getCriterion())));
        }
    }

    private Set<String> getErrors(List<CertificationResultTestData> duplicates, String criteria) {
        Set<String> errors = new HashSet<String>();
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
            public boolean test(CertificationResultTestData td1,
                    CertificationResultTestData td2) {
                return ObjectUtils.allNotNull(td1.getTestData(), td1.getTestData().getId(),
                        td2.getTestData(), td2.getTestData().getId())
                        && Objects.equals(td1.getTestData().getId(), td2.getTestData().getId())
                        && Objects.equals(td1.getVersion(), td2.getVersion());
            }
        };
    }

    private BiPredicate<CertificationResultTestData, CertificationResultTestData> duplicateIdPredicate() {
        return new BiPredicate<CertificationResultTestData, CertificationResultTestData>() {
            @Override
            public boolean test(CertificationResultTestData td1,
                    CertificationResultTestData td2) {
                return ObjectUtils.allNotNull(td1.getTestData(), td1.getTestData().getId(),
                        td2.getTestData(), td2.getTestData().getId())
                        && Objects.equals(td1.getTestData().getId(), td2.getTestData().getId())
                        && !Objects.equals(td1.getVersion(), td2.getVersion());
            }
        };
    }
}

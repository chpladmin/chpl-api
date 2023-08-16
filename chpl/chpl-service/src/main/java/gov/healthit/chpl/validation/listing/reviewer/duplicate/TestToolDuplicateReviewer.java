package gov.healthit.chpl.validation.listing.reviewer.duplicate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.criteriaattribute.testtool.CertificationResultTestTool;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import gov.healthit.chpl.validation.DuplicateReviewResult;

@Component("testToolDuplicateReviewer")
public class TestToolDuplicateReviewer {
    private ErrorMessageUtil errorMessageUtil;

    @Autowired
    public TestToolDuplicateReviewer(ErrorMessageUtil errorMessageUtil) {
        this.errorMessageUtil = errorMessageUtil;
    }

    public void review(CertifiedProductSearchDetails listing, CertificationResult certificationResult) {

        DuplicateReviewResult<CertificationResultTestTool> testToolDuplicateResults = new DuplicateReviewResult<CertificationResultTestTool>(duplicatePredicate());
        if (certificationResult.getTestToolsUsed() != null) {
            for (CertificationResultTestTool dto : certificationResult.getTestToolsUsed()) {
                testToolDuplicateResults.addObject(dto);
            }
        }
        if (testToolDuplicateResults.duplicatesExist()) {
            listing.addAllWarningMessages(
                    getWarnings(testToolDuplicateResults.getDuplicateList(),
                            Util.formatCriteriaNumber(certificationResult.getCriterion()))
                    .stream()
                    .collect(Collectors.toSet()));
            certificationResult.setTestToolsUsed(testToolDuplicateResults.getUniqueList());
        }

        DuplicateReviewResult<CertificationResultTestTool> testToolDuplicateIdResults = new DuplicateReviewResult<CertificationResultTestTool>(duplicateIdPredicate());
        if (certificationResult.getTestToolsUsed() != null) {
            for (CertificationResultTestTool dto : certificationResult.getTestToolsUsed()) {
                testToolDuplicateIdResults.addObject(dto);
            }
        }
        if (testToolDuplicateIdResults.duplicatesExist()) {
            listing.addAllBusinessErrorMessages(
                    getErrors(testToolDuplicateIdResults.getDuplicateList(),
                            Util.formatCriteriaNumber(certificationResult.getCriterion())));
        }
    }

    private Set<String> getErrors(List<CertificationResultTestTool> duplicates, String criteria) {
        Set<String> errors = new HashSet<String>();
        for (CertificationResultTestTool duplicate : duplicates) {
            String error = errorMessageUtil.getMessage("listing.criteria.duplicateTestToolName",
                    criteria, duplicate.getTestTool().getValue());
            errors.add(error);
        }
        return errors;
    }

    private List<String> getWarnings(List<CertificationResultTestTool> duplicates, String criteria) {
        List<String> warnings = new ArrayList<String>();
        for (CertificationResultTestTool duplicate : duplicates) {
            String warning = errorMessageUtil.getMessage("listing.criteria.duplicateTestToolNameAndVersion",
                    criteria, duplicate.getTestTool().getValue(),
                    duplicate.getVersion() == null ? "" : duplicate.getVersion());
            warnings.add(warning);
        }
        return warnings;
    }

    private BiPredicate<CertificationResultTestTool, CertificationResultTestTool> duplicatePredicate() {
        return new BiPredicate<CertificationResultTestTool, CertificationResultTestTool>() {
            @Override
            public boolean test(CertificationResultTestTool tt1,
                    CertificationResultTestTool tt2) {
                return ObjectUtils.allNotNull(tt1.getTestTool().getId(), tt2.getTestTool().getId())
                        && Objects.equals(tt1.getTestTool().getId(), tt2.getTestTool().getId())
                        && Objects.equals(tt1.getVersion(), tt2.getVersion());
            }
        };
    }

    private BiPredicate<CertificationResultTestTool, CertificationResultTestTool> duplicateIdPredicate() {
        return new BiPredicate<CertificationResultTestTool, CertificationResultTestTool>() {
            @Override
            public boolean test(CertificationResultTestTool tt1,
                    CertificationResultTestTool tt2) {
                return ObjectUtils.allNotNull(tt1.getTestTool().getId(), tt2.getTestTool().getId())
                        && Objects.equals(tt1.getTestTool().getId(), tt2.getTestTool().getId())
                        && !Objects.equals(tt1.getVersion(), tt2.getVersion());
            }
        };
    }
}

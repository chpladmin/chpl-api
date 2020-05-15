package gov.healthit.chpl.validation.listing.reviewer.duplicate;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultTestTool;
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

        DuplicateReviewResult<CertificationResultTestTool> testToolDuplicateResults =
                new DuplicateReviewResult<CertificationResultTestTool>(getPredicate());

        if (certificationResult.getTestToolsUsed() != null) {
            for (CertificationResultTestTool dto : certificationResult.getTestToolsUsed()) {
                testToolDuplicateResults.addObject(dto);
            }
        }

        if (testToolDuplicateResults.duplicatesExist()) {
            listing.getWarningMessages().addAll(
                    getWarnings(testToolDuplicateResults.getDuplicateList(),
                            Util.formatCriteriaNumber(certificationResult.getCriterion())));
            certificationResult.setTestToolsUsed(testToolDuplicateResults.getUniqueList());
        }
    }

    private List<String> getWarnings(List<CertificationResultTestTool> duplicates, String criteria) {
        List<String> warnings = new ArrayList<String>();
        for (CertificationResultTestTool duplicate : duplicates) {
            String warning = errorMessageUtil.getMessage("listing.criteria.duplicateTestTool",
                    criteria, duplicate.getTestToolName(), duplicate.getTestToolVersion());
            warnings.add(warning);
        }
        return warnings;
    }

    private BiPredicate<CertificationResultTestTool, CertificationResultTestTool> getPredicate() {
        return new BiPredicate<CertificationResultTestTool, CertificationResultTestTool>() {
            @Override
            public boolean test(CertificationResultTestTool dto1,
                    CertificationResultTestTool dto2) {

                return ObjectUtils.allNotNull(dto1.getTestToolName(), dto2.getTestToolName(),
                        dto1.getTestToolVersion(), dto2.getTestToolVersion())
                        && dto1.getTestToolName().equals(dto2.getTestToolName())
                        && dto1.getTestToolVersion().equals(dto2.getTestToolVersion());
            }
        };
    }
}


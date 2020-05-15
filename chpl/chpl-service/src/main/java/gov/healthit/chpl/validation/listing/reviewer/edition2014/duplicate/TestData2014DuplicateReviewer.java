package gov.healthit.chpl.validation.listing.reviewer.edition2014.duplicate;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultTestData;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.DuplicateReviewResult;

@Component("testData2014DuplicateReviewer")
public class TestData2014DuplicateReviewer {
    private ErrorMessageUtil errorMessageUtil;

    @Autowired
    public TestData2014DuplicateReviewer(ErrorMessageUtil errorMessageUtil) {
        this.errorMessageUtil = errorMessageUtil;
    }

    public void review(CertifiedProductSearchDetails listing, CertificationResult certificationResult) {

        DuplicateReviewResult<CertificationResultTestData> testDataDuplicateResults =
                new DuplicateReviewResult<CertificationResultTestData>(getPredicate());

        if (certificationResult.getTestDataUsed() != null) {
            for (CertificationResultTestData dto : certificationResult.getTestDataUsed()) {
                testDataDuplicateResults.addObject(dto);
            }
        }

        if (testDataDuplicateResults.duplicatesExist()) {
            listing.getWarningMessages().addAll(
                    getWarnings(testDataDuplicateResults.getDuplicateList(),
                            certificationResult.getCriterion().getNumber()));
            certificationResult.setTestDataUsed(testDataDuplicateResults.getUniqueList());
        }
    }

    private List<String> getWarnings(List<CertificationResultTestData> duplicates, String criteria) {
        List<String> warnings = new ArrayList<String>();
        for (CertificationResultTestData duplicate : duplicates) {
            String warning = errorMessageUtil.getMessage("listing.criteria.duplicateTestData.2014",
                    criteria, duplicate.getVersion());
            warnings.add(warning);
        }
        return warnings;
    }

    private BiPredicate<CertificationResultTestData, CertificationResultTestData> getPredicate() {
        return new BiPredicate<CertificationResultTestData, CertificationResultTestData>() {
            @Override
            public boolean test(CertificationResultTestData dto1,
                    CertificationResultTestData dto2) {
                return ObjectUtils.allNotNull(dto1.getVersion(), dto2.getVersion())
                        && dto1.getVersion().equals(dto2.getVersion());
            }
        };
    }
}

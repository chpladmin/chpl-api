package gov.healthit.chpl.validation.pendingListing.reviewer.edition2015.duplicate;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultTestDataDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.pendingListing.reviewer.duplicate.DuplicateReviewResult;

@Component("testData2015DuplicateReviewer")
public class TestData2015DuplicateReviewer {
    private ErrorMessageUtil errorMessageUtil;

    @Autowired
    public TestData2015DuplicateReviewer(final ErrorMessageUtil errorMessageUtil) {
        this.errorMessageUtil = errorMessageUtil;
    }

    public void review(final PendingCertifiedProductDTO listing, final PendingCertificationResultDTO certificationResult) {

        DuplicateReviewResult<PendingCertificationResultTestDataDTO> testDataDuplicateResults =
                new DuplicateReviewResult<PendingCertificationResultTestDataDTO>(getPredicate());

        if (certificationResult.getTestData() != null) {
            for (PendingCertificationResultTestDataDTO dto : certificationResult.getTestData()) {
                testDataDuplicateResults.addObject(dto);
            }
        }

        if (testDataDuplicateResults.duplicatesExist()) {
            listing.getWarningMessages().addAll(
                    getWarnings(testDataDuplicateResults.getDuplicateList(), certificationResult.getNumber()));
            certificationResult.setTestData(testDataDuplicateResults.getUniqueList());
        }
    }

    private List<String> getWarnings(final List<PendingCertificationResultTestDataDTO> duplicates, final String criteria) {
        List<String> warnings = new ArrayList<String>();
        for (PendingCertificationResultTestDataDTO duplicate : duplicates) {
            String warning = errorMessageUtil.getMessage("listing.criteria.duplicateTestData.2015",
                    criteria, duplicate.getEnteredName(), duplicate.getVersion(), duplicate.getAlteration());
            warnings.add(warning);
        }
        return warnings;
    }

    private BiPredicate<PendingCertificationResultTestDataDTO, PendingCertificationResultTestDataDTO> getPredicate() {
        return new BiPredicate<PendingCertificationResultTestDataDTO, PendingCertificationResultTestDataDTO>() {
            @Override
            public boolean test(final PendingCertificationResultTestDataDTO dto1,
                    final PendingCertificationResultTestDataDTO dto2) {
                return ObjectUtils.allNotNull(dto1.getEnteredName(), dto2.getEnteredName(), dto1.getVersion(),
                        dto2.getVersion())
                        && dto1.getEnteredName().equals(dto2.getEnteredName())
                        && dto1.getVersion().equals(dto2.getVersion());
            }
        };
    }
}

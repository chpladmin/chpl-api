package gov.healthit.chpl.validation.pendingListing.reviewer.duplicate;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultTestStandardDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component("testStandardDuplicateReviewer")
public class TestStandardDuplicateReviewer {
    private ErrorMessageUtil errorMessageUtil;

    @Autowired
    public TestStandardDuplicateReviewer(final ErrorMessageUtil errorMessageUtil) {
        this.errorMessageUtil = errorMessageUtil;
    }

    public void review(final PendingCertifiedProductDTO listing, final PendingCertificationResultDTO certificationResult) {

        DuplicateReviewResult<PendingCertificationResultTestStandardDTO> testStandardDuplicateResults =
                new DuplicateReviewResult<PendingCertificationResultTestStandardDTO>(getPredicate());


        if (certificationResult.getTestStandards() != null) {
            for (PendingCertificationResultTestStandardDTO dto : certificationResult.getTestStandards()) {
                testStandardDuplicateResults.addObject(dto);
            }
        }

        if (testStandardDuplicateResults.duplicatesExist()) {
            listing.getWarningMessages().addAll(
                    getWarnings(testStandardDuplicateResults.getDuplicateList(), certificationResult.getNumber()));
            certificationResult.setTestStandards(testStandardDuplicateResults.getUniqueList());
        }
    }

    private List<String> getWarnings(final List<PendingCertificationResultTestStandardDTO> duplicates, final String criteria) {
        List<String> warnings = new ArrayList<String>();
        for (PendingCertificationResultTestStandardDTO duplicate : duplicates) {
            String warning = errorMessageUtil.getMessage("listing.criteria.duplicateTestStandard",
                    criteria, duplicate.getName());
            warnings.add(warning);
        }
        return warnings;
    }

    private BiPredicate<PendingCertificationResultTestStandardDTO, PendingCertificationResultTestStandardDTO> getPredicate() {
        return new BiPredicate<PendingCertificationResultTestStandardDTO, PendingCertificationResultTestStandardDTO>() {
            @Override
            public boolean test(final PendingCertificationResultTestStandardDTO dto1,
                    final PendingCertificationResultTestStandardDTO dto2) {
                return ObjectUtils.allNotNull(dto1.getName(), dto2.getName())
                        && dto1.getName().equals(dto2.getName());
            }
        };
    }
}

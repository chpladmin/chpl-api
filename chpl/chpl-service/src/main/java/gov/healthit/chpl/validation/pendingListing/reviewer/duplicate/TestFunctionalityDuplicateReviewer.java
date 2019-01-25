package gov.healthit.chpl.validation.pendingListing.reviewer.duplicate;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dto.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.PendingCertificationResultTestFunctionalityDTO;
import gov.healthit.chpl.dto.PendingCertifiedProductDTO;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component("testFunctionalityDuplicateReviewer")
public class TestFunctionalityDuplicateReviewer {
    private ErrorMessageUtil errorMessageUtil;

    @Autowired
    public TestFunctionalityDuplicateReviewer(final ErrorMessageUtil errorMessageUtil) {
        this.errorMessageUtil = errorMessageUtil;
    }

    public void review(final PendingCertifiedProductDTO listing, final PendingCertificationResultDTO certificationResult) {

        DuplicateReviewResult<PendingCertificationResultTestFunctionalityDTO> testFunctionalityDuplicateResults =
                new DuplicateReviewResult<PendingCertificationResultTestFunctionalityDTO>(getPredicate());

        if (certificationResult.getTestFunctionality() != null) {
            for (PendingCertificationResultTestFunctionalityDTO dto : certificationResult.getTestFunctionality()) {
                testFunctionalityDuplicateResults.addObject(dto);
            }
        }

        if (testFunctionalityDuplicateResults.duplicatesExist()) {
            listing.getWarningMessages().addAll(
                    getWarnings(testFunctionalityDuplicateResults.getDuplicateList(), certificationResult.getNumber()));
            certificationResult.setTestFunctionality(testFunctionalityDuplicateResults.getUniqueList());
        }
    }

    private List<String> getWarnings(final List<PendingCertificationResultTestFunctionalityDTO> duplicates,
            final String criteria) {
        List<String> warnings = new ArrayList<String>();
        for (PendingCertificationResultTestFunctionalityDTO duplicate : duplicates) {
            String warning = errorMessageUtil.getMessage("listing.criteria.duplicateTestFunctionality",
                    criteria, duplicate.getNumber());
            warnings.add(warning);
        }
        return warnings;
    }

    private BiPredicate<
    PendingCertificationResultTestFunctionalityDTO, PendingCertificationResultTestFunctionalityDTO> getPredicate() {
        return
                new BiPredicate<
                PendingCertificationResultTestFunctionalityDTO, PendingCertificationResultTestFunctionalityDTO>() {
            @Override
            public boolean test(final PendingCertificationResultTestFunctionalityDTO dto1,
                    final PendingCertificationResultTestFunctionalityDTO dto2) {
                return ObjectUtils.allNotNull(dto1.getNumber(), dto2.getNumber())
                        && dto1.getNumber().equals(dto2.getNumber());
            }
        };
    }
}

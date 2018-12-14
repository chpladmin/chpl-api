package gov.healthit.chpl.validation.pendingListing.reviewer.edition2015.duplicate;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

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
    public TestFunctionalityDuplicateReviewer(ErrorMessageUtil errorMessageUtil) {
        this.errorMessageUtil = errorMessageUtil;
    }

    public void review(PendingCertifiedProductDTO listing, final PendingCertificationResultDTO certificationResult) {

        DuplicateReviewResult<PendingCertificationResultTestFunctionalityDTO> testFunctionalityDuplicateResults =
                new DuplicateReviewResult<PendingCertificationResultTestFunctionalityDTO>(getPredicate());

        if (certificationResult.getTestFunctionality() != null) {
            for (PendingCertificationResultTestFunctionalityDTO dto : certificationResult.getTestFunctionality()) {
                testFunctionalityDuplicateResults.addObject(dto);
            }
        }

        if (testFunctionalityDuplicateResults.getDuplicateList().size() > 0) {
            listing.getWarningMessages().addAll(getWarnings(testFunctionalityDuplicateResults.getDuplicateList(), certificationResult.getNumber()));
            certificationResult.setTestFunctionality(testFunctionalityDuplicateResults.getUniqueList());
        }
    }

    private List<String> getWarnings(List<PendingCertificationResultTestFunctionalityDTO> duplicates, String criteria) {
        List<String> warnings = new ArrayList<String>();
        for (PendingCertificationResultTestFunctionalityDTO duplicate : duplicates) {
            String warning = errorMessageUtil.getMessage("listing.criteria.duplicateTestFunctionality.2015",
                    criteria, duplicate.getNumber());
            warnings.add(warning);
        }
        return warnings;
    }

    private BiPredicate<PendingCertificationResultTestFunctionalityDTO, PendingCertificationResultTestFunctionalityDTO> getPredicate() {
        return
                new BiPredicate<PendingCertificationResultTestFunctionalityDTO, PendingCertificationResultTestFunctionalityDTO>() {
            @Override
            public boolean test(PendingCertificationResultTestFunctionalityDTO dto1, PendingCertificationResultTestFunctionalityDTO dto2) {
                if (dto1.getNumber() != null && dto2.getNumber() != null) {
                    return dto1.getNumber().equals(dto2.getNumber());
                } else {
                    return false;
                }
            }
        };
    }
}

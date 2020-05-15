package gov.healthit.chpl.validation.pendingListing.reviewer.duplicate;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultTestFunctionalityDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import gov.healthit.chpl.validation.DuplicateReviewResult;

@Component("pendingTestFunctionalityDuplicateReviewer")
public class TestFunctionalityDuplicateReviewer {
    private ErrorMessageUtil errorMessageUtil;

    @Autowired
    public TestFunctionalityDuplicateReviewer(ErrorMessageUtil errorMessageUtil) {
        this.errorMessageUtil = errorMessageUtil;
    }

    public void review(PendingCertifiedProductDTO listing, PendingCertificationResultDTO certificationResult) {

        DuplicateReviewResult<PendingCertificationResultTestFunctionalityDTO> testFunctionalityDuplicateResults =
                new DuplicateReviewResult<PendingCertificationResultTestFunctionalityDTO>(getPredicate());

        if (certificationResult.getTestFunctionality() != null) {
            for (PendingCertificationResultTestFunctionalityDTO dto : certificationResult.getTestFunctionality()) {
                testFunctionalityDuplicateResults.addObject(dto);
            }
        }

        if (testFunctionalityDuplicateResults.duplicatesExist()) {
            listing.getWarningMessages().addAll(getWarnings(
                            testFunctionalityDuplicateResults.getDuplicateList(),
                            Util.formatCriteriaNumber(certificationResult.getCriterion())));
            certificationResult.setTestFunctionality(testFunctionalityDuplicateResults.getUniqueList());
        }
    }

    private List<String> getWarnings(List<PendingCertificationResultTestFunctionalityDTO> duplicates,
            String criteria) {
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
            public boolean test(PendingCertificationResultTestFunctionalityDTO dto1,
                    PendingCertificationResultTestFunctionalityDTO dto2) {
                return ObjectUtils.allNotNull(dto1.getNumber(), dto2.getNumber())
                        && dto1.getNumber().equals(dto2.getNumber());
            }
        };
    }
}

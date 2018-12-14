package gov.healthit.chpl.validation.pendingListing.reviewer.edition2015.duplicate;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dto.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.PendingCertificationResultTestProcedureDTO;
import gov.healthit.chpl.dto.PendingCertifiedProductDTO;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component("testProcedureDuplicateReviewer")
public class TestProcedureDuplicateReviewer {
    private ErrorMessageUtil errorMessageUtil;

    @Autowired
    public TestProcedureDuplicateReviewer(ErrorMessageUtil errorMessageUtil) {
        this.errorMessageUtil = errorMessageUtil;
    }

    public void review(PendingCertifiedProductDTO listing, final PendingCertificationResultDTO certificationResult) {

        DuplicateReviewResult<PendingCertificationResultTestProcedureDTO> testProcedureDuplicateResults =
                new DuplicateReviewResult<PendingCertificationResultTestProcedureDTO>(getPredicate());

        if (certificationResult.getTestProcedures() != null) {
            for (PendingCertificationResultTestProcedureDTO dto : certificationResult.getTestProcedures()) {
                testProcedureDuplicateResults.addObject(dto);
            }
        }

        if (testProcedureDuplicateResults.getDuplicateList().size() > 0) {
            listing.getWarningMessages().addAll(getWarnings(testProcedureDuplicateResults.getDuplicateList(), certificationResult.getNumber()));
            certificationResult.setTestProcedures(testProcedureDuplicateResults.getUniqueList());
        }
    }

    private List<String> getWarnings(List<PendingCertificationResultTestProcedureDTO> duplicates, String criteria) {
        List<String> warnings = new ArrayList<String>();
        for (PendingCertificationResultTestProcedureDTO duplicate : duplicates) {
            String warning = errorMessageUtil.getMessage("listing.criteria.duplicateTestProcedure.2015",
                    criteria, duplicate.getEnteredName(), duplicate.getVersion());
            warnings.add(warning);
        }
        return warnings;
    }

    private BiPredicate<PendingCertificationResultTestProcedureDTO, PendingCertificationResultTestProcedureDTO> getPredicate() {
        return new BiPredicate<PendingCertificationResultTestProcedureDTO, PendingCertificationResultTestProcedureDTO>() {
            @Override
            public boolean test(PendingCertificationResultTestProcedureDTO dto1, PendingCertificationResultTestProcedureDTO dto2) {
                if (dto1.getEnteredName() != null && dto2.getEnteredName() != null
                        && dto1.getVersion() != null && dto2.getVersion() != null) {
                    return dto1.getEnteredName().equals(dto2.getEnteredName())
                            && dto1.getVersion().equals(dto2.getVersion());
                } else {
                    return false;
                }
            }
        };
    }
}

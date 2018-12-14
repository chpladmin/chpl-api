package gov.healthit.chpl.validation.pendingListing.reviewer.edition2015.duplicate;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dto.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.PendingCertificationResultTestToolDTO;
import gov.healthit.chpl.dto.PendingCertifiedProductDTO;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component("testToolDuplicateReviewer")
public class TestToolDuplicateReviewer {
    private ErrorMessageUtil errorMessageUtil;

    @Autowired
    public TestToolDuplicateReviewer(ErrorMessageUtil errorMessageUtil) {
        this.errorMessageUtil = errorMessageUtil;
    }

    public void review(PendingCertifiedProductDTO listing, final PendingCertificationResultDTO certificationResult) {

        DuplicateReviewResult<PendingCertificationResultTestToolDTO> testToolDuplicateResults =
                new DuplicateReviewResult<PendingCertificationResultTestToolDTO>(getPredicate());

        if (certificationResult.getTestTools() != null) {
            for (PendingCertificationResultTestToolDTO dto : certificationResult.getTestTools()) {
                testToolDuplicateResults.addObject(dto);
            }
        }

        if (testToolDuplicateResults.getDuplicateList().size() > 0) {
            listing.getWarningMessages().addAll(getWarnings(testToolDuplicateResults.getDuplicateList(), certificationResult.getNumber()));
            certificationResult.setTestTools(testToolDuplicateResults.getUniqueList());
        }
    }

    private List<String> getWarnings(List<PendingCertificationResultTestToolDTO> duplicates, String criteria) {
        List<String> warnings = new ArrayList<String>();
        for (PendingCertificationResultTestToolDTO duplicate : duplicates) {
            String warning = errorMessageUtil.getMessage("listing.criteria.duplicateTestTool.2015",
                    criteria, duplicate.getName(), duplicate.getVersion());
            warnings.add(warning);
        }
        return warnings;
    }

    private BiPredicate<PendingCertificationResultTestToolDTO, PendingCertificationResultTestToolDTO> getPredicate() {
        return new BiPredicate<PendingCertificationResultTestToolDTO, PendingCertificationResultTestToolDTO>() {
            @Override
            public boolean test(PendingCertificationResultTestToolDTO dto1, PendingCertificationResultTestToolDTO dto2) {
                if (dto1.getName() != null && dto2.getName() != null
                        && dto1.getVersion() != null && dto2.getVersion() != null) {
                    return dto1.getName().equals(dto2.getName())
                            && dto1.getVersion().equals(dto2.getVersion());
                } else {
                    return false;
                }
            }
        };
    }
}


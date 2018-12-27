package gov.healthit.chpl.validation.pendingListing.reviewer.duplicate;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

import org.apache.commons.lang3.ObjectUtils;
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
    public TestToolDuplicateReviewer(final ErrorMessageUtil errorMessageUtil) {
        this.errorMessageUtil = errorMessageUtil;
    }

    public void review(final PendingCertifiedProductDTO listing, final PendingCertificationResultDTO certificationResult) {

        DuplicateReviewResult<PendingCertificationResultTestToolDTO> testToolDuplicateResults =
                new DuplicateReviewResult<PendingCertificationResultTestToolDTO>(getPredicate());

        if (certificationResult.getTestTools() != null) {
            for (PendingCertificationResultTestToolDTO dto : certificationResult.getTestTools()) {
                testToolDuplicateResults.addObject(dto);
            }
        }

        if (testToolDuplicateResults.duplicatesExist()) {
            listing.getWarningMessages().addAll(
                    getWarnings(testToolDuplicateResults.getDuplicateList(), certificationResult.getNumber()));
            certificationResult.setTestTools(testToolDuplicateResults.getUniqueList());
        }
    }

    private List<String> getWarnings(final List<PendingCertificationResultTestToolDTO> duplicates, final String criteria) {
        List<String> warnings = new ArrayList<String>();
        for (PendingCertificationResultTestToolDTO duplicate : duplicates) {
            String warning = errorMessageUtil.getMessage("listing.criteria.duplicateTestTool",
                    criteria, duplicate.getName(), duplicate.getVersion());
            warnings.add(warning);
        }
        return warnings;
    }

    private BiPredicate<PendingCertificationResultTestToolDTO, PendingCertificationResultTestToolDTO> getPredicate() {
        return new BiPredicate<PendingCertificationResultTestToolDTO, PendingCertificationResultTestToolDTO>() {
            @Override
            public boolean test(final PendingCertificationResultTestToolDTO dto1,
                    final PendingCertificationResultTestToolDTO dto2) {

                return ObjectUtils.allNotNull(dto1.getName(), dto2.getName(), dto1.getVersion(), dto2.getVersion())
                        && dto1.getName().equals(dto2.getName())
                        && dto1.getVersion().equals(dto2.getVersion());
            }
        };
    }
}


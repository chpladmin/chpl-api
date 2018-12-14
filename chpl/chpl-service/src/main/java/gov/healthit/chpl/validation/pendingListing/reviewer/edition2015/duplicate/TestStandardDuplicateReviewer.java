package gov.healthit.chpl.validation.pendingListing.reviewer.edition2015.duplicate;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dto.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.PendingCertificationResultTestStandardDTO;
import gov.healthit.chpl.dto.PendingCertifiedProductDTO;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component("testStandardDuplicateReviewer")
public class TestStandardDuplicateReviewer {
    private ErrorMessageUtil errorMessageUtil;

    @Autowired
    public TestStandardDuplicateReviewer(ErrorMessageUtil errorMessageUtil) {
        this.errorMessageUtil = errorMessageUtil;
    }

    public void review(PendingCertifiedProductDTO listing, final PendingCertificationResultDTO certificationResult) {

        DuplicateReviewResult<PendingCertificationResultTestStandardDTO> testStandardDuplicateResults =
                new DuplicateReviewResult<PendingCertificationResultTestStandardDTO>(getPredicate());


        if (certificationResult.getTestStandards() != null) {
            for (PendingCertificationResultTestStandardDTO dto : certificationResult.getTestStandards()) {
                testStandardDuplicateResults.addObject(dto);
            }
        }

        if (testStandardDuplicateResults.getDuplicateList().size() > 0) {
            listing.getWarningMessages().addAll(getWarnings(testStandardDuplicateResults.getDuplicateList(), certificationResult.getNumber()));
            certificationResult.setTestStandards(testStandardDuplicateResults.getUniqueList());
        }
    }

    private List<String> getWarnings(List<PendingCertificationResultTestStandardDTO> duplicates, String criteria) {
        List<String> warnings = new ArrayList<String>();
        for (PendingCertificationResultTestStandardDTO duplicate : duplicates) {
            String warning = errorMessageUtil.getMessage("listing.criteria.duplicateTestStandard.2015",
                    criteria, duplicate.getName());
            warnings.add(warning);
        }
        return warnings;
    }

    private BiPredicate<PendingCertificationResultTestStandardDTO, PendingCertificationResultTestStandardDTO> getPredicate() {
        return new BiPredicate<PendingCertificationResultTestStandardDTO, PendingCertificationResultTestStandardDTO>() {
            @Override
            public boolean test(PendingCertificationResultTestStandardDTO dto1, PendingCertificationResultTestStandardDTO dto2) {
                if (dto1.getName() != null && dto2.getName() != null) {
                    return dto1.getName().equals(dto2.getName());
                } else {
                    return false;
                }
            }
        };
    }

    //    public void review(PendingCertifiedProductDTO listing, final PendingCertificationResultDTO certificationResult) {
    //        DuplicateReviewResult<PendingCertificationResultTestStandardDTO> testStandardDuplicateResults =
    //                removeDuplicates(certificationResult);
    //        if (testStandardDuplicateResults.getMessages().size() > 0) {
    //            listing.getWarningMessages().addAll(testStandardDuplicateResults.getMessages());
    //            certificationResult.setTestStandards(testStandardDuplicateResults.getObjects());
    //        }
    //    }
    //
    //    private DuplicateReviewResult<PendingCertificationResultTestStandardDTO> removeDuplicates(
    //            final PendingCertificationResultDTO certificationResult) {
    //
    //        DuplicateReviewResult<PendingCertificationResultTestStandardDTO> dupResults =
    //                new DuplicateReviewResult<PendingCertificationResultTestStandardDTO>();
    //
    //        if (certificationResult.getTestStandards() != null) {
    //            for (PendingCertificationResultTestStandardDTO dto : certificationResult.getTestStandards()) {
    //                if (isDuplicate(dupResults, dto)) {
    //                    // Item already exists
    //                    String warning = errorMessageUtil.getMessage("listing.criteria.duplicateTestStandard.2015",
    //                            certificationResult.getNumber(), dto.getName());
    //                    dupResults.getMessages().add(warning);
    //                } else {
    //                    //Add the item to the final list
    //                    dupResults.getObjects().add(dto);
    //                }
    //            }
    //        }
    //
    //        return dupResults;
    //    }
    //
    //    private Boolean isDuplicate(
    //            final DuplicateReviewResult<PendingCertificationResultTestStandardDTO> dupResults,
    //            final PendingCertificationResultTestStandardDTO testStandardDTO) {
    //        return dupResults.existsInObjects(testStandardDTO, new Predicate<PendingCertificationResultTestStandardDTO>() {
    //            @Override
    //            public boolean test(PendingCertificationResultTestStandardDTO dto2) {
    //                if (testStandardDTO.getName() != null && dto2.getName() != null) {
    //                    return testStandardDTO.getName().equals(dto2.getName());
    //                } else {
    //                    return false;
    //                }
    //            }
    //        });
    //    }
    //
}

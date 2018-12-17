package gov.healthit.chpl.validation.pendingListing.reviewer.edition2015.duplicate;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dto.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.PendingCertificationResultTestDataDTO;
import gov.healthit.chpl.dto.PendingCertifiedProductDTO;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.pendingListing.reviewer.duplicate.DuplicateReviewResult;

@Component("testData2015DuplicateReviewer")
public class TestData2015DuplicateReviewer {
    private ErrorMessageUtil errorMessageUtil;

    @Autowired
    public TestData2015DuplicateReviewer(ErrorMessageUtil errorMessageUtil) {
        this.errorMessageUtil = errorMessageUtil;
    }

    public void review(PendingCertifiedProductDTO listing, final PendingCertificationResultDTO certificationResult) {

        DuplicateReviewResult<PendingCertificationResultTestDataDTO> testDataDuplicateResults =
                new DuplicateReviewResult<PendingCertificationResultTestDataDTO>(getPredicate());

        if (certificationResult.getTestData() != null) {
            for (PendingCertificationResultTestDataDTO dto : certificationResult.getTestData()) {
                testDataDuplicateResults.addObject(dto);
            }
        }

        if (testDataDuplicateResults.duplicatesExist()) {
            listing.getWarningMessages().addAll(getWarnings(testDataDuplicateResults.getDuplicateList(), certificationResult.getNumber()));
            certificationResult.setTestData(testDataDuplicateResults.getUniqueList());
        }
    }

    private List<String> getWarnings(List<PendingCertificationResultTestDataDTO> duplicates, String criteria) {
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
            public boolean test(PendingCertificationResultTestDataDTO dto1, PendingCertificationResultTestDataDTO dto2) {
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

    //    public void review(PendingCertifiedProductDTO listing, final PendingCertificationResultDTO certificationResult) {
    //        DuplicateReviewResult<PendingCertificationResultTestDataDTO> testDataDuplicateResults =
    //                removeDuplicates(certificationResult);
    //        if (testDataDuplicateResults.getMessages().size() > 0) {
    //            listing.getWarningMessages().addAll(testDataDuplicateResults.getMessages());
    //            certificationResult.setTestData(testDataDuplicateResults.getObjects());
    //        }
    //    }
    //
    //    private DuplicateReviewResult<PendingCertificationResultTestDataDTO> removeDuplicates(
    //            final PendingCertificationResultDTO certificationResult) {
    //
    //        DuplicateReviewResult<PendingCertificationResultTestDataDTO> dupResults =
    //                new DuplicateReviewResult<PendingCertificationResultTestDataDTO>();
    //
    //        if (certificationResult.getTestData() != null) {
    //            for (PendingCertificationResultTestDataDTO dto : certificationResult.getTestData()) {
    //                if (isDuplicate(dupResults, dto)) {
    //                    // Item already exists
    //                    String warning = errorMessageUtil.getMessage("listing.criteria.duplicateTestData.2015",
    //                            certificationResult.getNumber(), dto.getEnteredName(), dto.getVersion(),
    //                            dto.getAlteration());
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
    //            final DuplicateReviewResult<PendingCertificationResultTestDataDTO> dupResults,
    //            final PendingCertificationResultTestDataDTO testDataDTO) {
    //        return dupResults.existsInObjects(testDataDTO, new Predicate<PendingCertificationResultTestDataDTO>() {
    //            @Override
    //            public boolean test(PendingCertificationResultTestDataDTO dto2) {
    //                if (testDataDTO.getEnteredName() != null && dto2.getEnteredName() != null
    //                        && testDataDTO.getVersion() != null && dto2.getVersion() != null
    //                        && testDataDTO.getAlteration() != null && dto2.getAlteration() != null) {
    //
    //                    return testDataDTO.getEnteredName().equals(dto2.getEnteredName())
    //                            && testDataDTO.getVersion().equals(dto2.getVersion())
    //                            && testDataDTO.getAlteration().equals(dto2.getAlteration());
    //                } else {
    //                    return false;
    //                }
    //            }
    //        });
    //    }

}

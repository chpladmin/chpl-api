package gov.healthit.chpl.validation.pendingListing.reviewer.duplicate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultTestDataDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import gov.healthit.chpl.validation.DuplicateReviewResult;

@Component("pendingTestDataDuplicateReviewer")
public class TestDataDuplicateReviewer {
    private ErrorMessageUtil errorMessageUtil;

    @Autowired
    public TestDataDuplicateReviewer(ErrorMessageUtil errorMessageUtil) {
        this.errorMessageUtil = errorMessageUtil;
    }

    public void review(PendingCertifiedProductDTO listing, PendingCertificationResultDTO certificationResult) {

        DuplicateReviewResult<PendingCertificationResultTestDataDTO> testDataDuplicateResults =
                new DuplicateReviewResult<PendingCertificationResultTestDataDTO>(duplicatePredicate());
        if (certificationResult.getTestData() != null) {
            for (PendingCertificationResultTestDataDTO dto : certificationResult.getTestData()) {
                testDataDuplicateResults.addObject(dto);
            }
        }
        if (testDataDuplicateResults.duplicatesExist()) {
            listing.getWarningMessages().addAll(
                    getWarnings(testDataDuplicateResults.getDuplicateList(),
                            Util.formatCriteriaNumber(certificationResult.getCriterion())));
            certificationResult.setTestData(testDataDuplicateResults.getUniqueList());
        }

        DuplicateReviewResult<PendingCertificationResultTestDataDTO> testDataDuplicateNameResults =
                new DuplicateReviewResult<PendingCertificationResultTestDataDTO>(duplicateIdPredicate());
        if (certificationResult.getTestData() != null) {
            for (PendingCertificationResultTestDataDTO dto : certificationResult.getTestData()) {
                testDataDuplicateNameResults.addObject(dto);
            }
        }
        if (testDataDuplicateNameResults.duplicatesExist()) {
            listing.getErrorMessages().addAll(
                    getErrors(testDataDuplicateNameResults.getDuplicateList(),
                            Util.formatCriteriaNumber(certificationResult.getCriterion())));
        }
    }

    private List<String> getErrors(List<PendingCertificationResultTestDataDTO> duplicates, String criteria) {
        List<String> errors = new ArrayList<String>();
        for (PendingCertificationResultTestDataDTO duplicate : duplicates) {
            String tdName = duplicate.getTestData() == null || duplicate.getTestData().getName() == null
                    ? duplicate.getEnteredName() : duplicate.getTestData().getName();
            String error = errorMessageUtil.getMessage("listing.criteria.duplicateTestDataName", criteria, tdName);
            errors.add(error);
        }
        return errors;
    }

    private List<String> getWarnings(List<PendingCertificationResultTestDataDTO> duplicates, String criteria) {
        List<String> warnings = new ArrayList<String>();
        for (PendingCertificationResultTestDataDTO duplicate : duplicates) {
            String tdName = duplicate.getTestData() == null || duplicate.getTestData().getName() == null
                    ? duplicate.getEnteredName() : duplicate.getTestData().getName();
            String warning = errorMessageUtil.getMessage("listing.criteria.duplicateTestDataNameAndVersion",
                        criteria, tdName,
                        duplicate.getVersion() == null ? "" : duplicate.getVersion());
            warnings.add(warning);
        }
        return warnings;
    }

    private BiPredicate<PendingCertificationResultTestDataDTO, PendingCertificationResultTestDataDTO> duplicatePredicate() {
        return new BiPredicate<PendingCertificationResultTestDataDTO, PendingCertificationResultTestDataDTO>() {
            @Override
            public boolean test(PendingCertificationResultTestDataDTO dto1,
                    PendingCertificationResultTestDataDTO dto2) {
                return ((ObjectUtils.allNotNull(dto1.getTestDataId(), dto2.getTestDataId())
                            && Objects.equals(dto1.getTestDataId(), dto2.getTestDataId()))
                        || (dto1.getTestDataId() == null && dto2.getTestDataId() == null
                            && ObjectUtils.allNotNull(dto1.getTestData(), dto2.getTestData(), dto1.getTestData().getId(),
                                dto2.getTestData().getId())
                                && Objects.equals(dto1.getTestData().getId(), dto2.getTestData().getId())))
                        && Objects.equals(dto1.getVersion(), dto2.getVersion());
            }
        };
    }

    private BiPredicate<PendingCertificationResultTestDataDTO, PendingCertificationResultTestDataDTO> duplicateIdPredicate() {
        return new BiPredicate<PendingCertificationResultTestDataDTO, PendingCertificationResultTestDataDTO>() {
            @Override
            public boolean test(PendingCertificationResultTestDataDTO dto1,
                    PendingCertificationResultTestDataDTO dto2) {
                return ((ObjectUtils.allNotNull(dto1.getTestDataId(), dto2.getTestDataId())
                            && Objects.equals(dto1.getTestDataId(), dto2.getTestDataId()))
                        || (dto1.getTestDataId() == null && dto2.getTestDataId() == null
                            && ObjectUtils.allNotNull(dto1.getTestData(), dto2.getTestData(), dto1.getTestData().getId(),
                                dto2.getTestData().getId())
                                && Objects.equals(dto1.getTestData().getId(), dto2.getTestData().getId())))
                        && !Objects.equals(dto1.getVersion(), dto2.getVersion());
            }
        };
    }
}

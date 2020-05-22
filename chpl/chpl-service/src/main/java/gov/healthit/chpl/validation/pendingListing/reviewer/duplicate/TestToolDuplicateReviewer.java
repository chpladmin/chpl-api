package gov.healthit.chpl.validation.pendingListing.reviewer.duplicate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultTestToolDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import gov.healthit.chpl.validation.DuplicateReviewResult;

@Component("pendingTestToolDuplicateReviewer")
public class TestToolDuplicateReviewer {
    private ErrorMessageUtil errorMessageUtil;

    @Autowired
    public TestToolDuplicateReviewer(ErrorMessageUtil errorMessageUtil) {
        this.errorMessageUtil = errorMessageUtil;
    }

    public void review(PendingCertifiedProductDTO listing, PendingCertificationResultDTO certificationResult) {
        DuplicateReviewResult<PendingCertificationResultTestToolDTO> testToolDuplicateResults =
                new DuplicateReviewResult<PendingCertificationResultTestToolDTO>(duplicatePredicate());
        if (certificationResult.getTestTools() != null) {
            for (PendingCertificationResultTestToolDTO dto : certificationResult.getTestTools()) {
                testToolDuplicateResults.addObject(dto);
            }
        }
        if (testToolDuplicateResults.duplicatesExist()) {
            listing.getWarningMessages().addAll(
                    getWarnings(testToolDuplicateResults.getDuplicateList(),
                            Util.formatCriteriaNumber(certificationResult.getCriterion())));
            certificationResult.setTestTools(testToolDuplicateResults.getUniqueList());
        }

        DuplicateReviewResult<PendingCertificationResultTestToolDTO> testToolDuplicateIdResults =
                new DuplicateReviewResult<PendingCertificationResultTestToolDTO>(duplicateIdPredicate());
        if (certificationResult.getTestTools() != null) {
            for (PendingCertificationResultTestToolDTO dto : certificationResult.getTestTools()) {
                testToolDuplicateIdResults.addObject(dto);
            }
        }
        if (testToolDuplicateIdResults.duplicatesExist()) {
            listing.getErrorMessages().addAll(
                    getErrors(testToolDuplicateIdResults.getDuplicateList(),
                            Util.formatCriteriaNumber(certificationResult.getCriterion())));
        }
    }

    private List<String> getErrors(List<PendingCertificationResultTestToolDTO> duplicates, String criteria) {
        List<String> errors = new ArrayList<String>();
        for (PendingCertificationResultTestToolDTO duplicate : duplicates) {
            String error = errorMessageUtil.getMessage("listing.criteria.duplicateTestToolName",
                    criteria, duplicate.getName());
            errors.add(error);
        }
        return errors;
    }

    private List<String> getWarnings(List<PendingCertificationResultTestToolDTO> duplicates, String criteria) {
        List<String> warnings = new ArrayList<String>();
        for (PendingCertificationResultTestToolDTO duplicate : duplicates) {
            String warning = errorMessageUtil.getMessage("listing.criteria.duplicateTestToolNameAndVersion",
                    criteria, duplicate.getName(),
                    duplicate.getVersion() == null ? "" : duplicate.getVersion());
            warnings.add(warning);
        }
        return warnings;
    }

    private BiPredicate<PendingCertificationResultTestToolDTO, PendingCertificationResultTestToolDTO> duplicatePredicate() {
        return new BiPredicate<PendingCertificationResultTestToolDTO, PendingCertificationResultTestToolDTO>() {
            @Override
            public boolean test(PendingCertificationResultTestToolDTO dto1,
                    PendingCertificationResultTestToolDTO dto2) {
                return ((ObjectUtils.allNotNull(dto1.getTestToolId(), dto2.getTestToolId())
                            && Objects.equals(dto1.getTestToolId(), dto2.getTestToolId()))
                        || (dto1.getTestToolId() == null && dto2.getTestToolId() == null
                            && ObjectUtils.allNotNull(dto1.getName(), dto2.getName())
                                && Objects.equals(dto1.getName(), dto2.getName())))
                        && Objects.equals(dto1.getVersion(), dto2.getVersion());
            }
        };
    }

    private BiPredicate<PendingCertificationResultTestToolDTO, PendingCertificationResultTestToolDTO> duplicateIdPredicate() {
        return new BiPredicate<PendingCertificationResultTestToolDTO, PendingCertificationResultTestToolDTO>() {
            @Override
            public boolean test(PendingCertificationResultTestToolDTO dto1,
                    PendingCertificationResultTestToolDTO dto2) {
                return ((ObjectUtils.allNotNull(dto1.getTestToolId(), dto2.getTestToolId())
                            && Objects.equals(dto1.getTestToolId(), dto2.getTestToolId()))
                        || (dto1.getTestToolId() == null && dto2.getTestToolId() == null
                            && ObjectUtils.allNotNull(dto1.getName(), dto2.getName())
                                && Objects.equals(dto1.getName(), dto2.getName())))
                        && !Objects.equals(dto1.getVersion(), dto2.getVersion());
            }
        };
    }
}


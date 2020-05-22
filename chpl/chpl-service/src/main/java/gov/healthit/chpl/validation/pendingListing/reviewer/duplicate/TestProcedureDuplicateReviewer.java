package gov.healthit.chpl.validation.pendingListing.reviewer.duplicate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultTestProcedureDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import gov.healthit.chpl.validation.DuplicateReviewResult;

@Component("pendingTestProcedureDuplicateReviewer")
public class TestProcedureDuplicateReviewer {
    private ErrorMessageUtil errorMessageUtil;

    @Autowired
    public TestProcedureDuplicateReviewer(ErrorMessageUtil errorMessageUtil) {
        this.errorMessageUtil = errorMessageUtil;
    }

    public void review(PendingCertifiedProductDTO listing, PendingCertificationResultDTO certificationResult) {

        DuplicateReviewResult<PendingCertificationResultTestProcedureDTO> testProcedureDuplicateResults =
                new DuplicateReviewResult<PendingCertificationResultTestProcedureDTO>(duplicatePredicate());
        if (certificationResult.getTestProcedures() != null) {
            for (PendingCertificationResultTestProcedureDTO dto : certificationResult.getTestProcedures()) {
                testProcedureDuplicateResults.addObject(dto);
            }
        }
        if (testProcedureDuplicateResults.duplicatesExist()) {
            listing.getWarningMessages().addAll(
                    getWarnings(testProcedureDuplicateResults.getDuplicateList(),
                            Util.formatCriteriaNumber(certificationResult.getCriterion())));
            certificationResult.setTestProcedures(testProcedureDuplicateResults.getUniqueList());
        }

        DuplicateReviewResult<PendingCertificationResultTestProcedureDTO> testProcedureDuplicateIdResults =
                new DuplicateReviewResult<PendingCertificationResultTestProcedureDTO>(duplicateIdPredicate());
        if (certificationResult.getTestProcedures() != null) {
            for (PendingCertificationResultTestProcedureDTO dto : certificationResult.getTestProcedures()) {
                testProcedureDuplicateIdResults.addObject(dto);
            }
        }
        if (testProcedureDuplicateIdResults.duplicatesExist()) {
            listing.getErrorMessages().addAll(
                    getErrors(testProcedureDuplicateIdResults.getDuplicateList(),
                            Util.formatCriteriaNumber(certificationResult.getCriterion())));
        }
    }

    private List<String> getErrors(List<PendingCertificationResultTestProcedureDTO> duplicates,
            String criteria) {
        List<String> warnings = new ArrayList<String>();
        for (PendingCertificationResultTestProcedureDTO duplicate : duplicates) {
            String tpName = duplicate.getTestProcedure() != null && duplicate.getTestProcedure().getName() != null
                    ? duplicate.getTestProcedure().getName() : duplicate.getEnteredName();
            String warning = errorMessageUtil.getMessage("listing.criteria.duplicateTestProcedureName",
                    criteria, tpName);
            warnings.add(warning);
        }
        return warnings;
    }

    private List<String> getWarnings(List<PendingCertificationResultTestProcedureDTO> duplicates,
            String criteria) {
        List<String> warnings = new ArrayList<String>();
        for (PendingCertificationResultTestProcedureDTO duplicate : duplicates) {
            String tpName = duplicate.getTestProcedure() != null && duplicate.getTestProcedure().getName() != null
                    ? duplicate.getTestProcedure().getName() : duplicate.getEnteredName();
            String warning = errorMessageUtil.getMessage("listing.criteria.duplicateTestProcedureNameAndVersion",
                    criteria, tpName, duplicate.getVersion() == null ? "" : duplicate.getVersion());
            warnings.add(warning);
        }
        return warnings;
    }

    private BiPredicate<PendingCertificationResultTestProcedureDTO, PendingCertificationResultTestProcedureDTO> duplicatePredicate() {
        return new BiPredicate<PendingCertificationResultTestProcedureDTO, PendingCertificationResultTestProcedureDTO>() {
            @Override
            public boolean test(PendingCertificationResultTestProcedureDTO dto1,
                    PendingCertificationResultTestProcedureDTO dto2) {
                return ((ObjectUtils.allNotNull(dto1.getTestProcedureId(), dto2.getTestProcedureId())
                        && Objects.equals(dto1.getTestProcedureId(), dto2.getTestProcedureId()))
                    || (dto1.getTestProcedureId() == null && dto2.getTestProcedureId() == null
                        && ObjectUtils.allNotNull(dto1.getEnteredName(), dto2.getEnteredName())
                            && Objects.equals(dto1.getEnteredName(), dto2.getEnteredName())))
                    && Objects.equals(dto1.getVersion(), dto2.getVersion());
            }
        };
    }

    private BiPredicate<PendingCertificationResultTestProcedureDTO, PendingCertificationResultTestProcedureDTO> duplicateIdPredicate() {
        return new BiPredicate<PendingCertificationResultTestProcedureDTO, PendingCertificationResultTestProcedureDTO>() {
            @Override
            public boolean test(PendingCertificationResultTestProcedureDTO dto1,
                    PendingCertificationResultTestProcedureDTO dto2) {
                return ((ObjectUtils.allNotNull(dto1.getTestProcedureId(), dto2.getTestProcedureId())
                        && Objects.equals(dto1.getTestProcedureId(), dto2.getTestProcedureId()))
                    || (dto1.getTestProcedureId() == null && dto2.getTestProcedureId() == null
                        && ObjectUtils.allNotNull(dto1.getEnteredName(), dto2.getEnteredName())
                            && Objects.equals(dto1.getEnteredName(), dto2.getEnteredName())))
                    && !Objects.equals(dto1.getVersion(), dto2.getVersion());
            }
        };
    }
}

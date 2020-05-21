package gov.healthit.chpl.validation.pendingListing.reviewer.edition2015.duplicate;

import java.util.ArrayList;
import java.util.List;
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

@Component("pendingTestProcedure2015DuplicateReviewer")
public class TestProcedure2015DuplicateReviewer {
    private ErrorMessageUtil errorMessageUtil;

    @Autowired
    public TestProcedure2015DuplicateReviewer(ErrorMessageUtil errorMessageUtil) {
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

        DuplicateReviewResult<PendingCertificationResultTestProcedureDTO> testProcedureDuplicateNameResults =
                new DuplicateReviewResult<PendingCertificationResultTestProcedureDTO>(duplicateNamePredicate());
        if (certificationResult.getTestProcedures() != null) {
            for (PendingCertificationResultTestProcedureDTO dto : certificationResult.getTestProcedures()) {
                testProcedureDuplicateNameResults.addObject(dto);
            }
        }
        if (testProcedureDuplicateNameResults.duplicatesExist()) {
            listing.getErrorMessages().addAll(
                    getErrors(testProcedureDuplicateNameResults.getDuplicateList(),
                            Util.formatCriteriaNumber(certificationResult.getCriterion())));
        }
    }

    private List<String> getErrors(List<PendingCertificationResultTestProcedureDTO> duplicates,
            String criteria) {
        List<String> warnings = new ArrayList<String>();
        for (PendingCertificationResultTestProcedureDTO duplicate : duplicates) {
            String warning = errorMessageUtil.getMessage("listing.criteria.duplicateTestProcedureName",
                    criteria, duplicate.getEnteredName());
            warnings.add(warning);
        }
        return warnings;
    }

    private List<String> getWarnings(List<PendingCertificationResultTestProcedureDTO> duplicates,
            String criteria) {
        List<String> warnings = new ArrayList<String>();
        for (PendingCertificationResultTestProcedureDTO duplicate : duplicates) {
            String warning = errorMessageUtil.getMessage("listing.criteria.duplicateTestProcedureNameAndVersion",
                    criteria, duplicate.getEnteredName(), duplicate.getVersion());
            warnings.add(warning);
        }
        return warnings;
    }

    private BiPredicate<PendingCertificationResultTestProcedureDTO, PendingCertificationResultTestProcedureDTO> duplicatePredicate() {
        return new BiPredicate<PendingCertificationResultTestProcedureDTO, PendingCertificationResultTestProcedureDTO>() {
            @Override
            public boolean test(PendingCertificationResultTestProcedureDTO dto1,
                    PendingCertificationResultTestProcedureDTO dto2) {
                return ObjectUtils.allNotNull(dto1.getEnteredName(), dto2.getEnteredName(),
                        dto1.getVersion(), dto2.getVersion())
                        && dto1.getEnteredName().equals(dto2.getEnteredName())
                        && dto1.getVersion().equals(dto2.getVersion());
            }
        };
    }

    private BiPredicate<PendingCertificationResultTestProcedureDTO, PendingCertificationResultTestProcedureDTO> duplicateNamePredicate() {
        return new BiPredicate<PendingCertificationResultTestProcedureDTO, PendingCertificationResultTestProcedureDTO>() {
            @Override
            public boolean test(PendingCertificationResultTestProcedureDTO dto1,
                    PendingCertificationResultTestProcedureDTO dto2) {
                return ObjectUtils.allNotNull(dto1.getEnteredName(), dto2.getEnteredName())
                        && dto1.getEnteredName().equals(dto2.getEnteredName());
            }
        };
    }
}

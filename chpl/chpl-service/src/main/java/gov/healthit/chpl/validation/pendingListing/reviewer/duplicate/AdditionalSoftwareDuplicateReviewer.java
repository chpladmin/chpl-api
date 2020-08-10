package gov.healthit.chpl.validation.pendingListing.reviewer.duplicate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultAdditionalSoftwareDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import gov.healthit.chpl.validation.DuplicateReviewResult;

@Component("pendingAdditionalSoftwareDuplicateReviewer")
public class AdditionalSoftwareDuplicateReviewer {
    private ErrorMessageUtil errorMessageUtil;

    @Autowired
    public AdditionalSoftwareDuplicateReviewer(ErrorMessageUtil errorMessageUtil) {
        this.errorMessageUtil = errorMessageUtil;
    }

    public void review(PendingCertifiedProductDTO listing, PendingCertificationResultDTO certificationResult) {
        DuplicateReviewResult<PendingCertificationResultAdditionalSoftwareDTO> addtlSoftwareDuplicateResults =
                new DuplicateReviewResult<PendingCertificationResultAdditionalSoftwareDTO>(getPredicate());
        if (certificationResult.getAdditionalSoftware() != null) {
            for (PendingCertificationResultAdditionalSoftwareDTO dto : certificationResult.getAdditionalSoftware()) {
                addtlSoftwareDuplicateResults.addObject(dto);
            }
        }
        if (addtlSoftwareDuplicateResults.duplicatesExist()) {
            listing.getWarningMessages().addAll(
                    getWarnings(addtlSoftwareDuplicateResults.getDuplicateList(),
                            Util.formatCriteriaNumber(certificationResult.getCriterion())));
            certificationResult.setAdditionalSoftware(addtlSoftwareDuplicateResults.getUniqueList());
        }
    }

    private List<String> getWarnings(List<PendingCertificationResultAdditionalSoftwareDTO> duplicates,
            String criteria) {
        List<String> warnings = new ArrayList<String>();
        for (PendingCertificationResultAdditionalSoftwareDTO duplicate : duplicates) {
            String warning = "";
            if (duplicate.getChplId() != null) {
                warning = errorMessageUtil.getMessage("listing.criteria.duplicateAdditionalSoftwareCP",
                        criteria, duplicate.getChplId(),
                        duplicate.getGrouping() == null ? "" : duplicate.getGrouping());
            } else if (duplicate.getName() != null) {
                warning = errorMessageUtil.getMessage("listing.criteria.duplicateAdditionalSoftwareNonCP",
                        criteria, duplicate.getName(),
                        duplicate.getVersion() == null ? "" : duplicate.getVersion(),
                        duplicate.getGrouping() == null ? "" : duplicate.getGrouping());
            }
            warnings.add(warning);
        }
        return warnings;
    }

    private BiPredicate<
    PendingCertificationResultAdditionalSoftwareDTO, PendingCertificationResultAdditionalSoftwareDTO> getPredicate() {
        return new BiPredicate<
                PendingCertificationResultAdditionalSoftwareDTO, PendingCertificationResultAdditionalSoftwareDTO>() {
            @Override
            public boolean test(PendingCertificationResultAdditionalSoftwareDTO dto1,
                    PendingCertificationResultAdditionalSoftwareDTO dto2) {
                if (ObjectUtils.allNotNull(dto1.getCertifiedProductId(), dto2.getCertifiedProductId())) {
                    return Objects.equals(dto1.getCertifiedProductId(), dto2.getCertifiedProductId())
                            && Objects.equals(dto1.getGrouping(), dto2.getGrouping());
                } else if (ObjectUtils.allNotNull(dto1.getChplId(), dto2.getChplId())) {
                    return Objects.equals(dto1.getChplId(), dto2.getChplId())
                            && Objects.equals(dto1.getGrouping(), dto2.getGrouping());
                } else if (ObjectUtils.allNotNull(dto1.getName(), dto2.getName())) {
                        return Objects.equals(dto1.getName(), dto2.getName())
                        && Objects.equals(dto1.getVersion(), dto2.getVersion())
                        && Objects.equals(dto1.getGrouping(), dto2.getGrouping());
                }
                return false;
            }
        };
    }
}
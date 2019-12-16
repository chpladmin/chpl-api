package gov.healthit.chpl.validation.pendingListing.reviewer.edition2014.duplicate;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultAdditionalSoftwareDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.pendingListing.reviewer.duplicate.DuplicateReviewResult;

@Component("additionalSoftware2014DuplicateReviewer")
public class AdditionalSoftware2014DuplicateReviewer {
    private ErrorMessageUtil errorMessageUtil;

    @Autowired
    public AdditionalSoftware2014DuplicateReviewer(final ErrorMessageUtil errorMessageUtil) {
        this.errorMessageUtil = errorMessageUtil;
    }

    public void review(final PendingCertifiedProductDTO listing, final PendingCertificationResultDTO certificationResult) {

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
                            certificationResult.getCriterion().getNumber()));
            certificationResult.setAdditionalSoftware(addtlSoftwareDuplicateResults.getUniqueList());
        }
    }

    private List<String> getWarnings(final List<PendingCertificationResultAdditionalSoftwareDTO> duplicates,
            final String criteria) {
        List<String> warnings = new ArrayList<String>();
        for (PendingCertificationResultAdditionalSoftwareDTO duplicate : duplicates) {
            String warning = "";
            if (duplicate.getChplId() != null) {
                warning = errorMessageUtil.getMessage("listing.criteria.duplicateAdditionalSoftwareCP.2014",
                        criteria, duplicate.getChplId());
            } else if (duplicate.getName() != null || duplicate.getVersion() != null
                    || duplicate.getGrouping() != null) {
                warning = errorMessageUtil.getMessage("listing.criteria.duplicateAdditionalSoftwareNonCP.2014",
                        criteria, duplicate.getName(), duplicate.getVersion());
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
            public boolean test(final PendingCertificationResultAdditionalSoftwareDTO dto1,
                    final PendingCertificationResultAdditionalSoftwareDTO dto2) {
                if (dto1.getChplId() != null && dto2.getChplId() != null) {

                    return dto1.getChplId().equals(dto2.getChplId());

                } else if (dto1.getName() != null && dto2.getName() != null
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
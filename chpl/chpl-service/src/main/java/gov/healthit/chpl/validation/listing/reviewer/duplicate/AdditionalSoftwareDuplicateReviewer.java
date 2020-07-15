package gov.healthit.chpl.validation.listing.reviewer.duplicate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultAdditionalSoftware;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import gov.healthit.chpl.validation.DuplicateReviewResult;

@Component("additionalSoftwareDuplicateReviewer")
public class AdditionalSoftwareDuplicateReviewer {
    private ErrorMessageUtil errorMessageUtil;

    @Autowired
    public AdditionalSoftwareDuplicateReviewer(ErrorMessageUtil errorMessageUtil) {
        this.errorMessageUtil = errorMessageUtil;
    }

    public void review(CertifiedProductSearchDetails listing, CertificationResult certificationResult) {

        DuplicateReviewResult<CertificationResultAdditionalSoftware> addtlSoftwareDuplicateResults =
                new DuplicateReviewResult<CertificationResultAdditionalSoftware>(duplicatePredicate());
        if (certificationResult.getAdditionalSoftware() != null) {
            for (CertificationResultAdditionalSoftware dto : certificationResult.getAdditionalSoftware()) {
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

    private List<String> getWarnings(List<CertificationResultAdditionalSoftware> duplicates,
            String criteria) {
        List<String> warnings = new ArrayList<String>();
        for (CertificationResultAdditionalSoftware duplicate : duplicates) {
            String warning = "";
            if (duplicate.getCertifiedProductNumber() != null) {
                warning = errorMessageUtil.getMessage("listing.criteria.duplicateAdditionalSoftwareCP",
                        criteria, duplicate.getCertifiedProductNumber(),
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

    private BiPredicate<CertificationResultAdditionalSoftware, CertificationResultAdditionalSoftware> duplicatePredicate() {
        return new BiPredicate<CertificationResultAdditionalSoftware, CertificationResultAdditionalSoftware>() {
            @Override
            public boolean test(CertificationResultAdditionalSoftware dto1,
                    CertificationResultAdditionalSoftware dto2) {
                if (ObjectUtils.allNotNull(dto1.getCertifiedProductId(), dto2.getCertifiedProductId())) {
                    return Objects.equals(dto1.getCertifiedProductId(), dto2.getCertifiedProductId())
                            && Objects.equals(dto1.getGrouping(), dto2.getGrouping());
                } else if (ObjectUtils.allNotNull(dto1.getCertifiedProductNumber(), dto2.getCertifiedProductNumber())) {
                    return Objects.equals(dto1.getCertifiedProductNumber(), dto2.getCertifiedProductNumber())
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

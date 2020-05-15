package gov.healthit.chpl.validation.listing.reviewer.edition2015.duplicate;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultAdditionalSoftware;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import gov.healthit.chpl.validation.DuplicateReviewResult;

@Component("additionalSoftware2015DuplicateReviewer")
public class AdditionalSoftware2015DuplicateReviewer {
    private ErrorMessageUtil errorMessageUtil;

    @Autowired
    public AdditionalSoftware2015DuplicateReviewer(ErrorMessageUtil errorMessageUtil) {
        this.errorMessageUtil = errorMessageUtil;
    }

    public void review(CertifiedProductSearchDetails listing, CertificationResult certificationResult) {

        DuplicateReviewResult<CertificationResultAdditionalSoftware> addtlSoftwareDuplicateResults =
                new DuplicateReviewResult<CertificationResultAdditionalSoftware>(getPredicate());

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
            if (duplicate.getCertifiedProductNumber() != null && duplicate.getGrouping() != null) {
                warning = errorMessageUtil.getMessage("listing.criteria.duplicateAdditionalSoftwareCP.2015",
                        criteria, duplicate.getCertifiedProductNumber(), duplicate.getGrouping());
            } else if (duplicate.getName() != null || duplicate.getVersion() != null
                    || duplicate.getGrouping() != null) {
                warning = errorMessageUtil.getMessage("listing.criteria.duplicateAdditionalSoftwareNonCP.2015",
                        criteria, duplicate.getName(), duplicate.getVersion(), duplicate.getGrouping());
            }
            warnings.add(warning);
        }
        return warnings;
    }

    private BiPredicate<
    CertificationResultAdditionalSoftware, CertificationResultAdditionalSoftware> getPredicate() {
        return new BiPredicate<
                CertificationResultAdditionalSoftware, CertificationResultAdditionalSoftware>() {
            @Override
            public boolean test(CertificationResultAdditionalSoftware dto1,
                    CertificationResultAdditionalSoftware dto2) {
                if (dto1.getCertifiedProductNumber() != null && dto2.getCertifiedProductNumber() != null
                        && dto1.getGrouping() != null && dto2.getGrouping() != null) {

                    return dto1.getCertifiedProductNumber().equals(dto2.getCertifiedProductNumber())
                            && dto1.getGrouping().equals(dto2.getGrouping());

                } else if (dto1.getName() != null && dto2.getName() != null
                        && dto1.getVersion() != null && dto2.getVersion() != null
                        && dto1.getGrouping() != null && dto2.getGrouping() != null) {

                    return dto1.getName().equals(dto2.getName())
                            && dto1.getVersion().equals(dto2.getVersion())
                            && dto1.getGrouping().equals(dto2.getGrouping());
                } else {
                    return false;
                }
            }
        };
    }
}

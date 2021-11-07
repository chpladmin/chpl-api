package gov.healthit.chpl.validation.listing.reviewer.duplicate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.svap.domain.CertificationResultSvap;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import gov.healthit.chpl.validation.DuplicateReviewResult;

@Component("svapDuplicateReviewer")
public class SvapDuplicateReviewer {
    private ErrorMessageUtil errorMessageUtil;

    @Autowired
    public SvapDuplicateReviewer(ErrorMessageUtil errorMessageUtil) {
        this.errorMessageUtil = errorMessageUtil;
    }

    public void review(CertifiedProductSearchDetails listing, CertificationResult certificationResult) {

        DuplicateReviewResult<CertificationResultSvap> svapDuplicateResults =
                new DuplicateReviewResult<CertificationResultSvap>(getPredicate());


        if (certificationResult.getSvaps() != null) {
            for (CertificationResultSvap svap : certificationResult.getSvaps()) {
                svapDuplicateResults.addObject(svap);
            }
        }

        if (svapDuplicateResults.duplicatesExist()) {
            listing.getWarningMessages().addAll(
                    getWarnings(svapDuplicateResults.getDuplicateList(),
                            Util.formatCriteriaNumber(certificationResult.getCriterion())));
            certificationResult.setSvaps(svapDuplicateResults.getUniqueList());
        }
    }

    private List<String> getWarnings(List<CertificationResultSvap> duplicates, String criteria) {
        List<String> warnings = new ArrayList<String>();
        for (CertificationResultSvap duplicate : duplicates) {
            String warning = errorMessageUtil.getMessage("listing.criteria.duplicateSvap",
                    criteria, duplicate.getRegulatoryTextCitation());
            warnings.add(warning);
        }
        return warnings;
    }

    private BiPredicate<CertificationResultSvap, CertificationResultSvap> getPredicate() {
        return new BiPredicate<CertificationResultSvap, CertificationResultSvap>() {
            @Override
            public boolean test(CertificationResultSvap svap1, CertificationResultSvap svap2) {
                return (ObjectUtils.allNotNull(svap1.getSvapId(), svap2.getSvapId())
                        && Objects.equals(svap1.getSvapId(),  svap2.getSvapId()))
                    || Objects.equals(svap1.getRegulatoryTextCitation(), svap2.getRegulatoryTextCitation());
            }
        };
    }
}

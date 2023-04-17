package gov.healthit.chpl.validation.listing.reviewer.duplicate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.functionalityTested.CertificationResultFunctionalityTested;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import gov.healthit.chpl.validation.DuplicateReviewResult;

@Component("functionalityTestedDuplicateReviewer")
public class FunctionalityTestedDuplicateReviewer {
    private ErrorMessageUtil errorMessageUtil;

    @Autowired
    public FunctionalityTestedDuplicateReviewer(ErrorMessageUtil errorMessageUtil) {
        this.errorMessageUtil = errorMessageUtil;
    }

    public void review(CertifiedProductSearchDetails listing, CertificationResult certificationResult) {

        DuplicateReviewResult<CertificationResultFunctionalityTested> functionalityTestedDuplicateResults =
                new DuplicateReviewResult<CertificationResultFunctionalityTested>(getPredicate());

        if (certificationResult.getFunctionalitiesTested() != null) {
            for (CertificationResultFunctionalityTested crft : certificationResult.getFunctionalitiesTested()) {
                functionalityTestedDuplicateResults.addObject(crft);
            }
        }

        if (functionalityTestedDuplicateResults.duplicatesExist()) {
            listing.addAllWarningMessages(
                    getWarnings(
                            functionalityTestedDuplicateResults.getDuplicateList(),
                            Util.formatCriteriaNumber(certificationResult.getCriterion()))
                    .stream()
                    .collect(Collectors.toSet()));
            certificationResult.setFunctionalitiesTested(functionalityTestedDuplicateResults.getUniqueList());
        }
    }

    private List<String> getWarnings(List<CertificationResultFunctionalityTested> duplicates,
            String criteria) {
        List<String> warnings = new ArrayList<String>();
        for (CertificationResultFunctionalityTested duplicate : duplicates) {
            String warning = errorMessageUtil.getMessage("listing.criteria.duplicateFunctionalityTested",
                    criteria, duplicate.getName());
            warnings.add(warning);
        }
        return warnings;
    }

    private BiPredicate<
    CertificationResultFunctionalityTested, CertificationResultFunctionalityTested> getPredicate() {
        return
                new BiPredicate<
                CertificationResultFunctionalityTested, CertificationResultFunctionalityTested>() {
            @Override
            public boolean test(CertificationResultFunctionalityTested ft1, CertificationResultFunctionalityTested ft2) {
                return (ObjectUtils.allNotNull(ft1.getFunctionalityTestedId(), ft2.getFunctionalityTestedId())
                        && Objects.equals(ft1.getFunctionalityTestedId(), ft2.getFunctionalityTestedId()))
                    || Objects.equals(ft1.getName(), ft2.getName());
            }
        };
    }
}

package gov.healthit.chpl.validation.listing.reviewer.duplicate;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.CertifiedProductTestingLab;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.DuplicateReviewResult;

@Component("atlDuplicateReviewer")
public class AtlDuplicateReviewer {
    private ErrorMessageUtil errorMessageUtil;

    @Autowired
    public AtlDuplicateReviewer(ErrorMessageUtil errorMessageUtil) {
        this.errorMessageUtil = errorMessageUtil;
    }

    public void review(CertifiedProductSearchDetails listing) {

        DuplicateReviewResult<CertifiedProductTestingLab> atlDuplicateResults =
                new DuplicateReviewResult<CertifiedProductTestingLab>(getPredicate());

        if (listing.getTestingLabs() != null) {
            for (CertifiedProductTestingLab dto : listing.getTestingLabs()) {
                atlDuplicateResults.addObject(dto);
            }
        }

        if (atlDuplicateResults.duplicatesExist()) {
            listing.getWarningMessages().addAll(getWarnings(atlDuplicateResults.getDuplicateList()));
            listing.setTestingLabs(atlDuplicateResults.getUniqueList());
        }
    }

    private List<String> getWarnings(List<CertifiedProductTestingLab> duplicates) {
        List<String> warnings = new ArrayList<String>();
        for (CertifiedProductTestingLab duplicate : duplicates) {
            String warning = errorMessageUtil.getMessage("listing.duplicateTestingLab", duplicate.getTestingLabName());
            warnings.add(warning);
        }
        return warnings;
    }

    private BiPredicate<CertifiedProductTestingLab, CertifiedProductTestingLab> getPredicate() {
        return new BiPredicate<CertifiedProductTestingLab, CertifiedProductTestingLab>() {
            @Override
            public boolean test(CertifiedProductTestingLab dto1,
                    CertifiedProductTestingLab dto2) {
                return ObjectUtils.allNotNull(dto1.getTestingLabName(), dto2.getTestingLabName())
                        && dto1.getTestingLabName().equals(dto2.getTestingLabName());
            }
        };
    }
}

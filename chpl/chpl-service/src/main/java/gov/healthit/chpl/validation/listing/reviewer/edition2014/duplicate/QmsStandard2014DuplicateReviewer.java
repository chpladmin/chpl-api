package gov.healthit.chpl.validation.listing.reviewer.edition2014.duplicate;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductQmsStandard;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.DuplicateReviewResult;

@Component("qmsStandard2014DuplicateReviewer")
public class QmsStandard2014DuplicateReviewer {
    private ErrorMessageUtil errorMessageUtil;

    @Autowired
    public QmsStandard2014DuplicateReviewer(ErrorMessageUtil errorMessageUtil) {
        this.errorMessageUtil = errorMessageUtil;
    }

    public void review(CertifiedProductSearchDetails listing) {

        DuplicateReviewResult<CertifiedProductQmsStandard> qmsStandardDuplicateResults =
                new DuplicateReviewResult<CertifiedProductQmsStandard>(getPredicate());

        if (listing.getQmsStandards() != null) {
            for (CertifiedProductQmsStandard dto : listing.getQmsStandards()) {
                qmsStandardDuplicateResults.addObject(dto);
            }
        }

        if (qmsStandardDuplicateResults.duplicatesExist()) {
            listing.getWarningMessages().addAll(getWarnings(qmsStandardDuplicateResults.getDuplicateList()));
            listing.setQmsStandards(qmsStandardDuplicateResults.getUniqueList());
        }
    }

    private List<String> getWarnings(List<CertifiedProductQmsStandard> duplicates) {
        List<String> warnings = new ArrayList<String>();
        for (CertifiedProductQmsStandard duplicate : duplicates) {
            String warning = errorMessageUtil.getMessage("listing.duplicateQmsStandard.2014", duplicate.getQmsStandardName());
            warnings.add(warning);
        }
        return warnings;
    }

    private BiPredicate<CertifiedProductQmsStandard, CertifiedProductQmsStandard> getPredicate() {
        return new BiPredicate<CertifiedProductQmsStandard, CertifiedProductQmsStandard>() {
            @Override
            public boolean test(CertifiedProductQmsStandard dto1,
                    CertifiedProductQmsStandard dto2) {
                return ObjectUtils.allNotNull(dto1.getQmsStandardName(), dto2.getQmsStandardName())
                        && dto1.getQmsStandardName().equals(dto2.getQmsStandardName());
            }
        };
    }

}

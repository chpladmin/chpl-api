package gov.healthit.chpl.validation.listing.reviewer.duplicate;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.DuplicateReviewResult;

@Component("icsSourceDuplicateReviewer")
public class IcsSourceDuplicateReviewer {

    private ErrorMessageUtil errorMessageUtil;

    @Autowired
    public IcsSourceDuplicateReviewer(ErrorMessageUtil errorMessageUtil) {
        this.errorMessageUtil = errorMessageUtil;
    }

    public void review(CertifiedProductSearchDetails listing) {
        DuplicateReviewResult<CertifiedProduct> icsSourceDuplicateResults =
                new DuplicateReviewResult<CertifiedProduct>(getPredicate());
        if (listing.getIcs() != null && listing.getIcs().getParents() != null) {
            for (CertifiedProduct dto : listing.getIcs().getParents()) {
                icsSourceDuplicateResults.addObject(dto);
            }
        }
        if (icsSourceDuplicateResults.duplicatesExist()) {
            listing.getWarningMessages().addAll(getWarnings(icsSourceDuplicateResults.getDuplicateList()));
            listing.getIcs().setParents(icsSourceDuplicateResults.getUniqueList());
        }
    }

    private List<String> getWarnings(List<CertifiedProduct> duplicates) {
        List<String> warnings = new ArrayList<String>();
        for (CertifiedProduct duplicate : duplicates) {
            String warning = errorMessageUtil.getMessage("listing.duplicateIcsSource",
                    duplicate.getChplProductNumber());
            warnings.add(warning);
        }
        return warnings;
    }

    private BiPredicate<CertifiedProduct, CertifiedProduct> getPredicate() {
        return new BiPredicate<CertifiedProduct, CertifiedProduct>() {
            @Override
            public boolean test(CertifiedProduct dto1,
                    CertifiedProduct dto2) {
                return ObjectUtils.allNotNull(dto1.getChplProductNumber(), dto2.getChplProductNumber())
                        && dto1.getChplProductNumber().equals(dto2.getChplProductNumber());
            }
        };
    }
}

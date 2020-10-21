package gov.healthit.chpl.validation.listing.reviewer.duplicate;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.ListingMipsMeasure;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.DuplicateReviewResult;

@Component("mipsMeasureDuplicateReviewer")
public class MipsMeasureDuplicateReviewer {
    private ErrorMessageUtil errorMessageUtil;

    @Autowired
    public MipsMeasureDuplicateReviewer(ErrorMessageUtil errorMessageUtil) {
        this.errorMessageUtil = errorMessageUtil;
    }

    public void review(CertifiedProductSearchDetails listing) {

        DuplicateReviewResult<ListingMipsMeasure> mipsMeasureDuplicateResults =
                new DuplicateReviewResult<ListingMipsMeasure>(getPredicate());
        if (listing.getMipsMeasures() != null) {
            for (ListingMipsMeasure measure : listing.getMipsMeasures()) {
                mipsMeasureDuplicateResults.addObject(measure);
            }
        }
        if (mipsMeasureDuplicateResults.duplicatesExist()) {
            listing.getWarningMessages().addAll(getWarnings(mipsMeasureDuplicateResults.getDuplicateList()));
            listing.setMipsMeasures(mipsMeasureDuplicateResults.getUniqueList());
        }
    }

    private List<String> getWarnings(List<ListingMipsMeasure> duplicates) {
        List<String> warnings = new ArrayList<String>();
        for (ListingMipsMeasure duplicate : duplicates) {
            String warning = errorMessageUtil.getMessage("listing.duplicateMipsMeasure",
                    duplicate.getMeasurementType().getName(),
                    duplicate.getMeasure().getAbbreviation());
            warnings.add(warning);
        }
        return warnings;
    }

    private BiPredicate<ListingMipsMeasure, ListingMipsMeasure> getPredicate() {
        return new BiPredicate<ListingMipsMeasure, ListingMipsMeasure>() {
            @Override
            public boolean test(ListingMipsMeasure measure1,
                    ListingMipsMeasure measure2) {
                return ObjectUtils.allNotNull(measure1, measure2, measure1.getMeasure(), measure2.getMeasure(),
                        measure2.getMeasurementType(), measure2.getMeasurementType())
                        && measure1.getMeasure().matches(measure2.getMeasure())
                        && measure1.getMeasurementType().matches(measure2.getMeasurementType());
            }
        };
    }

}

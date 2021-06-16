package gov.healthit.chpl.validation.listing.reviewer.duplicate;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.ListingMeasure;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.DuplicateReviewResult;

@Component("measureDuplicateReviewer")
public class MeasureDuplicateReviewer {
    private ErrorMessageUtil errorMessageUtil;

    @Autowired
    public MeasureDuplicateReviewer(ErrorMessageUtil errorMessageUtil) {
        this.errorMessageUtil = errorMessageUtil;
    }

    public void review(CertifiedProductSearchDetails listing) {

        DuplicateReviewResult<ListingMeasure> measureDuplicateResults =
                new DuplicateReviewResult<ListingMeasure>(getPredicate());
        if (listing.getMeasures() != null) {
            for (ListingMeasure measure : listing.getMeasures()) {
                measureDuplicateResults.addObject(measure);
            }
        }
        if (measureDuplicateResults.duplicatesExist()) {
            listing.getErrorMessages().addAll(getErrors(measureDuplicateResults.getDuplicateList()));
        }
    }

    private List<String> getErrors(List<ListingMeasure> duplicates) {
        List<String> errors = new ArrayList<String>();
        for (ListingMeasure duplicate : duplicates) {
            String error = errorMessageUtil.getMessage("listing.duplicateMeasure",
                    duplicate.getMeasureType().getName(),
                    duplicate.getMeasure().getName(),
                    duplicate.getMeasure().getAbbreviation());
            errors.add(error);
        }
        return errors;
    }

    private BiPredicate<ListingMeasure, ListingMeasure> getPredicate() {
        return new BiPredicate<ListingMeasure, ListingMeasure>() {
            @Override
            public boolean test(ListingMeasure measure1,
                    ListingMeasure measure2) {
                return ObjectUtils.allNotNull(measure1, measure2, measure1.getMeasure(), measure2.getMeasure(),
                        measure2.getMeasureType(), measure2.getMeasureType())
                        && measure1.getMeasure().matches(measure2.getMeasure())
                        && measure1.getMeasureType().matches(measure2.getMeasureType());
            }
        };
    }

}

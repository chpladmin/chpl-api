package gov.healthit.chpl.validation.listing.reviewer.duplicate;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

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

        DuplicateReviewResult<ListingMeasure> measureDuplicateResultsSameCriteria = new DuplicateReviewResult<ListingMeasure>(getPredicateSameCriteria());
        if (listing.getMeasures() != null) {
            for (ListingMeasure measure : listing.getMeasures()) {
                measureDuplicateResultsSameCriteria.addObject(measure);
            }
        }
        if (measureDuplicateResultsSameCriteria.duplicatesExist()) {
            listing.addAllWarningMessages(getMessages(measureDuplicateResultsSameCriteria.getDuplicateList(), "listing.duplicateMeasure.sameCriteria")
                    .stream()
                    .collect(Collectors.toSet()));
            listing.setMeasures(measureDuplicateResultsSameCriteria.getUniqueList());
        }

        DuplicateReviewResult<ListingMeasure> measureDuplicateResultsDifferentCriteria = new DuplicateReviewResult<ListingMeasure>(getPredicateDifferentCriteria());
        if (listing.getMeasures() != null) {
            for (ListingMeasure measure : listing.getMeasures()) {
                measureDuplicateResultsDifferentCriteria.addObject(measure);
            }
        }
        if (measureDuplicateResultsDifferentCriteria.duplicatesExist()) {
            listing.addAllBusinessErrorMessages(getMessages(measureDuplicateResultsDifferentCriteria.getDuplicateList(), "listing.duplicateMeasure.differentCriteria"));
        }
    }

    private BiPredicate<ListingMeasure, ListingMeasure> getPredicateSameCriteria() {
        return new BiPredicate<ListingMeasure, ListingMeasure>() {
            @Override
            public boolean test(ListingMeasure measure1, ListingMeasure measure2) {
                if (!ObjectUtils.allNotNull(measure1, measure2, measure1.getMeasure(), measure2.getMeasure(), measure2.getMeasureType(), measure2.getMeasureType())) {
                    return false;
                }
                if (!measure1.getMeasure().matches(measure2.getMeasure())) {
                    return false;
                }
                if (!measure1.matchesCriteria(measure2)) {
                    return false;
                }
                if (!measure1.getMeasureType().matches(measure2.getMeasureType())) {
                    return false;
                }
                return true;
            }
        };
    }

    private BiPredicate<ListingMeasure, ListingMeasure> getPredicateDifferentCriteria() {
        return new BiPredicate<ListingMeasure, ListingMeasure>() {
            @Override
            public boolean test(ListingMeasure measure1, ListingMeasure measure2) {
                if (!ObjectUtils.allNotNull(measure1, measure2, measure1.getMeasure(), measure2.getMeasure(), measure2.getMeasureType(), measure2.getMeasureType())) {
                    return false;
                }
                if (!measure1.getMeasure().matches(measure2.getMeasure())) {
                    return false;
                }
                if (measure1.matchesCriteria(measure2)) {
                    return false;
                }
                if (!measure1.getMeasureType().matches(measure2.getMeasureType())) {
                    return false;
                }
                return true;
            }
        };
    }

    private Set<String> getMessages(List<ListingMeasure> duplicates, String msgKey) {
        Set<String> messages = new HashSet<String>();
        for (ListingMeasure duplicate : duplicates) {
            String message = errorMessageUtil.getMessage(msgKey,
                    duplicate.getMeasureType().getName(),
                    duplicate.getMeasure().getName(),
                    duplicate.getMeasure().getAbbreviation());
            messages.add(message);
        }
        return messages;
    }
}

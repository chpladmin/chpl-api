package gov.healthit.chpl.validation.pendingListing.reviewer.duplicate;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductMeasureDTO;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.DuplicateReviewResult;

@Component("pendingMeasureDuplicateReviewer")
public class MeasureDuplicateReviewer {
    private ErrorMessageUtil errorMessageUtil;

    @Autowired
    public MeasureDuplicateReviewer(ErrorMessageUtil errorMessageUtil) {
        this.errorMessageUtil = errorMessageUtil;
    }

    public void review(PendingCertifiedProductDTO listing) {

        DuplicateReviewResult<PendingCertifiedProductMeasureDTO> measureDuplicateResultsSameCriteria =
                new DuplicateReviewResult<PendingCertifiedProductMeasureDTO>(getPredicateSameCriteria());
        if (listing.getMeasures() != null) {
            for (PendingCertifiedProductMeasureDTO measure : listing.getMeasures()) {
                measureDuplicateResultsSameCriteria.addObject(measure);
            }
        }
        if (measureDuplicateResultsSameCriteria.duplicatesExist()) {
            listing.getWarningMessages().addAll(getMessages(measureDuplicateResultsSameCriteria.getDuplicateList(), "listing.duplicateMeasure.sameCriteria"));
            listing.setMeasures(measureDuplicateResultsSameCriteria.getUniqueList());
        }

        DuplicateReviewResult<PendingCertifiedProductMeasureDTO> measureDuplicateResultsDifferentCriteria =
                new DuplicateReviewResult<PendingCertifiedProductMeasureDTO>(getPredicateDifferentCriteria());
        if (listing.getMeasures() != null) {
            for (PendingCertifiedProductMeasureDTO measure : listing.getMeasures()) {
                measureDuplicateResultsDifferentCriteria.addObject(measure);
            }
        }
        if (measureDuplicateResultsDifferentCriteria.duplicatesExist()) {
            listing.getErrorMessages().addAll(getMessages(measureDuplicateResultsDifferentCriteria.getDuplicateList(), "listing.duplicateMeasure.differentCriteria"));
        }
    }

    private BiPredicate<PendingCertifiedProductMeasureDTO, PendingCertifiedProductMeasureDTO> getPredicateSameCriteria() {
        return new BiPredicate<PendingCertifiedProductMeasureDTO, PendingCertifiedProductMeasureDTO>() {
            @Override
            public boolean test(PendingCertifiedProductMeasureDTO measure1,
                    PendingCertifiedProductMeasureDTO measure2) {
                if (!ObjectUtils.allNotNull(measure1, measure2, measure1.getMeasure(), measure2.getMeasure(), measure1.getMeasureType(), measure2.getMeasureType())) {
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


    private BiPredicate<PendingCertifiedProductMeasureDTO, PendingCertifiedProductMeasureDTO> getPredicateDifferentCriteria() {
        return new BiPredicate<PendingCertifiedProductMeasureDTO, PendingCertifiedProductMeasureDTO>() {
            @Override
            public boolean test(PendingCertifiedProductMeasureDTO measure1,
                    PendingCertifiedProductMeasureDTO measure2) {
                if (!ObjectUtils.allNotNull(measure1, measure2, measure1.getMeasure(), measure2.getMeasure(), measure1.getMeasureType(), measure2.getMeasureType())) {
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

    private List<String> getMessages(List<PendingCertifiedProductMeasureDTO> duplicates, String msgKey) {
        List<String> messages = new ArrayList<String>();
        for (PendingCertifiedProductMeasureDTO duplicate : duplicates) {
            String message = errorMessageUtil.getMessage(msgKey,
                    duplicate.getMeasureType().getName(),
                    duplicate.getMeasure().getName(),
                    duplicate.getMeasure().getAbbreviation());
            messages.add(message);
        }
        return messages;
    }

}

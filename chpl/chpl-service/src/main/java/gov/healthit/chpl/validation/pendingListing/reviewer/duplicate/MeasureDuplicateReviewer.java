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

        DuplicateReviewResult<PendingCertifiedProductMeasureDTO> measureDuplicateResults =
                new DuplicateReviewResult<PendingCertifiedProductMeasureDTO>(getPredicate());
        if (listing.getMeasures() != null) {
            for (PendingCertifiedProductMeasureDTO measure : listing.getMeasures()) {
                measureDuplicateResults.addObject(measure);
            }
        }
        if (measureDuplicateResults.duplicatesExist()) {
            listing.getWarningMessages().addAll(getWarnings(measureDuplicateResults.getDuplicateList()));
            listing.setMeasures(measureDuplicateResults.getUniqueList());
        }
    }

    private List<String> getWarnings(List<PendingCertifiedProductMeasureDTO> duplicates) {
        List<String> warnings = new ArrayList<String>();
        for (PendingCertifiedProductMeasureDTO duplicate : duplicates) {
            String warning = errorMessageUtil.getMessage("listing.duplicateMeasure",
                    duplicate.getMeasurementType().getName(),
                    duplicate.getMeasure().getName(),
                    duplicate.getMeasure().getAbbreviation());
            warnings.add(warning);
        }
        return warnings;
    }

    private BiPredicate<PendingCertifiedProductMeasureDTO, PendingCertifiedProductMeasureDTO> getPredicate() {
        return new BiPredicate<PendingCertifiedProductMeasureDTO, PendingCertifiedProductMeasureDTO>() {
            @Override
            public boolean test(PendingCertifiedProductMeasureDTO measure1,
                    PendingCertifiedProductMeasureDTO measure2) {
                return ObjectUtils.allNotNull(measure1, measure2, measure1.getMeasure(), measure2.getMeasure(),
                        measure2.getMeasurementType(), measure2.getMeasurementType())
                        && measure1.getMeasure().matches(measure2.getMeasure())
                        && measure1.getMeasurementType().matches(measure2.getMeasurementType());
            }
        };
    }

}

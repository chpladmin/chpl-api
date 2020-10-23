package gov.healthit.chpl.validation.pendingListing.reviewer.duplicate;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductMipsMeasureDTO;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.DuplicateReviewResult;

@Component("pendingMipsMeasureDuplicateReviewer")
public class MipsMeasureDuplicateReviewer {
    private ErrorMessageUtil errorMessageUtil;

    @Autowired
    public MipsMeasureDuplicateReviewer(ErrorMessageUtil errorMessageUtil) {
        this.errorMessageUtil = errorMessageUtil;
    }

    public void review(PendingCertifiedProductDTO listing) {

        DuplicateReviewResult<PendingCertifiedProductMipsMeasureDTO> mipsMeasureDuplicateResults =
                new DuplicateReviewResult<PendingCertifiedProductMipsMeasureDTO>(getPredicate());
        if (listing.getMipsMeasures() != null) {
            for (PendingCertifiedProductMipsMeasureDTO measure : listing.getMipsMeasures()) {
                mipsMeasureDuplicateResults.addObject(measure);
            }
        }
        if (mipsMeasureDuplicateResults.duplicatesExist()) {
            listing.getWarningMessages().addAll(getWarnings(mipsMeasureDuplicateResults.getDuplicateList()));
            listing.setMipsMeasures(mipsMeasureDuplicateResults.getUniqueList());
        }
    }

    private List<String> getWarnings(List<PendingCertifiedProductMipsMeasureDTO> duplicates) {
        List<String> warnings = new ArrayList<String>();
        for (PendingCertifiedProductMipsMeasureDTO duplicate : duplicates) {
            String warning = errorMessageUtil.getMessage("listing.duplicateMipsMeasure",
                    duplicate.getMeasurementType().getName(),
                    duplicate.getMeasure().getAbbreviation());
            warnings.add(warning);
        }
        return warnings;
    }

    private BiPredicate<PendingCertifiedProductMipsMeasureDTO, PendingCertifiedProductMipsMeasureDTO> getPredicate() {
        return new BiPredicate<PendingCertifiedProductMipsMeasureDTO, PendingCertifiedProductMipsMeasureDTO>() {
            @Override
            public boolean test(PendingCertifiedProductMipsMeasureDTO measure1,
                    PendingCertifiedProductMipsMeasureDTO measure2) {
                return ObjectUtils.allNotNull(measure1, measure2, measure1.getMeasure(), measure2.getMeasure(),
                        measure2.getMeasurementType(), measure2.getMeasurementType())
                        && measure1.getMeasure().matches(measure2.getMeasure())
                        && measure1.getMeasurementType().matches(measure2.getMeasurementType());
            }
        };
    }

}

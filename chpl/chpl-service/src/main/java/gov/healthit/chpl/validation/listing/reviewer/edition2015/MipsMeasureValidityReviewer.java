package gov.healthit.chpl.validation.listing.reviewer.edition2015;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.ListingMipsMeasure;
import gov.healthit.chpl.listing.mipsMeasure.MipsMeasureDAO;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.listing.reviewer.Reviewer;

@Component("mipsMeasureValidityReviewer")
public class MipsMeasureValidityReviewer implements Reviewer {

    private MipsMeasureDAO mipsMeasureDao;
    private ErrorMessageUtil errorMessageUtil;

    @Autowired
    public MipsMeasureValidityReviewer(MipsMeasureDAO mipsMeasureDao, ErrorMessageUtil errorMEssageUtil) {
        this.mipsMeasureDao = mipsMeasureDao;
        this.errorMessageUtil = errorMEssageUtil;
    }

    @Override
    public void review(CertifiedProductSearchDetails listing) {
        //TODO: if they have attested to G1 or G2 criterion, is at least one measure required?
        //TODO: does it matter if they attested to G1 criterion but marked Mips measures as type G2 (or vice versa)?
        //TODO: if a Mips measure is removed and the listing has no ICS, is it allowed?
        //TODO: add reviewer for duplicates

        for (ListingMipsMeasure measure : listing.getMipsMeasures()) {
            if (measure.getMeasure() != null && measure.getMeasure().getId() == null) {
            }
        }
        //for each measure in the listing
            //make sure all the associated criteria are equal to the allowed criteria
            //unless the measure has criteriaSelectionRequired, then the associated criteria should be a subset of allowed

    }
}

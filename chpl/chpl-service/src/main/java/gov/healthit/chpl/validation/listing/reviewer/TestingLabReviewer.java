package gov.healthit.chpl.validation.listing.reviewer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.ErrorMessageUtil;

/**
 * This class is separated from the RequiredDataReviewer to allow it to be applied separately to 2014 new vs legacy listings. Testing labs are not required for
 * legacy listings.
 *
 * @author kekey
 *
 */
@Component("edition20142015testingLabReviewer")
public class TestingLabReviewer implements Reviewer {
    @Autowired
    private ErrorMessageUtil msgUtil;

    @Override
    public void review(CertifiedProductSearchDetails listing) {
        if (listing.getTestingLabs() == null || listing.getTestingLabs().size() == 0) {
            listing.addDataErrorMessage(msgUtil.getMessage("atl.notFound"));
        }
    }
}

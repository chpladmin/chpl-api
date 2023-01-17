package gov.healthit.chpl.scheduler.job.ics.reviewer;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.ListingGraphDAO;
import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;

@Component
public class IncorrectIcsIncrementReviewer extends IcsErrorsReviewer {
    private static final int MIN_NUMBER_TO_NOT_NEED_PREFIX = 10;

    private ListingGraphDAO listingGraphDao;
    private String errorMessage;

    @Autowired
    public IncorrectIcsIncrementReviewer(ListingGraphDAO listingGraphDao,
            @Value("${ics.badIncrementError}") String errorMessage) {
        this.listingGraphDao = listingGraphDao;
        this.errorMessage = errorMessage;
    }

    @Override
    public String getIcsError(CertifiedProductSearchDetails listing) {
        // check if this listing has correct ICS increment
        // this listing's ICS code must be greater than the max of parent
        // ICS codes
        if (hasIcs(listing) && listing.getIcs() != null && listing.getIcs().getParents() != null
                && listing.getIcs().getParents().size() > 0) {
            List<Long> parentIds = new ArrayList<Long>();
            for (CertifiedProduct potentialParent : listing.getIcs().getParents()) {
                parentIds.add(potentialParent.getId());
            }

            Integer icsCode = getIcsCode(listing);
            Integer largestIcs = listingGraphDao.getLargestIcs(parentIds);
            int expectedIcsCode = largestIcs.intValue() + 1;
            if (icsCode.intValue() != expectedIcsCode) {
                String existing = (icsCode.toString().length() == 1 ? "0" : "") + icsCode.toString();
                String expected = (expectedIcsCode < MIN_NUMBER_TO_NOT_NEED_PREFIX ? "0" : "") + expectedIcsCode;
                return String.format(errorMessage, existing, expected);
            }
        }
        return null;
    }
}

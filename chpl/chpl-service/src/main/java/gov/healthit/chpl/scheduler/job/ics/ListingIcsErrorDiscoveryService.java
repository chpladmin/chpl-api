package gov.healthit.chpl.scheduler.job.ics;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.scheduler.job.ics.reviewer.GapWithoutIcsReviewer;
import gov.healthit.chpl.scheduler.job.ics.reviewer.IcsErrorsReviewer;
import gov.healthit.chpl.scheduler.job.ics.reviewer.IcsWithoutParentsReviewer;
import gov.healthit.chpl.scheduler.job.ics.reviewer.IncorrectIcsIncrementReviewer;
import gov.healthit.chpl.scheduler.job.ics.reviewer.MissingIcsSurveillanceReviewer;

@Service
public class ListingIcsErrorDiscoveryService {
    private List<IcsErrorsReviewer> icsErrorsReviewers;

    @Autowired
    public ListingIcsErrorDiscoveryService(IcsWithoutParentsReviewer icsWithParentsReviewer,
            IncorrectIcsIncrementReviewer incorrectIcsIncrementReviewer,
            GapWithoutIcsReviewer gapWithoutIcsReviewer,
            MissingIcsSurveillanceReviewer missingIcsSurveillanceReviewer) {
        icsErrorsReviewers = new ArrayList<IcsErrorsReviewer>();
        icsErrorsReviewers.add(icsWithParentsReviewer);
        icsErrorsReviewers.add(incorrectIcsIncrementReviewer);
        icsErrorsReviewers.add(gapWithoutIcsReviewer);
        icsErrorsReviewers.add(missingIcsSurveillanceReviewer);
    }

    public String getIcsErrorMessage(CertifiedProductSearchDetails listing) {
        String errorMessage = null;
        Iterator<IcsErrorsReviewer> reviewerIter = icsErrorsReviewers.iterator();
        while (reviewerIter.hasNext() && StringUtils.isEmpty(errorMessage)) {
            errorMessage = reviewerIter.next().getIcsError(listing);
        }
        return errorMessage;
    }
}

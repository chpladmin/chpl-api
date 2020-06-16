package gov.healthit.chpl.service;

import java.util.List;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.compliance.DirectReview;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class DirectReviewService {
    public List<DirectReview> populateDirectReviews(CertifiedProductSearchDetails listing) {
        LOGGER.info("Fetching direct review data for listing " + listing.getChplProductNumber());
        //TODO: fetch data
        return null;
    }
}

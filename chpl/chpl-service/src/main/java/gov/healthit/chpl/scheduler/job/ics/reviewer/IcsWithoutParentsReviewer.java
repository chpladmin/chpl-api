package gov.healthit.chpl.scheduler.job.ics.reviewer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "icsErrorsReportCreatorJobLogger")
@Component
public class IcsWithoutParentsReviewer extends IcsErrorsReviewer {

    private String errorMessage;

    @Autowired
    public IcsWithoutParentsReviewer(@Value("${ics.noInheritanceError}") String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public String getIcsError(CertifiedProductSearchDetails listing) {
        // check if listing has ICS but no family ties
        if (hasIcs(listing)
                && (listing.getIcs() == null || listing.getIcs().getParents() == null
                || listing.getIcs().getParents().size() == 0)) {
            LOGGER.info("\tListing " + listing.getId() + " has ICS but no parents.");
            return errorMessage;
        }
        return null;
    }
}

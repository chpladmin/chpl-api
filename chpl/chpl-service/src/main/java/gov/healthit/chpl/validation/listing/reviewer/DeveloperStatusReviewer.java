package gov.healthit.chpl.validation.listing.reviewer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.DeveloperStatusEventDTO;
import gov.healthit.chpl.entity.developer.DeveloperStatusType;
import gov.healthit.chpl.exception.EntityRetrievalException;

@Component("developerStatusReviewer")
public class DeveloperStatusReviewer implements Reviewer {

    @Autowired private DeveloperDAO developerDao;

    public void review(CertifiedProductSearchDetails listing) {
        try {
            if (listing.getDeveloper() != null && listing.getDeveloper().getDeveloperId() != null) {
                DeveloperDTO developer = developerDao.getById(listing.getDeveloper().getDeveloperId());
                if (developer != null) {
                    DeveloperStatusEventDTO mostRecentStatus = developer.getStatus();
                    if (mostRecentStatus == null || mostRecentStatus.getStatus() == null) {
                        listing.getErrorMessages().add("The current status of the developer " + developer.getName()
                        + " cannot be determined. A developer must be listed as Active in order to update certified products belongong to it.");
                    } else if (!mostRecentStatus.getStatus().getStatusName()
                            .equals(DeveloperStatusType.Active.toString())) {
                        listing.getErrorMessages().add("The developer " + developer.getName() + " has a status of "
                                + mostRecentStatus.getStatus().getStatusName()
                                + ". Certified products belonging to this developer cannot be updated until its status returns to Active.");
                    }
                } else {
                    listing.getErrorMessages()
                    .add("Could not find developer with id " + listing.getDeveloper().getDeveloperId());
                }
            }
        } catch (final EntityRetrievalException ex) {
            listing.getErrorMessages()
            .add("Could not find distinct developer with id " + listing.getDeveloper().getDeveloperId());
        }
    }
}

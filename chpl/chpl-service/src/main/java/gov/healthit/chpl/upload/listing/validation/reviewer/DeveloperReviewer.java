package gov.healthit.chpl.upload.listing.validation.reviewer;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.DeveloperStatus;
import gov.healthit.chpl.entity.developer.DeveloperStatusType;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.listing.reviewer.Reviewer;

@Component("developerReviewer")
public class DeveloperReviewer implements Reviewer {
    private DeveloperDAO developerDao;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public DeveloperReviewer(DeveloperDAO developerDao,
            ErrorMessageUtil msgUtil) {
        this.developerDao = developerDao;
        this.msgUtil = msgUtil;
    }

    public void review(CertifiedProductSearchDetails listing) {
        Developer developer = listing.getDeveloper();
        if (developer == null) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.missingDeveloper"));
            return;
        }

        checkDeveloperStatus(listing);
    }

    private void checkDeveloperStatus(CertifiedProductSearchDetails listing) {
        Developer developer = listing.getDeveloper();
        DeveloperStatus mostRecentStatus = developer.getStatus();
        if (mostRecentStatus == null || StringUtils.isEmpty(mostRecentStatus.getStatus())) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.developer.noStatusFound.noUpdate",
                    developer.getName() != null ? developer.getName() : "?"));
        } else if (!mostRecentStatus.getStatus().equals(DeveloperStatusType.Active.getName())) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.developer.notActive.noCreate",
                    developer.getName() != null ? developer.getName() : "?",
                    mostRecentStatus.getStatus()));
        }
    }
}

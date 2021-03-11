package gov.healthit.chpl.upload.listing.validation.reviewer;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.manager.DeveloperManager;
import gov.healthit.chpl.util.DeveloperMapper;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.listing.reviewer.Reviewer;

@Component("developerReviewer")
public class DeveloperReviewer implements Reviewer {
    private DeveloperManager developerManager;
    private ErrorMessageUtil msgUtil;
    private DeveloperMapper developerMapper;

    @Autowired
    public DeveloperReviewer(DeveloperManager developerManager,
            ErrorMessageUtil msgUtil) {
        this.developerManager = developerManager;
        this.msgUtil = msgUtil;
        this.developerMapper = new DeveloperMapper();
    }

    public void review(CertifiedProductSearchDetails listing) {
        Developer developer = listing.getDeveloper();
        if (developer == null) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.missingDeveloper"));
            return;
        }

        Set<String> developerErrorMessages = null;
        DeveloperDTO developerDto = developerMapper.to(developer);
        if (developer.getDeveloperId() != null) {
            developerErrorMessages = developerManager.runSystemValidations(developerDto);
        } else {
            developerErrorMessages = developerManager.runCreateValidations(developerDto);
        }

        if (developerErrorMessages != null) {
            developerErrorMessages.stream()
                .forEach(errMsg -> listing.getErrorMessages().add(errMsg));
        }
    }
}

package gov.healthit.chpl.validation.listing.reviewer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.DeveloperStatusEventDTO;
import gov.healthit.chpl.entity.developer.DeveloperStatusType;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component("developerStatusReviewer")
public class DeveloperStatusReviewer implements Reviewer {

    private DeveloperDAO developerDao;
    private ResourcePermissions resourcePermissions;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public DeveloperStatusReviewer(DeveloperDAO developerDao,
            ResourcePermissions resourcePermissions,
            ErrorMessageUtil msgUtil) {
        this.developerDao = developerDao;
        this.resourcePermissions = resourcePermissions;
        this.msgUtil = msgUtil;
    }

    public void review(CertifiedProductSearchDetails listing) {
        try {
            if (listing.getDeveloper() != null && listing.getDeveloper().getDeveloperId() != null) {
                DeveloperDTO developer = developerDao.getById(listing.getDeveloper().getDeveloperId());
                if (developer != null) {
                    DeveloperStatusEventDTO mostRecentStatus = developer.getStatus();
                    if (mostRecentStatus == null || mostRecentStatus.getStatus() == null) {
                        listing.getErrorMessages().add(msgUtil.getMessage(
                                "listing.developer.noStatusFound", developer.getName()));
                    } else {
                        if ((resourcePermissions.isUserRoleAdmin() || resourcePermissions.isUserRoleOnc())
                                && !checkAdminOrOncAllowedToEdit(developer)) {

                        } else if (!checkAcbAllowedToEdit(developer)) {
                            listing.getErrorMessages().add(msgUtil.getMessage(
                                    "listing.developer.notActive", developer.getName(), mostRecentStatus.getStatus().getStatusName()));
                        }
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

    private boolean checkAcbAllowedToEdit(DeveloperDTO developer) {
        DeveloperStatusEventDTO mostRecentStatus = developer.getStatus();
        return mostRecentStatus.getStatus().getStatusName().equals(DeveloperStatusType.Active.toString());
    }

    private boolean checkAdminOrOncAllowedToEdit(DeveloperDTO developer) {
        DeveloperStatusEventDTO mostRecentStatus = developer.getStatus();
        return mostRecentStatus.getStatus().getStatusName().equals(DeveloperStatusType.Active.toString())
                || mostRecentStatus.getStatus().getStatusName().equals(DeveloperStatusType.UnderCertificationBanByOnc.toString());
    }
}

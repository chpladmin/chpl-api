package gov.healthit.chpl.validation.pendingListing.reviewer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.DeveloperStatusEventDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.entity.developer.DeveloperStatusType;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component("pendingDeveloperStatusReviewer")
public class DeveloperStatusReviewer implements Reviewer {
    private static final Logger LOGGER = LogManager.getLogger(DeveloperStatusReviewer.class);

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

    public void review(PendingCertifiedProductDTO listing) {
        try {
            if (listing.getDeveloperId() != null) {
                DeveloperDTO developer = developerDao.getById(listing.getDeveloperId());
                if (developer != null) {
                    DeveloperStatusEventDTO mostRecentStatus = developer.getStatus();
                    if (mostRecentStatus == null || mostRecentStatus.getStatus() == null) {
                        listing.getErrorMessages().add(msgUtil.getMessage(
                                "listing.developer.noStatusFound.noCreate", developer.getName()));
                    } else {
                        if ((resourcePermissions.isUserRoleAdmin() || resourcePermissions.isUserRoleOnc())
                                && !checkAdminOrOncAllowedToCreate(developer)) {
                            listing.getErrorMessages().add(msgUtil.getMessage(
                                    "listing.developer.notActiveOrBanned.noCreate",
                                    developer.getName(), mostRecentStatus.getStatus().getStatusName(),
                                    DeveloperStatusType.Active.getName(),
                                    DeveloperStatusType.UnderCertificationBanByOnc.getName()));
                        } else if (!checkAcbAllowedToCreate(developer)) {
                            listing.getErrorMessages().add(msgUtil.getMessage(
                                    "listing.developer.notActive.noCreate", developer.getName(), mostRecentStatus.getStatus().getStatusName()));
                        }
                    }
                } else {
                    listing.getErrorMessages().add(msgUtil.getMessage("developer.notFound"));
                }
            }
        } catch (final EntityRetrievalException ex) {
            listing.getErrorMessages().add(msgUtil.getMessage("developer.notFound"));
            LOGGER.error(ex.getMessage(), ex);
        }
    }


    private boolean checkAcbAllowedToCreate(DeveloperDTO developer) {
        DeveloperStatusEventDTO mostRecentStatus = developer.getStatus();
        return mostRecentStatus.getStatus().getStatusName().equals(DeveloperStatusType.Active.toString());
    }

    private boolean checkAdminOrOncAllowedToCreate(DeveloperDTO developer) {
        DeveloperStatusEventDTO mostRecentStatus = developer.getStatus();
        return mostRecentStatus.getStatus().getStatusName().equals(DeveloperStatusType.Active.toString())
                || mostRecentStatus.getStatus().getStatusName().equals(DeveloperStatusType.UnderCertificationBanByOnc.toString());
    }
}

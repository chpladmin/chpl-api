package gov.healthit.chpl.upload.listing.validation.reviewer;

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
import lombok.extern.log4j.Log4j2;

@Component("listingUploadDeveloperStatusReviewer")
@Log4j2
public class DeveloperStatusReviewer {
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
                                "listing.developer.noStatusFound.noCreate", developer.getName()));
                    } else {
                        if (resourcePermissions.isUserRoleAdmin() || resourcePermissions.isUserRoleOnc()) {
                            if (!checkAdminOrOncAllowedToCreate(developer)) {
                                listing.getErrorMessages().add(msgUtil.getMessage(
                                    "listing.developer.notActiveOrBanned.noCreate",
                                    developer.getName(), mostRecentStatus.getStatus().getStatusName(),
                                    DeveloperStatusType.Active.getName(),
                                    DeveloperStatusType.UnderCertificationBanByOnc.getName()));
                            }
                        } else if (!checkAcbAllowedToCreate(developer)) {
                            listing.getErrorMessages().add(msgUtil.getMessage(
                                    "listing.developer.notActive.noCreate",
                                    developer.getName(), mostRecentStatus.getStatus().getStatusName()));
                        }
                    }
                } else {
                    listing.getErrorMessages().add(msgUtil.getMessage("developer.notFound"));
                }
            }
        } catch (EntityRetrievalException ex) {
            listing.getErrorMessages().add(msgUtil.getMessage("developer.notFound"));
            LOGGER.error(ex.getMessage(), ex);
        }
    }


    private boolean checkAcbAllowedToCreate(DeveloperDTO developer) {
        DeveloperStatusEventDTO mostRecentStatus = developer.getStatus();
        return mostRecentStatus.getStatus().getStatusName().equals(DeveloperStatusType.Active.getName());
    }

    private boolean checkAdminOrOncAllowedToCreate(DeveloperDTO developer) {
        DeveloperStatusEventDTO mostRecentStatus = developer.getStatus();
        return mostRecentStatus.getStatus().getStatusName().equals(DeveloperStatusType.Active.getName())
                || mostRecentStatus.getStatus().getStatusName().equals(DeveloperStatusType.UnderCertificationBanByOnc.getName());
    }
}

package gov.healthit.chpl.validation.listing.reviewer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.DeveloperStatus;
import gov.healthit.chpl.entity.developer.DeveloperStatusType;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.permissions.ResourcePermissionsFactory;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component("developerStatusReviewer")
public class DeveloperStatusReviewer implements Reviewer {
    private static final Logger LOGGER = LogManager.getLogger(DeveloperStatusReviewer.class);

    private DeveloperDAO developerDao;
    private ResourcePermissionsFactory resourcePermissionsFactory;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public DeveloperStatusReviewer(DeveloperDAO developerDao,
            ResourcePermissionsFactory resourcePermissionsFactory,
            ErrorMessageUtil msgUtil) {
        this.developerDao = developerDao;
        this.resourcePermissionsFactory = resourcePermissionsFactory;
        this.msgUtil = msgUtil;
    }

    @Override
    public void review(CertifiedProductSearchDetails listing) {
        try {
            if (listing.getDeveloper() != null && listing.getDeveloper().getId() != null) {
                Developer developer = developerDao.getById(listing.getDeveloper().getId());
                if (developer != null) {
                    DeveloperStatus mostRecentStatus = developer.getStatus();
                    if (mostRecentStatus == null || mostRecentStatus.getStatus() == null) {
                        listing.addBusinessErrorMessage(msgUtil.getMessage(
                                "listing.developer.noStatusFound.noUpdate", developer.getName()));
                    } else {
                        if (resourcePermissionsFactory.get().isUserRoleAdmin() || resourcePermissionsFactory.get().isUserRoleOnc()) {
                            if (!checkAdminOrOncAllowedToEdit(developer)) {
                                listing.addBusinessErrorMessage(msgUtil.getMessage(
                                        "listing.developer.notActiveOrBanned.noUpdate",
                                        developer.getName(), mostRecentStatus.getStatus(),
                                        DeveloperStatusType.Active.getName(),
                                        DeveloperStatusType.UnderCertificationBanByOnc.getName()));
                            }
                        } else if (!checkAcbAllowedToEdit(developer)) {
                            listing.addBusinessErrorMessage(msgUtil.getMessage(
                                    "listing.developer.notActive.noUpdate",
                                    developer.getName(), mostRecentStatus.getStatus()));
                        }
                    }
                } else {
                    listing.addBusinessErrorMessage(msgUtil.getMessage("developer.notFound"));
                }
            }
        } catch (final EntityRetrievalException ex) {
            listing.addDataErrorMessage(msgUtil.getMessage("developer.notFound"));
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    private boolean checkAcbAllowedToEdit(Developer developer) {
        DeveloperStatus mostRecentStatus = developer.getStatus();
        return mostRecentStatus.getStatus().equals(DeveloperStatusType.Active.getName());
    }

    private boolean checkAdminOrOncAllowedToEdit(Developer developer) {
        DeveloperStatus mostRecentStatus = developer.getStatus();
        return mostRecentStatus.getStatus().equals(DeveloperStatusType.Active.getName())
                || mostRecentStatus.getStatus().equals(DeveloperStatusType.UnderCertificationBanByOnc.getName());
    }
}

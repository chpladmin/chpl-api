package gov.healthit.chpl.upload.listing.normalizer;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.manager.DeveloperManager;
import gov.healthit.chpl.upload.listing.ListingUploadHandlerUtil;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class DeveloperNormalizer {
    private DeveloperDAO devDao;
    private ChplProductNumberUtil chplProductNumberUtil;
    private ListingUploadHandlerUtil uploadHandlerUtil;

    @Autowired
    public DeveloperNormalizer(DeveloperDAO devDao,
            ChplProductNumberUtil chplProductNumberUtil,
            ListingUploadHandlerUtil uploadHandlerUtil) {
        this.devDao = devDao;
        this.chplProductNumberUtil = chplProductNumberUtil;
        this.uploadHandlerUtil = uploadHandlerUtil;
    }

    public void normalize(CertifiedProductSearchDetails listing) {
        if (hasUserEnteredDeveloperName(listing)) {
            Developer systemDev = devDao.getByName(listing.getDeveloper().getUserEnteredName());
            if (systemDev != null) {
                copySystemDeveloperValues(listing.getDeveloper(), systemDev);
            } else {
                copyUserEnteredDeveloperValues(listing.getDeveloper());
            }
        } else if (!StringUtils.isEmpty(listing.getChplProductNumber())) {
            String devCode = "";
            try {
                devCode = chplProductNumberUtil.getDeveloperCode(listing.getChplProductNumber());
            } catch (Exception ex) {
            }
            if (!StringUtils.isEmpty(devCode) && !devCode.equals(DeveloperManager.NEW_DEVELOPER_CODE)) {
                Developer systemDev = devDao.getByCode(devCode);
                if (systemDev != null) {
                    if (listing.getDeveloper() == null) {
                        listing.setDeveloper(new Developer());
                    }
                    copySystemDeveloperValues(listing.getDeveloper(), systemDev);
                } else {
                    copyUserEnteredDeveloperValues(listing.getDeveloper());
                }
            } else if (listing.getDeveloper() != null
                    && !StringUtils.isEmpty(devCode) && devCode.equals(DeveloperManager.NEW_DEVELOPER_CODE)) {
                copyUserEnteredDeveloperValues(listing.getDeveloper());
            }
        }
    }

    private boolean hasUserEnteredDeveloperName(CertifiedProductSearchDetails listing) {
        return listing.getDeveloper() != null && listing.getDeveloper().getDeveloperId() == null
                && !StringUtils.isEmpty(listing.getDeveloper().getUserEnteredName());
    }

    private void copySystemDeveloperValues(Developer userEnteredDev, Developer systemDev) {
        userEnteredDev.setDeveloperId(systemDev.getDeveloperId());
        userEnteredDev.setDeveloperCode(systemDev.getDeveloperCode());
        userEnteredDev.setName(systemDev.getName());
        userEnteredDev.setWebsite(systemDev.getWebsite());
        userEnteredDev.setSelfDeveloper(systemDev.getSelfDeveloper());
        userEnteredDev.setContact(systemDev.getContact());
        userEnteredDev.setAddress(systemDev.getAddress());
        userEnteredDev.setStatusEvents(systemDev.getStatusEvents());
    }

    private void copyUserEnteredDeveloperValues(Developer userEnteredDev) {
        if (userEnteredDev == null) {
            return;
        }
        if (StringUtils.isEmpty(userEnteredDev.getName())) {
            userEnteredDev.setName(userEnteredDev.getUserEnteredName());
        }
        if (userEnteredDev.getSelfDeveloper() == null) {
            Boolean selfDeveloper = null;
            try {
                selfDeveloper = uploadHandlerUtil.parseBoolean(userEnteredDev.getUserEnteredSelfDeveloper());
            } catch (Exception ex) {
                LOGGER.warn("Could not turn " + userEnteredDev.getUserEnteredSelfDeveloper() + " into a boolean.");
            }
            userEnteredDev.setSelfDeveloper(selfDeveloper);
        }
        if (StringUtils.isEmpty(userEnteredDev.getWebsite())) {
            userEnteredDev.setWebsite(userEnteredDev.getUserEnteredWebsite());
        }
        if (userEnteredDev.getAddress() == null) {
            userEnteredDev.setAddress(userEnteredDev.getUserEnteredAddress());
        }
        if (userEnteredDev.getContact() == null) {
            userEnteredDev.setContact(userEnteredDev.getUserEnteredPointOfContact());
        }
    }
}

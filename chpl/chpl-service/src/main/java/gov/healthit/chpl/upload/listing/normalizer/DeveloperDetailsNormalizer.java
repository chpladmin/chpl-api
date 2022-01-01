package gov.healthit.chpl.upload.listing.normalizer;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.manager.DeveloperManager;
import gov.healthit.chpl.upload.listing.ListingUploadHandlerUtil;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class DeveloperDetailsNormalizer {
    private DeveloperDAO devDao;
    private ChplProductNumberUtil chplProductNumberUtil;
    private ListingUploadHandlerUtil uploadHandlerUtil;

    @Autowired
    public DeveloperDetailsNormalizer(DeveloperDAO devDao,
            ChplProductNumberUtil chplProductNumberUtil,
            ListingUploadHandlerUtil uploadHandlerUtil) {
        this.devDao = devDao;
        this.chplProductNumberUtil = chplProductNumberUtil;
        this.uploadHandlerUtil = uploadHandlerUtil;
    }

    public void normalize(CertifiedProductSearchDetails listing) {
        if (hasUserEnteredDeveloperName(listing)) {
            DeveloperDTO systemDev = devDao.getByName(listing.getDeveloper().getUserEnteredName());
            if (systemDev != null) {
                copySystemDeveloperValues(listing.getDeveloper(), new Developer(systemDev));
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
                DeveloperDTO systemDev = devDao.getByCode(devCode);
                if (systemDev != null) {
                    if (listing.getDeveloper() == null) {
                        listing.setDeveloper(new Developer());
                    }
                    copySystemDeveloperValues(listing.getDeveloper(), new Developer(systemDev));
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
        userEnteredDev.setTransparencyAttestations(systemDev.getTransparencyAttestations());
    }

    private void copyUserEnteredDeveloperValues(Developer userEnteredDev) {
        if (userEnteredDev == null) {
            return;
        }
        userEnteredDev.setName(userEnteredDev.getUserEnteredName());
        Boolean selfDeveloper = null;
        try {
            selfDeveloper = uploadHandlerUtil.parseBoolean(userEnteredDev.getUserEnteredSelfDeveloper());
        } catch (Exception ex) {
            LOGGER.warn("Could not turn " + userEnteredDev.getUserEnteredSelfDeveloper() + " into a boolean.");
        }
        userEnteredDev.setSelfDeveloper(selfDeveloper);
        userEnteredDev.setWebsite(userEnteredDev.getUserEnteredWebsite());
        userEnteredDev.setAddress(userEnteredDev.getUserEnteredAddress());
        userEnteredDev.setContact(userEnteredDev.getUserEnteredPointOfContact());
    }
}

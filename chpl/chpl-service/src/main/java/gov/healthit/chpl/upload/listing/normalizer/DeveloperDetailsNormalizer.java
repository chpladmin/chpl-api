package gov.healthit.chpl.upload.listing.normalizer;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.ContactDAO;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.dto.ContactDTO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.manager.DeveloperManager;
import gov.healthit.chpl.util.ChplProductNumberUtil;

@Component
public class DeveloperDetailsNormalizer {
    private DeveloperDAO devDao;
    private ContactDAO contactDao;
    private ChplProductNumberUtil chplProductNumberUtil;

    @Autowired
    public DeveloperDetailsNormalizer(DeveloperDAO devDao, ContactDAO contactDao,
            ChplProductNumberUtil chplProductNumberUtil) {
        this.devDao = devDao;
        this.contactDao = contactDao;
        this.chplProductNumberUtil = chplProductNumberUtil;
    }

    public void normalize(CertifiedProductSearchDetails listing) {
        if (listing.getDeveloper() != null && listing.getDeveloper().getContact() != null
                && listing.getDeveloper().getContact().getContactId() == null) {
            ContactDTO contactToFind = new ContactDTO();
            contactToFind.setFullName(listing.getDeveloper().getContact().getFullName());
            contactToFind.setEmail(listing.getDeveloper().getContact().getEmail());
            contactToFind.setPhoneNumber(listing.getDeveloper().getContact().getPhoneNumber());
            ContactDTO foundContact = contactDao.getByValues(contactToFind);
            if (foundContact != null) {
                listing.getDeveloper().getContact().setContactId(foundContact.getId());
            }
        }

        if (listing.getDeveloper() != null && listing.getDeveloper().getDeveloperId() == null
                && !StringUtils.isEmpty(listing.getDeveloper().getName())) {
            DeveloperDTO foundDev = devDao.getByName(listing.getDeveloper().getName());
            if (foundDev != null) {
                listing.getDeveloper().setDeveloperId(foundDev.getId());
                mashupEnteredAndStoredValues(listing.getDeveloper(), new Developer(foundDev));
            }
        } else if (!StringUtils.isEmpty(listing.getChplProductNumber())
                && listing.getDeveloper().getDeveloperId() == null
                && StringUtils.isEmpty(listing.getDeveloper().getName())) {
            String devCode = chplProductNumberUtil.getDeveloperCode(listing.getChplProductNumber());
            if (!StringUtils.isEmpty(devCode) && !devCode.equals(DeveloperManager.NEW_DEVELOPER_CODE)) {
                DeveloperDTO foundDev = devDao.getByCode(devCode);
                if (foundDev != null) {
                    listing.getDeveloper().setDeveloperId(foundDev.getId());
                    mashupEnteredAndStoredValues(listing.getDeveloper(), new Developer(foundDev));
                }
            }
        }
    }

    private void mashupEnteredAndStoredValues(Developer entered, Developer stored) {
        if (StringUtils.isEmpty(entered.getName())) {
            entered.setName(stored.getName());
        }
        if (StringUtils.isEmpty(entered.getWebsite())) {
            entered.setWebsite(stored.getWebsite());
        }
        if (entered.getSelfDeveloper() == null) {
            entered.setSelfDeveloper(stored.getSelfDeveloper());
        }
        if (entered.getContact() == null) {
            entered.setContact(stored.getContact());
        }
        if (entered.getContact() != null && StringUtils.isEmpty(entered.getContact().getFullName())) {
            entered.getContact().setFullName(stored.getContact().getFullName());
        }
        if (entered.getContact() != null && StringUtils.isEmpty(entered.getContact().getEmail())) {
            entered.getContact().setEmail(stored.getContact().getEmail());
        }
        if (entered.getContact() != null && StringUtils.isEmpty(entered.getContact().getPhoneNumber())) {
            entered.getContact().setPhoneNumber(stored.getContact().getPhoneNumber());
        }
        if (entered.getContact() != null && StringUtils.isEmpty(entered.getContact().getTitle())) {
            entered.getContact().setTitle(stored.getContact().getTitle());
        }
        if (entered.getAddress() == null) {
            entered.setAddress(stored.getAddress());
        }
        if (entered.getAddress() != null && StringUtils.isEmpty(entered.getAddress().getLine1())) {
            entered.getAddress().setLine1(stored.getAddress().getLine1());
        }
        if (entered.getAddress() != null && StringUtils.isEmpty(entered.getAddress().getLine2())) {
            entered.getAddress().setLine2(stored.getAddress().getLine2());
        }
        if (entered.getAddress() != null && StringUtils.isEmpty(entered.getAddress().getCity())) {
            entered.getAddress().setCity(stored.getAddress().getCity());
        }
        if (entered.getAddress() != null && StringUtils.isEmpty(entered.getAddress().getState())) {
            entered.getAddress().setState(stored.getAddress().getState());
        }
        if (entered.getAddress() != null && StringUtils.isEmpty(entered.getAddress().getZipcode())) {
            entered.getAddress().setZipcode(stored.getAddress().getZipcode());
        }
        if (entered.getAddress() != null && StringUtils.isEmpty(entered.getAddress().getCountry())) {
            entered.getAddress().setCountry(stored.getAddress().getCountry());
        }
    }
}

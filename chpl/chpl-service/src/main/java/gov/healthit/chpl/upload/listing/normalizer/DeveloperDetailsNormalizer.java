package gov.healthit.chpl.upload.listing.normalizer;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.ContactDAO;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.domain.Address;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.contact.PointOfContact;
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
        entered.setDeveloperCode(stored.getDeveloperCode());
        if (StringUtils.isEmpty(entered.getName())) {
            entered.setName(stored.getName());
        }
        if (StringUtils.isEmpty(entered.getWebsite())) {
            entered.setWebsite(stored.getWebsite());
        }
        if (entered.getSelfDeveloper() == null && StringUtils.isEmpty(entered.getSelfDeveloperStr())) {
            entered.setSelfDeveloper(stored.getSelfDeveloper());
        }
        if (entered.getContact() == null && stored.getContact() != null) {
            entered.setContact(new PointOfContact());
        }
        if (entered.getContact() != null && StringUtils.isEmpty(entered.getContact().getFullName())
                && stored.getContact() != null && !StringUtils.isEmpty(stored.getContact().getFullName())) {
            entered.getContact().setFullName(stored.getContact().getFullName());
        }
        if (entered.getContact() != null && StringUtils.isEmpty(entered.getContact().getEmail())
                && stored.getContact() != null && !StringUtils.isEmpty(stored.getContact().getEmail())) {
            entered.getContact().setEmail(stored.getContact().getEmail());
        }
        if (entered.getContact() != null && StringUtils.isEmpty(entered.getContact().getPhoneNumber())
                && stored.getContact() != null && !StringUtils.isEmpty(stored.getContact().getPhoneNumber())) {
            entered.getContact().setPhoneNumber(stored.getContact().getPhoneNumber());
        }
        if (entered.getContact() != null && StringUtils.isEmpty(entered.getContact().getTitle())
                && stored.getContact() != null && !StringUtils.isEmpty(stored.getContact().getTitle())) {
            entered.getContact().setTitle(stored.getContact().getTitle());
        }
        if (entered.getAddress() == null && stored.getAddress() != null) {
            entered.setAddress(new Address());
        }
        if (entered.getAddress() != null && StringUtils.isEmpty(entered.getAddress().getLine1())
                && stored.getAddress() != null && !StringUtils.isEmpty(stored.getAddress().getLine1())) {
            entered.getAddress().setLine1(stored.getAddress().getLine1());
        }
        if (entered.getAddress() != null && StringUtils.isEmpty(entered.getAddress().getLine2())
                && stored.getAddress() != null && !StringUtils.isEmpty(stored.getAddress().getLine2())) {
            entered.getAddress().setLine2(stored.getAddress().getLine2());
        }
        if (entered.getAddress() != null && StringUtils.isEmpty(entered.getAddress().getCity())
                && stored.getAddress() != null && !StringUtils.isEmpty(stored.getAddress().getCity())) {
            entered.getAddress().setCity(stored.getAddress().getCity());
        }
        if (entered.getAddress() != null && StringUtils.isEmpty(entered.getAddress().getState())
                && stored.getAddress() != null && !StringUtils.isEmpty(stored.getAddress().getState())) {
            entered.getAddress().setState(stored.getAddress().getState());
        }
        if (entered.getAddress() != null && StringUtils.isEmpty(entered.getAddress().getZipcode())
                && stored.getAddress() != null && !StringUtils.isEmpty(stored.getAddress().getZipcode())) {
            entered.getAddress().setZipcode(stored.getAddress().getZipcode());
        }
        if (entered.getAddress() != null && StringUtils.isEmpty(entered.getAddress().getCountry())
                && stored.getAddress() != null && !StringUtils.isEmpty(stored.getAddress().getCountry())) {
            entered.getAddress().setCountry(stored.getAddress().getCountry());
        }
        entered.setStatus(stored.getStatus());
        entered.setStatusEvents(stored.getStatusEvents());
        entered.setTransparencyAttestations(stored.getTransparencyAttestations());
    }
}

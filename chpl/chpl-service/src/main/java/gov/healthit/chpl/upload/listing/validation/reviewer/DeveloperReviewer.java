package gov.healthit.chpl.upload.listing.validation.reviewer;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.Address;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.DeveloperStatus;
import gov.healthit.chpl.domain.contact.PointOfContact;
import gov.healthit.chpl.entity.developer.DeveloperStatusType;
import gov.healthit.chpl.manager.DeveloperManager;
import gov.healthit.chpl.upload.listing.ListingUploadHandlerUtil;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.listing.reviewer.Reviewer;

@Component("developerReviewer")
public class DeveloperReviewer implements Reviewer {
    private static final String WARNING = "warning";
    private static final String ERROR = "error";

    private ErrorMessageUtil msgUtil;
    private UrlValidator urlValidator;
    private ChplProductNumberUtil chplProductNumberUtil;
    private ListingUploadHandlerUtil uploadHandlerUtil;

    @Autowired
    public DeveloperReviewer(ErrorMessageUtil msgUtil,
            ChplProductNumberUtil chplProductNumberUtil,
            ListingUploadHandlerUtil uploadHandlerUtil) {
        this.msgUtil = msgUtil;
        this.chplProductNumberUtil = chplProductNumberUtil;
        this.uploadHandlerUtil = uploadHandlerUtil;
        this.urlValidator = new UrlValidator();
    }

    public void review(CertifiedProductSearchDetails listing) {
        Developer developer = listing.getDeveloper();
        if (developer == null) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.missingDeveloper"));
            return;
        }

        if (developer.getDeveloperId() != null) {
            //it's an existing developer - give the user warnings if any required developer data is
            // 1) missing in the system, or 2) does not match the user-entered data
            reviewRequiredDeveloperData(listing, WARNING);
            reviewUserEnteredDataSystemDataMismatch(listing);
            reviewDeveloperStatusIsActive(listing);
        } else {
            String devCode = "";
            try {
                devCode = chplProductNumberUtil.getDeveloperCode(listing.getChplProductNumber());
            } catch (Exception ex) {
            }
            if (devCode.equals(DeveloperManager.NEW_DEVELOPER_CODE)) {
                //it's a new developer - give the user errors if any required data has not been entered
                reviewRequiredDeveloperData(listing, ERROR);
            } else {
                //it's not a new developer by code so it must be an error that no developer was found
                listing.getErrorMessages().add(msgUtil.getMessage("listing.developer.notFound", listing.getDeveloper().getUserEnteredName()));
            }
        }
    }

    private void reviewUserEnteredDataSystemDataMismatch(CertifiedProductSearchDetails listing) {
        Developer developer = listing.getDeveloper();
        if (!StringUtils.isEmpty(developer.getUserEnteredName()) && !developer.getUserEnteredName().equals(developer.getName())) {
            listing.getWarningMessages().add(msgUtil.getMessage("listing.developer.userAndSystemMismatch", "name", developer.getUserEnteredName(), developer.getName()));
        }
        if (!StringUtils.isEmpty(developer.getUserEnteredWebsite()) && !developer.getUserEnteredWebsite().equals(developer.getWebsite())) {
            listing.getWarningMessages().add(msgUtil.getMessage("listing.developer.userAndSystemMismatch", "website", developer.getUserEnteredWebsite(), developer.getWebsite()));
        }
        if (!StringUtils.isEmpty(developer.getUserEnteredSelfDeveloper())) {
            Boolean userEnteredSelfDeveloper = null;
            try {
                userEnteredSelfDeveloper = uploadHandlerUtil.parseBoolean(developer.getUserEnteredSelfDeveloper());
            } catch (Exception ex) {
            }
            if (userEnteredSelfDeveloper != null && !userEnteredSelfDeveloper.equals(developer.getSelfDeveloper())) {
                listing.getWarningMessages().add(msgUtil.getMessage("listing.developer.userAndSystemMismatch", "self-developer", developer.getUserEnteredSelfDeveloper(), developer.getSelfDeveloper() + ""));
            } else if (userEnteredSelfDeveloper == null && developer.getSelfDeveloper() != null) {
                listing.getWarningMessages().add(msgUtil.getMessage("listing.developer.userAndSystemMismatch", "self-developer", developer.getUserEnteredSelfDeveloper(), developer.getSelfDeveloper() + ""));
            } else if (userEnteredSelfDeveloper != null && developer.getSelfDeveloper() == null) {
                listing.getWarningMessages().add(msgUtil.getMessage("listing.developer.userAndSystemMismatch", "self-developer", developer.getUserEnteredSelfDeveloper(), "''"));
            }
        }
        if (developer.getUserEnteredAddress() != null && developer.getAddress() != null) {
            Address userEnteredAddress = developer.getUserEnteredAddress();
            Address address = developer.getAddress();
            if (!StringUtils.isEmpty(userEnteredAddress.getLine1()) && !userEnteredAddress.getLine1().equals(address.getLine1())) {
                listing.getWarningMessages().add(msgUtil.getMessage("listing.developer.userAndSystemMismatch", "street address", userEnteredAddress.getLine1(), address.getLine1()));
            }
            if (!StringUtils.isEmpty(userEnteredAddress.getCity()) && !userEnteredAddress.getCity().equals(address.getCity())) {
                listing.getWarningMessages().add(msgUtil.getMessage("listing.developer.userAndSystemMismatch", "city", userEnteredAddress.getCity(), address.getCity()));
            }
            if (!StringUtils.isEmpty(userEnteredAddress.getState()) && !userEnteredAddress.getState().equals(address.getState())) {
                listing.getWarningMessages().add(msgUtil.getMessage("listing.developer.userAndSystemMismatch", "state", userEnteredAddress.getState(), address.getState()));
            }
            if (!StringUtils.isEmpty(userEnteredAddress.getZipcode()) && !userEnteredAddress.getZipcode().equals(address.getZipcode())) {
                listing.getWarningMessages().add(msgUtil.getMessage("listing.developer.userAndSystemMismatch", "zipcode", userEnteredAddress.getZipcode(), address.getZipcode()));
            }
        }
        if (developer.getUserEnteredPointOfContact() != null && developer.getContact() != null) {
            PointOfContact userEnteredContact = developer.getUserEnteredPointOfContact();
            PointOfContact contact = developer.getContact();
            if (!StringUtils.isEmpty(userEnteredContact.getFullName()) && !userEnteredContact.getFullName().equals(contact.getFullName())) {
                listing.getWarningMessages().add(msgUtil.getMessage("listing.developer.userAndSystemMismatch", "point of contact name", userEnteredContact.getFullName(), contact.getFullName()));
            }
            if (!StringUtils.isEmpty(userEnteredContact.getEmail()) && !userEnteredContact.getEmail().equals(contact.getEmail())) {
                listing.getWarningMessages().add(msgUtil.getMessage("listing.developer.userAndSystemMismatch", "email address", userEnteredContact.getEmail(), contact.getEmail()));
            }
            if (!StringUtils.isEmpty(userEnteredContact.getPhoneNumber()) && !userEnteredContact.getPhoneNumber().equals(contact.getPhoneNumber())) {
                listing.getWarningMessages().add(msgUtil.getMessage("listing.developer.userAndSystemMismatch", "phone number", userEnteredContact.getPhoneNumber(), contact.getPhoneNumber()));
            }
        }
    }

    private void reviewRequiredDeveloperData(CertifiedProductSearchDetails listing, String msgLevel) {
        Developer developer = listing.getDeveloper();
        if (StringUtils.isEmpty(developer.getName())) {
            addMessageToListing(listing, msgUtil.getMessage("developer.nameRequired"), msgLevel);
        }

        if (developer.getSelfDeveloper() == null && !StringUtils.isEmpty(developer.getUserEnteredSelfDeveloper())) {
            addMessageToListing(listing,
                    msgUtil.getMessage("developer.selfDeveloper.invalid", developer.getUserEnteredSelfDeveloper()),
                    msgLevel);
        } else if (developer.getSelfDeveloper() == null && StringUtils.isEmpty(developer.getUserEnteredSelfDeveloper())) {
            addMessageToListing(listing, msgUtil.getMessage("developer.selfDeveloper.missing"), msgLevel);
        }

        reviewDeveloperWebsiteIsPresentAndValid(listing, developer.getWebsite(), msgLevel);
        reviewDeveloperAddressHasRequiredData(listing, developer.getAddress(), msgLevel);
        reviewDeveloperContactHasRequiredData(listing, developer.getContact(), msgLevel);
    }

    private void reviewDeveloperWebsiteIsPresentAndValid(CertifiedProductSearchDetails listing, String website, String msgLevel) {
        if (StringUtils.isEmpty(website)) {
            addMessageToListing(listing, msgUtil.getMessage("developer.websiteRequired"), msgLevel);
        } else if (!urlValidator.isValid(website)) {
            addMessageToListing(listing, msgUtil.getMessage("developer.websiteIsInvalid"), msgLevel);
        }
    }

    private void reviewDeveloperAddressHasRequiredData(CertifiedProductSearchDetails listing, Address address, String msgLevel) {
        if (address == null) {
            addMessageToListing(listing, msgUtil.getMessage("developer.addressRequired"), msgLevel);
        } else {
            if (StringUtils.isEmpty(address.getLine1())) {
                addMessageToListing(listing, msgUtil.getMessage("developer.address.streetRequired"), msgLevel);
            }
            if (StringUtils.isEmpty(address.getCity())) {
                addMessageToListing(listing, msgUtil.getMessage("developer.address.cityRequired"), msgLevel);
            }
            if (StringUtils.isEmpty(address.getState())) {
                addMessageToListing(listing, msgUtil.getMessage("developer.address.stateRequired"), msgLevel);
            }
            if (StringUtils.isEmpty(address.getZipcode())) {
                addMessageToListing(listing, msgUtil.getMessage("developer.address.zipRequired"), msgLevel);
            }
            if (StringUtils.isEmpty(address.getCountry())) {
                addMessageToListing(listing, msgUtil.getMessage("developer.address.countryRequired", Address.DEFAULT_COUNTRY), WARNING);
                address.setCountry(Address.DEFAULT_COUNTRY);
            }
        }
    }

    private void reviewDeveloperContactHasRequiredData(CertifiedProductSearchDetails listing, PointOfContact contact, String msgLevel) {
        if (contact == null) {
            addMessageToListing(listing, msgUtil.getMessage("developer.contactRequired"), msgLevel);
        } else {
            if (StringUtils.isEmpty(contact.getEmail())) {
                addMessageToListing(listing, msgUtil.getMessage("developer.contact.emailRequired"), msgLevel);
            }
            if (StringUtils.isEmpty(contact.getPhoneNumber())) {
                addMessageToListing(listing, msgUtil.getMessage("developer.contact.phoneRequired"), msgLevel);
            }
            if (StringUtils.isEmpty(contact.getFullName())) {
                addMessageToListing(listing, msgUtil.getMessage("developer.contact.nameRequired"), msgLevel);
            }
        }
    }

    private void reviewDeveloperStatusIsActive(CertifiedProductSearchDetails listing) {
        Developer developer = listing.getDeveloper();
        if (developer.getDeveloperId() != null) {
            DeveloperStatus mostRecentStatus = developer.getStatus();
            if (mostRecentStatus == null || StringUtils.isEmpty(mostRecentStatus.getStatus())) {
                listing.getErrorMessages().add(msgUtil.getMessage("developer.status.noCurrent"));
            } else if (!mostRecentStatus.getStatus().equals(DeveloperStatusType.Active.getName())) {
                listing.getErrorMessages().add(msgUtil.getMessage("listing.developer.notActive.noCreate",
                        developer.getName() != null ? developer.getName() : "?",
                        mostRecentStatus.getStatus()));
            }
        }
    }

    private void addMessageToListing(CertifiedProductSearchDetails listing, String message, String msgLevel) {
        if (msgLevel.equals(ERROR)) {
            listing.getErrorMessages().add(message);
        } else if (msgLevel.equals(WARNING)) {
            listing.getWarningMessages().add(message);
        }
    }
}

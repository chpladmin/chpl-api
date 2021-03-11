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
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.listing.reviewer.Reviewer;

@Component("developerReviewer")
public class DeveloperReviewer implements Reviewer {
    private ErrorMessageUtil msgUtil;
    private UrlValidator urlValidator;

    @Autowired
    public DeveloperReviewer(ErrorMessageUtil msgUtil) {
        this.msgUtil = msgUtil;
        this.urlValidator = new UrlValidator();
    }

    public void review(CertifiedProductSearchDetails listing) {
        Developer developer = listing.getDeveloper();
        if (developer == null) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.missingDeveloper"));
            return;
        }

        if (StringUtils.isEmpty(developer.getName())) {
            listing.getErrorMessages().add(msgUtil.getMessage("developer.nameRequired"));
        }

        if (developer.getSelfDeveloper() == null && !StringUtils.isEmpty(developer.getSelfDeveloperStr())) {
            listing.getErrorMessages().add(msgUtil.getMessage("developer.selfDeveloper.invalid", developer.getSelfDeveloperStr()));
        } else if (developer.getSelfDeveloper() == null && StringUtils.isEmpty(developer.getSelfDeveloperStr())) {
            listing.getErrorMessages().add(msgUtil.getMessage("developer.selfDeveloper.missing"));
        }

        reviewDeveloperWebsiteIsPresentAndValid(listing, developer.getWebsite());
        reviewDeveloperAddressHasRequiredData(listing, developer.getAddress());
        reviewDeveloperContactHasRequiredData(listing, developer.getContact());
        reviewDeveloperStatusIsActive(listing);
    }

    private void reviewDeveloperWebsiteIsPresentAndValid(CertifiedProductSearchDetails listing, String website) {
        if (StringUtils.isEmpty(website)) {
            listing.getErrorMessages().add(msgUtil.getMessage("developer.websiteRequired"));
        } else if (!urlValidator.isValid(website)) {
            listing.getErrorMessages().add(msgUtil.getMessage("developer.websiteIsInvalid"));
        }
    }

    private void reviewDeveloperAddressHasRequiredData(CertifiedProductSearchDetails listing, Address address) {
        if (StringUtils.isEmpty(address.getLine1())) {
            listing.getErrorMessages().add(msgUtil.getMessage("developer.address.streetRequired"));
        }
        if (StringUtils.isEmpty(address.getCity())) {
            listing.getErrorMessages().add(msgUtil.getMessage("developer.address.cityRequired"));
        }
        if (StringUtils.isEmpty(address.getState())) {
            listing.getErrorMessages().add(msgUtil.getMessage("developer.address.stateRequired"));
        }
        if (StringUtils.isEmpty(address.getZipcode())) {
            listing.getErrorMessages().add(msgUtil.getMessage("developer.address.zipRequired"));
        }
    }

    private void reviewDeveloperContactHasRequiredData(CertifiedProductSearchDetails listing, PointOfContact contact) {
        if (StringUtils.isEmpty(contact.getEmail())) {
            listing.getErrorMessages().add(msgUtil.getMessage("developer.contact.emailRequired"));
        }
        if (StringUtils.isEmpty(contact.getPhoneNumber())) {
            listing.getErrorMessages().add(msgUtil.getMessage("developer.contact.phoneRequired"));
        }
        if (StringUtils.isEmpty(contact.getFullName())) {
            listing.getErrorMessages().add(msgUtil.getMessage("developer.contact.nameRequired"));
        }
    }

    private void reviewDeveloperStatusIsActive(CertifiedProductSearchDetails listing) {
        Developer developer = listing.getDeveloper();
        DeveloperStatus mostRecentStatus = developer.getStatus();
        if (mostRecentStatus == null || StringUtils.isEmpty(mostRecentStatus.getStatus())) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.developer.noStatusFound.noUpdate",
                    developer.getName() != null ? developer.getName() : "?"));
        } else if (!mostRecentStatus.getStatus().equals(DeveloperStatusType.Active.getName())) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.developer.notActive.noCreate",
                    developer.getName() != null ? developer.getName() : "?",
                    mostRecentStatus.getStatus()));
        }
    }
}

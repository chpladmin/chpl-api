package gov.healthit.chpl.validation.pendingListing.review;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.dto.PendingCertifiedProductDTO;

@Component("pendingRequiredFieldReviewer")
public class RequiredFieldReviewer implements Reviewer {
    
    @Override
    public void review(PendingCertifiedProductDTO listing) {
        if (listing.getCertificationEditionId() == null && StringUtils.isEmpty(listing.getCertificationEdition())) {
            listing.getErrorMessages().add("Certification edition is required but was not found.");
        }
        if (listing.getCertificationDate() == null) {
            listing.getErrorMessages().add("Certification date was not found.");
        }
        if (listing.getCertificationBodyId() == null) {
            listing.getErrorMessages().add("ONC-ACB is required but was not found.");
        }
        if (StringUtils.isEmpty(listing.getUniqueId())) {
            listing.getErrorMessages().add("The product unique id is required.");
        }
        if (StringUtils.isEmpty(listing.getDeveloperName())) {
            listing.getErrorMessages().add("A developer name is required.");
        }
        if (StringUtils.isEmpty(listing.getProductName())) {
            listing.getErrorMessages().add("A product name is required.");
        }
        if (StringUtils.isEmpty(listing.getProductVersion())) {
            listing.getErrorMessages().add("A product version is required.");
        }
        if (listing.getDeveloperAddress() != null) {
            if (StringUtils.isEmpty(listing.getDeveloperAddress().getStreetLineOne())) {
                listing.getErrorMessages().add("Developer street address is required.");
            }
            if (StringUtils.isEmpty(listing.getDeveloperAddress().getCity())) {
                listing.getErrorMessages().add("Developer city is required.");
            }
            if (StringUtils.isEmpty(listing.getDeveloperAddress().getState())) {
                listing.getErrorMessages().add("Developer state is required.");
            }
            if (StringUtils.isEmpty(listing.getDeveloperAddress().getZipcode())) {
                listing.getErrorMessages().add("Developer zip code is required.");
            }
        } else {
            if (StringUtils.isEmpty(listing.getDeveloperStreetAddress())) {
                listing.getErrorMessages().add("Developer street address is required.");
            }
            if (StringUtils.isEmpty(listing.getDeveloperCity())) {
                listing.getErrorMessages().add("Developer city is required.");
            }
            if (StringUtils.isEmpty(listing.getDeveloperState())) {
                listing.getErrorMessages().add("Developer state is required.");
            }
            if (StringUtils.isEmpty(listing.getDeveloperZipCode())) {
                listing.getErrorMessages().add("Developer zip code is required.");
            }
        }
        if (StringUtils.isEmpty(listing.getDeveloperWebsite())) {
            listing.getErrorMessages().add("Developer website is required.");
        }
        if (StringUtils.isEmpty(listing.getDeveloperEmail())) {
            listing.getErrorMessages().add("Developer contact email address is required.");
        }
        if (StringUtils.isEmpty(listing.getDeveloperPhoneNumber())) {
            listing.getErrorMessages().add("Developer contact phone number is required.");
        }
        if (StringUtils.isEmpty(listing.getDeveloperContactName())) {
            listing.getErrorMessages().add("Developer contact name is required.");
        }
    }
}

package gov.healthit.chpl.questionableactivity.listing;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.questionableactivity.QuestionableActivityTriggerConcept;
import gov.healthit.chpl.questionableactivity.domain.QuestionableActivityListing;
import gov.healthit.chpl.util.CertificationStatusUtil;
import gov.healthit.chpl.util.CertifiedProductUtil;

@Component
public class CreateIcsListingWithWithdrawnParent implements ListingActivity {
    private CertifiedProductUtil cpUtil;

    @Autowired
    public CreateIcsListingWithWithdrawnParent(CertifiedProductUtil cpUtil) {
        this.cpUtil = cpUtil;
    }

    @Override
    public List<QuestionableActivityListing> check(CertifiedProductSearchDetails origListing, CertifiedProductSearchDetails newListing) {
        if (newListing.getIcs() != null && !CollectionUtils.isEmpty(newListing.getIcs().getParents())
                && anyListingsAreWithdrawn(newListing.getIcs().getParents())) {
            return List.of(QuestionableActivityListing.builder().build());
        }
        return null;
    }

    private boolean anyListingsAreWithdrawn(List<CertifiedProduct> listings) {
        listings.stream()
            .forEach(listing -> fillInListingStatusData(listing));

        return listings.stream()
                .filter(listing -> CertificationStatusUtil.isInactive(listing.getCertificationStatus()))
                .findAny().isPresent();
    }

    private void fillInListingStatusData(CertifiedProduct listing) {
        if (StringUtils.isEmpty(listing.getCertificationStatus())) {
            CertifiedProduct foundListing = cpUtil.getListing(listing.getChplProductNumber());
            if (foundListing != null) {
                listing.setId(foundListing.getId());
                listing.setCertificationDate(foundListing.getCertificationDate());
                listing.setCertificationStatus(foundListing.getCertificationStatus());
                listing.setCuresUpdate(foundListing.getCuresUpdate());
                listing.setEdition(foundListing.getEdition());
            }
        }
    }

    @Override
    public QuestionableActivityTriggerConcept getTriggerType() {
        return QuestionableActivityTriggerConcept.CREATED_ICS_LISTING_WITH_WITHDRAWN_PARENT;
    }
}

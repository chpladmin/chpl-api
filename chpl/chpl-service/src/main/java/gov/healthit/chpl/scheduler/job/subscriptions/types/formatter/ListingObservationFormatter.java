package gov.healthit.chpl.scheduler.job.subscriptions.types.formatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.search.ListingSearchService;
import gov.healthit.chpl.search.domain.ListingSearchResult;
import gov.healthit.chpl.subscription.domain.SubscriptionObservation;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2(topic = "subscriptionObservationsNotificationJobLogger")
public class ListingObservationFormatter extends ObservationTypeFormatter {

    private ListingSearchService listingSearchService;

    @Autowired
    public ListingObservationFormatter(ListingSearchService listingSearchService,
            Environment env) {
        super(env);
        this.listingSearchService = listingSearchService;
    }

    public String getSubscribedItemHeading(SubscriptionObservation observation) {
        ListingSearchResult listing = getListing(observation.getSubscription().getSubscribedObjectId());
        if (listing == null) {
            LOGGER.error("Cannot process subscription observation " + observation.getId());
            return "";
        }
        return listing.getChplProductNumber();
    }

    public String getSubscribedItemFooter(SubscriptionObservation observation) {
        ListingSearchResult listing = getListing(observation.getSubscription().getSubscribedObjectId());
        if (listing == null) {
            LOGGER.error("Cannot process subscription observation " + observation.getId());
            return "";
        }
        String listingUrl = String.format(getUnformattedListingDetailsUrl(), listing.getId());
        return String.format(getUnformattedSubscribedItemFooter(), "listing", listingUrl, listing.getChplProductNumber());
    }

    private ListingSearchResult getListing(Long listingId) {
        ListingSearchResult listing = null;
        try {
            listing = listingSearchService.findListing(listingId);
        } catch (Exception ex) {
            LOGGER.error("Listing with ID " + listingId + " not found.", ex);
            return null;
        }
        return listing;
    }
}

package gov.healthit.chpl.scheduler.job.subscriptions.types.formatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.subscription.domain.SubscriptionObservation;
import gov.healthit.chpl.subscription.service.SubscriptionLookupUtil;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2(topic = "subscriptionObservationsNotificationJobLogger")
public class ObservationTypeFormatterFactory {

    private ListingObservationFormatter listingObservationFormatter;
    private DeveloperObservationFormatter developerObservationFormatter;
    private ProductObservationFormatter productObservationFormatter;
    private SubscriptionLookupUtil lookupUtil;

    @Autowired
    public ObservationTypeFormatterFactory(ListingObservationFormatter listingObservationFormatter,
        DeveloperObservationFormatter developerObservationFormatter,
        ProductObservationFormatter productObservationFormatter,
        SubscriptionLookupUtil lookupUtil) {
        this.listingObservationFormatter = listingObservationFormatter;
        this.developerObservationFormatter = developerObservationFormatter;
        this.productObservationFormatter = productObservationFormatter;
        this.lookupUtil = lookupUtil;
    }

    public ObservationTypeFormatter getObjectTypeFormatter(SubscriptionObservation observation) {
        Long observationObjectTypeId = observation.getSubscription().getSubject().getType().getId();
        if (lookupUtil.getListingObjectTypeId().equals(observationObjectTypeId)) {
            return listingObservationFormatter;
        } else if (lookupUtil.getDeveloperObjectTypeId().equals(observationObjectTypeId)) {
            return developerObservationFormatter;
        } else if (lookupUtil.getProductObjectTypeId().equals(observationObjectTypeId)) {
            return productObservationFormatter;
        } else {
            LOGGER.error("No object type formatter found for observation type " + observationObjectTypeId);
        }
        return null;
    }
}

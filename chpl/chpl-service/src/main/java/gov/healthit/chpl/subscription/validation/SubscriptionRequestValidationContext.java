package gov.healthit.chpl.subscription.validation;

import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.dao.ProductDAO;
import gov.healthit.chpl.search.ListingSearchService;
import gov.healthit.chpl.subscription.dao.SubscriberDao;
import gov.healthit.chpl.subscription.dao.SubscriptionDao;
import gov.healthit.chpl.subscription.domain.SubscriptionRequest;
import gov.healthit.chpl.subscription.service.SubscriptionLookupUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
public class SubscriptionRequestValidationContext {
    private SubscriptionRequest subscriptionRequest;
    private SubscriberDao subscriberDao;
    private SubscriptionDao subscriptionDao;
    private DeveloperDAO developerDao;
    private ProductDAO productDao;
    private ListingSearchService listingSearchService;
    private ErrorMessageUtil errorMessageUtil;
    private SubscriptionLookupUtil lookupUtil;

    public SubscriptionRequestValidationContext(SubscriptionRequest subscriptionRequest,
        SubscriberDao subscriberDao,
        SubscriptionDao subscriptionDao,
        DeveloperDAO developerDao,
        ProductDAO productDao,
        ListingSearchService listingSearchService,
        ErrorMessageUtil errorMessageUtil,
        SubscriptionLookupUtil lookupUtil) {
        this.subscriptionRequest = subscriptionRequest;
        this.subscriberDao = subscriberDao;
        this.subscriptionDao = subscriptionDao;
        this.developerDao = developerDao;
        this.productDao = productDao;
        this.listingSearchService = listingSearchService;
        this.errorMessageUtil = errorMessageUtil;
        this.lookupUtil = lookupUtil;
    }
}

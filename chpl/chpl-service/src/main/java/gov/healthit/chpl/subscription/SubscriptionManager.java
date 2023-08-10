package gov.healthit.chpl.subscription;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.dao.ProductDAO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.search.ListingSearchService;
import gov.healthit.chpl.search.domain.ListingSearchResult;
import gov.healthit.chpl.subscriber.validation.SubscriberValidationContext;
import gov.healthit.chpl.subscriber.validation.SubscriberValidationService;
import gov.healthit.chpl.subscription.dao.SubscriberDao;
import gov.healthit.chpl.subscription.dao.SubscriptionDao;
import gov.healthit.chpl.subscription.dao.SubscriptionObservationDao;
import gov.healthit.chpl.subscription.domain.ChplItemSubscription;
import gov.healthit.chpl.subscription.domain.ChplItemSubscriptionGroup;
import gov.healthit.chpl.subscription.domain.ListingSubscriptionGroup;
import gov.healthit.chpl.subscription.domain.Subscriber;
import gov.healthit.chpl.subscription.domain.SubscriberRole;
import gov.healthit.chpl.subscription.domain.Subscription;
import gov.healthit.chpl.subscription.domain.SubscriptionObjectType;
import gov.healthit.chpl.subscription.domain.SubscriptionRequest;
import gov.healthit.chpl.subscription.service.SubscriberMessagingService;
import gov.healthit.chpl.subscription.service.SubscriptionLookupUtil;
import gov.healthit.chpl.subscription.validation.SubscriptionRequestValidationContext;
import gov.healthit.chpl.subscription.validation.SubscriptionRequestValidationService;
import gov.healthit.chpl.util.ErrorMessageUtil;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class SubscriptionManager {
    private SubscriberDao subscriberDao;
    private SubscriptionDao subscriptionDao;
    private SubscriptionObservationDao observationDao;
    private SubscriberMessagingService subscriberMessagingService;
    private DeveloperDAO developerDao;
    private ProductDAO productDao;
    private ListingSearchService listingSearchService;
    private ErrorMessageUtil errorMessageUtil;
    private SubscriptionLookupUtil lookupUtil;
    private SubscriberValidationService subscriberValidationService;
    private SubscriptionRequestValidationService subscriptionValidationService;

    @Autowired
    public SubscriptionManager(SubscriberDao subscriberDao,
            SubscriptionDao subscriptionDao,
            SubscriptionObservationDao observationDao,
            SubscriberMessagingService subscriberMessagingService,
            DeveloperDAO developerDao,
            ProductDAO productDao,
            ListingSearchService listingSearchService,
            ErrorMessageUtil errorMessageUtil,
            SubscriptionLookupUtil lookupUtil) {
        this.subscriberDao = subscriberDao;
        this.subscriptionDao = subscriptionDao;
        this.observationDao = observationDao;
        this.subscriberMessagingService = subscriberMessagingService;
        this.developerDao = developerDao;
        this.productDao = productDao;
        this.listingSearchService = listingSearchService;
        this.errorMessageUtil = errorMessageUtil;
        this.lookupUtil = lookupUtil;
        this.subscriptionValidationService = new SubscriptionRequestValidationService();
        this.subscriberValidationService = new SubscriberValidationService();
    }

    @Transactional
    public List<SubscriberRole> getAllRoles() {
        return subscriberDao.getAllRoles();
    }

    @Transactional
    public List<SubscriptionObjectType> getAllSubscriptionObjectTypes() {
        return subscriptionDao.getAllSubscriptionObjectTypes();
    }

    @Transactional
    public Subscriber subscribe(SubscriptionRequest subscriptionRequest) throws ValidationException {
        SubscriptionRequestValidationContext validationContext = getSubscriptionValidationContext(subscriptionRequest);
        ValidationException validationException = new ValidationException(subscriptionValidationService.validate(validationContext));
        if (validationException.getErrorMessages().size() > 0) {
            throw validationException;
        }

        Subscriber subscriber = subscriberDao.getSubscriberByEmail(subscriptionRequest.getEmail());
        if (subscriber == null) {
            UUID newSubscriberId = subscriberDao.createSubscriber(subscriptionRequest.getEmail());
            subscriber = subscriberDao.getSubscriberById(newSubscriberId);
        }

        if (subscriber.getStatus().getId().equals(lookupUtil.getPendingSubscriberStatusId())) {
            subscriberMessagingService.sendConfirmation(subscriber);
        }

        subscriptionDao.createSubscription(subscriber.getId(), subscriptionRequest.getSubscribedObjectTypeId(),
                subscriptionRequest.getSubscribedObjectId());
        return subscriber;
    }

    @Transactional
    public Subscriber getSubscriber(UUID subscriberId) throws EntityRetrievalException {
        Subscriber subscriber = subscriberDao.getSubscriberById(subscriberId);
        if (subscriber == null) {
            throw new EntityRetrievalException("No subscriber matching " + subscriberId + " was found.");
        }
        return subscriber;
    }

    @Transactional
    public List<? extends ChplItemSubscriptionGroup> getGroupedSubscriptions(UUID subscriberId) throws EntityRetrievalException {
        Subscriber subscriber = getSubscriber(subscriberId);

        //get flattened list of subscriptions
        List<Subscription> subscriptions = subscriptionDao.getSubscriptionsForSubscriber(subscriber.getId());

        //group the subscriptions together by subject ID and subscribedObjectID
        Map<Pair<Long, Long>, List<Subscription>> subscriptionsGroupedByChplItem
            = subscriptions.stream().collect(Collectors.groupingBy(
                    subscription -> new ImmutablePair<>(subscription.getSubject().getType().getId(), subscription.getSubscribedObjectId())));

        //create appropriate object and
        //fill in extra info such as Developer, Product, ACB, last notified date
        List<? extends ChplItemSubscriptionGroup> subscriptionGroups
            = subscriptionsGroupedByChplItem.values().stream()
                .map(subscriptionsForItem -> createChplItemSubscription(subscriptionsForItem))
                .filter(subscriptionGroup -> subscriptionGroup != null)
                .collect(Collectors.toList());
        return subscriptionGroups;
    }

    private ChplItemSubscriptionGroup createChplItemSubscription(List<Subscription> flatSubscriptionsForChplItem) {
        if (flatSubscriptionsForChplItem.get(0).getSubject().getType().getId()
                .equals(lookupUtil.getListingObjectTypeId())) {
            return createListingSubscriptionGroup(flatSubscriptionsForChplItem);
        } else if (flatSubscriptionsForChplItem.get(0).getSubject().getType().getId()
                .equals(lookupUtil.getDeveloperObjectTypeId())) {
            //TODO future ticket
            return null;
        }  else if (flatSubscriptionsForChplItem.get(0).getSubject().getType().getId()
                .equals(lookupUtil.getProductObjectTypeId())) {
            //TODO future ticket
            return null;
        }
        return null;
    }

    private ListingSubscriptionGroup createListingSubscriptionGroup(List<Subscription> flatSubscriptionsForListing) {
        Long listingId = flatSubscriptionsForListing.stream()
                .map(sub -> sub.getSubscribedObjectId())
                .findAny().get();

        ListingSearchResult listing = null;
        try {
            listing = listingSearchService.findListing(listingId);
        } catch (Exception ex) {
            LOGGER.error("Unable to find listing with ID " + listingId, ex);
        }

        if (listing != null) {
            return ListingSubscriptionGroup.builder()
                    .certificationBodyId(listing.getCertificationBody().getId())
                    .certificationBodyName(listing.getCertificationBody().getName())
                    .certifiedProductId(listing.getId())
                    .chplProductNumber(listing.getChplProductNumber())
                    .developerId(listing.getDeveloper().getId())
                    .developerName(listing.getDeveloper().getName())
                    .productId(listing.getProduct().getId())
                    .productName(listing.getProduct().getName())
                    .versionId(listing.getVersion().getId())
                    .version(listing.getVersion().getName())
                    .subscriptions(flatSubscriptionsForListing.stream()
                            .map(sub -> ChplItemSubscription.builder()
                                    .id(sub.getId())
                                    .consolidationMethod(sub.getConsolidationMethod())
                                    .subject(sub.getSubject())
                                    .build())
                            .collect(Collectors.toList()))
            .build();
        }
        return null;
    }

    @Transactional
    public Subscriber confirm(UUID subscriberId, Long roleId) throws ValidationException {
        SubscriberValidationContext validationContext = getSubscriberValidationContext(subscriberId);
        ValidationException validationException = new ValidationException(subscriberValidationService.validate(validationContext));
        if (validationException.getErrorMessages().size() > 0) {
            throw validationException;
        }
        subscriberDao.confirmSubscriber(subscriberId, roleId);

        Subscriber subscriber = subscriberDao.getSubscriberById(subscriberId);
        if (subscriber.getStatus().getId().equals(lookupUtil.getConfirmedSubscriberStatusId())) {
            subscriberMessagingService.sendWelcome(subscriber);
        }

        return subscriberDao.getSubscriberById(subscriberId);
    }

    @Transactional
    public void deleteSubscription(Subscriber subscriber, Long subscriptionId) {
        observationDao.deleteObservationsForSubscription(subscriptionId);
        subscriptionDao.deleteSubscription(subscriptionId);
        deleteSubscriberIfNoRemainingSubscriptions(subscriber);
    }

    @Transactional
    public void deleteSubscriptions(Subscriber subscriber, Long subscribedObjectTypeId, Long subscribedObjectId) throws EntityRetrievalException {
        if (subscriber == null
                || subscriber.getId() == null
                || subscriberDao.getSubscriberById(subscriber.getId()) == null) {
            throw new EntityRetrievalException("No subscriber matching " + subscriber.getId() + " was found.");
        }

        observationDao.deleteObservationsForSubscribedObject(subscriber.getId(), subscribedObjectTypeId, subscribedObjectId);
        subscriptionDao.deleteSubscriptions(subscriber.getId(), subscribedObjectTypeId, subscribedObjectId);
        deleteSubscriberIfNoRemainingSubscriptions(subscriber);
    }

    private void deleteSubscriberIfNoRemainingSubscriptions(Subscriber subscriber) {
        List<Subscription> subscriptions = subscriptionDao.getSubscriptionsForSubscriber(subscriber.getId());
        if (CollectionUtils.isEmpty(subscriptions)) {
            subscriberDao.deleteSubscriber(subscriber.getId());
        }
    }

    @Transactional
    public void unsubscribeAll(UUID subscriberId) throws EntityRetrievalException {
        Subscriber subscriber = getSubscriber(subscriberId);

        observationDao.deleteAllObservationsForSubscriber(subscriber.getId());
        subscriptionDao.deleteSubscriptions(subscriber.getId());
        subscriberDao.deleteSubscriber(subscriber.getId());
    }

    private SubscriptionRequestValidationContext getSubscriptionValidationContext(SubscriptionRequest subscriptionRequest) {
        return new SubscriptionRequestValidationContext(
                subscriptionRequest,
                subscriberDao,
                subscriptionDao,
                developerDao,
                productDao,
                listingSearchService,
                errorMessageUtil,
                lookupUtil);
    }

    private SubscriberValidationContext getSubscriberValidationContext(UUID subscriberId) {
        return new SubscriberValidationContext(
                subscriberId,
                subscriberDao,
                errorMessageUtil);
    }
}

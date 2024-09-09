package gov.healthit.chpl.web.controller;

import java.util.List;
import java.util.UUID;

import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import gov.healthit.chpl.domain.schedule.ChplOneTimeTrigger;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.subscription.SubscriptionManager;
import gov.healthit.chpl.subscription.domain.ChplItemSubscriptionGroup;
import gov.healthit.chpl.subscription.domain.Subscriber;
import gov.healthit.chpl.subscription.domain.SubscriberRequest;
import gov.healthit.chpl.subscription.domain.SubscriberRole;
import gov.healthit.chpl.subscription.domain.SubscriptionObjectType;
import gov.healthit.chpl.subscription.domain.SubscriptionRequest;
import gov.healthit.chpl.util.SwaggerSecurityRequirement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "subscriptions", description = "Manage public user subscriptions.")
@RestController
public class SubscriptionController {
    private SubscriptionManager subscriptionManager;

    @Autowired
    public SubscriptionController(SubscriptionManager subscriptionManager) {
        this.subscriptionManager = subscriptionManager;
    }

    @Operation(summary = "Get available subscriber roles.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/subscribers/roles",
        method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<SubscriberRole> getSubscriptionRoles() {
        return subscriptionManager.getAllRoles();
    }

    @Operation(summary = "Get available types of things that may be subscribed to.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/subscriptions/types",
        method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<SubscriptionObjectType> getSubscribedObjectTypes() {
        return subscriptionManager.getAllSubscriptionObjectTypes();
    }

    @Operation(summary = "Get information about a subscriber.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/subscribers/{subscriberId:^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$}",
        method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public Subscriber getSubscriber(@PathVariable String subscriberId) throws EntityRetrievalException {
        return subscriptionManager.getSubscriber(UUID.fromString(subscriberId));
    }

    @Operation(summary = "Gets all the subscriptions for a subscriber grouped by each item in the CHPL.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/subscribers/{subscriberId}/subscriptions",
        method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public List<? extends ChplItemSubscriptionGroup> getSubscriptionsForSubscriber(@PathVariable String subscriberId)
        throws EntityRetrievalException {
        return subscriptionManager.getGroupedSubscriptions(UUID.fromString(subscriberId));
    }

    @Operation(summary = "Delete one subscription",
            description = "Example: Unsubscribe subscriber with ID 1 from subscription ID 1",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/subscribers/{subscriberId}/subscriptions/{subscriptionId}",
        method = RequestMethod.DELETE, produces = "application/json; charset=utf-8")
    public void deleteSubscription(@PathVariable String subscriberId, @PathVariable Long subscriptionId)
            throws EntityRetrievalException {
        //NOTE: I put the subscriberID in the URL here because that makes it harder to guess the URL to delete a subscription.
        //Without it, you could put in a URL like DELETE /subscriptions/7 and delete a subscription that doesn't belong to you

        //throw 404 if invalid subscriber ID
        Subscriber subscriber = subscriptionManager.getSubscriber(UUID.fromString(subscriberId));
        subscriptionManager.deleteSubscription(subscriber, subscriptionId);
    }

    @Operation(summary = "Delete all subscriptions on a particular CHPL object for a subscriber. ",
            description = "Example: Unsubscribe subscriber with ID 1 from all notifications about listing ID 1",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/subscribers/{subscriberId}/subscriptions",
            method = RequestMethod.DELETE, produces = "application/json; charset=utf-8")
    public void deleteSubscriptionsForObject(@PathVariable String subscriberId,
            @RequestParam(name = "subscribedObjectTypeId") Long subscribedObjectTypeId,
            @RequestParam(name = "subscribedObjectId") Long subscribedObjectId) throws EntityRetrievalException {
        Subscriber subscriber = Subscriber.builder()
                .id(UUID.fromString(subscriberId))
                .build();
        subscriptionManager.deleteSubscriptions(subscriber, subscribedObjectTypeId, subscribedObjectId);
    }

    @Operation(summary = "Subscribe to periodic notifications about changes to a specific item in the CHPL. "
            + "A new subscriber will be created and will need confirm their email address "
            + "if there is not currently a subscriber with this email address.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/subscriptions",
        method = RequestMethod.POST, produces = "application/json; charset=utf-8",
        consumes = MediaType.APPLICATION_JSON_VALUE)
    public Subscriber subscribe(@RequestBody(required = true) SubscriptionRequest subscriptionRequest)
        throws ValidationException {
        return subscriptionManager.subscribe(subscriptionRequest);
    }

    @Operation(summary = "Request an email to be sent to the logged-in user with a report "
            + "of all subscription notifications that have been sent.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/subscriptions/notifications-report",
        method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public ChplOneTimeTrigger triggerSubscriptionObservationNotificationsReport()
            throws SchedulerException, ValidationException {
        return subscriptionManager.triggerSubscriptionObservationNotificationsReport();
    }

    @Operation(summary = "Confirm a subscriber's email address is valid. Once confirmed, the subscriber "
            + "will start receiving notifications at the specified email address.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/subscriptions/confirm-subscriber",
        method = RequestMethod.PUT, produces = "application/json; charset=utf-8",
        consumes = MediaType.APPLICATION_JSON_VALUE)
    public Subscriber confirmSubscriber(@RequestBody(required = true) SubscriberRequest request)
        throws ValidationException {
        return subscriptionManager.confirm(UUID.fromString(request.getSubscriberId()), request.getRoleId());
    }

    @Operation(summary = "Unsubscribe from all notifications associated with a subscriber and delete that subscriber.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/subscribers/{subscriberId:^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$}",
        method = RequestMethod.DELETE, produces = "application/json; charset=utf-8")
    public void unsubscribeAll(@PathVariable String subscriberId)
        throws EntityRetrievalException {
        subscriptionManager.unsubscribeAll(UUID.fromString(subscriberId));
    }
}

package gov.healthit.chpl.web.controller;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityNotFoundException;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.notification.Recipient;
import gov.healthit.chpl.domain.notification.Subscription;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.notification.NotificationTypeDTO;
import gov.healthit.chpl.dto.notification.NotificationTypeRecipientMapDTO;
import gov.healthit.chpl.dto.notification.RecipientDTO;
import gov.healthit.chpl.dto.notification.RecipientWithSubscriptionsDTO;
import gov.healthit.chpl.dto.notification.SubscriptionDTO;
import gov.healthit.chpl.manager.NotificationManager;
import gov.healthit.chpl.web.controller.results.NotificationRecipientResults;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value="notifications")
@RestController
@RequestMapping("/notifications")
public class NotificationController {

	private static final Logger LOGGER = LogManager.getLogger(NotificationController.class);

	@Autowired NotificationManager notificationManager;
	@Autowired MessageSource messageSource;

	@ApiOperation(value = "Get the list of all recipients and their associated subscriptions that are applicable to the currently logged in user")
	@RequestMapping(value = "/recipients", method = RequestMethod.GET, produces = "application/json; charset = utf-8")
	public @ResponseBody NotificationRecipientResults getAllRecipientsWithSubscriptions() {
		List<RecipientWithSubscriptionsDTO> foundSubs = notificationManager.getAll();

		List<Recipient> resultRecips = new ArrayList<Recipient>();
		for(RecipientWithSubscriptionsDTO foundSub : foundSubs) {
			Recipient recip = new Recipient(foundSub);
			resultRecips.add(recip);
		}

		NotificationRecipientResults results = new NotificationRecipientResults();
		results.setResults(resultRecips);
		return results;
	}

	@ApiOperation(value = "Update the email address and associated subscriptions of the recipient specified.")
	@RequestMapping(value = "/recipients/ {recipientId}/update", method = RequestMethod.POST, produces = "application/json; charset = utf-8")
	public @ResponseBody Recipient updateRecipient(@PathVariable("recipientId") Long recipientId,
			@RequestBody Recipient updatedRecipient) throws InvalidArgumentsException, EntityRetrievalException {
		if(recipientId.longValue() != updatedRecipient.getId().longValue()) {
			throw new InvalidArgumentsException("Recipient id '" + recipientId + "' in the URL does not match recipient id '" + updatedRecipient.getId() + "' in the request body.");
		}

		//get the current subscriptions
		RecipientWithSubscriptionsDTO existingRecipient = notificationManager.getAllForRecipient(recipientId);
		RecipientDTO recipient = new RecipientDTO();
		recipient.setId(updatedRecipient.getId());
		if(!existingRecipient.getEmail().equals(updatedRecipient.getEmail())) {
			//update the email address
			recipient = notificationManager.updateRecipient(recipientId, updatedRecipient.getEmail());
		}

		List<SubscriptionDTO> existingSubs = existingRecipient.getSubscriptions();

		//figure out what subscriptions need to be added
		List<SubscriptionDTO> subsToAdd = new ArrayList<SubscriptionDTO>();
		for(Subscription updatedSub : updatedRecipient.getSubscriptions()) {
			boolean alreadyExists = false;
			for(SubscriptionDTO existingSub : existingSubs) {
				if(updatedSub.getNotificationType().getId().longValue() == existingSub.getNotificationType().getId().longValue()
					&&
					((updatedSub.getAcb() == null && existingSub.getAcb() == null) ||
					 (updatedSub.getAcb().getId().longValue() == existingSub.getAcb().getId().longValue()))) {
					alreadyExists = true;
					}
			}
			if(!alreadyExists) {
				SubscriptionDTO toAdd = new SubscriptionDTO();
				if(updatedSub.getAcb() != null) {
					CertificationBodyDTO acb = new CertificationBodyDTO();
					acb.setId(updatedSub.getAcb().getId());
					toAdd.setAcb(acb);
				}
				NotificationTypeDTO type = new NotificationTypeDTO();
				type.setId(updatedSub.getNotificationType().getId());
				toAdd.setNotificationType(type);
				subsToAdd.add(toAdd);
			}
		}

		for(SubscriptionDTO subToAdd : subsToAdd) {
			NotificationTypeRecipientMapDTO newMapping = new NotificationTypeRecipientMapDTO();
			newMapping.setRecipient(recipient);
			newMapping.setSubscription(subToAdd);
			notificationManager.addRecipientNotificationMap(newMapping);
		}

		//figure out what subscriptions need to be deleted
		List<SubscriptionDTO> subsToRemove = new ArrayList<SubscriptionDTO>();
		for(SubscriptionDTO existingSub : existingSubs) {
			boolean stillExists = false;
			for(Subscription updatedSub : updatedRecipient.getSubscriptions()) {
				if(updatedSub.getNotificationType().getId().longValue() == existingSub.getNotificationType().getId().longValue()
					&&
					((updatedSub.getAcb() == null && existingSub.getAcb() == null) ||
					 (updatedSub.getAcb().getId().longValue() == existingSub.getAcb().getId().longValue()))) {
					stillExists = true;
				}
			}

			if(!stillExists) {
				subsToRemove.add(existingSub);
			}
		}

		for(SubscriptionDTO subToRemove : subsToRemove) {
			NotificationTypeRecipientMapDTO delMapping = new NotificationTypeRecipientMapDTO();
			delMapping.setRecipient(recipient);
			delMapping.setSubscription(subToRemove);
			notificationManager.deleteRecipientNotificationMap(delMapping);
		}

		RecipientWithSubscriptionsDTO recipToReturn = notificationManager.getAllForRecipient(recipientId);
		return new Recipient(recipToReturn);
	}

	@ApiOperation(value = "Creates a new recipient with any subscriptions included in the request body. At least 1 subscription is required.")
	@RequestMapping(value = "/recipients/create", method = RequestMethod.POST, produces = "application/json; charset = utf-8")
	public @ResponseBody Recipient createRecipient(@RequestBody Recipient recipientToAdd)
		throws InvalidArgumentsException, EntityCreationException, EntityRetrievalException {
		if(recipientToAdd.getSubscriptions() == null || recipientToAdd.getSubscriptions().size() == 0) {
			throw new InvalidArgumentsException("At least one subscription must be included with the request.");
		}
		if(recipientToAdd.getId() == null && StringUtils.isEmpty(recipientToAdd.getEmail())) {
			throw new InvalidArgumentsException("A recipient id or email address is required to create a new subscription.");
		}
		if(!StringUtils.isEmpty(recipientToAdd.getEmail()) && notificationManager.recipientEmailExists(recipientToAdd.getEmail())) {
			throw new InvalidArgumentsException("The email address '" + recipientToAdd.getEmail() + "' is already in the system.");
		}

		RecipientDTO recipientDto = new RecipientDTO();
		recipientDto.setEmailAddress(recipientToAdd.getEmail());
		RecipientDTO added = notificationManager.createRecipient(recipientDto);
		for(Subscription subscriptionToAdd : recipientToAdd.getSubscriptions()) {
			NotificationTypeRecipientMapDTO dtoToAdd = new NotificationTypeRecipientMapDTO();
			RecipientDTO recip = new RecipientDTO();
			recip.setId(recipientToAdd.getId());
			recip.setEmailAddress(recipientToAdd.getEmail());
			dtoToAdd.setRecipient(recip);
			SubscriptionDTO sub = new SubscriptionDTO();
			if(subscriptionToAdd.getAcb() != null) {
				CertificationBodyDTO acbDto = new CertificationBodyDTO();
				acbDto.setId(subscriptionToAdd.getAcb().getId());
				acbDto.setName(subscriptionToAdd.getAcb().getName());
				sub.setAcb(acbDto);
			}
			if(subscriptionToAdd.getNotificationType() == null || subscriptionToAdd.getNotificationType().getId() == null) {
				throw new InvalidArgumentsException("A notification type id is required for all subscriptions to be created.");
			} else {
				NotificationTypeDTO typeDto = new NotificationTypeDTO();
				typeDto.setId(subscriptionToAdd.getNotificationType().getId());
				sub.setNotificationType(typeDto);
			}
			dtoToAdd.setSubscription(sub);
			notificationManager.addRecipientNotificationMap(dtoToAdd);
		}
		RecipientWithSubscriptionsDTO dtoResult = notificationManager.getAllForRecipient(added.getId());
		if(dtoResult != null) {
			return new Recipient(dtoResult);
		}
		return null;

	}

	@ApiOperation(value = "Remove subscription(s) for a recipient.")
	@RequestMapping(value = "/recipients/ {recipientId}/delete", method = RequestMethod.POST, produces = "application/json; charset = utf-8")
	public @ResponseBody void deleteRecipient(@PathVariable("recipientId") Long recipientId)
		throws EntityRetrievalException, InvalidArgumentsException {
		try {
			notificationManager.deleteRecipient(recipientId);
		} catch(final EntityNotFoundException ex) {
			throw new InvalidArgumentsException(ex.getMessage());
		}
	}
}

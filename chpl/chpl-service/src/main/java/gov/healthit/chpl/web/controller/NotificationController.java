package gov.healthit.chpl.web.controller;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import gov.healthit.chpl.domain.notification.Recipient;
import gov.healthit.chpl.domain.notification.Subscription;
import gov.healthit.chpl.dto.notification.RecipientDTO;
import gov.healthit.chpl.dto.notification.RecipientWithSubscriptionsDTO;
import gov.healthit.chpl.manager.NotificationManager;
import gov.healthit.chpl.web.controller.results.NotificationRecipientResults;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value="notifications")
@RestController
@RequestMapping("/notifications")
public class NotificationController {
	
	private static final Logger logger = LogManager.getLogger(NotificationController.class);

	
	@Autowired NotificationManager notificationManager;

	@ApiOperation(value = "Get the list of all recipients and their associated subscriptions that are applicable to the currently logged in user")
	@RequestMapping(value = "/recipients", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
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
	
	@ApiOperation(value = "Update the email address of a recipient with the specified id keeping all subscriptions the same")
	@RequestMapping(value = "recipients/{recipientId}", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	public @ResponseBody Recipient updateAllRecipients(@PathVariable("recipientId") Long id,
			@RequestParam(value="email", required=true) String newEmailAddress) {
		RecipientDTO recip = new RecipientDTO();
		recip.setEmailAddress(newEmailAddress);
		notificationManager.updateRecipient(toUpdate);
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
}

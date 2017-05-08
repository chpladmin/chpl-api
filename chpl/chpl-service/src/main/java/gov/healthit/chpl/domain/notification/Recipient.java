package gov.healthit.chpl.domain.notification;

import java.util.ArrayList;
import java.util.List;

import gov.healthit.chpl.dto.notification.SubscriptionDTO;
import gov.healthit.chpl.dto.notification.RecipientDTO;
import gov.healthit.chpl.dto.notification.RecipientWithSubscriptionsDTO;

public class Recipient {
	private Long id;
	private String email;
	private List<Subscription> subscriptions;
	
	public Recipient() {
		subscriptions = new ArrayList<Subscription>();
	}
	
	public Recipient(RecipientDTO dto) {
		this();
		this.id = dto.getId();
		this.email = dto.getEmailAddress();
	}
	
	public Recipient(RecipientWithSubscriptionsDTO dto) {
		this();
		this.id = dto.getId();
		this.email = dto.getEmail();
		if(dto.getSubscriptions() != null && dto.getSubscriptions().size() > 0) {
			for(SubscriptionDTO subscriptionDto : dto.getSubscriptions()) {
				Subscription subscription = new Subscription(subscriptionDto);
				this.subscriptions.add(subscription);
			}
		}
	}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}

	public List<Subscription> getSubscriptions() {
		return subscriptions;
	}

	public void setSubscriptions(List<Subscription> subscriptions) {
		this.subscriptions = subscriptions;
	}
}

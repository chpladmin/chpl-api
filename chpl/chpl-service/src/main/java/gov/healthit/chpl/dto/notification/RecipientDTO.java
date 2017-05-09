package gov.healthit.chpl.dto.notification;

import gov.healthit.chpl.entity.notification.NotificationRecipientEntity;

public class RecipientDTO {
	private Long id;
	private String emailAddress;
	
	public RecipientDTO() {}
	
	public RecipientDTO(NotificationRecipientEntity entity) {
		this.id = entity.getId();
		this.emailAddress = entity.getEmail();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

}

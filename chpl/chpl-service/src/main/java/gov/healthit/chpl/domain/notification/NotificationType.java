package gov.healthit.chpl.domain.notification;

import gov.healthit.chpl.dto.notification.NotificationTypeDTO;

public class NotificationType {
	private Long id;
	private String name;
	private String description;
	private Boolean requiresAcb;
	
	public NotificationType() {}
	public NotificationType(NotificationTypeDTO dto) {
		this.id = dto.getId();
		this.name = dto.getName();
		this.description = dto.getDescription();
		this.requiresAcb = dto.getRequiresAcb();
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Boolean getRequiresAcb() {
		return requiresAcb;
	}
	public void setRequiresAcb(Boolean requiresAcb) {
		this.requiresAcb = requiresAcb;
	}
}

package gov.healthit.chpl.domain;

import gov.healthit.chpl.dto.JobTypeDTO;
import gov.healthit.chpl.dto.notification.NotificationTypeDTO;

public class JobType {
	private Long id;
	private String name;
	
	public JobType() {}
	public JobType(JobTypeDTO dto) {
		this.id = dto.getId();
		this.name = dto.getName();
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
}

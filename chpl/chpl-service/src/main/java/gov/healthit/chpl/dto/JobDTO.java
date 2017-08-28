package gov.healthit.chpl.dto;

import java.io.Serializable;
import java.util.Date;

import gov.healthit.chpl.entity.JobEntity;
import gov.healthit.chpl.entity.UcdProcessEntity;

public class JobDTO implements Serializable {
	private static final long serialVersionUID = -7841496230766066264L;
	private Long id;
	private JobTypeDTO jobType;
	private ContactDTO contact;
	private Date startTime;
	private Date endTime;
	private String data;
	
	public JobDTO(){}
	
	public JobDTO(JobEntity entity){		
		this.id = entity.getId();
		this.startTime = entity.getStartTime();
		this.endTime = entity.getEndTime();
		this.data = entity.getData();
		
		if(entity.getJobType() != null) {
			this.jobType = new JobTypeDTO(entity.getJobType());
		} else {
			this.jobType = new JobTypeDTO();
			this.jobType.setId(entity.getJobTypeId());
		}
		
		if(entity.getContact() != null) {
			this.contact = new ContactDTO(entity.getContact());
		} else {
			this.contact = new ContactDTO();
			this.contact.setId(entity.getContactId());
		}
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public JobTypeDTO getJobType() {
		return jobType;
	}

	public void setJobType(JobTypeDTO jobType) {
		this.jobType = jobType;
	}

	public ContactDTO getContact() {
		return contact;
	}

	public void setContact(ContactDTO contact) {
		this.contact = contact;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}
}

package gov.healthit.chpl.domain;

import java.util.ArrayList;
import java.util.List;

import gov.healthit.chpl.dto.job.JobDTO;
import gov.healthit.chpl.dto.job.JobMessageDTO;

public class Job {
	private Long id;
	private JobType type;
	private Contact user;
	private JobStatus status;
	private Long startTime;
	private Long endTime;
	private List<String> messages;
	
	public Job() {
		messages = new ArrayList<String>();
	}
	
	public Job(JobDTO dto) {
		this();
		this.id = dto.getId();
		this.type = new JobType(dto.getJobType());
		this.user = new Contact(dto.getContact());
		if(dto.getStatus() != null) {
			this.status = new JobStatus(dto.getStatus());
		}
		this.startTime = dto.getStartTime().getTime();
		this.endTime = dto.getEndTime() == null ? null : dto.getEndTime().getTime();
		if(dto.getMessages() != null) {
			for(JobMessageDTO message : dto.getMessages()) {
				this.messages.add(message.getMessage());
			}
		}
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public JobType getType() {
		return type;
	}
	public void setType(JobType type) {
		this.type = type;
	}
	public Contact getUser() {
		return user;
	}
	public void setUser(Contact user) {
		this.user = user;
	}
	public Long getStartTime() {
		return startTime;
	}
	public void setStartTime(Long startTime) {
		this.startTime = startTime;
	}
	public Long getEndTime() {
		return endTime;
	}
	public void setEndTime(Long endTime) {
		this.endTime = endTime;
	}

	public JobStatus getStatus() {
		return status;
	}

	public void setStatus(JobStatus status) {
		this.status = status;
	}

	public List<String> getMessages() {
		return messages;
	}

	public void setMessages(List<String> messages) {
		this.messages = messages;
	}
}

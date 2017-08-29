package gov.healthit.chpl.domain;

import gov.healthit.chpl.dto.JobDTO;

public class Job {
	private Long id;
	private JobType type;
	private Contact user;
	private Long startTime;
	private Long endTime;
	
	public Job() {}
	public Job(JobDTO dto) {
		this.id = dto.getId();
		this.type = new JobType(dto.getJobType());
		this.user = new Contact(dto.getContact());
		this.startTime = dto.getStartTime().getTime();
		this.endTime = dto.getEndTime().getTime();
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
}

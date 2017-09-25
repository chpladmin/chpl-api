package gov.healthit.chpl.dto.job;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import gov.healthit.chpl.auth.dto.UserDTO;
import gov.healthit.chpl.dto.ContactDTO;
import gov.healthit.chpl.entity.job.JobEntity;
import gov.healthit.chpl.entity.job.JobMessageEntity;

public class JobDTO implements Serializable {
	private static final long serialVersionUID = -7841496230766066264L;
	private Long id;
	private JobTypeDTO jobType;
	private UserDTO user;
	private JobStatusDTO status;
	private Date startTime;
	private Date endTime;
	private String data;
	private List<JobMessageDTO> messages;
	
	public JobDTO(){
		messages = new ArrayList<JobMessageDTO>();
	}
	
	public JobDTO(JobEntity entity){
		this();
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
		
		if(entity.getUser() != null) {
			this.user = new UserDTO(entity.getUser());
		} else {
			this.user = new UserDTO();
			this.user.setId(entity.getUserId());
		}
		
		if(entity.getStatus() != null) {
			this.status = new JobStatusDTO(entity.getStatus());
		}
		
		if(entity.getMessages() != null) {
			for(JobMessageEntity message : entity.getMessages()) {
				this.messages.add(new JobMessageDTO(message));
			}
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

	public JobStatusDTO getStatus() {
		return status;
	}

	public void setStatus(JobStatusDTO status) {
		this.status = status;
	}

	public List<JobMessageDTO> getMessages() {
		return messages;
	}

	public void setMessages(List<JobMessageDTO> messages) {
		this.messages = messages;
	}

	public UserDTO getUser() {
		return user;
	}

	public void setUser(UserDTO user) {
		this.user = user;
	}
}

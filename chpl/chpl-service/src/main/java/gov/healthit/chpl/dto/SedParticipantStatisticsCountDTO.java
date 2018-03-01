package gov.healthit.chpl.dto;

import java.io.Serializable;
import java.util.Date;

import gov.healthit.chpl.entity.SedParticipantStatisticsCountEntity;

public class SedParticipantStatisticsCountDTO implements Serializable {

	/** Serial Version UID. */
	private static final long serialVersionUID = -1536844909545189801L;
	
	private Long id;
	private Long sedCount;
	private Long participantCount;
	private Date creationDate;
    private Boolean deleted;
    private Date lastModifiedDate;
    private Long lastModifiedUser;
    
	public SedParticipantStatisticsCountDTO() {
		//Default Constructor
	}
	
	public SedParticipantStatisticsCountDTO(SedParticipantStatisticsCountEntity entity) {
		this.setId(entity.getId());
		this.setSedCount(entity.getSedCount());
		this.setParticipantCount(entity.getParticipantCount());
	 	this.creationDate = entity.getCreationDate();
        this.deleted = entity.getDeleted();
        this.lastModifiedDate = entity.getLastModifiedDate();
        this.lastModifiedUser = entity.getLastModifiedUser();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getParticipantCount() {
		return participantCount;
	}

	public void setParticipantCount(Long participantCount) {
		this.participantCount = participantCount;
	}

	public Long getSedCount() {
		return sedCount;
	}

	public void setSedCount(Long sedCount) {
		this.sedCount = sedCount;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public Boolean getDeleted() {
		return deleted;
	}

	public void setDeleted(Boolean deleted) {
		this.deleted = deleted;
	}

	public Date getLastModifiedDate() {
		return lastModifiedDate;
	}

	public void setLastModifiedDate(Date lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}

	public Long getLastModifiedUser() {
		return lastModifiedUser;
	}

	public void setLastModifiedUser(Long lastModifiedUser) {
		this.lastModifiedUser = lastModifiedUser;
	}
}

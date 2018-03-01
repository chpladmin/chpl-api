package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.Date;

import gov.healthit.chpl.dto.SedParticipantStatisticsCountDTO;

public class SedParticipantStatisticsCount implements Serializable {
	private static final long serialVersionUID = 6166234350175390349L;
	
	private Long id;
	private Long sedCount;
	private Long participantCount;
	private Date creationDate;
    private Boolean deleted;
    private Date lastModifiedDate;
    private Long lastModifiedUser;
    
    public SedParticipantStatisticsCount() {
    	//Default Constructor
    }
    
    public SedParticipantStatisticsCount(SedParticipantStatisticsCountDTO dto) {
    	this.id = dto.getId();
    	this.sedCount = dto.getSedCount();
    	this.participantCount = dto.getParticipantCount();
    	this.deleted = dto.getDeleted();
        this.lastModifiedDate = dto.getLastModifiedDate();
        this.lastModifiedUser = dto.getLastModifiedUser();
        this.creationDate = dto.getCreationDate();
    }

    public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getSedCount() {
		return sedCount;
	}
	public void setSedCount(Long sedCount) {
		this.sedCount = sedCount;
	}
	public Long getParticipantCount() {
		return participantCount;
	}
	public void setParticipantCount(Long participantCount) {
		this.participantCount = participantCount;
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

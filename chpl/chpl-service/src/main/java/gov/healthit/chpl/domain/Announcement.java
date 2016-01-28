package gov.healthit.chpl.domain;
import gov.healthit.chpl.dto.AnnouncementDTO;

import java.util.Date;


public class Announcement{

	

	private Long id;
	public String title;
	private String text;
	private Date startDate;
	private Date endDate;
	private Boolean isPublic;
	private Date creationDate;
	private Boolean deleted;
	private Date lastModifiedDate;
	private Long lastModifiedUser;


	public Announcement(){}

	public Announcement(AnnouncementDTO dto){
		this.id = dto.getId();
		this.title = dto.getTitle();
		this.text = dto.getText();
		this.startDate = dto.getStartDate();
		this.endDate = dto.getEndDate();
		this.isPublic = dto.getIsPublic();
		this.deleted = dto.getDeleted();
		this.lastModifiedDate = dto.getLastModifiedDate();
		this.lastModifiedUser = dto.getLastModifiedUser();
		this.creationDate = dto.getCreationDate();

	}
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
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

	public Boolean getIsPublic() {
		return isPublic;
	}

	public void setIsPublic(Boolean isPublic) {
		this.isPublic = isPublic;
	}

}



package gov.healthit.chpl.entity;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name="corrective_action_plan_documentation")
public class CorrectiveActionPlanDocumentationEntity {
	
	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Basic( optional = false )
	@Column( name = "corrective_action_plan_documentation_id", nullable = false  )
	private Long id;
	
	@Basic( optional = false )
	@OneToOne(optional = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "corrective_action_plan_id", unique=true, nullable = true)
	private CorrectiveActionPlanEntity correctiveActionPlan;
	
	@Basic( optional = false )
	@Column(name = "filename")
	private String fileName;
	
	@Column(name = "filetype")
	private String fileType;
	
	@Basic( optional = false )
	@Column(name = "filedata")
	private byte[] fileData;
	
	@Basic( optional = false )
	@Column( name = "creation_date", nullable = false  )
	private Date creationDate;
	
	@Basic( optional = false )
	@Column( name = "last_modified_date", nullable = false  )
	private Date lastModifiedDate;
	
	@Basic( optional = false )
	@NotNull()
	@Column( name = "last_modified_user", nullable = false  )
	private Long lastModifiedUser;
	
	@Basic( optional = false )
	@NotNull()
	@Column( nullable = false  )
	private Boolean deleted;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public CorrectiveActionPlanEntity getCorrectiveActionPlan() {
		return correctiveActionPlan;
	}

	public void setCorrectiveActionPlan(CorrectiveActionPlanEntity correctiveActionPlan) {
		this.correctiveActionPlan = correctiveActionPlan;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
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

	public Boolean getDeleted() {
		return deleted;
	}

	public void setDeleted(Boolean deleted) {
		this.deleted = deleted;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFileType() {
		return fileType;
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	public byte[]  getFileData() {
		return fileData;
	}

	public void setFileData(byte[]  fileData) {
		this.fileData = fileData;
	}
}

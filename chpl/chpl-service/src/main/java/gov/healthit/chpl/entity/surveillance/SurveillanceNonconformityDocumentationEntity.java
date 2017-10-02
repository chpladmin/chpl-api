package gov.healthit.chpl.entity.surveillance;

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

@Entity
@Table(name="surveillance_nonconformity_document")
public class SurveillanceNonconformityDocumentationEntity {
	
	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column( name = "id", nullable = false  )
	private Long id;
	
	@Column(name = "surveillance_nonconformity_id")
	private Long nonconformityId;
	
	@OneToOne(optional = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "surveillance_nonconformity_id", insertable = false, updatable = false)
	private SurveillanceNonconformityEntity nonconformityEntity;
	
	@Column(name = "filename")
	private String fileName;
	
	@Column(name = "filetype")
	private String fileType;
	
	@Basic(fetch = FetchType.LAZY)
	@Column(name = "filedata")
	private byte[] fileData;
	
	@Column( name = "creation_date", nullable = false, insertable = false, updatable = false  )
	private Date creationDate;
	
	@Basic( optional = false )
	@Column( name = "last_modified_date", nullable = false, insertable = false, updatable = false  )
	private Date lastModifiedDate;
	
	@Column( name = "last_modified_user", nullable = false  )
	private Long lastModifiedUser;
	
	@Column( nullable = false  )
	private Boolean deleted;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

	public Long getNonconformityId() {
		return nonconformityId;
	}

	public void setNonconformityId(Long nonconformityId) {
		this.nonconformityId = nonconformityId;
	}

	public SurveillanceNonconformityEntity getNonconformityEntity() {
		return nonconformityEntity;
	}

	public void setNonconformityEntity(SurveillanceNonconformityEntity nonconformityEntity) {
		this.nonconformityEntity = nonconformityEntity;
	}
}

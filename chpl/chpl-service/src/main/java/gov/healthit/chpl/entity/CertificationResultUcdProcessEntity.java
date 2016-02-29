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


@Entity
@Table(name = "certification_result_ucd_process")
public class CertificationResultUcdProcessEntity {
	
	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Basic( optional = false )
	@Column(name = "certification_result_ucd_process_id")
	private Long id;
	
	@Basic( optional = false )
	@Column( name = "certification_result_id", nullable = false  )
	private Long certificationResultId;
	
	@Column(name = "ucd_process_id")
	private Long ucdProcessId;
	
	@Column(name = "ucd_process_details")
	private String ucdProcessDetails;
	
	@Basic( optional = true )
	@OneToOne(optional = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "ucd_process_id", unique=true, nullable = true, insertable=false, updatable= false)
	private UcdProcessEntity ucdProcess;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getCertificationResultId() {
		return certificationResultId;
	}

	public void setCertificationResultId(Long certificationResultId) {
		this.certificationResultId = certificationResultId;
	}
	
	@Basic( optional = false )
	@Column( name = "creation_date", nullable = false  )
	protected Date creationDate;
	
	@Basic( optional = false )
	@Column( nullable = false  )
	protected Boolean deleted;
	
	@Basic( optional = false )
	@Column( name = "last_modified_date", nullable = false  )
	protected Date lastModifiedDate;
	
	@Basic( optional = false )
	@Column( name = "last_modified_user", nullable = false  )
	protected Long lastModifiedUser;
	
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

	public Long getUcdProcessId() {
		return ucdProcessId;
	}

	public void setUcdProcessId(Long ucdProcessId) {
		this.ucdProcessId = ucdProcessId;
	}

	public String getUcdProcessDetails() {
		return ucdProcessDetails;
	}

	public void setUcdProcessDetails(String ucdProcessDetails) {
		this.ucdProcessDetails = ucdProcessDetails;
	}

	public UcdProcessEntity getUcdProcess() {
		return ucdProcess;
	}

	public void setUcdProcess(UcdProcessEntity ucdProcess) {
		this.ucdProcess = ucdProcess;
	}
}

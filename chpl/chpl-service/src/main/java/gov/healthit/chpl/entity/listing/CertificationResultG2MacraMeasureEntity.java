package gov.healthit.chpl.entity.listing;

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

import gov.healthit.chpl.entity.MacraMeasureEntity;


@Entity
@Table(name = "certification_result_g2_macra")
public class CertificationResultG2MacraMeasureEntity {
	
	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column( name = "id", nullable = false  )
	private Long id;

	@Column(name = "certification_result_id", nullable = false )	
	private Long certificationResultId;

	@Column(name = "macra_id")
	private Long macraId;
	
	@OneToOne(optional = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "macra_id", insertable = false, updatable = false)
	private MacraMeasureEntity macraMeasure;
	
	@Basic( optional = false )
	@Column( name = "last_modified_date", nullable = false, insertable = false, updatable = false  )
	private Date lastModifiedDate;
	
	@Basic( optional = false )
	@Column( name = "last_modified_user", nullable = false  )
	private Long lastModifiedUser;
	
	@Basic( optional = false )
	@Column( name = "creation_date", nullable = false, insertable = false, updatable = false  )
	private Date creationDate;
	
	@Basic( optional = false )
	@Column( name = "deleted", nullable = false  )
	private Boolean deleted;
	
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
	public Long getMacraId() {
		return macraId;
	}
	public void setMacraId(Long macraId) {
		this.macraId = macraId;
	}
	public MacraMeasureEntity getMacraMeasure() {
		return macraMeasure;
	}
	public void setMacraMeasure(MacraMeasureEntity macraMeasure) {
		this.macraMeasure = macraMeasure;
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

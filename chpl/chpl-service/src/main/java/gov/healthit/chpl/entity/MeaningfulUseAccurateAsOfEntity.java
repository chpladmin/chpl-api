package gov.healthit.chpl.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="muu_accurate_as_of_date")
public class MeaningfulUseAccurateAsOfEntity implements Cloneable, Serializable {
	private static final long serialVersionUID = -1463562876433962214L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Basic(optional = false)
	@Column(name = "muu_accurate_as_of_date_id", nullable = false)
	private Long id;

	@Basic(optional = false)
	@Column(name = "accurate_as_of_date", updatable = true, nullable = false)
	private Date accurateAsOfDate;

	@Column(name = "deleted")
	private Boolean deleted;

	@Column(name = "last_modified_user")
	private Long lastModifiedUser;

	@Column(name = "creation_date", insertable = false, updatable = false  )
	private Date creationDate;

	@Column(name = "last_modified_date", insertable = false, updatable = false )
	private Date lastModifiedDate;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getAccurateAsOfDate() {
		return accurateAsOfDate;
	}

	public void setAccurateAsOfDate(Date accurateAsOfDate) {
		this.accurateAsOfDate = accurateAsOfDate;
	}

	public Boolean getDeleted() {
		return deleted;
	}

	public void setDeleted(Boolean deleted) {
		this.deleted = deleted;
	}

	public Long getLastModifiedUser() {
		return lastModifiedUser;
	}

	public void setLastModifiedUser(Long lastModifiedUser) {
		this.lastModifiedUser = lastModifiedUser;
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
}

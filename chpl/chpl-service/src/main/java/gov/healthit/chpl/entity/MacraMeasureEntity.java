package gov.healthit.chpl.entity;

import java.util.Date;

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
@Table(name = "macra_criteria_map")
public class MacraMeasureEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "criteria_id")
	private Long certificationCriterionId;

	@OneToOne(optional = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "criteria_id", insertable = false, updatable = false)
	private CertificationCriterionEntity certificationCriterion;

	@Column(name = "value")
	private String value;

	@Column(name = "name")
	private String name;

	@Column(name = "description")
	private String description;

	@Column( name = "creation_date", nullable = false, updatable = false, insertable = false  )
	protected Date creationDate;

	@Column( nullable = false  )
	protected Boolean deleted;

	@Column( name = "last_modified_date", nullable = false, updatable = false, insertable = false  )
	protected Date lastModifiedDate;

	@Column( name = "last_modified_user", nullable = false  )
	protected Long lastModifiedUser;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

	public Long getCertificationCriterionId() {
		return certificationCriterionId;
	}

	public void setCertificationCriterionId(Long certificationCriterionId) {
		this.certificationCriterionId = certificationCriterionId;
	}

	public CertificationCriterionEntity getCertificationCriterion() {
		return certificationCriterion;
	}

	public void setCertificationCriterion(CertificationCriterionEntity certificationCriterion) {
		this.certificationCriterion = certificationCriterion;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}

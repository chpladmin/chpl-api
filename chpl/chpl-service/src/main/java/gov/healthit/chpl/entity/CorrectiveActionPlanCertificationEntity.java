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
@Table(name="corrective_action_plan_certification_result")
public class CorrectiveActionPlanCertificationEntity {
	
	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Basic( optional = false )
	@Column( name = "corrective_action_plan_certification_result_id", nullable = false  )
	private Long id;
	
	@Basic( optional = false )
	@OneToOne(optional = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "corrective_action_plan_id", unique=true, nullable = true)
	private CorrectiveActionPlanEntity correctiveActionPlan;
	
	@Basic( optional = false )
	@OneToOne(optional = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "certification_criterion_id", unique=true, nullable = true)
	private CertificationCriterionEntity certificationCriterion;
	
	@Basic( optional = false )
	@Column(name = "acb_summary")
	private String acbSummary;
	
	@Basic( optional = false )
	@Column(name = "developer_summary")
	private String developerSummaryDescription;
	
	@Column(name = "resolution")
	private String resolution;
	
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

	public CertificationCriterionEntity getCertificationCriterion() {
		return certificationCriterion;
	}

	public void setCertificationCriterion(CertificationCriterionEntity certificationCriterion) {
		this.certificationCriterion = certificationCriterion;
	}

	public String getAcbSummary() {
		return acbSummary;
	}

	public void setAcbSummary(String acbSummary) {
		this.acbSummary = acbSummary;
	}

	public String getDeveloperSummaryDescription() {
		return developerSummaryDescription;
	}

	public void setDeveloperSummaryDescription(String developerSummaryDescription) {
		this.developerSummaryDescription = developerSummaryDescription;
	}

	public String getResolution() {
		return resolution;
	}

	public void setResolution(String resolution) {
		this.resolution = resolution;
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
}

package gov.healthit.chpl.entity;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name="corrective_action_plan")
public class CorrectiveActionPlanEntity {
	
	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Basic( optional = false )
	@Column( name = "corrective_action_plan_id", nullable = false  )
	private Long id;
	
	@Basic( optional = false )
	@Column(name="certified_product_id")
	private Long certifiedProductId;
	
	@Basic( optional = false )
	@Column(name="acb_summary")
	private String acbSummaryDescription;
	
	@Basic( optional = false )
	@Column(name = "developer_summary")
	private String developerSummaryDescription;
	
	@Basic( optional = false )
	@Column(name = "approval_date")
	private Date approvalDate;
	
	@Basic( optional = false )
	@Column(name = "effective_date")
	private Date effectiveDate;
	
	@Basic( optional = false )
	@Column(name = "completion_date_estimated")
	private Date estimatedCompletionDate;
	
	@Column(name = "completion_date_actual")
	private Date actualCompletionDate;
	
	@Basic( optional = false )
	@Column(name = "noncompliance_determination_date")
	private Date noncomplainceDeterminationDate;
	
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

	public Long getCertifiedProductId() {
		return certifiedProductId;
	}

	public void setCertifiedProductId(Long certifiedProductId) {
		this.certifiedProductId = certifiedProductId;
	}

	public String getAcbSummaryDescription() {
		return acbSummaryDescription;
	}

	public void setAcbSummaryDescription(String acbSummaryDescription) {
		this.acbSummaryDescription = acbSummaryDescription;
	}

	public String getDeveloperSummaryDescription() {
		return developerSummaryDescription;
	}

	public void setDeveloperSummaryDescription(String developerSummaryDescription) {
		this.developerSummaryDescription = developerSummaryDescription;
	}

	public Date getApprovalDate() {
		return approvalDate;
	}

	public void setApprovalDate(Date approvalDate) {
		this.approvalDate = approvalDate;
	}

	public Date getEffectiveDate() {
		return effectiveDate;
	}

	public void setEffectiveDate(Date effectiveDate) {
		this.effectiveDate = effectiveDate;
	}

	public Date getEstimatedCompletionDate() {
		return estimatedCompletionDate;
	}

	public void setEstimatedCompletionDate(Date estimatedCompletionDate) {
		this.estimatedCompletionDate = estimatedCompletionDate;
	}

	public Date getActualCompletionDate() {
		return actualCompletionDate;
	}

	public void setActualCompletionDate(Date actualCompletionDate) {
		this.actualCompletionDate = actualCompletionDate;
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

	public Date getNoncomplainceDeterminationDate() {
		return noncomplainceDeterminationDate;
	}

	public void setNoncomplainceDeterminationDate(Date noncomplainceDeterminationDate) {
		this.noncomplainceDeterminationDate = noncomplainceDeterminationDate;
	}

}

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
	@Column(name="surveillance_start")
	private Date surveillanceStartDate;
	
	@Column(name="surveillance_end")
	private Date surveillanceEndDate;
	
	@Basic( optional = false )
	@Column(name = "surveillance_result")
	private Boolean surveillanceResult;
	
	@Basic( optional = false )
	@Column(name = "noncompliance_determination_date")
	private Date nonComplianceDeterminationDate;
	
	@Column(name = "approval_date")
	private Date approvalDate;
	
	@Column(name = "start_date")
	private Date startDate;
	
	@Column(name = "completion_date_required")
	private Date requiredCompletionDate;
	
	@Column(name = "completion_date_actual")
	private Date actualCompletionDate;
	
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

	public Date getApprovalDate() {
		return approvalDate;
	}

	public void setApprovalDate(Date approvalDate) {
		this.approvalDate = approvalDate;
	}
	
	public Date getActualCompletionDate() {
		return actualCompletionDate;
	}

	public void setActualCompletionDate(Date actualCompletionDate) {
		this.actualCompletionDate = actualCompletionDate;
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

	public Date getSurveillanceStartDate() {
		return surveillanceStartDate;
	}

	public void setSurveillanceStartDate(Date surveillanceStartDate) {
		this.surveillanceStartDate = surveillanceStartDate;
	}

	public Date getSurveillanceEndDate() {
		return surveillanceEndDate;
	}

	public void setSurveillanceEndDate(Date surveillanceEndDate) {
		this.surveillanceEndDate = surveillanceEndDate;
	}

	public Boolean getSurveillanceResult() {
		return surveillanceResult;
	}

	public void setSurveillanceResult(Boolean surveillanceResult) {
		this.surveillanceResult = surveillanceResult;
	}

	public Date getNonComplianceDeterminationDate() {
		return nonComplianceDeterminationDate;
	}

	public void setNonComplianceDeterminationDate(Date nonComplianceDeterminationDate) {
		this.nonComplianceDeterminationDate = nonComplianceDeterminationDate;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getRequiredCompletionDate() {
		return requiredCompletionDate;
	}

	public void setRequiredCompletionDate(Date requiredCompletionDate) {
		this.requiredCompletionDate = requiredCompletionDate;
	}
}

package gov.healthit.chpl.entity.surveillance;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Where;

import gov.healthit.chpl.entity.CertificationCriterionEntity;
import gov.healthit.chpl.entity.NonconformityStatusEntity;


@Entity
@Table(name = "surveillance_nonconformity")
public class SurveillanceNonconformityEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "surveillance_requirement_id")
	private Long surveillanceRequirementId;

	@Column(name = "certification_criterion_id")
	private Long certificationCriterionId;

	@OneToOne(optional = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "certification_criterion_id", insertable = false, updatable = false)
	private CertificationCriterionEntity certificationCriterionEntity;

	@Column(name = "nonconformity_type")
	private String type;

	@Column(name = "nonconformity_status_id")
	private Long nonconformityStatusId;

	@OneToOne(optional = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "nonconformity_status_id", insertable = false, updatable = false)
	private NonconformityStatusEntity nonconformityStatus;

	@Column(name = "date_of_determination")
	private Date dateOfDetermination;

	@Column(name = "corrective_action_plan_approval_date")
	private Date capApproval;

	@Column(name = "corrective_action_start_date")
	private Date capStart;

	@Column(name = "corrective_action_must_complete_date")
	private Date capMustCompleteDate;

	@Column(name = "corrective_action_end_date")
	private Date capEndDate;

	@Column(name = "summary")
	private String summary;

	@Column(name = "findings")
	private String findings;

	@Column(name = "sites_passed")
	private Integer sitesPassed;

	@Column(name = "total_sites")
	private Integer totalSites;

	@Column(name = "developer_explanation")
	private String developerExplanation;

	@Column(name = "resolution")
	private String resolution;

	@Column( name = "deleted")
	private Boolean deleted;

	@Column( name = "last_modified_user")
	private Long lastModifiedUser;

	@Column( name = "creation_date", insertable = false, updatable = false  )
	private Date creationDate;

	@Column( name = "last_modified_date", insertable = false, updatable = false )
	private Date lastModifiedDate;

	@OneToMany( fetch = FetchType.LAZY, mappedBy = "nonconformityId"  )
	@Basic( optional = false )
	@Column( name = "surveillance_nonconformity_id", nullable = false  )
	@Where(clause="deleted <> 'true'")
	private Set<SurveillanceNonconformityDocumentationEntity> documents = new HashSet<SurveillanceNonconformityDocumentationEntity>();

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Date getDateOfDetermination() {
		return dateOfDetermination;
	}

	public void setDateOfDetermination(Date dateOfDetermination) {
		this.dateOfDetermination = dateOfDetermination;
	}

	public Date getCapApproval() {
		return capApproval;
	}

	public void setCapApproval(Date capApproval) {
		this.capApproval = capApproval;
	}

	public Date getCapStart() {
		return capStart;
	}

	public void setCapStart(Date capStart) {
		this.capStart = capStart;
	}

	public Date getCapMustCompleteDate() {
		return capMustCompleteDate;
	}

	public void setCapMustCompleteDate(Date capMustCompleteDate) {
		this.capMustCompleteDate = capMustCompleteDate;
	}

	public Date getCapEndDate() {
		return capEndDate;
	}

	public void setCapEndDate(Date capEndDate) {
		this.capEndDate = capEndDate;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getFindings() {
		return findings;
	}

	public void setFindings(String findings) {
		this.findings = findings;
	}

	public Integer getSitesPassed() {
		return sitesPassed;
	}

	public void setSitesPassed(Integer sitesPassed) {
		this.sitesPassed = sitesPassed;
	}

	public Integer getTotalSites() {
		return totalSites;
	}

	public void setTotalSites(Integer totalSites) {
		this.totalSites = totalSites;
	}

	public String getDeveloperExplanation() {
		return developerExplanation;
	}

	public void setDeveloperExplanation(String developerExplanation) {
		this.developerExplanation = developerExplanation;
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

	public Long getSurveillanceRequirementId() {
		return surveillanceRequirementId;
	}

	public void setSurveillanceRequirementId(Long surveillanceRequirementId) {
		this.surveillanceRequirementId = surveillanceRequirementId;
	}

	public Long getCertificationCriterionId() {
		return certificationCriterionId;
	}

	public void setCertificationCriterionId(Long certificationCriterionId) {
		this.certificationCriterionId = certificationCriterionId;
	}

	public CertificationCriterionEntity getCertificationCriterionEntity() {
		return certificationCriterionEntity;
	}

	public void setCertificationCriterionEntity(CertificationCriterionEntity certificationCriterionEntity) {
		this.certificationCriterionEntity = certificationCriterionEntity;
	}

	public Long getNonconformityStatusId() {
		return nonconformityStatusId;
	}

	public void setNonconformityStatusId(Long nonconformityStatusId) {
		this.nonconformityStatusId = nonconformityStatusId;
	}

	public NonconformityStatusEntity getNonconformityStatus() {
		return nonconformityStatus;
	}

	public void setNonconformityStatus(NonconformityStatusEntity nonconformityStatus) {
		this.nonconformityStatus = nonconformityStatus;
	}

	public Set<SurveillanceNonconformityDocumentationEntity> getDocuments() {
		return documents;
	}

	public void setDocuments(Set<SurveillanceNonconformityDocumentationEntity> documents) {
		this.documents = documents;
	}
}

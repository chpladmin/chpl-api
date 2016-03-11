package gov.healthit.chpl.entity;

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
import javax.persistence.Transient;

@Entity
@Table(name="pending_certification_result")
public class PendingCertificationResultEntity {
	
	@Transient 
	private boolean hasAdditionalSoftware;
	
	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column( name = "pending_certification_result_id", nullable = false  )
	private Long id;
	
	@Basic( optional = true )
	@OneToOne(optional = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "certification_criterion_id", unique=true, nullable = true)
	private CertificationCriterionEntity mappedCriterion;
	
	@Basic( optional = false )
	@Column(name = "pending_certified_product_id", nullable = false )	
	private Long pendingCertifiedProductId;

	@Column(name = "meets_criteria")
	private Boolean meetsCriteria;

	@Column(name = "gap")
	private Boolean gap;
	
	@Column(name = "sed")
	private Boolean sed;
	
	@Column(name = "g1_success")
	private Boolean g1Success;
	
	@Column(name = "g2_success")
	private Boolean g2Success;
	
	@Basic( optional = false )
	@Column( name = "last_modified_date", nullable = false  )
	private Date lastModifiedDate;
	
	@Basic( optional = false )
	@Column( name = "last_modified_user", nullable = false  )
	private Long lastModifiedUser;
	
	@Basic( optional = false )
	@Column( name = "creation_date", nullable = false  )
	private Date creationDate;
	
	@Basic( optional = false )
	@Column( name = "deleted", nullable = false  )
	private Boolean deleted;

	@OneToMany(fetch = FetchType.LAZY, mappedBy="pendingCertificationResultId")
	@Basic( optional = false )
	@Column( name = "pending_certification_result_id", nullable = false  )
	private Set<PendingCertificationResultUcdProcessEntity> ucdProcesses;
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy="pendingCertificationResultId")
	@Basic( optional = false )
	@Column( name = "pending_certification_result_id", nullable = false  )
	private Set<PendingCertificationResultTestStandardEntity> testStandards;
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy="pendingCertificationResultId")
	@Basic( optional = false )
	@Column( name = "pending_certification_result_id", nullable = false  )
	private Set<PendingCertificationResultTestFunctionalityEntity> testFunctionality;
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy="pendingCertificationResultId")
	@Basic( optional = false )
	@Column( name = "pending_certification_result_id", nullable = false  )
	private Set<PendingCertificationResultAdditionalSoftwareEntity> additionalSoftware;
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy="pendingCertificationResultId")
	@Basic( optional = false )
	@Column( name = "pending_certification_result_id", nullable = false  )
	private Set<PendingCertificationResultTestProcedureEntity> testProcedures;
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy="pendingCertificationResultId")
	@Basic( optional = false )
	@Column( name = "pending_certification_result_id", nullable = false  )
	private Set<PendingCertificationResultTestDataEntity> testData;
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy="pendingCertificationResultId")
	@Basic( optional = false )
	@Column( name = "pending_certification_result_id", nullable = false  )
	private Set<PendingCertificationResultTestToolEntity> testTools;
	
	
	public PendingCertificationResultEntity() {
		ucdProcesses = new HashSet<PendingCertificationResultUcdProcessEntity>();
		testStandards = new HashSet<PendingCertificationResultTestStandardEntity>();
		testFunctionality = new HashSet<PendingCertificationResultTestFunctionalityEntity>();
		additionalSoftware = new HashSet<PendingCertificationResultAdditionalSoftwareEntity>();
		testProcedures = new HashSet<PendingCertificationResultTestProcedureEntity>();
		testData = new HashSet<PendingCertificationResultTestDataEntity>();
		testTools = new HashSet<PendingCertificationResultTestToolEntity>();
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getPendingCertifiedProductId() {
		return pendingCertifiedProductId;
	}

	public void setPendingCertifiedProductId(Long pendingCertifiedProductId) {
		this.pendingCertifiedProductId = pendingCertifiedProductId;
	}

	public Boolean getMeetsCriteria() {
		return meetsCriteria;
	}

	public void setMeetsCriteria(Boolean meetsCriteria) {
		this.meetsCriteria = meetsCriteria;
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

	public CertificationCriterionEntity getMappedCriterion() {
		return mappedCriterion;
	}

	public void setMappedCriterion(CertificationCriterionEntity mappedCriterion) {
		this.mappedCriterion = mappedCriterion;
	}

	public Boolean getGap() {
		return gap;
	}

	public void setGap(Boolean gap) {
		this.gap = gap;
	}

	public Boolean getSed() {
		return sed;
	}

	public void setSed(Boolean sed) {
		this.sed = sed;
	}

	public Boolean getG1Success() {
		return g1Success;
	}

	public void setG1Success(Boolean g1Success) {
		this.g1Success = g1Success;
	}

	public Boolean getG2Success() {
		return g2Success;
	}

	public void setG2Success(Boolean g2Success) {
		this.g2Success = g2Success;
	}

	public Set<PendingCertificationResultTestStandardEntity> getTestStandards() {
		return testStandards;
	}

	public void setTestStandards(Set<PendingCertificationResultTestStandardEntity> testStandards) {
		this.testStandards = testStandards;
	}

	public Set<PendingCertificationResultTestFunctionalityEntity> getTestFunctionality() {
		return testFunctionality;
	}

	public void setTestFunctionality(Set<PendingCertificationResultTestFunctionalityEntity> testFunctionality) {
		this.testFunctionality = testFunctionality;
	}

	public Set<PendingCertificationResultAdditionalSoftwareEntity> getAdditionalSoftware() {
		return additionalSoftware;
	}

	public void setAdditionalSoftware(Set<PendingCertificationResultAdditionalSoftwareEntity> additionalSoftware) {
		this.additionalSoftware = additionalSoftware;
	}

	public Set<PendingCertificationResultTestProcedureEntity> getTestProcedures() {
		return testProcedures;
	}

	public void setTestProcedures(Set<PendingCertificationResultTestProcedureEntity> testProcedures) {
		this.testProcedures = testProcedures;
	}

	public Set<PendingCertificationResultTestDataEntity> getTestData() {
		return testData;
	}

	public void setTestData(Set<PendingCertificationResultTestDataEntity> testData) {
		this.testData = testData;
	}

	public Set<PendingCertificationResultTestToolEntity> getTestTools() {
		return testTools;
	}

	public void setTestTools(Set<PendingCertificationResultTestToolEntity> testTools) {
		this.testTools = testTools;
	}

	public Set<PendingCertificationResultUcdProcessEntity> getUcdProcesses() {
		return ucdProcesses;
	}

	public void setUcdProcesses(Set<PendingCertificationResultUcdProcessEntity> ucdProcesses) {
		this.ucdProcesses = ucdProcesses;
	}

	public boolean isHasAdditionalSoftware() {
		return hasAdditionalSoftware;
	}

	public void setHasAdditionalSoftware(boolean hasAdditionalSoftware) {
		this.hasAdditionalSoftware = hasAdditionalSoftware;
	}

}

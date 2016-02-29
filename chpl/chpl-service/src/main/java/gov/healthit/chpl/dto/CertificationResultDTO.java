package gov.healthit.chpl.dto;

import gov.healthit.chpl.entity.CertificationResultEntity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CertificationResultDTO {
	
	private Long id;
	private Long certificationCriterionId;
	private Long certifiedProductId;
	private Date creationDate;
	private Boolean deleted;
	private Boolean gap;
	private Boolean sed;
	private Boolean successful;
	private Boolean g1Success;
	private Boolean g2Success;
	private Date lastModifiedDate;
	private Long lastModifiedUser;
	
	private List<CertificationResultUcdProcessDTO> ucdProcesses;
	private List<CertificationResultTestFunctionalityDTO> testFunctionality;
	private List<CertificationResultTestProcedureDTO> testProcedures;
 	private List<CertificationResultTestDataDTO> testData;
	private List<CertificationResultTestToolDTO> testTools;
	private List<CertificationResultTestStandardDTO> testStandards;
	private List<CertificationResultAdditionalSoftwareDTO> additionalSoftware;
	
	public CertificationResultDTO(){
		ucdProcesses = new ArrayList<CertificationResultUcdProcessDTO>();
		additionalSoftware = new ArrayList<CertificationResultAdditionalSoftwareDTO>();
		testStandards = new ArrayList<CertificationResultTestStandardDTO>();
		testTools = new ArrayList<CertificationResultTestToolDTO>();
		testData = new ArrayList<CertificationResultTestDataDTO>();
		testProcedures = new ArrayList<CertificationResultTestProcedureDTO>();
		testFunctionality = new ArrayList<CertificationResultTestFunctionalityDTO>();
	}
	
	public List<CertificationResultTestProcedureDTO> getTestProcedures() {
		return testProcedures;
	}

	public void setTestProcedures(List<CertificationResultTestProcedureDTO> testProcedures) {
		this.testProcedures = testProcedures;
	}

	public CertificationResultDTO(CertificationResultEntity entity){
		this();
		this.id = entity.getId();
		this.certificationCriterionId = entity.getCertificationCriterionId();
		this.certifiedProductId = entity.getCertifiedProductId();
		this.creationDate = entity.getCreationDate();
		this.gap = entity.isGap();
		this.sed = entity.getSed();
		this.g1Success = entity.getG1Success();
		this.g2Success = entity.getG2Success();
		this.successful = entity.isSuccess();
		this.deleted = entity.getDeleted();
		this.lastModifiedDate = entity.getLastModifiedDate();
		this.lastModifiedUser = entity.getLastModifiedUser();
	}
	
	public Long getCertificationCriterionId() {
		return certificationCriterionId;
	}
	public void setCertificationCriterionId(Long certificationCriterionId) {
		this.certificationCriterionId = certificationCriterionId;
	}
	public Long getCertifiedProductId() {
		return certifiedProductId;
	}
	public void setCertifiedProduct(Long certifiedProductId) {
		this.certifiedProductId = certifiedProductId;
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
	public Boolean getGap() {
		return gap;
	}
	public void setGap(Boolean gap) {
		this.gap = gap;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
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
	public Boolean getSuccessful() {
		return successful;
	}
	public void setSuccessful(Boolean successful) {
		this.successful = successful;
	}

	public List<CertificationResultAdditionalSoftwareDTO> getAdditionalSoftware() {
		return additionalSoftware;
	}

	public void setAdditionalSoftware(List<CertificationResultAdditionalSoftwareDTO> list) {
		this.additionalSoftware = list;
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

	public void setCertifiedProductId(Long certifiedProductId) {
		this.certifiedProductId = certifiedProductId;
	}

	public List<CertificationResultTestStandardDTO> getTestStandards() {
		return testStandards;
	}

	public void setTestStandards(List<CertificationResultTestStandardDTO> testStandards) {
		this.testStandards = testStandards;
	}

	public List<CertificationResultTestToolDTO> getTestTools() {
		return testTools;
	}

	public void setTestTools(List<CertificationResultTestToolDTO> testTools) {
		this.testTools = testTools;
	}

	public List<CertificationResultTestDataDTO> getTestData() {
		return testData;
	}

	public void setTestData(List<CertificationResultTestDataDTO> testData) {
		this.testData = testData;
	}

	public List<CertificationResultTestFunctionalityDTO> getTestFunctionality() {
		return testFunctionality;
	}

	public void setTestFunctionality(List<CertificationResultTestFunctionalityDTO> testFunctionality) {
		this.testFunctionality = testFunctionality;
	}

	public List<CertificationResultUcdProcessDTO> getUcdProcesses() {
		return ucdProcesses;
	}

	public void setUcdProcesses(List<CertificationResultUcdProcessDTO> ucdProcesses) {
		this.ucdProcesses = ucdProcesses;
	}
	
}

package gov.healthit.chpl.dto;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.util.StringUtils;

import gov.healthit.chpl.domain.CQMResultCertification;
import gov.healthit.chpl.domain.CQMResultDetails;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultAdditionalSoftware;
import gov.healthit.chpl.domain.CertificationResultTestData;
import gov.healthit.chpl.domain.CertificationResultTestFunctionality;
import gov.healthit.chpl.domain.CertificationResultTestParticipant;
import gov.healthit.chpl.domain.CertificationResultTestProcedure;
import gov.healthit.chpl.domain.CertificationResultTestStandard;
import gov.healthit.chpl.domain.CertificationResultTestTask;
import gov.healthit.chpl.domain.CertificationResultTestTool;
import gov.healthit.chpl.domain.CertificationResultUcdProcess;
import gov.healthit.chpl.domain.CertifiedProductAccessibilityStandard;
import gov.healthit.chpl.domain.CertifiedProductQmsStandard;
import gov.healthit.chpl.domain.CertifiedProductTargetedUser;
import gov.healthit.chpl.domain.PendingCertifiedProductDetails;
import gov.healthit.chpl.entity.PendingCertificationResultEntity;
import gov.healthit.chpl.entity.PendingCertifiedProductAccessibilityStandardEntity;
import gov.healthit.chpl.entity.PendingCertifiedProductEntity;
import gov.healthit.chpl.entity.PendingCertifiedProductQmsStandardEntity;
import gov.healthit.chpl.entity.PendingCertifiedProductTargetedUserEntity;
import gov.healthit.chpl.entity.PendingCqmCriterionEntity;

public class PendingCertifiedProductDTO implements Serializable {
	private static final long serialVersionUID = 8778880570983282001L;
	private Long id;
	private Long practiceTypeId;
    private Long developerId;
	private AddressDTO developerAddress; 
    private Long productId;
    private Long productVersionId;    
    private Long certificationEditionId;    
    private Long certificationBodyId;    
    private Long productClassificationId;
    private Long testingLabId;
    private Set<String> errorMessages;
    private Set<String> warningMessages;
    
    /**
    * fields directly from the spreadsheet
    **/
    private String uniqueId;    
    private String recordStatus;    
    private String practiceType; 
    private String testingLabName;
    private String developerName;    
    private String productName;    
    private String productVersion;    
    private String certificationEdition;    
    private String acbCertificationId;    
    private String certificationBodyName;
    private String productClassificationName;
    private Date certificationDate;
    private String developerStreetAddress;
    private String developerCity;
    private String developerState;
    private String developerZipCode;
    private String developerWebsite;
    private String developerEmail;
    private String developerContactName;
    private String developerPhoneNumber;
    private Long developerContactId;
    private String reportFileLocation;
    private String sedReportFileLocation;
    private String sedIntendedUserDescription;
    private Date sedTestingEnd;
	private Boolean ics;
	private Boolean accessibilityCertified;
	private String transparencyAttestation;
	private String transparencyAttestationUrl;
	
	private List<PendingCertificationResultDTO> certificationCriterion;
	private List<PendingCqmCriterionDTO> cqmCriterion;
	private List<PendingCertifiedProductQmsStandardDTO> qmsStandards;
	private List<PendingCertifiedProductTargetedUserDTO> targetedUsers; 
	private List<PendingCertifiedProductAccessibilityStandardDTO> accessibilityStandards;
	
	private Date uploadDate;
	
	public PendingCertifiedProductDTO(){
		this.errorMessages = new HashSet<String>();	
		this.warningMessages = new HashSet<String>();
		this.certificationCriterion = new ArrayList<PendingCertificationResultDTO>();
		this.cqmCriterion = new ArrayList<PendingCqmCriterionDTO>();
		this.qmsStandards = new ArrayList<PendingCertifiedProductQmsStandardDTO>();
		this.targetedUsers = new ArrayList<PendingCertifiedProductTargetedUserDTO>();
		this.accessibilityStandards = new ArrayList<PendingCertifiedProductAccessibilityStandardDTO>();
	}
	
	public PendingCertifiedProductDTO(PendingCertifiedProductDetails details) {
		this();
		this.id = details.getId();
		if(details.getPracticeType().get("id") != null) {
			String practiceTypeId = details.getPracticeType().get("id").toString();
			this.practiceTypeId =  new Long(practiceTypeId);
		}
		if(details.getPracticeType().get("name") != null) {
			this.practiceType = details.getPracticeType().get("name").toString();
		}
		
		if(details.getTestingLab() != null && details.getTestingLab().get("id") != null) {
			this.testingLabId = new Long(details.getTestingLab().get("id").toString());
		}
		if(details.getTestingLab() != null && details.getTestingLab().get("name") != null) {
			this.testingLabName = details.getTestingLab().get("name").toString();
		}
		
		this.developerId = details.getDeveloper().getDeveloperId();
		this.developerName = details.getDeveloper().getName();
		this.developerWebsite = details.getDeveloper().getWebsite();
		if(details.getDeveloper().getContact() != null) {
			this.developerEmail = details.getDeveloper().getContact().getEmail();
			this.developerContactName = details.getDeveloper().getContact().getLastName();
			this.developerPhoneNumber = details.getDeveloper().getContact().getPhoneNumber();
		}
		if(details.getDeveloper().getAddress() != null) {
			AddressDTO address = new AddressDTO();
			address.setId(details.getDeveloper().getAddress().getAddressId());
			address.setStreetLineOne(details.getDeveloper().getAddress().getLine1());
			address.setCity(details.getDeveloper().getAddress().getCity());
			address.setState(details.getDeveloper().getAddress().getState());
			address.setZipcode(details.getDeveloper().getAddress().getZipcode());
			address.setCountry("US");
			this.developerAddress = address;
		}
		
		if(details.getProduct() != null && details.getProduct().getProductId() != null) {
			String productId = details.getProduct().getProductId().toString();
			this.productId = new Long(productId);
		}
		if(details.getProduct() != null && !StringUtils.isEmpty(details.getProduct().getName())) {
			this.productName = details.getProduct().getName();
		}
		if(details.getVersion() != null && details.getVersion().getVersionId() != null) {
			String productVersionId = details.getVersion().getVersionId().toString();
			this.productVersionId = new Long(productVersionId);
		}
		if(details.getVersion() != null && !StringUtils.isEmpty(details.getVersion().getVersion())) {
			this.productVersion = details.getVersion().getVersion();
		}
		
		if(details.getCertificationEdition().get("id") != null) {
			String certificationEditionId = details.getCertificationEdition().get("id").toString();
			this.certificationEditionId = new Long(certificationEditionId);
		}
		if(details.getCertificationEdition().get("name") != null) {
			this.certificationEdition = details.getCertificationEdition().get("name").toString();
		}
		
		if(details.getCertifyingBody().get("id") != null) {
			String certificationBodyId = details.getCertifyingBody().get("id").toString();
			this.certificationBodyId = new Long(certificationBodyId);
		}
		if(details.getCertifyingBody().get("name") != null) {
			this.certificationBodyName = details.getCertifyingBody().get("name").toString();
		}
		
		if(details.getClassificationType().get("id") != null) {
			String classificationTypeId = details.getClassificationType().get("id").toString();
			this.productClassificationId = new Long(classificationTypeId);
		}
		if(details.getClassificationType().get("name") != null) {
			this.productClassificationName = details.getClassificationType().get("name").toString();
		}

		if(details.getCertificationDate() != null) {
			this.certificationDate = new Date(details.getCertificationDate());
		}
		
		this.uniqueId = details.getChplProductNumber();
		this.recordStatus = details.getRecordStatus();
		this.acbCertificationId = details.getAcbCertificationId(); 
		this.reportFileLocation = details.getReportFileLocation();
		this.sedReportFileLocation = details.getSedReportFileLocation();
		this.sedIntendedUserDescription = details.getSedIntendedUserDescription();
		this.sedTestingEnd = details.getSedTestingEnd();
		this.ics = details.getIcs();
		this.accessibilityCertified = details.getAccessibilityCertified();
		this.transparencyAttestation = details.getTransparencyAttestation();
		this.transparencyAttestationUrl = details.getTransparencyAttestationUrl();
		this.accessibilityCertified = details.getAccessibilityCertified();
		
		List<CertifiedProductQmsStandard> qmsStandards = details.getQmsStandards();
		if(qmsStandards != null && qmsStandards.size() > 0) {
			for(CertifiedProductQmsStandard qms : qmsStandards) {
				PendingCertifiedProductQmsStandardDTO qmsDto = new PendingCertifiedProductQmsStandardDTO();
				qmsDto.setApplicableCriteria(qms.getApplicableCriteria());
				qmsDto.setModification(qms.getQmsModification());
				qmsDto.setQmsStandardId(qms.getQmsStandardId());
				qmsDto.setName(qms.getQmsStandardName());
				this.qmsStandards.add(qmsDto);
			}
		}
		
		List<CertifiedProductTargetedUser> targetedUsers = details.getTargetedUsers();
		if(targetedUsers != null && targetedUsers.size() > 0) {
			for(CertifiedProductTargetedUser tu : targetedUsers) {
				PendingCertifiedProductTargetedUserDTO tuDto = new PendingCertifiedProductTargetedUserDTO();
				tuDto.setTargetedUserId(tu.getTargetedUserId());
				tuDto.setName(tu.getTargetedUserName());
				this.targetedUsers.add(tuDto);
			}
		}
		
		List<CertifiedProductAccessibilityStandard> accStds = details.getAccessibilityStandards();
		if(accStds != null && accStds.size() > 0) {
			for(CertifiedProductAccessibilityStandard as : accStds) {
				PendingCertifiedProductAccessibilityStandardDTO asDto = new PendingCertifiedProductAccessibilityStandardDTO();
				asDto.setAccessibilityStandardId(as.getAccessibilityStandardId());
				asDto.setName(as.getAccessibilityStandardName());
				this.accessibilityStandards.add(asDto);
			}
		}
		
		List<CertificationResult> certificationResults = details.getCertificationResults();
		for(CertificationResult crResult : certificationResults) {
			PendingCertificationResultDTO certDto = new PendingCertificationResultDTO();
			certDto.setNumber(crResult.getNumber());
			certDto.setTitle(crResult.getTitle());
			certDto.setMeetsCriteria(crResult.isSuccess());
			certDto.setGap(crResult.isGap());
			certDto.setG1Success(crResult.isG1Success());
			certDto.setG2Success(crResult.isG2Success());
			certDto.setSed(crResult.isSed());
			certDto.setApiDocumentation(crResult.getApiDocumentation());
			certDto.setPrivacySecurityFramework(crResult.getPrivacySecurityFramework());
			
			if(crResult.getUcdProcesses() != null && crResult.getUcdProcesses().size() > 0) {
				for(CertificationResultUcdProcess ucd : crResult.getUcdProcesses()) {
					PendingCertificationResultUcdProcessDTO ucdDto = new PendingCertificationResultUcdProcessDTO();
					ucdDto.setUcdProcessId(ucd.getUcdProcessId());
					ucdDto.setUcdProcessDetails(ucd.getUcdProcessDetails());
					ucdDto.setUcdProcessName(ucd.getUcdProcessName());
					certDto.getUcdProcesses().add(ucdDto);
				}
			}
			
			if(crResult.getAdditionalSoftware() != null && crResult.getAdditionalSoftware().size() > 0) {
				for(CertificationResultAdditionalSoftware software : crResult.getAdditionalSoftware()) {
					PendingCertificationResultAdditionalSoftwareDTO as = new PendingCertificationResultAdditionalSoftwareDTO();
					as.setCertifiedProductId(software.getCertifiedProductId());
					as.setChplId(software.getCertifiedProductNumber());
					as.setJustification(software.getJustification());
					as.setName(software.getName());
					as.setVersion(software.getVersion());
					as.setGrouping(software.getGrouping());
					certDto.getAdditionalSoftware().add(as);
				}
			}
			
			if(crResult.getTestDataUsed() != null && crResult.getTestDataUsed().size() > 0) {
				for(CertificationResultTestData testData : crResult.getTestDataUsed()) {
					PendingCertificationResultTestDataDTO testDto = new PendingCertificationResultTestDataDTO();
					testDto.setAlteration(testData.getAlteration());
					testDto.setVersion(testData.getVersion());
					certDto.getTestData().add(testDto);
				}
			}
			
			if(crResult.getTestFunctionality() != null && crResult.getTestFunctionality().size() > 0) {
				for(CertificationResultTestFunctionality func : crResult.getTestFunctionality()) {
					PendingCertificationResultTestFunctionalityDTO funcDto = new PendingCertificationResultTestFunctionalityDTO();
					funcDto.setNumber(func.getName());
					funcDto.setTestFunctionalityId(func.getTestFunctionalityId());
					certDto.getTestFunctionality().add(funcDto);
				}
			}
			
			if(crResult.getTestProcedures() != null && crResult.getTestProcedures().size() > 0) {
				for(CertificationResultTestProcedure proc : crResult.getTestProcedures()) {
					PendingCertificationResultTestProcedureDTO procDto = new PendingCertificationResultTestProcedureDTO();
					procDto.setTestProcedureId(proc.getTestProcedureId());
					procDto.setVersion(proc.getTestProcedureVersion());
					certDto.getTestProcedures().add(procDto);
				}
			}
			
			if(crResult.getTestStandards() != null && crResult.getTestStandards().size() > 0) {
				for(CertificationResultTestStandard std : crResult.getTestStandards()) {
					PendingCertificationResultTestStandardDTO stdDto = new PendingCertificationResultTestStandardDTO();
					stdDto.setName(std.getTestStandardName());
					stdDto.setTestStandardId(std.getTestStandardId());
					certDto.getTestStandards().add(stdDto);
				}
			}
			
			if(crResult.getTestToolsUsed() != null && crResult.getTestToolsUsed().size() > 0) {
				for(CertificationResultTestTool tool : crResult.getTestToolsUsed()) {
					PendingCertificationResultTestToolDTO toolDto = new PendingCertificationResultTestToolDTO();
					toolDto.setName(tool.getTestToolName());
					toolDto.setVersion(tool.getTestToolVersion());
					toolDto.setTestToolId(tool.getTestToolId());
					certDto.getTestTools().add(toolDto);
				}
			}
			
			if(crResult.getTestTasks() != null && crResult.getTestTasks().size() > 0) {
				for(CertificationResultTestTask task : crResult.getTestTasks()) {
					PendingCertificationResultTestTaskDTO crTaskDto = new PendingCertificationResultTestTaskDTO();
					PendingTestTaskDTO taskDto = new PendingTestTaskDTO();
					taskDto.setDescription(task.getDescription());
					taskDto.setTaskErrors(task.getTaskErrors());
					taskDto.setTaskErrorsStddev(task.getTaskErrorsStddev());
					taskDto.setTaskPathDeviationObserved(task.getTaskPathDeviationObserved());
					taskDto.setTaskPathDeviationOptimal(task.getTaskPathDeviationOptimal());
					taskDto.setTaskRating(task.getTaskRating());
					taskDto.setTaskRatingScale(task.getTaskRatingScale());
					taskDto.setTaskRatingStddev(task.getTaskRatingStddev());
					taskDto.setTaskSuccessAverage(task.getTaskSuccessAverage());
					taskDto.setTaskSuccessStddev(task.getTaskSuccessStddev());
					taskDto.setTaskTimeAvg(task.getTaskTimeAvg());
					taskDto.setTaskTimeDeviationObservedAvg(task.getTaskTimeDeviationObservedAvg());
					taskDto.setTaskTimeDeviationOptimalAvg(task.getTaskTimeDeviationOptimalAvg());
					taskDto.setTaskTimeStddev(task.getTaskTimeStddev());
					taskDto.setUniqueId(task.getUniqueId());
					crTaskDto.setPendingTestTask(taskDto);
					
					for(CertificationResultTestParticipant part : task.getTestParticipants()) {
						PendingCertificationResultTestTaskParticipantDTO crPartDto = new PendingCertificationResultTestTaskParticipantDTO();
						PendingTestParticipantDTO partDto = new PendingTestParticipantDTO();
						partDto.setAssistiveTechnologyNeeds(part.getAssistiveTechnologyNeeds());
						partDto.setComputerExperienceMonths(part.getComputerExperienceMonths());
						
						partDto.setEducationTypeId(part.getEducationTypeId());
						EducationTypeDTO etDto = new EducationTypeDTO();
						etDto.setName(part.getEducationTypeName());
						etDto.setId(part.getEducationTypeId());
						
						partDto.setAgeRangeId(part.getAgeRangeId());
						AgeRangeDTO ageDto = new AgeRangeDTO();
						ageDto.setAge(part.getAgeRange());
						ageDto.setId(part.getAgeRangeId());
						partDto.setAgeRange(ageDto);
						
						partDto.setGender(part.getGender());
						partDto.setOccupation(part.getOccupation());
						partDto.setProductExperienceMonths(part.getProductExperienceMonths());
						partDto.setProfessionalExperienceMonths(part.getProfessionalExperienceMonths());
						partDto.setUniqueId(part.getUniqueId());
						crPartDto.setTestParticipant(partDto);
						crTaskDto.getTaskParticipants().add(crPartDto);
					}
					certDto.getTestTasks().add(crTaskDto);
				}
			}
			
			this.certificationCriterion.add(certDto);
		}
		
		List<CQMResultDetails> cqmResults = details.getCqmResults();
		for(CQMResultDetails cqmResult : cqmResults) {
			if(cqmResult.getSuccessVersions() != null && cqmResult.getSuccessVersions().size() > 0) {
				for(String version : cqmResult.getSuccessVersions()) {
					PendingCqmCriterionDTO cqmDto = new PendingCqmCriterionDTO();
					cqmDto.setCmsId(cqmResult.getCmsId());
					cqmDto.setNqfNumber(cqmResult.getNqfNumber());
					cqmDto.setCqmNumber(cqmResult.getNumber());
					cqmDto.setTitle(cqmResult.getTitle());
					cqmDto.setTypeId(cqmResult.getTypeId());
					cqmDto.setDomain(cqmResult.getDomain());
					cqmDto.setMeetsCriteria(Boolean.TRUE);
					cqmDto.setVersion(version);
					for(CQMResultCertification cqmCert : cqmResult.getCriteria()) {
						PendingCqmCertificationCriterionDTO pendingCqmCert = new PendingCqmCertificationCriterionDTO();
						pendingCqmCert.setCertificationCriteriaNumber(cqmCert.getCertificationNumber());
						pendingCqmCert.setCertificationId(cqmCert.getCertificationId());
						pendingCqmCert.setCqmId(cqmDto.getId());
						cqmDto.getCertifications().add(pendingCqmCert);
					}
					this.cqmCriterion.add(cqmDto);
				}
			} else {
				PendingCqmCriterionDTO cqmDto = new PendingCqmCriterionDTO();
				cqmDto.setCmsId(cqmResult.getCmsId());
				cqmDto.setNqfNumber(cqmResult.getNqfNumber());
				cqmDto.setCqmNumber(cqmResult.getNumber());
				cqmDto.setMeetsCriteria(cqmResult.isSuccess());
				cqmDto.setTitle(cqmResult.getTitle());
				cqmDto.setTypeId(cqmResult.getTypeId());
				cqmDto.setDomain(cqmResult.getDomain());
				this.cqmCriterion.add(cqmDto);	
			}
		}	
	}
	
	public PendingCertifiedProductDTO(PendingCertifiedProductEntity entity){	
		this();
		this.id = entity.getId();
		this.practiceTypeId = entity.getPracticeTypeId();
		this.testingLabId = entity.getTestingLabId();
		this.developerId = entity.getDeveloperId();
		//this.developerAddress = new AddressDTO(entity.getDeveloperAddress());
		this.productId = entity.getProductId();
		this.productVersionId = entity.getProductVersionId();
		this.certificationEditionId = entity.getCertificationEditionId();
		this.certificationBodyId = entity.getCertificationBodyId();
		this.productClassificationId = entity.getProductClassificationId();
		
		this.uniqueId = entity.getUniqueId();
		this.recordStatus = entity.getRecordStatus();
		this.practiceType = entity.getPracticeType();
		this.testingLabName = entity.getTestingLabName();
		this.developerName = entity.getDeveloperName();
		this.productName = entity.getProductName();
		this.productVersion = entity.getProductVersion();
		this.certificationEdition = entity.getCertificationEdition();
		this.acbCertificationId = entity.getAcbCertificationId();
		this.certificationBodyName = entity.getCertificationBodyName();
		this.productClassificationName = entity.getProductClassificationName();
		this.certificationDate = entity.getCertificationDate();
		this.developerStreetAddress = entity.getDeveloperStreetAddress();
		this.developerCity = entity.getDeveloperCity();
		this.developerState = entity.getDeveloperState();
		this.developerZipCode = entity.getDeveloperZipCode();
		this.developerWebsite = entity.getDeveloperWebsite();
		this.developerEmail = entity.getDeveloperEmail();
		this.developerContactName = entity.getDeveloperContactName();
		this.developerPhoneNumber = entity.getDeveloperPhoneNumber();
		this.developerContactId = entity.getDeveloperContactId();
		this.reportFileLocation = entity.getReportFileLocation();
		this.sedReportFileLocation = entity.getSedReportFileLocation();
		this.sedIntendedUserDescription = entity.getSedIntendedUserDescription();
		this.sedTestingEnd = entity.getSedTestingEnd();
		this.ics = entity.getIcs();
		this.accessibilityCertified = entity.getAccessibilityCertified();
		if(entity.getTransparencyAttestation() != null) {
			this.transparencyAttestation = entity.getTransparencyAttestation().toString();
		}
		this.transparencyAttestationUrl = entity.getTransparencyAttestationUrl();
		
		this.uploadDate = entity.getCreationDate();
		
		Set<PendingCertifiedProductQmsStandardEntity> qmsStandards = entity.getQmsStandards();
		if(qmsStandards != null && qmsStandards.size() > 0) {
			for(PendingCertifiedProductQmsStandardEntity qmsStandard : qmsStandards) {
				this.qmsStandards.add(new PendingCertifiedProductQmsStandardDTO(qmsStandard));
			}
		}
		
		Set<PendingCertifiedProductTargetedUserEntity> targetedUsers = entity.getTargetedUsers();
		if(targetedUsers != null && targetedUsers.size() > 0) {
			for(PendingCertifiedProductTargetedUserEntity tu : targetedUsers) {
				this.targetedUsers.add(new PendingCertifiedProductTargetedUserDTO(tu));
			}
		}
		
		Set<PendingCertifiedProductAccessibilityStandardEntity> accStds = entity.getAccessibilityStandards();
		if(accStds != null && accStds.size() > 0) {
			for(PendingCertifiedProductAccessibilityStandardEntity as : accStds) {
				this.accessibilityStandards.add(new PendingCertifiedProductAccessibilityStandardDTO(as));
			}
		}
		
		Set<PendingCertificationResultEntity> criterionEntities = entity.getCertificationCriterion();
		if(criterionEntities != null && criterionEntities.size() > 0) {
			for(PendingCertificationResultEntity crEntity : criterionEntities) {
				this.certificationCriterion.add(new PendingCertificationResultDTO(crEntity));
			}
		}
		Set<PendingCqmCriterionEntity> cqmEntities = entity.getCqmCriterion();
		if(cqmEntities != null && cqmEntities.size() > 0) {
			for(PendingCqmCriterionEntity cqmEntity : cqmEntities) {
				this.cqmCriterion.add(new PendingCqmCriterionDTO(cqmEntity));
			}	
		}
	}

	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	public Long getPracticeTypeId() {
		return practiceTypeId;
	}

	public void setPracticeTypeId(Long practiceTypeId) {
		this.practiceTypeId = practiceTypeId;
	}

	public Long getDeveloperId() {
		return developerId;
	}

	public void setDeveloperId(Long developerId) {
		this.developerId = developerId;
	}

	public AddressDTO getDeveloperAddress() {
		return developerAddress;
	}

	public void setDeveloperAddress(AddressDTO developerAddress) {
		this.developerAddress = developerAddress;
	}

	public Long getProductId() {
		return productId;
	}

	public void setProductId(Long productId) {
		this.productId = productId;
	}

	public Long getProductVersionId() {
		return productVersionId;
	}

	public void setProductVersionId(Long productVersionId) {
		this.productVersionId = productVersionId;
	}

	public Long getCertificationEditionId() {
		return certificationEditionId;
	}

	public void setCertificationEditionId(Long certificationEditionId) {
		this.certificationEditionId = certificationEditionId;
	}

	public Long getCertificationBodyId() {
		return certificationBodyId;
	}

	public void setCertificationBodyId(Long certificationBodyId) {
		this.certificationBodyId = certificationBodyId;
	}

	public Long getProductClassificationId() {
		return productClassificationId;
	}

	public void setProductClassificationId(Long productClassificationId) {
		this.productClassificationId = productClassificationId;
	}

	public String getUniqueId() {
		return uniqueId;
	}

	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}

	public String getRecordStatus() {
		return recordStatus;
	}

	public void setRecordStatus(String recordStatus) {
		this.recordStatus = recordStatus;
	}

	public String getPracticeType() {
		return practiceType;
	}

	public void setPracticeType(String practiceType) {
		this.practiceType = practiceType;
	}

	public String getDeveloperName() {
		return developerName;
	}

	public void setDeveloperName(String developerName) {
		this.developerName = developerName;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public String getProductVersion() {
		return productVersion;
	}

	public void setProductVersion(String productVersion) {
		this.productVersion = productVersion;
	}

	public String getCertificationEdition() {
		return certificationEdition;
	}

	public void setCertificationEdition(String certificationEdition) {
		this.certificationEdition = certificationEdition;
	}

	public String getAcbCertificationId() {
		return acbCertificationId;
	}

	public void setAcbCertificationId(String acbCertificationId) {
		this.acbCertificationId = acbCertificationId;
	}

	public String getCertificationBodyName() {
		return certificationBodyName;
	}

	public void setCertificationBodyName(String certificationBodyName) {
		this.certificationBodyName = certificationBodyName;
	}

	public String getProductClassificationName() {
		return productClassificationName;
	}

	public void setProductClassificationName(String productClassificationName) {
		this.productClassificationName = productClassificationName;
	}

	public Date getCertificationDate() {
		return certificationDate;
	}

	public void setCertificationDate(Date certificationDate) {
		this.certificationDate = certificationDate;
	}

	public String getDeveloperStreetAddress() {
		return developerStreetAddress;
	}

	public void setDeveloperStreetAddress(String developerStreetAddress) {
		this.developerStreetAddress = developerStreetAddress;
	}

	public String getDeveloperCity() {
		return developerCity;
	}

	public void setDeveloperCity(String developerCity) {
		this.developerCity = developerCity;
	}

	public String getDeveloperState() {
		return developerState;
	}

	public void setDeveloperState(String developerState) {
		this.developerState = developerState;
	}

	public String getDeveloperZipCode() {
		return developerZipCode;
	}

	public void setDeveloperZipCode(String developerZipCode) {
		this.developerZipCode = developerZipCode;
	}

	public String getDeveloperWebsite() {
		return developerWebsite;
	}

	public void setDeveloperWebsite(String developerWebsite) {
		this.developerWebsite = developerWebsite;
	}

	public String getDeveloperEmail() {
		return developerEmail;
	}

	public void setDeveloperEmail(String developerEmail) {
		this.developerEmail = developerEmail;
	}

	public String getReportFileLocation() {
		return reportFileLocation;
	}

	public void setReportFileLocation(String reportFileLocation) {
		this.reportFileLocation = reportFileLocation;
	}

	public List<PendingCertificationResultDTO> getCertificationCriterion() {
		return certificationCriterion;
	}

	public void setCertificationCriterion(List<PendingCertificationResultDTO> certificationCriterion) {
		this.certificationCriterion = certificationCriterion;
	}

	public List<PendingCqmCriterionDTO> getCqmCriterion() {
		return cqmCriterion;
	}

	public void setCqmCriterion(List<PendingCqmCriterionDTO> cqmCriterion) {
		this.cqmCriterion = cqmCriterion;
	}

	public Date getUploadDate() {
		return uploadDate;
	}

	public void setUploadDate(Date uploadDate) {
		this.uploadDate = uploadDate;
	}

	public Set<String> getErrorMessages() {
		return errorMessages;
	}

	public void setErrorMessages(Set<String> errorMessages) {
		this.errorMessages = errorMessages;
	}

	public Set<String> getWarningMessages() {
		return warningMessages;
	}

	public void setWarningMessages(Set<String> warningMessages) {
		this.warningMessages = warningMessages;
	}

	public Boolean getIcs() {
		return ics;
	}

	public void setIcs(Boolean ics) {
		this.ics = ics;
	}

	public Long getTestingLabId() {
		return testingLabId;
	}

	public void setTestingLabId(Long testingLabId) {
		this.testingLabId = testingLabId;
	}

	public String getTestingLabName() {
		return testingLabName;
	}

	public void setTestingLabName(String testingLabName) {
		this.testingLabName = testingLabName;
	}

	public String getDeveloperContactName() {
		return developerContactName;
	}

	public void setDeveloperContactName(String developerContactName) {
		this.developerContactName = developerContactName;
	}

	public String getDeveloperPhoneNumber() {
		return developerPhoneNumber;
	}

	public void setDeveloperPhoneNumber(String developerPhoneNumber) {
		this.developerPhoneNumber = developerPhoneNumber;
	}

	public String getSedReportFileLocation() {
		return sedReportFileLocation;
	}

	public void setSedReportFileLocation(String sedReportFileLocation) {
		this.sedReportFileLocation = sedReportFileLocation;
	}

	public List<PendingCertifiedProductQmsStandardDTO> getQmsStandards() {
		return qmsStandards;
	}

	public void setQmsStandards(List<PendingCertifiedProductQmsStandardDTO> qmsStandards) {
		this.qmsStandards = qmsStandards;
	}

	public String getTransparencyAttestation() {
		return transparencyAttestation;
	}

	public void setTransparencyAttestation(String transparencyAttestation) {
		this.transparencyAttestation = transparencyAttestation;
	}

	public Long getDeveloperContactId() {
		return developerContactId;
	}

	public void setDeveloperContactId(Long developerContactId) {
		this.developerContactId = developerContactId;
	}

	public String getTransparencyAttestationUrl() {
		return transparencyAttestationUrl;
	}

	public void setTransparencyAttestationUrl(String transparencyAttestationUrl) {
		this.transparencyAttestationUrl = transparencyAttestationUrl;
	}

	public List<PendingCertifiedProductTargetedUserDTO> getTargetedUsers() {
		return targetedUsers;
	}

	public void setTargetedUsers(List<PendingCertifiedProductTargetedUserDTO> targetedUsers) {
		this.targetedUsers = targetedUsers;
	}

	public Boolean getAccessibilityCertified() {
		return accessibilityCertified;
	}

	public void setAccessibilityCertified(Boolean accessibilityCertified) {
		this.accessibilityCertified = accessibilityCertified;
	}

	public List<PendingCertifiedProductAccessibilityStandardDTO> getAccessibilityStandards() {
		return accessibilityStandards;
	}

	public void setAccessibilityStandards(List<PendingCertifiedProductAccessibilityStandardDTO> accessibilityStandards) {
		this.accessibilityStandards = accessibilityStandards;
	}

	public String getSedIntendedUserDescription() {
		return sedIntendedUserDescription;
	}

	public void setSedIntendedUserDescription(String sedIntendedUserDescription) {
		this.sedIntendedUserDescription = sedIntendedUserDescription;
	}

	public Date getSedTestingEnd() {
		return sedTestingEnd;
	}

	public void setSedTestingEnd(Date sedTestingEnd) {
		this.sedTestingEnd = sedTestingEnd;
	}
}

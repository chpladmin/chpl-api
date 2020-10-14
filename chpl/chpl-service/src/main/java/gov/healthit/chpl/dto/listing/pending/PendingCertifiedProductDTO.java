package gov.healthit.chpl.dto.listing.pending;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.domain.CQMResultCertification;
import gov.healthit.chpl.domain.CQMResultDetails;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultAdditionalSoftware;
import gov.healthit.chpl.domain.CertificationResultTestData;
import gov.healthit.chpl.domain.CertificationResultTestFunctionality;
import gov.healthit.chpl.domain.CertificationResultTestProcedure;
import gov.healthit.chpl.domain.CertificationResultTestStandard;
import gov.healthit.chpl.domain.CertificationResultTestTool;
import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.CertifiedProductAccessibilityStandard;
import gov.healthit.chpl.domain.CertifiedProductQmsStandard;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.CertifiedProductTargetedUser;
import gov.healthit.chpl.domain.CertifiedProductTestingLab;
import gov.healthit.chpl.domain.PendingCertifiedProductDetails;
import gov.healthit.chpl.domain.TestParticipant;
import gov.healthit.chpl.domain.TestTask;
import gov.healthit.chpl.domain.UcdProcess;
import gov.healthit.chpl.dto.AddressDTO;
import gov.healthit.chpl.dto.AgeRangeDTO;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.dto.EducationTypeDTO;
import gov.healthit.chpl.dto.TestDataDTO;
import gov.healthit.chpl.dto.TestProcedureDTO;
import gov.healthit.chpl.dto.TransparencyAttestationDTO;
import gov.healthit.chpl.entity.listing.pending.PendingCertificationResultEntity;
import gov.healthit.chpl.entity.listing.pending.PendingCertifiedProductAccessibilityStandardEntity;
import gov.healthit.chpl.entity.listing.pending.PendingCertifiedProductEntity;
import gov.healthit.chpl.entity.listing.pending.PendingCertifiedProductParentListingEntity;
import gov.healthit.chpl.entity.listing.pending.PendingCertifiedProductQmsStandardEntity;
import gov.healthit.chpl.entity.listing.pending.PendingCertifiedProductTargetedUserEntity;
import gov.healthit.chpl.entity.listing.pending.PendingCertifiedProductTestingLabMapEntity;
import gov.healthit.chpl.entity.listing.pending.PendingCqmCriterionEntity;
import gov.healthit.chpl.util.Util;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Singular;

@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
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
    private Boolean deleted;
    private Long lastModifiedUser;
    @Singular
    private Set<String> errorMessages = new HashSet<String>();
    @Singular
    private Set<String> warningMessages = new HashSet<String>();

    private String uniqueId;
    private String recordStatus;
    private String practiceType;
    private String developerName;
    private String productName;
    private String productVersion;
    private String certificationEdition;
    private String acbCertificationId;
    private String certificationBodyName;
    private String productClassificationName;
    @Setter(AccessLevel.NONE)
    @Getter(AccessLevel.NONE)
    private Date certificationDate;
    private String developerStreetAddress;
    private String developerCity;
    private String developerState;
    private String developerZipCode;
    private String developerWebsite;
    private String developerEmail;
    private String developerContactName;
    private String developerPhoneNumber;
    private Boolean selfDeveloper;
    private Long developerContactId;
    private String reportFileLocation;
    private String sedReportFileLocation;
    private String sedIntendedUserDescription;
    @Setter(AccessLevel.NONE)
    @Getter(AccessLevel.NONE)
    private Date sedTestingEnd;
    private Boolean ics;
    private Boolean hasQms;
    private Boolean accessibilityCertified;
    private TransparencyAttestationDTO transparencyAttestation;
    private String transparencyAttestationUrl;

    @Singular
    private List<PendingCertifiedProductTestingLabDTO> testingLabs = new ArrayList<PendingCertifiedProductTestingLabDTO>();

    @Singular
    private List<CertifiedProductDetailsDTO> icsParents = new ArrayList<CertifiedProductDetailsDTO>();

    @Singular
    private List<CertifiedProductDetailsDTO> icsChildren = new ArrayList<CertifiedProductDetailsDTO>();

    @Singular("certificationCriterionSingle")
    private List<PendingCertificationResultDTO> certificationCriterion = new ArrayList<PendingCertificationResultDTO>();

    @Singular("cqmCriterionSingle")
    private List<PendingCqmCriterionDTO> cqmCriterion = new ArrayList<PendingCqmCriterionDTO>();

    @Singular
    private List<PendingCertifiedProductQmsStandardDTO> qmsStandards = new ArrayList<PendingCertifiedProductQmsStandardDTO>();

    @Singular
    private List<PendingCertifiedProductTargetedUserDTO> targetedUsers = new ArrayList<PendingCertifiedProductTargetedUserDTO>();

    @Singular
    private List<PendingCertifiedProductAccessibilityStandardDTO> accessibilityStandards = new ArrayList<PendingCertifiedProductAccessibilityStandardDTO>();

    @Setter(AccessLevel.NONE)
    @Getter(AccessLevel.NONE)
    private Date uploadDate;

    public PendingCertifiedProductDTO(PendingCertifiedProductDetails details) {
        this.id = details.getId();
        if (details.getPracticeType().get("id") != null) {
            this.practiceTypeId = Long.valueOf(details.getPracticeType().get("id").toString());
        }
        if (details.getPracticeType().get("name") != null) {
            this.practiceType = details.getPracticeType().get("name").toString();
        }
        this.developerId = details.getDeveloper().getDeveloperId();
        this.developerName = details.getDeveloper().getName();
        this.developerWebsite = details.getDeveloper().getWebsite();
        this.selfDeveloper = details.getDeveloper().getSelfDeveloper();
        if (details.getDeveloper().getContact() != null) {
            this.developerEmail = details.getDeveloper().getContact().getEmail();
            this.developerContactName = details.getDeveloper().getContact().getFullName();
            this.developerPhoneNumber = details.getDeveloper().getContact().getPhoneNumber();
        }
        if (details.getDeveloper().getAddress() != null) {
            AddressDTO address = new AddressDTO();
            address.setId(details.getDeveloper().getAddress().getAddressId());
            address.setStreetLineOne(details.getDeveloper().getAddress().getLine1());
            address.setCity(details.getDeveloper().getAddress().getCity());
            address.setState(details.getDeveloper().getAddress().getState());
            address.setZipcode(details.getDeveloper().getAddress().getZipcode());
            address.setCountry("US");
            this.developerAddress = address;
        }

        if (details.getProduct() != null && details.getProduct().getProductId() != null) {
            this.productId = Long.valueOf(details.getProduct().getProductId().toString());
        }
        if (details.getProduct() != null && !StringUtils.isEmpty(details.getProduct().getName())) {
            this.productName = details.getProduct().getName();
        }
        if (details.getVersion() != null && details.getVersion().getVersionId() != null) {
            this.productVersionId = Long.valueOf(details.getVersion().getVersionId().toString());
        }
        if (details.getVersion() != null && !StringUtils.isEmpty(details.getVersion().getVersion())) {
            this.productVersion = details.getVersion().getVersion();
        }
        if (details.getCertificationEdition().get(CertifiedProductSearchDetails.EDITION_ID_KEY) != null) {
            this.certificationEditionId = Long
                    .valueOf(details.getCertificationEdition().get(CertifiedProductSearchDetails.EDITION_ID_KEY).toString());
        }
        if (details.getCertificationEdition().get(CertifiedProductSearchDetails.EDITION_NAME_KEY) != null) {
            this.certificationEdition = details.getCertificationEdition().get(CertifiedProductSearchDetails.EDITION_NAME_KEY)
                    .toString();
        }
        if (details.getCertifyingBody().get(CertifiedProductSearchDetails.ACB_ID_KEY) != null) {
            this.certificationBodyId = Long
                    .valueOf(details.getCertifyingBody().get(CertifiedProductSearchDetails.ACB_ID_KEY).toString());
        }
        if (details.getCertifyingBody().get(CertifiedProductSearchDetails.ACB_NAME_KEY) != null) {
            this.certificationBodyName = details.getCertifyingBody().get(CertifiedProductSearchDetails.ACB_NAME_KEY).toString();
        }
        if (details.getClassificationType().get("id") != null) {
            String classificationTypeId = details.getClassificationType().get("id").toString();
            this.productClassificationId = Long.valueOf(classificationTypeId);
        }
        if (details.getClassificationType().get("name") != null) {
            this.productClassificationName = details.getClassificationType().get("name").toString();
        }
        if (details.getCertificationDate() != null) {
            this.certificationDate = new Date(details.getCertificationDate());
        }

        this.uniqueId = details.getChplProductNumber();
        this.recordStatus = details.getRecordStatus();
        this.acbCertificationId = details.getAcbCertificationId();
        this.reportFileLocation = details.getReportFileLocation();
        this.sedReportFileLocation = details.getSedReportFileLocation();
        this.sedIntendedUserDescription = details.getSedIntendedUserDescription();
        this.sedTestingEnd = details.getSedTestingEndDate();
        this.hasQms = details.getHasQms();
        this.ics = (details.getIcs() == null || details.getIcs().getInherits() == null) ? Boolean.FALSE
                : details.getIcs().getInherits();
        this.accessibilityCertified = details.getAccessibilityCertified();
        if (details.getTransparencyAttestation() != null) {
            this.transparencyAttestation = new TransparencyAttestationDTO(details.getTransparencyAttestation());
        }
        this.transparencyAttestationUrl = details.getTransparencyAttestationUrl();
        this.accessibilityCertified = details.getAccessibilityCertified();

        if (details.getIcs() != null) {
            if (details.getIcs().getParents() != null && details.getIcs().getParents().size() > 0) {
                for (CertifiedProduct parent : details.getIcs().getParents()) {
                    CertifiedProductDetailsDTO parentCp = new CertifiedProductDetailsDTO();
                    parentCp.setId(parent.getId());
                    parentCp.setChplProductNumber(parent.getChplProductNumber());
                    this.icsParents.add(parentCp);
                }
            }
            if (details.getIcs().getChildren() != null && details.getIcs().getChildren().size() > 0) {
                for (CertifiedProduct child : details.getIcs().getChildren()) {
                    CertifiedProductDetailsDTO childCp = new CertifiedProductDetailsDTO();
                    childCp.setId(child.getId());
                    childCp.setChplProductNumber(child.getChplProductNumber());
                    this.icsChildren.add(childCp);
                }
            }
        }

        List<CertifiedProductTestingLab> testingLabs = details.getTestingLabs();
        if (testingLabs != null && testingLabs.size() > 0) {
            for (CertifiedProductTestingLab tl : testingLabs) {
                PendingCertifiedProductTestingLabDTO tlDto = new PendingCertifiedProductTestingLabDTO();
                if (tl.getTestingLabId() != null) {
                    tlDto.setTestingLabId(tl.getTestingLabId());
                }
                if (tl.getTestingLabName() != null) {
                    tlDto.setTestingLabName(tl.getTestingLabName());
                }
                this.testingLabs.add(tlDto);
            }
        }

        List<CertifiedProductQmsStandard> qmsStandards = details.getQmsStandards();
        if (qmsStandards != null && qmsStandards.size() > 0) {
            for (CertifiedProductQmsStandard qms : qmsStandards) {
                PendingCertifiedProductQmsStandardDTO qmsDto = new PendingCertifiedProductQmsStandardDTO();
                qmsDto.setApplicableCriteria(qms.getApplicableCriteria());
                qmsDto.setModification(qms.getQmsModification());
                qmsDto.setQmsStandardId(qms.getQmsStandardId());
                qmsDto.setName(qms.getQmsStandardName());
                this.qmsStandards.add(qmsDto);
            }
        }

        List<CertifiedProductAccessibilityStandard> accStds = details.getAccessibilityStandards();
        if (accStds != null && accStds.size() > 0) {
            for (CertifiedProductAccessibilityStandard as : accStds) {
                PendingCertifiedProductAccessibilityStandardDTO asDto = new PendingCertifiedProductAccessibilityStandardDTO();
                asDto.setAccessibilityStandardId(as.getAccessibilityStandardId());
                asDto.setName(as.getAccessibilityStandardName());
                this.accessibilityStandards.add(asDto);
            }
        }

        List<CertifiedProductTargetedUser> targetedUsers = details.getTargetedUsers();
        if (targetedUsers != null && targetedUsers.size() > 0) {
            for (CertifiedProductTargetedUser tu : targetedUsers) {
                PendingCertifiedProductTargetedUserDTO tuDto = new PendingCertifiedProductTargetedUserDTO();
                tuDto.setName(tu.getTargetedUserName());
                tuDto.setTargetedUserId(tu.getTargetedUserId());
                this.targetedUsers.add(tuDto);
            }
        }

        List<CertificationResult> certificationResults = details.getCertificationResults();
        for (CertificationResult crResult : certificationResults) {
            PendingCertificationResultDTO certDto = new PendingCertificationResultDTO();
            CertificationCriterionDTO criterion = null;
            if (crResult.getCriterion() != null) {
                criterion = new CertificationCriterionDTO(crResult.getCriterion());
            }
            certDto.setCriterion(criterion);
            certDto.setMeetsCriteria(crResult.isSuccess());
            certDto.setGap(crResult.isGap());
            certDto.setG1Success(crResult.isG1Success());
            certDto.setG2Success(crResult.isG2Success());
            certDto.setSed(crResult.isSed());
            certDto.setApiDocumentation(crResult.getApiDocumentation());
            certDto.setPrivacySecurityFramework(crResult.getPrivacySecurityFramework());
            certDto.setAttestationAnswer(crResult.getAttestationAnswer());
            certDto.setDocumentationUrl(crResult.getDocumentationUrl());
            certDto.setExportDocumentation(crResult.getExportDocumentation());
            certDto.setUseCases(crResult.getUseCases());

            if (crResult.getAdditionalSoftware() != null && crResult.getAdditionalSoftware().size() > 0) {
                for (CertificationResultAdditionalSoftware software : crResult.getAdditionalSoftware()) {
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

            if (crResult.getTestDataUsed() != null && crResult.getTestDataUsed().size() > 0) {
                for (CertificationResultTestData certResultTestData : crResult.getTestDataUsed()) {
                    PendingCertificationResultTestDataDTO testDataDto = new PendingCertificationResultTestDataDTO();
                    if (certResultTestData.getTestData() != null) {
                        testDataDto.setTestDataId(certResultTestData.getTestData().getId());
                        TestDataDTO testData = new TestDataDTO();
                        testData.setId(certResultTestData.getTestData().getId());
                        testData.setName(certResultTestData.getTestData().getName());
                        testDataDto.setTestData(testData);
                    }
                    testDataDto.setAlteration(certResultTestData.getAlteration());
                    testDataDto.setVersion(certResultTestData.getVersion());
                    certDto.getTestData().add(testDataDto);
                }
            }

            if (crResult.getTestFunctionality() != null && crResult.getTestFunctionality().size() > 0) {
                for (CertificationResultTestFunctionality func : crResult.getTestFunctionality()) {
                    PendingCertificationResultTestFunctionalityDTO funcDto = new PendingCertificationResultTestFunctionalityDTO();
                    funcDto.setNumber(func.getName());
                    funcDto.setTestFunctionalityId(func.getTestFunctionalityId());
                    certDto.getTestFunctionality().add(funcDto);
                }
            }

            if (crResult.getTestProcedures() != null && crResult.getTestProcedures().size() > 0) {
                for (CertificationResultTestProcedure proc : crResult.getTestProcedures()) {
                    PendingCertificationResultTestProcedureDTO procDto = new PendingCertificationResultTestProcedureDTO();
                    if (proc.getTestProcedure() != null) {
                        procDto.setTestProcedureId(proc.getTestProcedure().getId());
                        TestProcedureDTO testProcedure = new TestProcedureDTO();
                        testProcedure.setId(proc.getTestProcedure().getId());
                        testProcedure.setName(proc.getTestProcedure().getName());
                        procDto.setTestProcedure(testProcedure);
                    }
                    procDto.setVersion(proc.getTestProcedureVersion());
                    certDto.getTestProcedures().add(procDto);
                }
            }

            if (crResult.getTestStandards() != null && crResult.getTestStandards().size() > 0) {
                for (CertificationResultTestStandard std : crResult.getTestStandards()) {
                    PendingCertificationResultTestStandardDTO stdDto = new PendingCertificationResultTestStandardDTO();
                    stdDto.setName(std.getTestStandardName());
                    stdDto.setTestStandardId(std.getTestStandardId());
                    certDto.getTestStandards().add(stdDto);
                }
            }

            if (crResult.getTestToolsUsed() != null && crResult.getTestToolsUsed().size() > 0) {
                for (CertificationResultTestTool tool : crResult.getTestToolsUsed()) {
                    PendingCertificationResultTestToolDTO toolDto = new PendingCertificationResultTestToolDTO();
                    toolDto.setName(tool.getTestToolName());
                    toolDto.setVersion(tool.getTestToolVersion());
                    toolDto.setTestToolId(tool.getTestToolId());
                    certDto.getTestTools().add(toolDto);
                }
            }

            if (details.getSed() != null && details.getSed().getUcdProcesses() != null
                    && details.getSed().getUcdProcesses().size() > 0) {
                for (UcdProcess ucd : details.getSed().getUcdProcesses()) {
                    boolean hasCriteria = false;
                    for (CertificationCriterion criteria : ucd.getCriteria()) {
                        if (criteria.getId().equals(certDto.getCriterion().getId())) {
                            hasCriteria = true;
                        }
                    }
                    if (hasCriteria) {
                        PendingCertificationResultUcdProcessDTO ucdDto = new PendingCertificationResultUcdProcessDTO();
                        ucdDto.setUcdProcessId(ucd.getId());
                        ucdDto.setUcdProcessDetails(ucd.getDetails());
                        ucdDto.setUcdProcessName(ucd.getName());
                        certDto.getUcdProcesses().add(ucdDto);
                    }
                }
            }

            if (details.getSed() != null && details.getSed().getTestTasks() != null
                    && details.getSed().getTestTasks().size() > 0) {
                for (TestTask task : details.getSed().getTestTasks()) {
                    boolean hasCriteria = false;
                    for (CertificationCriterion criteria : task.getCriteria()) {
                        if (criteria.getId().equals(certDto.getCriterion().getId())) {
                            hasCriteria = true;
                        }
                    }
                    if (hasCriteria) {
                        PendingCertificationResultTestTaskDTO crTaskDto = new PendingCertificationResultTestTaskDTO();
                        PendingTestTaskDTO taskDto = new PendingTestTaskDTO();
                        taskDto.setDescription(task.getDescription());
                        if (task.getTaskErrors() != null) {
                            taskDto.setTaskErrors(task.getTaskErrors().toString());
                        }
                        if (task.getTaskErrorsStddev() != null) {
                            taskDto.setTaskErrorsStddev(task.getTaskErrorsStddev().toString());
                        }
                        if (task.getTaskPathDeviationObserved() != null) {
                            taskDto.setTaskPathDeviationObserved(task.getTaskPathDeviationObserved().toString());
                        }
                        if (task.getTaskPathDeviationOptimal() != null) {
                            taskDto.setTaskPathDeviationOptimal(task.getTaskPathDeviationOptimal().toString());
                        }
                        if (task.getTaskRating() != null) {
                            taskDto.setTaskRating(task.getTaskRating().toString());
                        }
                        taskDto.setTaskRatingScale(task.getTaskRatingScale());
                        if (task.getTaskRatingStddev() != null) {
                            taskDto.setTaskRatingStddev(task.getTaskRatingStddev().toString());
                        }
                        if (task.getTaskSuccessAverage() != null) {
                            taskDto.setTaskSuccessAverage(task.getTaskSuccessAverage().toString());
                        }
                        if (task.getTaskSuccessStddev() != null) {
                            taskDto.setTaskSuccessStddev(task.getTaskSuccessStddev().toString());
                        }
                        if (task.getTaskTimeAvg() != null) {
                            taskDto.setTaskTimeAvg(task.getTaskTimeAvg().toString());
                        }
                        if (task.getTaskTimeDeviationObservedAvg() != null) {
                            taskDto.setTaskTimeDeviationObservedAvg(task.getTaskTimeDeviationObservedAvg().toString());
                        }
                        if (task.getTaskTimeDeviationOptimalAvg() != null) {
                            taskDto.setTaskTimeDeviationOptimalAvg(task.getTaskTimeDeviationOptimalAvg().toString());
                        }
                        if (task.getTaskTimeStddev() != null) {
                            taskDto.setTaskTimeStddev(task.getTaskTimeStddev().toString());
                        }
                        taskDto.setUniqueId(task.getUniqueId());
                        crTaskDto.setPendingTestTask(taskDto);

                        for (TestParticipant part : task.getTestParticipants()) {
                            PendingCertificationResultTestTaskParticipantDTO crPartDto = new PendingCertificationResultTestTaskParticipantDTO();
                            PendingTestParticipantDTO partDto = new PendingTestParticipantDTO();
                            partDto.setAssistiveTechnologyNeeds(part.getAssistiveTechnologyNeeds());
                            if (part.getComputerExperienceMonths() != null) {
                                partDto.setComputerExperienceMonths(part.getComputerExperienceMonths().toString());
                            }

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
                            if (part.getProductExperienceMonths() != null) {
                                partDto.setProductExperienceMonths(part.getProductExperienceMonths().toString());
                            }
                            if (part.getProfessionalExperienceMonths() != null) {
                                partDto.setProfessionalExperienceMonths(part.getProfessionalExperienceMonths().toString());
                            }
                            partDto.setUniqueId(part.getUniqueId());
                            crPartDto.setTestParticipant(partDto);
                            crTaskDto.getTaskParticipants().add(crPartDto);
                        }
                        certDto.getTestTasks().add(crTaskDto);
                    }
                }
            }

            this.certificationCriterion.add(certDto);
        }

        copyCriterionIdsToCqmMappings(details);
        List<CQMResultDetails> cqmResults = details.getCqmResults();
        for (CQMResultDetails cqmResult : cqmResults) {
            if (cqmResult.getSuccessVersions() != null && cqmResult.getSuccessVersions().size() > 0) {
                for (String version : cqmResult.getSuccessVersions()) {
                    PendingCqmCriterionDTO cqmDto = new PendingCqmCriterionDTO();
                    cqmDto.setCmsId(cqmResult.getCmsId());
                    cqmDto.setNqfNumber(cqmResult.getNqfNumber());
                    cqmDto.setCqmNumber(cqmResult.getNumber());
                    cqmDto.setTitle(cqmResult.getTitle());
                    cqmDto.setTypeId(cqmResult.getTypeId());
                    cqmDto.setDomain(cqmResult.getDomain());
                    cqmDto.setMeetsCriteria(Boolean.TRUE);
                    cqmDto.setVersion(version);
                    for (CQMResultCertification cqmCert : cqmResult.getCriteria()) {
                        PendingCqmCertificationCriterionDTO pendingCqmCert = new PendingCqmCertificationCriterionDTO();
                        pendingCqmCert.setCertificationId(cqmCert.getCertificationId());
                        pendingCqmCert.setCertificationCriteriaNumber(cqmCert.getCertificationNumber());
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

    public PendingCertifiedProductDTO(PendingCertifiedProductEntity entity) {
        this();
        this.id = entity.getId();
        this.hasQms = entity.isHasQms();
        this.practiceTypeId = entity.getPracticeTypeId();
        this.deleted = entity.getDeleted();
        this.lastModifiedUser = entity.getLastModifiedUser();
        this.developerId = entity.getDeveloperId();
        this.productId = entity.getProductId();
        this.productVersionId = entity.getProductVersionId();
        this.certificationEditionId = entity.getCertificationEditionId();
        this.certificationBodyId = entity.getCertificationBodyId();
        this.productClassificationId = entity.getProductClassificationId();

        this.uniqueId = entity.getUniqueId();
        this.recordStatus = entity.getRecordStatus();
        this.practiceType = entity.getPracticeType();
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
        this.selfDeveloper = entity.getSelfDeveloper();
        this.reportFileLocation = entity.getReportFileLocation();
        this.sedReportFileLocation = entity.getSedReportFileLocation();
        this.sedIntendedUserDescription = entity.getSedIntendedUserDescription();
        this.sedTestingEnd = entity.getSedTestingEnd();
        this.ics = entity.getIcs();
        this.accessibilityCertified = entity.getAccessibilityCertified();
        if (entity.getTransparencyAttestation() != null) {
            this.transparencyAttestation = new TransparencyAttestationDTO(entity.getTransparencyAttestation().toString());
        }
        this.transparencyAttestationUrl = entity.getTransparencyAttestationUrl();

        this.uploadDate = entity.getCreationDate();

        Set<PendingCertifiedProductTestingLabMapEntity> testingLabs = entity.getTestingLabs();
        if (testingLabs != null && testingLabs.size() > 0) {
            for (PendingCertifiedProductTestingLabMapEntity testingLab : testingLabs) {
                this.testingLabs.add(new PendingCertifiedProductTestingLabDTO(testingLab));
            }
        }

        Set<PendingCertifiedProductQmsStandardEntity> qmsStandards = entity.getQmsStandards();
        if (qmsStandards != null && qmsStandards.size() > 0) {
            for (PendingCertifiedProductQmsStandardEntity qmsStandard : qmsStandards) {
                this.qmsStandards.add(new PendingCertifiedProductQmsStandardDTO(qmsStandard));
            }
        }

        Set<PendingCertifiedProductTargetedUserEntity> targetedUsers = entity.getTargetedUsers();
        if (targetedUsers != null && targetedUsers.size() > 0) {
            for (PendingCertifiedProductTargetedUserEntity tu : targetedUsers) {
                this.targetedUsers.add(new PendingCertifiedProductTargetedUserDTO(tu));
            }
        }

        Set<PendingCertifiedProductAccessibilityStandardEntity> accStds = entity.getAccessibilityStandards();
        if (accStds != null && accStds.size() > 0) {
            for (PendingCertifiedProductAccessibilityStandardEntity as : accStds) {
                this.accessibilityStandards.add(new PendingCertifiedProductAccessibilityStandardDTO(as));
            }
        }

        Set<PendingCertifiedProductParentListingEntity> parents = entity.getParentListings();
        if (parents != null && parents.size() > 0) {
            for (PendingCertifiedProductParentListingEntity parent : parents) {
                CertifiedProductDetailsDTO listing = new CertifiedProductDetailsDTO();
                listing.setId(parent.getParentListingId());
                listing.setChplProductNumber(parent.getParentListingUniqueId());
                if (parent.getParentListing() != null) {
                    listing.setChplProductNumber(parent.getParentListing().getChplProductNumber());
                    listing.setCertificationDate(parent.getParentListing().getCertificationDate());
                    listing.setYear(parent.getParentListing().getEdition());
                }
                this.icsParents.add(listing);
            }
        }

        Set<PendingCertificationResultEntity> criterionEntities = entity.getCertificationCriterion();
        if (criterionEntities != null && criterionEntities.size() > 0) {
            for (PendingCertificationResultEntity crEntity : criterionEntities) {
                this.certificationCriterion.add(new PendingCertificationResultDTO(crEntity));
            }
        }
        Set<PendingCqmCriterionEntity> cqmEntities = entity.getCqmCriterion();
        if (cqmEntities != null && cqmEntities.size() > 0) {
            for (PendingCqmCriterionEntity cqmEntity : cqmEntities) {
                this.cqmCriterion.add(new PendingCqmCriterionDTO(cqmEntity));
            }
        }
    }

    public Date getCertificationDate() {
        return Util.getNewDate(certificationDate);
    }

    public void setCertificationDate(Date certificationDate) {
        this.certificationDate = Util.getNewDate(certificationDate);
    }

    public Date getUploadDate() {
        return Util.getNewDate(uploadDate);
    }

    public void setUploadDate(Date uploadDate) {
        this.uploadDate = Util.getNewDate(uploadDate);
    }

    public Date getSedTestingEnd() {
        return Util.getNewDate(sedTestingEnd);
    }

    public void setSedTestingEnd(Date sedTestingEnd) {
        this.sedTestingEnd = Util.getNewDate(sedTestingEnd);
    }

    private void copyCriterionIdsToCqmMappings(PendingCertifiedProductDetails listing) {
        for (CQMResultDetails cqmResult : listing.getCqmResults()) {
            for (CQMResultCertification cqmCertMapping : cqmResult.getCriteria()) {
                if (cqmCertMapping.getCertificationId() == null
                        && !StringUtils.isEmpty(cqmCertMapping.getCertificationNumber())) {
                    for (CertificationResult certResult : listing.getCertificationResults()) {
                        if (certResult.isSuccess().equals(Boolean.TRUE)
                                && certResult.getCriterion().getNumber().equals(cqmCertMapping.getCertificationNumber())) {
                            cqmCertMapping.setCertificationId(certResult.getCriterion().getId());
                        }
                    }
                }
            }
        }
    }
}

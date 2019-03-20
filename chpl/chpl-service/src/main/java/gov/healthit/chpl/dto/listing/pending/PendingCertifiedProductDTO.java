package gov.healthit.chpl.dto.listing.pending;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.util.StringUtils;

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
import gov.healthit.chpl.domain.CertifiedProductTargetedUser;
import gov.healthit.chpl.domain.CertifiedProductTestingLab;
import gov.healthit.chpl.domain.MacraMeasure;
import gov.healthit.chpl.domain.PendingCertifiedProductDetails;
import gov.healthit.chpl.domain.TestParticipant;
import gov.healthit.chpl.domain.TestTask;
import gov.healthit.chpl.domain.UcdProcess;
import gov.healthit.chpl.dto.AddressDTO;
import gov.healthit.chpl.dto.AgeRangeDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.dto.EducationTypeDTO;
import gov.healthit.chpl.dto.MacraMeasureDTO;
import gov.healthit.chpl.dto.TestDataDTO;
import gov.healthit.chpl.dto.TestProcedureDTO;
import gov.healthit.chpl.entity.listing.pending.PendingCertificationResultEntity;
import gov.healthit.chpl.entity.listing.pending.PendingCertifiedProductAccessibilityStandardEntity;
import gov.healthit.chpl.entity.listing.pending.PendingCertifiedProductEntity;
import gov.healthit.chpl.entity.listing.pending.PendingCertifiedProductParentListingEntity;
import gov.healthit.chpl.entity.listing.pending.PendingCertifiedProductQmsStandardEntity;
import gov.healthit.chpl.entity.listing.pending.PendingCertifiedProductTargetedUserEntity;
import gov.healthit.chpl.entity.listing.pending.PendingCertifiedProductTestingLabMapEntity;
import gov.healthit.chpl.entity.listing.pending.PendingCqmCriterionEntity;
import gov.healthit.chpl.util.Util;

/**
 * Pending Certified Product DTO.
 * @author alarned
 *
 */
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
    private Set<String> errorMessages;
    private Set<String> warningMessages;

    /**
     * fields directly from the spreadsheet
     **/
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
    private Boolean hasQms;
    private Boolean accessibilityCertified;
    private String transparencyAttestation;
    private String transparencyAttestationUrl;

    private List<PendingCertifiedProductTestingLabDTO> testingLabs;
    private List<CertifiedProductDetailsDTO> icsParents;
    private List<CertifiedProductDetailsDTO> icsChildren;
    private List<PendingCertificationResultDTO> certificationCriterion;
    private List<PendingCqmCriterionDTO> cqmCriterion;
    private List<PendingCertifiedProductQmsStandardDTO> qmsStandards;
    private List<PendingCertifiedProductTargetedUserDTO> targetedUsers;
    private List<PendingCertifiedProductAccessibilityStandardDTO> accessibilityStandards;

    private Date uploadDate;

    /**
     * Default constructor.
     */
    public PendingCertifiedProductDTO() {
        this.errorMessages = new HashSet<String>();
        this.warningMessages = new HashSet<String>();
        this.testingLabs = new ArrayList<PendingCertifiedProductTestingLabDTO>();
        this.icsParents = new ArrayList<CertifiedProductDetailsDTO>();
        this.icsChildren = new ArrayList<CertifiedProductDetailsDTO>();
        this.certificationCriterion = new ArrayList<PendingCertificationResultDTO>();
        this.cqmCriterion = new ArrayList<PendingCqmCriterionDTO>();
        this.qmsStandards = new ArrayList<PendingCertifiedProductQmsStandardDTO>();
        this.targetedUsers = new ArrayList<PendingCertifiedProductTargetedUserDTO>();
        this.accessibilityStandards = new ArrayList<PendingCertifiedProductAccessibilityStandardDTO>();
    }

    /**
     * Construct from details object.
     * @param details the object
     */
    public PendingCertifiedProductDTO(final PendingCertifiedProductDetails details) {
        this();
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
        if (details.getCertificationEdition().get("id") != null) {
            this.certificationEditionId = Long.valueOf(details.getCertificationEdition().get("id").toString());
        }
        if (details.getCertificationEdition().get("name") != null) {
            this.certificationEdition = details.getCertificationEdition().get("name").toString();
        }
        if (details.getCertifyingBody().get("id") != null) {
            this.certificationBodyId = Long.valueOf(details.getCertifyingBody().get("id").toString());
        }
        if (details.getCertifyingBody().get("name") != null) {
            this.certificationBodyName = details.getCertifyingBody().get("name").toString();
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
        this.transparencyAttestation = details.getTransparencyAttestation();
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
                PendingCertifiedProductAccessibilityStandardDTO asDto =
                        new PendingCertifiedProductAccessibilityStandardDTO();
                asDto.setAccessibilityStandardId(as.getAccessibilityStandardId());
                asDto.setName(as.getAccessibilityStandardName());
                this.accessibilityStandards.add(asDto);
            }
        }

        List<CertifiedProductTargetedUser> targetedUsers = details.getTargetedUsers();
        if (targetedUsers != null && targetedUsers.size() > 0) {
            for (CertifiedProductTargetedUser tu : targetedUsers) {
                PendingCertifiedProductTargetedUserDTO tuDto =
                        new PendingCertifiedProductTargetedUserDTO();
                tuDto.setName(tu.getTargetedUserName());
                tuDto.setTargetedUserId(tu.getTargetedUserId());
                this.targetedUsers.add(tuDto);
            }
        }

        List<CertificationResult> certificationResults = details.getCertificationResults();
        for (CertificationResult crResult : certificationResults) {
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

            if (crResult.getAdditionalSoftware() != null && crResult.getAdditionalSoftware().size() > 0) {
                for (CertificationResultAdditionalSoftware software : crResult.getAdditionalSoftware()) {
                    PendingCertificationResultAdditionalSoftwareDTO as =
                            new PendingCertificationResultAdditionalSoftwareDTO();
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
                    PendingCertificationResultTestFunctionalityDTO funcDto =
                            new PendingCertificationResultTestFunctionalityDTO();
                    funcDto.setNumber(func.getName());
                    funcDto.setTestFunctionalityId(func.getTestFunctionalityId());
                    certDto.getTestFunctionality().add(funcDto);
                }
            }

            if (crResult.getTestProcedures() != null && crResult.getTestProcedures().size() > 0) {
                for (CertificationResultTestProcedure proc : crResult.getTestProcedures()) {
                    PendingCertificationResultTestProcedureDTO procDto =
                            new PendingCertificationResultTestProcedureDTO();
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

            if (crResult.getG1MacraMeasures() != null && crResult.getG1MacraMeasures().size() > 0) {
                for (MacraMeasure mm : crResult.getG1MacraMeasures()) {
                    PendingCertificationResultMacraMeasureDTO mmDto = new PendingCertificationResultMacraMeasureDTO();
                    mmDto.setId(mm.getId());
                    mmDto.setMacraMeasureId(mm.getId());

                    MacraMeasureDTO measure = new MacraMeasureDTO();
                    measure.setId(mm.getId());
                    measure.setValue(mm.getAbbreviation());
                    mmDto.setMacraMeasure(measure);
                    certDto.getG1MacraMeasures().add(mmDto);
                }
            }

            if (crResult.getG2MacraMeasures() != null && crResult.getG2MacraMeasures().size() > 0) {
                for (MacraMeasure mm : crResult.getG2MacraMeasures()) {
                    PendingCertificationResultMacraMeasureDTO mmDto = new PendingCertificationResultMacraMeasureDTO();
                    mmDto.setId(mm.getId());
                    mmDto.setMacraMeasureId(mm.getId());

                    MacraMeasureDTO measure = new MacraMeasureDTO();
                    measure.setId(mm.getId());
                    measure.setValue(mm.getAbbreviation());
                    mmDto.setMacraMeasure(measure);
                    certDto.getG2MacraMeasures().add(mmDto);
                }
            }

            if (details.getSed() != null && details.getSed().getUcdProcesses() != null
                    && details.getSed().getUcdProcesses().size() > 0) {
                for (UcdProcess ucd : details.getSed().getUcdProcesses()) {
                    boolean hasCriteria = false;
                    for (CertificationCriterion criteria : ucd.getCriteria()) {
                        if (criteria.getNumber().equals(certDto.getNumber())) {
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
                        if (criteria.getNumber().equals(certDto.getNumber())) {
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
                            PendingCertificationResultTestTaskParticipantDTO crPartDto =
                                    new PendingCertificationResultTestTaskParticipantDTO();
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

    /**
     * Construct with entity.
     * @param entity the entity
     */
    public PendingCertifiedProductDTO(final PendingCertifiedProductEntity entity) {
        this();
        this.id = entity.getId();
        this.hasQms = entity.isHasQms();
        this.practiceTypeId = entity.getPracticeTypeId();
        this.deleted = entity.getDeleted();
        this.lastModifiedUser = entity.getLastModifiedUser();
        this.developerId = entity.getDeveloperId();
        // this.developerAddress = new AddressDTO(entity.getDeveloperAddress());
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
        this.reportFileLocation = entity.getReportFileLocation();
        this.sedReportFileLocation = entity.getSedReportFileLocation();
        this.sedIntendedUserDescription = entity.getSedIntendedUserDescription();
        this.sedTestingEnd = entity.getSedTestingEnd();
        this.ics = entity.getIcs();
        this.accessibilityCertified = entity.getAccessibilityCertified();
        if (entity.getTransparencyAttestation() != null) {
            this.transparencyAttestation = entity.getTransparencyAttestation().toString();
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

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Boolean getHasQms() {
        return hasQms;
    }

    public void setHasQms(final Boolean hasQms) {
        this.hasQms = hasQms;
    }

    public Long getPracticeTypeId() {
        return practiceTypeId;
    }

    public void setPracticeTypeId(final Long practiceTypeId) {
        this.practiceTypeId = practiceTypeId;
    }

    public Long getDeveloperId() {
        return developerId;
    }

    public void setDeveloperId(final Long developerId) {
        this.developerId = developerId;
    }

    public AddressDTO getDeveloperAddress() {
        return developerAddress;
    }

    public void setDeveloperAddress(final AddressDTO developerAddress) {
        this.developerAddress = developerAddress;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(final Long productId) {
        this.productId = productId;
    }

    public Long getProductVersionId() {
        return productVersionId;
    }

    public void setProductVersionId(final Long productVersionId) {
        this.productVersionId = productVersionId;
    }

    public Long getCertificationEditionId() {
        return certificationEditionId;
    }

    public void setCertificationEditionId(final Long certificationEditionId) {
        this.certificationEditionId = certificationEditionId;
    }

    public Long getCertificationBodyId() {
        return certificationBodyId;
    }

    public void setCertificationBodyId(final Long certificationBodyId) {
        this.certificationBodyId = certificationBodyId;
    }

    public Long getProductClassificationId() {
        return productClassificationId;
    }

    public void setProductClassificationId(final Long productClassificationId) {
        this.productClassificationId = productClassificationId;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(final String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getRecordStatus() {
        return recordStatus;
    }

    public void setRecordStatus(final String recordStatus) {
        this.recordStatus = recordStatus;
    }

    public String getPracticeType() {
        return practiceType;
    }

    public void setPracticeType(final String practiceType) {
        this.practiceType = practiceType;
    }

    public String getDeveloperName() {
        return developerName;
    }

    public void setDeveloperName(final String developerName) {
        this.developerName = developerName;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(final String productName) {
        this.productName = productName;
    }

    public String getProductVersion() {
        return productVersion;
    }

    public void setProductVersion(final String productVersion) {
        this.productVersion = productVersion;
    }

    public String getCertificationEdition() {
        return certificationEdition;
    }

    public void setCertificationEdition(final String certificationEdition) {
        this.certificationEdition = certificationEdition;
    }

    public String getAcbCertificationId() {
        return acbCertificationId;
    }

    public void setAcbCertificationId(final String acbCertificationId) {
        this.acbCertificationId = acbCertificationId;
    }

    public String getCertificationBodyName() {
        return certificationBodyName;
    }

    public void setCertificationBodyName(final String certificationBodyName) {
        this.certificationBodyName = certificationBodyName;
    }

    public String getProductClassificationName() {
        return productClassificationName;
    }

    public void setProductClassificationName(final String productClassificationName) {
        this.productClassificationName = productClassificationName;
    }

    public Date getCertificationDate() {
        return Util.getNewDate(certificationDate);
    }

    public void setCertificationDate(final Date certificationDate) {
        this.certificationDate = Util.getNewDate(certificationDate);
    }

    public String getDeveloperStreetAddress() {
        return developerStreetAddress;
    }

    public void setDeveloperStreetAddress(final String developerStreetAddress) {
        this.developerStreetAddress = developerStreetAddress;
    }

    public String getDeveloperCity() {
        return developerCity;
    }

    public void setDeveloperCity(final String developerCity) {
        this.developerCity = developerCity;
    }

    public String getDeveloperState() {
        return developerState;
    }

    public void setDeveloperState(final String developerState) {
        this.developerState = developerState;
    }

    public String getDeveloperZipCode() {
        return developerZipCode;
    }

    public void setDeveloperZipCode(final String developerZipCode) {
        this.developerZipCode = developerZipCode;
    }

    public String getDeveloperWebsite() {
        return developerWebsite;
    }

    public void setDeveloperWebsite(final String developerWebsite) {
        this.developerWebsite = developerWebsite;
    }

    public String getDeveloperEmail() {
        return developerEmail;
    }

    public void setDeveloperEmail(final String developerEmail) {
        this.developerEmail = developerEmail;
    }

    public String getReportFileLocation() {
        return reportFileLocation;
    }

    public void setReportFileLocation(final String reportFileLocation) {
        this.reportFileLocation = reportFileLocation;
    }

    public List<PendingCertificationResultDTO> getCertificationCriterion() {
        return certificationCriterion;
    }

    public void setCertificationCriterion(final List<PendingCertificationResultDTO> certificationCriterion) {
        this.certificationCriterion = certificationCriterion;
    }

    public List<PendingCqmCriterionDTO> getCqmCriterion() {
        return cqmCriterion;
    }

    public void setCqmCriterion(final List<PendingCqmCriterionDTO> cqmCriterion) {
        this.cqmCriterion = cqmCriterion;
    }

    public Date getUploadDate() {
        return Util.getNewDate(uploadDate);
    }

    public void setUploadDate(final Date uploadDate) {
        this.uploadDate = Util.getNewDate(uploadDate);
    }

    public Set<String> getErrorMessages() {
        return errorMessages;
    }

    public void setErrorMessages(final Set<String> errorMessages) {
        this.errorMessages = errorMessages;
    }

    public Set<String> getWarningMessages() {
        return warningMessages;
    }

    public void setWarningMessages(final Set<String> warningMessages) {
        this.warningMessages = warningMessages;
    }

    public Boolean getIcs() {
        return ics;
    }

    public void setIcs(final Boolean ics) {
        this.ics = ics;
    }

    public String getDeveloperContactName() {
        return developerContactName;
    }

    public void setDeveloperContactName(final String developerContactName) {
        this.developerContactName = developerContactName;
    }

    public String getDeveloperPhoneNumber() {
        return developerPhoneNumber;
    }

    public void setDeveloperPhoneNumber(final String developerPhoneNumber) {
        this.developerPhoneNumber = developerPhoneNumber;
    }

    public String getSedReportFileLocation() {
        return sedReportFileLocation;
    }

    public void setSedReportFileLocation(final String sedReportFileLocation) {
        this.sedReportFileLocation = sedReportFileLocation;
    }

    public List<PendingCertifiedProductQmsStandardDTO> getQmsStandards() {
        return qmsStandards;
    }

    public void setQmsStandards(final List<PendingCertifiedProductQmsStandardDTO> qmsStandards) {
        this.qmsStandards = qmsStandards;
    }

    public String getTransparencyAttestation() {
        return transparencyAttestation;
    }

    public void setTransparencyAttestation(final String transparencyAttestation) {
        this.transparencyAttestation = transparencyAttestation;
    }

    public Long getDeveloperContactId() {
        return developerContactId;
    }

    public void setDeveloperContactId(final Long developerContactId) {
        this.developerContactId = developerContactId;
    }

    public String getTransparencyAttestationUrl() {
        return transparencyAttestationUrl;
    }

    public void setTransparencyAttestationUrl(final String transparencyAttestationUrl) {
        this.transparencyAttestationUrl = transparencyAttestationUrl;
    }

    public List<PendingCertifiedProductTestingLabDTO> getTestingLabs() {
        return testingLabs;
    }

    public void setTestingLabs(final  List<PendingCertifiedProductTestingLabDTO> testingLabs) {
        this.testingLabs = testingLabs;
    }

    public List<PendingCertifiedProductTargetedUserDTO> getTargetedUsers() {
        return targetedUsers;
    }

    public void setTargetedUsers(final List<PendingCertifiedProductTargetedUserDTO> targetedUsers) {
        this.targetedUsers = targetedUsers;
    }

    public Boolean getAccessibilityCertified() {
        return accessibilityCertified;
    }

    public void setAccessibilityCertified(final Boolean accessibilityCertified) {
        this.accessibilityCertified = accessibilityCertified;
    }

    public List<PendingCertifiedProductAccessibilityStandardDTO> getAccessibilityStandards() {
        return accessibilityStandards;
    }

    public void setAccessibilityStandards(final
            List<PendingCertifiedProductAccessibilityStandardDTO> accessibilityStandards) {
        this.accessibilityStandards = accessibilityStandards;
    }

    public String getSedIntendedUserDescription() {
        return sedIntendedUserDescription;
    }

    public void setSedIntendedUserDescription(final String sedIntendedUserDescription) {
        this.sedIntendedUserDescription = sedIntendedUserDescription;
    }

    public Date getSedTestingEnd() {
        return Util.getNewDate(sedTestingEnd);
    }

    public void setSedTestingEnd(final Date sedTestingEnd) {
        this.sedTestingEnd = Util.getNewDate(sedTestingEnd);
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(final Boolean deleted) {
        this.deleted = deleted;
    }

    public Long getLastModifiedUser() {
        return lastModifiedUser;
    }

    public void setLastModifiedUser(final Long lastModifiedUser) {
        this.lastModifiedUser = lastModifiedUser;
    }

    public List<CertifiedProductDetailsDTO> getIcsParents() {
        return icsParents;
    }

    public void setIcsParents(final List<CertifiedProductDetailsDTO> icsParents) {
        this.icsParents = icsParents;
    }

    public List<CertifiedProductDetailsDTO> getIcsChildren() {
        return icsChildren;
    }

    public void setIcsChildren(final List<CertifiedProductDetailsDTO> icsChildren) {
        this.icsChildren = icsChildren;
    }
}

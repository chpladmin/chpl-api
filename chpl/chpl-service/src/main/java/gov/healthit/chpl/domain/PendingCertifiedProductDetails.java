package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.util.StringUtils;

import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultAdditionalSoftwareDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultMacraMeasureDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultTestDataDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultTestFunctionalityDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultTestProcedureDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultTestStandardDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultTestTaskDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultTestTaskParticipantDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultTestToolDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultUcdProcessDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductAccessibilityStandardDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductQmsStandardDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductTargetedUserDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductTestingLabDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCqmCertificationCriterionDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCqmCriterionDTO;
import gov.healthit.chpl.dto.listing.pending.PendingTestParticipantDTO;
import gov.healthit.chpl.dto.listing.pending.PendingTestTaskDTO;

/**
 * Pending Certified Product Details domain object.
 * @author alarned
 *
 */
public class PendingCertifiedProductDetails extends CertifiedProductSearchDetails implements Serializable {
    private static final long serialVersionUID = -461584179489619328L;
    private String recordStatus;
    private Boolean hasQms;

    /**
     * Default constructor.
     */
    public PendingCertifiedProductDetails() {
    }

    /**
     * Constructor from DTO.
     * @param dto the DTO
     */
    public PendingCertifiedProductDetails(final PendingCertifiedProductDTO dto) {
        this.setId(dto.getId());
        this.setErrorMessages(dto.getErrorMessages());
        this.setWarningMessages(dto.getWarningMessages());
        this.setRecordStatus(dto.getRecordStatus());
        this.setChplProductNumber(dto.getUniqueId());
        this.setHasQms(dto.getHasQms());
        this.setReportFileLocation(dto.getReportFileLocation());
        this.setSedReportFileLocation(dto.getSedReportFileLocation());
        this.setSedIntendedUserDescription(dto.getSedIntendedUserDescription());
        this.setSedTestingEndDate(dto.getSedTestingEnd());
        this.setAcbCertificationId(dto.getAcbCertificationId());
        InheritedCertificationStatus ics = new InheritedCertificationStatus();
        ics.setInherits(dto.getIcs());
        this.setIcs(ics);
        this.setAccessibilityCertified(dto.getAccessibilityCertified());

        Map<String, Object> classificationTypeMap = new HashMap<String, Object>();
        if (dto.getProductClassificationId() == null) {
            classificationTypeMap.put("id", null);
        } else {
            classificationTypeMap.put("id", dto.getProductClassificationId());
        }
        classificationTypeMap.put("name", dto.getProductClassificationName());
        this.setClassificationType(classificationTypeMap);

        this.setOtherAcb(null);

        Developer developer = new Developer();
        developer.setDeveloperId(dto.getDeveloperId());
        developer.setName(dto.getDeveloperName());
        developer.setWebsite(dto.getDeveloperWebsite());

        Contact developerContact = new Contact();
        developerContact.setFullName(dto.getDeveloperContactName());
        developerContact.setEmail(dto.getDeveloperEmail());
        developerContact.setPhoneNumber(dto.getDeveloperPhoneNumber());
        developer.setContact(developerContact);

        if (dto.getDeveloperAddress() != null) {
            Address address = new Address();
            address.setAddressId(dto.getDeveloperAddress().getId());
            address.setLine1(dto.getDeveloperStreetAddress());
            address.setCity(dto.getDeveloperCity());
            address.setState(dto.getDeveloperState());
            address.setZipcode(dto.getDeveloperZipCode());
            address.setCountry(dto.getDeveloperAddress().getCountry());
            developer.setAddress(address);
        } else {
            Address address = new Address();
            address.setLine1(dto.getDeveloperStreetAddress());
            address.setCity(dto.getDeveloperCity());
            address.setState(dto.getDeveloperState());
            address.setZipcode(dto.getDeveloperZipCode());
            developer.setAddress(address);
        }

        this.setDeveloper(developer);

        Product product = new Product();
        product.setProductId(dto.getProductId());
        product.setName(dto.getProductName());
        this.setProduct(product);

        ProductVersion version = new ProductVersion();
        version.setVersionId(dto.getProductVersionId());
        version.setVersion(dto.getProductVersion());
        this.setVersion(version);

        Map<String, Object> certificationEditionMap = new HashMap<String, Object>();
        if (dto.getCertificationEditionId() == null) {
            certificationEditionMap.put("id", null);
        } else {
            certificationEditionMap.put("id", dto.getCertificationEditionId());
        }
        certificationEditionMap.put("name", dto.getCertificationEdition());
        this.setCertificationEdition(certificationEditionMap);

        Map<String, Object> practiceTypeMap = new HashMap<String, Object>();
        if (dto.getPracticeTypeId() == null) {
            practiceTypeMap.put("id", null);
        } else {
            practiceTypeMap.put("id", dto.getPracticeTypeId());
        }
        practiceTypeMap.put("name", dto.getPracticeType());
        this.setPracticeType(practiceTypeMap);

        Map<String, Object> certifyingBodyMap = new HashMap<String, Object>();
        if (dto.getCertificationBodyId() == null) {
            certifyingBodyMap.put("id", null);
        } else {
            certifyingBodyMap.put("id", dto.getCertificationBodyId());
        }
        certifyingBodyMap.put("name", dto.getCertificationBodyName());
        this.setCertifyingBody(certifyingBodyMap);

        List<PendingCertifiedProductTestingLabDTO> tlDtos = dto.getTestingLabs();
        if (tlDtos != null && tlDtos.size() > 0) {
            for (PendingCertifiedProductTestingLabDTO tlDto : tlDtos) {
                CertifiedProductTestingLab tl = new CertifiedProductTestingLab();
                if (tlDto.getTestingLabId() != null) {
                    tl.setTestingLabId(tlDto.getTestingLabId());
                }
                if (tlDto.getTestingLabName() != null) {
                    tl.setTestingLabName(tlDto.getTestingLabName());
                }
                this.getTestingLabs().add(tl);
            }
        }

        if (dto.getCertificationDate() != null) {
            this.setCertificationDate(dto.getCertificationDate().getTime());
        }

        if (dto.getCertificationCriterion() == null) {
            this.setCountCerts(0);
        } else {
            int certCount = 0;
            for (PendingCertificationResultDTO cert : dto.getCertificationCriterion()) {
                if (cert.getMeetsCriteria()) {
                    certCount++;
                }
            }
            this.setCountCerts(certCount);
        }

        if (dto.getCqmCriterion() == null) {
            this.setCountCqms(0);
        } else {
            int cqmCount = 0;
            Set<String> cqmsMet = new HashSet<String>();
            for (PendingCqmCriterionDTO cqm : dto.getCqmCriterion()) {
                if (!cqmsMet.contains(cqm.getCmsId()) && cqm.isMeetsCriteria()) {
                    cqmsMet.add(cqm.getCmsId());
                    cqmCount++;
                }
            }
            this.setCountCqms(cqmCount);
        }

        this.setTransparencyAttestation(dto.getTransparencyAttestation());
        this.setTransparencyAttestationUrl(dto.getTransparencyAttestationUrl());

        List<PendingCertifiedProductQmsStandardDTO> qmsDtos = dto.getQmsStandards();
        if (qmsDtos != null && qmsDtos.size() > 0) {
            for (PendingCertifiedProductQmsStandardDTO qmsDto : qmsDtos) {
                CertifiedProductQmsStandard qms = new CertifiedProductQmsStandard();
                qms.setApplicableCriteria(qmsDto.getApplicableCriteria());
                qms.setQmsModification(qmsDto.getModification());
                qms.setQmsStandardName(qmsDto.getName());
                qms.setQmsStandardId(qmsDto.getQmsStandardId());
                this.getQmsStandards().add(qms);
            }
        }

        List<PendingCertifiedProductTargetedUserDTO> tuDtos = dto.getTargetedUsers();
        if (tuDtos != null && tuDtos.size() > 0) {
            for (PendingCertifiedProductTargetedUserDTO tuDto : tuDtos) {
                CertifiedProductTargetedUser tu = new CertifiedProductTargetedUser();
                tu.setTargetedUserId(tuDto.getTargetedUserId());
                tu.setTargetedUserName(tuDto.getName());
                this.getTargetedUsers().add(tu);
            }
        }

        List<PendingCertifiedProductAccessibilityStandardDTO> asDtos = dto.getAccessibilityStandards();
        if (asDtos != null && asDtos.size() > 0) {
            for (PendingCertifiedProductAccessibilityStandardDTO asDto : asDtos) {
                CertifiedProductAccessibilityStandard as = new CertifiedProductAccessibilityStandard();
                as.setAccessibilityStandardId(asDto.getAccessibilityStandardId());
                as.setAccessibilityStandardName(asDto.getName());
                this.getAccessibilityStandards().add(as);
            }
        }

        List<CertifiedProductDetailsDTO> parentListings = dto.getIcsParents();
        if (parentListings != null && parentListings.size() > 0) {
            for (CertifiedProductDetailsDTO parentListing : parentListings) {
                CertifiedProduct cp = new CertifiedProduct(parentListing);
                this.getIcs().getParents().add(cp);
            }
        }

        List<CertificationResult> certList = new ArrayList<CertificationResult>();
        for (PendingCertificationResultDTO certCriterion : dto.getCertificationCriterion()) {
            CertificationCriterion criteria = new CertificationCriterion();
            criteria.setNumber(certCriterion.getNumber());
            criteria.setTitle(certCriterion.getTitle());

            CertificationResult cert = new CertificationResult();
            cert.setNumber(certCriterion.getNumber());
            cert.setTitle(certCriterion.getTitle());
            cert.setSuccess(certCriterion.getMeetsCriteria());
            cert.setGap(certCriterion.getGap());
            cert.setSed(certCriterion.getSed());
            cert.setG1Success(certCriterion.getG1Success());
            cert.setG2Success(certCriterion.getG2Success());
            cert.setApiDocumentation(certCriterion.getApiDocumentation());
            cert.setPrivacySecurityFramework(certCriterion.getPrivacySecurityFramework());

            if (certCriterion.getAdditionalSoftware() != null) {
                for (PendingCertificationResultAdditionalSoftwareDTO as : certCriterion.getAdditionalSoftware()) {
                    CertificationResultAdditionalSoftware software = new CertificationResultAdditionalSoftware();
                    software.setCertifiedProductId(as.getCertifiedProductId());
                    software.setCertifiedProductNumber(as.getChplId());
                    software.setName(as.getName());
                    software.setVersion(as.getVersion());
                    software.setJustification(as.getJustification());
                    software.setGrouping(as.getGrouping());
                    cert.getAdditionalSoftware().add(software);
                }
            } else {
                cert.setAdditionalSoftware(null);
            }

            if (certCriterion.getTestData() != null) {
                for (PendingCertificationResultTestDataDTO td : certCriterion.getTestData()) {
                    CertificationResultTestData certResultTestData = new CertificationResultTestData();
                    TestData testData = new TestData();
                    if (td.getTestData() != null) {
                        testData.setId(td.getTestData().getId());
                        testData.setName(td.getTestData().getName());
                    } else {
                        testData.setId(td.getTestDataId());
                        testData.setName(td.getEnteredName());
                    }
                    certResultTestData.setTestData(testData);
                    certResultTestData.setVersion(td.getVersion());
                    certResultTestData.setAlteration(td.getAlteration());
                    cert.getTestDataUsed().add(certResultTestData);
                }
            } else {
                cert.setTestDataUsed(null);
            }

            if (certCriterion.getTestTools() != null) {
                for (PendingCertificationResultTestToolDTO tt : certCriterion.getTestTools()) {
                    CertificationResultTestTool testTool = new CertificationResultTestTool();
                    testTool.setTestToolId(tt.getTestToolId());
                    testTool.setTestToolName(tt.getName());
                    testTool.setTestToolVersion(tt.getVersion());
                    cert.getTestToolsUsed().add(testTool);
                }
            } else {
                cert.setTestToolsUsed(null);
            }

            if (certCriterion.getTestFunctionality() != null) {
                for (PendingCertificationResultTestFunctionalityDTO tf : certCriterion.getTestFunctionality()) {
                    CertificationResultTestFunctionality testFunc = new CertificationResultTestFunctionality();
                    testFunc.setTestFunctionalityId(tf.getTestFunctionalityId());
                    testFunc.setName(tf.getNumber());
                    cert.getTestFunctionality().add(testFunc);
                }
            } else {
                cert.setTestFunctionality(null);
            }

            if (certCriterion.getTestProcedures() != null) {
                for (PendingCertificationResultTestProcedureDTO tp : certCriterion.getTestProcedures()) {
                    CertificationResultTestProcedure certResultTestProc = new CertificationResultTestProcedure();
                    TestProcedure testProc = new TestProcedure();
                    if (tp.getTestProcedure() != null) {
                        testProc.setId(tp.getTestProcedure().getId());
                        testProc.setName(tp.getTestProcedure().getName());
                    } else {
                        testProc.setId(tp.getTestProcedureId());
                        testProc.setName(tp.getEnteredName());
                    }
                    certResultTestProc.setTestProcedure(testProc);
                    certResultTestProc.setTestProcedureVersion(tp.getVersion());
                    cert.getTestProcedures().add(certResultTestProc);
                }
            } else {
                cert.setTestProcedures(null);
            }

            if (certCriterion.getTestStandards() != null) {
                for (PendingCertificationResultTestStandardDTO ts : certCriterion.getTestStandards()) {
                    CertificationResultTestStandard testStd = new CertificationResultTestStandard();
                    testStd.setTestStandardId(ts.getTestStandardId());
                    testStd.setTestStandardName(ts.getName());
                    cert.getTestStandards().add(testStd);
                }
            } else {
                cert.setTestStandards(null);
            }

            if (certCriterion.getG1MacraMeasures() != null) {
                for (PendingCertificationResultMacraMeasureDTO mm : certCriterion.getG1MacraMeasures()) {
                    if (mm.getMacraMeasure() != null) {
                        MacraMeasure measure = new MacraMeasure(mm.getMacraMeasure());
                        cert.getG1MacraMeasures().add(measure);
                    } else {
                        MacraMeasure measure = new MacraMeasure();
                        measure.setId(mm.getMacraMeasureId());
                        cert.getG1MacraMeasures().add(measure);
                    }
                }
            } else {
                cert.setG1MacraMeasures(null);
            }

            if (certCriterion.getG2MacraMeasures() != null) {
                for (PendingCertificationResultMacraMeasureDTO mm : certCriterion.getG2MacraMeasures()) {
                    if (mm.getMacraMeasure() != null) {
                        MacraMeasure measure = new MacraMeasure(mm.getMacraMeasure());
                        cert.getG2MacraMeasures().add(measure);
                    } else {
                        MacraMeasure measure = new MacraMeasure();
                        measure.setId(mm.getMacraMeasureId());
                        cert.getG2MacraMeasures().add(measure);
                    }
                }
            } else {
                cert.setG2MacraMeasures(null);
            }

            // set all sed data: ucd processes and test tasks

            if (certCriterion.getUcdProcesses() != null && certCriterion.getUcdProcesses().size() > 0) {
                for (PendingCertificationResultUcdProcessDTO ucdDto : certCriterion.getUcdProcesses()) {
                    boolean alreadyExists = false;
                    UcdProcess newUcd = new UcdProcess();
                    newUcd.setId(ucdDto.getUcdProcessId());
                    newUcd.setName(ucdDto.getUcdProcessName());
                    newUcd.setDetails(ucdDto.getUcdProcessDetails());
                    for (UcdProcess currUcd : this.getSed().getUcdProcesses()) {
                        if (newUcd.matches(currUcd)) {
                            alreadyExists = true;
                            currUcd.getCriteria().add(criteria);
                        }
                    }
                    if (!alreadyExists) {
                        newUcd.getCriteria().add(criteria);
                        this.getSed().getUcdProcesses().add(newUcd);
                    }
                }
                cert.setSed(Boolean.TRUE);
            }

            if (certCriterion.getTestTasks() != null && certCriterion.getTestTasks().size() > 0) {
                cert.setSed(Boolean.TRUE);
                for (PendingCertificationResultTestTaskDTO ttDto : certCriterion.getTestTasks()) {
                    if (ttDto.getPendingTestTask() != null) {
                        boolean alreadyExists = false;
                        PendingTestTaskDTO tt = ttDto.getPendingTestTask();
                        TestTask newTask = new TestTask();
                        newTask.setUniqueId(tt.getUniqueId());
                        newTask.setDescription(tt.getDescription());
                        newTask.setTaskErrors(tt.getTaskErrors());
                        newTask.setTaskErrorsStddev(tt.getTaskErrorsStddev());
                        newTask.setTaskPathDeviationObserved(tt.getTaskPathDeviationObserved() == null ? ""
                                : tt.getTaskPathDeviationObserved() + "");
                        newTask.setTaskPathDeviationOptimal(
                                tt.getTaskPathDeviationOptimal() == null ? "" : tt.getTaskPathDeviationOptimal() + "");
                        newTask.setTaskRating(tt.getTaskRating());
                        newTask.setTaskRatingScale(tt.getTaskRatingScale());
                        newTask.setTaskRatingStddev(tt.getTaskRatingStddev());
                        newTask.setTaskSuccessAverage(tt.getTaskSuccessAverage());
                        newTask.setTaskSuccessStddev(tt.getTaskSuccessStddev());
                        newTask.setTaskTimeAvg(tt.getTaskTimeAvg() == null ? "" : tt.getTaskTimeAvg() + "");
                        newTask.setTaskTimeDeviationObservedAvg(tt.getTaskTimeDeviationObservedAvg() == null ? ""
                                : tt.getTaskTimeDeviationObservedAvg() + "");
                        newTask.setTaskTimeDeviationOptimalAvg(tt.getTaskTimeDeviationOptimalAvg() == null ? ""
                                : tt.getTaskTimeDeviationOptimalAvg() + "");
                        newTask.setTaskTimeStddev(tt.getTaskTimeStddev() == null ? "" : tt.getTaskTimeStddev() + "");

                        if (ttDto.getTaskParticipants() != null) {
                            for (PendingCertificationResultTestTaskParticipantDTO ptDto : ttDto.getTaskParticipants()) {
                                if (ptDto.getTestParticipant() != null) {
                                    PendingTestParticipantDTO pt = ptDto.getTestParticipant();
                                    TestParticipant part = new TestParticipant();
                                    part.setUniqueId(pt.getUniqueId());
                                    part.setAgeRangeId(pt.getAgeRangeId());
                                    if (pt.getAgeRange() != null) {
                                        part.setAgeRange(pt.getAgeRange().getAge());
                                    }
                                    part.setAssistiveTechnologyNeeds(pt.getAssistiveTechnologyNeeds());
                                    part.setComputerExperienceMonths(pt.getComputerExperienceMonths() == null ? ""
                                            : pt.getComputerExperienceMonths() + "");
                                    part.setEducationTypeId(pt.getEducationTypeId());
                                    if (pt.getEducationType() != null) {
                                        part.setEducationTypeName(pt.getEducationType().getName());
                                    }
                                    part.setGender(pt.getGender());
                                    part.setOccupation(pt.getOccupation());
                                    part.setProductExperienceMonths(pt.getProductExperienceMonths() == null ? ""
                                            : pt.getProductExperienceMonths() + "");
                                    part.setProfessionalExperienceMonths(pt.getProfessionalExperienceMonths() == null
                                            ? "" : pt.getProfessionalExperienceMonths() + "");
                                    newTask.getTestParticipants().add(part);
                                }
                            }
                        }

                        for (TestTask currTask : this.getSed().getTestTasks()) {
                            if (newTask.matches(currTask)) {
                                alreadyExists = true;
                                currTask.getCriteria().add(criteria);
                            }
                        }
                        if (!alreadyExists) {
                            newTask.getCriteria().add(criteria);
                            this.getSed().getTestTasks().add(newTask);
                        }
                    }
                }
            }

            certList.add(cert);
        }
        this.setCertificationResults(certList);

        // set cqm results
        List<CQMResultDetails> cqmResults = new ArrayList<CQMResultDetails>();
        for (PendingCqmCriterionDTO pendingCqm : dto.getCqmCriterion()) {
            boolean existingCms = false;
            if (!StringUtils.isEmpty(pendingCqm.getCmsId())) {
                for (CQMResultDetails result : cqmResults) {
                    if (!dto.getCertificationEdition().equals("2011")
                            && result.getCmsId().equals(pendingCqm.getCmsId())) {
                        existingCms = true;
                        result.getSuccessVersions().add(pendingCqm.getVersion());
                    }
                }
            }

            if (!existingCms) {
                CQMResultDetails cqm = new CQMResultDetails();
                cqm.setCmsId(pendingCqm.getCmsId());
                cqm.setNqfNumber(pendingCqm.getNqfNumber());
                cqm.setNumber(pendingCqm.getCqmNumber());
                cqm.setTitle(pendingCqm.getTitle());
                cqm.setTypeId(pendingCqm.getTypeId());
                cqm.setDomain(pendingCqm.getDomain());
                if (!dto.getCertificationEdition().equals("2011") && !StringUtils.isEmpty(pendingCqm.getCmsId())) {
                    cqm.getSuccessVersions().add(pendingCqm.getVersion());
                } else if (!StringUtils.isEmpty(pendingCqm.getNqfNumber())) {
                    cqm.setSuccess(pendingCqm.isMeetsCriteria());
                }
                // now add criteria mappings to all of our cqms
                List<PendingCqmCertificationCriterionDTO> criteria = pendingCqm.getCertifications();
                if (criteria != null && criteria.size() > 0) {
                    for (PendingCqmCertificationCriterionDTO criteriaDTO : criteria) {
                        CQMResultCertification c = new CQMResultCertification();
                        c.setCertificationId(criteriaDTO.getCertificationId());
                        c.setCertificationNumber(criteriaDTO.getCertificationCriteriaNumber());
                        c.setId(criteriaDTO.getId());
                        cqm.getCriteria().add(c);
                    }
                }

                cqmResults.add(cqm);
            }
        }
        this.setCqmResults(cqmResults);
    }

    public String getRecordStatus() {
        return recordStatus;
    }

    public void setRecordStatus(final String recordStatus) {
        this.recordStatus = recordStatus;
    }

    public Boolean getHasQms() {
        return hasQms;
    }

    public void setHasQms(final Boolean hasQms) {
        this.hasQms = hasQms;
    }
}

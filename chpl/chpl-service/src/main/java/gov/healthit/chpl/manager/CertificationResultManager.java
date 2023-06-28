package gov.healthit.chpl.manager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.conformanceMethod.domain.CertificationResultConformanceMethod;
import gov.healthit.chpl.criteriaattribute.testtool.TestTool;
import gov.healthit.chpl.criteriaattribute.testtool.TestToolDAO;
import gov.healthit.chpl.dao.AgeRangeDAO;
import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.dao.CertificationResultDAO;
import gov.healthit.chpl.dao.CertifiedProductSearchDAO;
import gov.healthit.chpl.dao.EducationTypeDAO;
import gov.healthit.chpl.dao.TestParticipantDAO;
import gov.healthit.chpl.dao.TestStandardDAO;
import gov.healthit.chpl.dao.TestTaskDAO;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultAdditionalSoftware;
import gov.healthit.chpl.domain.CertificationResultTestData;
import gov.healthit.chpl.domain.CertificationResultTestProcedure;
import gov.healthit.chpl.domain.CertificationResultTestStandard;
import gov.healthit.chpl.domain.CertificationResultTestTool;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.CertifiedProductUcdProcess;
import gov.healthit.chpl.domain.TestParticipant;
import gov.healthit.chpl.domain.TestTask;
import gov.healthit.chpl.dto.AgeRangeDTO;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.dto.CertificationResultAdditionalSoftwareDTO;
import gov.healthit.chpl.dto.CertificationResultDTO;
import gov.healthit.chpl.dto.CertificationResultTestDataDTO;
import gov.healthit.chpl.dto.CertificationResultTestProcedureDTO;
import gov.healthit.chpl.dto.CertificationResultTestStandardDTO;
import gov.healthit.chpl.dto.CertificationResultTestTaskDTO;
import gov.healthit.chpl.dto.CertificationResultUcdProcessDTO;
import gov.healthit.chpl.dto.EducationTypeDTO;
import gov.healthit.chpl.dto.TestParticipantDTO;
import gov.healthit.chpl.dto.TestStandardDTO;
import gov.healthit.chpl.dto.TestTaskDTO;
import gov.healthit.chpl.entity.listing.CertificationResultConformanceMethodEntity;
import gov.healthit.chpl.entity.listing.CertificationResultOptionalStandardEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.functionalityTested.CertificationResultFunctionalityTested;
import gov.healthit.chpl.functionalityTested.CertificationResultFunctionalityTestedDAO;
import gov.healthit.chpl.functionalityTested.FunctionalityTested;
import gov.healthit.chpl.functionalityTested.FunctionalityTestedDAO;
import gov.healthit.chpl.manager.impl.SecuredManager;
import gov.healthit.chpl.optionalStandard.domain.CertificationResultOptionalStandard;
import gov.healthit.chpl.svap.domain.CertificationResultSvap;
import gov.healthit.chpl.util.Util;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class CertificationResultManager extends SecuredManager {
    private CertifiedProductSearchDAO cpDao;
    private CertificationCriterionDAO criteriaDao;
    private CertificationResultDAO certResultDAO;
    private CertificationResultFunctionalityTestedDAO certResultFuncTestedDao;
    private TestStandardDAO testStandardDAO;
    private TestToolDAO testToolDAO;
    private FunctionalityTestedDAO functionalityTestedDao;
    private TestParticipantDAO testParticipantDAO;
    private AgeRangeDAO ageDao;
    private EducationTypeDAO educDao;
    private TestTaskDAO testTaskDAO;

    @SuppressWarnings("checkstyle:parameternumber")
    @Autowired
    public CertificationResultManager(CertifiedProductSearchDAO cpDao, CertificationCriterionDAO criteriaDao,
            CertificationResultDAO certResultDAO, CertificationResultFunctionalityTestedDAO certResultFuncTestedDao,
            TestStandardDAO testStandardDAO, TestToolDAO testToolDAO,
            FunctionalityTestedDAO functionalityTestedDao, TestParticipantDAO testParticipantDAO,
            AgeRangeDAO ageDao, EducationTypeDAO educDao, TestTaskDAO testTaskDAO) {
        this.cpDao = cpDao;
        this.criteriaDao = criteriaDao;
        this.certResultDAO = certResultDAO;
        this.certResultFuncTestedDao = certResultFuncTestedDao;
        this.testStandardDAO = testStandardDAO;
        this.testToolDAO = testToolDAO;
        this.functionalityTestedDao = functionalityTestedDao;
        this.testParticipantDAO = testParticipantDAO;
        this.ageDao = ageDao;
        this.educDao = educDao;
        this.testTaskDAO = testTaskDAO;
    }

    @SuppressWarnings({"checkstyle:methodlength", "checkstyle:linelength"})
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).CERTIFICATION_RESULTS, "
            + "T(gov.healthit.chpl.permissions.domains.CertificationResultsDomainPermissions).UPDATE, #existingListing)")
    @Transactional(rollbackFor = {
            EntityRetrievalException.class, EntityCreationException.class
    })
    public int update(CertifiedProductSearchDetails existingListing, CertifiedProductSearchDetails updatedListing,
            CertificationResult orig, CertificationResult updated)
            throws EntityCreationException, EntityRetrievalException, IOException {

        int numChanges = 0;
        // does the cert result need updated?
        boolean hasChanged = false;
        if (!StringUtils.equals(orig.getApiDocumentation(), updated.getApiDocumentation())
                || !StringUtils.equals(orig.getPrivacySecurityFramework(), updated.getPrivacySecurityFramework())
                || !Objects.equals(orig.isG1Success(), updated.isG1Success())
                || !Objects.equals(orig.isG2Success(), updated.isG2Success())
                || !Objects.equals(orig.isGap(), updated.isGap())
                || !Objects.equals(orig.isSed(), updated.isSed())
                || !Objects.equals(orig.isSuccess(), updated.isSuccess())
                || !Objects.equals(orig.getAttestationAnswer(), updated.getAttestationAnswer())
                || !Objects.equals(orig.getDocumentationUrl(), updated.getDocumentationUrl())
                || !Objects.equals(orig.getExportDocumentation(), updated.getExportDocumentation())
                || !Objects.equals(orig.getUseCases(), updated.getUseCases())
                || !Objects.equals(orig.getServiceBaseUrlList(), updated.getServiceBaseUrlList())) {
            hasChanged = true;
        }
        if (hasChanged) {
            CertificationResultDTO toUpdate = new CertificationResultDTO();
            toUpdate.setId(orig.getId());
            toUpdate.setCertifiedProductId(updatedListing.getId());
            CertificationCriterionDTO criteria = criteriaDao.getById(orig.getCriterion().getId());
            if (criteria == null || criteria.getId() == null) {
                throw new EntityCreationException(
                        "Cannot add certification result mapping for unknown criteria " + orig.getCriterion().getNumber());
            } else {
                toUpdate.setCertificationCriterionId(criteria.getId());
            }
            toUpdate.setSuccessful(updated.isSuccess());
            toUpdate.setG1Success(updated.isG1Success());
            toUpdate.setG2Success(updated.isG2Success());

            if (toUpdate.getSuccessful() != null && toUpdate.getSuccessful().booleanValue()) {
                toUpdate.setApiDocumentation(updated.getApiDocumentation());
                toUpdate.setPrivacySecurityFramework(updated.getPrivacySecurityFramework());
                toUpdate.setGap(updated.isGap());
                toUpdate.setSed(updated.isSed());
                toUpdate.setAttestationAnswer(updated.getAttestationAnswer());
                toUpdate.setDocumentationUrl(updated.getDocumentationUrl());
                toUpdate.setExportDocumentation(updated.getExportDocumentation());
                toUpdate.setUseCases(updated.getUseCases());
                toUpdate.setServiceBaseUrlList(updated.getServiceBaseUrlList());
            } else {
                toUpdate.setApiDocumentation(null);
                toUpdate.setPrivacySecurityFramework(null);
                toUpdate.setGap(null);
                toUpdate.setSed(null);
                toUpdate.setAttestationAnswer(null);
                toUpdate.setDocumentationUrl(null);
                toUpdate.setExportDocumentation(null);
                toUpdate.setUseCases(null);
                toUpdate.setServiceBaseUrlList(null);
            }

            certResultDAO.update(toUpdate);
            numChanges++;
        }

        if (updated.isSuccess() == null || !updated.isSuccess()) {
            // similar to delete - remove all related items
            numChanges += updateAdditionalSoftware(updated, orig.getAdditionalSoftware(), null);
            numChanges += updateConformanceMethods(updatedListing, updated, orig.getConformanceMethods(), null);
            numChanges += updateOptionalStandards(updatedListing, updated, orig.getOptionalStandards(), null);
            numChanges += updateTestStandards(updatedListing, updated, orig.getTestStandards(), null);
            numChanges += updateTestTools(updated, orig.getTestToolsUsed(), null);
            numChanges += updateTestData(updated, orig.getTestDataUsed(), null);
            numChanges += updateTestProcedures(updated, orig.getTestProcedures(), null);
            numChanges += updateFunctionalitiesTested(updatedListing, updated, orig.getFunctionalitiesTested(), null);
            numChanges += updateSvap(updated, orig.getSvaps(), null);

            if (existingListing.getSed() != null && existingListing.getSed().getUcdProcesses() != null
                    && existingListing.getSed().getUcdProcesses().size() > 0) {
                List<CertifiedProductUcdProcess> origUcdsForCriteria = new ArrayList<CertifiedProductUcdProcess>();
                for (CertifiedProductUcdProcess existingUcd : existingListing.getSed().getUcdProcesses()) {
                    boolean ucdMeetsCriteria = false;
                    for (CertificationCriterion ucdCriteria : existingUcd.getCriteria()) {
                        if (ucdCriteria.getId().equals(updated.getCriterion().getId())) {
                            ucdMeetsCriteria = true;
                        }
                    }
                    if (ucdMeetsCriteria) {
                        origUcdsForCriteria.add(existingUcd);
                    }
                }
                numChanges += updateUcdProcesses(updated, origUcdsForCriteria, null);
            }

            if (existingListing.getSed() != null && existingListing.getSed().getTestTasks() != null
                    && existingListing.getSed().getTestTasks().size() > 0) {
                List<TestTask> origTestTasksForCriteria = new ArrayList<TestTask>();
                for (TestTask existingTestTask : existingListing.getSed().getTestTasks()) {
                    boolean taskMeetsCriteria = false;
                    for (CertificationCriterion taskCriteria : existingTestTask.getCriteria()) {
                        if (taskCriteria.getId().equals(updated.getCriterion().getId())) {
                            taskMeetsCriteria = true;
                        }
                    }
                    if (taskMeetsCriteria) {
                        origTestTasksForCriteria.add(existingTestTask);
                    }
                }
                numChanges += updateTestTasks(updated, origTestTasksForCriteria, null);
            }
        } else {
            // create/update all related items
            numChanges += updateAdditionalSoftware(updated, orig.getAdditionalSoftware(), updated.getAdditionalSoftware());
            numChanges += updateConformanceMethods(updatedListing, updated, orig.getConformanceMethods(), updated.getConformanceMethods());
            numChanges += updateOptionalStandards(updatedListing, updated, orig.getOptionalStandards(), updated.getOptionalStandards());
            numChanges += updateTestStandards(updatedListing, updated, orig.getTestStandards(), updated.getTestStandards());
            numChanges += updateTestTools(updated, orig.getTestToolsUsed(), updated.getTestToolsUsed());
            numChanges += updateTestData(updated, orig.getTestDataUsed(), updated.getTestDataUsed());
            numChanges += updateTestProcedures(updated, orig.getTestProcedures(), updated.getTestProcedures());
            numChanges += updateFunctionalitiesTested(updatedListing, updated, orig.getFunctionalitiesTested(), updated.getFunctionalitiesTested());
            numChanges += updateSvap(updated, orig.getSvaps(), updated.getSvaps());

            List<CertifiedProductUcdProcess> origUcdsForCriteria = new ArrayList<CertifiedProductUcdProcess>();
            List<CertifiedProductUcdProcess> updatedUcdsForCriteria = new ArrayList<CertifiedProductUcdProcess>();
            if (existingListing.getSed() != null && existingListing.getSed().getUcdProcesses() != null
                    && existingListing.getSed().getUcdProcesses().size() > 0) {
                for (CertifiedProductUcdProcess existingUcd : existingListing.getSed().getUcdProcesses()) {
                    boolean ucdMeetsCriteria = false;
                    for (CertificationCriterion ucdCriteria : existingUcd.getCriteria()) {
                        if (ucdCriteria.getId().equals(updated.getCriterion().getId())
                                && orig.isSed() != null && orig.isSed()) {
                            ucdMeetsCriteria = true;
                        }
                    }
                    if (ucdMeetsCriteria) {
                        origUcdsForCriteria.add(existingUcd);
                    }
                }
            }
            if (updatedListing.getSed() != null && updatedListing.getSed().getUcdProcesses() != null
                    && updatedListing.getSed().getUcdProcesses().size() > 0) {
                for (CertifiedProductUcdProcess updatedUcd : updatedListing.getSed().getUcdProcesses()) {
                    boolean ucdMeetsCriteria = false;
                    for (CertificationCriterion ucdCriteria : updatedUcd.getCriteria()) {
                        if (ucdCriteria.getId().equals(updated.getCriterion().getId())
                                && updated.isSed() != null && updated.isSed()) {
                            ucdMeetsCriteria = true;
                        }
                    }
                    if (ucdMeetsCriteria) {
                        updatedUcdsForCriteria.add(updatedUcd);
                    }
                }
            }
            numChanges += updateUcdProcesses(updated, origUcdsForCriteria, updatedUcdsForCriteria);

            List<TestTask> origTestTasksForCriteria = new ArrayList<TestTask>();
            List<TestTask> updatedTestTasksForCriteria = new ArrayList<TestTask>();
            if (existingListing.getSed() != null && existingListing.getSed().getTestTasks() != null
                    && existingListing.getSed().getTestTasks().size() > 0) {
                for (TestTask existingTask : existingListing.getSed().getTestTasks()) {
                    boolean taskMeetsCriteria = false;
                    for (CertificationCriterion taskCriteria : existingTask.getCriteria()) {
                        if (taskCriteria.getId().equals(updated.getCriterion().getId())
                                && orig.isSed() != null && orig.isSed()) {
                            taskMeetsCriteria = true;
                        }
                    }
                    if (taskMeetsCriteria) {
                        origTestTasksForCriteria.add(existingTask);
                    }
                }
            }
            if (updatedListing.getSed() != null && updatedListing.getSed().getTestTasks() != null
                    && updatedListing.getSed().getTestTasks().size() > 0) {
                // Go through all the updated test tasks and participants.
                // Any with a negative ID are new and need to be added and the
                // negative ID could be repeated so a task/participant with a
                // negative id
                // needs to be added once and then that ID needs to be replaced
                // with
                // the created item's ID anywhere else that it is found
                for (TestTask task : updatedListing.getSed().getTestTasks()) {
                    if (task.getId() < 0) {
                        long prevId = task.getId();
                        TestTaskDTO createdTask = testTaskDAO.create(convert(task));
                        for (TestTask otherTask : updatedListing.getSed().getTestTasks()) {
                            if (otherTask.getId().longValue() == prevId) {
                                otherTask.setId(createdTask.getId());
                            }
                        }
                    }

                    for (TestParticipant participant : task.getTestParticipants()) {
                        if (participant.getId() < 0) {
                            long prevId = participant.getId();
                            TestParticipantDTO createdParticipant = testParticipantDAO.create(convert(participant));
                            for (TestTask otherTask : updatedListing.getSed().getTestTasks()) {
                                for (TestParticipant otherParticipant : otherTask.getTestParticipants()) {
                                    if (otherParticipant.getId().longValue() == prevId) {
                                        otherParticipant.setId(createdParticipant.getId());
                                    }
                                }
                            }
                        }
                    }
                }

                // now find appropriate test tasks for this certification result
                for (TestTask updatedTask : updatedListing.getSed().getTestTasks()) {
                    boolean taskMeetsCriteria = false;
                    for (CertificationCriterion taskCriteria : updatedTask.getCriteria()) {
                        if (taskCriteria.getId().equals(updated.getCriterion().getId())
                                && updated.isSed() != null && updated.isSed()) {
                            taskMeetsCriteria = true;
                        }
                    }
                    if (taskMeetsCriteria) {
                        updatedTestTasksForCriteria.add(updatedTask);
                    }
                }
            }
            numChanges += updateTestTasks(updated, origTestTasksForCriteria, updatedTestTasksForCriteria);
        }

        return numChanges;
    }

    private int updateAdditionalSoftware(CertificationResult certResult,
            List<CertificationResultAdditionalSoftware> existingAdditionalSoftware,
            List<CertificationResultAdditionalSoftware> updatedAdditionalSoftware)
            throws EntityCreationException, EntityRetrievalException {
        int numChanges = 0;
        List<CertificationResultAdditionalSoftware> additionalSoftwareToAdd = new ArrayList<CertificationResultAdditionalSoftware>();
        List<CertificationResultAdditionalSoftwarePair> additionalSoftwareToUpdate = new ArrayList<CertificationResultAdditionalSoftwarePair>();
        List<Long> idsToRemove = new ArrayList<Long>();

        // figure out which additional software to add
        if (updatedAdditionalSoftware != null && updatedAdditionalSoftware.size() > 0) {
            // fill in potentially missing cp id
            for (CertificationResultAdditionalSoftware updatedItem : updatedAdditionalSoftware) {
                if (updatedItem.getCertifiedProductId() == null
                        && !StringUtils.isEmpty(updatedItem.getCertifiedProductNumber())) {
                    Long cpId = cpDao.getListingIdByUniqueChplNumber(updatedItem.getCertifiedProductNumber());
                    updatedItem.setCertifiedProductId(cpId);
                }
            }

            if (existingAdditionalSoftware == null || existingAdditionalSoftware.size() == 0) {
                // existing listing has none, add all from the update
                for (CertificationResultAdditionalSoftware updatedItem : updatedAdditionalSoftware) {
                    additionalSoftwareToAdd.add(updatedItem);
                }
            } else if (existingAdditionalSoftware.size() > 0) {
                // existing listing has some, compare to the update to see if
                // any are different
                for (CertificationResultAdditionalSoftware updatedItem : updatedAdditionalSoftware) {
                    boolean inExistingListing = false;
                    for (CertificationResultAdditionalSoftware existingItem : existingAdditionalSoftware) {
                        if (updatedItem.matches(existingItem)) {
                            inExistingListing = true;
                            additionalSoftwareToUpdate
                                    .add(new CertificationResultAdditionalSoftwarePair(existingItem, updatedItem));
                        }
                    }

                    if (!inExistingListing) {
                        additionalSoftwareToAdd.add(updatedItem);
                    }
                }
            }
        }

        // figure out which additional software to remove
        if (existingAdditionalSoftware != null && existingAdditionalSoftware.size() > 0) {
            // if the updated listing has none, remove them all from existing
            if (updatedAdditionalSoftware == null || updatedAdditionalSoftware.size() == 0) {
                for (CertificationResultAdditionalSoftware existingItem : existingAdditionalSoftware) {
                    idsToRemove.add(existingItem.getId());
                }
            } else if (updatedAdditionalSoftware.size() > 0) {
                for (CertificationResultAdditionalSoftware existingItem : existingAdditionalSoftware) {
                    boolean inUpdatedListing = false;
                    for (CertificationResultAdditionalSoftware updatedItem : updatedAdditionalSoftware) {
                        inUpdatedListing = !inUpdatedListing ? existingItem.matches(updatedItem) : inUpdatedListing;
                    }
                    if (!inUpdatedListing) {
                        idsToRemove.add(existingItem.getId());
                    }
                }
            }
        }

        numChanges = additionalSoftwareToAdd.size() + idsToRemove.size();
        for (CertificationResultAdditionalSoftware toAdd : additionalSoftwareToAdd) {
            CertificationResultAdditionalSoftwareDTO toAddDto = convert(certResult.getId(), toAdd);
            certResultDAO.addAdditionalSoftwareMapping(toAddDto);
        }

        for (CertificationResultAdditionalSoftwarePair toUpdate : additionalSoftwareToUpdate) {
            boolean hasChanged = false;
            if (!Objects.equals(toUpdate.getOrig().getJustification(), toUpdate.getUpdated().getJustification())
                    || !Objects.equals(toUpdate.getOrig().getName(), toUpdate.getUpdated().getName())
                    || !Objects.equals(toUpdate.getOrig().getGrouping(), toUpdate.getUpdated().getGrouping())
                    || !Objects.equals(toUpdate.getOrig().getVersion(), toUpdate.getUpdated().getVersion())
                    || !Objects.equals(toUpdate.getOrig().getCertifiedProductId(),
                            toUpdate.getUpdated().getCertifiedProductId())
                    || !Objects.equals(toUpdate.getOrig().getCertifiedProductNumber(),
                            toUpdate.getUpdated().getCertifiedProductNumber())) {
                hasChanged = true;
            }
            if (hasChanged) {
                CertificationResultAdditionalSoftwareDTO toUpdateDto = convert(certResult.getId(),
                        toUpdate.getUpdated());
                certResultDAO.updateAdditionalSoftwareMapping(toUpdateDto);
                numChanges++;
            }
        }

        for (Long idToRemove : idsToRemove) {
            certResultDAO.deleteAdditionalSoftwareMapping(idToRemove);
        }
        return numChanges;
    }

    private int updateUcdProcesses(CertificationResult certResult, List<CertifiedProductUcdProcess> existingUcdProcesses,
            List<CertifiedProductUcdProcess> updatedUcdProcesses)
            throws EntityCreationException, EntityRetrievalException, IOException {
        int numChanges = 0;
        List<CertifiedProductUcdProcess> ucdToAdd = new ArrayList<CertifiedProductUcdProcess>();
        List<CertificationResultUcdProcessPair> ucdToUpdate = new ArrayList<CertificationResultUcdProcessPair>();
        List<Long> idsToRemove = new ArrayList<Long>();

        // figure out which ucd processes to add
        if (updatedUcdProcesses != null && updatedUcdProcesses.size() > 0) {
            if (existingUcdProcesses == null || existingUcdProcesses.size() == 0) {
                // existing listing has none, add all from the update
                for (CertifiedProductUcdProcess updatedItem : updatedUcdProcesses) {
                    ucdToAdd.add(updatedItem);
                }
            } else if (existingUcdProcesses.size() > 0) {
                // existing listing has some, compare to the update to see if
                // any are different
                for (CertifiedProductUcdProcess updatedItem : updatedUcdProcesses) {
                    boolean inExistingListing = false;
                    for (CertifiedProductUcdProcess existingItem : existingUcdProcesses) {
                        if (updatedItem.matches(existingItem)) {
                            inExistingListing = true;
                            ucdToUpdate.add(new CertificationResultUcdProcessPair(existingItem, updatedItem));
                        }
                    }

                    if (!inExistingListing) {
                        ucdToAdd.add(updatedItem);
                    }
                }
            }
        }

        // figure out which ucd processes to remove
        if (existingUcdProcesses != null && existingUcdProcesses.size() > 0) {
            // if the updated listing has none, remove them all from existing
            if (updatedUcdProcesses == null || updatedUcdProcesses.size() == 0) {
                for (CertifiedProductUcdProcess existingItem : existingUcdProcesses) {
                    idsToRemove.add(existingItem.getId());
                }
            } else if (updatedUcdProcesses.size() > 0) {
                for (CertifiedProductUcdProcess existingItem : existingUcdProcesses) {
                    boolean inUpdatedListing = false;
                    for (CertifiedProductUcdProcess updatedItem : updatedUcdProcesses) {
                        inUpdatedListing = !inUpdatedListing ? existingItem.matches(updatedItem) : inUpdatedListing;
                    }
                    if (!inUpdatedListing) {
                        idsToRemove.add(existingItem.getId());
                    }
                }
            }
        }

        numChanges = ucdToAdd.size() + idsToRemove.size();

        for (CertifiedProductUcdProcess toAdd : ucdToAdd) {
            CertificationResultUcdProcessDTO toAddDto = new CertificationResultUcdProcessDTO();
            toAddDto.setCertificationResultId(certResult.getId());
            toAddDto.setUcdProcessId(toAdd.getId());
            toAddDto.setUcdProcessDetails(toAdd.getDetails());
            certResultDAO.addUcdProcessMapping(toAddDto);
        }

        for (CertificationResultUcdProcessPair toUpdate : ucdToUpdate) {
            boolean hasChanged = false;
            if (!Objects.equals(toUpdate.getOrig().getDetails(), toUpdate.getUpdated().getDetails())) {
                hasChanged = true;
            }
            if (hasChanged) {
                CertificationResultUcdProcessDTO toUpdateDto = new CertificationResultUcdProcessDTO();
                toUpdateDto.setId(toUpdate.getOrig().getId());
                toUpdateDto.setCertificationResultId(certResult.getId());
                toUpdateDto.setUcdProcessId(toUpdate.getUpdated().getId());
                toUpdateDto.setUcdProcessDetails(toUpdate.getUpdated().getDetails());
                certResultDAO.updateUcdProcessMapping(toUpdateDto);
            }
        }

        for (Long idToRemove : idsToRemove) {
            certResultDAO.deleteUcdProcessMapping(certResult.getId(), idToRemove);
        }
        return numChanges;
    }

    private int updateConformanceMethods(CertifiedProductSearchDetails listing, CertificationResult certResult,
            List<CertificationResultConformanceMethod> existingConformanceMethods,
            List<CertificationResultConformanceMethod> updatedConformanceMethods) throws EntityCreationException {

        int numChanges = 0;

        // Handle added data
        List<CertificationResultConformanceMethod> addedCMs = subtractConformanceMethodLists(
                updatedConformanceMethods != null ? updatedConformanceMethods : new ArrayList<CertificationResultConformanceMethod>(),
                        existingConformanceMethods != null ? existingConformanceMethods : new ArrayList<CertificationResultConformanceMethod>());
        addedCMs.stream()
        .forEach(crcm -> {
            CertificationResultConformanceMethodEntity toAddEntity = new CertificationResultConformanceMethodEntity();
            toAddEntity.setCertificationResultId(certResult.getId());
            toAddEntity.setConformanceMethodId(crcm.getConformanceMethod().getId());
            toAddEntity.setVersion(crcm.getConformanceMethodVersion());
            certResultDAO.addConformanceMethodMapping(toAddEntity);
        });
        numChanges += addedCMs.size();

        //  Handle removed data
        List<CertificationResultConformanceMethod> removedCMs = subtractConformanceMethodLists(
                existingConformanceMethods != null ? existingConformanceMethods : new ArrayList<CertificationResultConformanceMethod>(),
                        updatedConformanceMethods != null ? updatedConformanceMethods : new ArrayList<CertificationResultConformanceMethod>());
        removedCMs.stream()
        .forEach(crcm -> certResultDAO.deleteConformanceMethodMapping(crcm.getId()));
        numChanges += removedCMs.size();

        return numChanges;
    }

    @SuppressWarnings("checkstyle:linelength")
    private List<CertificationResultConformanceMethod> subtractConformanceMethodLists(List<CertificationResultConformanceMethod> listA, List<CertificationResultConformanceMethod> listB) {
        Predicate<CertificationResultConformanceMethod> notInListB = cmFromA -> !listB.stream()
                .anyMatch(cm -> cm.matches(cmFromA));
        return listA.stream()
                .filter(notInListB)
                .collect(Collectors.toList());
    }

    private int updateOptionalStandards(CertifiedProductSearchDetails listing, CertificationResult certResult,
            List<CertificationResultOptionalStandard> existingOptionalStandards,
            List<CertificationResultOptionalStandard> updatedOptionalStandards) throws EntityCreationException {

        int numChanges = 0;
        List<CertificationResultOptionalStandard> optionalStandardToAdd = new ArrayList<CertificationResultOptionalStandard>();
        List<Long> idsToRemove = new ArrayList<Long>();

        // figure out which data to add
        if (updatedOptionalStandards != null && updatedOptionalStandards.size() > 0) {
            if (existingOptionalStandards == null || existingOptionalStandards.size() == 0) {
                // existing listing has none, add all from the update
                for (CertificationResultOptionalStandard updatedItem : updatedOptionalStandards) {
                    optionalStandardToAdd.add(updatedItem);
                }
            } else if (existingOptionalStandards.size() > 0) {
                // existing listing has some, compare to the update to see if
                // any need to be added
                for (CertificationResultOptionalStandard updatedItem : updatedOptionalStandards) {
                    boolean inExistingListing = false;
                    for (CertificationResultOptionalStandard existingItem : existingOptionalStandards) {
                        if (updatedItem.matches(existingItem)) {
                            inExistingListing = true;
                        }
                    }

                    if (!inExistingListing) {
                        optionalStandardToAdd.add(updatedItem);
                    }
                }
            }
        }

        // figure out which data to remove
        if (existingOptionalStandards != null && existingOptionalStandards.size() > 0) {
            // if the updated listing has none, remove them all from existing
            if (updatedOptionalStandards == null || updatedOptionalStandards.size() == 0) {
                for (CertificationResultOptionalStandard existingItem : existingOptionalStandards) {
                    idsToRemove.add(existingItem.getId());
                }
            } else if (updatedOptionalStandards.size() > 0) {
                for (CertificationResultOptionalStandard existingItem : existingOptionalStandards) {
                    boolean inUpdatedListing = false;
                    for (CertificationResultOptionalStandard updatedItem : updatedOptionalStandards) {
                        inUpdatedListing = !inUpdatedListing ? existingItem.matches(updatedItem) : inUpdatedListing;
                    }
                    if (!inUpdatedListing) {
                        idsToRemove.add(existingItem.getId());
                    }
                }
            }
        }

        numChanges = optionalStandardToAdd.size() + idsToRemove.size();
        for (CertificationResultOptionalStandard toAdd : optionalStandardToAdd) {
            CertificationResultOptionalStandardEntity toAddEntity = new CertificationResultOptionalStandardEntity();
            toAddEntity.setCertificationResultId(certResult.getId());
            toAddEntity.setOptionalStandardId(toAdd.getOptionalStandardId());
            certResultDAO.addOptionalStandardMapping(toAddEntity);
        }

        for (Long idToRemove : idsToRemove) {
            certResultDAO.deleteOptionalStandardMapping(idToRemove);
        }
        return numChanges;
    }

    private int updateTestStandards(CertifiedProductSearchDetails listing, CertificationResult certResult,
            List<CertificationResultTestStandard> existingTestStandards,
            List<CertificationResultTestStandard> updatedTestStandards) throws EntityCreationException {
        int numChanges = 0;
        String editionIdString = listing.getCertificationEdition().get(CertifiedProductSearchDetails.EDITION_ID_KEY).toString();
        List<CertificationResultTestStandardDTO> testStandardsToAdd = new ArrayList<CertificationResultTestStandardDTO>();
        List<Long> idsToRemove = new ArrayList<Long>();

        // figure out which test standards to add
        if (updatedTestStandards != null && updatedTestStandards.size() > 0) {
            // fill in potentially missing test standard id
            for (CertificationResultTestStandard updatedItem : updatedTestStandards) {
                if (updatedItem.getTestStandardId() == null
                        && !StringUtils.isEmpty(updatedItem.getTestStandardName())) {
                    TestStandardDTO foundStd = testStandardDAO.getByNumberAndEdition(updatedItem.getTestStandardName(),
                            Long.valueOf(editionIdString));
                    if (foundStd == null) {
                        LOGGER.error("Could not find test standard " + updatedItem.getTestStandardName()
                                + "; will not be adding this as a test standard to certification result id "
                                + certResult.getId() + ", criteria " + certResult.getCriterion().getNumber());
                    } else {
                        updatedItem.setTestStandardId(foundStd.getId());
                    }
                }
            }

            if (existingTestStandards == null || existingTestStandards.size() == 0) {
                // existing listing has none, add all from the update
                for (CertificationResultTestStandard updatedItem : updatedTestStandards) {
                    CertificationResultTestStandardDTO toAdd = new CertificationResultTestStandardDTO();
                    toAdd.setCertificationResultId(certResult.getId());
                    toAdd.setTestStandardId(updatedItem.getTestStandardId());
                    testStandardsToAdd.add(toAdd);
                }
            } else if (existingTestStandards.size() > 0) {
                // existing listing has some, compare to the update to see if
                // any are different
                for (CertificationResultTestStandard updatedItem : updatedTestStandards) {
                    boolean inExistingListing = false;
                    for (CertificationResultTestStandard existingItem : existingTestStandards) {
                        inExistingListing = !inExistingListing ? updatedItem.matches(existingItem) : inExistingListing;
                    }

                    if (!inExistingListing) {
                        CertificationResultTestStandardDTO toAdd = new CertificationResultTestStandardDTO();
                        toAdd.setCertificationResultId(certResult.getId());
                        toAdd.setTestStandardId(updatedItem.getTestStandardId());
                        testStandardsToAdd.add(toAdd);
                    }
                }
            }
        }

        // figure out which ucd processes to remove
        if (existingTestStandards != null && existingTestStandards.size() > 0) {
            // if the updated listing has none, remove them all from existing
            if (updatedTestStandards == null || updatedTestStandards.size() == 0) {
                for (CertificationResultTestStandard existingItem : existingTestStandards) {
                    idsToRemove.add(existingItem.getId());
                }
            } else if (updatedTestStandards.size() > 0) {
                for (CertificationResultTestStandard existingItem : existingTestStandards) {
                    boolean inUpdatedListing = false;
                    for (CertificationResultTestStandard updatedItem : updatedTestStandards) {
                        inUpdatedListing = !inUpdatedListing ? existingItem.matches(updatedItem) : inUpdatedListing;
                    }
                    if (!inUpdatedListing) {
                        idsToRemove.add(existingItem.getId());
                    }
                }
            }
        }

        numChanges = testStandardsToAdd.size() + idsToRemove.size();
        for (CertificationResultTestStandardDTO toAdd : testStandardsToAdd) {
            certResultDAO.addTestStandardMapping(toAdd);
        }

        for (Long idToRemove : idsToRemove) {
            certResultDAO.deleteTestStandardMapping(idToRemove);
        }
        return numChanges;
    }

    //TODO: OCD-4242 - Is there a better way to do this!
    private int updateTestTools(CertificationResult certResult, List<CertificationResultTestTool> existingTestTools,
            List<CertificationResultTestTool> updatedTestTools) throws EntityCreationException {
        int numChanges = 0;
        List<CertificationResultTestTool> testToolsToAdd = new ArrayList<CertificationResultTestTool>();
        List<Long> idsToRemove = new ArrayList<Long>();

        // figure out which test tools to add
        if (updatedTestTools != null && updatedTestTools.size() > 0) {
            // fill in potentially missing test standard id
            for (CertificationResultTestTool updatedItem : updatedTestTools) {
                if (updatedItem.getTestToolId() == null && !StringUtils.isEmpty(updatedItem.getValue())) {
                    TestTool foundTool = testToolDAO.getByName(updatedItem.getValue());
                    if (foundTool == null) {
                        LOGGER.error("Could not find test tool " + updatedItem.getValue()
                                + "; will not be adding this as a test tool to certification result id "
                                + certResult.getId() + ", criteria " + certResult.getCriterion().getNumber());
                    } else {
                        updatedItem.setTestTool(foundTool);
                        updatedItem.setVersion(updatedItem.getVersion());
                    }
                }
            }

            if (existingTestTools == null || existingTestTools.size() == 0) {
                // existing listing has none, add all from the update
                for (CertificationResultTestTool updatedItem : updatedTestTools) {
                    if (updatedItem.getTestToolId() != null) {
                        testToolsToAdd.add(CertificationResultTestTool.builder()
                                .certificationResultId(certResult.getId())
                                .testTool(testToolDAO.getById(updatedItem.getTestToolId()))
                                .version(updatedItem.getVersion())
                                .build());
                    }
                }
            } else if (existingTestTools.size() > 0) {
                // existing listing has some, compare to the update to see if
                // any are different
                for (CertificationResultTestTool updatedItem : updatedTestTools) {
                    boolean inExistingListing = false;
                    for (CertificationResultTestTool existingItem : existingTestTools) {
                        inExistingListing = !inExistingListing ? updatedItem.matches(existingItem) : inExistingListing;
                    }

                    if (!inExistingListing) {
                        if (updatedItem.getTestToolId() != null) {
                            testToolsToAdd.add(CertificationResultTestTool.builder()
                                    .certificationResultId(certResult.getId())
                                    .testTool(testToolDAO.getById(updatedItem.getTestToolId()))
                                    .version(updatedItem.getVersion())
                                    .build());
                        }
                    }
                }
            }
        }

        // figure out which test tools to remove
        if (existingTestTools != null && existingTestTools.size() > 0) {
            // if the updated listing has none, remove them all from existing
            if (updatedTestTools == null || updatedTestTools.size() == 0) {
                for (CertificationResultTestTool existingItem : existingTestTools) {
                    idsToRemove.add(existingItem.getId());
                }
            } else if (updatedTestTools.size() > 0) {
                for (CertificationResultTestTool existingItem : existingTestTools) {
                    boolean inUpdatedListing = false;
                    for (CertificationResultTestTool updatedItem : updatedTestTools) {
                        inUpdatedListing = !inUpdatedListing ? existingItem.matches(updatedItem) : inUpdatedListing;
                    }
                    if (!inUpdatedListing) {
                        idsToRemove.add(existingItem.getId());
                    }
                }
            }
        }

        numChanges = testToolsToAdd.size() + idsToRemove.size();
        for (CertificationResultTestTool toAdd : testToolsToAdd) {
            certResultDAO.addTestToolMapping(toAdd);
        }

        for (Long idToRemove : idsToRemove) {
            certResultDAO.deleteTestToolMapping(idToRemove);
        }
        return numChanges;
    }

    private int updateTestData(CertificationResult certResult, List<CertificationResultTestData> existingTestData,
            List<CertificationResultTestData> updatedTestData)
            throws EntityCreationException, EntityRetrievalException {
        int numChanges = 0;
        List<CertificationResultTestData> testDataToAdd = new ArrayList<CertificationResultTestData>();
        List<CertificationResultTestDataPair> testDataToUpdate = new ArrayList<CertificationResultTestDataPair>();
        List<Long> idsToRemove = new ArrayList<Long>();

        // figure out which test data to add
        if (updatedTestData != null && updatedTestData.size() > 0) {
            if (existingTestData == null || existingTestData.size() == 0) {
                // existing listing has none, add all from the update
                for (CertificationResultTestData updatedItem : updatedTestData) {
                    testDataToAdd.add(updatedItem);
                }
            } else if (existingTestData.size() > 0) {
                // existing listing has some, compare to the update to see if
                // any are different
                for (CertificationResultTestData updatedItem : updatedTestData) {
                    boolean inExistingListing = false;
                    for (CertificationResultTestData existingItem : existingTestData) {
                        if (updatedItem.matches(existingItem)) {
                            inExistingListing = true;
                            testDataToUpdate.add(new CertificationResultTestDataPair(existingItem, updatedItem));
                        }
                    }

                    if (!inExistingListing) {
                        testDataToAdd.add(updatedItem);
                    }
                }
            }
        }

        // figure out which test data to remove
        if (existingTestData != null && existingTestData.size() > 0) {
            // if the updated listing has none, remove them all from existing
            if (updatedTestData == null || updatedTestData.size() == 0) {
                for (CertificationResultTestData existingItem : existingTestData) {
                    idsToRemove.add(existingItem.getId());
                }
            } else if (updatedTestData.size() > 0) {
                for (CertificationResultTestData existingItem : existingTestData) {
                    boolean inUpdatedListing = false;
                    for (CertificationResultTestData updatedItem : updatedTestData) {
                        inUpdatedListing = !inUpdatedListing ? existingItem.matches(updatedItem) : inUpdatedListing;
                    }
                    if (!inUpdatedListing) {
                        idsToRemove.add(existingItem.getId());
                    }
                }
            }
        }

        numChanges = testDataToAdd.size() + idsToRemove.size();
        for (CertificationResultTestData toAdd : testDataToAdd) {
            CertificationResultTestDataDTO toAddDto = new CertificationResultTestDataDTO();
            toAddDto.setCertificationResultId(certResult.getId());
            toAddDto.setTestDataId(toAdd.getTestData().getId());
            toAddDto.setAlteration(toAdd.getAlteration());
            toAddDto.setVersion(toAdd.getVersion());
            certResultDAO.addTestDataMapping(toAddDto);
        }

        for (CertificationResultTestDataPair toUpdate : testDataToUpdate) {
            boolean hasChanged = false;
            if (!Objects.equals(toUpdate.getOrig().getTestData().getId(),
                    toUpdate.getUpdated().getTestData().getId())
                    || !Objects.equals(toUpdate.getOrig().getAlteration(), toUpdate.getUpdated().getAlteration())
                    || !Objects.equals(toUpdate.getOrig().getVersion(), toUpdate.getUpdated().getVersion())) {
                hasChanged = true;
            }

            if (hasChanged) {
                CertificationResultTestDataDTO toUpdateDto = new CertificationResultTestDataDTO();
                toUpdateDto.setId(toUpdate.getOrig().getId());
                toUpdateDto.setCertificationResultId(certResult.getId());
                toUpdateDto.setTestDataId(toUpdate.getUpdated().getTestData().getId());
                toUpdateDto.setAlteration(toUpdate.getUpdated().getAlteration());
                toUpdateDto.setVersion(toUpdate.getUpdated().getVersion());
                certResultDAO.updateTestDataMapping(toUpdateDto);
                numChanges++;
            }
        }

        for (Long idToRemove : idsToRemove) {
            certResultDAO.deleteTestDataMapping(idToRemove);
        }
        return numChanges;
    }

    private int updateTestProcedures(CertificationResult certResult,
            List<CertificationResultTestProcedure> existingTestProcedures,
            List<CertificationResultTestProcedure> updatedTestProcedures) throws EntityCreationException {
        int numChanges = 0;
        List<CertificationResultTestProcedureDTO> testProceduresToAdd = new ArrayList<CertificationResultTestProcedureDTO>();
        List<Long> idsToRemove = new ArrayList<Long>();

        // figure out which test procedures to add
        if (updatedTestProcedures != null && updatedTestProcedures.size() > 0) {
            if (existingTestProcedures == null || existingTestProcedures.size() == 0) {
                // existing listing has none, add all from the update
                for (CertificationResultTestProcedure updatedItem : updatedTestProcedures) {
                    CertificationResultTestProcedureDTO toAdd = new CertificationResultTestProcedureDTO();
                    toAdd.setCertificationResultId(certResult.getId());
                    toAdd.setVersion(updatedItem.getTestProcedureVersion());
                    toAdd.setTestProcedureId(updatedItem.getTestProcedure().getId());
                    testProceduresToAdd.add(toAdd);
                }
            } else if (existingTestProcedures.size() > 0) {
                // existing listing has some, compare to the update to see if
                // any are different
                for (CertificationResultTestProcedure updatedItem : updatedTestProcedures) {
                    boolean inExistingListing = false;
                    for (CertificationResultTestProcedure existingItem : existingTestProcedures) {
                        inExistingListing = !inExistingListing ? updatedItem.matches(existingItem) : inExistingListing;
                    }

                    if (!inExistingListing) {
                        CertificationResultTestProcedureDTO toAdd = new CertificationResultTestProcedureDTO();
                        toAdd.setCertificationResultId(certResult.getId());
                        toAdd.setVersion(updatedItem.getTestProcedureVersion());
                        toAdd.setTestProcedureId(updatedItem.getTestProcedure().getId());
                        testProceduresToAdd.add(toAdd);
                    }
                }
            }
        }

        // figure out which test data to remove
        if (existingTestProcedures != null && existingTestProcedures.size() > 0) {
            // if the updated listing has none, remove them all from existing
            if (updatedTestProcedures == null || updatedTestProcedures.size() == 0) {
                for (CertificationResultTestProcedure existingItem : existingTestProcedures) {
                    idsToRemove.add(existingItem.getId());
                }
            } else if (updatedTestProcedures.size() > 0) {
                for (CertificationResultTestProcedure existingItem : existingTestProcedures) {
                    boolean inUpdatedListing = false;
                    for (CertificationResultTestProcedure updatedItem : updatedTestProcedures) {
                        inUpdatedListing = !inUpdatedListing ? existingItem.matches(updatedItem) : inUpdatedListing;
                    }
                    if (!inUpdatedListing) {
                        idsToRemove.add(existingItem.getId());
                    }
                }
            }
        }

        numChanges = testProceduresToAdd.size() + idsToRemove.size();
        for (CertificationResultTestProcedureDTO toAdd : testProceduresToAdd) {
            certResultDAO.addTestProcedureMapping(toAdd);
        }

        for (Long idToRemove : idsToRemove) {
            certResultDAO.deleteTestProcedureMapping(idToRemove);
        }
        return numChanges;
    }

    private int updateFunctionalitiesTested(CertifiedProductSearchDetails listing, CertificationResult certResult,
            List<CertificationResultFunctionalityTested> existingFunctionalitiesTested,
            List<CertificationResultFunctionalityTested> updatedFunctionalitiesTested) throws EntityCreationException {
        int numChanges = 0;
        List<CertificationResultFunctionalityTested> functionalitiesTestedToAdd = new ArrayList<CertificationResultFunctionalityTested>();
        List<Long> idsToRemove = new ArrayList<Long>();

        // figure out which functionalities tested to add
        if (updatedFunctionalitiesTested != null && updatedFunctionalitiesTested.size() > 0) {
            // fill in potentially missing functionality tested id
            for (CertificationResultFunctionalityTested updatedItem : updatedFunctionalitiesTested) {
                if (updatedItem.getFunctionalityTestedId() == null && !StringUtils.isEmpty(updatedItem.getName())) {
                    FunctionalityTested foundFunc = getFunctionalityTested(updatedItem.getName(), certResult.getCriterion().getId());
                    if (foundFunc == null) {
                        LOGGER.error("Could not find functionality tested " + updatedItem.getName()
                                + "; will not be adding this as a functionality tested to listing id " + listing.getId()
                                + ", criteria " + Util.formatCriteriaNumber(certResult.getCriterion()));
                    } else {
                        updatedItem.setFunctionalityTestedId(foundFunc.getId());
                    }
                }
            }

            if (existingFunctionalitiesTested == null || existingFunctionalitiesTested.size() == 0) {
                // existing listing has none, add all from the update
                for (CertificationResultFunctionalityTested updatedItem : updatedFunctionalitiesTested) {
                    if (updatedItem.getFunctionalityTestedId() != null) {
                        CertificationResultFunctionalityTested toAdd = new CertificationResultFunctionalityTested();
                        toAdd.setCertificationResultId(certResult.getId());
                        toAdd.setFunctionalityTestedId(updatedItem.getFunctionalityTestedId());
                        functionalitiesTestedToAdd.add(toAdd);
                    }
                }
            } else if (existingFunctionalitiesTested.size() > 0) {
                // existing listing has some, compare to the update to see if
                // any are different
                for (CertificationResultFunctionalityTested updatedItem : updatedFunctionalitiesTested) {
                    boolean inExistingListing = false;
                    for (CertificationResultFunctionalityTested existingItem : existingFunctionalitiesTested) {
                        inExistingListing = !inExistingListing ? updatedItem.matches(existingItem) : inExistingListing;
                    }

                    if (!inExistingListing) {
                        if (updatedItem.getFunctionalityTestedId() != null) {
                            CertificationResultFunctionalityTested toAdd = new CertificationResultFunctionalityTested();
                            toAdd.setCertificationResultId(certResult.getId());
                            toAdd.setFunctionalityTestedId(updatedItem.getFunctionalityTestedId());
                            functionalitiesTestedToAdd.add(toAdd);
                        }
                    }
                }
            }
        }

        // figure out which functionality tested to remove
        if (existingFunctionalitiesTested != null && existingFunctionalitiesTested.size() > 0) {
            // if the updated listing has none, remove them all from existing
            if (updatedFunctionalitiesTested == null || updatedFunctionalitiesTested.size() == 0) {
                for (CertificationResultFunctionalityTested existingItem : existingFunctionalitiesTested) {
                    idsToRemove.add(existingItem.getId());
                }
            } else if (updatedFunctionalitiesTested.size() > 0) {
                for (CertificationResultFunctionalityTested existingItem : existingFunctionalitiesTested) {
                    boolean inUpdatedListing = false;
                    for (CertificationResultFunctionalityTested updatedItem : updatedFunctionalitiesTested) {
                        inUpdatedListing = !inUpdatedListing ? existingItem.matches(updatedItem) : inUpdatedListing;
                    }
                    if (!inUpdatedListing) {
                        idsToRemove.add(existingItem.getId());
                    }
                }
            }
        }

        numChanges = functionalitiesTestedToAdd.size() + idsToRemove.size();
        for (CertificationResultFunctionalityTested toAdd : functionalitiesTestedToAdd) {
            certResultFuncTestedDao.addFunctionalityTestedMapping(toAdd);
        }

        for (Long idToRemove : idsToRemove) {
            certResultFuncTestedDao.deleteFunctionalityTestedMapping(idToRemove);
        }
        return numChanges;
    }

    private FunctionalityTested getFunctionalityTested(String functionalityTestedNumber, Long criterionId) {
        Map<Long, List<FunctionalityTested>> funcTestedMappings = functionalityTestedDao.getFunctionalitiesTestedCriteriaMaps();
        if (!funcTestedMappings.containsKey(criterionId)) {
            return null;
        }
        List<FunctionalityTested> functionalityTestedForCriterion = funcTestedMappings.get(criterionId);
        Optional<FunctionalityTested> funcTestedOpt = functionalityTestedForCriterion.stream()
            .filter(funcTested -> funcTested.getName().equalsIgnoreCase(functionalityTestedNumber))
            .findAny();
        return funcTestedOpt.isPresent() ? funcTestedOpt.get() : null;
    }

    private int updateTestTasks(CertificationResult certResult, List<TestTask> existingTestTasks,
            List<TestTask> updatedTestTasks) throws EntityCreationException, EntityRetrievalException {
        int numChanges = 0;
        Set<TestTask> testTasksToAdd = new HashSet<TestTask>();
        Set<TestTaskPair> testTasksToUpdate = new HashSet<TestTaskPair>();
        Set<TestTask> testTasksToRemove = new HashSet<TestTask>();

        // figure out which test tasks to add
        if (updatedTestTasks != null && updatedTestTasks.size() > 0) {
            if (existingTestTasks == null || existingTestTasks.size() == 0) {
                // existing listing has none, add all from the update
                for (TestTask updatedItem : updatedTestTasks) {
                    testTasksToAdd.add(updatedItem);
                }
            } else if (existingTestTasks.size() > 0) {
                // existing listing has some, compare to the update to see if
                // any are different
                for (TestTask updatedItem : updatedTestTasks) {
                    boolean inExistingListing = false;
                    for (TestTask existingItem : existingTestTasks) {
                        if (!inExistingListing && updatedItem.matches(existingItem)) {
                            inExistingListing = true;
                            testTasksToUpdate.add(new TestTaskPair(existingItem, updatedItem));
                        }
                    }

                    if (!inExistingListing) {
                        testTasksToAdd.add(updatedItem);
                    }
                }
            }
        }

        // figure out which test tasks to remove
        if (existingTestTasks != null && existingTestTasks.size() > 0) {
            // if the updated listing has none, remove them all from existing
            if (updatedTestTasks == null || updatedTestTasks.size() == 0) {
                for (TestTask existingItem : existingTestTasks) {
                    testTasksToRemove.add(existingItem);
                }
            } else if (updatedTestTasks.size() > 0) {
                for (TestTask existingItem : existingTestTasks) {
                    boolean inUpdatedListing = false;
                    for (TestTask updatedItem : updatedTestTasks) {
                        if (!inUpdatedListing && updatedItem.matches(existingItem)) {
                            inUpdatedListing = true;
                        }
                    }
                    if (!inUpdatedListing) {
                        testTasksToRemove.add(existingItem);
                    }
                }
            }
        }

        for (TestTask toAdd : testTasksToAdd) {
            numChanges += createTestTask(certResult, toAdd);
        }

        for (TestTask toRemove : testTasksToRemove) {
            numChanges += deleteTestTask(certResult, toRemove);
        }

        for (TestTaskPair toUpdate : testTasksToUpdate) {
            numChanges += updateTestTask(certResult, toUpdate.getOrig(), toUpdate.getUpdated());
        }

        return numChanges;
    }

    private int createTestTask(CertificationResult certResult, TestTask task)
            throws EntityCreationException, EntityRetrievalException {
        int numChanges = 1;
        TestTaskDTO taskToCreate = convert(task);
        TestTaskDTO createdTask = testTaskDAO.create(taskToCreate);
        taskToCreate.setId(createdTask.getId());

        CertificationResultTestTaskDTO certResultTask = new CertificationResultTestTaskDTO();
        certResultTask.setCertificationResultId(certResult.getId());
        certResultTask.setTestTaskId(taskToCreate.getId());
        certResultTask.setTestTask(taskToCreate);
        certResultTask = certResultDAO.addTestTaskMapping(certResultTask);

        return numChanges;
    }

    private int deleteTestTask(CertificationResult certResult, TestTask task)
            throws EntityCreationException, EntityRetrievalException {
        int numChanges = 0;
        certResultDAO.deleteTestTaskMapping(certResult.getId(), task.getId());
        numChanges++;
        return numChanges;
    }

    private int updateTestTask(CertificationResult certResult, TestTask existingTask, TestTask updatedTask)
            throws EntityRetrievalException, EntityCreationException {
        int numChanges = 0;
        boolean isDifferent = false;
        if (!StringUtils.equals(existingTask.getDescription(), updatedTask.getDescription())
                || !StringUtils.equals(existingTask.getTaskRatingScale(), updatedTask.getTaskRatingScale())
                || !Objects.equals(existingTask.getTaskErrors(), updatedTask.getTaskErrors())
                || !Objects.equals(existingTask.getTaskErrorsStddev(), updatedTask.getTaskErrorsStddev())
                || !Objects.equals(existingTask.getTaskPathDeviationObserved(),
                        updatedTask.getTaskPathDeviationObserved())
                || !Objects.equals(existingTask.getTaskPathDeviationOptimal(),
                        updatedTask.getTaskPathDeviationOptimal())
                || !Objects.equals(existingTask.getTaskRating(), updatedTask.getTaskRating())
                || !StringUtils.equals(existingTask.getTaskRatingScale(), updatedTask.getTaskRatingScale())
                || !Objects.equals(existingTask.getTaskRatingStddev(), updatedTask.getTaskRatingStddev())
                || !Objects.equals(existingTask.getTaskSuccessAverage(), updatedTask.getTaskSuccessAverage())
                || !Objects.equals(existingTask.getTaskSuccessStddev(), updatedTask.getTaskSuccessStddev())
                || !Objects.equals(existingTask.getTaskTimeAvg(), updatedTask.getTaskTimeAvg())
                || !Objects.equals(existingTask.getTaskTimeDeviationObservedAvg(),
                        updatedTask.getTaskTimeDeviationObservedAvg())
                || !Objects.equals(existingTask.getTaskTimeDeviationOptimalAvg(),
                        updatedTask.getTaskTimeDeviationOptimalAvg())
                || !Objects.equals(existingTask.getTaskTimeStddev(), updatedTask.getTaskTimeStddev())) {
            isDifferent = true;
        }

        if (isDifferent) {
            TestTaskDTO taskToUpdate = convert(updatedTask);
            taskToUpdate.setId(existingTask.getId());
            testTaskDAO.update(taskToUpdate);
            numChanges++;
        }

        numChanges += updateTaskParticipants(existingTask, existingTask.getTestParticipants(),
                updatedTask.getTestParticipants());
        return numChanges;
    }

    private int updateTaskParticipants(TestTask testTask, Collection<TestParticipant> existingParticipants,
            Collection<TestParticipant> updatedParticipants) throws EntityCreationException, EntityRetrievalException {
        int numChanges = 0;
        Set<TestParticipantDTO> participantsToAdd = new HashSet<TestParticipantDTO>();
        Set<TestParticipantPair> participantsToUpdate = new HashSet<TestParticipantPair>();
        Set<Long> idsToRemove = new HashSet<Long>();

        // figure out which participants to add
        if (updatedParticipants != null && updatedParticipants.size() > 0) {
            // fill in potentially missing participant id
            for (TestParticipant updatedItem : updatedParticipants) {
                if (updatedItem.getId() == null) {
                    TestParticipantDTO participantToAdd = convert(updatedItem);
                    TestParticipantDTO addedParticipant = testParticipantDAO.create(participantToAdd);
                    updatedItem.setId(addedParticipant.getId());
                }
            }

            if (existingParticipants == null || existingParticipants.size() == 0) {
                // existing listing has none, add all from the update
                for (TestParticipant updatedItem : updatedParticipants) {
                    if (updatedItem.getId() != null) {
                        TestParticipantDTO toAdd = new TestParticipantDTO();
                        toAdd.setId(updatedItem.getId());
                        participantsToAdd.add(toAdd);
                    }
                }
            } else if (existingParticipants.size() > 0) {
                // existing listing has some, compare to the update to see if
                // any are different
                for (TestParticipant updatedItem : updatedParticipants) {
                    boolean inExistingListing = false;
                    for (TestParticipant existingItem : existingParticipants) {
                        inExistingListing = !inExistingListing ? updatedItem.matches(existingItem) : inExistingListing;
                        if (updatedItem.getId().longValue() == existingItem.getId().longValue()) {
                            participantsToUpdate.add(new TestParticipantPair(existingItem, updatedItem));
                        }
                    }

                    if (!inExistingListing) {
                        if (updatedItem.getId() != null) {
                            TestParticipantDTO toAdd = new TestParticipantDTO();
                            toAdd.setId(updatedItem.getId());
                            participantsToAdd.add(toAdd);
                        }
                    }
                }
            }
        }

        // figure out which participants to remove
        if (existingParticipants != null && existingParticipants.size() > 0) {
            // if the updated listing has none, remove them all from existing
            if (updatedParticipants == null || updatedParticipants.size() == 0) {
                for (TestParticipant existingItem : existingParticipants) {
                    idsToRemove.add(existingItem.getId());
                }
            } else if (updatedParticipants.size() > 0) {
                for (TestParticipant existingItem : existingParticipants) {
                    boolean inUpdatedListing = false;
                    for (TestParticipant updatedItem : updatedParticipants) {
                        inUpdatedListing = !inUpdatedListing ? existingItem.matches(updatedItem) : inUpdatedListing;
                    }
                    if (!inUpdatedListing) {
                        idsToRemove.add(existingItem.getId());
                    }
                }
            }
        }

        numChanges = participantsToAdd.size() + idsToRemove.size();

        for (TestParticipantDTO participantToAdd : participantsToAdd) {
            certResultDAO.addTestParticipantMapping(convert(testTask), participantToAdd);
        }

        for (TestParticipantPair toUpdate : participantsToUpdate) {
            boolean isDifferent = false;
            TestParticipant existingPart = toUpdate.getOrig();
            TestParticipant updatedPart = toUpdate.getUpdated();
            if (!StringUtils.equals(existingPart.getAgeRange(), updatedPart.getAgeRange())
                    || !StringUtils.equals(existingPart.getAssistiveTechnologyNeeds(),
                            updatedPart.getAssistiveTechnologyNeeds())
                    || !Objects.equals(existingPart.getComputerExperienceMonths(),
                            updatedPart.getComputerExperienceMonths())
                    || !StringUtils.equals(existingPart.getEducationTypeName(), updatedPart.getEducationTypeName())
                    || !StringUtils.equals(existingPart.getGender(), updatedPart.getGender())
                    || !StringUtils.equals(existingPart.getOccupation(), updatedPart.getOccupation())
                    || !Objects.equals(existingPart.getProductExperienceMonths(),
                            updatedPart.getProductExperienceMonths())
                    || !Objects.equals(existingPart.getProfessionalExperienceMonths(),
                            updatedPart.getProfessionalExperienceMonths())) {
                isDifferent = true;
            }

            if (isDifferent) {
                TestParticipantDTO toUpdateDto = convert(toUpdate.getUpdated());
                testParticipantDAO.update(toUpdateDto);
                numChanges++;
            }
        }

        for (Long idToRemove : idsToRemove) {
            certResultDAO.deleteTestParticipantMapping(testTask.getId(), idToRemove);
        }
        return numChanges;
    }

    private int updateSvap(CertificationResult certResult, List<CertificationResultSvap> existingSvaps,
            List<CertificationResultSvap> updatedSvaps) {
        int updates = 0;

        //Get added SVAPs
        List<CertificationResultSvap> addedSvaps = subtractLists(
                updatedSvaps != null ? updatedSvaps : new ArrayList<CertificationResultSvap>(),
                existingSvaps != null ? existingSvaps : new ArrayList<CertificationResultSvap>());

        addedSvaps.stream()
                .forEach(crs -> certResultDAO.addCertificationResultSvap(crs, certResult.getId()));
        updates += addedSvaps.size();


        //Get removed SVAPs
        List<CertificationResultSvap> removedSvaps = subtractLists(
                existingSvaps != null ? existingSvaps : new ArrayList<CertificationResultSvap>(),
                updatedSvaps != null ? updatedSvaps : new ArrayList<CertificationResultSvap>());

        removedSvaps.stream()
                .forEach(crs -> certResultDAO.deleteCertificationResultSvap(crs));
        updates += removedSvaps.size();

        return updates;
    }

    @SuppressWarnings("checkstyle:linelength")
    private List<CertificationResultSvap> subtractLists(List<CertificationResultSvap> listA, List<CertificationResultSvap> listB) {
        Predicate<CertificationResultSvap> notInListB = svapFromA -> !listB.stream()
                .anyMatch(svap -> svap.getSvapId().equals(svapFromA.getSvapId()));
        return listA.stream()
                .filter(notInListB)
                .collect(Collectors.toList());
    }

    private CertificationResultAdditionalSoftwareDTO convert(Long certResultId,
            CertificationResultAdditionalSoftware orig) {
        CertificationResultAdditionalSoftwareDTO result = new CertificationResultAdditionalSoftwareDTO();
        result.setId(orig.getId());
        result.setCertificationResultId(certResultId);
        result.setCertifiedProductId(orig.getCertifiedProductId());
        result.setCertifiedProductNumber(orig.getCertifiedProductNumber());
        result.setGrouping(orig.getGrouping());
        result.setJustification(orig.getJustification());
        result.setName(orig.getName());
        result.setVersion(orig.getVersion());
        return result;
    }

    private TestTaskDTO convert(TestTask task) {
        TestTaskDTO result = new TestTaskDTO();
        result.setId(task.getId());
        result.setDescription(task.getDescription());
        result.setTaskErrors(task.getTaskErrors());
        result.setTaskErrorsStddev(task.getTaskErrorsStddev());
        result.setTaskPathDeviationObserved(task.getTaskPathDeviationObserved());
        result.setTaskPathDeviationOptimal(task.getTaskPathDeviationOptimal());
        result.setTaskRating(task.getTaskRating());
        result.setTaskRatingScale(task.getTaskRatingScale());
        result.setTaskRatingStddev(task.getTaskRatingStddev());
        result.setTaskSuccessAverage(task.getTaskSuccessAverage());
        result.setTaskSuccessStddev(task.getTaskSuccessStddev());
        result.setTaskTimeAvg(task.getTaskTimeAvg());
        result.setTaskTimeDeviationObservedAvg(task.getTaskTimeDeviationObservedAvg());
        result.setTaskTimeDeviationOptimalAvg(task.getTaskTimeDeviationOptimalAvg());
        result.setTaskTimeStddev(task.getTaskTimeStddev());
        if (task.getTestParticipants() != null && task.getTestParticipants().size() > 0) {
            for (TestParticipant part : task.getTestParticipants()) {
                result.getParticipants().add(convert(part));
            }
        }
        return result;
    }

    private TestParticipantDTO convert(TestParticipant domain) {
        TestParticipantDTO result = new TestParticipantDTO();
        result.setId(domain.getId());
        result.setAssistiveTechnologyNeeds(domain.getAssistiveTechnologyNeeds());
        result.setComputerExperienceMonths(domain.getComputerExperienceMonths());
        result.setGender(domain.getGender());
        result.setOccupation(domain.getOccupation());
        result.setProductExperienceMonths(domain.getProductExperienceMonths());
        result.setProfessionalExperienceMonths(domain.getProfessionalExperienceMonths());
        if (domain.getAgeRangeId() == null && !StringUtils.isEmpty(domain.getAgeRange())) {
            AgeRangeDTO age = ageDao.getByName(domain.getAgeRange());
            if (age != null) {
                result.setAgeRangeId(age.getId());
            } else {
                LOGGER.error("Could not find matching age range for " + domain.getAgeRange());
            }
        } else if (domain.getAgeRangeId() != null) {
            result.setAgeRangeId(domain.getAgeRangeId());
        }

        if (domain.getEducationTypeId() == null && !StringUtils.isEmpty(domain.getEducationTypeName())) {
            EducationTypeDTO educ = educDao.getByName(domain.getEducationTypeName());
            if (educ != null) {
                result.setEducationTypeId(educ.getId());
            } else {
                LOGGER.error("Could not find matching education level " + domain.getEducationTypeName());
            }
        } else if (domain.getEducationTypeId() != null) {
            result.setEducationTypeId(domain.getEducationTypeId());
        }
        return result;
    }

    public List<CertificationResultAdditionalSoftwareDTO> getAdditionalSoftwareMappingsForCertificationResult(
            Long certificationResultId) {
        return certResultDAO.getAdditionalSoftwareForCertificationResult(certificationResultId);
    }

    public boolean getCertifiedProductHasAdditionalSoftware(Long certifiedProductId) {
        return certResultDAO.getCertifiedProductHasAdditionalSoftware(certifiedProductId);
    }

    public List<CertificationResultUcdProcessDTO> getUcdProcessesForCertificationResult(Long certificationResultId) {
        return certResultDAO.getUcdProcessesForCertificationResult(certificationResultId);
    }

    public List<CertificationResultTestStandardDTO> getTestStandardsForCertificationResult(Long certificationResultId) {
        return certResultDAO.getTestStandardsForCertificationResult(certificationResultId);
    }

    public List<CertificationResultTestTool> getTestToolsForCertificationResult(Long certificationResultId) {
        return certResultDAO.getTestToolsForCertificationResult(certificationResultId);
    }

    public List<CertificationResultTestDataDTO> getTestDataForCertificationResult(Long certificationResultId) {
        return certResultDAO.getTestDataForCertificationResult(certificationResultId);
    }

    public List<CertificationResultTestProcedureDTO> getTestProceduresForCertificationResult(
            Long certificationResultId) {
        return certResultDAO.getTestProceduresForCertificationResult(certificationResultId);
    }

    public List<CertificationResultFunctionalityTested> getFunctionalitiesTestedForCertificationResult(
            Long certificationResultId) {
        return certResultFuncTestedDao.getFunctionalitiesTestedForCertificationResult(certificationResultId);
    }

    public List<CertificationResultTestTaskDTO> getTestTasksForCertificationResult(Long certificationResultId) {
        return certResultDAO.getTestTasksForCertificationResult(certificationResultId);
    }

    public List<CertificationResultSvap> getSvapsForCertificationResult(Long certificationResultId) {
        return certResultDAO.getSvapForCertificationResult(certificationResultId);
    }

    private static class CertificationResultAdditionalSoftwarePair {
        private CertificationResultAdditionalSoftware orig;
        private CertificationResultAdditionalSoftware updated;

        CertificationResultAdditionalSoftwarePair(final CertificationResultAdditionalSoftware orig,
                final CertificationResultAdditionalSoftware updated) {
            this.orig = orig;
            this.updated = updated;
        }

        public CertificationResultAdditionalSoftware getOrig() {
            return orig;
        }

        public void setOrig(final CertificationResultAdditionalSoftware orig) {
            this.orig = orig;
        }

        public CertificationResultAdditionalSoftware getUpdated() {
            return updated;
        }

        public void setUpdated(final CertificationResultAdditionalSoftware updated) {
            this.updated = updated;
        }
    }

    private static class CertificationResultUcdProcessPair {
        private CertifiedProductUcdProcess orig;
        private CertifiedProductUcdProcess updated;

        CertificationResultUcdProcessPair(final CertifiedProductUcdProcess orig, final CertifiedProductUcdProcess updated) {
            this.orig = orig;
            this.updated = updated;
        }

        public CertifiedProductUcdProcess getOrig() {
            return orig;
        }

        public void setOrig(final CertifiedProductUcdProcess orig) {
            this.orig = orig;
        }

        public CertifiedProductUcdProcess getUpdated() {
            return updated;
        }

        public void setUpdated(final CertifiedProductUcdProcess updated) {
            this.updated = updated;
        }
    }

    private static class CertificationResultTestDataPair {
        private CertificationResultTestData orig;
        private CertificationResultTestData updated;

        CertificationResultTestDataPair(final CertificationResultTestData orig,
                final CertificationResultTestData updated) {
            this.orig = orig;
            this.updated = updated;
        }

        public CertificationResultTestData getOrig() {
            return orig;
        }

        public void setOrig(final CertificationResultTestData orig) {
            this.orig = orig;
        }

        public CertificationResultTestData getUpdated() {
            return updated;
        }

        public void setUpdated(final CertificationResultTestData updated) {
            this.updated = updated;
        }
    }

    private static class TestTaskPair {
        private TestTask orig;
        private TestTask updated;

        TestTaskPair(final TestTask orig, final TestTask updated) {
            this.orig = orig;
            this.updated = updated;
        }

        public TestTask getOrig() {
            return orig;
        }

        public void setOrig(final TestTask orig) {
            this.orig = orig;
        }

        public TestTask getUpdated() {
            return updated;
        }

        public void setUpdated(final TestTask updated) {
            this.updated = updated;
        }
    }

    private static class TestParticipantPair {
        private TestParticipant orig;
        private TestParticipant updated;

        TestParticipantPair(final TestParticipant orig, final TestParticipant updated) {
            this.orig = orig;
            this.updated = updated;
        }

        public TestParticipant getOrig() {
            return orig;
        }

        public void setOrig(final TestParticipant orig) {
            this.orig = orig;
        }

        public TestParticipant getUpdated() {
            return updated;
        }

        public void setUpdated(final TestParticipant updated) {
            this.updated = updated;
        }
    }
}

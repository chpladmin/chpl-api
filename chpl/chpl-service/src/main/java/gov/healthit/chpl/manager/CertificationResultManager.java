package gov.healthit.chpl.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.codeset.CertificationResultCodeSetService;
import gov.healthit.chpl.conformanceMethod.domain.CertificationResultConformanceMethod;
import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.dao.CertificationResultDAO;
import gov.healthit.chpl.dao.TestStandardDAO;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultAdditionalSoftware;
import gov.healthit.chpl.domain.CertificationResultTestData;
import gov.healthit.chpl.domain.CertificationResultTestProcedure;
import gov.healthit.chpl.domain.CertificationResultTestStandard;
import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.CertifiedProductUcdProcess;
import gov.healthit.chpl.domain.TestStandard;
import gov.healthit.chpl.dto.CertificationResultAdditionalSoftwareDTO;
import gov.healthit.chpl.dto.CertificationResultDTO;
import gov.healthit.chpl.dto.CertificationResultTestDataDTO;
import gov.healthit.chpl.dto.CertificationResultTestProcedureDTO;
import gov.healthit.chpl.dto.CertificationResultTestStandardDTO;
import gov.healthit.chpl.dto.CertificationResultTestTaskDTO;
import gov.healthit.chpl.dto.CertificationResultUcdProcessDTO;
import gov.healthit.chpl.entity.listing.CertificationResultConformanceMethodEntity;
import gov.healthit.chpl.entity.listing.CertificationResultOptionalStandardEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.functionalitytested.CertificationResultFunctionalityTestedDAO;
import gov.healthit.chpl.functionalitytested.CertificationResultFunctionalityTestedService;
import gov.healthit.chpl.functionalitytested.FunctionalityTestedDAO;
import gov.healthit.chpl.manager.impl.SecuredManager;
import gov.healthit.chpl.optionalStandard.domain.CertificationResultOptionalStandard;
import gov.healthit.chpl.standard.CertificationResultStandardService;
import gov.healthit.chpl.svap.domain.CertificationResultSvap;
import gov.healthit.chpl.testtool.CertificationResultTestToolService;
import gov.healthit.chpl.util.CertifiedProductUtil;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class CertificationResultManager extends SecuredManager {
    private CertifiedProductUtil cpUtil;
    private CertificationCriterionDAO criteriaDao;
    private CertificationResultDAO certResultDAO;
    private TestStandardDAO testStandardDAO;
    private CertificationResultTestToolService certResultTestToolService;
    private CertificationResultFunctionalityTestedService certResultFunctionalityTestedService;
    private CertificationResultStandardService certResultStandardService;
    private CertificationResultCodeSetService certificationResultCodeSetService;

    @SuppressWarnings("checkstyle:parameternumber")
    @Autowired
    public CertificationResultManager(CertifiedProductUtil cpUtil, CertificationCriterionDAO criteriaDao,
            CertificationResultDAO certResultDAO, CertificationResultFunctionalityTestedDAO certResultFuncTestedDao,
            TestStandardDAO testStandardDAO, FunctionalityTestedDAO functionalityTestedDao,
            CertificationResultTestToolService certResultTestToolService,
            CertificationResultFunctionalityTestedService certResultFunctionalityTestedService,
            CertificationResultStandardService certResultStandardService,
            CertificationResultCodeSetService certificationResultCodeSetService) {
        this.cpUtil = cpUtil;
        this.criteriaDao = criteriaDao;
        this.certResultDAO = certResultDAO;
        this.testStandardDAO = testStandardDAO;
        this.certResultTestToolService = certResultTestToolService;
        this.certResultFunctionalityTestedService = certResultFunctionalityTestedService;
        this.certResultStandardService = certResultStandardService;
        this.certificationResultCodeSetService = certificationResultCodeSetService;
    }

    @SuppressWarnings({"checkstyle:methodlength", "checkstyle:linelength"})
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).CERTIFICATION_RESULTS, "
            + "T(gov.healthit.chpl.permissions.domains.CertificationResultsDomainPermissions).UPDATE, #existingListing)")
    @Transactional(rollbackFor = {
            EntityRetrievalException.class, EntityCreationException.class
    })
    public int createOrUpdate(CertifiedProductSearchDetails existingListing, CertifiedProductSearchDetails updatedListing,
            CertificationResult orig, CertificationResult updated)
            throws EntityCreationException, EntityRetrievalException {

        int numChanges = 0;
        if ((orig == null || orig.getId() == null) && updated != null) {
            //this is a new cert result we are adding
            long addedCertResultId = certResultDAO.create(updatedListing.getId(), updated);
            updated.setId(addedCertResultId);
            numChanges++;
        } else {
            boolean hasChanged = false;
            if (!Objects.equals(orig.getSuccess(), updated.getSuccess())
                    || !StringUtils.equals(orig.getApiDocumentation(), updated.getApiDocumentation())
                    || !StringUtils.equals(orig.getPrivacySecurityFramework(), updated.getPrivacySecurityFramework())
                    || !Objects.equals(orig.getG1Success(), updated.getG1Success())
                    || !Objects.equals(orig.getG2Success(), updated.getG2Success())
                    || !Objects.equals(orig.getGap(), updated.getGap())
                    || !Objects.equals(orig.getSed(), updated.getSed())
                    || !Objects.equals(orig.getAttestationAnswer(), updated.getAttestationAnswer())
                    || !Objects.equals(orig.getDocumentationUrl(), updated.getDocumentationUrl())
                    || !Objects.equals(orig.getExportDocumentation(), updated.getExportDocumentation())
                    || !Objects.equals(orig.getUseCases(), updated.getUseCases())
                    || !Objects.equals(orig.getRiskManagementSummaryInformation(), updated.getRiskManagementSummaryInformation())
                    || !Objects.equals(orig.getServiceBaseUrlList(), updated.getServiceBaseUrlList())) {
                hasChanged = true;
            }
            if (hasChanged) {
                updateCertResultBasicData(existingListing.getId(), orig, updated);
                numChanges++;
            }
        }

        // update all related attributes
        numChanges += updateAdditionalSoftware(updated, orig.getAdditionalSoftware(), updated.getAdditionalSoftware());
        numChanges += updateConformanceMethods(updatedListing, updated, orig.getConformanceMethods(), updated.getConformanceMethods());
        numChanges += updateOptionalStandards(updatedListing, updated, orig.getOptionalStandards(), updated.getOptionalStandards());
        numChanges += updateTestStandards(updatedListing, updated, orig.getTestStandards(), updated.getTestStandards());
        numChanges += certResultTestToolService.synchronizeTestTools(updated, orig.getTestToolsUsed(), updated.getTestToolsUsed());
        numChanges += updateTestData(updated, orig.getTestDataUsed(), updated.getTestDataUsed());
        numChanges += updateTestProcedures(updated, orig.getTestProcedures(), updated.getTestProcedures());
        numChanges += certResultFunctionalityTestedService.synchronizeFunctionalitiesTested(updated, orig.getFunctionalitiesTested(), updated.getFunctionalitiesTested());
        numChanges += certResultStandardService.synchronizeStandards(updated,  orig.getStandards(), updated.getStandards());
        numChanges += updateSvap(updated, orig.getSvaps(), updated.getSvaps());
        numChanges += certificationResultCodeSetService.synchronizeCodeSets(updated,  orig.getCodeSets(), updated.getCodeSets());

        List<CertifiedProductUcdProcess> origUcdsForCriteria = new ArrayList<CertifiedProductUcdProcess>();
        List<CertifiedProductUcdProcess> updatedUcdsForCriteria = new ArrayList<CertifiedProductUcdProcess>();
        if (existingListing.getSed() != null && existingListing.getSed().getUcdProcesses() != null
                && existingListing.getSed().getUcdProcesses().size() > 0) {
            for (CertifiedProductUcdProcess existingUcd : existingListing.getSed().getUcdProcesses()) {
                boolean ucdMeetsCriteria = false;
                for (CertificationCriterion ucdCriteria : existingUcd.getCriteria()) {
                    if (ucdCriteria.getId().equals(updated.getCriterion().getId())
                            && orig.getSed() != null && orig.getSed()) {
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
                            && updated.getSed() != null && updated.getSed()) {
                        ucdMeetsCriteria = true;
                    }
                }
                if (ucdMeetsCriteria) {
                    updatedUcdsForCriteria.add(updatedUcd);
                }
            }
        }
        numChanges += updateUcdProcesses(updated, origUcdsForCriteria, updatedUcdsForCriteria);
        return numChanges;
    }

    private void updateCertResultBasicData(Long listingId, CertificationResult origCertResult, CertificationResult updatedCertResult)
        throws EntityCreationException, EntityRetrievalException {
        CertificationResultDTO toUpdate = new CertificationResultDTO();
        toUpdate.setId(origCertResult.getId());
        toUpdate.setCertifiedProductId(listingId);
        CertificationCriterion criteria = criteriaDao.getById(origCertResult.getCriterion().getId());
        if (criteria == null || criteria.getId() == null) {
            throw new EntityCreationException(
                    "Cannot add certification result mapping for unknown criteria " + origCertResult.getCriterion().getNumber());
        } else {
            toUpdate.setCertificationCriterionId(criteria.getId());
        }
        toUpdate.setSuccessful(updatedCertResult.getSuccess());
        toUpdate.setG1Success(updatedCertResult.getG1Success());
        toUpdate.setG2Success(updatedCertResult.getG2Success());

        if (toUpdate.getSuccessful() != null && toUpdate.getSuccessful().booleanValue()) {
            toUpdate.setApiDocumentation(updatedCertResult.getApiDocumentation());
            toUpdate.setPrivacySecurityFramework(updatedCertResult.getPrivacySecurityFramework());
            toUpdate.setGap(updatedCertResult.getGap());
            toUpdate.setSed(updatedCertResult.getSed());
            toUpdate.setAttestationAnswer(updatedCertResult.getAttestationAnswer());
            toUpdate.setDocumentationUrl(updatedCertResult.getDocumentationUrl());
            toUpdate.setExportDocumentation(updatedCertResult.getExportDocumentation());
            toUpdate.setUseCases(updatedCertResult.getUseCases());
            toUpdate.setRiskManagementSummaryInformation(updatedCertResult.getRiskManagementSummaryInformation());
            toUpdate.setServiceBaseUrlList(updatedCertResult.getServiceBaseUrlList());
        }
        certResultDAO.update(toUpdate);
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
                    CertifiedProduct cp = cpUtil.getListing(updatedItem.getCertifiedProductNumber());
                    if (cp != null) {
                        updatedItem.setCertifiedProductId(cp.getId());
                    }
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
            throws EntityCreationException, EntityRetrievalException {
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
        Long editionId = listing.getEdition() != null ? listing.getEdition().getId() : null;
        List<CertificationResultTestStandardDTO> testStandardsToAdd = new ArrayList<CertificationResultTestStandardDTO>();
        List<Long> idsToRemove = new ArrayList<Long>();

        // figure out which test standards to add
        if (updatedTestStandards != null && updatedTestStandards.size() > 0) {
            // fill in potentially missing test standard id
            for (CertificationResultTestStandard updatedItem : updatedTestStandards) {
                if (updatedItem.getTestStandardId() == null
                        && !StringUtils.isEmpty(updatedItem.getTestStandardName())) {
                    TestStandard foundStd = testStandardDAO.getByNumberAndEdition(updatedItem.getTestStandardName(), editionId);
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

    public boolean getCertifiedProductHasAdditionalSoftware(Long certifiedProductId) {
        return certResultDAO.getCertifiedProductHasAdditionalSoftware(certifiedProductId);
    }

    public List<CertificationResultUcdProcessDTO> getUcdProcessesForCertificationResult(Long certificationResultId) {
        return certResultDAO.getUcdProcessesForCertificationResult(certificationResultId);
    }

    public List<CertificationResultTestTaskDTO> getTestTasksForCertificationResult(Long certificationResultId) {
        return certResultDAO.getTestTasksForCertificationResult(certificationResultId);
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

        public CertificationResultAdditionalSoftware getUpdated() {
            return updated;
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

        public CertifiedProductUcdProcess getUpdated() {
            return updated;
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

        public CertificationResultTestData getUpdated() {
            return updated;
        }
    }
}

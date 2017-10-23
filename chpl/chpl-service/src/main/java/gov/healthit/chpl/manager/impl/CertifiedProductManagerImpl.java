package gov.healthit.chpl.manager.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.persistence.EntityNotFoundException;

import org.apache.commons.lang.ObjectUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.core.env.Environment;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.caching.CacheNames;
import gov.healthit.chpl.caching.ClearAllCaches;
import gov.healthit.chpl.dao.AccessibilityStandardDAO;
import gov.healthit.chpl.dao.CQMCriterionDAO;
import gov.healthit.chpl.dao.CQMResultDAO;
import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.dao.CertificationResultDAO;
import gov.healthit.chpl.dao.CertificationStatusDAO;
import gov.healthit.chpl.dao.CertificationStatusEventDAO;
import gov.healthit.chpl.dao.CertifiedProductAccessibilityStandardDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.CertifiedProductQmsStandardDAO;
import gov.healthit.chpl.dao.CertifiedProductTargetedUserDAO;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.dao.DeveloperStatusDAO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.ListingGraphDAO;
import gov.healthit.chpl.dao.MacraMeasureDAO;
import gov.healthit.chpl.dao.QmsStandardDAO;
import gov.healthit.chpl.dao.TargetedUserDAO;
import gov.healthit.chpl.dao.TestFunctionalityDAO;
import gov.healthit.chpl.dao.TestParticipantDAO;
import gov.healthit.chpl.dao.TestProcedureDAO;
import gov.healthit.chpl.dao.TestStandardDAO;
import gov.healthit.chpl.dao.TestTaskDAO;
import gov.healthit.chpl.dao.TestToolDAO;
import gov.healthit.chpl.dao.UcdProcessDAO;
import gov.healthit.chpl.dao.search.CertifiedProductSearchDAO;
import gov.healthit.chpl.domain.CQMResultCertification;
import gov.healthit.chpl.domain.CQMResultDetails;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.CertifiedProductAccessibilityStandard;
import gov.healthit.chpl.domain.CertifiedProductQmsStandard;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.CertifiedProductTargetedUser;
import gov.healthit.chpl.domain.IcsFamilyTreeNode;
import gov.healthit.chpl.domain.InheritedCertificationStatus;
import gov.healthit.chpl.domain.ListingUpdateRequest;
import gov.healthit.chpl.domain.MeaningfulUseUser;
import gov.healthit.chpl.domain.concept.ActivityConcept;
import gov.healthit.chpl.dto.AccessibilityStandardDTO;
import gov.healthit.chpl.dto.AddressDTO;
import gov.healthit.chpl.dto.CQMCriterionDTO;
import gov.healthit.chpl.dto.CQMResultCriteriaDTO;
import gov.healthit.chpl.dto.CQMResultDTO;
import gov.healthit.chpl.dto.CQMResultDetailsDTO;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.dto.CertificationResultAdditionalSoftwareDTO;
import gov.healthit.chpl.dto.CertificationResultDTO;
import gov.healthit.chpl.dto.CertificationResultMacraMeasureDTO;
import gov.healthit.chpl.dto.CertificationResultTestDataDTO;
import gov.healthit.chpl.dto.CertificationResultTestFunctionalityDTO;
import gov.healthit.chpl.dto.CertificationResultTestProcedureDTO;
import gov.healthit.chpl.dto.CertificationResultTestStandardDTO;
import gov.healthit.chpl.dto.CertificationResultTestTaskDTO;
import gov.healthit.chpl.dto.CertificationResultTestToolDTO;
import gov.healthit.chpl.dto.CertificationResultUcdProcessDTO;
import gov.healthit.chpl.dto.CertificationStatusDTO;
import gov.healthit.chpl.dto.CertificationStatusEventDTO;
import gov.healthit.chpl.dto.CertifiedProductAccessibilityStandardDTO;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.dto.CertifiedProductQmsStandardDTO;
import gov.healthit.chpl.dto.CertifiedProductTargetedUserDTO;
import gov.healthit.chpl.dto.ContactDTO;
import gov.healthit.chpl.dto.DeveloperACBMapDTO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.DeveloperStatusDTO;
import gov.healthit.chpl.dto.DeveloperStatusEventDTO;
import gov.healthit.chpl.dto.ListingToListingMapDTO;
import gov.healthit.chpl.dto.PendingCertificationResultAdditionalSoftwareDTO;
import gov.healthit.chpl.dto.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.PendingCertificationResultMacraMeasureDTO;
import gov.healthit.chpl.dto.PendingCertificationResultTestDataDTO;
import gov.healthit.chpl.dto.PendingCertificationResultTestFunctionalityDTO;
import gov.healthit.chpl.dto.PendingCertificationResultTestProcedureDTO;
import gov.healthit.chpl.dto.PendingCertificationResultTestStandardDTO;
import gov.healthit.chpl.dto.PendingCertificationResultTestTaskDTO;
import gov.healthit.chpl.dto.PendingCertificationResultTestTaskParticipantDTO;
import gov.healthit.chpl.dto.PendingCertificationResultTestToolDTO;
import gov.healthit.chpl.dto.PendingCertificationResultUcdProcessDTO;
import gov.healthit.chpl.dto.PendingCertifiedProductAccessibilityStandardDTO;
import gov.healthit.chpl.dto.PendingCertifiedProductDTO;
import gov.healthit.chpl.dto.PendingCertifiedProductQmsStandardDTO;
import gov.healthit.chpl.dto.PendingCertifiedProductTargetedUserDTO;
import gov.healthit.chpl.dto.PendingCqmCertificationCriterionDTO;
import gov.healthit.chpl.dto.PendingCqmCriterionDTO;
import gov.healthit.chpl.dto.PendingTestParticipantDTO;
import gov.healthit.chpl.dto.PendingTestTaskDTO;
import gov.healthit.chpl.dto.ProductDTO;
import gov.healthit.chpl.dto.ProductVersionDTO;
import gov.healthit.chpl.dto.QmsStandardDTO;
import gov.healthit.chpl.dto.TargetedUserDTO;
import gov.healthit.chpl.dto.TestFunctionalityDTO;
import gov.healthit.chpl.dto.TestParticipantDTO;
import gov.healthit.chpl.dto.TestProcedureDTO;
import gov.healthit.chpl.dto.TestStandardDTO;
import gov.healthit.chpl.dto.TestTaskDTO;
import gov.healthit.chpl.dto.TestToolDTO;
import gov.healthit.chpl.dto.UcdProcessDTO;
import gov.healthit.chpl.entity.CertificationStatusType;
import gov.healthit.chpl.entity.developer.DeveloperStatusType;
import gov.healthit.chpl.manager.ActivityManager;
import gov.healthit.chpl.manager.CertificationBodyManager;
import gov.healthit.chpl.manager.CertificationResultManager;
import gov.healthit.chpl.manager.CertifiedProductDetailsManager;
import gov.healthit.chpl.manager.CertifiedProductManager;
import gov.healthit.chpl.manager.DeveloperManager;
import gov.healthit.chpl.manager.ProductManager;
import gov.healthit.chpl.manager.ProductVersionManager;
import gov.healthit.chpl.web.controller.InvalidArgumentsException;
import gov.healthit.chpl.web.controller.results.MeaningfulUseUserResults;

@Service("certifiedProductManager")
public class CertifiedProductManagerImpl implements CertifiedProductManager {
    private static final Logger LOGGER = LogManager.getLogger(CertifiedProductManagerImpl.class);

    @Autowired
    private Environment env;

    @Autowired
    CertifiedProductDAO cpDao;
    @Autowired
    CertifiedProductSearchDAO searchDao;
    @Autowired
    CertificationResultDAO certDao;
    @Autowired
    CertificationCriterionDAO certCriterionDao;
    @Autowired
    QmsStandardDAO qmsDao;
    @Autowired
    TargetedUserDAO targetedUserDao;
    @Autowired
    AccessibilityStandardDAO asDao;
    @Autowired
    CertifiedProductQmsStandardDAO cpQmsDao;
    @Autowired
    CertifiedProductTargetedUserDAO cpTargetedUserDao;
    @Autowired
    CertifiedProductAccessibilityStandardDAO cpAccStdDao;
    @Autowired
    CQMResultDAO cqmResultDAO;
    @Autowired
    CQMCriterionDAO cqmCriterionDao;
    @Autowired
    CertificationBodyDAO acbDao;
    @Autowired
    DeveloperDAO developerDao;
    @Autowired
    DeveloperStatusDAO devStatusDao;
    @Autowired
    DeveloperManager developerManager;
    @Autowired
    ProductManager productManager;
    @Autowired
    ProductVersionManager versionManager;
    @Autowired
    CertificationStatusEventDAO statusEventDao;
    @Autowired
    CertificationResultManager certResultManager;
    @Autowired
    TestToolDAO testToolDao;
    @Autowired
    TestStandardDAO testStandardDao;
    @Autowired
    TestProcedureDAO testProcDao;
    @Autowired
    TestFunctionalityDAO testFuncDao;
    @Autowired
    UcdProcessDAO ucdDao;
    @Autowired
    TestParticipantDAO testParticipantDao;
    @Autowired
    TestTaskDAO testTaskDao;
    @Autowired
    MacraMeasureDAO macraDao;
    @Autowired
    CertificationStatusDAO certStatusDao;
    @Autowired
    ListingGraphDAO listingGraphDao;
    @Autowired
    CertificationResultDAO certResultDao;

    @Autowired
    public ActivityManager activityManager;

    @Autowired
    public CertifiedProductDetailsManager detailsManager;

    @Autowired
    public CertificationBodyManager acbManager;

    public CertifiedProductManagerImpl() {
    }

    @Override
    @Transactional(readOnly = true)
    public CertifiedProductDTO getById(Long id) throws EntityRetrievalException {
        CertifiedProductDTO result = cpDao.getById(id);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public CertifiedProductDTO getByChplProductNumber(String chplProductNumber) throws EntityRetrievalException {
        CertifiedProductDTO result = cpDao.getByChplNumber(chplProductNumber);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean chplIdExists(String id) throws EntityRetrievalException {
        if (StringUtils.isEmpty(id)) {
            return false;
        }

        boolean exists = false;
        if (id.startsWith("CHP")) {
            CertifiedProductDTO existing = cpDao.getByChplNumber(id);
            if (existing != null) {
                exists = true;
            }
        } else {
            try {
                CertifiedProductDetailsDTO existing = cpDao.getByChplUniqueId(id);
                if (existing != null) {
                    exists = true;
                }
            } catch (final EntityRetrievalException ex) {
                LOGGER.error("Could not look up " + id, ex);
            }
        }
        return exists;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CertifiedProductDetailsDTO> getDetailsByIds(List<Long> ids) throws EntityRetrievalException {
        return cpDao.getDetailsByIds(ids);
    }

    @Override
    @Transactional(readOnly = true)
    public CertifiedProductDetailsDTO getDetailsById(Long ids) throws EntityRetrievalException {
        return cpDao.getDetailsById(ids);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CertifiedProductDetailsDTO> getAll() {
        return cpDao.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CertifiedProductDetailsDTO> getAllWithEditPermission() {
        List<CertificationBodyDTO> userAcbs = acbManager.getAllForUser(false);
        if (userAcbs == null || userAcbs.size() == 0) {
            return new ArrayList<CertifiedProductDetailsDTO>();
        }
        List<Long> acbIdList = new ArrayList<Long>(userAcbs.size());
        for (CertificationBodyDTO dto : userAcbs) {
            acbIdList.add(dto.getId());
        }
        return cpDao.getDetailsByAcbIds(acbIdList);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CertifiedProductDetailsDTO> getByVersion(Long versionId) throws EntityRetrievalException {
        versionManager.getById(versionId); // throws 404 if bad id
        return cpDao.getDetailsByVersionId(versionId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CertifiedProductDetailsDTO> getByProduct(Long productId) throws EntityRetrievalException {
        productManager.getById(productId); // throws 404 if bad id
        return cpDao.getDetailsByProductId(productId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CertifiedProductDetailsDTO> getByVersionWithEditPermission(Long versionId)
            throws EntityRetrievalException {
        versionManager.getById(versionId); // throws 404 if bad id
        List<CertificationBodyDTO> userAcbs = acbManager.getAllForUser(false);
        if (userAcbs == null || userAcbs.size() == 0) {
            return new ArrayList<CertifiedProductDetailsDTO>();
        }
        List<Long> acbIdList = new ArrayList<Long>(userAcbs.size());
        for (CertificationBodyDTO dto : userAcbs) {
            acbIdList.add(dto.getId());
        }
        return cpDao.getDetailsByVersionAndAcbIds(versionId, acbIdList);
    }

    @Override
    @Transactional
    public List<IcsFamilyTreeNode> getIcsFamilyTree(Long certifiedProductId) throws EntityRetrievalException {
        getById(certifiedProductId); // sends back 404 if bad id

        List<IcsFamilyTreeNode> familyTree = new ArrayList<IcsFamilyTreeNode>();
        Map<Long, Boolean> queue = new HashMap<Long, Boolean>();
        List<Long> toAdd = new ArrayList<Long>();

        // add first element to processing queue
        queue.put(certifiedProductId, false);

        // while queue contains elements that need processing
        while (queue.containsValue(false)) {
            for (Entry<Long, Boolean> cp : queue.entrySet()) {
                Boolean isProcessed = cp.getValue();
                Long cpId = cp.getKey();
                if (!isProcessed) {
                    IcsFamilyTreeNode node = searchDao.getICSFamilyTree(cpId);
                    // add family to array that will be used to add to
                    // processing array
                    familyTree.add(node);
                    // done processing node - set processed to true
                    for (CertifiedProduct certProd : node.getChildren()) {
                        toAdd.add(certProd.getId());
                    }
                    for (CertifiedProduct certProd : node.getParents()) {
                        toAdd.add(certProd.getId());
                    }
                    queue.put(cpId, true);
                }
            }
            // add elements from toAdd array to queue if they are not already
            // there
            for (Long id : toAdd) {
                if (!queue.containsKey(id)) {
                    queue.put(id, false);
                }
            }
            toAdd.clear();
        }

        return familyTree;
    }

    @Override
    @PreAuthorize("(hasRole('ROLE_ACB_STAFF') or hasRole('ROLE_ACB_ADMIN')) "
            + "and hasPermission(#acbId, 'gov.healthit.chpl.dto.CertificationBodyDTO', admin)")
    @Transactional(readOnly = false)
    @CacheEvict(value = {
            CacheNames.ALL_DEVELOPERS, CacheNames.ALL_DEVELOPERS_INCLUDING_DELETED, CacheNames.COLLECTIONS_DEVELOPERS,
            CacheNames.DEVELOPER_NAMES, CacheNames.PRODUCT_NAMES, CacheNames.SEARCH,
            CacheNames.COUNT_MULTI_FILTER_SEARCH_RESULTS
    }, allEntries = true)
    public CertifiedProductDTO createFromPending(Long acbId, PendingCertifiedProductDTO pendingCp)
            throws EntityRetrievalException, EntityCreationException, JsonProcessingException {

        CertifiedProductDTO toCreate = new CertifiedProductDTO();
        toCreate.setAcbCertificationId(pendingCp.getAcbCertificationId());
        toCreate.setReportFileLocation(pendingCp.getReportFileLocation());
        toCreate.setSedReportFileLocation(pendingCp.getSedReportFileLocation());
        toCreate.setSedIntendedUserDescription(pendingCp.getSedIntendedUserDescription());
        toCreate.setSedTestingEnd(pendingCp.getSedTestingEnd());
        toCreate.setIcs(pendingCp.getIcs());
        toCreate.setAccessibilityCertified(pendingCp.getAccessibilityCertified());
        toCreate.setPracticeTypeId(pendingCp.getPracticeTypeId());
        toCreate.setProductClassificationTypeId(pendingCp.getProductClassificationId());
        toCreate.setCreationDate(new Date());
        toCreate.setDeleted(false);
        toCreate.setLastModifiedDate(new Date());
        toCreate.setLastModifiedUser(Util.getCurrentUser().getId());

        if (pendingCp.getCertificationBodyId() == null) {
            throw new EntityCreationException("ACB ID must be specified.");
        }
        toCreate.setCertificationBodyId(pendingCp.getCertificationBodyId());

        if (pendingCp.getTestingLabId() == null) {
            throw new EntityCreationException("ATL ID must be specified.");
        }
        toCreate.setTestingLabId(pendingCp.getTestingLabId());

        if (pendingCp.getCertificationEditionId() == null) {
            throw new EntityCreationException(
                    "The ID of an existing certification edition (year) must be provided. A new certification edition cannot be created via this process.");
        }
        toCreate.setCertificationEditionId(pendingCp.getCertificationEditionId());

        String status = pendingCp.getRecordStatus();
        if (StringUtils.isEmpty(status)) {
            throw new EntityCreationException(
                    "Cannot determine certification status. Is this a new record? An update? A removal?");
        }
        if (status.trim().equalsIgnoreCase("new")) {
            CertificationStatusDTO statusDto = certStatusDao.getByStatusName("Active");
            toCreate.setCertificationStatusId(statusDto.getId());
        }
        toCreate.setTransparencyAttestationUrl(pendingCp.getTransparencyAttestationUrl());

        DeveloperDTO developer = null;
        if (pendingCp.getDeveloperId() == null) {
            DeveloperDTO newDeveloper = new DeveloperDTO();
            if (StringUtils.isEmpty(pendingCp.getDeveloperName())) {
                throw new EntityCreationException("You must provide a developer name to create a new developer.");
            }
            newDeveloper.setName(pendingCp.getDeveloperName());
            newDeveloper.setWebsite(pendingCp.getDeveloperWebsite());
            DeveloperACBMapDTO transparencyMap = new DeveloperACBMapDTO();
            transparencyMap.setAcbId(pendingCp.getCertificationBodyId());
            transparencyMap.setAcbName(pendingCp.getCertificationBodyName());
            transparencyMap.setTransparencyAttestation(pendingCp.getTransparencyAttestation());
            newDeveloper.getTransparencyAttestationMappings().add(transparencyMap);
            AddressDTO developerAddress = pendingCp.getDeveloperAddress();
            newDeveloper.setAddress(developerAddress);
            ContactDTO developerContact = new ContactDTO();
            developerContact.setLastName(pendingCp.getDeveloperContactName());
            developerContact.setPhoneNumber(pendingCp.getDeveloperPhoneNumber());
            developerContact.setEmail(pendingCp.getDeveloperEmail());
            newDeveloper.setContact(developerContact);
            // create the dev, address, and contact
            developer = developerManager.create(newDeveloper);
            pendingCp.setDeveloperId(developer.getId());
        }

        if (pendingCp.getProductId() == null) {
            ProductDTO newProduct = new ProductDTO();
            if (pendingCp.getProductName() == null) {
                throw new EntityCreationException("Either product name or ID must be provided.");
            }
            newProduct.setName(pendingCp.getProductName());
            newProduct.setDeveloperId(pendingCp.getDeveloperId());
            newProduct.setReportFileLocation(pendingCp.getReportFileLocation());
            newProduct = productManager.create(newProduct);
            pendingCp.setProductId(newProduct.getId());
        }

        if (pendingCp.getProductVersionId() == null) {
            ProductVersionDTO newVersion = new ProductVersionDTO();
            if (pendingCp.getProductVersion() == null) {
                throw new EntityCreationException("Either version id or version must be provided.");
            }
            newVersion.setVersion(pendingCp.getProductVersion());
            newVersion.setProductId(pendingCp.getProductId());
            newVersion = versionManager.create(newVersion);
            pendingCp.setProductVersionId(newVersion.getId());
        }
        toCreate.setProductVersionId(pendingCp.getProductVersionId());

        String uniqueId = pendingCp.getUniqueId();
        String[] uniqueIdParts = uniqueId.split("\\.");
        toCreate.setProductCode(uniqueIdParts[4]);
        toCreate.setVersionCode(uniqueIdParts[5]);
        toCreate.setIcsCode(uniqueIdParts[6]);
        toCreate.setAdditionalSoftwareCode(uniqueIdParts[7]);
        toCreate.setCertifiedDateCode(uniqueIdParts[8]);

        CertifiedProductDTO newCertifiedProduct = cpDao.create(toCreate);

        // ics
        if (pendingCp.getIcsParents() != null && pendingCp.getIcsParents().size() > 0) {
            for (CertifiedProductDetailsDTO parentCpDto : pendingCp.getIcsParents()) {
                CertifiedProduct cp = searchDao.getByChplProductNumber(parentCpDto.getChplProductNumber());
                if (cp != null) {
                    ListingToListingMapDTO toAdd = new ListingToListingMapDTO();
                    toAdd.setChildId(newCertifiedProduct.getId());
                    toAdd.setParentId(cp.getId());
                    listingGraphDao.createListingMap(toAdd);
                }
            }
        }
        if (pendingCp.getIcsChildren() != null && pendingCp.getIcsChildren().size() > 0) {
            for (CertifiedProductDetailsDTO childCpDto : pendingCp.getIcsChildren()) {
                CertifiedProduct cp = searchDao.getByChplProductNumber(childCpDto.getChplProductNumber());
                if (cp != null) {
                    ListingToListingMapDTO toAdd = new ListingToListingMapDTO();
                    toAdd.setChildId(cp.getId());
                    toAdd.setParentId(newCertifiedProduct.getId());
                    listingGraphDao.createListingMap(toAdd);
                }
            }
        }

        // qms
        if (pendingCp.getQmsStandards() != null && pendingCp.getQmsStandards().size() > 0) {
            for (PendingCertifiedProductQmsStandardDTO qms : pendingCp.getQmsStandards()) {
                CertifiedProductQmsStandardDTO qmsDto = new CertifiedProductQmsStandardDTO();
                if (qms.getQmsStandardId() == null) {
                    QmsStandardDTO toAdd = new QmsStandardDTO();
                    toAdd.setName(qms.getName());
                    toAdd = qmsDao.create(toAdd);
                    qmsDto.setQmsStandardId(toAdd.getId());
                } else {
                    qmsDto.setQmsStandardId(qms.getQmsStandardId());
                }
                qmsDto.setCertifiedProductId(newCertifiedProduct.getId());
                qmsDto.setApplicableCriteria(qms.getApplicableCriteria());
                qmsDto.setQmsModification(qms.getModification());
                cpQmsDao.createCertifiedProductQms(qmsDto);
            }
        }

        // targeted users
        if (pendingCp.getTargetedUsers() != null && pendingCp.getTargetedUsers().size() > 0) {
            for (PendingCertifiedProductTargetedUserDTO tu : pendingCp.getTargetedUsers()) {
                CertifiedProductTargetedUserDTO tuDto = new CertifiedProductTargetedUserDTO();
                if (tu.getTargetedUserId() == null) {
                    TargetedUserDTO toAdd = new TargetedUserDTO();
                    toAdd.setName(tu.getName());
                    toAdd = targetedUserDao.create(toAdd);
                    tuDto.setTargetedUserId(toAdd.getId());
                } else {
                    tuDto.setTargetedUserId(tu.getTargetedUserId());
                }
                tuDto.setCertifiedProductId(newCertifiedProduct.getId());
                cpTargetedUserDao.createCertifiedProductTargetedUser(tuDto);
            }
        }

        // accessibility standards
        if (pendingCp.getAccessibilityStandards() != null && pendingCp.getAccessibilityStandards().size() > 0) {
            for (PendingCertifiedProductAccessibilityStandardDTO as : pendingCp.getAccessibilityStandards()) {
                CertifiedProductAccessibilityStandardDTO asDto = new CertifiedProductAccessibilityStandardDTO();
                asDto.setCertifiedProductId(newCertifiedProduct.getId());

                if (as.getAccessibilityStandardId() != null) {
                    asDto.setAccessibilityStandardName(as.getName());
                    asDto.setAccessibilityStandardId(as.getAccessibilityStandardId());
                } else {
                    // check again for a matching accessibility std because the
                    // uesr could have edited
                    // it since upload
                    AccessibilityStandardDTO match = asDao.getByName(as.getName());
                    if (match != null) {
                        asDto.setAccessibilityStandardName(match.getName());
                        asDto.setAccessibilityStandardId(match.getId());
                    } else {
                        // if it wasn't there then create it
                        AccessibilityStandardDTO asToCreate = new AccessibilityStandardDTO();
                        asToCreate.setName(as.getName());
                        match = asDao.create(asToCreate);
                        asDto.setAccessibilityStandardId(match.getId());
                        asDto.setAccessibilityStandardName(match.getName());
                    }
                }

                if (asDto.getAccessibilityStandardId() != null) {
                    cpAccStdDao.createCertifiedProductAccessibilityStandard(asDto);
                } else {
                    LOGGER.error("Could not insert accessibility standard with null id. Name was " + as.getName());
                }
            }
        }

        // certs
        if (pendingCp.getCertificationCriterion() != null && pendingCp.getCertificationCriterion().size() > 0) {

            // participants and tasks are re-used across multiple certifications
            // within the same product
            List<TestParticipantDTO> testParticipantsAdded = new ArrayList<TestParticipantDTO>();
            List<TestTaskDTO> testTasksAdded = new ArrayList<TestTaskDTO>();

            for (PendingCertificationResultDTO certResult : pendingCp.getCertificationCriterion()) {
                CertificationCriterionDTO criterion = certCriterionDao.getByName(certResult.getNumber());
                if (criterion == null) {
                    throw new EntityCreationException(
                            "Could not find certification criterion with number " + certResult.getNumber());
                }
                CertificationResultDTO certResultToCreate = new CertificationResultDTO();
                certResultToCreate.setCertificationCriterionId(criterion.getId());
                certResultToCreate.setCertifiedProduct(newCertifiedProduct.getId());
                certResultToCreate.setSuccessful(certResult.getMeetsCriteria());
                boolean isCertified = (certResultToCreate.getSuccessful() != null
                        && certResultToCreate.getSuccessful().booleanValue() == true);
                certResultToCreate.setGap(isCertified ? certResult.getGap() : null);
                certResultToCreate.setG1Success(isCertified ? certResult.getG1Success() : null);
                certResultToCreate.setG2Success(isCertified ? certResult.getG2Success() : null);
                if (isCertified && certResult.getSed() == null) {
                    if (certResult.getUcdProcesses() != null && certResult.getUcdProcesses().size() > 0) {
                        certResultToCreate.setSed(Boolean.TRUE);
                    } else {
                        certResultToCreate.setSed(Boolean.FALSE);
                    }
                } else {
                    certResultToCreate.setSed(isCertified ? certResult.getSed() : null);
                }
                certResultToCreate.setApiDocumentation(isCertified ? certResult.getApiDocumentation() : null);
                certResultToCreate
                        .setPrivacySecurityFramework(isCertified ? certResult.getPrivacySecurityFramework() : null);
                CertificationResultDTO createdCert = certDao.create(certResultToCreate);
                if (isCertified) {
                    if (certResult.getAdditionalSoftware() != null && certResult.getAdditionalSoftware().size() > 0) {
                        for (PendingCertificationResultAdditionalSoftwareDTO software : certResult
                                .getAdditionalSoftware()) {
                            CertificationResultAdditionalSoftwareDTO as = new CertificationResultAdditionalSoftwareDTO();
                            as.setCertifiedProductId(software.getCertifiedProductId());
                            as.setJustification(software.getJustification());
                            as.setName(software.getName());
                            as.setVersion(software.getVersion());
                            as.setGrouping(software.getGrouping());
                            as.setCertificationResultId(createdCert.getId());
                            certDao.addAdditionalSoftwareMapping(as);
                        }
                    }

                    if (certResult.getUcdProcesses() != null && certResult.getUcdProcesses().size() > 0) {
                        for (PendingCertificationResultUcdProcessDTO ucd : certResult.getUcdProcesses()) {
                            CertificationResultUcdProcessDTO ucdDto = new CertificationResultUcdProcessDTO();
                            if (ucd.getUcdProcessId() == null) {
                                UcdProcessDTO newUcd = new UcdProcessDTO();
                                newUcd.setName(ucd.getUcdProcessName());
                                newUcd = ucdDao.create(newUcd);
                                ucdDto.setUcdProcessId(newUcd.getId());
                            } else {
                                ucdDto.setUcdProcessId(ucd.getUcdProcessId());
                            }
                            ucdDto.setCertificationResultId(createdCert.getId());
                            ucdDto.setUcdProcessDetails(ucd.getUcdProcessDetails());
                            certDao.addUcdProcessMapping(ucdDto);
                        }
                    }

                    if (certResult.getTestData() != null && certResult.getTestData().size() > 0) {
                        for (PendingCertificationResultTestDataDTO testData : certResult.getTestData()) {
                            CertificationResultTestDataDTO testDto = new CertificationResultTestDataDTO();
                            testDto.setAlteration(testData.getAlteration());
                            testDto.setVersion(testData.getVersion());
                            testDto.setCertificationResultId(createdCert.getId());
                            certDao.addTestDataMapping(testDto);
                        }
                    }

                    if (certResult.getTestFunctionality() != null && certResult.getTestFunctionality().size() > 0) {
                        for (PendingCertificationResultTestFunctionalityDTO func : certResult.getTestFunctionality()) {
                            if (func.getTestFunctionalityId() != null) {
                                CertificationResultTestFunctionalityDTO funcDto = new CertificationResultTestFunctionalityDTO();
                                funcDto.setTestFunctionalityId(func.getTestFunctionalityId());
                                funcDto.setCertificationResultId(createdCert.getId());
                                certDao.addTestFunctionalityMapping(funcDto);
                            } else {
                                // check again for a matching test tool because
                                // the user could have edited
                                // it since upload
                                TestFunctionalityDTO match = testFuncDao.getByNumberAndEdition(func.getNumber(),
                                        pendingCp.getCertificationEditionId());
                                if (match != null) {
                                    CertificationResultTestFunctionalityDTO funcDto = new CertificationResultTestFunctionalityDTO();
                                    funcDto.setTestFunctionalityId(match.getId());
                                    funcDto.setCertificationResultId(createdCert.getId());
                                    certDao.addTestFunctionalityMapping(funcDto);
                                } else {
                                    LOGGER.error("Could not insert test functionality with null id. Number was "
                                            + func.getNumber() + " and edition id "
                                            + pendingCp.getCertificationEditionId());
                                }
                            }
                        }
                    }

                    if (certResult.getTestProcedures() != null && certResult.getTestProcedures().size() > 0) {
                        for (PendingCertificationResultTestProcedureDTO proc : certResult.getTestProcedures()) {
                            CertificationResultTestProcedureDTO procDto = new CertificationResultTestProcedureDTO();
                            if (proc.getTestProcedureId() == null) {
                                TestProcedureDTO tp = new TestProcedureDTO();
                                tp.setVersion(proc.getVersion());
                                tp = testProcDao.create(tp);
                                procDto.setTestProcedureId(tp.getId());
                            } else {
                                procDto.setTestProcedureId(proc.getTestProcedureId());
                            }
                            procDto.setTestProcedureVersion(proc.getVersion());
                            procDto.setCertificationResultId(createdCert.getId());
                            certDao.addTestProcedureMapping(procDto);
                        }
                    }

                    if (certResult.getTestStandards() != null && certResult.getTestStandards().size() > 0) {
                        for (PendingCertificationResultTestStandardDTO std : certResult.getTestStandards()) {
                            CertificationResultTestStandardDTO stdDto = new CertificationResultTestStandardDTO();
                            if (std.getTestStandardId() == null) {
                                // try to look up by name and edition
                                TestStandardDTO foundTestStandard = testStandardDao.getByNumberAndEdition(std.getName(),
                                        pendingCp.getCertificationEditionId());
                                if (foundTestStandard == null) {
                                    // if not found create a new test standard
                                    TestStandardDTO ts = new TestStandardDTO();
                                    ts.setName(std.getName());
                                    ts.setCertificationEditionId(pendingCp.getCertificationEditionId());
                                    ts = testStandardDao.create(ts);
                                    stdDto.setTestStandardId(ts.getId());
                                } else {
                                    stdDto.setTestStandardId(foundTestStandard.getId());
                                }
                            } else {
                                stdDto.setTestStandardId(std.getTestStandardId());
                            }
                            stdDto.setCertificationResultId(createdCert.getId());
                            // make sure this isn't a duplicate test standard
                            // for this criteria
                            CertificationResultTestStandardDTO existingMapping = certDao.lookupTestStandardMapping(
                                    stdDto.getCertificationResultId(), stdDto.getTestStandardId());
                            if (existingMapping == null) {
                                certDao.addTestStandardMapping(stdDto);
                            }
                        }
                    }

                    if (certResult.getTestTools() != null && certResult.getTestTools().size() > 0) {
                        for (PendingCertificationResultTestToolDTO tool : certResult.getTestTools()) {
                            if (tool.getTestToolId() != null) {
                                CertificationResultTestToolDTO toolDto = new CertificationResultTestToolDTO();
                                toolDto.setTestToolId(tool.getTestToolId());
                                toolDto.setTestToolVersion(tool.getVersion());
                                toolDto.setCertificationResultId(createdCert.getId());
                                certDao.addTestToolMapping(toolDto);
                            } else {
                                // check again for a matching test tool because
                                // the user could have edited
                                // it since upload
                                TestToolDTO match = testToolDao.getByName(tool.getName());
                                if (match != null) {
                                    CertificationResultTestToolDTO toolDto = new CertificationResultTestToolDTO();
                                    toolDto.setTestToolId(match.getId());
                                    toolDto.setTestToolVersion(tool.getVersion());
                                    toolDto.setCertificationResultId(createdCert.getId());
                                    certDao.addTestToolMapping(toolDto);
                                } else {
                                    LOGGER.error("Could not insert test tool with null id. Name was " + tool.getName());
                                }
                            }
                        }
                    }

                    if (certResult.getG1MacraMeasures() != null && certResult.getG1MacraMeasures().size() > 0) {
                        for (PendingCertificationResultMacraMeasureDTO pendingMeasure : certResult
                                .getG1MacraMeasures()) {
                            // the validator set the macraMeasure value so it's
                            // definitely filled in
                            if (pendingMeasure.getMacraMeasure() != null
                                    && pendingMeasure.getMacraMeasure().getId() != null) {
                                CertificationResultMacraMeasureDTO crMeasure = new CertificationResultMacraMeasureDTO();
                                crMeasure.setMeasure(pendingMeasure.getMacraMeasure());
                                crMeasure.setCertificationResultId(createdCert.getId());
                                certDao.addG1MacraMeasureMapping(crMeasure);
                            } else {
                                LOGGER.error("Found G1 Macra Measure with null value for " + certResult.getNumber());
                            }
                        }
                    }

                    if (certResult.getG2MacraMeasures() != null && certResult.getG2MacraMeasures().size() > 0) {
                        for (PendingCertificationResultMacraMeasureDTO pendingMeasure : certResult
                                .getG2MacraMeasures()) {
                            // the validator set the macraMeasure value so it's
                            // definitely filled in
                            if (pendingMeasure.getMacraMeasure() != null
                                    && pendingMeasure.getMacraMeasure().getId() != null) {
                                CertificationResultMacraMeasureDTO crMeasure = new CertificationResultMacraMeasureDTO();
                                crMeasure.setMeasure(pendingMeasure.getMacraMeasure());
                                crMeasure.setCertificationResultId(createdCert.getId());
                                certDao.addG2MacraMeasureMapping(crMeasure);
                            } else {
                                LOGGER.error("Found G2 Macra Measure with null value for " + certResult.getNumber());
                            }
                        }
                    }

                    if (certResult.getTestTasks() != null && certResult.getTestTasks().size() > 0) {
                        // Map<Long, Long> pendingTaskToConfirmedTaskMap = new
                        // HashMap<Long, Long>();
                        for (PendingCertificationResultTestTaskDTO certTask : certResult.getTestTasks()) {
                            // have we already added this one?
                            TestTaskDTO existingTt = null;
                            for (TestTaskDTO tt : testTasksAdded) {
                                if (certTask.getPendingTestTask() != null && certTask.getPendingTestTask().getUniqueId()
                                        .equals(tt.getPendingUniqueId())) {
                                    existingTt = tt;
                                }
                            }
                            if (existingTt == null && certTask.getPendingTestTask() != null) {
                                PendingTestTaskDTO pendingTask = certTask.getPendingTestTask();
                                // if(pendingTaskToConfirmedTaskMap.get(pendingTask.getId())
                                // != null) {
                                // existingTt =
                                // testTaskDao.getById(pendingTaskToConfirmedTaskMap.get(pendingTask.getId()));
                                // } else {
                                TestTaskDTO tt = new TestTaskDTO();
                                tt.setDescription(pendingTask.getDescription());
                                tt.setTaskErrors(pendingTask.getTaskErrors());
                                tt.setTaskErrorsStddev(pendingTask.getTaskErrorsStddev());
                                tt.setTaskPathDeviationObserved(pendingTask.getTaskPathDeviationObserved());
                                tt.setTaskPathDeviationOptimal(pendingTask.getTaskPathDeviationOptimal());
                                tt.setTaskRating(pendingTask.getTaskRating());
                                tt.setTaskRatingScale(pendingTask.getTaskRatingScale());
                                tt.setTaskRatingStddev(pendingTask.getTaskRatingStddev());
                                tt.setTaskSuccessAverage(pendingTask.getTaskSuccessAverage());
                                tt.setTaskSuccessStddev(pendingTask.getTaskSuccessStddev());
                                tt.setTaskTimeAvg(pendingTask.getTaskTimeAvg());
                                tt.setTaskTimeDeviationObservedAvg(pendingTask.getTaskTimeDeviationObservedAvg());
                                tt.setTaskTimeDeviationOptimalAvg(pendingTask.getTaskTimeDeviationOptimalAvg());
                                tt.setTaskTimeStddev(pendingTask.getTaskTimeStddev());

                                // add test task
                                existingTt = testTaskDao.create(tt);
                                // pendingTaskToConfirmedTaskMap.put(pendingTask.getId(),
                                // existingTt.getId());
                                // }
                                existingTt.setPendingUniqueId(pendingTask.getUniqueId());
                                testTasksAdded.add(existingTt);
                            }
                            // add mapping from cert result to test task
                            CertificationResultTestTaskDTO taskDto = new CertificationResultTestTaskDTO();
                            taskDto.setTestTaskId(existingTt.getId());
                            taskDto.setCertificationResultId(createdCert.getId());
                            taskDto.setTestTask(existingTt);

                            if (certTask.getTaskParticipants() != null) {
                                for (PendingCertificationResultTestTaskParticipantDTO certTaskPart : certTask
                                        .getTaskParticipants()) {
                                    PendingTestParticipantDTO certPart = certTaskPart.getTestParticipant();
                                    if (certPart != null) {
                                        TestParticipantDTO existingPart = null;
                                        for (TestParticipantDTO currPart : testParticipantsAdded) {
                                            if (currPart.getPendingUniqueId().equals(certPart.getUniqueId())) {
                                                existingPart = currPart;
                                            }
                                        }
                                        if (existingPart == null) {
                                            TestParticipantDTO tp = new TestParticipantDTO();
                                            tp.setAgeRangeId(certPart.getAgeRangeId());
                                            tp.setAssistiveTechnologyNeeds(certPart.getAssistiveTechnologyNeeds());
                                            tp.setComputerExperienceMonths(certPart.getComputerExperienceMonths());
                                            tp.setEducationTypeId(certPart.getEducationTypeId());
                                            tp.setGender(certPart.getGender());
                                            tp.setOccupation(certPart.getOccupation());
                                            tp.setProductExperienceMonths(certPart.getProductExperienceMonths());
                                            tp.setProfessionalExperienceMonths(
                                                    certPart.getProfessionalExperienceMonths());

                                            // add participant
                                            existingPart = testParticipantDao.create(tp);
                                            existingPart.setPendingUniqueId(certPart.getUniqueId());
                                            testParticipantsAdded.add(existingPart);
                                        }
                                        taskDto.getTestTask().getParticipants().add(existingPart);
                                    }
                                }
                            }

                            certDao.addTestTaskMapping(taskDto);
                        }
                    }
                }
            }
        }

        // cqms
        // we only insert successful ones, but all of the ones in the pendingDTO
        // are successful
        if (pendingCp.getCqmCriterion() != null && pendingCp.getCqmCriterion().size() > 0) {
            for (PendingCqmCriterionDTO pendingCqm : pendingCp.getCqmCriterion()) {
                if (pendingCqm.isMeetsCriteria() && !StringUtils.isEmpty(pendingCqm.getVersion())) {
                    CQMCriterionDTO criterion = null;
                    if (pendingCqm.getCmsId().startsWith("CMS")) {
                        criterion = cqmCriterionDao.getCMSByNumberAndVersion(pendingCqm.getCmsId(),
                                pendingCqm.getVersion());

                        if (criterion == null) {
                            throw new EntityCreationException("Could not find a CQM with number "
                                    + pendingCqm.getCmsId() + " and version " + pendingCqm.getVersion() + ".");
                        }

                        CQMResultDTO cqmResultToCreate = new CQMResultDTO();
                        cqmResultToCreate.setCqmCriterionId(criterion.getId());
                        cqmResultToCreate.setCertifiedProductId(newCertifiedProduct.getId());
                        cqmResultToCreate.setSuccess(pendingCqm.isMeetsCriteria());
                        if (pendingCqm.getCertifications() != null) {
                            for (PendingCqmCertificationCriterionDTO cert : pendingCqm.getCertifications()) {
                                CQMResultCriteriaDTO certDto = new CQMResultCriteriaDTO();
                                if (!StringUtils.isEmpty(cert.getCertificationId())) {
                                    certDto.setCriterionId(cert.getCertificationId());
                                    cqmResultToCreate.getCriteria().add(certDto);
                                } else if (!StringUtils.isEmpty(cert.getCertificationCriteriaNumber())) {
                                    CertificationCriterionDTO critDto = certCriterionDao
                                            .getByName(cert.getCertificationCriteriaNumber());
                                    if (critDto != null) {
                                        certDto.setCriterionId(critDto.getId());
                                        cqmResultToCreate.getCriteria().add(certDto);
                                    } else {
                                        LOGGER.error("Could not find a matching certification criterion for '"
                                                + cert.getCertificationCriteriaNumber() + "'.");
                                    }
                                } else {
                                    LOGGER.error("Neither certification id or number was specified.");
                                }
                            }
                        }
                        cqmResultDAO.create(cqmResultToCreate);
                    }
                }
            }
        }

        // if all this was successful, insert a certification status event for
        // the certification date
        CertificationStatusDTO activeCertStatus = certStatusDao
                .getByStatusName(CertificationStatusType.Active.toString());
        CertificationStatusEventDTO certEvent = new CertificationStatusEventDTO();
        certEvent.setCreationDate(new Date());
        certEvent.setDeleted(false);
        Date certificationDate = pendingCp.getCertificationDate();
        certEvent.setEventDate(certificationDate);
        certEvent.setStatus(activeCertStatus);
        certEvent.setCertifiedProductId(newCertifiedProduct.getId());
        statusEventDao.create(certEvent);
        
        return newCertifiedProduct;
    }

    @Override
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Transactional(readOnly = false)
    @ClearAllCaches
    public CertifiedProductDTO changeOwnership(Long certifiedProductId, Long acbId)
            throws EntityRetrievalException, JsonProcessingException, EntityCreationException {
        CertifiedProductDTO toUpdate = cpDao.getById(certifiedProductId);
        toUpdate.setCertificationBodyId(acbId);
        return cpDao.update(toUpdate);
    }

    @Override
    @PreAuthorize("hasRole('ROLE_ADMIN') or " + "( (hasRole('ROLE_ACB_STAFF') or hasRole('ROLE_ACB_ADMIN'))"
            + "  and hasPermission(#acbId, 'gov.healthit.chpl.dto.CertificationBodyDTO', admin)" + ")")
    @Transactional(readOnly = false)
    public void sanitizeUpdatedListingData(Long acbId, CertifiedProductSearchDetails listing)
            throws EntityNotFoundException {
        // make sure the ui didn't send any error or warning messages back
        listing.setErrorMessages(new HashSet<String>());
        listing.setWarningMessages(new HashSet<String>());

        // make sure IDs are filled in for all parents for the updated listing
        if (listing.getIcs() != null && listing.getIcs().getParents() != null
                && listing.getIcs().getParents().size() > 0) {
            for (CertifiedProduct parent : listing.getIcs().getParents()) {
                if (parent.getId() == null && !StringUtils.isEmpty(parent.getChplProductNumber())) {
                    try {
                    CertifiedProduct found = searchDao.getByChplProductNumber(parent.getChplProductNumber());
                        if (found != null) {
                            parent.setId(found.getId());
                        }
                    } catch(Exception ignore) {}
                } else if (parent.getId() == null) {
                    throw new EntityNotFoundException(
                            "Every ICS parent must have either a CHPL ID or a CHPL Product Number.");
                }
            }
        }

        // make sure IDs are filled in for all children for the updated listing
        if (listing.getIcs() != null && listing.getIcs().getChildren() != null
                && listing.getIcs().getChildren().size() > 0) {
            for (CertifiedProduct child : listing.getIcs().getChildren()) {
                if (child.getId() == null && !StringUtils.isEmpty(child.getChplProductNumber())) {
                    CertifiedProduct found = searchDao.getByChplProductNumber(child.getChplProductNumber());
                    if (found != null) {
                        child.setId(found.getId());
                    }
                } else if (child.getId() == null) {
                    throw new EntityNotFoundException(
                            "Every ICS child must have either a CHPL ID or a CHPL Product Number.");
                }
            }
        }
    }

    @Override
    @PreAuthorize("hasRole('ROLE_ADMIN') or " + "( (hasRole('ROLE_ACB_STAFF') or hasRole('ROLE_ACB_ADMIN'))"
            + "  and hasPermission(#acbId, 'gov.healthit.chpl.dto.CertificationBodyDTO', admin)" + ")")
    @Transactional(rollbackFor = {
            EntityRetrievalException.class, EntityCreationException.class, JsonProcessingException.class,
            AccessDeniedException.class, InvalidArgumentsException.class
    })
    @CacheEvict(value = {
            CacheNames.ALL_DEVELOPERS, CacheNames.ALL_DEVELOPERS_INCLUDING_DELETED, CacheNames.COLLECTIONS_DEVELOPERS,
            CacheNames.SEARCH, CacheNames.COUNT_MULTI_FILTER_SEARCH_RESULTS
    }, allEntries = true)
    public CertifiedProductDTO update(Long acbId, ListingUpdateRequest updateRequest,
            CertifiedProductSearchDetails existingListing) throws AccessDeniedException, EntityRetrievalException,
            JsonProcessingException, EntityCreationException, InvalidArgumentsException {

        CertifiedProductSearchDetails updatedListing = updateRequest.getListing();
        Long listingId = updatedListing.getId();
        Long certificationStatusId = new Long(updatedListing.getCertificationStatus().get("id").toString());
        Long productVersionId = new Long(updatedListing.getVersion().getVersionId());

        // look at the updated status and see if a developer ban is appropriate
        CertificationStatusDTO updatedCertificationStatus = certStatusDao.getById(certificationStatusId);
        DeveloperDTO cpDeveloper = developerDao.getByVersion(productVersionId);
        if (cpDeveloper == null) {
            LOGGER.error("Could not find developer for product version with id " + productVersionId);
            throw new EntityNotFoundException(
                    "No developer could be located for the certified product in the update. Update cannot continue.");
        }
        DeveloperStatusDTO newDevStatusDto = null;
        switch (CertificationStatusType.getValue(updatedCertificationStatus.getStatus())) {
        case SuspendedByOnc:
        case TerminatedByOnc:
            // only onc admin can do this and it always triggers developer ban
            if (Util.isUserRoleAdmin()) {
                // find the new developer status
                if (updatedCertificationStatus.getStatus().equals(CertificationStatusType.SuspendedByOnc.toString())) {
                    newDevStatusDto = devStatusDao.getByName(DeveloperStatusType.SuspendedByOnc.toString());
                } else if (updatedCertificationStatus.getStatus()
                        .equals(CertificationStatusType.TerminatedByOnc.toString())) {
                    newDevStatusDto = devStatusDao.getByName(DeveloperStatusType.UnderCertificationBanByOnc.toString());
                }
            } else if (!Util.isUserRoleAdmin()) {
                LOGGER.error("User " + Util.getUsername()
                        + " does not have ROLE_ADMIN and cannot change the status of developer for certified product with id "
                        + listingId);
                throw new AccessDeniedException(
                        "User does not have admin permission to change " + cpDeveloper.getName() + " status.");
            }
            break;
        case WithdrawnByDeveloperUnderReview:
            // conditionally change the status of the developer if the new
            // listing status
            // is withdrawn by dev under surv/review (acb admin and onc admin
            // can do this)
            if ((Util.isUserRoleAdmin() || Util.isUserRoleAcbAdmin())) {
                if (updateRequest.getBanDeveloper() != null && updateRequest.getBanDeveloper().booleanValue() == true) {
                    newDevStatusDto = devStatusDao.getByName(DeveloperStatusType.UnderCertificationBanByOnc.toString());
                } else {
                    LOGGER.info("Request was made to update listing status to " + updatedCertificationStatus.getStatus()
                            + " but not ban the developer.");
                }
            } else if (!Util.isUserRoleAdmin() && !Util.isUserRoleAcbAdmin()) {
                LOGGER.error("User " + Util.getUsername()
                        + " does not have ROLE_ADMIN or ROLE_ACB_ADMIN and cannot change the status of developer for certified product with id "
                        + listingId);
                throw new AccessDeniedException(
                        "User does not have admin permission to change " + cpDeveloper.getName() + " status.");
            }
            break;
        default:
            LOGGER.info("New listing status is " + updatedCertificationStatus.getStatus()
                    + " which does not trigger a developer ban.");
            break;
        }
        if (newDevStatusDto != null) {
            DeveloperStatusEventDTO statusHistoryToAdd = new DeveloperStatusEventDTO();
            statusHistoryToAdd.setDeveloperId(cpDeveloper.getId());
            statusHistoryToAdd.setStatus(newDevStatusDto);
            statusHistoryToAdd.setStatusDate(new Date());
            cpDeveloper.getStatusEvents().add(statusHistoryToAdd);
            developerManager.update(cpDeveloper);
        }

        CertifiedProductDTO dtoToUpdate = new CertifiedProductDTO(updatedListing);
        CertifiedProductDTO result = cpDao.update(dtoToUpdate);
        if (updatedListing != null) {
            updateIcsChildren(listingId, existingListing.getIcs(), updatedListing.getIcs());
            updateIcsParents(listingId, existingListing.getIcs(), updatedListing.getIcs());
            updateQmsStandards(listingId, existingListing.getQmsStandards(), updatedListing.getQmsStandards());
            updateTargetedUsers(listingId, existingListing.getTargetedUsers(), updatedListing.getTargetedUsers());
            updateAccessibilityStandards(listingId, existingListing.getAccessibilityStandards(),
                    updatedListing.getAccessibilityStandards());
            updateCertificationDate(listingId, new Date(existingListing.getCertificationDate()),
                    new Date(updatedListing.getCertificationDate()));
            updateCertificationStatusEvents(listingId,
                    new Long(existingListing.getCertificationStatus().get("id").toString()),
                    new Long(updatedListing.getCertificationStatus().get("id").toString()));
            updateCertifications(result.getCertificationBodyId(), existingListing, updatedListing,
                    existingListing.getCertificationResults(), updatedListing.getCertificationResults());
            updateCqms(result, existingListing.getCqmResults(), updatedListing.getCqmResults());
        }
        return result;
    }

    /**
     * Intelligently determine what updates need to be made to ICS parents.
     * 
     * @param existingIcs
     * @param updatedIcs
     */
    private void updateIcsParents(Long listingId, InheritedCertificationStatus existingIcs,
            InheritedCertificationStatus updatedIcs) throws EntityCreationException {
        // update ics parents as necessary
        List<Long> parentIdsToAdd = new ArrayList<Long>();
        List<Long> parentIdsToRemove = new ArrayList<Long>();

        if (updatedIcs != null && updatedIcs.getParents() != null && updatedIcs.getParents().size() > 0) {
            if (existingIcs == null || existingIcs.getParents() == null || existingIcs.getParents().size() == 0) {
                // existing listing has no ics parents, add all from the update
                if (updatedIcs.getParents() != null && updatedIcs.getParents().size() > 0) {
                    for (CertifiedProduct parent : updatedIcs.getParents()) {
                        if (parent.getId() != null) {
                            parentIdsToAdd.add(parent.getId());
                        }
                    }
                }
            } else if (existingIcs.getParents().size() > 0) {
                // existing listing has parents, compare to the update to see if
                // any are different
                for (CertifiedProduct parent : updatedIcs.getParents()) {
                    boolean inExistingListing = false;
                    for (CertifiedProduct existingParent : existingIcs.getParents()) {
                        if (parent.getId().longValue() == existingParent.getId().longValue()) {
                            inExistingListing = true;
                        }
                    }

                    if (!inExistingListing) {
                        parentIdsToAdd.add(parent.getId());
                    }
                }
            }
        }

        if (existingIcs != null && existingIcs.getParents() != null && existingIcs.getParents().size() > 0) {
            // if the updated listing has no parents, remove them all from
            // existing
            if (updatedIcs == null || updatedIcs.getParents() == null || updatedIcs.getParents().size() == 0) {
                for (CertifiedProduct existingParent : existingIcs.getParents()) {
                    parentIdsToRemove.add(existingParent.getId());
                }
            } else if (updatedIcs.getParents().size() > 0) {
                for (CertifiedProduct existingParent : existingIcs.getParents()) {
                    boolean inUpdatedListing = false;
                    for (CertifiedProduct parent : updatedIcs.getParents()) {
                        if (existingParent.getId().longValue() == parent.getId().longValue()) {
                            inUpdatedListing = true;
                        }
                    }
                    if (!inUpdatedListing) {
                        parentIdsToRemove.add(existingParent.getId());
                    }
                }
            }
        }
        // run DAO updates
        for (Long parentIdToAdd : parentIdsToAdd) {
            ListingToListingMapDTO toAdd = new ListingToListingMapDTO();
            toAdd.setParentId(parentIdToAdd);
            toAdd.setChildId(listingId);
            listingGraphDao.createListingMap(toAdd);
        }

        for (Long parentIdToRemove : parentIdsToRemove) {
            ListingToListingMapDTO toDelete = new ListingToListingMapDTO();
            toDelete.setParentId(parentIdToRemove);
            toDelete.setChildId(listingId);
            listingGraphDao.deleteListingMap(toDelete);
        }
    }

    /**
     * Intelligently update the ICS children relationships
     * 
     * @param existingIcs
     * @param updatedIcs
     */
    private void updateIcsChildren(Long listingId, InheritedCertificationStatus existingIcs,
            InheritedCertificationStatus updatedIcs) throws EntityCreationException {
        // update ics children as necessary
        List<Long> childIdsToAdd = new ArrayList<Long>();
        List<Long> childIdsToRemove = new ArrayList<Long>();

        if (updatedIcs != null && updatedIcs.getChildren() != null && updatedIcs.getChildren().size() > 0) {
            if (existingIcs == null || existingIcs.getChildren() == null || existingIcs.getChildren().size() == 0) {
                // existing listing has no ics parents, add all from the update
                if (updatedIcs.getChildren() != null && updatedIcs.getChildren().size() > 0) {
                    for (CertifiedProduct child : updatedIcs.getChildren()) {
                        if (child.getId() != null) {
                            childIdsToAdd.add(child.getId());
                        }
                    }
                }
            } else if (existingIcs.getChildren().size() > 0) {
                // existing listing has children, compare to the update to see
                // if any are different
                for (CertifiedProduct child : updatedIcs.getChildren()) {
                    boolean inExistingListing = false;
                    for (CertifiedProduct existingChild : existingIcs.getChildren()) {
                        if (child.getId().longValue() == existingChild.getId().longValue()) {
                            inExistingListing = true;
                        }
                    }

                    if (!inExistingListing) {
                        childIdsToAdd.add(child.getId());
                    }
                }
            }
        }

        if (existingIcs != null && existingIcs.getChildren() != null && existingIcs.getChildren().size() > 0) {
            // if the updated listing has no children, remove them all from
            // existing
            if (updatedIcs == null || updatedIcs.getChildren() == null || updatedIcs.getChildren().size() == 0) {
                for (CertifiedProduct existingChild : existingIcs.getChildren()) {
                    childIdsToRemove.add(existingChild.getId());
                }
            } else if (updatedIcs.getChildren().size() > 0) {
                for (CertifiedProduct existingChild : existingIcs.getChildren()) {
                    boolean inUpdatedListing = false;
                    for (CertifiedProduct child : updatedIcs.getChildren()) {
                        if (existingChild.getId().longValue() == child.getId().longValue()) {
                            inUpdatedListing = true;
                        }
                    }
                    if (!inUpdatedListing) {
                        childIdsToRemove.add(existingChild.getId());
                    }
                }
            }
        }

        // update listings in dao
        for (Long childIdToAdd : childIdsToAdd) {
            ListingToListingMapDTO toAdd = new ListingToListingMapDTO();
            toAdd.setChildId(childIdToAdd);
            toAdd.setParentId(listingId);
            listingGraphDao.createListingMap(toAdd);
        }

        for (Long childIdToRemove : childIdsToRemove) {
            ListingToListingMapDTO toDelete = new ListingToListingMapDTO();
            toDelete.setChildId(childIdToRemove);
            toDelete.setParentId(listingId);
            listingGraphDao.deleteListingMap(toDelete);
        }
    }

    private int updateQmsStandards(Long listingId, List<CertifiedProductQmsStandard> existingQmsStandards,
            List<CertifiedProductQmsStandard> updatedQmsStandards)
            throws EntityCreationException, EntityRetrievalException, JsonProcessingException {

        int numChanges = 0;
        List<CertifiedProductQmsStandard> qmsToAdd = new ArrayList<CertifiedProductQmsStandard>();
        List<QmsStandardPair> qmsToUpdate = new ArrayList<QmsStandardPair>();
        List<Long> idsToRemove = new ArrayList<Long>();

        // figure out which QMS to add
        if (updatedQmsStandards != null && updatedQmsStandards.size() > 0) {
            if (existingQmsStandards == null || existingQmsStandards.size() == 0) {
                // existing listing has none, add all from the update
                for (CertifiedProductQmsStandard updatedItem : updatedQmsStandards) {
                    qmsToAdd.add(updatedItem);
                }
            } else if (existingQmsStandards.size() > 0) {
                // existing listing has some, compare to the update to see if
                // any are different
                for (CertifiedProductQmsStandard updatedItem : updatedQmsStandards) {
                    boolean inExistingListing = false;
                    for (CertifiedProductQmsStandard existingItem : existingQmsStandards) {
                        if (updatedItem.matches(existingItem)) {
                            inExistingListing = true;
                            qmsToUpdate.add(new QmsStandardPair(existingItem, updatedItem));
                        }
                    }

                    if (!inExistingListing) {
                        qmsToAdd.add(updatedItem);
                    }
                }
            }
        }

        // figure out which QMS to remove
        if (existingQmsStandards != null && existingQmsStandards.size() > 0) {
            // if the updated listing has none, remove them all from existing
            if (updatedQmsStandards == null || updatedQmsStandards.size() == 0) {
                for (CertifiedProductQmsStandard existingItem : existingQmsStandards) {
                    idsToRemove.add(existingItem.getId());
                }
            } else if (updatedQmsStandards.size() > 0) {
                for (CertifiedProductQmsStandard existingItem : existingQmsStandards) {
                    boolean inUpdatedListing = false;
                    for (CertifiedProductQmsStandard updatedItem : updatedQmsStandards) {
                        inUpdatedListing = !inUpdatedListing ? existingItem.matches(updatedItem) : inUpdatedListing;
                    }
                    if (!inUpdatedListing) {
                        idsToRemove.add(existingItem.getId());
                    }
                }
            }
        }

        numChanges = qmsToAdd.size() + idsToRemove.size();
        for (CertifiedProductQmsStandard toAdd : qmsToAdd) {
            QmsStandardDTO qmsItem = qmsDao.findOrCreate(toAdd.getQmsStandardId(), toAdd.getQmsStandardName());
            CertifiedProductQmsStandardDTO qmsDto = new CertifiedProductQmsStandardDTO();
            qmsDto.setApplicableCriteria(toAdd.getApplicableCriteria());
            qmsDto.setCertifiedProductId(listingId);
            qmsDto.setQmsModification(toAdd.getQmsModification());
            qmsDto.setQmsStandardId(qmsItem.getId());
            qmsDto.setQmsStandardName(qmsItem.getName());
            cpQmsDao.createCertifiedProductQms(qmsDto);
        }

        for (QmsStandardPair toUpdate : qmsToUpdate) {
            boolean hasChanged = false;
            if (!ObjectUtils.equals(toUpdate.getOrig().getApplicableCriteria(),
                    toUpdate.getUpdated().getApplicableCriteria())
                    || !ObjectUtils.equals(toUpdate.getOrig().getQmsModification(),
                            toUpdate.getUpdated().getQmsModification())) {
                hasChanged = true;
            }

            if (hasChanged) {
                CertifiedProductQmsStandard stdToUpdate = toUpdate.getUpdated();
                QmsStandardDTO qmsItem = qmsDao.findOrCreate(stdToUpdate.getQmsStandardId(),
                        stdToUpdate.getQmsStandardName());
                CertifiedProductQmsStandardDTO qmsDto = new CertifiedProductQmsStandardDTO();
                qmsDto.setId(stdToUpdate.getId());
                qmsDto.setApplicableCriteria(stdToUpdate.getApplicableCriteria());
                qmsDto.setCertifiedProductId(listingId);
                qmsDto.setQmsModification(stdToUpdate.getQmsModification());
                qmsDto.setQmsStandardId(qmsItem.getId());
                qmsDto.setQmsStandardName(qmsItem.getName());
                cpQmsDao.updateCertifiedProductQms(qmsDto);
                numChanges++;
            }
        }

        for (Long idToRemove : idsToRemove) {
            cpQmsDao.deleteCertifiedProductQms(idToRemove);
        }
        return numChanges;
    }

    private int updateTargetedUsers(Long listingId, List<CertifiedProductTargetedUser> existingTargetedUsers,
            List<CertifiedProductTargetedUser> updatedTargetedUsers)
            throws EntityCreationException, EntityRetrievalException, JsonProcessingException {

        int numChanges = 0;
        List<CertifiedProductTargetedUser> tusToAdd = new ArrayList<CertifiedProductTargetedUser>();
        List<Long> idsToRemove = new ArrayList<Long>();

        // figure out which targeted user to add
        if (updatedTargetedUsers != null && updatedTargetedUsers.size() > 0) {
            if (existingTargetedUsers == null || existingTargetedUsers.size() == 0) {
                // existing listing has none, add all from the update
                for (CertifiedProductTargetedUser updatedItem : updatedTargetedUsers) {
                    tusToAdd.add(updatedItem);
                }
            } else if (existingTargetedUsers.size() > 0) {
                // existing listing has some, compare to the update to see if
                // any are different
                for (CertifiedProductTargetedUser updatedItem : updatedTargetedUsers) {
                    boolean inExistingListing = false;
                    for (CertifiedProductTargetedUser existingItem : existingTargetedUsers) {
                        inExistingListing = !inExistingListing ? updatedItem.matches(existingItem) : inExistingListing;
                    }

                    if (!inExistingListing) {
                        tusToAdd.add(updatedItem);
                    }
                }
            }
        }

        // figure out which targeted users to remove
        if (existingTargetedUsers != null && existingTargetedUsers.size() > 0) {
            // if the updated listing has none, remove them all from existing
            if (updatedTargetedUsers == null || updatedTargetedUsers.size() == 0) {
                for (CertifiedProductTargetedUser existingItem : existingTargetedUsers) {
                    idsToRemove.add(existingItem.getId());
                }
            } else if (updatedTargetedUsers.size() > 0) {
                for (CertifiedProductTargetedUser existingItem : existingTargetedUsers) {
                    boolean inUpdatedListing = false;
                    for (CertifiedProductTargetedUser updatedItem : updatedTargetedUsers) {
                        inUpdatedListing = !inUpdatedListing ? existingItem.matches(updatedItem) : inUpdatedListing;
                    }
                    if (!inUpdatedListing) {
                        idsToRemove.add(existingItem.getId());
                    }
                }
            }
        }

        numChanges = tusToAdd.size() + idsToRemove.size();
        for (CertifiedProductTargetedUser toAdd : tusToAdd) {
            TargetedUserDTO item = targetedUserDao.findOrCreate(toAdd.getTargetedUserId(), toAdd.getTargetedUserName());
            CertifiedProductTargetedUserDTO tuDto = new CertifiedProductTargetedUserDTO();
            tuDto.setTargetedUserId(item.getId());
            tuDto.setTargetedUserName(item.getName());
            tuDto.setCertifiedProductId(listingId);
            cpTargetedUserDao.createCertifiedProductTargetedUser(tuDto);
        }

        for (Long idToRemove : idsToRemove) {
            cpTargetedUserDao.deleteCertifiedProductTargetedUser(idToRemove);
        }
        return numChanges;
    }

    private int updateAccessibilityStandards(Long listingId,
            List<CertifiedProductAccessibilityStandard> existingAccessibilityStandards,
            List<CertifiedProductAccessibilityStandard> updatedAccessibilityStandards)
            throws EntityCreationException, EntityRetrievalException, JsonProcessingException {

        int numChanges = 0;
        List<CertifiedProductAccessibilityStandard> accStdsToAdd = new ArrayList<CertifiedProductAccessibilityStandard>();
        List<Long> idsToRemove = new ArrayList<Long>();

        // figure out which accessibility standards to add
        if (updatedAccessibilityStandards != null && updatedAccessibilityStandards.size() > 0) {
            if (existingAccessibilityStandards == null || existingAccessibilityStandards.size() == 0) {
                // existing listing has none, add all from the update
                for (CertifiedProductAccessibilityStandard updatedItem : updatedAccessibilityStandards) {
                    accStdsToAdd.add(updatedItem);
                }
            } else if (existingAccessibilityStandards.size() > 0) {
                // existing listing has some, compare to the update to see if
                // any are different
                for (CertifiedProductAccessibilityStandard updatedItem : updatedAccessibilityStandards) {
                    boolean inExistingListing = false;
                    for (CertifiedProductAccessibilityStandard existingItem : existingAccessibilityStandards) {
                        inExistingListing = !inExistingListing ? updatedItem.matches(existingItem) : inExistingListing;
                    }

                    if (!inExistingListing) {
                        accStdsToAdd.add(updatedItem);
                    }
                }
            }
        }

        // figure out which targeted users to remove
        if (existingAccessibilityStandards != null && existingAccessibilityStandards.size() > 0) {
            // if the updated listing has none, remove them all from existing
            if (updatedAccessibilityStandards == null || updatedAccessibilityStandards.size() == 0) {
                for (CertifiedProductAccessibilityStandard existingItem : existingAccessibilityStandards) {
                    idsToRemove.add(existingItem.getId());
                }
            } else if (updatedAccessibilityStandards.size() > 0) {
                for (CertifiedProductAccessibilityStandard existingItem : existingAccessibilityStandards) {
                    boolean inUpdatedListing = false;
                    for (CertifiedProductAccessibilityStandard updatedItem : updatedAccessibilityStandards) {
                        inUpdatedListing = !inUpdatedListing ? existingItem.matches(updatedItem) : inUpdatedListing;
                    }
                    if (!inUpdatedListing) {
                        idsToRemove.add(existingItem.getId());
                    }
                }
            }
        }

        numChanges = accStdsToAdd.size() + idsToRemove.size();
        for (CertifiedProductAccessibilityStandard toAdd : accStdsToAdd) {
            AccessibilityStandardDTO item = asDao.findOrCreate(toAdd.getAccessibilityStandardId(),
                    toAdd.getAccessibilityStandardName());
            CertifiedProductAccessibilityStandardDTO toAddStd = new CertifiedProductAccessibilityStandardDTO();
            toAddStd.setAccessibilityStandardId(item.getId());
            toAddStd.setAccessibilityStandardName(item.getName());
            toAddStd.setCertifiedProductId(listingId);
            cpAccStdDao.createCertifiedProductAccessibilityStandard(toAddStd);
        }

        for (Long idToRemove : idsToRemove) {
            cpAccStdDao.deleteCertifiedProductAccessibilityStandards(idToRemove);
        }
        return numChanges;
    }

    private void updateCertificationDate(Long listingId, Date existingCertDate, Date newCertDate)
            throws EntityCreationException, EntityRetrievalException, JsonProcessingException {
        if (existingCertDate != null && newCertDate != null && existingCertDate.getTime() != newCertDate.getTime()) {
            CertificationStatusEventDTO certificationEvent = statusEventDao
                    .findInitialCertificationEventForCertifiedProduct(listingId);
            if (certificationEvent != null) {
                certificationEvent.setEventDate(newCertDate);
                statusEventDao.update(certificationEvent);
            }
        }
    }

    private void updateCertificationStatusEvents(Long listingId, Long existingCertificationStatusId,
            Long updatedCertificationStatusId)
            throws EntityCreationException, EntityRetrievalException, JsonProcessingException {
        if (existingCertificationStatusId != null && updatedCertificationStatusId != null
                && existingCertificationStatusId.longValue() != updatedCertificationStatusId.longValue()) {
            CertificationStatusEventDTO certificationEvent = new CertificationStatusEventDTO();
            certificationEvent.setCertifiedProductId(listingId);
            certificationEvent.setEventDate(new Date());
            CertificationStatusDTO status = certStatusDao.getById(updatedCertificationStatusId);
            if (status == null) {
                throw new EntityRetrievalException(
                        "No certification status found with id " + updatedCertificationStatusId);
            }
            certificationEvent.setStatus(status);
            statusEventDao.create(certificationEvent);
        }
    }

    private int updateCertifications(Long acbId, CertifiedProductSearchDetails existingListing,
            CertifiedProductSearchDetails updatedListing, List<CertificationResult> existingCertifications,
            List<CertificationResult> updatedCertifications)
            throws EntityCreationException, EntityRetrievalException, JsonProcessingException {

        int numChanges = 0;

        // replace the value of the result. we shouldn't have to add or delete
        // any cert results
        // because for certification criteria, all results are always there
        // whether they were
        // successful or not

        for (CertificationResult updatedItem : updatedCertifications) {
            for (CertificationResult existingItem : existingCertifications) {
                if (!StringUtils.isEmpty(updatedItem.getNumber()) && !StringUtils.isEmpty(existingItem.getNumber())
                        && updatedItem.getNumber().equals(existingItem.getNumber())) {
                    numChanges += certResultManager.update(acbId, existingListing, updatedListing, existingItem,
                            updatedItem);
                }
            }
        }

        return numChanges;
    }

    private int updateCqms(CertifiedProductDTO listing, List<CQMResultDetails> existingCqmDetails,
            List<CQMResultDetails> updatedCqmDetails)
            throws EntityCreationException, EntityRetrievalException, JsonProcessingException {
        // convert to CQMResultDetailsDTO since CMS CQMs can have multiple
        // entries
        // per success version. work with these objects instead of the passed-in
        // ones
        List<CQMResultDetailsDTO> existingCqms = new ArrayList<CQMResultDetailsDTO>();
        for (CQMResultDetails existingItem : existingCqmDetails) {
            List<CQMResultDetailsDTO> toAdd = convert(existingItem);
            existingCqms.addAll(toAdd);
        }
        List<CQMResultDetailsDTO> updatedCqms = new ArrayList<CQMResultDetailsDTO>();
        for (CQMResultDetails updatedItem : updatedCqmDetails) {
            List<CQMResultDetailsDTO> toAdd = convert(updatedItem);
            updatedCqms.addAll(toAdd);
        }

        int numChanges = 0;
        List<CQMResultDetailsDTO> cqmsToAdd = new ArrayList<CQMResultDetailsDTO>();
        List<CQMResultDetailsPair> cqmsToUpdate = new ArrayList<CQMResultDetailsPair>();
        List<Long> idsToRemove = new ArrayList<Long>();

        // figure out which cqms to add
        if (updatedCqms != null && updatedCqms.size() > 0) {
            // existing listing has some, compare to the update to see if any
            // are different
            for (CQMResultDetailsDTO updatedItem : updatedCqms) {
                boolean inExistingListing = false;
                for (CQMResultDetailsDTO existingItem : existingCqms) {
                    if (!inExistingListing && StringUtils.isEmpty(updatedItem.getCmsId())
                            && StringUtils.isEmpty(existingItem.getCmsId())
                            && !StringUtils.isEmpty(updatedItem.getNqfNumber())
                            && !StringUtils.isEmpty(existingItem.getNqfNumber())
                            && !updatedItem.getNqfNumber().equals("N/A") && !existingItem.getNqfNumber().equals("N/A")
                            && updatedItem.getNqfNumber().equals(existingItem.getNqfNumber())) {
                        // NQF is the same if the NQF numbers are equal
                        inExistingListing = true;
                        cqmsToUpdate.add(new CQMResultDetailsPair(existingItem, updatedItem));
                    } else if (!inExistingListing && updatedItem.getCmsId() != null && existingItem.getCmsId() != null
                            && updatedItem.getCmsId().equals(existingItem.getCmsId())
                            && updatedItem.getVersion() != null && existingItem.getVersion() != null
                            && updatedItem.getVersion().equals(existingItem.getVersion())) {
                        // CMS is the same if the CMS ID and version is equal
                        inExistingListing = true;
                        cqmsToUpdate.add(new CQMResultDetailsPair(existingItem, updatedItem));
                    }
                }

                if (!inExistingListing) {
                    cqmsToAdd.add(updatedItem);
                }
            }
        }

        // figure out which cqms to remove
        if (existingCqms != null && existingCqms.size() > 0) {
            for (CQMResultDetailsDTO existingItem : existingCqms) {
                boolean inUpdatedListing = false;
                for (CQMResultDetailsDTO updatedItem : updatedCqms) {
                    if (!inUpdatedListing && StringUtils.isEmpty(updatedItem.getCmsId())
                            && StringUtils.isEmpty(existingItem.getCmsId())
                            && !StringUtils.isEmpty(updatedItem.getNqfNumber())
                            && !StringUtils.isEmpty(existingItem.getNqfNumber())
                            && !updatedItem.getNqfNumber().equals("N/A") && !existingItem.getNqfNumber().equals("N/A")
                            && updatedItem.getNqfNumber().equals(existingItem.getNqfNumber())) {
                        // NQF is the same if the NQF numbers are equal
                        inUpdatedListing = true;
                    } else if (!inUpdatedListing && updatedItem.getCmsId() != null && existingItem.getCmsId() != null
                            && updatedItem.getCmsId().equals(existingItem.getCmsId())
                            && updatedItem.getVersion() != null && existingItem.getVersion() != null
                            && updatedItem.getVersion().equals(existingItem.getVersion())) {
                        // CMS is the same if the CMS ID and version is equal
                        inUpdatedListing = true;
                    }
                }
                if (!inUpdatedListing) {
                    idsToRemove.add(existingItem.getId());
                }
            }
        }

        numChanges = cqmsToAdd.size() + idsToRemove.size();

        for (CQMResultDetailsDTO toAdd : cqmsToAdd) {
            CQMCriterionDTO criterion = null;
            if (StringUtils.isEmpty(toAdd.getCmsId())) {
                criterion = cqmCriterionDao.getNQFByNumber(toAdd.getNumber());
            } else if (toAdd.getCmsId().startsWith("CMS")) {
                criterion = cqmCriterionDao.getCMSByNumberAndVersion(toAdd.getCmsId(), toAdd.getVersion());
            }
            if (criterion == null) {
                throw new EntityRetrievalException(
                        "Could not find CQM with number " + toAdd.getCmsId() + " and version " + toAdd.getVersion());
            }

            CQMResultDTO newCQMResult = new CQMResultDTO();
            newCQMResult.setCertifiedProductId(listing.getId());
            newCQMResult.setCqmCriterionId(criterion.getId());
            newCQMResult.setCreationDate(new Date());
            newCQMResult.setDeleted(false);
            newCQMResult.setSuccess(true);
            CQMResultDTO created = cqmResultDAO.create(newCQMResult);
            if (toAdd.getCriteria() != null && toAdd.getCriteria().size() > 0) {
                for (CQMResultCriteriaDTO criteria : toAdd.getCriteria()) {
                    criteria.setCqmResultId(created.getId());
                    Long mappedCriterionId = findCqmCriterionId(criteria);
                    criteria.setCriterionId(mappedCriterionId);
                    cqmResultDAO.createCriteriaMapping(criteria);
                }
            }
        }

        for (CQMResultDetailsPair toUpdate : cqmsToUpdate) {
            numChanges += updateCqm(listing, toUpdate.getOrig(), toUpdate.getUpdated());
        }

        for (Long idToRemove : idsToRemove) {
            cqmResultDAO.deleteMappingsForCqmResult(idToRemove);
            cqmResultDAO.delete(idToRemove);
        }

        return numChanges;
    }

    private int updateCqm(CertifiedProductDTO listing, CQMResultDetailsDTO existingCqm, CQMResultDetailsDTO updatedCqm)
            throws EntityRetrievalException {
        int numChanges = 0;
        // look for changes in the cqms and update if necessary
        if (!ObjectUtils.equals(existingCqm.getSuccess(), updatedCqm.getSuccess())) {
            CQMResultDTO toUpdate = new CQMResultDTO();
            toUpdate.setId(existingCqm.getId());
            toUpdate.setCertifiedProductId(listing.getId());
            toUpdate.setCqmCriterionId(updatedCqm.getCqmCriterionId());
            toUpdate.setSuccess(updatedCqm.getSuccess());
            cqmResultDAO.update(toUpdate);
        }

        // need to compare existing with updated cqm criteria in case there are
        // differences
        List<CQMResultCriteriaDTO> criteriaToAdd = new ArrayList<CQMResultCriteriaDTO>();
        List<CQMResultCriteriaDTO> criteriaToRemove = new ArrayList<CQMResultCriteriaDTO>();

        for (CQMResultCriteriaDTO existingItem : existingCqm.getCriteria()) {
            boolean exists = false;
            for (CQMResultCriteriaDTO updatedItem : updatedCqm.getCriteria()) {
                if (existingItem.getCriterion().getNumber().equals(updatedItem.getCriterion().getNumber())) {
                    exists = true;
                }
            }
            if (!exists) {
                criteriaToRemove.add(existingItem);
            }
        }

        for (CQMResultCriteriaDTO updatedItem : updatedCqm.getCriteria()) {
            boolean exists = false;
            for (CQMResultCriteriaDTO existingItem : existingCqm.getCriteria()) {
                if (existingItem.getCriterion().getNumber().equals(updatedItem.getCriterion().getNumber())) {
                    exists = true;
                }
            }
            if (!exists) {
                criteriaToAdd.add(updatedItem);
            }
        }

        numChanges = criteriaToAdd.size() + criteriaToRemove.size();
        for (CQMResultCriteriaDTO currToAdd : criteriaToAdd) {
            currToAdd.setCqmResultId(existingCqm.getId());
            Long mappedCriterionId = findCqmCriterionId(currToAdd);
            currToAdd.setCriterionId(mappedCriterionId);
            cqmResultDAO.createCriteriaMapping(currToAdd);
        }
        for (CQMResultCriteriaDTO currToRemove : criteriaToRemove) {
            cqmResultDAO.deleteCriteriaMapping(currToRemove.getId());
        }
        return numChanges;
    }

    private Long findCqmCriterionId(CQMResultCriteriaDTO cqm) throws EntityRetrievalException {
        if (cqm.getCriterionId() != null) {
            return cqm.getCriterionId();
        }
        if (cqm.getCriterion() != null && !StringUtils.isEmpty(cqm.getCriterion().getNumber())) {
            CertificationCriterionDTO cert = certCriterionDao.getByName(cqm.getCriterion().getNumber());
            if (cert != null) {
                return cert.getId();
            } else {
                throw new EntityRetrievalException(
                        "Could not find certification criteria with number " + cqm.getCriterion().getNumber());
            }
        } else if (cqm.getCriterion() != null && cqm.getCriterion().getId() != null) {
            return cqm.getCriterion().getId();
        } else {
            throw new EntityRetrievalException("A criteria id or number must be provided.");
        }
    }

    private List<CQMResultDetailsDTO> convert(CQMResultDetails cqm) {
        List<CQMResultDetailsDTO> result = new ArrayList<CQMResultDetailsDTO>();

        if (!StringUtils.isEmpty(cqm.getCmsId()) && cqm.getSuccessVersions() != null
                && cqm.getSuccessVersions().size() > 0) {
            for (String version : cqm.getSuccessVersions()) {
                CQMResultDetailsDTO dto = new CQMResultDetailsDTO();
                dto.setId(cqm.getId());
                dto.setNqfNumber(cqm.getNqfNumber());
                dto.setCmsId(cqm.getCmsId());
                dto.setNumber(cqm.getNumber());
                dto.setCmsId(cqm.getCmsId());
                dto.setNqfNumber(cqm.getNqfNumber());
                dto.setTitle(cqm.getTitle());
                dto.setVersion(version);
                dto.setSuccess(Boolean.TRUE);
                if (cqm.getCriteria() != null && cqm.getCriteria().size() > 0) {
                    for (CQMResultCertification criteria : cqm.getCriteria()) {
                        CQMResultCriteriaDTO cqmdto = new CQMResultCriteriaDTO();
                        cqmdto.setId(criteria.getId());
                        cqmdto.setCriterionId(criteria.getCertificationId());
                        CertificationCriterionDTO certDto = new CertificationCriterionDTO();
                        certDto.setNumber(criteria.getCertificationNumber());
                        cqmdto.setCriterion(certDto);
                        dto.getCriteria().add(cqmdto);
                    }
                }
                result.add(dto);
            }
        } else if (StringUtils.isEmpty(cqm.getCmsId())) {
            CQMResultDetailsDTO dto = new CQMResultDetailsDTO();
            dto.setId(cqm.getId());
            dto.setNqfNumber(cqm.getNqfNumber());
            dto.setCmsId(cqm.getCmsId());
            dto.setNumber(cqm.getNumber());
            dto.setCmsId(cqm.getCmsId());
            dto.setNqfNumber(cqm.getNqfNumber());
            dto.setTitle(cqm.getTitle());
            dto.setSuccess(cqm.isSuccess());
            result.add(dto);
        }
        return result;
    }

    @Override
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_ONC_STAFF')")
    @Transactional(readOnly = false)
    @CacheEvict(value = {
            CacheNames.GET_DECERTIFIED_DEVELOPERS, CacheNames.SEARCH
    }, allEntries = true)
    public MeaningfulUseUserResults updateMeaningfulUseUsers(Set<MeaningfulUseUser> meaningfulUseUserSet)
            throws EntityCreationException, EntityRetrievalException, IOException {
        MeaningfulUseUserResults meaningfulUseUserResults = new MeaningfulUseUserResults();
        List<MeaningfulUseUser> errors = new ArrayList<MeaningfulUseUser>();
        List<MeaningfulUseUser> results = new ArrayList<MeaningfulUseUser>();

        for (MeaningfulUseUser muu : meaningfulUseUserSet) {
            if (StringUtils.isEmpty(muu.getError())) {
                try {
                    // If bad input, add error for this MeaningfulUseUser and
                    // continue
                    if ((muu.getProductNumber() == null || muu.getProductNumber().isEmpty())) {
                        muu.setError("Line " + muu.getCsvLineNumber()
                                + ": Field \"chpl_product_number\" has invalid value: \"" + muu.getProductNumber()
                                + "\".");
                    } else if (muu.getNumberOfUsers() == null) {
                        muu.setError("Line " + muu.getCsvLineNumber()
                                + ": Field \"num_meaningful_users\" has invalid value: \"" + muu.getNumberOfUsers()
                                + "\".");
                    } else {
                        CertifiedProductDTO dto = new CertifiedProductDTO();
                        // check if 2014 edition CHPL Product Number exists
                        if (cpDao.getByChplNumber(muu.getProductNumber()) != null) {
                            dto.setChplProductNumber(muu.getProductNumber());
                            dto.setMeaningfulUseUsers(muu.getNumberOfUsers());
                        }
                        // check if 2015 edition CHPL Product Number exists
                        else if (cpDao.getByChplUniqueId(muu.getProductNumber()) != null) {
                            dto.setChplProductNumber(muu.getProductNumber());
                            dto.setMeaningfulUseUsers(muu.getNumberOfUsers());
                        }
                        // If neither exist, add error
                        else {
                            throw new EntityRetrievalException();
                        }

                        try {
                            CertifiedProductDTO returnDto = cpDao.updateMeaningfulUseUsers(dto);
                            muu.setCertifiedProductId(returnDto.getId());
                            results.add(muu);
                        } catch (final EntityRetrievalException e) {
                            muu.setError("Line " + muu.getCsvLineNumber()
                                    + ": Field \"chpl_product_number\" with value \"" + muu.getProductNumber()
                                    + "\" is invalid. " + "The provided \"chpl_product_number\" does not exist.");
                            errors.add(muu);
                        }
                    }
                } catch (Exception e) {
                    muu.setError("Line " + muu.getCsvLineNumber() + ": Field \"chpl_product_number\" with value \""
                            + muu.getProductNumber() + "\" is invalid. "
                            + "The provided \"chpl_product_number\" does not exist.");
                    errors.add(muu);
                }
            } else {
                errors.add(muu);
            }
        }

        meaningfulUseUserResults.setMeaningfulUseUsers(results);
        meaningfulUseUserResults.setErrors(errors);
        return meaningfulUseUserResults;
    }

    private class QmsStandardPair {
        CertifiedProductQmsStandard orig;
        CertifiedProductQmsStandard updated;

        public QmsStandardPair() {
        }

        public QmsStandardPair(CertifiedProductQmsStandard orig, CertifiedProductQmsStandard updated) {
            this.orig = orig;
            this.updated = updated;
        }

        public CertifiedProductQmsStandard getOrig() {
            return orig;
        }

        public void setOrig(final CertifiedProductQmsStandard orig) {
            this.orig = orig;
        }

        public CertifiedProductQmsStandard getUpdated() {
            return updated;
        }

        public void setUpdated(final CertifiedProductQmsStandard updated) {
            this.updated = updated;
        }

    }

    private class CQMResultDetailsPair {
        CQMResultDetailsDTO orig;
        CQMResultDetailsDTO updated;

        public CQMResultDetailsPair() {
        }

        public CQMResultDetailsPair(CQMResultDetailsDTO orig, CQMResultDetailsDTO updated) {
            this.orig = orig;
            this.updated = updated;
        }

        public CQMResultDetailsDTO getOrig() {
            return orig;
        }

        public void setOrig(final CQMResultDetailsDTO orig) {
            this.orig = orig;
        }

        public CQMResultDetailsDTO getUpdated() {
            return updated;
        }

        public void setUpdated(final CQMResultDetailsDTO updated) {
            this.updated = updated;
        }
    }
}

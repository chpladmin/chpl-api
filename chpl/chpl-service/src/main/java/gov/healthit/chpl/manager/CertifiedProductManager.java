package gov.healthit.chpl.manager;

import static org.quartz.JobKey.jobKey;
import static org.quartz.TriggerBuilder.newTrigger;
import static org.quartz.TriggerKey.triggerKey;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import javax.persistence.EntityNotFoundException;

import org.apache.commons.lang3.ObjectUtils;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.caching.CacheNames;
import gov.healthit.chpl.certifiedproduct.CertifiedProductDetailsManager;
import gov.healthit.chpl.dao.AccessibilityStandardDAO;
import gov.healthit.chpl.dao.CQMCriterionDAO;
import gov.healthit.chpl.dao.CQMResultDAO;
import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.dao.CertificationResultDAO;
import gov.healthit.chpl.dao.CertificationStatusDAO;
import gov.healthit.chpl.dao.CertificationStatusEventDAO;
import gov.healthit.chpl.dao.CertifiedProductAccessibilityStandardDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.CertifiedProductQmsStandardDAO;
import gov.healthit.chpl.dao.CertifiedProductSearchDAO;
import gov.healthit.chpl.dao.CertifiedProductSearchResultDAO;
import gov.healthit.chpl.dao.CertifiedProductTargetedUserDAO;
import gov.healthit.chpl.dao.CertifiedProductTestingLabDAO;
import gov.healthit.chpl.dao.CuresUpdateEventDAO;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.dao.DeveloperStatusDAO;
import gov.healthit.chpl.dao.FuzzyChoicesDAO;
import gov.healthit.chpl.dao.ListingGraphDAO;
import gov.healthit.chpl.dao.PromotingInteroperabilityUserDAO;
import gov.healthit.chpl.dao.QmsStandardDAO;
import gov.healthit.chpl.dao.TargetedUserDAO;
import gov.healthit.chpl.dao.TestDataDAO;
import gov.healthit.chpl.dao.TestFunctionalityDAO;
import gov.healthit.chpl.dao.TestParticipantDAO;
import gov.healthit.chpl.dao.TestProcedureDAO;
import gov.healthit.chpl.dao.TestStandardDAO;
import gov.healthit.chpl.dao.TestTaskDAO;
import gov.healthit.chpl.dao.TestToolDAO;
import gov.healthit.chpl.dao.TestingLabDAO;
import gov.healthit.chpl.dao.UcdProcessDAO;
import gov.healthit.chpl.domain.CQMResultCertification;
import gov.healthit.chpl.domain.CQMResultDetails;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationStatus;
import gov.healthit.chpl.domain.CertificationStatusEvent;
import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.CertifiedProductAccessibilityStandard;
import gov.healthit.chpl.domain.CertifiedProductQmsStandard;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.CertifiedProductTargetedUser;
import gov.healthit.chpl.domain.CertifiedProductTestingLab;
import gov.healthit.chpl.domain.IcsFamilyTreeNode;
import gov.healthit.chpl.domain.InheritedCertificationStatus;
import gov.healthit.chpl.domain.ListingMeasure;
import gov.healthit.chpl.domain.ListingUpdateRequest;
import gov.healthit.chpl.domain.PromotingInteroperabilityUser;
import gov.healthit.chpl.domain.activity.ActivityConcept;
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
import gov.healthit.chpl.dto.CertificationResultTestDataDTO;
import gov.healthit.chpl.dto.CertificationResultTestFunctionalityDTO;
import gov.healthit.chpl.dto.CertificationResultTestProcedureDTO;
import gov.healthit.chpl.dto.CertificationResultTestStandardDTO;
import gov.healthit.chpl.dto.CertificationResultTestTaskDTO;
import gov.healthit.chpl.dto.CertificationResultTestToolDTO;
import gov.healthit.chpl.dto.CertificationResultUcdProcessDTO;
import gov.healthit.chpl.dto.CertifiedProductAccessibilityStandardDTO;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.dto.CertifiedProductQmsStandardDTO;
import gov.healthit.chpl.dto.CertifiedProductTargetedUserDTO;
import gov.healthit.chpl.dto.CertifiedProductTestingLabDTO;
import gov.healthit.chpl.dto.ContactDTO;
import gov.healthit.chpl.dto.CuresUpdateEventDTO;
import gov.healthit.chpl.dto.DeveloperACBMapDTO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.DeveloperStatusDTO;
import gov.healthit.chpl.dto.DeveloperStatusEventDTO;
import gov.healthit.chpl.dto.FuzzyChoicesDTO;
import gov.healthit.chpl.dto.ListingToListingMapDTO;
import gov.healthit.chpl.dto.ProductDTO;
import gov.healthit.chpl.dto.ProductVersionDTO;
import gov.healthit.chpl.dto.QmsStandardDTO;
import gov.healthit.chpl.dto.TargetedUserDTO;
import gov.healthit.chpl.dto.TestDataDTO;
import gov.healthit.chpl.dto.TestFunctionalityDTO;
import gov.healthit.chpl.dto.TestParticipantDTO;
import gov.healthit.chpl.dto.TestProcedureDTO;
import gov.healthit.chpl.dto.TestStandardDTO;
import gov.healthit.chpl.dto.TestTaskDTO;
import gov.healthit.chpl.dto.TestToolDTO;
import gov.healthit.chpl.dto.TestingLabDTO;
import gov.healthit.chpl.dto.UcdProcessDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultAdditionalSoftwareDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultOptionalStandardDTO;
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
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductMeasureDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductQmsStandardDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductTargetedUserDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductTestingLabDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCqmCertificationCriterionDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCqmCriterionDTO;
import gov.healthit.chpl.dto.listing.pending.PendingTestParticipantDTO;
import gov.healthit.chpl.dto.listing.pending.PendingTestTaskDTO;
import gov.healthit.chpl.entity.CertificationStatusType;
import gov.healthit.chpl.entity.FuzzyType;
import gov.healthit.chpl.entity.developer.DeveloperStatusType;
import gov.healthit.chpl.entity.listing.CertificationResultOptionalStandardEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.exception.MissingReasonException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.listing.measure.ListingMeasureDAO;
import gov.healthit.chpl.manager.impl.SecuredManager;
import gov.healthit.chpl.optionalStandard.dao.OptionalStandardDAO;
import gov.healthit.chpl.optionalStandard.domain.CertificationResultOptionalStandard;
import gov.healthit.chpl.optionalStandard.domain.OptionalStandard;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.service.CuresUpdateService;
import gov.healthit.chpl.util.AuthUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.listing.ListingValidatorFactory;
import gov.healthit.chpl.validation.listing.Validator;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service("certifiedProductManager")
public class CertifiedProductManager extends SecuredManager {
    private ErrorMessageUtil msgUtil;
    private CertifiedProductDAO cpDao;
    private CertifiedProductSearchDAO searchDao;
    private CertificationResultDAO certDao;
    private CertificationCriterionDAO certCriterionDao;
    private QmsStandardDAO qmsDao;
    private TargetedUserDAO targetedUserDao;
    private AccessibilityStandardDAO asDao;
    private CertifiedProductQmsStandardDAO cpQmsDao;
    private ListingMeasureDAO cpMeasureDao;
    private CertifiedProductTestingLabDAO cpTestingLabDao;
    private CertifiedProductTargetedUserDAO cpTargetedUserDao;
    private CertifiedProductAccessibilityStandardDAO cpAccStdDao;
    private CQMResultDAO cqmResultDAO;
    private CQMCriterionDAO cqmCriterionDao;
    private TestingLabDAO atlDao;
    private DeveloperDAO developerDao;
    private DeveloperStatusDAO devStatusDao;
    private DeveloperManager developerManager;
    private ProductManager productManager;
    private ProductVersionManager versionManager;
    private CertificationStatusEventDAO statusEventDao;
    private CuresUpdateEventDAO curesUpdateDao;
    private PromotingInteroperabilityUserDAO piuDao;
    private CertificationResultManager certResultManager;
    private OptionalStandardDAO optionalStandardDao;
    private TestToolDAO testToolDao;
    private TestStandardDAO testStandardDao;
    private TestProcedureDAO testProcDao;
    private TestDataDAO testDataDao;
    private TestFunctionalityDAO testFuncDao;
    private UcdProcessDAO ucdDao;
    private TestParticipantDAO testParticipantDao;
    private TestTaskDAO testTaskDao;
    private CertificationStatusDAO certStatusDao;
    private ListingGraphDAO listingGraphDao;
    private FuzzyChoicesDAO fuzzyChoicesDao;
    private ResourcePermissions resourcePermissions;
    private CertifiedProductSearchResultDAO certifiedProductSearchResultDAO;
    private CertifiedProductDetailsManager certifiedProductDetailsManager;
    private PendingCertifiedProductManager pcpManager;
    private ActivityManager activityManager;
    private ListingValidatorFactory validatorFactory;
    private CuresUpdateService curesUpdateService;
    private CertificationCriterionService criteriaService;

    private static final int PROD_CODE_LOC = 4;
    private static final int VER_CODE_LOC = 5;
    private static final int ICS_CODE_LOC = 6;
    private static final int SW_CODE_LOC = 7;
    private static final int DATE_CODE_LOC = 8;

    public CertifiedProductManager() {
    }

    @SuppressWarnings({"checkstyle:parameternumber"})
    @Autowired
    public CertifiedProductManager(ErrorMessageUtil msgUtil,
            CertifiedProductDAO cpDao, CertifiedProductSearchDAO searchDao,
            CertificationResultDAO certDao, CertificationCriterionDAO certCriterionDao,
            QmsStandardDAO qmsDao, TargetedUserDAO targetedUserDao,
            AccessibilityStandardDAO asDao, CertifiedProductQmsStandardDAO cpQmsDao,
            ListingMeasureDAO cpMeasureDao,
            CertifiedProductTestingLabDAO cpTestingLabDao,
            CertifiedProductTargetedUserDAO cpTargetedUserDao,
            CertifiedProductAccessibilityStandardDAO cpAccStdDao, CQMResultDAO cqmResultDAO,
            CQMCriterionDAO cqmCriterionDao, TestingLabDAO atlDao,
            DeveloperDAO developerDao, DeveloperStatusDAO devStatusDao,
            @Lazy DeveloperManager developerManager, ProductManager productManager,
            ProductVersionManager versionManager, CertificationStatusEventDAO statusEventDao,
            CuresUpdateEventDAO curesUpdateDao,
            PromotingInteroperabilityUserDAO piuDao, CertificationResultManager certResultManager,
            OptionalStandardDAO optionalStandardDao,
            TestToolDAO testToolDao, TestStandardDAO testStandardDao,
            TestProcedureDAO testProcDao, TestDataDAO testDataDao,
            TestFunctionalityDAO testFuncDao, UcdProcessDAO ucdDao,
            TestParticipantDAO testParticipantDao, TestTaskDAO testTaskDao,
            CertificationStatusDAO certStatusDao, ListingGraphDAO listingGraphDao,
            FuzzyChoicesDAO fuzzyChoicesDao, ResourcePermissions resourcePermissions,
            CertifiedProductSearchResultDAO certifiedProductSearchResultDAO,
            CertifiedProductDetailsManager certifiedProductDetailsManager,
            PendingCertifiedProductManager pcpManager,
            ActivityManager activityManager, ListingValidatorFactory validatorFactory,
            CuresUpdateService curesUpdateService,
            CertificationCriterionService criteriaService) {

        this.msgUtil = msgUtil;
        this.cpDao = cpDao;
        this.searchDao = searchDao;
        this.certDao = certDao;
        this.certCriterionDao = certCriterionDao;
        this.qmsDao = qmsDao;
        this.targetedUserDao = targetedUserDao;
        this.asDao = asDao;
        this.cpQmsDao = cpQmsDao;
        this.cpMeasureDao = cpMeasureDao;
        this.cpTestingLabDao = cpTestingLabDao;
        this.cpTargetedUserDao = cpTargetedUserDao;
        this.cpAccStdDao = cpAccStdDao;
        this.cqmResultDAO = cqmResultDAO;
        this.cqmCriterionDao = cqmCriterionDao;
        this.atlDao = atlDao;
        this.developerDao = developerDao;
        this.devStatusDao = devStatusDao;
        this.developerManager = developerManager;
        this.productManager = productManager;
        this.versionManager = versionManager;
        this.statusEventDao = statusEventDao;
        this.curesUpdateDao = curesUpdateDao;
        this.piuDao = piuDao;
        this.certResultManager = certResultManager;
        this.optionalStandardDao = optionalStandardDao;
        this.testToolDao = testToolDao;
        this.testStandardDao = testStandardDao;
        this.testProcDao = testProcDao;
        this.testDataDao = testDataDao;
        this.testFuncDao = testFuncDao;
        this.ucdDao = ucdDao;
        this.testParticipantDao = testParticipantDao;
        this.testTaskDao = testTaskDao;
        this.certStatusDao = certStatusDao;
        this.listingGraphDao = listingGraphDao;
        this.fuzzyChoicesDao = fuzzyChoicesDao;
        this.resourcePermissions = resourcePermissions;
        this.certifiedProductSearchResultDAO = certifiedProductSearchResultDAO;
        this.certifiedProductDetailsManager = certifiedProductDetailsManager;
        this.pcpManager = pcpManager;
        this.activityManager = activityManager;
        this.validatorFactory = validatorFactory;
        this.curesUpdateService = curesUpdateService;
        this.criteriaService = criteriaService;
    }

    @Transactional(readOnly = true)
    public CertifiedProductDTO getById(Long id) throws EntityRetrievalException {
        CertifiedProductDTO result = cpDao.getById(id);
        return result;
    }

    @Transactional(readOnly = true)
    public CertifiedProductDTO getByChplProductNumber(String chplProductNumber) throws EntityRetrievalException {
        CertifiedProductDTO result = cpDao.getByChplNumber(chplProductNumber);
        return result;
    }

    @Transactional(readOnly = true)
    public List<CertifiedProductDetailsDTO> getByDeveloperId(Long developerId) throws EntityRetrievalException {
        return cpDao.findByDeveloperId(developerId);
    }

    @Transactional(readOnly = true)
    public List<CertifiedProductDetailsDTO> getDetailsByIds(List<Long> ids) throws EntityRetrievalException {
        return cpDao.getDetailsByIds(ids);
    }

    @Transactional(readOnly = true)
    public CertifiedProductDetailsDTO getDetailsById(Long ids) throws EntityRetrievalException {
        return cpDao.getDetailsById(ids);
    }

    @Transactional(readOnly = true)
    public List<CertifiedProductDetailsDTO> getAll() {
        return cpDao.findAll();
    }

    @Transactional(readOnly = true)
    public List<CertifiedProduct> getByVersion(Long versionId) throws EntityRetrievalException {
        versionManager.getById(versionId); // throws 404 if bad id
        return cpDao.getDetailsByVersionId(versionId);
    }

    @Transactional(readOnly = true)
    public List<CertifiedProductDetailsDTO> getByProduct(Long productId) throws EntityRetrievalException {
        productManager.getById(productId); // throws 404 if bad id
        return cpDao.getDetailsByProductId(productId);
    }

    @Transactional(readOnly = true)
    public List<CertifiedProduct> getByVersionWithEditPermission(Long versionId)
            throws EntityRetrievalException {
        versionManager.getById(versionId); // throws 404 if bad id
        List<CertificationBodyDTO> userAcbs = resourcePermissions.getAllAcbsForCurrentUser();
        if (userAcbs == null || userAcbs.size() == 0) {
            return new ArrayList<CertifiedProduct>();
        }
        List<Long> acbIdList = new ArrayList<Long>(userAcbs.size());
        for (CertificationBodyDTO dto : userAcbs) {
            acbIdList.add(dto.getId());
        }
        return cpDao.getDetailsByVersionAndAcbIds(versionId, acbIdList);
    }

    @Transactional
    public List<IcsFamilyTreeNode> getIcsFamilyTree(String chplProductNumber) throws EntityRetrievalException {

        CertifiedProductDetailsDTO dto = getCertifiedProductDetailsDtoByChplProductNumber(chplProductNumber);

        return getIcsFamilyTree(dto.getId());
    }

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

    @SuppressWarnings({"checkstyle:linelength", "checkstyle:methodlength"})
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).CERTIFIED_PRODUCT, "
            + "T(gov.healthit.chpl.permissions.domains.CertifiedProductDomainPermissions).CREATE_FROM_PENDING, #pendingCp)")
    @Transactional(readOnly = false)
    @CacheEvict(value = {
            CacheNames.ALL_DEVELOPERS, CacheNames.ALL_DEVELOPERS_INCLUDING_DELETED, CacheNames.COLLECTIONS_DEVELOPERS,
            CacheNames.COLLECTIONS_LISTINGS, CacheNames.COLLECTIONS_SEARCH, CacheNames.PRODUCT_NAMES, CacheNames.DEVELOPER_NAMES
    }, allEntries = true)
    public CertifiedProductDTO createFromPending(PendingCertifiedProductDTO pendingCp, boolean acknowledgeWarnings)
            throws EntityRetrievalException, EntityCreationException, IOException {

        CertifiedProductDTO toCreate = new CertifiedProductDTO();
        toCreate.setPendingCertifiedProductId(pendingCp.getId());
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
        toCreate.setLastModifiedUser(AuthUtil.getAuditId());

        if (pendingCp.getCertificationBodyId() == null) {
            throw new EntityCreationException("ACB ID must be specified.");
        }
        toCreate.setCertificationBodyId(pendingCp.getCertificationBodyId());

        if (pendingCp.getCertificationEditionId() == null) {
            throw new EntityCreationException("The ID of an existing certification edition (year) must be provided. "
                    + "  A new certification edition cannot be created via this process.");
        }
        toCreate.setCertificationEditionId(pendingCp.getCertificationEditionId());
        toCreate.setMandatoryDisclosures(pendingCp.getMandatoryDisclosures());
        toCreate.setSvapNoticeUrl(pendingCp.getSvapNoticeUrl());

        DeveloperDTO developer = null;
        if (pendingCp.getDeveloperId() == null) {
            DeveloperDTO newDeveloper = new DeveloperDTO();
            if (StringUtils.isEmpty(pendingCp.getDeveloperName())) {
                throw new EntityCreationException("You must provide a developer name to create a new developer.");
            }
            newDeveloper.setName(pendingCp.getDeveloperName());
            newDeveloper.setWebsite(pendingCp.getDeveloperWebsite());
            newDeveloper.setSelfDeveloper(pendingCp.getSelfDeveloper() == null ? Boolean.FALSE : pendingCp.getSelfDeveloper());
            DeveloperACBMapDTO transparencyMap = new DeveloperACBMapDTO();
            transparencyMap.setAcbId(pendingCp.getCertificationBodyId());
            transparencyMap.setAcbName(pendingCp.getCertificationBodyName());
            transparencyMap.setTransparencyAttestation(pendingCp.getTransparencyAttestation());
            newDeveloper.getTransparencyAttestationMappings().add(transparencyMap);
            AddressDTO developerAddress = pendingCp.getDeveloperAddress();
            newDeveloper.setAddress(developerAddress);
            ContactDTO developerContact = new ContactDTO();
            developerContact.setFullName(pendingCp.getDeveloperContactName());
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
            newProduct.getOwner().setId(pendingCp.getDeveloperId());
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
        toCreate.setProductCode(uniqueIdParts[PROD_CODE_LOC]);
        toCreate.setVersionCode(uniqueIdParts[VER_CODE_LOC]);
        toCreate.setIcsCode(uniqueIdParts[ICS_CODE_LOC]);
        toCreate.setAdditionalSoftwareCode(uniqueIdParts[SW_CODE_LOC]);
        toCreate.setCertifiedDateCode(uniqueIdParts[DATE_CODE_LOC]);

        CertifiedProductDTO newCertifiedProduct = cpDao.create(toCreate);

        // ATLs
        if (pendingCp.getTestingLabs() != null && pendingCp.getTestingLabs().size() > 0) {
            for (PendingCertifiedProductTestingLabDTO tl : pendingCp.getTestingLabs()) {
                CertifiedProductTestingLabDTO tlDto = new CertifiedProductTestingLabDTO();
                tlDto.setTestingLabId(atlDao.getByName(tl.getTestingLabName()).getId());
                tlDto.setTestingLabName(tl.getTestingLabName());
                tlDto.setCertifiedProductId(newCertifiedProduct.getId());
                cpTestingLabDao.createCertifiedProductTestingLab(tlDto);
            }
        } else {
            throw new EntityCreationException("ATL ID must be specified.");
        }

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

        List<String> fuzzyQmsChoices = fuzzyChoicesDao.getByType(FuzzyType.QMS_STANDARD).getChoices();

        // qms
        if (pendingCp.getQmsStandards() != null && pendingCp.getQmsStandards().size() > 0) {
            for (PendingCertifiedProductQmsStandardDTO pendingQms : pendingCp.getQmsStandards()) {
                if (!fuzzyQmsChoices.contains(pendingQms.getName())) {
                    fuzzyQmsChoices.add(pendingQms.getName());
                    FuzzyChoicesDTO dto = new FuzzyChoicesDTO();
                    dto.setFuzzyType(FuzzyType.QMS_STANDARD);
                    dto.setChoices(fuzzyQmsChoices);
                    fuzzyChoicesDao.update(dto);
                }
                CertifiedProductQmsStandardDTO qmsDto = new CertifiedProductQmsStandardDTO();
                QmsStandardDTO qms = qmsDao.findOrCreate(pendingQms.getQmsStandardId(), pendingQms.getName());
                qmsDto.setQmsStandardId(qms.getId());
                qmsDto.setCertifiedProductId(newCertifiedProduct.getId());
                qmsDto.setApplicableCriteria(pendingQms.getApplicableCriteria());
                qmsDto.setQmsModification(pendingQms.getModification());
                cpQmsDao.createCertifiedProductQms(qmsDto);
            }
        }

        if (pendingCp.getMeasures() != null && pendingCp.getMeasures().size() > 0) {
            for (PendingCertifiedProductMeasureDTO pendingMeasure : pendingCp.getMeasures()) {
                ListingMeasure measureToAdd = new ListingMeasure();
                measureToAdd.setMeasure(pendingMeasure.getMeasure());
                measureToAdd.setMeasureType(pendingMeasure.getMeasureType());
                measureToAdd.setAssociatedCriteria(pendingMeasure.getAssociatedCriteria());
                cpMeasureDao.createCertifiedProductMeasureMapping(newCertifiedProduct.getId(), measureToAdd);
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

        List<String> fuzzyAsChoices = fuzzyChoicesDao.getByType(FuzzyType.ACCESSIBILITY_STANDARD).getChoices();

        // accessibility standards
        if (pendingCp.getAccessibilityStandards() != null && pendingCp.getAccessibilityStandards().size() > 0) {
            for (PendingCertifiedProductAccessibilityStandardDTO as : pendingCp.getAccessibilityStandards()) {
                CertifiedProductAccessibilityStandardDTO asDto = new CertifiedProductAccessibilityStandardDTO();
                asDto.setCertifiedProductId(newCertifiedProduct.getId());
                if (!fuzzyAsChoices.contains(as.getName())) {
                    fuzzyAsChoices.add(as.getName());
                    FuzzyChoicesDTO dto = new FuzzyChoicesDTO();
                    dto.setFuzzyType(FuzzyType.ACCESSIBILITY_STANDARD);
                    dto.setChoices(fuzzyAsChoices);
                    fuzzyChoicesDao.update(dto);
                }
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
                CertificationCriterionDTO criterion = certCriterionDao.getByNumberAndTitle(
                        certResult.getCriterion().getNumber(), certResult.getCriterion().getTitle());
                if (criterion == null) {
                    throw new EntityCreationException(
                            "Could not find certification criterion with number " + certResult.getCriterion().getNumber());
                }
                CertificationResultDTO certResultToCreate = new CertificationResultDTO();
                certResultToCreate.setCertificationCriterionId(criterion.getId());
                certResultToCreate.setCertifiedProductId(newCertifiedProduct.getId());
                certResultToCreate.setSuccessful(certResult.getMeetsCriteria());
                boolean isCertified = (certResultToCreate.getSuccessful() != null
                        && certResultToCreate.getSuccessful().booleanValue());
                certResultToCreate.setGap(isCertified ? certResult.getGap() : null);
                certResultToCreate.setG1Success(certResult.getG1Success());
                certResultToCreate.setG2Success(certResult.getG2Success());

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
                certResultToCreate.setPrivacySecurityFramework(isCertified ? certResult.getPrivacySecurityFramework() : null);
                certResultToCreate.setAttestationAnswer(isCertified ? certResult.getAttestationAnswer() : null);
                certResultToCreate.setDocumentationUrl(isCertified ? certResult.getDocumentationUrl() : null);
                certResultToCreate.setExportDocumentation(isCertified ? certResult.getExportDocumentation() : null);
                certResultToCreate.setUseCases(isCertified ? certResult.getUseCases() : null);
                certResultToCreate.setServiceBaseUrlList(isCertified ? certResult.getServiceBaseUrlList() : null);
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

                    if (certResult.getOptionalStandards() != null && certResult.getOptionalStandards().size() > 0) {
                        for (PendingCertificationResultOptionalStandardDTO std : certResult.getOptionalStandards()) {
                            CertificationResultOptionalStandardEntity standard = new CertificationResultOptionalStandardEntity();
                            if (std.getOptionalStandardId() == null) {
                                OptionalStandard foundOptionalStandard = optionalStandardDao.getByCitation(std.getCitation());
                                if (foundOptionalStandard != null) {
                                    standard.setOptionalStandardId(foundOptionalStandard.getId());
                                } else {
                                    LOGGER.error("Will not insert optional standard with null id. Citation was " + std.getCitation());
                                }
                            } else {
                                standard.setOptionalStandardId(std.getOptionalStandardId());
                            }
                            standard.setCertificationResultId(createdCert.getId());
                            CertificationResultOptionalStandard existingMapping = certDao.lookupOptionalStandardMapping(
                                    standard.getCertificationResultId(), standard.getOptionalStandardId());
                            if (existingMapping == null) {
                                certDao.addOptionalStandardMapping(standard);
                            }
                        }
                    }

                    List<String> fuzzyUcdChoices = fuzzyChoicesDao.getByType(FuzzyType.UCD_PROCESS).getChoices();

                    if (certResult.getUcdProcesses() != null && certResult.getUcdProcesses().size() > 0) {
                        for (PendingCertificationResultUcdProcessDTO pendingUcd : certResult.getUcdProcesses()) {
                            if (!fuzzyUcdChoices.contains(pendingUcd.getUcdProcessName())) {
                                fuzzyUcdChoices.add(pendingUcd.getUcdProcessName());
                                FuzzyChoicesDTO dto = new FuzzyChoicesDTO();
                                dto.setFuzzyType(FuzzyType.UCD_PROCESS);
                                dto.setChoices(fuzzyUcdChoices);
                                fuzzyChoicesDao.update(dto);
                            }
                            CertificationResultUcdProcessDTO ucdDto = new CertificationResultUcdProcessDTO();
                            UcdProcessDTO ucd = ucdDao.findOrCreate(pendingUcd.getUcdProcessId(),
                                    pendingUcd.getUcdProcessName());

                            ucdDto.setUcdProcessId(ucd.getId());
                            ucdDto.setCertificationResultId(createdCert.getId());
                            ucdDto.setUcdProcessDetails(pendingUcd.getUcdProcessDetails());
                            certDao.addUcdProcessMapping(ucdDto);
                        }
                    }

                    if (certResult.getTestData() != null && certResult.getTestData().size() > 0) {
                        for (PendingCertificationResultTestDataDTO testData : certResult.getTestData()) {
                            CertificationResultTestDataDTO testDto = new CertificationResultTestDataDTO();
                            testDto.setAlteration(testData.getAlteration());
                            testDto.setVersion(testData.getVersion());
                            testDto.setCertificationResultId(createdCert.getId());
                            if (testData.getTestDataId() != null) {
                                testDto.setTestDataId(testData.getTestDataId());
                                testDto.setTestData(testData.getTestData());
                                certDao.addTestDataMapping(testDto);
                            } else if (testData.getTestData() != null) {
                                TestDataDTO foundTestData = testDataDao.getByCriterionAndValue(
                                        certResult.getCriterion().getId(), testData.getTestData().getName());
                                if (foundTestData == null) {
                                    LOGGER.error("Could not find test data for " + certResult.getCriterion().getNumber()
                                            + " and test data name " + testData.getTestData().getName());
                                } else {
                                    testDto.setTestData(foundTestData);
                                    testDto.setTestDataId(foundTestData.getId());
                                    certDao.addTestDataMapping(testDto);
                                }
                            } else {
                                LOGGER.error("No valid test data was supplied.");
                            }
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
                                    CertificationResultTestFunctionalityDTO funcDto
                                        = new CertificationResultTestFunctionalityDTO();

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
                            procDto.setVersion(proc.getVersion());
                            procDto.setCertificationResultId(createdCert.getId());

                            if (proc.getTestProcedureId() != null) {
                                procDto.setTestProcedureId(proc.getTestProcedureId());
                                procDto.setTestProcedure(proc.getTestProcedure());
                                certDao.addTestProcedureMapping(procDto);
                            } else if (proc.getTestProcedure() != null) {
                                // check again for a matching test procedure
                                // because
                                // the user could have edited it since upload
                                TestProcedureDTO foundTp = testProcDao.getByCriterionIdAndValue(
                                        certResult.getCriterion().getId(), proc.getTestProcedure().getName());
                                if (foundTp == null) {
                                    LOGGER.error("Could not find test procedure for " + certResult.getCriterion().getNumber()
                                            + " and test procedure name " + proc.getTestProcedure().getName());
                                } else {
                                    procDto.setTestProcedure(foundTp);
                                    procDto.setTestProcedureId(foundTp.getId());
                                    certDao.addTestProcedureMapping(procDto);
                                }
                            } else {
                                LOGGER.error("No valid test procedure was supplied.");
                            }
                        }
                    }

                    if (certResult.getTestStandards() != null && certResult.getTestStandards().size() > 0) {
                        for (PendingCertificationResultTestStandardDTO std : certResult.getTestStandards()) {
                            CertificationResultTestStandardDTO stdDto = new CertificationResultTestStandardDTO();
                            if (std.getTestStandardId() == null) {
                                // try to look up by name and edition
                                TestStandardDTO foundTestStandard = testStandardDao.getByNumberAndEdition(std.getName(),
                                        pendingCp.getCertificationEditionId());
                                if (foundTestStandard != null) {
                                    stdDto.setTestStandardId(foundTestStandard.getId());
                                } else {
                                    LOGGER.error("Will not insert test standard with null id. Name was " + std.getName());
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
                                tt.setTaskErrors(Float.valueOf(pendingTask.getTaskErrors()));
                                tt.setTaskErrorsStddev(Float.valueOf(pendingTask.getTaskErrorsStddev()));
                                tt.setTaskPathDeviationObserved(
                                        Integer.valueOf(pendingTask.getTaskPathDeviationObserved()));
                                tt.setTaskPathDeviationOptimal(
                                        Integer.valueOf(pendingTask.getTaskPathDeviationOptimal()));
                                tt.setTaskRating(Float.valueOf(pendingTask.getTaskRating()));
                                tt.setTaskRatingScale(pendingTask.getTaskRatingScale());
                                tt.setTaskRatingStddev(Float.valueOf(pendingTask.getTaskRatingStddev()));
                                tt.setTaskSuccessAverage(Float.valueOf(pendingTask.getTaskSuccessAverage()));
                                tt.setTaskSuccessStddev(Float.valueOf(pendingTask.getTaskSuccessStddev()));
                                tt.setTaskTimeAvg(Long.valueOf(pendingTask.getTaskTimeAvg()));
                                tt.setTaskTimeDeviationObservedAvg(
                                        Integer.valueOf(pendingTask.getTaskTimeDeviationObservedAvg()));
                                tt.setTaskTimeDeviationOptimalAvg(
                                        Integer.valueOf(pendingTask.getTaskTimeDeviationOptimalAvg()));
                                tt.setTaskTimeStddev(Integer.valueOf(pendingTask.getTaskTimeStddev()));

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
                            if (existingTt != null) {
                                taskDto.setTestTaskId(existingTt.getId());
                                taskDto.setCertificationResultId(createdCert.getId());
                                taskDto.setTestTask(existingTt);
                            }

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
                                            tp.setComputerExperienceMonths(
                                                    Integer.valueOf(certPart.getComputerExperienceMonths()));
                                            tp.setEducationTypeId(certPart.getEducationTypeId());
                                            tp.setGender(certPart.getGender());
                                            tp.setOccupation(certPart.getOccupation());
                                            tp.setProductExperienceMonths(
                                                    Integer.valueOf(certPart.getProductExperienceMonths()));
                                            tp.setProfessionalExperienceMonths(
                                                    Integer.valueOf(certPart.getProfessionalExperienceMonths()));

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
                                            .getById(cert.getCertificationId());
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
        CertificationStatus activeCertStatus = certStatusDao.getByStatusName(CertificationStatusType.Active.toString());
        Date certificationDate = pendingCp.getCertificationDate();
        CertificationStatusEvent certEvent = CertificationStatusEvent.builder()
                .eventDate(certificationDate.getTime())
                .status(activeCertStatus)
                .build();
        statusEventDao.create(newCertifiedProduct.getId(), certEvent);

        CuresUpdateEventDTO curesEvent = new CuresUpdateEventDTO();
        curesEvent.setCreationDate(new Date());
        curesEvent.setDeleted(false);
        curesEvent.setEventDate(certificationDate);
        curesEvent.setCuresUpdate(curesUpdateService.isCuresUpdate(pendingCp));
        curesEvent.setCertifiedProductId(newCertifiedProduct.getId());
        curesUpdateDao.create(curesEvent);

        pcpManager.confirm(pendingCp.getCertificationBodyId(), pendingCp.getId());
        return newCertifiedProduct;
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ONC')")
    @Transactional(readOnly = false)
    @CacheEvict(value = {
            CacheNames.COLLECTIONS_DEVELOPERS, CacheNames.GET_DECERTIFIED_DEVELOPERS
    }, allEntries = true)
    // listings collection is not evicted here because it's pre-fetched and
    // handled in a listener
    // no other caches have ACB data so we do not need to clear all
    public CertifiedProductDTO changeOwnership(Long certifiedProductId, Long acbId)
            throws EntityRetrievalException, JsonProcessingException, EntityCreationException {
        CertifiedProductDTO toUpdate = cpDao.getById(certifiedProductId);
        toUpdate.setCertificationBodyId(acbId);
        return cpDao.update(toUpdate);
    }

    private void sanitizeUpdatedListingData(CertifiedProductSearchDetails listing)
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
                    } catch (Exception ignore) {
                    }
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

        listing.getMeasures().stream()
            .forEach(measure -> associateMeasureWithCuresAndOriginalCriteria(measure));
    }

    private void associateMeasureWithCuresAndOriginalCriteria(ListingMeasure measure) {
        List<CertificationCriterion> expectedAssociatedCriteriaForMeasure = new ArrayList<CertificationCriterion>();
        for (CertificationCriterion associatedCriterion : measure.getAssociatedCriteria()) {
            List<CertificationCriterion> allCriteriaWithNumber = criteriaService.getByNumber(associatedCriterion.getNumber());
            expectedAssociatedCriteriaForMeasure.addAll(allCriteriaWithNumber);
        }
        measure.getAssociatedCriteria().addAll(expectedAssociatedCriteriaForMeasure);
    }

    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).CERTIFIED_PRODUCT, "
            + "T(gov.healthit.chpl.permissions.domains.CertifiedProductDomainPermissions).UPDATE, #updateRequest)")
    @Transactional(rollbackFor = {
            EntityRetrievalException.class, EntityCreationException.class, JsonProcessingException.class,
            AccessDeniedException.class, InvalidArgumentsException.class
    })
    @CacheEvict(value = {
            CacheNames.ALL_DEVELOPERS, CacheNames.ALL_DEVELOPERS_INCLUDING_DELETED, CacheNames.COLLECTIONS_DEVELOPERS,
            CacheNames.COLLECTIONS_LISTINGS, CacheNames.COLLECTIONS_SEARCH
    }, allEntries = true)
    public CertifiedProductDTO update(ListingUpdateRequest updateRequest)
            throws AccessDeniedException, EntityRetrievalException, JsonProcessingException, EntityCreationException,
            InvalidArgumentsException, IOException, ValidationException, MissingReasonException {

        CertifiedProductSearchDetails updatedListing = updateRequest.getListing();
        CertifiedProductSearchDetails existingListing = certifiedProductDetailsManager
                .getCertifiedProductDetails(updatedListing.getId());

        // clean up what was sent in - some necessary IDs or other fields may be missing
        sanitizeUpdatedListingData(updatedListing);

        // validate - throws ValidationException if the listing cannot be updated
        validateListingForUpdate(existingListing, updatedListing, updateRequest.isAcknowledgeWarnings());

        // if listing status has changed that may trigger other changes to developer status
        performSecondaryActionsBasedOnStatusChanges(existingListing, updatedListing, updateRequest.getReason());

        // Update the listing
        CertifiedProductDTO dtoToUpdate = new CertifiedProductDTO(updatedListing);
        CertifiedProductDTO result = cpDao.update(dtoToUpdate);
        updateListingsChildData(existingListing, updatedListing);

        // Log the activity
        logCertifiedProductUpdateActivity(existingListing, updateRequest.getReason());

        return result;
    }

    private void logCertifiedProductUpdateActivity(CertifiedProductSearchDetails existingListing,
            String reason) throws JsonProcessingException, EntityCreationException, EntityRetrievalException {
        CertifiedProductSearchDetails changedProduct = certifiedProductDetailsManager.
                getCertifiedProductDetails(existingListing.getId());
        activityManager.addActivity(ActivityConcept.CERTIFIED_PRODUCT, existingListing.getId(),
                "Updated certified product " + changedProduct.getChplProductNumber() + ".", existingListing,
                changedProduct, reason);
    }

    @SuppressWarnings({"checkstyle:linelength"})
    private void updateListingsChildData(CertifiedProductSearchDetails existingListing, CertifiedProductSearchDetails updatedListing)
            throws EntityCreationException, EntityRetrievalException, IOException {

        updateTestingLabs(updatedListing.getId(), existingListing.getTestingLabs(), updatedListing.getTestingLabs());
        updateIcsChildren(updatedListing.getId(), existingListing.getIcs(), updatedListing.getIcs());
        updateIcsParents(updatedListing.getId(), existingListing.getIcs(), updatedListing.getIcs());
        updateQmsStandards(updatedListing.getId(), existingListing.getQmsStandards(), updatedListing.getQmsStandards());
        updateMeasures(updatedListing.getId(), existingListing.getMeasures(), updatedListing.getMeasures());
        updateTargetedUsers(updatedListing.getId(), existingListing.getTargetedUsers(),
                updatedListing.getTargetedUsers());
        updateAccessibilityStandards(updatedListing.getId(), existingListing.getAccessibilityStandards(),
                updatedListing.getAccessibilityStandards());
        updateCertificationDate(updatedListing.getId(), new Date(existingListing.getCertificationDate()),
                new Date(updatedListing.getCertificationDate()));
        updateCertificationStatusEvents(updatedListing.getId(), existingListing.getCertificationEvents(),
                updatedListing.getCertificationEvents());
        updateCuresUpdateEvents(updatedListing.getId(), existingListing.getCuresUpdate(),
                updatedListing);
        updatePromotingInteroperabilityUserHistory(updatedListing.getId(), existingListing.getPromotingInteroperabilityUserHistory(),
                updatedListing.getPromotingInteroperabilityUserHistory());
        updateCertifications(existingListing, updatedListing,
                existingListing.getCertificationResults(), updatedListing.getCertificationResults());
        copyCriterionIdsToCqmMappings(updatedListing);
        updateCqms(updatedListing, existingListing.getCqmResults(), updatedListing.getCqmResults());
    }

    private void performSecondaryActionsBasedOnStatusChanges(CertifiedProductSearchDetails existingListing,
            CertifiedProductSearchDetails updatedListing, String reason)
            throws EntityRetrievalException, JsonProcessingException, EntityCreationException, ValidationException {
        Long listingId = updatedListing.getId();
        Long productVersionId = updatedListing.getVersion().getVersionId();
        CertificationStatus updatedStatus = updatedListing.getCurrentStatus().getStatus();
        CertificationStatus existingStatus = existingListing.getCurrentStatus().getStatus();
        // if listing status has changed that may trigger other changes
        // to developer status
        if (ObjectUtils.notEqual(updatedStatus.getName(), existingStatus.getName())) {
            // look at the updated status and see if a developer ban is
            // appropriate
            CertificationStatus updatedStatusObj = certStatusDao.getById(updatedStatus.getId());
            DeveloperDTO cpDeveloper = developerDao.getByVersion(productVersionId);
            if (cpDeveloper == null) {
                LOGGER.error("Could not find developer for product version with id " + productVersionId);
                throw new EntityNotFoundException(
                        "No developer could be located for the certified product in the update. Update cannot continue.");
            }
            DeveloperStatusDTO newDevStatusDto = null;
            switch (CertificationStatusType.getValue(updatedStatusObj.getName())) {
            case SuspendedByOnc:
            case TerminatedByOnc:
                // only onc admin can do this and it always triggers developer
                // ban
                if (resourcePermissions.isUserRoleAdmin() || resourcePermissions.isUserRoleOnc()) {
                    // find the new developer status
                    if (updatedStatusObj.getName().equals(CertificationStatusType.SuspendedByOnc.toString())) {
                        newDevStatusDto = devStatusDao.getByName(DeveloperStatusType.SuspendedByOnc.toString());
                    } else if (updatedStatusObj.getName()
                            .equals(CertificationStatusType.TerminatedByOnc.toString())) {
                        newDevStatusDto = devStatusDao
                                .getByName(DeveloperStatusType.UnderCertificationBanByOnc.toString());
                    }
                } else if (!resourcePermissions.isUserRoleAdmin() && !resourcePermissions.isUserRoleOnc()) {
                    LOGGER.error("User " + AuthUtil.getUsername()
                            + " does not have ROLE_ADMIN or ROLE_ONC and cannot change the status of developer for certified "
                            + "product with id " + listingId);
                    throw new AccessDeniedException(
                            "User does not have admin permission to change " + cpDeveloper.getName() + " status.");
                }
                break;
            case WithdrawnByAcb:
            case WithdrawnByDeveloperUnderReview:
                // initiate TriggerDeveloperBan job, telling ONC that they might
                // need to ban a Developer
                triggerDeveloperBan(updatedListing, reason);
                break;
            default:
                LOGGER.info("New listing status is " + updatedStatusObj.getName()
                        + " which does not trigger a developer ban.");
                break;
            }
            if (newDevStatusDto != null) {
                DeveloperStatusEventDTO statusHistoryToAdd = new DeveloperStatusEventDTO();
                statusHistoryToAdd.setDeveloperId(cpDeveloper.getId());
                statusHistoryToAdd.setStatus(newDevStatusDto);
                statusHistoryToAdd.setStatusDate(new Date());
                statusHistoryToAdd.setReason(msgUtil.getMessage("developer.statusAutomaticallyChanged"));
                cpDeveloper.getStatusEvents().add(statusHistoryToAdd);
                developerManager.update(cpDeveloper, false);
            }
        }

    }

    private void validateListingForUpdate(CertifiedProductSearchDetails existingListing,
            CertifiedProductSearchDetails updatedListing, boolean acknowledgeWarnings) throws ValidationException {
        Validator validator = validatorFactory.getValidator(updatedListing);
        if (validator != null) {
            validator.validate(existingListing, updatedListing);
        }

        if ((updatedListing.getErrorMessages() != null && updatedListing.getErrorMessages().size() > 0)
                || (!acknowledgeWarnings && updatedListing.getWarningMessages() != null
                && updatedListing.getWarningMessages().size() > 0)) {
            for (String err : updatedListing.getErrorMessages()) {
                LOGGER.error("Error updating listing " + updatedListing.getChplProductNumber() + ": " + err);
            }
            for (String warning : updatedListing.getWarningMessages()) {
                LOGGER.error("Warning updating listing " + updatedListing.getChplProductNumber() + ": " + warning);
            }
            throw new ValidationException(updatedListing.getErrorMessages(), updatedListing.getWarningMessages());
        }
    }

    private int updateTestingLabs(Long listingId, List<CertifiedProductTestingLab> existingTestingLabs,
            List<CertifiedProductTestingLab> updatedTestingLabs)
            throws EntityCreationException, EntityRetrievalException, JsonProcessingException {

        int numChanges = 0;
        List<CertifiedProductTestingLab> tlsToAdd = new ArrayList<CertifiedProductTestingLab>();
        List<Long> idsToRemove = new ArrayList<Long>();

        // figure out which testing labs to add
        if (updatedTestingLabs != null && updatedTestingLabs.size() > 0) {
            if (existingTestingLabs == null || existingTestingLabs.size() == 0) {
                // existing listing has none, add all from the update
                for (CertifiedProductTestingLab updatedItem : updatedTestingLabs) {
                    tlsToAdd.add(updatedItem);
                }
            } else if (existingTestingLabs.size() > 0) {
                // existing listing has some, compare to the update to see if
                // any are different
                for (CertifiedProductTestingLab updatedItem : updatedTestingLabs) {
                    boolean inExistingListing = false;
                    for (CertifiedProductTestingLab existingItem : existingTestingLabs) {
                        inExistingListing = !inExistingListing ? updatedItem.matches(existingItem) : inExistingListing;
                    }

                    if (!inExistingListing) {
                        tlsToAdd.add(updatedItem);
                    }
                }
            }
        }

        // figure out which testing labs to remove
        if (existingTestingLabs != null && existingTestingLabs.size() > 0) {
            // if the updated listing has none, remove them all from existing
            if (updatedTestingLabs == null || updatedTestingLabs.size() == 0) {
                for (CertifiedProductTestingLab existingItem : existingTestingLabs) {
                    idsToRemove.add(existingItem.getId());
                }
            } else if (updatedTestingLabs.size() > 0) {
                for (CertifiedProductTestingLab existingItem : existingTestingLabs) {
                    boolean inUpdatedListing = false;
                    for (CertifiedProductTestingLab updatedItem : updatedTestingLabs) {
                        inUpdatedListing = !inUpdatedListing ? existingItem.matches(updatedItem) : inUpdatedListing;
                    }
                    if (!inUpdatedListing) {
                        idsToRemove.add(existingItem.getId());
                    }
                }
            }
        }

        numChanges = tlsToAdd.size() + idsToRemove.size();
        for (CertifiedProductTestingLab toAdd : tlsToAdd) {
            TestingLabDTO item = atlDao.getByName(toAdd.getTestingLabName());
            CertifiedProductTestingLabDTO tlDto = new CertifiedProductTestingLabDTO();
            tlDto.setTestingLabId(item.getId());
            tlDto.setTestingLabName(item.getName());
            tlDto.setTestingLabCode(item.getTestingLabCode());
            tlDto.setCertifiedProductId(listingId);
            cpTestingLabDao.createCertifiedProductTestingLab(tlDto);
        }

        for (Long idToRemove : idsToRemove) {
            cpTestingLabDao.deleteCertifiedProductTestingLab(idToRemove);
        }
        return numChanges;
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
            throws EntityCreationException, EntityRetrievalException, JsonProcessingException, IOException {

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

        List<String> fuzzyQmsChoices = fuzzyChoicesDao.getByType(FuzzyType.QMS_STANDARD).getChoices();
        for (CertifiedProductQmsStandard toAdd : qmsToAdd) {
            if (!fuzzyQmsChoices.contains(toAdd.getQmsStandardName())) {
                fuzzyQmsChoices.add(toAdd.getQmsStandardName());
                FuzzyChoicesDTO dto = new FuzzyChoicesDTO();
                dto.setFuzzyType(FuzzyType.QMS_STANDARD);
                dto.setChoices(fuzzyQmsChoices);
                fuzzyChoicesDao.update(dto);
            }
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

    private int updateMeasures(Long listingId, List<ListingMeasure> existingMeasures,
            List<ListingMeasure> updatedMeasures)
            throws EntityCreationException, EntityRetrievalException, JsonProcessingException, IOException {

        int numChanges = 0;
        List<ListingMeasure> measuresToAdd = new ArrayList<ListingMeasure>();
        List<MeasurePair> measuresToUpdate = new ArrayList<MeasurePair>();
        List<Long> idsToRemove = new ArrayList<Long>();

        // figure out which measures to add
        if (updatedMeasures != null && updatedMeasures.size() > 0) {
            if (existingMeasures == null || existingMeasures.size() == 0) {
                // existing listing has none, add all from the update
                for (ListingMeasure updatedItem : updatedMeasures) {
                    measuresToAdd.add(updatedItem);
                }
            } else if (existingMeasures.size() > 0) {
                // existing listing has some, compare to the update to see if
                // any are different
                for (ListingMeasure updatedItem : updatedMeasures) {
                    boolean inExistingListing = false;
                    for (ListingMeasure existingItem : existingMeasures) {
                        if (updatedItem.getId() != null && updatedItem.getId().equals(existingItem.getId())) {
                            inExistingListing = true;
                            measuresToUpdate.add(new MeasurePair(existingItem, updatedItem));
                        }
                    }

                    if (!inExistingListing) {
                        measuresToAdd.add(updatedItem);
                    }
                }
            }
        }

        // figure out which measures to remove
        if (existingMeasures != null && existingMeasures.size() > 0) {
            // if the updated listing has none, remove them all from existing
            if (updatedMeasures == null || updatedMeasures.size() == 0) {
                for (ListingMeasure existingItem : existingMeasures) {
                    idsToRemove.add(existingItem.getId());
                }
            } else if (updatedMeasures.size() > 0) {
                for (ListingMeasure existingItem : existingMeasures) {
                    boolean inUpdatedListing = false;
                    for (ListingMeasure updatedItem : updatedMeasures) {
                        inUpdatedListing = !inUpdatedListing
                                ? existingItem.getId().equals(updatedItem.getId()) : inUpdatedListing;
                    }
                    if (!inUpdatedListing) {
                        idsToRemove.add(existingItem.getId());
                    }
                }
            }
        }

        numChanges = measuresToAdd.size() + idsToRemove.size();

        for (ListingMeasure measure : measuresToAdd) {
            cpMeasureDao.createCertifiedProductMeasureMapping(listingId, measure);
        }

        for (MeasurePair toUpdate : measuresToUpdate) {
            boolean hasChanged = false;
            if (!toUpdate.getUpdated().matches(toUpdate.getOrig())) {
                hasChanged = true;
            }

            if (hasChanged) {
                cpMeasureDao.updateCertifiedProductMeasureMapping(toUpdate.getUpdated());
                numChanges++;
            }
        }

        for (Long idToRemove : idsToRemove) {
            cpMeasureDao.deleteCertifiedProductMeasure(idToRemove);
        }
        return numChanges;
    }

    private int updateTargetedUsers(Long listingId,
            List<CertifiedProductTargetedUser> existingTargetedUsers,
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
             throws EntityCreationException, EntityRetrievalException, JsonProcessingException, IOException {

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

        List<String> fuzzyAsChoices = fuzzyChoicesDao.getByType(FuzzyType.ACCESSIBILITY_STANDARD).getChoices();
        for (CertifiedProductAccessibilityStandard toAdd : accStdsToAdd) {
            if (!fuzzyAsChoices.contains(toAdd.getAccessibilityStandardName())) {
                fuzzyAsChoices.add(toAdd.getAccessibilityStandardName());
                FuzzyChoicesDTO dto = new FuzzyChoicesDTO();
                dto.setFuzzyType(FuzzyType.ACCESSIBILITY_STANDARD);
                dto.setChoices(fuzzyAsChoices);
                fuzzyChoicesDao.update(dto);
            }

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
            CertificationStatusEvent certificationEvent = statusEventDao
                    .findInitialCertificationEventForCertifiedProduct(listingId);
            if (certificationEvent != null) {
                certificationEvent.setEventDate(newCertDate.getTime());
                statusEventDao.update(listingId, certificationEvent);
            }
        }
    }

    private int updateCertificationStatusEvents(Long listingId,
            List<CertificationStatusEvent> existingStatusEvents,
            List<CertificationStatusEvent> updatedStatusEvents)
            throws EntityCreationException, EntityRetrievalException, JsonProcessingException {

        int numChanges = 0;
        List<CertificationStatusEvent> statusEventsToAdd = new ArrayList<CertificationStatusEvent>();
        List<CertificationStatusEventPair> statusEventsToUpdate = new ArrayList<CertificationStatusEventPair>();
        List<Long> idsToRemove = new ArrayList<Long>();

        // figure out which status events to add
        if (updatedStatusEvents != null && updatedStatusEvents.size() > 0) {
            if (existingStatusEvents == null || existingStatusEvents.size() == 0) {
                // existing listing has none, add all from the update
                for (CertificationStatusEvent updatedItem : updatedStatusEvents) {
                    statusEventsToAdd.add(updatedItem);
                }
            } else if (existingStatusEvents.size() > 0) {
                // existing listing has some, compare to the update to see if
                // any are different
                for (CertificationStatusEvent updatedItem : updatedStatusEvents) {
                    boolean inExistingListing = false;
                    for (CertificationStatusEvent existingItem : existingStatusEvents) {
                        if (updatedItem.matches(existingItem)) {
                            inExistingListing = true;
                            statusEventsToUpdate.add(new CertificationStatusEventPair(existingItem, updatedItem));
                        }
                    }

                    if (!inExistingListing) {
                        statusEventsToAdd.add(updatedItem);
                    }
                }
            }
        }

        // figure out which status events to remove
        if (existingStatusEvents != null && existingStatusEvents.size() > 0) {
            // if the updated listing has none, remove them all from existing
            if (updatedStatusEvents == null || updatedStatusEvents.size() == 0) {
                for (CertificationStatusEvent existingItem : existingStatusEvents) {
                    idsToRemove.add(existingItem.getId());
                }
            } else if (updatedStatusEvents.size() > 0) {
                for (CertificationStatusEvent existingItem : existingStatusEvents) {
                    boolean inUpdatedListing = false;
                    for (CertificationStatusEvent updatedItem : updatedStatusEvents) {
                        inUpdatedListing = !inUpdatedListing ? existingItem.matches(updatedItem) : inUpdatedListing;
                    }
                    if (!inUpdatedListing) {
                        idsToRemove.add(existingItem.getId());
                    }
                }
            }
        }

        numChanges = statusEventsToAdd.size() + idsToRemove.size();
        for (CertificationStatusEvent toAdd : statusEventsToAdd) {
            CertificationStatusEvent statusEventToAdd = CertificationStatusEvent.builder()
                    .eventDate(toAdd.getEventDate())
                    .reason(toAdd.getReason())
                    .build();
            statusEventToAdd.setReason(toAdd.getReason());
            if (toAdd.getStatus() == null) {
                String msg = msgUtil.getMessage("listing.missingCertificationStatus");
                throw new EntityRetrievalException(msg);
            } else if (toAdd.getStatus().getId() != null) {
                CertificationStatus status = certStatusDao.getById(toAdd.getStatus().getId());
                if (status == null) {
                    String msg = msgUtil.getMessage("listing.badCertificationStatusId", toAdd.getStatus().getId());
                    throw new EntityRetrievalException(msg);
                }
                statusEventToAdd.setStatus(status);
            } else if (!StringUtils.isEmpty(toAdd.getStatus().getName())) {
                CertificationStatus status = certStatusDao.getByStatusName(toAdd.getStatus().getName());
                if (status == null) {
                    String msg = msgUtil.getMessage("listing.badCertificationStatusName", toAdd.getStatus().getName());
                    throw new EntityRetrievalException(msg);
                }
                statusEventToAdd.setStatus(status);
            }
            statusEventDao.create(listingId, statusEventToAdd);
        }

        for (CertificationStatusEventPair toUpdate : statusEventsToUpdate) {
            boolean hasChanged = false;
            if (!ObjectUtils.equals(toUpdate.getOrig().getEventDate(), toUpdate.getUpdated().getEventDate())
                    || !ObjectUtils.equals(toUpdate.getOrig().getStatus().getId(),
                            toUpdate.getUpdated().getStatus().getId())
                    || !ObjectUtils.equals(toUpdate.getOrig().getStatus().getName(),
                            toUpdate.getUpdated().getStatus().getName())
                    || !ObjectUtils.equals(toUpdate.getOrig().getReason(), toUpdate.getUpdated().getReason())) {
                hasChanged = true;
            }

            if (hasChanged) {
                CertificationStatusEvent cseToUpdate = toUpdate.getUpdated();
                CertificationStatusEvent updatedStatusEvent = CertificationStatusEvent.builder()
                        .id(cseToUpdate.getId())
                        .eventDate(cseToUpdate.getEventDate())
                        .reason(cseToUpdate.getReason())
                        .build();
                if (cseToUpdate.getStatus() == null) {
                    String msg = msgUtil.getMessage("listing.missingCertificationStatus");
                    throw new EntityRetrievalException(msg);
                } else if (cseToUpdate.getStatus().getId() != null) {
                    CertificationStatus status = certStatusDao.getById(cseToUpdate.getStatus().getId());
                    if (status == null) {
                        String msg = msgUtil.getMessage("listing.badCertificationStatusId",
                                cseToUpdate.getStatus().getId());
                        throw new EntityRetrievalException(msg);
                    }
                    updatedStatusEvent.setStatus(status);
                } else if (!StringUtils.isEmpty(cseToUpdate.getStatus().getName())) {
                    CertificationStatus status = certStatusDao.getByStatusName(cseToUpdate.getStatus().getName());
                    if (status == null) {
                        String msg = msgUtil.getMessage("listing.badCertificationStatusName",
                                cseToUpdate.getStatus().getName());
                        throw new EntityRetrievalException(msg);
                    }
                    updatedStatusEvent.setStatus(status);
                }
                statusEventDao.update(listingId, updatedStatusEvent);
                numChanges++;
            }
        }

        for (Long idToRemove : idsToRemove) {
            statusEventDao.delete(idToRemove);
        }
        return numChanges;
    }

    private int updateCuresUpdateEvents(Long listingId, Boolean existingCuresUpdate,
            CertifiedProductSearchDetails updatedListing) throws EntityCreationException, EntityRetrievalException {
        int numChanges = 0;
        String currentStatus = updatedListing.getCurrentStatus().getStatus().getName();
        if (currentStatus.equalsIgnoreCase(CertificationStatusType.Active.getName())
                || currentStatus.equalsIgnoreCase(CertificationStatusType.SuspendedByAcb.getName())
                || currentStatus.equalsIgnoreCase(CertificationStatusType.SuspendedByOnc.getName())) {
            Boolean isCuresUpdate = curesUpdateService.isCuresUpdate(updatedListing);
            if (existingCuresUpdate != isCuresUpdate) {
                CuresUpdateEventDTO curesEvent = new CuresUpdateEventDTO();
                curesEvent.setCreationDate(new Date());
                curesEvent.setDeleted(false);
                curesEvent.setEventDate(new Date());
                curesEvent.setCuresUpdate(isCuresUpdate);
                curesEvent.setCertifiedProductId(listingId);
                curesUpdateDao.create(curesEvent);
                numChanges += 1;
            }
        }
        return numChanges;
    }

    private int updatePromotingInteroperabilityUserHistory(Long listingId,
            List<PromotingInteroperabilityUser> existingPiuHistory,
            List<PromotingInteroperabilityUser> updatedPiuHistory)
            throws EntityCreationException, EntityRetrievalException, JsonProcessingException {

        int numChanges = 0;
        List<PromotingInteroperabilityUser> itemsToAdd = new ArrayList<PromotingInteroperabilityUser>();
        List<PromotingInteroperabilityUserPair> itemsToUpdate = new ArrayList<PromotingInteroperabilityUserPair>();
        List<Long> idsToRemove = new ArrayList<Long>();

        // figure out which status events to add
        if (updatedPiuHistory != null && updatedPiuHistory.size() > 0) {
            if (existingPiuHistory == null || existingPiuHistory.size() == 0) {
                // existing listing has none, add all from the update
                for (PromotingInteroperabilityUser updatedItem : updatedPiuHistory) {
                    itemsToAdd.add(updatedItem);
                }
            } else if (existingPiuHistory.size() > 0) {
                // existing listing has some, compare to the update to see if
                // any are different
                for (PromotingInteroperabilityUser updatedItem : updatedPiuHistory) {
                    boolean inExistingListing = false;
                    for (PromotingInteroperabilityUser existingItem : existingPiuHistory) {
                        if (updatedItem.matches(existingItem)) {
                            inExistingListing = true;
                            itemsToUpdate.add(new PromotingInteroperabilityUserPair(existingItem, updatedItem));
                        }
                    }

                    if (!inExistingListing) {
                        itemsToAdd.add(updatedItem);
                    }
                }
            }
        }

        // figure out which muu items to remove
        if (existingPiuHistory != null && existingPiuHistory.size() > 0) {
            // if the updated listing has none, remove them all from existing
            if (updatedPiuHistory == null || updatedPiuHistory.size() == 0) {
                for (PromotingInteroperabilityUser existingItem : existingPiuHistory) {
                    idsToRemove.add(existingItem.getId());
                }
            } else if (updatedPiuHistory.size() > 0) {
                for (PromotingInteroperabilityUser existingItem : existingPiuHistory) {
                    boolean inUpdatedListing = false;
                    for (PromotingInteroperabilityUser updatedItem : updatedPiuHistory) {
                        inUpdatedListing = !inUpdatedListing ? existingItem.matches(updatedItem) : inUpdatedListing;
                    }
                    if (!inUpdatedListing) {
                        idsToRemove.add(existingItem.getId());
                    }
                }
            }
        }

        numChanges = itemsToAdd.size() + idsToRemove.size();
        for (PromotingInteroperabilityUser toAdd : itemsToAdd) {
            piuDao.create(listingId, toAdd);
        }

        for (PromotingInteroperabilityUserPair toUpdate : itemsToUpdate) {
            boolean hasChanged = false;
            if (!Objects.equals(toUpdate.getOrig().getUserCount(), toUpdate.getUpdated().getUserCount())
                    || !Objects.equals(toUpdate.getOrig().getUserCountDate(), toUpdate.getUpdated().getUserCountDate())) {
                hasChanged = true;
            }

            if (hasChanged) {
                piuDao.update(toUpdate.getUpdated());
                numChanges++;
            }
        }

        for (Long idToRemove : idsToRemove) {
            piuDao.delete(idToRemove);
        }
        return numChanges;
    }

    private int updateCertifications(CertifiedProductSearchDetails existingListing,
            CertifiedProductSearchDetails updatedListing, List<CertificationResult> existingCertifications,
            List<CertificationResult> updatedCertifications)
            throws EntityCreationException, EntityRetrievalException, JsonProcessingException, IOException {

        int numChanges = 0;

        // replace the value of the result. we shouldn't have to add or delete any cert results
        // because for certification criteria, all results are always there whether they were
        // successful or not

        for (CertificationResult updatedItem : updatedCertifications) {
            for (CertificationResult existingItem : existingCertifications) {
                if (updatedItem.getCriterion() != null && existingItem.getCriterion() != null
                        && updatedItem.getCriterion().getId().equals(existingItem.getCriterion().getId())) {
                    numChanges += certResultManager.update(existingListing, updatedListing, existingItem,
                            updatedItem);
                }
            }
        }

        return numChanges;
    }

    private void copyCriterionIdsToCqmMappings(CertifiedProductSearchDetails listing) {
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

    private int updateCqms(CertifiedProductSearchDetails listing, List<CQMResultDetails> existingCqmDetails,
            List<CQMResultDetails> updatedCqmDetails)
            throws EntityCreationException, EntityRetrievalException, JsonProcessingException {
        // convert to CQMResultDetailsDTO since CMS CQMs can have multiple entries
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

    private int updateCqm(CertifiedProductSearchDetails listing, CQMResultDetailsDTO existingCqm,
            CQMResultDetailsDTO updatedCqm) throws EntityRetrievalException {

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
                if (existingItem.getCriterionId().equals(updatedItem.getCriterionId())) {
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
                if (existingItem.getCriterionId().equals(updatedItem.getCriterionId())) {
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
        } else if (cqm.getCriterion() != null && cqm.getCriterion().getId() != null) {
            return cqm.getCriterion().getId();
        } else if (cqm.getCriterion() != null && !StringUtils.isEmpty(cqm.getCriterion().getNumber())
                && !StringUtils.isEmpty(cqm.getCriterion().getTitle())) {
            CertificationCriterionDTO cert = certCriterionDao.getByNumberAndTitle(
                    cqm.getCriterion().getNumber(), cqm.getCriterion().getTitle());
            if (cert != null) {
                return cert.getId();
            } else {
                throw new EntityRetrievalException(
                        "Could not find certification criteria with number " + cqm.getCriterion().getNumber());
            }
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
                dto.setTitle(cqm.getTitle());
                dto.setVersion(version);
                dto.setSuccess(Boolean.TRUE);
                if (cqm.getCriteria() != null && cqm.getCriteria().size() > 0) {
                    for (CQMResultCertification criteria : cqm.getCriteria()) {
                        CQMResultCriteriaDTO cqmdto = new CQMResultCriteriaDTO();
                        cqmdto.setId(criteria.getId());
                        cqmdto.setCriterionId(criteria.getCertificationId());
                        CertificationCriterionDTO certDto = new CertificationCriterionDTO();
                        certDto.setId(criteria.getCertificationId());
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
            dto.setTitle(cqm.getTitle());
            dto.setSuccess(cqm.isSuccess());
            result.add(dto);
        }
        return result;
    }

    private CertifiedProductDetailsDTO getCertifiedProductDetailsDtoByChplProductNumber(String chplProductNumber)
            throws EntityRetrievalException {

        List<CertifiedProductDetailsDTO> dtos = certifiedProductSearchResultDAO
                .getByChplProductNumber(chplProductNumber);

        if (dtos.size() == 0) {
            throw new EntityRetrievalException("Could not retrieve CertifiedProductSearchDetails.");
        }
        return dtos.get(0);
    }

    private void triggerDeveloperBan(CertifiedProductSearchDetails updatedListing, String reason) {
        Scheduler scheduler;
        try {
            scheduler = getScheduler();

            TriggerKey triggerId = triggerKey("triggerBanNow_" + new Date().getTime(), "triggerDeveloperBanTrigger");
            JobKey jobId = jobKey("Trigger Developer Ban Notification", SchedulerManager.CHPL_JOBS_KEY);

            Trigger qzTrigger = newTrigger().withIdentity(triggerId).startNow().forJob(jobId)
                    .usingJobData("status", updatedListing.getCurrentStatus().getStatus().getName())
                    .usingJobData("dbId", updatedListing.getId())
                    .usingJobData("chplId", updatedListing.getChplProductNumber())
                    .usingJobData("developer", updatedListing.getDeveloper().getName())
                    .usingJobData("acb", updatedListing.getCertifyingBody().get(CertifiedProductSearchDetails.ACB_NAME_KEY)
                            .toString())
                    .usingJobData("changeDate", new Date().getTime())
                    .usingJobData("fullName", AuthUtil.getCurrentUser().getFullName())
                    .usingJobData("effectiveDate", updatedListing.getCurrentStatus().getEventDate())
                    .usingJobData("openNcs", updatedListing.getCountOpenNonconformities())
                    .usingJobData("closedNcs", updatedListing.getCountClosedNonconformities())
                    .usingJobData("reason", updatedListing.getCurrentStatus().getReason())
                    .usingJobData("reasonForChange", reason).build();
            scheduler.scheduleJob(qzTrigger);
        } catch (SchedulerException e) {
            LOGGER.error("Could not start Trigger Developer Ban", e);
        }
    }

    private Scheduler getScheduler() throws SchedulerException {
        StdSchedulerFactory sf = new StdSchedulerFactory();
        sf.initialize();
        Scheduler scheduler = sf.getScheduler();
        return scheduler;
    }

    @Data
    private static class CertificationStatusEventPair {
        private CertificationStatusEvent orig;
        private CertificationStatusEvent updated;

        CertificationStatusEventPair() {
        }

        CertificationStatusEventPair(CertificationStatusEvent orig, CertificationStatusEvent updated) {

            this.orig = orig;
            this.updated = updated;
        }
    }

    @Data
    @NoArgsConstructor
    private static class PromotingInteroperabilityUserPair {
        private PromotingInteroperabilityUser orig;
        private PromotingInteroperabilityUser updated;

        PromotingInteroperabilityUserPair(PromotingInteroperabilityUser orig, PromotingInteroperabilityUser updated) {
            this.orig = orig;
            this.updated = updated;
        }
    }

    @Data
    private static class QmsStandardPair {
        private CertifiedProductQmsStandard orig;
        private CertifiedProductQmsStandard updated;

        QmsStandardPair() {
        }

        QmsStandardPair(CertifiedProductQmsStandard orig, CertifiedProductQmsStandard updated) {
            this.orig = orig;
            this.updated = updated;
        }
    }

    @Data
    private static class MeasurePair {
        private ListingMeasure orig;
        private ListingMeasure updated;

        MeasurePair() {
        }

        MeasurePair(ListingMeasure orig, ListingMeasure updated) {
            this.orig = orig;
            this.updated = updated;
        }
    }

    @Data
    private static class CQMResultDetailsPair {
        private CQMResultDetailsDTO orig;
        private CQMResultDetailsDTO updated;

        CQMResultDetailsPair() {
        }

        CQMResultDetailsPair(CQMResultDetailsDTO orig, CQMResultDetailsDTO updated) {
            this.orig = orig;
            this.updated = updated;
        }
    }
}

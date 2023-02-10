package gov.healthit.chpl.manager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import javax.persistence.EntityNotFoundException;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.quartz.JobDataMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.accessibilityStandard.AccessibilityStandard;
import gov.healthit.chpl.accessibilityStandard.AccessibilityStandardDAO;
import gov.healthit.chpl.caching.CacheNames;
import gov.healthit.chpl.certifiedproduct.CertifiedProductDetailsManager;
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
import gov.healthit.chpl.dao.ListingGraphDAO;
import gov.healthit.chpl.dao.PromotingInteroperabilityUserDAO;
import gov.healthit.chpl.dao.TargetedUserDAO;
import gov.healthit.chpl.dao.TestingLabDAO;
import gov.healthit.chpl.domain.CQMResultCertification;
import gov.healthit.chpl.domain.CQMResultDetails;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationStatus;
import gov.healthit.chpl.domain.CertificationStatusEvent;
import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.CertifiedProductAccessibilityStandard;
import gov.healthit.chpl.domain.CertifiedProductQmsStandard;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.CertifiedProductTargetedUser;
import gov.healthit.chpl.domain.CertifiedProductTestingLab;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.DeveloperStatus;
import gov.healthit.chpl.domain.DeveloperStatusEvent;
import gov.healthit.chpl.domain.IcsFamilyTreeNode;
import gov.healthit.chpl.domain.InheritedCertificationStatus;
import gov.healthit.chpl.domain.ListingMeasure;
import gov.healthit.chpl.domain.ListingUpdateRequest;
import gov.healthit.chpl.domain.PromotingInteroperabilityUser;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.domain.schedule.ChplJob;
import gov.healthit.chpl.domain.schedule.ChplOneTimeTrigger;
import gov.healthit.chpl.dto.CQMCriterionDTO;
import gov.healthit.chpl.dto.CQMResultCriteriaDTO;
import gov.healthit.chpl.dto.CQMResultDTO;
import gov.healthit.chpl.dto.CQMResultDetailsDTO;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.dto.CertifiedProductAccessibilityStandardDTO;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.dto.CertifiedProductQmsStandardDTO;
import gov.healthit.chpl.dto.CertifiedProductTargetedUserDTO;
import gov.healthit.chpl.dto.CertifiedProductTestingLabDTO;
import gov.healthit.chpl.dto.CuresUpdateEventDTO;
import gov.healthit.chpl.dto.ListingToListingMapDTO;
import gov.healthit.chpl.dto.TargetedUserDTO;
import gov.healthit.chpl.dto.TestingLabDTO;
import gov.healthit.chpl.entity.CertificationStatusType;
import gov.healthit.chpl.entity.developer.DeveloperStatusType;
import gov.healthit.chpl.exception.CertifiedProductUpdateException;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.exception.MissingReasonException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.listing.measure.ListingMeasureDAO;
import gov.healthit.chpl.manager.impl.SecuredManager;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.qmsStandard.QmsStandard;
import gov.healthit.chpl.qmsStandard.QmsStandardDAO;
import gov.healthit.chpl.scheduler.job.TriggerDeveloperBanJob;
import gov.healthit.chpl.search.annotation.ReplaceListingSearchCache;
import gov.healthit.chpl.service.CuresUpdateService;
import gov.healthit.chpl.sharedstore.listing.ListingIcsSharedStoreHandler;
import gov.healthit.chpl.sharedstore.listing.ListingStoreRemove;
import gov.healthit.chpl.sharedstore.listing.RemoveBy;
import gov.healthit.chpl.upload.listing.normalizer.ListingDetailsNormalizer;
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
    private CertificationStatusDAO certStatusDao;
    private ListingGraphDAO listingGraphDao;
    private ResourcePermissions resourcePermissions;
    private CertifiedProductSearchResultDAO certifiedProductSearchResultDAO;
    private CertifiedProductDetailsManager certifiedProductDetailsManager;
    private SchedulerManager schedulerManager;
    private ActivityManager activityManager;
    private ListingDetailsNormalizer listingNormalizer;
    private ListingValidatorFactory validatorFactory;
    private CuresUpdateService curesUpdateService;
    private ListingIcsSharedStoreHandler icsSharedStoreHandler;

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
            CertificationStatusDAO certStatusDao, ListingGraphDAO listingGraphDao,
            ResourcePermissions resourcePermissions,
            CertifiedProductSearchResultDAO certifiedProductSearchResultDAO,
            CertifiedProductDetailsManager certifiedProductDetailsManager,
            SchedulerManager schedulerManager,
            ActivityManager activityManager, ListingDetailsNormalizer listingNormalizer,
            ListingValidatorFactory validatorFactory,
            CuresUpdateService curesUpdateService,
            @Lazy ListingIcsSharedStoreHandler icsSharedStoreHandler) {

        this.msgUtil = msgUtil;
        this.cpDao = cpDao;
        this.searchDao = searchDao;
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
        this.certStatusDao = certStatusDao;
        this.listingGraphDao = listingGraphDao;
        this.resourcePermissions = resourcePermissions;
        this.certifiedProductSearchResultDAO = certifiedProductSearchResultDAO;
        this.certifiedProductDetailsManager = certifiedProductDetailsManager;
        this.schedulerManager = schedulerManager;
        this.activityManager = activityManager;
        this.listingNormalizer = listingNormalizer;
        this.validatorFactory = validatorFactory;
        this.curesUpdateService = curesUpdateService;
        this.icsSharedStoreHandler = icsSharedStoreHandler;
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

    @Deprecated
    @Transactional
    public List<IcsFamilyTreeNode> getIcsFamilyTree(String chplProductNumber) throws EntityRetrievalException {
        CertifiedProductDetailsDTO dto = getCertifiedProductDetailsDtoByChplProductNumber(chplProductNumber);
        return getIcsFamilyTree(dto.getId());
    }

    @Deprecated
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

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ONC')")
    @Transactional(readOnly = false)
    @CacheEvict(value = {
            CacheNames.GET_DECERTIFIED_DEVELOPERS, CacheNames.COLLECTIONS_DEVELOPERS
    }, allEntries = true)
    // listings collection is not evicted here because it's pre-fetched and
    // handled in a listener
    // no other caches have ACB data so we do not need to clear all
    @ListingStoreRemove(removeBy = RemoveBy.LISTING_ID, id = "#certifiedProductId")
    public CertifiedProductDTO changeOwnership(Long certifiedProductId, Long acbId)
            throws EntityRetrievalException, JsonProcessingException, EntityCreationException, CertifiedProductUpdateException {
        CertifiedProductDTO toUpdate = cpDao.getById(certifiedProductId);
        toUpdate.setCertificationBodyId(acbId);
        return cpDao.update(toUpdate);
    }

    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).CERTIFIED_PRODUCT, "
            + "T(gov.healthit.chpl.permissions.domains.CertifiedProductDomainPermissions).UPDATE, #updateRequest)")
    @Transactional(rollbackFor = {
            EntityRetrievalException.class, EntityCreationException.class, JsonProcessingException.class,
            AccessDeniedException.class, InvalidArgumentsException.class, CertifiedProductUpdateException.class
    })
    @CacheEvict(value = {
            CacheNames.ALL_DEVELOPERS, CacheNames.ALL_DEVELOPERS_INCLUDING_DELETED,
            CacheNames.COLLECTIONS_DEVELOPERS, CacheNames.COMPLAINTS
    }, allEntries = true)
    @ListingStoreRemove(removeBy = RemoveBy.LISTING_ID, id = "#updateRequest.listing.id")
    @ReplaceListingSearchCache
    public CertifiedProductDTO update(ListingUpdateRequest updateRequest)
            throws AccessDeniedException, JsonProcessingException, InvalidArgumentsException, IOException,
            ValidationException, MissingReasonException, CertifiedProductUpdateException {

        CertifiedProductSearchDetails existingListing = null;
        try {
            CertifiedProductSearchDetails updatedListing = updateRequest.getListing();
            existingListing = certifiedProductDetailsManager
                    .getCertifiedProductDetails(updatedListing.getId());

            listingNormalizer.normalize(updatedListing);

            // validate - throws ValidationException if the listing cannot be updated
            validateListingForUpdate(existingListing, updatedListing, updateRequest.isAcknowledgeWarnings());

            // if listing status has changed that may trigger other changes to developer status
            performSecondaryActionsBasedOnStatusChanges(existingListing, updatedListing, updateRequest.getReason());

            //clear all ICS family from the shared store so that ICS relationships
            //are cleared for ICS additions and ICS removals
            icsSharedStoreHandler.handle(updateRequest.getListing().getId());

            // Update the listing
            CertifiedProductDTO dtoToUpdate = new CertifiedProductDTO(updatedListing);
            CertifiedProductDTO result = cpDao.update(dtoToUpdate);
            updateListingsChildData(existingListing, updatedListing);
            updateRwtEligibilityForListingAndChildren(result);

            // Log the activity
            logCertifiedProductUpdateActivity(existingListing, updateRequest.getReason());

            return result;
        } catch (EntityRetrievalException | EntityCreationException ex) {
            String msg = msgUtil.getMessage("listing.badListingData", existingListing.getChplProductNumber());
            CertifiedProductUpdateException exception = new CertifiedProductUpdateException(msg);
            LOGGER.error(msg, ex);
            throw exception;
        }
    }

    private void logCertifiedProductUpdateActivity(CertifiedProductSearchDetails existingListing,
            String reason) throws JsonProcessingException, EntityCreationException, EntityRetrievalException {

        CertifiedProductSearchDetails changedProduct
                = certifiedProductDetailsManager.getCertifiedProductDetailsNoCache(existingListing.getId());

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

    private void updateRwtEligibilityForListingAndChildren(CertifiedProductDTO listing) {
        List<CertifiedProductDTO> listingChildren = listingGraphDao.getChildren(listing.getId());
        if (!CollectionUtils.isEmpty(listingChildren)) {
            for (CertifiedProductDTO child : listingChildren) {
                if (Integer.valueOf(listing.getIcsCode()) >= Integer.valueOf(child.getIcsCode())) {
                    continue;
                }
                updateRwtEligibilityForListingAndChildren(child);
            }
        }
    }

    private void performSecondaryActionsBasedOnStatusChanges(CertifiedProductSearchDetails existingListing,
            CertifiedProductSearchDetails updatedListing, String reason)
            throws EntityRetrievalException, JsonProcessingException, EntityCreationException, ValidationException {
        Long listingId = updatedListing.getId();
        Long productVersionId = updatedListing.getVersion().getId();
        CertificationStatus updatedStatus = updatedListing.getCurrentStatus().getStatus();
        CertificationStatus existingStatus = existingListing.getCurrentStatus().getStatus();
        // if listing status has changed that may trigger other changes
        // to developer status
        if (ObjectUtils.notEqual(updatedStatus.getName(), existingStatus.getName())) {
            // look at the updated status and see if a developer ban is appropriate
            CertificationStatus updatedStatusObj = certStatusDao.getById(updatedStatus.getId());
            Developer cpDeveloper = developerDao.getByVersion(productVersionId);
            if (cpDeveloper == null) {
                LOGGER.error("Could not find developer for product version with id " + productVersionId);
                throw new EntityNotFoundException(
                        "No developer could be located for the certified product in the update. Update cannot continue.");
            }
            DeveloperStatus newDevStatus = null;
            switch (CertificationStatusType.getValue(updatedStatusObj.getName())) {
            case SuspendedByOnc:
            case TerminatedByOnc:
                // only onc admin can do this and it always triggers developer ban
                if (resourcePermissions.isUserRoleAdmin() || resourcePermissions.isUserRoleOnc()) {
                    // find the new developer status
                    if (updatedStatusObj.getName().equals(CertificationStatusType.SuspendedByOnc.toString())) {
                        newDevStatus = devStatusDao.getByName(DeveloperStatusType.SuspendedByOnc.toString());
                    } else if (updatedStatusObj.getName()
                            .equals(CertificationStatusType.TerminatedByOnc.toString())) {
                        newDevStatus = devStatusDao
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
            if (newDevStatus != null) {
                DeveloperStatusEvent statusHistoryToAdd = new DeveloperStatusEvent();
                statusHistoryToAdd.setDeveloperId(cpDeveloper.getId());
                statusHistoryToAdd.setStatus(newDevStatus);
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

        for (CertifiedProductQmsStandard toAdd : qmsToAdd) {
            QmsStandard qmsItem = qmsDao.getById(toAdd.getQmsStandardId());
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
            if (!Objects.equals(toUpdate.getOrig().getApplicableCriteria(),
                    toUpdate.getUpdated().getApplicableCriteria())
                    || !Objects.equals(toUpdate.getOrig().getQmsModification(),
                            toUpdate.getUpdated().getQmsModification())) {
                hasChanged = true;
            }

            if (hasChanged) {
                CertifiedProductQmsStandard stdToUpdate = toUpdate.getUpdated();
                QmsStandard qmsItem = qmsDao.getById(stdToUpdate.getQmsStandardId());
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

        for (CertifiedProductAccessibilityStandard toAdd : accStdsToAdd) {
            AccessibilityStandard item = asDao.getById(toAdd.getAccessibilityStandardId());
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
            if (!Objects.equals(toUpdate.getOrig().getEventDate(), toUpdate.getUpdated().getEventDate())
                    || !Objects.equals(toUpdate.getOrig().getStatus().getId(),
                            toUpdate.getUpdated().getStatus().getId())
                    || !Objects.equals(toUpdate.getOrig().getStatus().getName(),
                            toUpdate.getUpdated().getStatus().getName())
                    || !Objects.equals(toUpdate.getOrig().getReason(), toUpdate.getUpdated().getReason())) {
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
        if (!Objects.equals(existingCqm.getSuccess(), updatedCqm.getSuccess())) {
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
        ChplOneTimeTrigger possibleDeveloperBanTrigger = new ChplOneTimeTrigger();
        ChplJob triggerDeveloperBanJob = new ChplJob();
        triggerDeveloperBanJob.setName(TriggerDeveloperBanJob.JOB_NAME);
        triggerDeveloperBanJob.setGroup(SchedulerManager.CHPL_JOBS_KEY);
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put(TriggerDeveloperBanJob.UPDATED_LISTING, updatedListing);
        jobDataMap.put(TriggerDeveloperBanJob.USER, AuthUtil.getCurrentUser());
        jobDataMap.put(TriggerDeveloperBanJob.CHANGE_DATE, System.currentTimeMillis());
        jobDataMap.put(TriggerDeveloperBanJob.USER_PROVIDED_REASON, reason);
        triggerDeveloperBanJob.setJobDataMap(jobDataMap);
        possibleDeveloperBanTrigger.setJob(triggerDeveloperBanJob);
        possibleDeveloperBanTrigger.setRunDateMillis(System.currentTimeMillis() + SchedulerManager.FIVE_SECONDS_IN_MILLIS);
        try {
            possibleDeveloperBanTrigger = schedulerManager.createBackgroundJobTrigger(possibleDeveloperBanTrigger);
        } catch (Exception ex) {
            LOGGER.error("Unable to schedule Trigger Developer Ban Job.", ex);
        }
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

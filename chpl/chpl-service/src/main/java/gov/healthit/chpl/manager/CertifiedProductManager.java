package gov.healthit.chpl.manager;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.quartz.JobDataMap;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.accessibilityStandard.AccessibilityStandard;
import gov.healthit.chpl.accessibilityStandard.AccessibilityStandardDAO;
import gov.healthit.chpl.caching.CacheNames;
import gov.healthit.chpl.caching.ListingSearchCacheRefresh;
import gov.healthit.chpl.certifiedproduct.CertifiedProductDetailsManager;
import gov.healthit.chpl.certifiedproduct.service.CertificationResultSynchronizationService;
import gov.healthit.chpl.certifiedproduct.service.CertificationStatusEventsService;
import gov.healthit.chpl.certifiedproduct.service.CqmResultSynchronizationService;
import gov.healthit.chpl.certifiedproduct.service.SedSynchronizationService;
import gov.healthit.chpl.dao.CertificationStatusDAO;
import gov.healthit.chpl.dao.CertificationStatusEventDAO;
import gov.healthit.chpl.dao.CertifiedProductAccessibilityStandardDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.CertifiedProductQmsStandardDAO;
import gov.healthit.chpl.dao.CertifiedProductTargetedUserDAO;
import gov.healthit.chpl.dao.CertifiedProductTestingLabDAO;
import gov.healthit.chpl.dao.ListingGraphDAO;
import gov.healthit.chpl.dao.PromotingInteroperabilityUserDAO;
import gov.healthit.chpl.dao.TargetedUserDAO;
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
import gov.healthit.chpl.domain.InheritedCertificationStatus;
import gov.healthit.chpl.domain.ListingMeasure;
import gov.healthit.chpl.domain.ListingUpdateRequest;
import gov.healthit.chpl.domain.PromotingInteroperabilityUser;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.domain.schedule.ChplJob;
import gov.healthit.chpl.domain.schedule.ChplOneTimeTrigger;
import gov.healthit.chpl.domain.schedule.ScheduledSystemJob;
import gov.healthit.chpl.dto.CertifiedProductAccessibilityStandardDTO;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.dto.CertifiedProductQmsStandardDTO;
import gov.healthit.chpl.dto.CertifiedProductTargetedUserDTO;
import gov.healthit.chpl.dto.ListingToListingMapDTO;
import gov.healthit.chpl.dto.TargetedUserDTO;
import gov.healthit.chpl.email.ChplHtmlEmailBuilder;
import gov.healthit.chpl.exception.ActivityException;
import gov.healthit.chpl.exception.CertifiedProductUpdateException;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.exception.MissingReasonException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.listing.measure.ListingMeasureDAO;
import gov.healthit.chpl.manager.auth.UserManager;
import gov.healthit.chpl.manager.impl.SecuredManager;
import gov.healthit.chpl.notifier.BusinessRulesOverrideNotifierMessage;
import gov.healthit.chpl.notifier.ChplTeamNotifier;
import gov.healthit.chpl.notifier.FutureCertificationStatusNotifierMessage;
import gov.healthit.chpl.permissions.ResourcePermissionsFactory;
import gov.healthit.chpl.qmsStandard.QmsStandard;
import gov.healthit.chpl.qmsStandard.QmsStandardDAO;
import gov.healthit.chpl.scheduler.job.certificationStatus.UpdateCurrentCertificationStatusJob;
import gov.healthit.chpl.sharedstore.listing.ListingIcsSharedStoreHandler;
import gov.healthit.chpl.sharedstore.listing.ListingStoreRemove;
import gov.healthit.chpl.sharedstore.listing.RemoveBy;
import gov.healthit.chpl.upload.listing.normalizer.ListingDetailsNormalizer;
import gov.healthit.chpl.util.AuthUtil;
import gov.healthit.chpl.util.DateUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.listing.ListingValidatorFactory;
import gov.healthit.chpl.validation.listing.Validator;
import gov.healthit.chpl.validation.listing.normalizer.BaselineStandardAsOfTodayNormalizer;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service("certifiedProductManager")
public class CertifiedProductManager extends SecuredManager {
    private ErrorMessageUtil msgUtil;
    private CertifiedProductDAO cpDao;
    private QmsStandardDAO qmsDao;
    private TargetedUserDAO targetedUserDao;
    private AccessibilityStandardDAO asDao;
    private CertifiedProductQmsStandardDAO cpQmsDao;
    private ListingMeasureDAO cpMeasureDao;
    private CertifiedProductTestingLabDAO cpTestingLabDao;
    private CertifiedProductTargetedUserDAO cpTargetedUserDao;
    private CertifiedProductAccessibilityStandardDAO cpAccStdDao;
    private ProductManager productManager;
    private ProductVersionManager versionManager;
    private CertificationStatusEventDAO statusEventDao;
    private PromotingInteroperabilityUserDAO piuDao;
    private CertificationResultSynchronizationService certResultService;
    private CqmResultSynchronizationService cqmResultService;
    private SedSynchronizationService sedService;
    private CertificationStatusDAO certStatusDao;
    private ListingGraphDAO listingGraphDao;
    private ResourcePermissionsFactory resourcePermissionsFactory;
    private CertifiedProductDetailsManager certifiedProductDetailsManager;
    private SchedulerManager schedulerManager;
    private UserManager userManager;
    private ActivityManager activityManager;
    private ListingDetailsNormalizer listingNormalizer;
    private BaselineStandardAsOfTodayNormalizer baselineStandardNormalizer;
    private ListingValidatorFactory validatorFactory;
    private ListingIcsSharedStoreHandler icsSharedStoreHandler;
    private CertificationStatusEventsService certStatusEventsService;
    private ChplTeamNotifier chplTeamNotifier;
    private Environment env;
    private ChplHtmlEmailBuilder chplHtmlEmailBuilder;

    public CertifiedProductManager() {
    }

    @SuppressWarnings({
            "checkstyle:parameternumber"
    })
    @Autowired
    public CertifiedProductManager(ErrorMessageUtil msgUtil, CertifiedProductDAO cpDao,
            QmsStandardDAO qmsDao, TargetedUserDAO targetedUserDao, AccessibilityStandardDAO asDao, CertifiedProductQmsStandardDAO cpQmsDao,
            ListingMeasureDAO cpMeasureDao, CertifiedProductTestingLabDAO cpTestingLabDao,
            CertifiedProductTargetedUserDAO cpTargetedUserDao,
            CertifiedProductAccessibilityStandardDAO cpAccStdDao,
            ProductManager productManager, ProductVersionManager versionManager, CertificationStatusEventDAO statusEventDao,
            PromotingInteroperabilityUserDAO piuDao,
            CertificationResultSynchronizationService certResultService, CqmResultSynchronizationService cqmResultService,
            SedSynchronizationService sedService,
            CertificationStatusDAO certStatusDao, ListingGraphDAO listingGraphDao,
            ResourcePermissionsFactory resourcePermissionsFactory,
            CertifiedProductDetailsManager certifiedProductDetailsManager,
            SchedulerManager schedulerManager, ActivityManager activityManager, UserManager userManager,
            ListingDetailsNormalizer listingNormalizer,
            BaselineStandardAsOfTodayNormalizer baselineStandardNormalizer,
            ListingValidatorFactory validatorFactory,
            @Lazy ListingIcsSharedStoreHandler icsSharedStoreHandler,
            CertificationStatusEventsService certStatusEventsService, ChplTeamNotifier chplteamNotifier,
            Environment env, ChplHtmlEmailBuilder chplHtmlEmailBuilder) {

        this.msgUtil = msgUtil;
        this.cpDao = cpDao;
        this.qmsDao = qmsDao;
        this.targetedUserDao = targetedUserDao;
        this.asDao = asDao;
        this.cpQmsDao = cpQmsDao;
        this.cpMeasureDao = cpMeasureDao;
        this.cpTestingLabDao = cpTestingLabDao;
        this.cpTargetedUserDao = cpTargetedUserDao;
        this.cpAccStdDao = cpAccStdDao;
        this.productManager = productManager;
        this.versionManager = versionManager;
        this.statusEventDao = statusEventDao;
        this.piuDao = piuDao;
        this.certResultService = certResultService;
        this.cqmResultService = cqmResultService;
        this.sedService = sedService;
        this.certStatusDao = certStatusDao;
        this.listingGraphDao = listingGraphDao;
        this.resourcePermissionsFactory = resourcePermissionsFactory;
        this.certifiedProductDetailsManager = certifiedProductDetailsManager;
        this.schedulerManager = schedulerManager;
        this.activityManager = activityManager;
        this.userManager = userManager;
        this.listingNormalizer = listingNormalizer;
        this.baselineStandardNormalizer = baselineStandardNormalizer;
        this.validatorFactory = validatorFactory;
        this.icsSharedStoreHandler = icsSharedStoreHandler;
        this.certStatusEventsService = certStatusEventsService;
        this.chplTeamNotifier = chplteamNotifier;
        this.env = env;
        this.chplHtmlEmailBuilder = chplHtmlEmailBuilder;
    }

    @Transactional(readOnly = true)
    public CertifiedProductDTO getById(Long id) throws EntityRetrievalException {
        CertifiedProductDTO result = cpDao.getById(id);
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
            CacheNames.COLLECTIONS_LISTINGS,
            CacheNames.COLLECTIONS_DEVELOPERS, CacheNames.COMPLAINTS,
            CacheNames.QUESTIONABLE_ACTIVITIES
    }, allEntries = true)
    @ListingSearchCacheRefresh
    @ListingStoreRemove(removeBy = RemoveBy.LISTING_ID, id = "#updateRequest.listing.id")
    public CertifiedProductDTO update(ListingUpdateRequest updateRequest)
            throws MissingReasonException, ValidationException, InvalidArgumentsException, IOException, ActivityException, CertifiedProductUpdateException {

        CertifiedProductSearchDetails existingListing = null;
        try {
            CertifiedProductSearchDetails updatedListing = updateRequest.getListing();
            existingListing = certifiedProductDetailsManager.getCertifiedProductDetails(updatedListing.getId());

            listingNormalizer.normalize(updatedListing, List.of(baselineStandardNormalizer));

            // validate - throws ValidationException if the listing cannot be updated
            validateListingForUpdate(existingListing, updatedListing, updateRequest.isAcknowledgeWarnings(), updateRequest.isAcknowledgeBusinessErrors());

            // clear all ICS family from the shared store so that ICS relationships
            // are cleared for ICS additions and ICS removals
            icsSharedStoreHandler.handle(updateRequest.getListing().getId());

            // Update the listing
            CertifiedProductDTO dtoToUpdate = new CertifiedProductDTO(updatedListing);
            CertifiedProductDTO result = cpDao.update(dtoToUpdate);
            updateListingsChildData(existingListing, updatedListing);
            updateRwtEligibilityForListingAndChildren(result);

            CertifiedProductSearchDetails updatedListingNoCache = certifiedProductDetailsManager.getCertifiedProductDetailsNoCache(existingListing.getId());

            // Log the activity
            Long activityId = logCertifiedProductUpdateActivity(existingListing, updatedListingNoCache, updateRequest.getReason());

            deleteTriggersForRemovedFutureListingStatusChanges(existingListing, updatedListingNoCache);
            createTriggersForAddedFutureListingStatusChanges(existingListing, updatedListingNoCache, activityId, updateRequest.getReason());

            //Send notification to Team
            if (wereBusinessRulesOverriddenDuringUpdate(updateRequest)) {
                chplTeamNotifier.sendNotification(new BusinessRulesOverrideNotifierMessage(
                        updateRequest.getListing().getChplProductNumber(),
                        AuthUtil.getCurrentUser(),
                        updateRequest.getListing().getBusinessErrorMessages(),
                        env,
                        chplHtmlEmailBuilder));
            }

            return result;
        } catch (EntityRetrievalException | EntityCreationException ex) {
            String msg = msgUtil.getMessage("listing.badListingData", existingListing.getChplProductNumber());
            CertifiedProductUpdateException exception = new CertifiedProductUpdateException(msg);
            LOGGER.error(msg, ex);
            throw exception;
        }
    }


    private boolean wereBusinessRulesOverriddenDuringUpdate(ListingUpdateRequest request) {
        return !request.getListing().getBusinessErrorMessages().isEmpty()
                && request.isAcknowledgeBusinessErrors();
    }

    private Long logCertifiedProductUpdateActivity(CertifiedProductSearchDetails existingListing, CertifiedProductSearchDetails updatedListing,
            String reason) throws ActivityException {
        Long activityId = activityManager.addActivity(ActivityConcept.CERTIFIED_PRODUCT, existingListing.getId(),
                "Updated certified product " + updatedListing.getChplProductNumber() + ".", existingListing,
                updatedListing, reason);
        return activityId;
    }

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
        updateCertificationStatusEvents(existingListing, updatedListing);

        updatePromotingInteroperabilityUserHistory(updatedListing.getId(),
                existingListing.getPromotingInteroperabilityUserHistory(),
                updatedListing.getPromotingInteroperabilityUserHistory());
        updateCertifications(existingListing, updatedListing,
                existingListing.getCertificationResults(), updatedListing.getCertificationResults());
        copyCriterionIdsToCqmMappings(updatedListing);
        updateCqms(updatedListing, existingListing.getCqmResults(), updatedListing.getCqmResults());
        updateSed(existingListing, updatedListing);
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

    private void validateListingForUpdate(CertifiedProductSearchDetails existingListing,
            CertifiedProductSearchDetails updatedListing, boolean acknowledgeWarnings, boolean acknowledgeBusinessErrors) throws ValidationException {
        Validator validator = validatorFactory.getValidator(updatedListing);
        if (validator != null) {
            validator.validate(existingListing, updatedListing);
        }

        if (shouldValidationExceptionBeThrown(updatedListing, acknowledgeBusinessErrors, acknowledgeWarnings)) {
            for (String err : updatedListing.getErrorMessages()) {
                LOGGER.error("Error updating listing " + updatedListing.getChplProductNumber() + ": " + err);
            }
            for (String warning : updatedListing.getWarningMessages()) {
                LOGGER.error("Warning updating listing " + updatedListing.getChplProductNumber() + ": " + warning);
            }
            throw new ValidationException(updatedListing.getErrorMessages(), updatedListing.getBusinessErrorMessages(),
                    updatedListing.getDataErrorMessages(), updatedListing.getWarningMessages());
        }
    }

    private int updateTestingLabs(Long listingId, List<CertifiedProductTestingLab> existingTestingLabs,
            List<CertifiedProductTestingLab> updatedTestingLabs)
            throws EntityCreationException, EntityRetrievalException, JsonProcessingException {

        int numChanges = 0;

        List<CertifiedProductTestingLab> testingLabsToAdd = new ArrayList<CertifiedProductTestingLab>(updatedTestingLabs);
        testingLabsToAdd.removeIf(tl -> doesCertifiedProductTestingLabExistInList(tl, existingTestingLabs));

        List<CertifiedProductTestingLab> testingLabsToRemove = new ArrayList<CertifiedProductTestingLab>(existingTestingLabs);
        testingLabsToRemove.removeIf(tl -> doesCertifiedProductTestingLabExistInList(tl, updatedTestingLabs));

        numChanges = testingLabsToAdd.size() + testingLabsToRemove.size();

        testingLabsToAdd.forEach(tl -> addCertifiedProductTestingLab(tl, listingId));
        testingLabsToRemove.forEach(tl -> deleteCertifiedProductTestingLab(tl));

        return numChanges;
    }

    private Boolean doesCertifiedProductTestingLabExistInList(CertifiedProductTestingLab toFind, List<CertifiedProductTestingLab> list) {
        return list.stream()
                .filter(item -> item.matches(toFind))
                .findAny()
                .isPresent();
    }

    private void deleteCertifiedProductTestingLab(CertifiedProductTestingLab toDelete) {
        try {
            cpTestingLabDao.deleteCertifiedProductTestingLab(toDelete.getId());
        } catch (Exception e) {
            LOGGER.info("Could not delete CertifiedProductTestingLab with Id: {}, {}", toDelete.getId(), e.getMessage(), e);
        }
    }

    private void addCertifiedProductTestingLab(CertifiedProductTestingLab toAdd, Long listingId) {
        try {
            cpTestingLabDao.createCertifiedProductTestingLab(toAdd, listingId);
        } catch (Exception e) {
            LOGGER.info("Could not add CertifiedProductTestingLab with Id: {}, for Listing: {}, {}", toAdd.getTestingLab().getId(), listingId, e.getMessage(), e);
        }
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
                            if (haveQmsDetailsChanged(existingItem, updatedItem)) {
                                inExistingListing = true;
                                qmsToAdd.add(updatedItem);
                                idsToRemove.add(existingItem.getId());
                            }
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

        for (Long idToRemove : idsToRemove) {
            cpQmsDao.deleteCertifiedProductQms(idToRemove);
        }
        return numChanges;
    }

    private boolean haveQmsDetailsChanged(CertifiedProductQmsStandard orig, CertifiedProductQmsStandard updated) {
        return !StringUtils.equals(orig.getApplicableCriteria(), updated.getApplicableCriteria())
                || !StringUtils.equals(orig.getQmsModification(), updated.getQmsModification());
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
                                ? existingItem.getId().equals(updatedItem.getId())
                                : inUpdatedListing;
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

    private int updateCertificationStatusEvents(CertifiedProductSearchDetails existingListing,
            CertifiedProductSearchDetails updatedListing)
            throws EntityCreationException, EntityRetrievalException, JsonProcessingException {

        int numChanges = 0;
        List<CertificationStatusEvent> statusEventsToAdd = new ArrayList<CertificationStatusEvent>();
        List<Long> idsToRemove = new ArrayList<Long>();

        List<CertificationStatusEvent> addedCertStatusEvents
            = certStatusEventsService.getAddedCertificationStatusEvents(existingListing, updatedListing);
        addedCertStatusEvents.stream()
            .forEach(addedCertStatusEvent -> statusEventsToAdd.add(addedCertStatusEvent));

        List<CertificationStatusEvent> removedCertStatusEvents
            = certStatusEventsService.getRemovedCertificationStatusEvents(existingListing, updatedListing);
        removedCertStatusEvents.stream()
            .forEach(removedCertStatusEvent -> idsToRemove.add(removedCertStatusEvent.getId()));

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
            statusEventDao.create(updatedListing.getId(), statusEventToAdd);
        }

        for (Long idToRemove : idsToRemove) {
            statusEventDao.delete(idToRemove);
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
            throws EntityCreationException, EntityRetrievalException {
        int numChanges = 0;
        try {
            numChanges = certResultService.synchronizeCertificationResults(
                    existingListing, updatedListing, existingCertifications, updatedCertifications);
        } catch (Exception ex) {
            LOGGER.error("Exception synchronizing certification results for listing " + existingListing.getId(), ex);
            throw new EntityCreationException("Exception synchronizing certification results for listing " + existingListing.getId());
        }
        return numChanges;
    }

    private void copyCriterionIdsToCqmMappings(CertifiedProductSearchDetails listing) {
        for (CQMResultDetails cqmResult : listing.getCqmResults()) {
            for (CQMResultCertification cqmCertMapping : cqmResult.getCriteria()) {
                if (cqmCertMapping.getCertificationId() == null
                        && !StringUtils.isEmpty(cqmCertMapping.getCertificationNumber())) {
                    for (CertificationResult certResult : listing.getCertificationResults()) {
                        if (certResult.getSuccess().equals(Boolean.TRUE)
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
        return cqmResultService.synchronizeCqms(listing, existingCqmDetails, updatedCqmDetails);
    }

    private void updateSed(CertifiedProductSearchDetails origListing, CertifiedProductSearchDetails updatedListing)
            throws EntityCreationException, EntityRetrievalException {
        sedService.synchronizeTestTasks(origListing, updatedListing,
                origListing.getSed() == null || CollectionUtils.isEmpty(origListing.getSed().getTestTasks()) ? List.of() : origListing.getSed().getTestTasks(),
                updatedListing.getSed() == null || CollectionUtils.isEmpty(updatedListing.getSed().getTestTasks()) ? List.of() : updatedListing.getSed().getTestTasks());
    }

    private void deleteTriggersForRemovedFutureListingStatusChanges(CertifiedProductSearchDetails existingListing,
            CertifiedProductSearchDetails updatedListing) {
        List<CertificationStatusEvent> removedStatusEvents
            = certStatusEventsService.getRemovedCertificationStatusEvents(existingListing, updatedListing);
        removedStatusEvents.stream()
            .filter(removedStatusEvent -> removedStatusEvent.getEventDay().isAfter(LocalDate.now()))
            .forEach(removedFutureStatusEvent -> deleteTriggerForCertificationStatusEvent(existingListing.getId(), removedFutureStatusEvent.getEventDay()));
    }

    private void deleteTriggerForCertificationStatusEvent(Long listingId, LocalDate certStatusChangeDay) {
        ScheduledSystemJob scheduledJobForListingOnDay = getScheduledJobForListingOnDay(certStatusChangeDay, listingId);
        if (scheduledJobForListingOnDay != null) {
            LOGGER.info("Update certification status job scheduled for " + listingId + " on "
                    + certStatusChangeDay + ". Cancelling trigger " + scheduledJobForListingOnDay.getTriggerName());
            try {
                schedulerManager.deleteTriggerWithoutNotification(scheduledJobForListingOnDay.getTriggerGroup(),
                        scheduledJobForListingOnDay.getTriggerName());
            } catch (SchedulerException ex) {
                LOGGER.error("Unable to delete trigger " + scheduledJobForListingOnDay.getTriggerName()
                        + " in group " + scheduledJobForListingOnDay.getTriggerGroup());
            }
        }
    }

    private void createTriggersForAddedFutureListingStatusChanges(CertifiedProductSearchDetails existingListing,
            CertifiedProductSearchDetails updatedListing, Long activityId, String reason) {
        List<CertificationStatusEvent> addedCertStatusEvents
            = certStatusEventsService.getAddedCertificationStatusEvents(existingListing, updatedListing);
        addedCertStatusEvents.stream()
            .filter(addedCertStatusEvent -> !addedCertStatusEvent.getEventDay().isBefore(LocalDate.now()))
            .forEach(addedCertStatusEvent -> createTriggerToUpdateCurrentCertificationStatusJob(
                    updatedListing,
                    activityId,
                    addedCertStatusEvent.getEventDay(),
                    reason));
    }

    private void createTriggerToUpdateCurrentCertificationStatusJob(CertifiedProductSearchDetails updatedListing, Long activityId,
            LocalDate currentStatusUpdateDay, String reason) {

        ChplOneTimeTrigger updateStatusTrigger = new ChplOneTimeTrigger();
        ChplJob updateStatusJob = new ChplJob();
        updateStatusJob.setName(UpdateCurrentCertificationStatusJob.JOB_NAME);
        updateStatusJob.setGroup(SchedulerManager.CHPL_BACKGROUND_JOBS_KEY);
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put(UpdateCurrentCertificationStatusJob.LISTING_ID, updatedListing.getId());
        jobDataMap.put(UpdateCurrentCertificationStatusJob.USER, AuthUtil.getCurrentUser());
        jobDataMap.put(UpdateCurrentCertificationStatusJob.CERTIFICATION_STATUS_EVENT_DAY, currentStatusUpdateDay);
        jobDataMap.put(UpdateCurrentCertificationStatusJob.ACTIVITY_ID, activityId);
        jobDataMap.put(UpdateCurrentCertificationStatusJob.USER_PROVIDED_REASON, reason);
        updateStatusJob.setJobDataMap(jobDataMap);
        updateStatusTrigger.setJob(updateStatusJob);

        if (currentStatusUpdateDay.isAfter(LocalDate.now())) {
            sendFutureCertificationStatusNotification(updatedListing, currentStatusUpdateDay, activityId);
            LocalDateTime fiveAfterMidnightEastern = LocalDateTime.of(currentStatusUpdateDay, LocalTime.of(0, 5));
            updateStatusTrigger.setRunDateMillis(DateUtil.toEpochMillis(DateUtil.fromEasternToSystem(fiveAfterMidnightEastern)));
        } else {
            LOGGER.info("Activity " + activityId + " added a certification status to listing " + updatedListing.getId() + " that was in the past. No job will be scheduled.");
            return;
        }

        try {
            updateStatusTrigger = schedulerManager.createBackgroundJobTrigger(updateStatusTrigger);
            LOGGER.info("Scheduled " + updateStatusTrigger.getJob().getName() + " to run on " + currentStatusUpdateDay
                    + " for listing " + updatedListing.getId() + " and activity ID " + activityId);
        } catch (Exception ex) {
            LOGGER.error("Unable to schedule " + updateStatusTrigger.getJob().getName() + ".", ex);
        }
    }

    private ScheduledSystemJob getScheduledJobForListingOnDay(LocalDate jobDay, Long listingId) {
        Optional<ScheduledSystemJob> scheduledJobForListingOnDay = getAllScheduledSystemJobs().stream()
                .filter(systemJob -> systemJob.getName().equalsIgnoreCase(UpdateCurrentCertificationStatusJob.JOB_NAME)
                        && DateUtil.toLocalDate(systemJob.getNextRunDate().getTime()).equals(jobDay)
                        && systemJob.getJobDataMap().get(UpdateCurrentCertificationStatusJob.LISTING_ID) != null
                        && systemJob.getJobDataMap().getLong(UpdateCurrentCertificationStatusJob.LISTING_ID) == listingId.longValue())
                .findAny();
        return scheduledJobForListingOnDay.isPresent() ? scheduledJobForListingOnDay.get() : null;
    }

    private List<ScheduledSystemJob> getAllScheduledSystemJobs() {
        try {
            return schedulerManager.getScheduledSystemJobsForUser();
        } catch (SchedulerException e) {
            LOGGER.error("Could not retrieve list of scheduled system Quartz jobs", e);
            return List.of();
        }
    }

    private void sendFutureCertificationStatusNotification(CertifiedProductSearchDetails listing,
            LocalDate currentStatusUpdateDay, Long activityId) {
        chplTeamNotifier.sendFutureCertificationStatusUsedNotification(
                new FutureCertificationStatusNotifierMessage(listing, activityId, currentStatusUpdateDay, AuthUtil.getUsername(), env, chplHtmlEmailBuilder));
    }

    private boolean doErrorMessagesExist(CertifiedProductSearchDetails listing) {
        return !CollectionUtils.isEmpty(listing.getErrorMessages().castToCollection());
    }

    private boolean doBusinessErrorMessagesExist(CertifiedProductSearchDetails listing) {
        return !CollectionUtils.isEmpty(listing.getBusinessErrorMessages().castToCollection());
    }

    private boolean doWarningMessagesExist(CertifiedProductSearchDetails listing) {
        return !CollectionUtils.isEmpty(listing.getWarningMessages().castToCollection());
    }

    private boolean doDataErrorMessagesExist(CertifiedProductSearchDetails listing) {
        return !CollectionUtils.isEmpty(listing.getDataErrorMessages().castToCollection());
    }

    private boolean shouldValidationExceptionBeThrown(CertifiedProductSearchDetails listing, boolean acknowledgeBusinessErrors, boolean acknowledgeWarnings) {
        // return true when we want to throw ValidationException
        if (doErrorMessagesExist(listing)) {
            if (resourcePermissionsFactory.get().isUserRoleAdmin() || resourcePermissionsFactory.get().isUserRoleOnc()) {
                return doDataErrorMessagesExist(listing) || (doBusinessErrorMessagesExist(listing) && !acknowledgeBusinessErrors);
            } else {
                return true;
            }
        } else {
            return doWarningMessagesExist(listing) && !acknowledgeWarnings;
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
}

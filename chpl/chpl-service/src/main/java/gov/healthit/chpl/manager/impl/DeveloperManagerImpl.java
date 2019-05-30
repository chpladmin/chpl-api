package gov.healthit.chpl.manager.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ff4j.FF4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.caching.CacheNames;
import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.DecertifiedDeveloper;
import gov.healthit.chpl.domain.DecertifiedDeveloperResult;
import gov.healthit.chpl.domain.DeveloperTransparency;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.dto.DecertifiedDeveloperDTO;
import gov.healthit.chpl.dto.DecertifiedDeveloperDTODeprecated;
import gov.healthit.chpl.dto.DeveloperACBMapDTO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.DeveloperStatusEventDTO;
import gov.healthit.chpl.dto.DeveloperStatusEventPair;
import gov.healthit.chpl.dto.ProductDTO;
import gov.healthit.chpl.dto.ProductOwnerDTO;
import gov.healthit.chpl.entity.AttestationType;
import gov.healthit.chpl.entity.developer.DeveloperStatusType;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.MissingReasonException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.ActivityManager;
import gov.healthit.chpl.manager.CertificationBodyManager;
import gov.healthit.chpl.manager.CertifiedProductDetailsManager;
import gov.healthit.chpl.manager.CertifiedProductManager;
import gov.healthit.chpl.manager.DeveloperManager;
import gov.healthit.chpl.manager.ProductManager;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.AuthUtil;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.ValidationUtils;
import gov.healthit.chpl.validation.developer.DeveloperCreationValidator;
import gov.healthit.chpl.validation.developer.DeveloperUpdateValidator;

/**
 * Implementation of DeveloperManager class.
 * 
 * @author TYoung
 *
 */
@Lazy
@Service
public class DeveloperManagerImpl extends SecuredManager implements DeveloperManager {
    private static final Logger LOGGER = LogManager.getLogger(DeveloperManagerImpl.class);

    private DeveloperDAO developerDao;
    private ProductManager productManager;
    private CertificationBodyManager acbManager;
    private CertifiedProductManager cpManager;
    private CertifiedProductDetailsManager cpdManager;
    private CertificationBodyDAO certificationBodyDao;
    private CertifiedProductDAO certifiedProductDAO;
    private ChplProductNumberUtil chplProductNumberUtil;
    private ActivityManager activityManager;
    private DeveloperCreationValidator creationValidator;
    private DeveloperUpdateValidator updateValidator;
    private ErrorMessageUtil msgUtil;
    private ResourcePermissions resourcePermissions;
    private FF4j ff4j;

    /**
     * Autowired constructor for dependency injection.
     *
     * @param developerDao
     * @param productManager
     * @param acbManager
     * @param certificationBodyDao
     * @param certifiedProductDAO
     * @param chplProductNumberUtil
     * @param activityManager
     * @param msgUtil
     */
    @Autowired
    public DeveloperManagerImpl(final DeveloperDAO developerDao, final ProductManager productManager,
            final CertificationBodyManager acbManager, final CertifiedProductManager cpManager,
            final CertifiedProductDetailsManager cpdManager, final CertificationBodyDAO certificationBodyDao,
            final CertifiedProductDAO certifiedProductDAO, final ChplProductNumberUtil chplProductNumberUtil,
            final ActivityManager activityManager, final DeveloperCreationValidator creationValidator,
            final DeveloperUpdateValidator updateValidator, final ErrorMessageUtil msgUtil,
            final ResourcePermissions resourcePermissions, final FF4j ff4j) {
        this.developerDao = developerDao;
        this.productManager = productManager;
        this.acbManager = acbManager;
        this.cpManager = cpManager;
        this.cpdManager = cpdManager;
        this.certificationBodyDao = certificationBodyDao;
        this.certifiedProductDAO = certifiedProductDAO;
        this.chplProductNumberUtil = chplProductNumberUtil;
        this.activityManager = activityManager;
        this.creationValidator = creationValidator;
        this.updateValidator = updateValidator;
        this.msgUtil = msgUtil;
        this.resourcePermissions = resourcePermissions;
        this.ff4j = ff4j;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(CacheNames.ALL_DEVELOPERS)
    public List<DeveloperDTO> getAll() {
        List<DeveloperDTO> allDevelopers = developerDao.findAll();
        List<DeveloperDTO> allDevelopersWithTransparencies = addTransparencyMappings(allDevelopers);
        return allDevelopersWithTransparencies;
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).DEVELOPER, "
            + "T(gov.healthit.chpl.permissions.domains.DeveloperDomainPermissions).GET_ALL_WITH_DELETED)")
    @Cacheable(CacheNames.ALL_DEVELOPERS_INCLUDING_DELETED)
    public List<DeveloperDTO> getAllIncludingDeleted() {
        List<DeveloperDTO> allDevelopers = developerDao.findAllIncludingDeleted();
        List<DeveloperDTO> allDevelopersWithTransparencies = addTransparencyMappings(allDevelopers);
        return allDevelopersWithTransparencies;
    }

    @Override
    @Transactional(readOnly = true)
    public DeveloperDTO getById(final Long id, final boolean allowDeleted)
            throws EntityRetrievalException {
        DeveloperDTO developer = developerDao.getById(id, allowDeleted);
        List<CertificationBodyDTO> availableAcbs = resourcePermissions.getAllAcbsForCurrentUser();
        if (availableAcbs == null || availableAcbs.size() == 0) {
            availableAcbs = acbManager.getAll();
        }
        // someone will see either the transparencies that apply to the ACBs to
        // which they have access
        // or they will see the transparencies for all ACBs if they are an admin
        // or not logged in
        for (CertificationBodyDTO acb : availableAcbs) {
            DeveloperACBMapDTO map = developerDao.getTransparencyMapping(developer.getId(), acb.getId());
            if (map == null) {
                DeveloperACBMapDTO mapToAdd = new DeveloperACBMapDTO();
                mapToAdd.setAcbId(acb.getId());
                mapToAdd.setAcbName(acb.getName());
                mapToAdd.setDeveloperId(developer.getId());
                mapToAdd.setTransparencyAttestation(null);
                developer.getTransparencyAttestationMappings().add(mapToAdd);
            } else {
                map.setAcbName(acb.getName());
                developer.getTransparencyAttestationMappings().add(map);
            }
        }
        return developer;
    }

    @Override
    @Transactional(readOnly = true)
    public DeveloperDTO getById(final Long id) throws EntityRetrievalException {
        return getById(id, false);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(CacheNames.COLLECTIONS_DEVELOPERS)
    public List<DeveloperTransparency> getDeveloperCollection() {
        return developerDao.getAllDevelopersWithTransparencies();
    }

    @Override
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).DEVELOPER, "
            + "T(gov.healthit.chpl.permissions.domains.DeveloperDomainPermissions).UPDATE, #updatedDev)")
    @Transactional(readOnly = false)
    @CacheEvict(value = {
            CacheNames.ALL_DEVELOPERS, CacheNames.ALL_DEVELOPERS_INCLUDING_DELETED, CacheNames.COLLECTIONS_DEVELOPERS,
            CacheNames.GET_DECERTIFIED_DEVELOPERS
    }, allEntries = true)
    public DeveloperDTO update(final DeveloperDTO updatedDev, final boolean doValidation)
            throws EntityRetrievalException, JsonProcessingException, EntityCreationException, MissingReasonException,
            ValidationException {
        if (doValidation) {
            // validation is not done during listing update -> developer ban
            // but should be done at other times
            Set<String> errors = updateValidator.validate(updatedDev);
            if (errors != null && errors.size() > 0) {
                throw new ValidationException(errors, null);
            }
        }

        DeveloperDTO beforeDev = getById(updatedDev.getId());
        DeveloperStatusEventDTO newDevStatus = updatedDev.getStatus();
        DeveloperStatusEventDTO currDevStatus = beforeDev.getStatus();
        if (currDevStatus == null || currDevStatus.getStatus() == null) {
            String msg = msgUtil.getMessage("developer.noStatusFound", beforeDev.getName());
            LOGGER.error(msg);
            throw new EntityCreationException(msg);
        }

        // if any of the statuses (new, old, or any other status in the history)
        // is Under Certification Ban by ONC make sure there is a reason given
        for (DeveloperStatusEventDTO statusEvent : updatedDev.getStatusEvents()) {
            if (statusEvent.getStatus().getStatusName()
                    .equals(DeveloperStatusType.UnderCertificationBanByOnc.toString())
                    && StringUtils.isEmpty(statusEvent.getReason())) {
                throw new MissingReasonException(msgUtil.getMessage("developer.missingReasonForBan",
                        DeveloperStatusType.UnderCertificationBanByOnc.toString()));
            }
        }

        // if the before status is not Active and the user is not ROLE_ADMIN
        // then nothing can be changed
        if (!currDevStatus.getStatus().getStatusName().equals(DeveloperStatusType.Active.toString())
                && !resourcePermissions.isUserRoleAdmin() && !resourcePermissions.isUserRoleOnc()) {
            String msg = msgUtil.getMessage("developer.notActiveNotAdminCantChangeStatus", AuthUtil.getUsername(),
                    beforeDev.getName());
            LOGGER.error(msg);
            throw new EntityCreationException(msg);
        }

        // if the status history has been modified, the user must be role admin
        // except that an acb admin can change to UnderCertificationBanByOnc
        // triggered by listing status update
        boolean devStatusHistoryUpdated = isStatusHistoryUpdated(beforeDev, updatedDev);
        if (devStatusHistoryUpdated
                && newDevStatus.getStatus().getStatusName()
                        .equals(DeveloperStatusType.UnderCertificationBanByOnc.toString())
                && !resourcePermissions.isUserRoleAdmin() && !resourcePermissions.isUserRoleOnc()) {
            String msg = msgUtil.getMessage("developer.statusChangeNotAllowedWithoutAdmin",
                    DeveloperStatusType.UnderCertificationBanByOnc.toString());
            throw new EntityCreationException(msg);
        } else if (devStatusHistoryUpdated
                && !newDevStatus.getStatus().getStatusName()
                        .equals(DeveloperStatusType.UnderCertificationBanByOnc.toString())
                && resourcePermissions.isUserRoleAdmin() && resourcePermissions.isUserRoleOnc()) {
            String msg = msgUtil.getMessage("developer.statusHistoryChangeNotAllowedWithoutAdmin");
            throw new EntityCreationException(msg);
        }

        // determine if the status has been changed in most cases only allowed by ROLE_ADMIN but ROLE_ACB
        // can change it to UnderCertificationBanByOnc
        boolean currentStatusChanged = !currDevStatus.getStatus().getStatusName()
                .equals(newDevStatus.getStatus().getStatusName());
        if (currentStatusChanged
                && !newDevStatus.getStatus().getStatusName()
                        .equals(DeveloperStatusType.UnderCertificationBanByOnc.toString())
                && !resourcePermissions.isUserRoleAdmin() && !resourcePermissions.isUserRoleOnc()) {
            String msg = msgUtil.getMessage("developer.statusChangeNotAllowedWithoutAdmin");
            throw new EntityCreationException(msg);
        } else {
            /*
             * Check to see that the Developer's website is valid.
             */
            if (!StringUtils.isEmpty(updatedDev.getWebsite())) {
                if (!ValidationUtils.isWellFormedUrl(updatedDev.getWebsite())) {
                    String msg = msgUtil.getMessage("developer.websiteIsInvalid");
                    throw new EntityCreationException(msg);
                }
            }

            if (beforeDev.getContact() != null && beforeDev.getContact().getId() != null) {
                updatedDev.getContact().setId(beforeDev.getContact().getId());
            }

            developerDao.update(updatedDev);
            updateStatusHistory(beforeDev, updatedDev);
            createOrUpdateTransparencyMappings(updatedDev);
        }
        DeveloperDTO after = getById(updatedDev.getId());
        activityManager.addActivity(ActivityConcept.DEVELOPER, after.getId(),
                "Developer " + updatedDev.getName() + " was updated.", beforeDev, after);
        return after;
    }

    /**
     * Add or edit a transparency mapping between ACB and Developer.
     * If the current user does not have access to an ACB the mapping
     * will be ignored.
     * @param developer
     */
    private void createOrUpdateTransparencyMappings(final DeveloperDTO developer) {
        List<CertificationBodyDTO> availableAcbs = resourcePermissions.getAllAcbsForCurrentUser();
        if (availableAcbs != null && availableAcbs.size() > 0) {
            for (CertificationBodyDTO acb : availableAcbs) {
                DeveloperACBMapDTO existingMap = developerDao.getTransparencyMapping(developer.getId(), acb.getId());
                if (existingMap == null) {
                    DeveloperACBMapDTO developerMappingToCreate = new DeveloperACBMapDTO();
                    developerMappingToCreate.setAcbId(acb.getId());
                    developerMappingToCreate.setDeveloperId(developer.getId());
                    for (DeveloperACBMapDTO attMap : developer.getTransparencyAttestationMappings()) {
                        if (attMap.getAcbName().equals(acb.getName())) {
                            developerMappingToCreate.setTransparencyAttestation(attMap.getTransparencyAttestation());
                            developerDao.createTransparencyMapping(developerMappingToCreate);
                        }
                    }
                } else {
                    for (DeveloperACBMapDTO attMap : developer.getTransparencyAttestationMappings()) {
                        if (attMap.getAcbName().equals(acb.getName())) {
                            existingMap.setTransparencyAttestation(attMap.getTransparencyAttestation());
                            developerDao.updateTransparencyMapping(existingMap);
                        }
                    }
                }
            }
        }
    }

    private void updateStatusHistory(final DeveloperDTO beforeDev, final DeveloperDTO updatedDev)
            throws EntityRetrievalException, EntityCreationException {
        // update status history
        List<DeveloperStatusEventDTO> statusEventsToAdd = new ArrayList<DeveloperStatusEventDTO>();
        List<DeveloperStatusEventPair> statusEventsToUpdate = new ArrayList<DeveloperStatusEventPair>();
        List<DeveloperStatusEventDTO> statusEventsToRemove = new ArrayList<DeveloperStatusEventDTO>();

        statusEventsToUpdate = DeveloperStatusEventsHelper.getUpdatedEvents(beforeDev.getStatusEvents(),
                updatedDev.getStatusEvents());
        statusEventsToRemove = DeveloperStatusEventsHelper.getRemovedEvents(beforeDev.getStatusEvents(),
                updatedDev.getStatusEvents());
        statusEventsToAdd = DeveloperStatusEventsHelper.getAddedEvents(beforeDev.getStatusEvents(),
                updatedDev.getStatusEvents());

        for (DeveloperStatusEventPair toUpdate : statusEventsToUpdate) {
            boolean hasChanged = false;
            if (!ObjectUtils.equals(toUpdate.getOrig().getStatusDate(), toUpdate.getUpdated().getStatusDate())
                    || !ObjectUtils.equals(toUpdate.getOrig().getStatus().getId(),
                            toUpdate.getUpdated().getStatus().getId())
                    || !ObjectUtils.equals(toUpdate.getOrig().getStatus().getStatusName(),
                            toUpdate.getUpdated().getStatus().getStatusName())
                    || !ObjectUtils.equals(toUpdate.getOrig().getReason(), toUpdate.getUpdated().getReason())) {
                hasChanged = true;
            }

            if (hasChanged) {
                DeveloperStatusEventDTO dseToUpdate = toUpdate.getUpdated();
                dseToUpdate.setDeveloperId(beforeDev.getId());
                developerDao.updateDeveloperStatusEvent(dseToUpdate);
            }
        }

        for (DeveloperStatusEventDTO toAdd : statusEventsToAdd) {
            toAdd.setDeveloperId(beforeDev.getId());
            developerDao.createDeveloperStatusEvent(toAdd);
        }

        for (DeveloperStatusEventDTO toRemove : statusEventsToRemove) {
            developerDao.deleteDeveloperStatusEvent(toRemove);
        }
    }

    @Override
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).DEVELOPER, "
            + "T(gov.healthit.chpl.permissions.domains.DeveloperDomainPermissions).CREATE)")
    @Transactional(readOnly = false)
    @CacheEvict(value = {
            CacheNames.ALL_DEVELOPERS, CacheNames.ALL_DEVELOPERS_INCLUDING_DELETED, CacheNames.COLLECTIONS_DEVELOPERS
    }, allEntries = true)
    public DeveloperDTO create(final DeveloperDTO dto)
            throws EntityRetrievalException, EntityCreationException, JsonProcessingException {

        /*
         * Check to see that the Developer's website is valid.
         */
        if (!StringUtils.isEmpty(dto.getWebsite())) {
            if (!ValidationUtils.isWellFormedUrl(dto.getWebsite())) {
                String msg = msgUtil.getMessage("developer.websiteIsInvalid");
                throw new EntityCreationException(msg);
            }
        }

        DeveloperDTO created = developerDao.create(dto);
        dto.setId(created.getId());
        createOrUpdateTransparencyMappings(dto);
        activityManager.addActivity(ActivityConcept.DEVELOPER, created.getId(),
                "Developer " + created.getName() + " has been created.", null, created);
        return created;
    }

    @Override
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).DEVELOPER, "
            + "T(gov.healthit.chpl.permissions.domains.DeveloperDomainPermissions).MERGE, #developerIdsToMerge)")
    @Transactional(readOnly = false)
    @CacheEvict(value = {
            CacheNames.ALL_DEVELOPERS, CacheNames.ALL_DEVELOPERS_INCLUDING_DELETED, CacheNames.COLLECTIONS_DEVELOPERS,
            CacheNames.GET_DECERTIFIED_DEVELOPERS
    }, allEntries = true)
    public DeveloperDTO merge(final List<Long> developerIdsToMerge, final DeveloperDTO developerToCreate)
            throws EntityRetrievalException, JsonProcessingException, EntityCreationException, ValidationException {

        // merging doesn't require developer address which is why the update validator
        // is getting used here.
        Set<String> errors = updateValidator.validate(developerToCreate);
        if (errors != null && errors.size() > 0) {
            throw new ValidationException(errors, null);
        }

        List<DeveloperDTO> beforeDevelopers = new ArrayList<DeveloperDTO>();
        for (Long developerId : developerIdsToMerge) {
            beforeDevelopers.add(developerDao.getById(developerId));
        }

        // Check to see if the merge will create any duplicate chplProductNumbers
        List<DuplicateChplProdNumber> duplicateChplProdNumbers = getDuplicateChplProductNumbersBasedOnDevMerge(
                developerIdsToMerge, developerToCreate.getDeveloperCode());
        if (duplicateChplProdNumbers.size() != 0) {
            throw new ValidationException(getDuplicateChplProductNumberErrorMessages(duplicateChplProdNumbers), null);
        }

        // check if the transparency attestation for each developer is conflicting
        List<CertificationBodyDTO> allAcbs = acbManager.getAll();
        for (CertificationBodyDTO acb : allAcbs) {
            AttestationType transparencyAttestation = null;
            for (DeveloperDTO dev : beforeDevelopers) {
                DeveloperACBMapDTO taMap = developerDao.getTransparencyMapping(dev.getId(), acb.getId());
                if (taMap != null && !StringUtils.isEmpty(taMap.getTransparencyAttestation())) {
                    AttestationType currAtt = AttestationType.getValue(taMap.getTransparencyAttestation());
                    if (transparencyAttestation == null) {
                        transparencyAttestation = currAtt;
                    } else if (currAtt != transparencyAttestation) {
                        throw new EntityCreationException("Cannot complete merge because " + acb.getName()
                                + " has a conflicting transparency attestation for these developers.");
                    }
                }
            }

            if (transparencyAttestation != null) {
                DeveloperACBMapDTO devMap = new DeveloperACBMapDTO();
                devMap.setAcbId(acb.getId());
                devMap.setAcbName(acb.getName());
                devMap.setTransparencyAttestation(transparencyAttestation.name());
                developerToCreate.getTransparencyAttestationMappings().add(devMap);
            }
        }

        DeveloperDTO createdDeveloper = create(developerToCreate);
        // search for any products assigned to the list of developers passed in
        List<ProductDTO> developerProducts = productManager.getByDevelopers(developerIdsToMerge);
        for (ProductDTO product : developerProducts) {
            // add an item to the ownership history of each product
            ProductOwnerDTO historyToAdd = new ProductOwnerDTO();
            historyToAdd.setProductId(product.getId());
            DeveloperDTO prevOwner = new DeveloperDTO();
            prevOwner.setId(product.getDeveloperId());
            historyToAdd.setDeveloper(prevOwner);
            historyToAdd.setTransferDate(System.currentTimeMillis());
            product.getOwnerHistory().add(historyToAdd);
            // reassign those products to the new developer
            product.setDeveloperId(createdDeveloper.getId());
            productManager.update(product);

        }
        // - mark the passed in developers as deleted
        for (Long developerId : developerIdsToMerge) {
            List<CertificationBodyDTO> availableAcbs = resourcePermissions.getAllAcbsForCurrentUser();
            if (availableAcbs != null && availableAcbs.size() > 0) {
                for (CertificationBodyDTO acb : availableAcbs) {
                    developerDao.deleteTransparencyMapping(developerId, acb.getId());
                }
            }
            developerDao.delete(developerId);
        }

        activityManager.addActivity(
                ActivityConcept.DEVELOPER, createdDeveloper.getId(), "Merged " + developerIdsToMerge.size()
                        + " developers into new developer '" + createdDeveloper.getName() + "'.",
                beforeDevelopers, createdDeveloper);

        return createdDeveloper;
    }

    /**
     * Splits a developer into two. The new developer will have at least one product assigned to it
     * that used to be assigned to the original developer along with the versions and listings
     * associated with those products. At least one product along with its versions and listings
     * will remain assigned to the original developer.
     * Since the developer code is auto-generated in the database, any listing that gets
     * transferred to the new developer will automatically have a unique ID (no other developer
     * can have the same developer code).
     */
    @Override
    @Transactional(rollbackFor = {
            EntityRetrievalException.class, EntityCreationException.class, JsonProcessingException.class,
            AccessDeniedException.class
    })
    @CacheEvict(value = {
            CacheNames.ALL_DEVELOPERS, CacheNames.ALL_DEVELOPERS_INCLUDING_DELETED, CacheNames.COLLECTIONS_DEVELOPERS,
            CacheNames.GET_DECERTIFIED_DEVELOPERS
    }, allEntries = true)
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).DEVELOPER, "
            + "T(gov.healthit.chpl.permissions.domains.DeveloperDomainPermissions).SPLIT, #oldDeveloper)")
    public DeveloperDTO split(final DeveloperDTO oldDeveloper, final DeveloperDTO developerToCreate,
            final List<Long> productIdsToMove) throws ValidationException, AccessDeniedException,
            EntityRetrievalException, EntityCreationException, JsonProcessingException {
        // check developer fields for all valid values
        Set<String> devErrors = creationValidator.validate(developerToCreate);
        if (devErrors != null && devErrors.size() > 0) {
            throw new ValidationException(devErrors, null);
        }

        // create the new developer and log activity
        DeveloperDTO createdDeveloper = create(developerToCreate);

        // re-assign products to the new developer
        // log activity for all listings whose ID will have changed
        Date splitDate = new Date();
        List<CertificationBodyDTO> allowedAcbs = resourcePermissions.getAllAcbsForCurrentUser();
        for (Long productIdToMove : productIdsToMove) {
            List<CertifiedProductDetailsDTO> affectedListings = cpManager.getByProduct(productIdToMove);
            // need to get details for affected listings now before the product is re-assigned
            // so that any listings with a generated new-style CHPL ID have the old developer code
            Map<Long, CertifiedProductSearchDetails> beforeListingDetails = new HashMap<Long, CertifiedProductSearchDetails>();
            for (CertifiedProductDetailsDTO affectedListing : affectedListings) {
                CertifiedProductSearchDetails beforeListing = cpdManager
                        .getCertifiedProductDetails(affectedListing.getId());

                // make sure each listing associated with the new developer
                boolean hasAccessToAcb = false;
                for (CertificationBodyDTO allowedAcb : allowedAcbs) {
                    if (allowedAcb.getId().longValue() == affectedListing.getCertificationBodyId().longValue()) {
                        hasAccessToAcb = true;
                    }
                }
                if (!hasAccessToAcb) {
                    throw new AccessDeniedException(
                            msgUtil.getMessage("acb.accessDenied.listingUpdate", beforeListing.getChplProductNumber(),
                                    beforeListing.getCertifyingBody().get(CertifiedProductSearchDetails.ACB_NAME_KEY)));
                }

                beforeListingDetails.put(beforeListing.getId(), beforeListing);
            }

            // move the product to be owned by the new developer
            ProductDTO productToMove = productManager.getById(productIdToMove);
            productToMove.setDeveloperId(createdDeveloper.getId());
            // add owner history for old developer
            ProductOwnerDTO newOwner = new ProductOwnerDTO();
            newOwner.setProductId(productToMove.getId());
            newOwner.setDeveloper(oldDeveloper);
            newOwner.setTransferDate(splitDate.getTime());
            productToMove.getOwnerHistory().add(newOwner);
            productManager.update(productToMove);

            // get the listing details again - this time they will have the new developer code
            // so the change will show up in activity reports
            for (CertifiedProductDetailsDTO affectedListing : affectedListings) {
                CertifiedProductSearchDetails afterListing = cpdManager
                        .getCertifiedProductDetails(affectedListing.getId());
                CertifiedProductSearchDetails beforeListing = beforeListingDetails.get(afterListing.getId());
                activityManager.addActivity(ActivityConcept.CERTIFIED_PRODUCT, beforeListing.getId(),
                        "Updated certified product " + afterListing.getChplProductNumber() + ".", beforeListing,
                        afterListing);
            }
        }

        DeveloperDTO afterDeveloper = null;
        if (ff4j.check(FeatureList.BETTER_SPLIT)) {
            //the split is complete - log split activity
            //get the original developer object from the db to make sure it's all filled in
            DeveloperDTO origDeveloper = getById(oldDeveloper.getId());
            afterDeveloper = getById(createdDeveloper.getId());
            List<DeveloperDTO> splitDevelopers = new ArrayList<DeveloperDTO>();
            splitDevelopers.add(origDeveloper);
            splitDevelopers.add(afterDeveloper);
            activityManager.addActivity(ActivityConcept.DEVELOPER, afterDeveloper.getId(),
                    "Split developer " + origDeveloper.getName() + " into " + origDeveloper.getName()
                    + " and " + afterDeveloper.getName(),
                    origDeveloper, splitDevelopers);
        } else {
            afterDeveloper = getById(createdDeveloper.getId());
        }
        return afterDeveloper;
    }

    /**
     * Clones a list of DeveloperStatusEventDTO.
     * @param original
     *            - List<DeveloperStatusEventDTO>
     * @return List<DeveloperStatusEventDTO>
     */
    public static List<DeveloperStatusEventDTO> cloneDeveloperStatusEventList(
            final List<DeveloperStatusEventDTO> original) {
        List<DeveloperStatusEventDTO> clone = new ArrayList<DeveloperStatusEventDTO>();
        for (DeveloperStatusEventDTO event : original) {
            clone.add(new DeveloperStatusEventDTO(event));
        }
        return clone;
    }

    private Set<String> getDuplicateChplProductNumberErrorMessages(
            final List<DuplicateChplProdNumber> duplicateChplProdNumbers) {

        Set<String> messages = new HashSet<String>();

        for (DuplicateChplProdNumber dup : duplicateChplProdNumbers) {
            messages.add(msgUtil.getMessage("developer.merge.dupChplProdNbrs", dup.getOrigChplProductNumberA(),
                    dup.getOrigChplProductNumberB()));
        }
        return messages;
    }

    private List<DuplicateChplProdNumber> getDuplicateChplProductNumbersBasedOnDevMerge(final List<Long> developerIds,
            final String newDeveloperCode) {

        // key = new chpl prod nbr, value = orig chpl prod nbr
        HashMap<String, String> newChplProductNumbers = new HashMap<String, String>();

        String newChplProductNumber = "";

        // Hold the list of duplicate chpl prod nbrs {new, origA, origB} where "origA" and "origB" are the
        // original chpl prod nbrs that would be duplicated during merge and "new" is chpl prod nbr that
        // "origA" and "origB" would be updated to
        List<DuplicateChplProdNumber> duplicatedChplProductNumbers = new ArrayList<DuplicateChplProdNumber>();

        for (Long developerId : developerIds) {
            List<CertifiedProductDetailsDTO> certifiedProducts = certifiedProductDAO.findByDeveloperId(developerId);

            for (CertifiedProductDetailsDTO certifiedProduct : certifiedProducts) {
                newChplProductNumber = "";
                if (certifiedProduct.getChplProductNumber().startsWith("CHP")) {
                    newChplProductNumber = certifiedProduct.getChplProductNumber();
                } else {
                    newChplProductNumber = chplProductNumberUtil.getChplProductNumber(certifiedProduct.getYear(),
                            chplProductNumberUtil.parseChplProductNumber(certifiedProduct.getChplProductNumber())
                                    .getAtlCode(),
                            certifiedProduct.getCertificationBodyCode(), newDeveloperCode,
                            certifiedProduct.getProductCode(), certifiedProduct.getVersionCode(),
                            certifiedProduct.getIcsCode(), certifiedProduct.getAdditionalSoftwareCode(),
                            certifiedProduct.getCertifiedDateCode());
                }
                if (newChplProductNumbers.containsKey(newChplProductNumber)) {
                    duplicatedChplProductNumbers
                            .add(new DuplicateChplProdNumber(newChplProductNumbers.get(newChplProductNumber),
                                    certifiedProduct.getChplProductNumber(), newChplProductNumber));
                } else {
                    newChplProductNumbers.put(newChplProductNumber, certifiedProduct.getChplProductNumber());
                }
            }
        }
        return duplicatedChplProductNumbers;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DecertifiedDeveloperResult> getDecertifiedDevelopers() throws EntityRetrievalException {
        List<DecertifiedDeveloperDTODeprecated> dtoList = new ArrayList<DecertifiedDeveloperDTODeprecated>();
        List<DecertifiedDeveloperResult> decertifiedDeveloperResults = new ArrayList<DecertifiedDeveloperResult>();

        dtoList = developerDao.getDecertifiedDevelopers();

        for (DecertifiedDeveloperDTODeprecated dto : dtoList) {
            List<CertificationBody> certifyingBody = new ArrayList<CertificationBody>();
            for (Long oncacbId : dto.getAcbIdList()) {
                CertificationBody cb = new CertificationBody(certificationBodyDao.getById(oncacbId));
                certifyingBody.add(cb);
            }

            DecertifiedDeveloperResult decertifiedDeveloper = new DecertifiedDeveloperResult(
                    developerDao.getById(dto.getDeveloperId()), certifyingBody, dto.getDecertificationDate(),
                    dto.getNumMeaningfulUse(), dto.getEarliestNumMeaningfulUseDate(),
                    dto.getLatestNumMeaningfulUseDate());
            decertifiedDeveloperResults.add(decertifiedDeveloper);
        }
        return decertifiedDeveloperResults;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(CacheNames.GET_DECERTIFIED_DEVELOPERS)
    public List<DecertifiedDeveloper> getDecertifiedDeveloperCollection() {
        List<DecertifiedDeveloperDTO> dtoList = new ArrayList<DecertifiedDeveloperDTO>();
        List<DecertifiedDeveloper> decertifiedDeveloperResults = new ArrayList<DecertifiedDeveloper>();
        dtoList = developerDao.getDecertifiedDeveloperCollection();

        for (DecertifiedDeveloperDTO dto : dtoList) {
            DecertifiedDeveloper decertDev = new DecertifiedDeveloper();
            decertDev.setDeveloperId(dto.getDeveloper().getId());
            decertDev.setDeveloperName(dto.getDeveloper().getName());
            decertDev.setDecertificationDate(dto.getDecertificationDate());
            for (CertificationBodyDTO acb : dto.getAcbs()) {
                decertDev.getAcbNames().add(acb.getName());
            }
            decertifiedDeveloperResults.add(decertDev);
        }
        return decertifiedDeveloperResults;
    }

    private boolean isStatusHistoryUpdated(final DeveloperDTO original, final DeveloperDTO changed) {
        boolean hasChanged = false;
        if ((original.getStatusEvents() != null && changed.getStatusEvents() == null)
                || (original.getStatusEvents() == null && changed.getStatusEvents() != null)
                || (original.getStatusEvents().size() != changed.getStatusEvents().size())) {
            hasChanged = true;
        } else {
            // neither status history is null and they have the same size history arrays
            // so now check for any differences in the values of each
            for (DeveloperStatusEventDTO origStatusHistory : original.getStatusEvents()) {
                boolean foundMatchInChanged = false;
                for (DeveloperStatusEventDTO changedStatusHistory : changed.getStatusEvents()) {
                    if (origStatusHistory.getStatus().getId() != null
                            && changedStatusHistory.getStatus().getId() != null
                            && origStatusHistory.getStatus().getId().equals(changedStatusHistory.getStatus().getId())
                            && origStatusHistory.getStatusDate().getTime() == changedStatusHistory.getStatusDate()
                                    .getTime()) {
                        foundMatchInChanged = true;
                    }
                }
                hasChanged = hasChanged || !foundMatchInChanged;
            }
        }
        return hasChanged;
    }

    private List<DeveloperDTO> addTransparencyMappings(final List<DeveloperDTO> developers) {
        List<DeveloperACBMapDTO> transparencyMaps = developerDao.getAllTransparencyMappings();
        Map<Long, DeveloperDTO> mappedDevelopers = new HashMap<Long, DeveloperDTO>();
        for (DeveloperDTO dev : developers) {
            mappedDevelopers.put(dev.getId(), dev);
        }
        for (DeveloperACBMapDTO map : transparencyMaps) {
            if (map.getAcbId() != null) {
                mappedDevelopers.get(map.getDeveloperId()).getTransparencyAttestationMappings().add(map);
            }
        }
        List<DeveloperDTO> ret = new ArrayList<DeveloperDTO>();
        for (DeveloperDTO dev : mappedDevelopers.values()) {
            ret.add(dev);
        }
        return ret;
    }

    private class DuplicateChplProdNumber {
        private String origChplProductNumberA;
        private String origChplProductNumberB;

        DuplicateChplProdNumber(final String origChplProductNumberA, final String origChplProductNumberB,
                final String newChplProductNumber) {
            this.origChplProductNumberA = origChplProductNumberA;
            this.origChplProductNumberB = origChplProductNumberB;
        }

        public String getOrigChplProductNumberA() {
            return origChplProductNumberA;
        }

        public String getOrigChplProductNumberB() {
            return origChplProductNumberB;
        }

        @Override
        public String toString() {
            return msgUtil.getMessage("developer.merge.dupChplProdNbrs.duplicate", origChplProductNumberA,
                    origChplProductNumberB);
        }
    }
}

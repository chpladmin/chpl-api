package gov.healthit.chpl.manager.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.caching.CacheNames;
import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.DecertifiedDeveloper;
import gov.healthit.chpl.domain.DecertifiedDeveloperResult;
import gov.healthit.chpl.domain.DeveloperTransparency;
import gov.healthit.chpl.domain.PendingCertifiedProductDetails;
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
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.entity.AttestationType;
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
import gov.healthit.chpl.manager.rules.ValidationRule;
import gov.healthit.chpl.manager.rules.developer.DeveloperValidationContext;
import gov.healthit.chpl.manager.rules.developer.DeveloperValidationFactory;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.ValidationUtils;

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
    private ErrorMessageUtil msgUtil;
    private ResourcePermissions resourcePermissions;
    private DeveloperValidationFactory developerValidationFactory;

    /**
     * Autowired constructor for dependency injection.
     *
     * @param developerDao
     * @param productManager
     * @param acbManager
     * @param cpManager
     * @param cpdManager
     * @param certificationBodyDao
     * @param certifiedProductDAO
     * @param chplProductNumberUtil
     * @param activityManager
     * @param msgUtil
     * @param resourcePermissions
     * @param developerValidationFactory
     */
    @Autowired
    public DeveloperManagerImpl(final DeveloperDAO developerDao, final ProductManager productManager,
            final CertificationBodyManager acbManager, final CertifiedProductManager cpManager,
            final CertifiedProductDetailsManager cpdManager, final CertificationBodyDAO certificationBodyDao,
            final CertifiedProductDAO certifiedProductDAO, final ChplProductNumberUtil chplProductNumberUtil,
            final ActivityManager activityManager, final ErrorMessageUtil msgUtil,
            final ResourcePermissions resourcePermissions,
            final DeveloperValidationFactory developerValidationFactory) {
        this.developerDao = developerDao;
        this.productManager = productManager;
        this.acbManager = acbManager;
        this.cpManager = cpManager;
        this.cpdManager = cpdManager;
        this.certificationBodyDao = certificationBodyDao;
        this.certifiedProductDAO = certifiedProductDAO;
        this.chplProductNumberUtil = chplProductNumberUtil;
        this.activityManager = activityManager;
        this.msgUtil = msgUtil;
        this.resourcePermissions = resourcePermissions;
        this.developerValidationFactory = developerValidationFactory;
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
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).DEVELOPER, "
            + "T(gov.healthit.chpl.permissions.domains.DeveloperDomainPermissions).GET_ALL_USERS, #devId)")
    public List<UserDTO> getAllUsersOnDeveloper(final Long devId) throws EntityRetrievalException {
        DeveloperDTO dev = getById(devId);
        return resourcePermissions.getAllUsersOnDeveloper(dev);
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
        DeveloperDTO beforeDev = getById(updatedDev.getId());

        if (doValidation) {
            // validation is not done during listing update -> developer ban
            // but should be done at other times
            Set<String> errors = runUpdateValidations(updatedDev);
            errors.addAll(runChangeValidations(updatedDev, beforeDev));
            if (errors != null && errors.size() > 0) {
                throw new ValidationException(errors);
            }
        }

        if (beforeDev.getContact() != null && beforeDev.getContact().getId() != null) {
            updatedDev.getContact().setId(beforeDev.getContact().getId());
        }
        developerDao.update(updatedDev);
        updateStatusHistory(beforeDev, updatedDev);
        createOrUpdateTransparencyMappings(updatedDev);

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

        // merging doesn't require developer address so runUpdateValidations is
        // used here
        Set<String> errors = runUpdateValidations(developerToCreate);
        if (errors != null && errors.size() > 0) {
            throw new ValidationException(errors);
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
        // check developer fields for all valid values (except transparency attestation)
        Set<String> devErrors = runCreateValidations(developerToCreate);
        if (devErrors != null && devErrors.size() > 0) {
            throw new ValidationException(devErrors);
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

    @Override
    public void validateDeveloperInSystemIfExists(final PendingCertifiedProductDetails pendingCp)
            throws EntityRetrievalException, ValidationException {
        if (!isNewDeveloperCode(pendingCp.getChplProductNumber())) {
            DeveloperDTO systemDeveloperDTO = null;
            if (pendingCp.getDeveloper() != null && pendingCp.getDeveloper().getDeveloperId() != null) {
                systemDeveloperDTO = getById(pendingCp.getDeveloper().getDeveloperId());
            }
            if (systemDeveloperDTO != null) {
                final Object pendingAcbNameObj = pendingCp.getCertifyingBody()
                        .get(CertifiedProductSearchDetails.ACB_NAME_KEY);
                if (pendingAcbNameObj != null && !StringUtils.isEmpty(pendingAcbNameObj.toString())) {
                    Set<String> sysDevErrorMessages = runSystemValidations(systemDeveloperDTO,
                            pendingAcbNameObj.toString());
                    if (!sysDevErrorMessages.isEmpty()) {
                        throw new ValidationException(sysDevErrorMessages);
                    }
                } else {
                    LOGGER.error("Unable to validate system developer as the pending ACB Name is null "
                            + "or its String representation is null or empty");
                    throw new ValidationException(msgUtil.getMessage("system.developer.pendingACBNameNullOrEmpty"));
                }
            } else {
                LOGGER.warn("Skipping system validation due to null pending developer or a null system developer");
            }
        } else {
            LOGGER.info("Skipping system validation due to new developer code '" + NEW_DEVELOPER_CODE + "'");
        }
    }

    private boolean isNewDeveloperCode(final String chplProductNumber) {
        String devCode = chplProductNumberUtil.getDeveloperCode(chplProductNumber);
        return StringUtils.equals(devCode, NEW_DEVELOPER_CODE);
    }

    private Set<String> runUpdateValidations(final DeveloperDTO dto) {
        List<ValidationRule<DeveloperValidationContext>> rules = new ArrayList<ValidationRule<DeveloperValidationContext>>();
        rules.add(developerValidationFactory.getRule(DeveloperValidationFactory.NAME));
        rules.add(developerValidationFactory.getRule(DeveloperValidationFactory.WEBSITE_WELL_FORMED));
        rules.add(developerValidationFactory.getRule(DeveloperValidationFactory.CONTACT));
        rules.add(developerValidationFactory.getRule(DeveloperValidationFactory.STATUS_EVENTS));
        return runValidations(rules, dto);
    }

    private Set<String> runChangeValidations(final DeveloperDTO dto, final DeveloperDTO beforeDev) {
        List<ValidationRule<DeveloperValidationContext>> rules = new ArrayList<ValidationRule<DeveloperValidationContext>>();
        rules.add(developerValidationFactory.getRule(DeveloperValidationFactory.EDIT_TRANSPARENCY_ATTESTATION));
        rules.add(developerValidationFactory.getRule(DeveloperValidationFactory.HAS_STATUS));
        rules.add(developerValidationFactory.getRule(DeveloperValidationFactory.STATUS_MISSING_BAN_REASON));
        rules.add(developerValidationFactory.getRule(DeveloperValidationFactory.PRIOR_STATUS_ACTIVE));
        rules.add(developerValidationFactory.getRule(DeveloperValidationFactory.EDIT_STATUS_HISTORY));
        rules.add(developerValidationFactory.getRule(DeveloperValidationFactory.STATUS_CHANGED));
        return runValidations(rules, dto, null, beforeDev);
    }

    private Set<String> runCreateValidations(final DeveloperDTO dto) {
        List<ValidationRule<DeveloperValidationContext>> rules = new ArrayList<ValidationRule<DeveloperValidationContext>>();
        rules.add(developerValidationFactory.getRule(DeveloperValidationFactory.NAME));
        rules.add(developerValidationFactory.getRule(DeveloperValidationFactory.WEBSITE_WELL_FORMED));
        rules.add(developerValidationFactory.getRule(DeveloperValidationFactory.CONTACT));
        rules.add(developerValidationFactory.getRule(DeveloperValidationFactory.ADDRESS));
        return runValidations(rules, dto);
    }

    private Set<String> runSystemValidations(final DeveloperDTO dto, final String pendingAcbName) {
        List<ValidationRule<DeveloperValidationContext>> rules = new ArrayList<ValidationRule<DeveloperValidationContext>>();
        rules.add(developerValidationFactory.getRule(DeveloperValidationFactory.NAME));
        rules.add(developerValidationFactory.getRule(DeveloperValidationFactory.WEBSITE_REQUIRED));
        rules.add(developerValidationFactory.getRule(DeveloperValidationFactory.CONTACT));
        rules.add(developerValidationFactory.getRule(DeveloperValidationFactory.ADDRESS));
        rules.add(developerValidationFactory.getRule(DeveloperValidationFactory.TRANSPARENCY_ATTESTATION));
        return runValidations(rules, dto, pendingAcbName);
    }

    private Set<String> runValidations(final List<ValidationRule<DeveloperValidationContext>> rules,
            final DeveloperDTO dto) {
        return runValidations(rules, dto, null);
    }

    private Set<String> runValidations(final List<ValidationRule<DeveloperValidationContext>> rules,
            final DeveloperDTO dto, final String pendingAcbName) {
        return runValidations(rules, dto, pendingAcbName, null);
    }

    private Set<String> runValidations(final List<ValidationRule<DeveloperValidationContext>> rules,
            final DeveloperDTO dto, final String pendingAcbName, final DeveloperDTO beforeDev) {
        Set<String> errorMessages = new HashSet<String>();
        DeveloperValidationContext context = new DeveloperValidationContext(dto, msgUtil, pendingAcbName, beforeDev);

        for (ValidationRule<DeveloperValidationContext> rule : rules) {
            if (!rule.isValid(context)) {
                errorMessages.addAll(rule.getMessages());
            }
        }
        return errorMessages;
    }
}

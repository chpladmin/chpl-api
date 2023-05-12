package gov.healthit.chpl.manager;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.quartz.JobDataMap;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.caching.CacheNames;
import gov.healthit.chpl.caching.ListingSearchCacheRefresh;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.developer.search.DeveloperSearchResult;
import gov.healthit.chpl.domain.Address;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.DecertifiedDeveloper;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.DeveloperStatusEvent;
import gov.healthit.chpl.domain.IdNamePair;
import gov.healthit.chpl.domain.Product;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.domain.contact.PointOfContact;
import gov.healthit.chpl.domain.developer.hierarchy.DeveloperTree;
import gov.healthit.chpl.domain.developer.hierarchy.ProductTree;
import gov.healthit.chpl.domain.developer.hierarchy.SimpleListing;
import gov.healthit.chpl.domain.developer.hierarchy.VersionTree;
import gov.healthit.chpl.domain.schedule.ChplJob;
import gov.healthit.chpl.domain.schedule.ChplOneTimeTrigger;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.dto.DeveloperStatusEventPair;
import gov.healthit.chpl.dto.ProductVersionDTO;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.entity.developer.DeveloperStatusType;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.auth.UserManager;
import gov.healthit.chpl.manager.impl.DeveloperStatusEventsHelper;
import gov.healthit.chpl.manager.impl.SecuredManager;
import gov.healthit.chpl.manager.rules.ValidationRule;
import gov.healthit.chpl.manager.rules.developer.DeveloperValidationContext;
import gov.healthit.chpl.manager.rules.developer.DeveloperValidationFactory;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.scheduler.job.SplitDeveloperJob;
import gov.healthit.chpl.scheduler.job.developer.JoinDeveloperJob;
import gov.healthit.chpl.sharedstore.listing.ListingStoreRemove;
import gov.healthit.chpl.sharedstore.listing.RemoveBy;
import gov.healthit.chpl.util.AuthUtil;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import gov.healthit.chpl.util.DateUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;
import lombok.extern.log4j.Log4j2;

@Lazy
@Service
@Log4j2
public class DeveloperManager extends SecuredManager {
    public static final String NEW_DEVELOPER_CODE = "XXXX";

    private DeveloperDAO developerDao;
    private ProductManager productManager;
    private ProductVersionManager versionManager;
    private UserManager userManager;
    private CertificationBodyManager acbManager;
    private CertifiedProductDAO certifiedProductDao;
    private ChplProductNumberUtil chplProductNumberUtil;
    private ActivityManager activityManager;
    private ErrorMessageUtil msgUtil;
    private ResourcePermissions resourcePermissions;
    private DeveloperValidationFactory developerValidationFactory;
    private SchedulerManager schedulerManager;

    @Autowired
    @SuppressWarnings("checkstyle:parameternumber")
    public DeveloperManager(DeveloperDAO developerDao, ProductManager productManager, ProductVersionManager versionManager,
            UserManager userManager, CertificationBodyManager acbManager,
            CertifiedProductDAO certifiedProductDAO, ChplProductNumberUtil chplProductNumberUtil,
            ActivityManager activityManager, ErrorMessageUtil msgUtil, ResourcePermissions resourcePermissions,
            DeveloperValidationFactory developerValidationFactory,
            SchedulerManager schedulerManager) {
        this.developerDao = developerDao;
        this.productManager = productManager;
        this.versionManager = versionManager;
        this.userManager = userManager;
        this.acbManager = acbManager;
        this.certifiedProductDao = certifiedProductDAO;
        this.chplProductNumberUtil = chplProductNumberUtil;
        this.activityManager = activityManager;
        this.msgUtil = msgUtil;
        this.resourcePermissions = resourcePermissions;
        this.developerValidationFactory = developerValidationFactory;
        this.schedulerManager = schedulerManager;
    }

    @Transactional(readOnly = true)
    @Cacheable(CacheNames.ALL_DEVELOPERS)
    public List<Developer> getAll() {
        return developerDao.findAll();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).DEVELOPER, "
            + "T(gov.healthit.chpl.permissions.domains.DeveloperDomainPermissions).GET_ALL_WITH_DELETED)")
    @Cacheable(CacheNames.ALL_DEVELOPERS_INCLUDING_DELETED)
    public List<Developer> getAllIncludingDeleted() {
        return developerDao.findAllIncludingDeleted();
    }

    @Transactional(readOnly = true)
    @Cacheable(CacheNames.COLLECTIONS_DEVELOPERS)
    public List<DeveloperSearchResult> getDeveloperSearchResults() {
        Map<Developer, Set<CertificationBody>> allDevelopersWithAcbs = developerDao.findAllDevelopersWithAcbs();
        return allDevelopersWithAcbs.keySet().stream()
                .map(developer -> convertToSearchResult(developer, allDevelopersWithAcbs.get(developer)))
                .collect(Collectors.toList());
    }

    private DeveloperSearchResult convertToSearchResult(Developer developer, Set<CertificationBody> acbs) {
        return DeveloperSearchResult.builder()
                .id(developer.getId())
                .name(developer.getName())
                .code(developer.getDeveloperCode())
                .address(developer.getAddress())
                .contact(developer.getContact())
                .associatedAcbs(acbs.stream()
                        .map(acb -> IdNamePair.builder()
                                .id(acb.getId())
                                .name(acb.getName())
                                .build())
                        .collect(Collectors.toSet()))
                .status(IdNamePair.builder()
                        .id(developer.getStatus().getId())
                        .name(developer.getStatus().getStatus())
                        .build())
                .decertificationDate(calculateDecertificationDate(developer))
                .build();
    }

    private LocalDate calculateDecertificationDate(Developer developer) {
        if (developer.getStatus().getStatus().equals(DeveloperStatusType.UnderCertificationBanByOnc.getName())) {
            return DateUtil.toLocalDate(developer.getMostRecentStatusEvent().getStatusDate().getTime());
        }
        return null;
    }

    @Transactional(readOnly = true)
    public Developer getById(Long id, boolean allowDeleted) throws EntityRetrievalException {
        Developer developer = developerDao.getById(id, allowDeleted);
        return developer;
    }

    @Transactional(readOnly = true)
    public Developer getById(Long id) throws EntityRetrievalException {
        return getById(id, false);
    }

    public DeveloperTree getHierarchyById(Long id) throws EntityRetrievalException {
        List<CertificationBodyDTO> acbs = acbManager.getAll();
        Developer developer = getById(id);
        List<Product> products = productManager.getByDeveloper(developer.getId());
        List<ProductVersionDTO> versions = versionManager.getByDeveloper(developer.getId());
        List<CertifiedProductDetailsDTO> listings = certifiedProductDao.findListingsByDeveloperId(developer.getId());

        DeveloperTree developerTree = new DeveloperTree(developer);
        products.stream().forEach(product -> {
            developerTree.getProducts().add(new ProductTree(product));
        });

        developerTree.getProducts().stream().forEach(product -> {
            List<ProductVersionDTO> productVersions =
                    versions.stream()
                    .filter(version -> version.getProductId().equals(product.getId()))
                    .collect(Collectors.toList());
            productVersions.stream().forEach(version -> {
                product.getVersions().add(new VersionTree(version));
            });
        });

        developerTree.getProducts().stream().forEach(product -> {
            product.getVersions().stream().forEach(version -> {
                List<SimpleListing> listingsForVersion = listings.stream()
                        .filter(listing -> listing.getVersion().getId().equals(version.getId()))
                        .map(listing -> convertToSimpleListing(listing, acbs))
                        .collect(Collectors.toList());
                    version.getListings().addAll(listingsForVersion);
                });
            });
        return developerTree;
    }

    private SimpleListing convertToSimpleListing(CertifiedProductDetailsDTO listingDto, List<CertificationBodyDTO> acbs) {
        SimpleListing listingLeaf = new SimpleListing();
        Optional<CertificationBodyDTO> listingAcb = acbs.stream()
                .filter(acb -> acb.getId().equals(listingDto.getCertificationBodyId())).findFirst();
        if (listingAcb != null && listingAcb.isPresent()) {
            listingLeaf.setAcb(new CertificationBody(listingAcb.get()));
        }
        listingLeaf.setSurveillanceCount(listingDto.getCountSurveillance());
        listingLeaf.setOpenSurveillanceCount(listingDto.getCountOpenSurveillance());
        listingLeaf.setClosedSurveillanceCount(listingDto.getCountClosedSurveillance());
        listingLeaf.setOpenSurveillanceNonConformityCount(listingDto.getCountOpenNonconformities());
        listingLeaf.setClosedSurveillanceNonConformityCount(listingDto.getCountClosedNonconformities());
        listingLeaf.setCertificationDate(listingDto.getCertificationDate().getTime());
        listingLeaf.setCertificationStatus(listingDto.getCertificationStatusName());
        listingLeaf.setChplProductNumber(listingDto.getChplProductNumber());
        listingLeaf.setCuresUpdate(listingDto.getCuresUpdate());
        listingLeaf.setEdition(listingDto.getYear());
        listingLeaf.setId(listingDto.getId());
        listingLeaf.setLastModifiedDate(listingDto.getLastModifiedDate().getTime());
        return listingLeaf;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).DEVELOPER, "
            + "T(gov.healthit.chpl.permissions.domains.DeveloperDomainPermissions).GET_ALL_USERS, #devId)")
    public List<UserDTO> getAllUsersOnDeveloper(Long devId) throws EntityRetrievalException {
        Developer dev = getById(devId);
        return resourcePermissions.getAllUsersOnDeveloper(dev);
    }

    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).DEVELOPER, "
            + "T(gov.healthit.chpl.permissions.domains.DeveloperDomainPermissions).UPDATE, #updatedDev)")
    @Transactional(readOnly = false)
    @CacheEvict(value = {
            CacheNames.ALL_DEVELOPERS, CacheNames.ALL_DEVELOPERS_INCLUDING_DELETED,
            CacheNames.COLLECTIONS_DEVELOPERS,
            CacheNames.GET_DECERTIFIED_DEVELOPERS, CacheNames.DEVELOPER_NAMES,
            CacheNames.QUESTIONABLE_ACTIVITIES, CacheNames.COLLECTIONS_LISTINGS
    }, allEntries = true)
    @ListingStoreRemove(removeBy = RemoveBy.DEVELOPER_ID, id = "#updatedDev.id")
    @ListingSearchCacheRefresh
    public Developer update(Developer updatedDev, boolean doUpdateValidations)
            throws EntityRetrievalException, JsonProcessingException, EntityCreationException, ValidationException {
        Developer beforeDev = getById(updatedDev.getId());
        normalizeSpaces(updatedDev);

        if (updatedDev.equals(beforeDev)) {
            LOGGER.info("Developer did not change - not saving");
            LOGGER.info(updatedDev.toString());
            return beforeDev;
        }

        Set<String> errors = null;
        if (doUpdateValidations) {
            // update validations are not done during listing update -> developer ban
            // but should be done at other times, with some possible exceptions
            errors = runUpdateValidations(updatedDev, beforeDev);
        }
        if (errors == null) {
            errors = new HashSet<String>();
        }
        errors.addAll(runChangeValidations(updatedDev, beforeDev));
        if (errors.size() > 0) {
            throw new ValidationException(errors);
        }

        if (beforeDev.getContact() != null && beforeDev.getContact().getContactId() != null) {
            updatedDev.getContact().setContactId(beforeDev.getContact().getContactId());
        }
        developerDao.update(updatedDev);
        updateStatusHistory(beforeDev, updatedDev);
        Developer after = getById(updatedDev.getId());
        activityManager.addActivity(ActivityConcept.DEVELOPER, after.getId(),
                "Developer " + updatedDev.getName() + " was updated.", beforeDev, after);
        return after;
    }

    private void updateStatusHistory(Developer beforeDev, Developer updatedDev)
            throws EntityRetrievalException, EntityCreationException {
        // update status history
        List<DeveloperStatusEvent> statusEventsToAdd = new ArrayList<DeveloperStatusEvent>();
        List<DeveloperStatusEventPair> statusEventsToUpdate = new ArrayList<DeveloperStatusEventPair>();
        List<DeveloperStatusEvent> statusEventsToRemove = new ArrayList<DeveloperStatusEvent>();

        statusEventsToUpdate = DeveloperStatusEventsHelper.getUpdatedEvents(beforeDev.getStatusEvents(),
                updatedDev.getStatusEvents());
        statusEventsToRemove = DeveloperStatusEventsHelper.getRemovedEvents(beforeDev.getStatusEvents(),
                updatedDev.getStatusEvents());
        statusEventsToAdd = DeveloperStatusEventsHelper.getAddedEvents(beforeDev.getStatusEvents(),
                updatedDev.getStatusEvents());

        for (DeveloperStatusEventPair toUpdate : statusEventsToUpdate) {
            boolean hasChanged = false;
            if (!Objects.equals(toUpdate.getOrig().getStatusDate(), toUpdate.getUpdated().getStatusDate())
                    || !Objects.equals(toUpdate.getOrig().getStatus().getId(),
                            toUpdate.getUpdated().getStatus().getId())
                    || !Objects.equals(toUpdate.getOrig().getStatus().getStatus(),
                            toUpdate.getUpdated().getStatus().getStatus())
                    || !Objects.equals(toUpdate.getOrig().getReason(), toUpdate.getUpdated().getReason())) {
                hasChanged = true;
            }

            if (hasChanged) {
                DeveloperStatusEvent dseToUpdate = toUpdate.getUpdated();
                dseToUpdate.setDeveloperId(beforeDev.getId());
                developerDao.updateDeveloperStatusEvent(dseToUpdate);
            }
        }

        for (DeveloperStatusEvent toAdd : statusEventsToAdd) {
            toAdd.setDeveloperId(beforeDev.getId());
            developerDao.createDeveloperStatusEvent(toAdd);
        }

        for (DeveloperStatusEvent toRemove : statusEventsToRemove) {
            developerDao.deleteDeveloperStatusEvent(toRemove);
        }
    }

    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).DEVELOPER, "
            + "T(gov.healthit.chpl.permissions.domains.DeveloperDomainPermissions).CREATE)")
    @Transactional(readOnly = false)
    @CacheEvict(value = {
            CacheNames.ALL_DEVELOPERS, CacheNames.ALL_DEVELOPERS_INCLUDING_DELETED,
            CacheNames.COLLECTIONS_DEVELOPERS
    }, allEntries = true)
    public Long create(Developer developer)
            throws EntityCreationException, EntityRetrievalException, JsonProcessingException {
        normalizeSpaces(developer);
        runCreateValidations(developer, null);
        Long developerId = developerDao.create(developer);
        developer.setId(developerId);

        Developer createdDevloperDto = developerDao.getById(developerId);
        activityManager.addActivity(ActivityConcept.DEVELOPER, developerId,
                "Developer " + developer.getName() + " has been created.", null, createdDevloperDto);
        return developerId;
    }

    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).DEVELOPER, "
            + "T(gov.healthit.chpl.permissions.domains.DeveloperDomainPermissions).JOIN)")
    @Transactional(readOnly = false)
    @CacheEvict(value = {
            CacheNames.ALL_DEVELOPERS, CacheNames.ALL_DEVELOPERS_INCLUDING_DELETED,
            CacheNames.COLLECTIONS_DEVELOPERS,
            CacheNames.GET_DECERTIFIED_DEVELOPERS, CacheNames.DEVELOPER_NAMES,
            CacheNames.QUESTIONABLE_ACTIVITIES,
            CacheNames.COLLECTIONS_LISTINGS
    }, allEntries = true)
    public ChplOneTimeTrigger join(Long owningDeveloperId, List<Long> joiningDeveloperIds)
            throws EntityRetrievalException, JsonProcessingException, EntityCreationException,
            SchedulerException, ValidationException {
        List<Developer> beforeDevelopers = new ArrayList<Developer>();
        for (Long developerId : joiningDeveloperIds) {
            beforeDevelopers.add(developerDao.getById(developerId));
        }

        // Check to see if the join will create any duplicate chplProductNumbers
        Developer owningDeveloper = developerDao.getById(owningDeveloperId);
        List<DuplicateChplProdNumber> duplicateChplProdNumbers = getDuplicateChplProductNumbersBasedOnDevJoin(
                joiningDeveloperIds, owningDeveloper.getDeveloperCode());
        if (duplicateChplProdNumbers.size() != 0) {
            throw new ValidationException(getDuplicateChplProductNumberErrorMessages(duplicateChplProdNumbers), null);
        }

        UserDTO jobUser = null;
        try {
            jobUser = userManager.getById(AuthUtil.getCurrentUser().getId());
        } catch (UserRetrievalException ex) {
            LOGGER.error("Could not find user to execute job.");
        }

        ChplOneTimeTrigger joinDevelopersTrigger = new ChplOneTimeTrigger();
        ChplJob joinDevelopersJob = new ChplJob();
        joinDevelopersJob.setName(JoinDeveloperJob.JOB_NAME);
        joinDevelopersJob.setGroup(SchedulerManager.CHPL_BACKGROUND_JOBS_KEY);
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put(JoinDeveloperJob.JOINING_DEVELOPERS, beforeDevelopers);
        jobDataMap.put(JoinDeveloperJob.DEVELOPER_TO_JOIN, owningDeveloper);
        jobDataMap.put(JoinDeveloperJob.USER_KEY, jobUser);
        joinDevelopersJob.setJobDataMap(jobDataMap);
        joinDevelopersTrigger.setJob(joinDevelopersJob);
        joinDevelopersTrigger.setRunDateMillis(System.currentTimeMillis() + SchedulerManager.FIVE_SECONDS_IN_MILLIS);
        joinDevelopersTrigger = schedulerManager.createBackgroundJobTrigger(joinDevelopersTrigger);
        return joinDevelopersTrigger;
    }

    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).DEVELOPER, "
            + "T(gov.healthit.chpl.permissions.domains.DeveloperDomainPermissions).SPLIT, #oldDeveloper)")
    @CacheEvict(value = {
            CacheNames.DEVELOPER_NAMES, CacheNames.QUESTIONABLE_ACTIVITIES, CacheNames.COLLECTIONS_LISTINGS
    }, allEntries = true)
    @ListingSearchCacheRefresh
    @ListingStoreRemove(removeBy = RemoveBy.DEVELOPER_ID, id = "#oldDeveloper.id")
    public ChplOneTimeTrigger split(Developer oldDeveloper, Developer developerToCreate,
            List<Long> productIdsToMove) throws ValidationException, SchedulerException {
        normalizeSpaces(developerToCreate);
        // check developer fields for all valid values
        Set<String> devErrors = runCreateValidations(developerToCreate, null);
        if (devErrors != null && devErrors.size() > 0) {
            throw new ValidationException(devErrors);
        }

        UserDTO jobUser = null;
        try {
            jobUser = userManager.getById(AuthUtil.getCurrentUser().getId());
        } catch (UserRetrievalException ex) {
            LOGGER.error("Could not find user to execute job.");
        }

        ChplOneTimeTrigger splitDeveloperTrigger = new ChplOneTimeTrigger();
        ChplJob splitDeveloperJob = new ChplJob();
        splitDeveloperJob.setName(SplitDeveloperJob.JOB_NAME);
        splitDeveloperJob.setGroup(SchedulerManager.CHPL_BACKGROUND_JOBS_KEY);
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put(SplitDeveloperJob.OLD_DEVELOPER_KEY, oldDeveloper);
        jobDataMap.put(SplitDeveloperJob.NEW_DEVELOPER_KEY, developerToCreate);
        jobDataMap.put(SplitDeveloperJob.PRODUCT_IDS_TO_MOVE_KEY, productIdsToMove);
        jobDataMap.put(SplitDeveloperJob.USER_KEY, jobUser);
        splitDeveloperJob.setJobDataMap(jobDataMap);
        splitDeveloperTrigger.setJob(splitDeveloperJob);
        splitDeveloperTrigger.setRunDateMillis(System.currentTimeMillis() + SchedulerManager.FIVE_SECONDS_IN_MILLIS);
        splitDeveloperTrigger = schedulerManager.createBackgroundJobTrigger(splitDeveloperTrigger);
        return splitDeveloperTrigger;
    }

    private Set<String> getDuplicateChplProductNumberErrorMessages(List<DuplicateChplProdNumber> duplicateChplProdNumbers) {
        Set<String> messages = new HashSet<String>();
        for (DuplicateChplProdNumber dup : duplicateChplProdNumbers) {
            messages.add(msgUtil.getMessage("developer.join.duplicateChplProductNumbers", dup.getOrigChplProductNumberA(),
                    dup.getOrigChplProductNumberB()));
        }
        return messages;
    }

    private List<DuplicateChplProdNumber> getDuplicateChplProductNumbersBasedOnDevJoin(List<Long> developerIds,
            String newDeveloperCode) {

        // key = new chpl prod nbr, value = orig chpl prod nbr
        HashMap<String, String> newChplProductNumbers = new HashMap<String, String>();

        String newChplProductNumber = "";

        // Hold the list of duplicate chpl prod nbrs {new, origA, origB} where "origA" and "origB" are the
        // original chpl prod nbrs that would be duplicated during merge and "new" is chpl prod nbr that
        // "origA" and "origB" would be updated to
        List<DuplicateChplProdNumber> duplicatedChplProductNumbers = new ArrayList<DuplicateChplProdNumber>();

        for (Long developerId : developerIds) {
            List<CertifiedProductDetailsDTO> certifiedProducts = certifiedProductDao.findByDeveloperId(developerId);

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

    @Deprecated
    @Transactional(readOnly = true)
    @Cacheable(CacheNames.GET_DECERTIFIED_DEVELOPERS)
    public List<DecertifiedDeveloper> getDecertifiedDeveloperCollection() {
        return developerDao.getDecertifiedDeveloperCollection();
    }

    private void normalizeSpaces(Developer developer) {
        developer.setName(StringUtils.normalizeSpace(developer.getName()));
        Address address = developer.getAddress();
        if (address != null) {
            address.normalizeSpaces();
        }
        PointOfContact contact = developer.getContact();
        if (contact != null) {
            contact.normalizeSpaces();
        }
    }

    private Set<String> runUpdateValidations(Developer developer, Developer beforeDev) {
        List<ValidationRule<DeveloperValidationContext>> rules = new ArrayList<ValidationRule<DeveloperValidationContext>>();
        rules.add(developerValidationFactory.getRule(DeveloperValidationFactory.NAME));
        rules.add(developerValidationFactory.getRule(DeveloperValidationFactory.WEBSITE_WELL_FORMED));
        rules.add(developerValidationFactory.getRule(DeveloperValidationFactory.CONTACT));
        rules.add(developerValidationFactory.getRule(DeveloperValidationFactory.STATUS_EVENTS));
        return runChangeValidations(rules, developer, beforeDev);
    }

    private Set<String> runChangeValidations(Developer afterDev, Developer beforeDev) {
        List<ValidationRule<DeveloperValidationContext>> rules = new ArrayList<ValidationRule<DeveloperValidationContext>>();
        rules.add(developerValidationFactory.getRule(DeveloperValidationFactory.HAS_STATUS));
        rules.add(developerValidationFactory.getRule(DeveloperValidationFactory.STATUS_MISSING_BAN_REASON));
        rules.add(developerValidationFactory.getRule(DeveloperValidationFactory.PRIOR_STATUS_ACTIVE));
        rules.add(developerValidationFactory.getRule(DeveloperValidationFactory.EDIT_STATUS_HISTORY));
        rules.add(developerValidationFactory.getRule(DeveloperValidationFactory.STATUS_CHANGED));
        return runChangeValidations(rules, afterDev, beforeDev);
    }

    public Set<String> runCreateValidations(Developer developer, List<Developer> beforeDevelopers) {
        List<ValidationRule<DeveloperValidationContext>> rules = new ArrayList<ValidationRule<DeveloperValidationContext>>();
        rules.add(developerValidationFactory.getRule(DeveloperValidationFactory.NAME));
        rules.add(developerValidationFactory.getRule(DeveloperValidationFactory.WEBSITE_WELL_FORMED));
        rules.add(developerValidationFactory.getRule(DeveloperValidationFactory.CONTACT));
        rules.add(developerValidationFactory.getRule(DeveloperValidationFactory.ADDRESS));
        rules.add(developerValidationFactory.getRule(DeveloperValidationFactory.ACTIVE_STATUS));
        return runValidations(rules, developer, beforeDevelopers);
    }

    private Set<String> runChangeValidations(List<ValidationRule<DeveloperValidationContext>> rules,
            Developer developer, Developer beforeDev) {
        Set<String> errorMessages = new HashSet<String>();
        DeveloperValidationContext context = new DeveloperValidationContext(developer, msgUtil, beforeDev);

        for (ValidationRule<DeveloperValidationContext> rule : rules) {
            if (!rule.isValid(context)) {
                errorMessages.addAll(rule.getMessages());
            }
        }
        return errorMessages;
    }

    private Set<String> runValidations(List<ValidationRule<DeveloperValidationContext>> rules,
            Developer developer, List<Developer> beforeDevs) {
        Set<String> errorMessages = new HashSet<String>();
        DeveloperValidationContext context = new DeveloperValidationContext(developer, msgUtil, beforeDevs);

        for (ValidationRule<DeveloperValidationContext> rule : rules) {
            if (!rule.isValid(context)) {
                errorMessages.addAll(rule.getMessages());
            }
        }
        return errorMessages;
    }

    private class DuplicateChplProdNumber {
        private String origChplProductNumberA;
        private String origChplProductNumberB;

        DuplicateChplProdNumber(String origChplProductNumberA, String origChplProductNumberB,
                String newChplProductNumber) {
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

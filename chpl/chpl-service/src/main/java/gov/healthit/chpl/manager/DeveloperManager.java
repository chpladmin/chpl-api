package gov.healthit.chpl.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.ff4j.FF4j;
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
import gov.healthit.chpl.domain.PendingCertifiedProductDetails;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.domain.schedule.ChplJob;
import gov.healthit.chpl.domain.schedule.ChplOneTimeTrigger;
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
import gov.healthit.chpl.dto.TransparencyAttestationDTO;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.entity.AttestationType;
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
import gov.healthit.chpl.util.AuthUtil;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.ValidationUtils;
import lombok.extern.log4j.Log4j2;

@Lazy
@Service
@Log4j2
public class DeveloperManager extends SecuredManager {
    public static final String NEW_DEVELOPER_CODE = "XXXX";

    private DeveloperDAO developerDao;
    private ProductManager productManager;
    private UserManager userManager;
    private CertificationBodyManager acbManager;
    private CertificationBodyDAO certificationBodyDao;
    private CertifiedProductDAO certifiedProductDAO;
    private ChplProductNumberUtil chplProductNumberUtil;
    private ActivityManager activityManager;
    private ErrorMessageUtil msgUtil;
    private ResourcePermissions resourcePermissions;
    private DeveloperValidationFactory developerValidationFactory;
    private ValidationUtils validationUtils;
    private TransparencyAttestationManager transparencyAttestationManager;
    private SchedulerManager schedulerManager;
    private FF4j ff4j;

    @Autowired
    public DeveloperManager(DeveloperDAO developerDao, ProductManager productManager, UserManager userManager,
            CertificationBodyManager acbManager, CertificationBodyDAO certificationBodyDao,
            CertifiedProductDAO certifiedProductDAO, ChplProductNumberUtil chplProductNumberUtil,
            ActivityManager activityManager, ErrorMessageUtil msgUtil, ResourcePermissions resourcePermissions,
            DeveloperValidationFactory developerValidationFactory, ValidationUtils validationUtils,
            TransparencyAttestationManager transparencyAttestationManager, SchedulerManager schedulerManager,
            FF4j ff4j) {
        this.developerDao = developerDao;
        this.productManager = productManager;
        this.userManager = userManager;
        this.acbManager = acbManager;
        this.certificationBodyDao = certificationBodyDao;
        this.certifiedProductDAO = certifiedProductDAO;
        this.chplProductNumberUtil = chplProductNumberUtil;
        this.activityManager = activityManager;
        this.msgUtil = msgUtil;
        this.resourcePermissions = resourcePermissions;
        this.developerValidationFactory = developerValidationFactory;
        this.validationUtils = validationUtils;
        this.transparencyAttestationManager = transparencyAttestationManager;
        this.schedulerManager = schedulerManager;
        this.ff4j = ff4j;
    }

    @Transactional(readOnly = true)
    @Cacheable(CacheNames.ALL_DEVELOPERS)
    public List<DeveloperDTO> getAll() {
        List<DeveloperDTO> allDevelopers = developerDao.findAll();
        List<DeveloperDTO> allDevelopersWithTransparencies = addTransparencyMappings(allDevelopers);
        return allDevelopersWithTransparencies;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).DEVELOPER, "
            + "T(gov.healthit.chpl.permissions.domains.DeveloperDomainPermissions).GET_ALL_WITH_DELETED)")
    @Cacheable(CacheNames.ALL_DEVELOPERS_INCLUDING_DELETED)
    public List<DeveloperDTO> getAllIncludingDeleted() {
        List<DeveloperDTO> allDevelopers = developerDao.findAllIncludingDeleted();
        List<DeveloperDTO> allDevelopersWithTransparencies = addTransparencyMappings(allDevelopers);
        return allDevelopersWithTransparencies;
    }

    @Transactional(readOnly = true)
    public DeveloperDTO getById(Long id, boolean allowDeleted)
            throws EntityRetrievalException {
        DeveloperDTO developer = developerDao.getById(id, allowDeleted);
        List<CertificationBodyDTO> availableAcbs = acbManager.getAll();
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

    @Transactional(readOnly = true)
    public DeveloperDTO getById(Long id) throws EntityRetrievalException {
        return getById(id, false);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).DEVELOPER, "
            + "T(gov.healthit.chpl.permissions.domains.DeveloperDomainPermissions).GET_ALL_USERS, #devId)")
    public List<UserDTO> getAllUsersOnDeveloper(Long devId) throws EntityRetrievalException {
        DeveloperDTO dev = getById(devId);
        return resourcePermissions.getAllUsersOnDeveloper(dev);
    }

    @Transactional(readOnly = true)
    @Cacheable(CacheNames.COLLECTIONS_DEVELOPERS)
    public List<DeveloperTransparency> getDeveloperCollection() {
        return developerDao.getAllDevelopersWithTransparencies();
    }

    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).DEVELOPER, "
            + "T(gov.healthit.chpl.permissions.domains.DeveloperDomainPermissions).UPDATE, #updatedDev)")
    @Transactional(readOnly = false)
    @CacheEvict(value = {
            CacheNames.ALL_DEVELOPERS, CacheNames.ALL_DEVELOPERS_INCLUDING_DELETED, CacheNames.COLLECTIONS_DEVELOPERS,
            CacheNames.GET_DECERTIFIED_DEVELOPERS
    }, allEntries = true)
    public DeveloperDTO update(DeveloperDTO updatedDev, boolean doUpdateValidations)
            throws EntityRetrievalException, JsonProcessingException, EntityCreationException, ValidationException {
        DeveloperDTO beforeDev = getById(updatedDev.getId());

        if (updatedDev.equals(beforeDev)) {
            LOGGER.info("Developer did not change - not saving");
            LOGGER.info(updatedDev.toString());
            return beforeDev;
        }

        Set<String> errors = null;
        if (doUpdateValidations) {
            // update validations are not done during listing update -> developer ban
            // but should be done at other times, with some possible exceptions
            errors = runUpdateValidations(updatedDev);
        }
        if (errors == null) {
            errors = new HashSet<String>();
        }
        errors.addAll(runChangeValidations(updatedDev, beforeDev));
        if (errors.size() > 0) {
            throw new ValidationException(errors);
        }

        if (beforeDev.getContact() != null && beforeDev.getContact().getId() != null) {
            updatedDev.getContact().setId(beforeDev.getContact().getId());
        }
        developerDao.update(updatedDev);
        updateStatusHistory(beforeDev, updatedDev);

        if (ff4j.check(FeatureList.EFFECTIVE_RULE_DATE_PLUS_ONE_WEEK)) {
            if (resourcePermissions.isUserRoleAdmin() || resourcePermissions.isUserRoleOnc()) {
                createOrUpdateTransparencyMappings(updatedDev);
            }
        } else {
            createOrUpdateTransparencyMappings(updatedDev);
        }

        DeveloperDTO after = getById(updatedDev.getId());
        activityManager.addActivity(ActivityConcept.DEVELOPER, after.getId(),
                "Developer " + updatedDev.getName() + " was updated.", beforeDev, after);
        return after;
    }

    private void createOrUpdateTransparencyMappings(DeveloperDTO developer) {
        transparencyAttestationManager.save(developer);
    }

    private void updateStatusHistory(DeveloperDTO beforeDev, DeveloperDTO updatedDev)
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

    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).DEVELOPER, "
            + "T(gov.healthit.chpl.permissions.domains.DeveloperDomainPermissions).CREATE)")
    @Transactional(readOnly = false)
    @CacheEvict(value = {
            CacheNames.ALL_DEVELOPERS, CacheNames.ALL_DEVELOPERS_INCLUDING_DELETED, CacheNames.COLLECTIONS_DEVELOPERS
    }, allEntries = true)
    public DeveloperDTO create(DeveloperDTO dto)
            throws EntityRetrievalException, EntityCreationException, JsonProcessingException {

        /*
         * Check to see that the Developer's website is valid.
         */
        if (!StringUtils.isEmpty(dto.getWebsite())) {
            if (!validationUtils.isWellFormedUrl(dto.getWebsite())) {
                String msg = msgUtil.getMessage("developer.websiteIsInvalid");
                throw new EntityCreationException(msg);
            }
        }

        DeveloperDTO created = developerDao.create(dto);
        dto.setId(created.getId());

        if (ff4j.check(FeatureList.EFFECTIVE_RULE_DATE_PLUS_ONE_WEEK)) {
            if (resourcePermissions.isUserRoleAdmin() || resourcePermissions.isUserRoleOnc()) {
                createOrUpdateTransparencyMappings(dto);
            }
        } else {
            createOrUpdateTransparencyMappings(dto);
        }

        activityManager.addActivity(ActivityConcept.DEVELOPER, created.getId(),
                "Developer " + created.getName() + " has been created.", null, created);
        return created;
    }

    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).DEVELOPER, "
            + "T(gov.healthit.chpl.permissions.domains.DeveloperDomainPermissions).MERGE, #developerIdsToMerge)")
    @Transactional(readOnly = false)
    @CacheEvict(value = {
            CacheNames.ALL_DEVELOPERS, CacheNames.ALL_DEVELOPERS_INCLUDING_DELETED, CacheNames.COLLECTIONS_DEVELOPERS,
            CacheNames.GET_DECERTIFIED_DEVELOPERS
    }, allEntries = true)
    public DeveloperDTO merge(List<Long> developerIdsToMerge, DeveloperDTO developerToCreate)
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
                if (taMap != null
                        && taMap.getTransparencyAttestation() != null
                        && !StringUtils.isEmpty(taMap.getTransparencyAttestation().getTransparencyAttestation())) {
                    AttestationType currAtt = AttestationType.getValue(
                            taMap.getTransparencyAttestation().getTransparencyAttestation());
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
                devMap.setTransparencyAttestation(new TransparencyAttestationDTO(transparencyAttestation.name()));
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

    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).DEVELOPER, "
            + "T(gov.healthit.chpl.permissions.domains.DeveloperDomainPermissions).SPLIT, #oldDeveloper)")
    public ChplOneTimeTrigger split(DeveloperDTO oldDeveloper, DeveloperDTO developerToCreate,
            List<Long> productIdsToMove) throws ValidationException, SchedulerException {
        // check developer fields for all valid values
        Set<String> devErrors = runCreateValidations(developerToCreate);
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
        splitDeveloperTrigger.setRunDateMillis(System.currentTimeMillis() + 5000); //5 secs from now
        splitDeveloperTrigger = schedulerManager.createBackgroundJobTrigger(splitDeveloperTrigger);
        return splitDeveloperTrigger;
    }

    public static List<DeveloperStatusEventDTO> cloneDeveloperStatusEventList(List<DeveloperStatusEventDTO> original) {
        List<DeveloperStatusEventDTO> clone = new ArrayList<DeveloperStatusEventDTO>();
        for (DeveloperStatusEventDTO event : original) {
            clone.add(new DeveloperStatusEventDTO(event));
        }
        return clone;
    }

    private Set<String> getDuplicateChplProductNumberErrorMessages(List<DuplicateChplProdNumber> duplicateChplProdNumbers) {

        Set<String> messages = new HashSet<String>();

        for (DuplicateChplProdNumber dup : duplicateChplProdNumbers) {
            messages.add(msgUtil.getMessage("developer.merge.dupChplProdNbrs", dup.getOrigChplProductNumberA(),
                    dup.getOrigChplProductNumberB()));
        }
        return messages;
    }

    private List<DuplicateChplProdNumber> getDuplicateChplProductNumbersBasedOnDevMerge(List<Long> developerIds,
            String newDeveloperCode) {

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

    public void validateDeveloperInSystemIfExists(PendingCertifiedProductDetails pendingCp)
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
            LOGGER.info("Skipping system validation due to new developer code '" + getNewDeveloperCode() + "'");
        }
    }

    @Deprecated
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

    private List<DeveloperDTO> addTransparencyMappings(List<DeveloperDTO> developers) {
        List<CertificationBodyDTO> availableAcbs = acbManager.getAll();
        List<DeveloperACBMapDTO> transparencyMaps = developerDao.getAllTransparencyMappings();
        Map<Long, DeveloperDTO> mappedDevelopers = new HashMap<Long, DeveloperDTO>();
        for (DeveloperDTO dev : developers) {
            // initialize each developer object with null transparency attestation mappings
            // for every ACB
            for (CertificationBodyDTO acb : availableAcbs) {
                DeveloperACBMapDTO mapToAdd = new DeveloperACBMapDTO();
                mapToAdd.setAcbId(acb.getId());
                mapToAdd.setAcbName(acb.getName());
                mapToAdd.setDeveloperId(dev.getId());
                mapToAdd.setTransparencyAttestation(null);
                dev.getTransparencyAttestationMappings().add(mapToAdd);
            }
            mappedDevelopers.put(dev.getId(), dev);
        }

        // fill in existing values for transparency Maps for acb+developer
        for (DeveloperACBMapDTO transparencyMap : transparencyMaps) {
            if (transparencyMap.getAcbId() != null) {
                DeveloperDTO dev = mappedDevelopers.get(transparencyMap.getDeveloperId());
                List<DeveloperACBMapDTO> devTransparencyMap = dev.getTransparencyAttestationMappings();
                for (DeveloperACBMapDTO acbMapping : devTransparencyMap) {
                    if (acbMapping.getAcbId().equals(transparencyMap.getAcbId())) {
                        acbMapping.setTransparencyAttestation(transparencyMap.getTransparencyAttestation());
                    }
                }
            }
        }

        List<DeveloperDTO> developersWithTransparency = new ArrayList<DeveloperDTO>();
        for (DeveloperDTO dev : mappedDevelopers.values()) {
            developersWithTransparency.add(dev);
        }
        return developersWithTransparency;
    }

    private boolean isNewDeveloperCode(String chplProductNumber) {
        String devCode = chplProductNumberUtil.getDeveloperCode(chplProductNumber);
        return StringUtils.equals(devCode, getNewDeveloperCode());
    }

    private Set<String> runUpdateValidations(DeveloperDTO dto) {
        List<ValidationRule<DeveloperValidationContext>> rules = new ArrayList<ValidationRule<DeveloperValidationContext>>();
        rules.add(developerValidationFactory.getRule(DeveloperValidationFactory.NAME));
        rules.add(developerValidationFactory.getRule(DeveloperValidationFactory.WEBSITE_WELL_FORMED));
        rules.add(developerValidationFactory.getRule(DeveloperValidationFactory.CONTACT));
        rules.add(developerValidationFactory.getRule(DeveloperValidationFactory.STATUS_EVENTS));
        return runValidations(rules, dto);
    }

    private Set<String> runChangeValidations(DeveloperDTO dto, DeveloperDTO beforeDev) {
        List<ValidationRule<DeveloperValidationContext>> rules = new ArrayList<ValidationRule<DeveloperValidationContext>>();
        rules.add(developerValidationFactory.getRule(DeveloperValidationFactory.EDIT_TRANSPARENCY_ATTESTATION));
        rules.add(developerValidationFactory.getRule(DeveloperValidationFactory.HAS_STATUS));
        rules.add(developerValidationFactory.getRule(DeveloperValidationFactory.STATUS_MISSING_BAN_REASON));
        rules.add(developerValidationFactory.getRule(DeveloperValidationFactory.PRIOR_STATUS_ACTIVE));
        rules.add(developerValidationFactory.getRule(DeveloperValidationFactory.EDIT_STATUS_HISTORY));
        rules.add(developerValidationFactory.getRule(DeveloperValidationFactory.STATUS_CHANGED));
        return runValidations(rules, dto, null, beforeDev);
    }

    private Set<String> runCreateValidations(DeveloperDTO dto) {
        List<ValidationRule<DeveloperValidationContext>> rules = new ArrayList<ValidationRule<DeveloperValidationContext>>();
        rules.add(developerValidationFactory.getRule(DeveloperValidationFactory.NAME));
        rules.add(developerValidationFactory.getRule(DeveloperValidationFactory.WEBSITE_WELL_FORMED));
        rules.add(developerValidationFactory.getRule(DeveloperValidationFactory.CONTACT));
        rules.add(developerValidationFactory.getRule(DeveloperValidationFactory.ADDRESS));
        rules.add(developerValidationFactory.getRule(DeveloperValidationFactory.ACTIVE_STATUS));
        return runValidations(rules, dto);
    }

    private Set<String> runSystemValidations(DeveloperDTO dto, String pendingAcbName) {
        List<ValidationRule<DeveloperValidationContext>> rules = new ArrayList<ValidationRule<DeveloperValidationContext>>();
        rules.add(developerValidationFactory.getRule(DeveloperValidationFactory.NAME));
        rules.add(developerValidationFactory.getRule(DeveloperValidationFactory.WEBSITE_REQUIRED));
        rules.add(developerValidationFactory.getRule(DeveloperValidationFactory.CONTACT));
        rules.add(developerValidationFactory.getRule(DeveloperValidationFactory.ADDRESS));
        rules.add(developerValidationFactory.getRule(DeveloperValidationFactory.TRANSPARENCY_ATTESTATION));
        return runValidations(rules, dto, pendingAcbName);
    }

    private Set<String> runValidations(List<ValidationRule<DeveloperValidationContext>> rules,
            DeveloperDTO dto) {
        return runValidations(rules, dto, null);
    }

    private Set<String> runValidations(List<ValidationRule<DeveloperValidationContext>> rules,
            DeveloperDTO dto, String pendingAcbName) {
        return runValidations(rules, dto, pendingAcbName, null);
    }

    private Set<String> runValidations(List<ValidationRule<DeveloperValidationContext>> rules,
            DeveloperDTO dto, String pendingAcbName, DeveloperDTO beforeDev) {
        Set<String> errorMessages = new HashSet<String>();
        DeveloperValidationContext context = new DeveloperValidationContext(dto, msgUtil, pendingAcbName, beforeDev);

        for (ValidationRule<DeveloperValidationContext> rule : rules) {
            if (!rule.isValid(context)) {
                errorMessages.addAll(rule.getMessages());
            }
        }
        return errorMessages;
    }

    public static String getNewDeveloperCode() {
        return NEW_DEVELOPER_CODE;
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

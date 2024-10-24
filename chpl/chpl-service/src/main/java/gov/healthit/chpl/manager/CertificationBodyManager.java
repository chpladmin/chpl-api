package gov.healthit.chpl.manager;

import java.time.LocalDate;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.caching.CacheNames;
import gov.healthit.chpl.caching.ListingSearchCacheRefresh;
import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.domain.auth.User;
import gov.healthit.chpl.exception.ActivityException;
import gov.healthit.chpl.exception.EmailNotSentException;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.impl.SecuredManager;
import gov.healthit.chpl.permissions.ResourcePermissionsFactory;
import gov.healthit.chpl.sharedstore.listing.ListingStoreRemove;
import gov.healthit.chpl.sharedstore.listing.RemoveBy;
import gov.healthit.chpl.util.AuthUtil;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service("certificationBodyManager")
public class CertificationBodyManager extends SecuredManager {
    private CertificationBodyDAO certificationBodyDao;
    private ActivityManager activityManager;
    private SchedulerManager schedulerManager;
    private ResourcePermissionsFactory resourcePermissionsFactory;

    @Autowired
    public CertificationBodyManager(CertificationBodyDAO certificationBodyDao,
            ActivityManager activityManager, @Lazy SchedulerManager schedulerManager,
            ResourcePermissionsFactory resourcePermissionsFactory) {
        this.certificationBodyDao = certificationBodyDao;
        this.activityManager = activityManager;
        this.schedulerManager = schedulerManager;
        this.resourcePermissionsFactory = resourcePermissionsFactory;
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).CERTIFICATION_BODY, "
            + "T(gov.healthit.chpl.permissions.domains.CertificationBodyDomainPermissions).CREATE)")
    public CertificationBody create(CertificationBody acb) throws EntityCreationException, EntityRetrievalException, ActivityException {
        // assign a code
        String maxCode = certificationBodyDao.getMaxCode();
        int maxCodeValue = Integer.parseInt(maxCode);
        int nextCodeValue = maxCodeValue + 1;

        String nextAcbCode = "";
        if (nextCodeValue < 10) {
            nextAcbCode = "0" + nextCodeValue;
        } else if (nextCodeValue > 99) {
            throw new EntityCreationException(
                    "Cannot create a 2-digit ACB code since there are more than 99 ACBs in the system.");
        } else {
            nextAcbCode = nextCodeValue + "";
        }
        acb.setAcbCode(nextAcbCode);
        acb.setRetired(false);

        // Create the ACB itself
        CertificationBody result = certificationBodyDao.create(acb);

        LOGGER.debug("Created acb " + result + " and granted admin permission to recipient "
                + gov.healthit.chpl.util.AuthUtil.getUsername());

        String activityMsg = "Created Certification Body " + result.getName();

        activityManager.addActivity(ActivityConcept.CERTIFICATION_BODY, result.getId(), activityMsg,
                null, result);

        return result;
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).CERTIFICATION_BODY, "
            + "T(gov.healthit.chpl.permissions.domains.CertificationBodyDomainPermissions).UPDATE, #acb)")
    @CacheEvict(value = {
            CacheNames.GET_DECERTIFIED_DEVELOPERS,
            CacheNames.COLLECTIONS_DEVELOPERS,
            CacheNames.COLLECTIONS_LISTINGS,
            CacheNames.COMPLAINTS
    }, allEntries = true)
    @ListingStoreRemove(removeBy = RemoveBy.ACB_ID, id = "#acb.id")
    @ListingSearchCacheRefresh
    // no other caches have ACB data so we do not need to clear all
    public CertificationBody update(CertificationBody acbToUpdate) throws EntityRetrievalException, SchedulerException, ValidationException, ActivityException, InvalidArgumentsException {
        // Get the ACB as it is currently in the database to find out if
        // the retired flag was changed.
        // Retirement and un-retirement is done as a separate manager action
        // because security is different from normal ACB updates - only admins are
        // allowed whereas an ACB admin can update other info

        CertificationBody existingAcb = resourcePermissionsFactory.get().getAcbIfPermissionById(acbToUpdate.getId());
        if (acbToUpdate.isRetired()) {
            // we are retiring this ACB - no other updates can happen
            existingAcb.setRetirementDay(acbToUpdate.getRetirementDay());
            existingAcb.setRetired(true);
            retire(existingAcb);
        } else {
            if (existingAcb.isRetired()) {
                // unretire the ACB
                unretire(acbToUpdate.getId());
            }

            if (StringUtils.isEmpty(acbToUpdate.getWebsite())) {
                throw new InvalidArgumentsException("A website is required to update the certification body");
            }
            if (acbToUpdate.getAddress() == null) {
                throw new InvalidArgumentsException("An address is required to update the certification body");
            }
            CertificationBody result = null;
            CertificationBody toUpdate = certificationBodyDao.getById(acbToUpdate.getId());
            result = certificationBodyDao.update(acbToUpdate);
            if (!StringUtils.equals(acbToUpdate.getName(), toUpdate.getName())) {
                schedulerManager.changeAcbName(toUpdate.getName(), acbToUpdate.getName());
            }

            String activityMsg = "Updated acb " + acbToUpdate.getName();
            activityManager.addActivity(ActivityConcept.CERTIFICATION_BODY, result.getId(), activityMsg,
                    toUpdate, result);

        }
        return getById(acbToUpdate.getId());
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).CERTIFICATION_BODY, "
            + "T(gov.healthit.chpl.permissions.domains.CertificationBodyDomainPermissions).RETIRE)")
    @CacheEvict(value = {
            CacheNames.COLLECTIONS_LISTINGS
    }, allEntries = true)
    @ListingStoreRemove(removeBy = RemoveBy.ACB_ID, id = "#acb.id")
    @ListingSearchCacheRefresh
    public CertificationBody retire(CertificationBody acb) throws EntityRetrievalException, SchedulerException, ValidationException, ActivityException {
        if (acb.getRetirementDay() == null || LocalDate.now().isBefore(acb.getRetirementDay())) {
            throw new IllegalArgumentException("Retirement date is required and must be before \"now\".");
        }
        CertificationBody beforeAcb = certificationBodyDao.getById(acb.getId());
        CertificationBody result = certificationBodyDao.update(acb);
        try {
            schedulerManager.retireAcb(beforeAcb.getName());
        } catch (EmailNotSentException ex) {
            LOGGER.catching(ex);
        }

        String activityMsg = "Retired acb " + acb.getName();
        activityManager.addActivity(ActivityConcept.CERTIFICATION_BODY, result.getId(), activityMsg,
                beforeAcb, result);
        return result;
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).CERTIFICATION_BODY, "
            + "T(gov.healthit.chpl.permissions.domains.CertificationBodyDomainPermissions).UNRETIRE)")
    @CacheEvict(value = {
            CacheNames.COLLECTIONS_LISTINGS
    }, allEntries = true)
    @ListingSearchCacheRefresh
    @ListingStoreRemove(removeBy = RemoveBy.ACB_ID, id = "#acbId")
    public CertificationBody unretire(Long acbId) throws EntityRetrievalException, ActivityException {
        CertificationBody beforeAcb = certificationBodyDao.getById(acbId);
        CertificationBody toUnretire = certificationBodyDao.getById(acbId);
        toUnretire.setRetired(false);
        toUnretire.setRetirementDay(null);
        CertificationBody result = certificationBodyDao.update(toUnretire);

        String activityMsg = "Unretired acb " + toUnretire.getName();
        activityManager.addActivity(ActivityConcept.CERTIFICATION_BODY, result.getId(), activityMsg,
                beforeAcb, result);
        return result;
    }

    @Transactional(readOnly = true)
    public List<CertificationBody> getAll() {
        return certificationBodyDao.findAll();
    }

    @Transactional(readOnly = true)
    public List<CertificationBody> getAllEditable() {
        if (AuthUtil.getCurrentUser() != null) {
            return resourcePermissionsFactory.get().getAllAcbsForCurrentUser();
        } else {
            return List.of();
        }
    }

    @Transactional(readOnly = true)
    public List<CertificationBody> getAllActive() {
        return certificationBodyDao.findAllActive();
    }

    @Transactional(readOnly = true)
    public CertificationBody getById(Long id) throws EntityRetrievalException {
        return certificationBodyDao.getById(id);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).CERTIFICATION_BODY, "
            + "T(gov.healthit.chpl.permissions.domains.CertificationBodyDomainPermissions).GET_USERS, #acbId)")
    public List<User> getUsers(Long acbId) throws InvalidArgumentsException, EntityRetrievalException {
        CertificationBody acb = resourcePermissionsFactory.get().getAcbIfPermissionById(acbId);
        if (acb == null) {
            throw new InvalidArgumentsException("Could not find the ACB specified.");
        }

        return resourcePermissionsFactory.get().getAllUsersOnAcb(acb);
    }


    public void setCertificationBodyDAO(final CertificationBodyDAO acbDAO) {
        this.certificationBodyDao = acbDAO;
    }

}

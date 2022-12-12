package gov.healthit.chpl.manager;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.caching.CacheNames;
import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.exception.EmailNotSentException;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.impl.SecuredManager;
import gov.healthit.chpl.manager.impl.UpdateCertifiedBodyException;
import gov.healthit.chpl.sharedstore.listing.ListingStoreRemove;
import gov.healthit.chpl.sharedstore.listing.RemoveBy;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service("certificationBodyManager")
public class CertificationBodyManager extends SecuredManager {
    private CertificationBodyDAO certificationBodyDao;
    private ActivityManager activityManager;
    private SchedulerManager schedulerManager;

    @Autowired
    public CertificationBodyManager(CertificationBodyDAO certificationBodyDao,
            ActivityManager activityManager, @Lazy SchedulerManager schedulerManager) {
        this.certificationBodyDao = certificationBodyDao;
        this.activityManager = activityManager;
        this.schedulerManager = schedulerManager;
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).CERTIFICATION_BODY, "
            + "T(gov.healthit.chpl.permissions.domains.CertificationBodyDomainPermissions).CREATE)")
    public CertificationBodyDTO create(CertificationBodyDTO acb)
            throws UserRetrievalException, EntityCreationException, EntityRetrievalException, JsonProcessingException {
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
        CertificationBodyDTO result = certificationBodyDao.create(acb);

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
            CacheNames.COLLECTIONS_SEARCH,
            CacheNames.COMPLAINTS
    }, allEntries = true)
    @ListingStoreRemove(removeBy = RemoveBy.ACB_ID, id = "#acb.id")
    // no other caches have ACB data so we do not need to clear all
    public CertificationBodyDTO update(CertificationBodyDTO acb)
            throws EntityRetrievalException, JsonProcessingException, EntityCreationException,
            UpdateCertifiedBodyException, SchedulerException, ValidationException {

        CertificationBodyDTO result = null;
        CertificationBodyDTO toUpdate = certificationBodyDao.getById(acb.getId());
        result = certificationBodyDao.update(acb);
        if (!StringUtils.equals(acb.getName(), toUpdate.getName())) {
            schedulerManager.changeAcbName(toUpdate.getName(), acb.getName());
        }

        String activityMsg = "Updated acb " + acb.getName();
        activityManager.addActivity(ActivityConcept.CERTIFICATION_BODY, result.getId(), activityMsg,
                toUpdate, result);
        return result;
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).CERTIFICATION_BODY, "
            + "T(gov.healthit.chpl.permissions.domains.CertificationBodyDomainPermissions).RETIRE)")
    @CacheEvict(value = {
            CacheNames.COLLECTIONS_LISTINGS, CacheNames.COLLECTIONS_SEARCH
    }, allEntries = true)
    @ListingStoreRemove(removeBy = RemoveBy.ACB_ID, id = "#acb.id")
    public CertificationBodyDTO retire(CertificationBodyDTO acb)
            throws EntityRetrievalException, JsonProcessingException, EntityCreationException, IllegalArgumentException,
            SchedulerException, ValidationException {
        Date now = new Date();
        if (acb.getRetirementDate() == null || now.before(acb.getRetirementDate())) {
            throw new IllegalArgumentException("Retirement date is required and must be before \"now\".");
        }
        CertificationBodyDTO beforeAcb = certificationBodyDao.getById(acb.getId());
        CertificationBodyDTO result = certificationBodyDao.update(acb);
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
            CacheNames.COLLECTIONS_LISTINGS, CacheNames.COLLECTIONS_SEARCH
    }, allEntries = true)
    @ListingStoreRemove(removeBy = RemoveBy.ACB_ID, id = "#acbId")
    public CertificationBodyDTO unretire(Long acbId) throws EntityRetrievalException, JsonProcessingException,
            EntityCreationException, UpdateCertifiedBodyException {
        CertificationBodyDTO beforeAcb = certificationBodyDao.getById(acbId);
        CertificationBodyDTO toUnretire = certificationBodyDao.getById(acbId);
        toUnretire.setRetired(false);
        toUnretire.setRetirementDate(null);
        CertificationBodyDTO result = certificationBodyDao.update(toUnretire);

        String activityMsg = "Unretired acb " + toUnretire.getName();
        activityManager.addActivity(ActivityConcept.CERTIFICATION_BODY, result.getId(), activityMsg,
                beforeAcb, result);
        return result;
    }

    @Transactional(readOnly = true)
    public List<CertificationBodyDTO> getAll() {
        return certificationBodyDao.findAll();
    }

    @Transactional(readOnly = true)
    public List<CertificationBodyDTO> getAllActive() {
        return certificationBodyDao.findAllActive();
    }

    @Transactional(readOnly = true)
    public CertificationBodyDTO getById(Long id) throws EntityRetrievalException {
        return certificationBodyDao.getById(id);
    }

    public void setCertificationBodyDAO(final CertificationBodyDAO acbDAO) {
        this.certificationBodyDao = acbDAO;
    }

}

package gov.healthit.chpl.manager.impl;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.auth.user.UserRetrievalException;
import gov.healthit.chpl.caching.CacheNames;
import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.ActivityManager;
import gov.healthit.chpl.manager.CertificationBodyManager;
import gov.healthit.chpl.manager.SchedulerManager;
import gov.healthit.chpl.manager.UserPermissionsManager;

/**
 * Business logic for accessing and updating ACBs.
 * 
 * @author kekey
 *
 */
@Service("certificationBodyManager")
public class CertificationBodyManagerImpl extends SecuredManager implements CertificationBodyManager {
    private static final Logger LOGGER = LogManager.getLogger(CertificationBodyManagerImpl.class);

    private CertificationBodyDAO certificationBodyDao;
    private ActivityManager activityManager;
    private SchedulerManager schedulerManager;
    private UserPermissionsManager userPermissionsManager;

    @Autowired
    public CertificationBodyManagerImpl(final CertificationBodyDAO certificationBodyDao,
            final ActivityManager activityManager, @Lazy final SchedulerManager schedulerManager,
            final UserPermissionsManager userPermissionsManager) {
        this.certificationBodyDao = certificationBodyDao;
        this.activityManager = activityManager;
        this.schedulerManager = schedulerManager;
        this.userPermissionsManager = userPermissionsManager;
    }

    @Override
    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).CERTIFICATION_BODY, "
            + "T(gov.healthit.chpl.permissions.domains.CertificationBodyDomainPermissions).CREATE)")
    public CertificationBodyDTO create(final CertificationBodyDTO acb)
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

        // Grant the current principal administrative permission to the ACB
        userPermissionsManager.addAcbPermission(result, Util.getCurrentUser().getId());

        LOGGER.debug("Created acb " + result + " and granted admin permission to recipient "
                + gov.healthit.chpl.auth.Util.getUsername());

        String activityMsg = "Created Certification Body " + result.getName();

        activityManager.addActivity(ActivityConcept.CERTIFICATION_BODY, result.getId(), activityMsg,
                null, result);

        return result;
    }

    @Override
    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).CERTIFICATION_BODY, "
            + "T(gov.healthit.chpl.permissions.domains.CertificationBodyDomainPermissions).UPDATE, #acb)")
    @CacheEvict(value = {
            CacheNames.COLLECTIONS_DEVELOPERS, CacheNames.GET_DECERTIFIED_DEVELOPERS
    }, allEntries = true)
    // listings collection is not evicted here because it's pre-fetched and handled in a listener
    // no other caches have ACB data so we do not need to clear all
    public CertificationBodyDTO update(final CertificationBodyDTO acb)
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

    @Override
    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).CERTIFICATION_BODY, "
            + "T(gov.healthit.chpl.permissions.domains.CertificationBodyDomainPermissions).RETIRE)")
    public CertificationBodyDTO retire(final CertificationBodyDTO acb)
            throws EntityRetrievalException, JsonProcessingException, EntityCreationException, IllegalArgumentException,
            SchedulerException, ValidationException {
        Date now = new Date();
        if (acb.getRetirementDate() == null || now.before(acb.getRetirementDate())) {
            throw new IllegalArgumentException("Retirement date is required and must be before \"now\".");
        }
        CertificationBodyDTO beforeAcb = certificationBodyDao.getById(acb.getId());
        CertificationBodyDTO result = certificationBodyDao.update(acb);
        schedulerManager.retireAcb(beforeAcb.getName());

        String activityMsg = "Retired acb " + acb.getName();
        activityManager.addActivity(ActivityConcept.CERTIFICATION_BODY, result.getId(), activityMsg,
                beforeAcb, result);
        return result;
    }

    @Override
    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).CERTIFICATION_BODY, "
            + "T(gov.healthit.chpl.permissions.domains.CertificationBodyDomainPermissions).UNRETIRE)")
    public CertificationBodyDTO unretire(final Long acbId) throws EntityRetrievalException, JsonProcessingException,
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

    @Override
    @Transactional(readOnly = true)
    public List<CertificationBodyDTO> getAll() {
        return certificationBodyDao.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CertificationBodyDTO> getAllActive() {
        return certificationBodyDao.findAllActive();
    }

    @Override
    @Transactional(readOnly = true)
    public CertificationBodyDTO getById(final Long id) throws EntityRetrievalException {
        return certificationBodyDao.getById(id);
    }

    public void setCertificationBodyDAO(final CertificationBodyDAO acbDAO) {
        this.certificationBodyDao = acbDAO;
    }

}

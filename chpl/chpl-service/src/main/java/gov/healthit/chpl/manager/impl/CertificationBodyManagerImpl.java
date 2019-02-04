package gov.healthit.chpl.manager.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.model.AccessControlEntry;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.Sid;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.auth.dao.UserDAO;
import gov.healthit.chpl.auth.dto.UserDTO;
import gov.healthit.chpl.auth.user.UserRetrievalException;
import gov.healthit.chpl.caching.CacheNames;
import gov.healthit.chpl.caching.ClearAllCaches;
import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.domain.concept.ActivityConcept;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.ActivityManager;
import gov.healthit.chpl.manager.CertificationBodyManager;
import gov.healthit.chpl.manager.SchedulerManager;
import gov.healthit.chpl.manager.UserPermissionsManager;
import gov.healthit.chpl.permissions.Permissions;

/**
 * Business logic for accessing and updating ACBs.
 * 
 * @author kekey
 *
 */
@Service("certificationBodyManager")
public class CertificationBodyManagerImpl extends ApplicationObjectSupport implements CertificationBodyManager {
    private static final Logger LOGGER = LogManager.getLogger(CertificationBodyManagerImpl.class);

    private CertificationBodyDAO certificationBodyDao;
    private UserDAO userDao;
    private MutableAclService mutableAclService;
    private ActivityManager activityManager;
    private SchedulerManager schedulerManager;

    @Autowired
    public CertificationBodyManagerImpl(final CertificationBodyDAO certificationBodyDao, final UserDAO userDao,
            final MutableAclService mutableAclService, final ActivityManager activityManager,
            @Lazy final SchedulerManager schedulerManager) {
        this.certificationBodyDao = certificationBodyDao;
        this.userDao = userDao;
        this.mutableAclService = mutableAclService;
        this.activityManager = activityManager;
        this.schedulerManager = schedulerManager;
    }

    @Autowired
    private UserPermissionsManager userPermissionsManager;

    @Autowired
    private Permissions permissions;

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).CERTIFICATION_BODY, "
            + "T(gov.healthit.chpl.permissions.domains.CertificationBodyDomainPermissions).CREATE)")
    @ClearAllCaches
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
        userPermissionsManager.addPermission(result, Util.getCurrentUser().getId());

        LOGGER.debug("Created acb " + result + " and granted admin permission to recipient "
                + gov.healthit.chpl.auth.Util.getUsername());

        String activityMsg = "Created Certification Body " + result.getName();

        activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_CERTIFICATION_BODY, result.getId(), activityMsg,
                null, result);

        return result;
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).CERTIFICATION_BODY, "
            + "T(gov.healthit.chpl.permissions.domains.CertificationBodyDomainPermissions).UPDATE, #acb)")
    @ClearAllCaches
    public CertificationBodyDTO update(final CertificationBodyDTO acb) throws EntityRetrievalException,
            JsonProcessingException, EntityCreationException, UpdateCertifiedBodyException {

        CertificationBodyDTO result = null;
        CertificationBodyDTO toUpdate = certificationBodyDao.getById(acb.getId());
        result = certificationBodyDao.update(acb);

        String activityMsg = "Updated acb " + acb.getName();
        activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_CERTIFICATION_BODY, result.getId(), activityMsg,
                toUpdate, result);
        return result;
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).CERTIFICATION_BODY, "
            + "T(gov.healthit.chpl.permissions.domains.CertificationBodyDomainPermissions).RETIRE)")
    @CacheEvict(CacheNames.CERT_BODY_NAMES)
    public CertificationBodyDTO retire(final CertificationBodyDTO acb)
            throws EntityRetrievalException, JsonProcessingException, EntityCreationException, IllegalArgumentException,
            SchedulerException, ValidationException {
        Date now = new Date();
        if (acb.getRetirementDate() == null || now.before(acb.getRetirementDate())) {
            throw new IllegalArgumentException("Retirement date is required and must be before \"now\".");
        }
        CertificationBodyDTO result = null;
        CertificationBodyDTO toUpdate = certificationBodyDao.getById(acb.getId());
        toUpdate.setRetired(true);
        toUpdate.setRetirementDate(acb.getRetirementDate());
        result = certificationBodyDao.update(toUpdate);
        schedulerManager.retireAcb(toUpdate.getName());

        String activityMsg = "Retired acb " + toUpdate.getName();
        activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_CERTIFICATION_BODY, result.getId(), activityMsg,
                toUpdate, result);
        return result;
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).CERTIFICATION_BODY, "
            + "T(gov.healthit.chpl.permissions.domains.CertificationBodyDomainPermissions).UNRETIRE)")
    @CacheEvict(CacheNames.CERT_BODY_NAMES)
    public CertificationBodyDTO unretire(final Long acbId) throws EntityRetrievalException, JsonProcessingException,
            EntityCreationException, UpdateCertifiedBodyException {
        CertificationBodyDTO result = null;
        CertificationBodyDTO toUpdate = certificationBodyDao.getById(acbId);
        toUpdate.setRetired(false);
        toUpdate.setRetirementDate(null);
        result = certificationBodyDao.update(toUpdate);

        String activityMsg = "Unretired acb " + toUpdate.getName();
        activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_CERTIFICATION_BODY, result.getId(), activityMsg,
                toUpdate, result);
        return result;
    }

    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).CERTIFICATION_BODY, "
            + "T(gov.healthit.chpl.permissions.domains.CertificationBodyDomainPermissions).PERMISSIONS_BY_USER, #acb)")
    public List<Permission> getPermissionsForUser(final CertificationBodyDTO acb, final Sid recipient) {
        ObjectIdentity oid = new ObjectIdentityImpl(CertificationBodyDTO.class, acb.getId());
        MutableAcl acl = (MutableAcl) mutableAclService.readAclById(oid);

        List<Permission> permissions = new ArrayList<Permission>();
        List<AccessControlEntry> entries = acl.getEntries();
        for (int i = 0; i < entries.size(); i++) {
            AccessControlEntry currEntry = entries.get(i);
            if (currEntry.getSid().equals(recipient)) {
                permissions.add(currEntry.getPermission());
            }
        }
        return permissions;
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).CERTIFICATION_BODY, "
            + "T(gov.healthit.chpl.permissions.domains.CertificationBodyDomainPermissions).DELETE_ALL_ACB_PERMISSIONS_FOR_USER, #acb)")
    public void deleteAllPermissionsOnAcb(final CertificationBodyDTO acb, final Sid recipient) {
        ObjectIdentity oid = new ObjectIdentityImpl(CertificationBodyDTO.class, acb.getId());
        MutableAcl acl = (MutableAcl) mutableAclService.readAclById(oid);

        // TODO: this seems very dangerous. I think we should somehow prevent
        // from deleting the ADMIN user???
        List<AccessControlEntry> entries = acl.getEntries();

        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i).getSid().equals(recipient)) {
                acl.deleteAce(i);
                // cannot just loop through deleting because the "entries"
                // list changes size each time that we delete one
                // so we have to re-fetch the entries and re-set the counter
                entries = acl.getEntries();
                i = 0;
            }
        }

        mutableAclService.updateAcl(acl);
        LOGGER.debug("Deleted all acb " + acb + " ACL permissions for recipient " + recipient);
    }

    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).CERTIFICATION_BODY, "
            + "T(gov.healthit.chpl.permissions.domains.CertificationBodyDomainPermissions).DELETE_ALL_PERMISSIONS_FOR_USER)")
    public void deletePermissionsForUser(final UserDTO userDto) throws UserRetrievalException {
        UserDTO foundUser = userDto;
        if (foundUser.getSubjectName() == null) {
            foundUser = userDao.getById(userDto.getId());
        }

        List<CertificationBodyDTO> acbs = certificationBodyDao.findAll();
        for (CertificationBodyDTO acb : acbs) {
            ObjectIdentity oid = new ObjectIdentityImpl(CertificationBodyDTO.class, acb.getId());
            MutableAcl acl = (MutableAcl) mutableAclService.readAclById(oid);

            List<Permission> permissions = new ArrayList<Permission>();
            List<AccessControlEntry> entries = acl.getEntries();
            for (int i = 0; i < entries.size(); i++) {
                AccessControlEntry currEntry = entries.get(i);
                if (currEntry.getSid().equals(foundUser.getSubjectName())) {
                    permissions.remove(currEntry.getPermission());
                }
            }
        }
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
    @PostFilter("hasAnyRole('ROLE_ADMIN', 'ROLE_ONC') or "
            + "hasPermission(filterObject, 'read') or hasPermission(filterObject, admin)")
    public List<CertificationBodyDTO> getAllForUser() {
        return certificationBodyDao.findAll();
    }

    @Transactional(readOnly = true)
    public CertificationBodyDTO getById(final Long id) throws EntityRetrievalException {
        return certificationBodyDao.getById(id);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ONC', 'ROLE_INVITED_USER_CREATOR') or "
            + "hasPermission(#id, 'gov.healthit.chpl.dto.CertificationBodyDTO', read) or "
            + "hasPermission(#id, 'gov.healthit.chpl.dto.CertificationBodyDTO', admin)")
    public CertificationBodyDTO getIfPermissionById(final Long id) throws EntityRetrievalException {
        return certificationBodyDao.getById(id);
    }

    public MutableAclService getMutableAclService() {
        return mutableAclService;
    }

    public void setCertificationBodyDAO(final CertificationBodyDAO acbDAO) {
        this.certificationBodyDao = acbDAO;
    }

    public void setMutableAclService(final MutableAclService mutableAclService) {
        this.mutableAclService = mutableAclService;
    }

}

package gov.healthit.chpl.manager.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.AccessControlEntry;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.NotFoundException;
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
import gov.healthit.chpl.dao.TestingLabDAO;
import gov.healthit.chpl.domain.concept.ActivityConcept;
import gov.healthit.chpl.dto.TestingLabDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.ActivityManager;
import gov.healthit.chpl.manager.TestingLabManager;

@Service
public class TestingLabManagerImpl extends ApplicationObjectSupport implements TestingLabManager {
    private static final Logger LOGGER = LogManager.getLogger(TestingLabManagerImpl.class);

    @Autowired
    private TestingLabDAO testingLabDAO;
    @Autowired
    private UserDAO userDAO;
    @Autowired
    private MutableAclService mutableAclService;
    @Autowired
    private ActivityManager activityManager;

    @Transactional
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ONC')")
    public TestingLabDTO create(final TestingLabDTO atl)
            throws UserRetrievalException, EntityCreationException, EntityRetrievalException, JsonProcessingException {
        String maxCode = testingLabDAO.getMaxCode();
        int maxCodeValue = Integer.parseInt(maxCode);
        int nextCodeValue = maxCodeValue + 1;

        String nextAtlCode = "";
        if (nextCodeValue < 10) {
            nextAtlCode = "0" + nextCodeValue;
        } else if (nextCodeValue > 99) {
            throw new EntityCreationException(
                    "Cannot create a 2-digit ATL code since there are more than 99 ATLs in the system.");
        } else {
            nextAtlCode = nextCodeValue + "";
        }
        atl.setTestingLabCode(nextAtlCode);
        atl.setRetired(false);
        // Create the atl itself
        TestingLabDTO result = testingLabDAO.create(atl);

        // Grant the current principal administrative permission to the ATL
        addPermission(result, Util.getCurrentUser().getId(), BasePermission.ADMINISTRATION);

        LOGGER.debug("Created testing lab " + result + " and granted admin permission to recipient "
                + gov.healthit.chpl.auth.Util.getUsername());

        String activityMsg = "Created Testing Lab " + result.getName();

        activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_ATL, result.getId(), activityMsg, null, result);

        return result;
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ONC') "
            + "or hasPermission(#atl, admin)")
    public TestingLabDTO update(final TestingLabDTO atl) throws EntityRetrievalException, JsonProcessingException,
            EntityCreationException, UpdateTestingLabException {

        TestingLabDTO toUpdate = testingLabDAO.getById(atl.getId());
        TestingLabDTO result = testingLabDAO.update(atl);

        String activityMsg = "Updated testing lab " + atl.getName();
        activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_ATL, result.getId(), activityMsg, toUpdate,
                result);
        return result;
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ONC')")
    public TestingLabDTO retire(final TestingLabDTO atl) throws EntityRetrievalException,
    JsonProcessingException, EntityCreationException, UpdateTestingLabException {
        Date now = new Date();
        if (now.before(atl.getRetirementDate())) {
            throw new UpdateTestingLabException("Retirement date must be before \"now\".");
        }
        TestingLabDTO result = null;
        TestingLabDTO toUpdate = testingLabDAO.getById(atl.getId());
        toUpdate.setRetired(true);
        toUpdate.setRetirementDate(atl.getRetirementDate());
        result = testingLabDAO.update(toUpdate);

        String activityMsg = "Retired atl " + toUpdate.getName();
        activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_ATL, result.getId(), activityMsg,
                toUpdate, result);
        return result;
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ONC')")
    public TestingLabDTO unretire(final Long atlId) throws EntityRetrievalException,
        JsonProcessingException, EntityCreationException, UpdateTestingLabException {
        TestingLabDTO result = null;
        TestingLabDTO toUpdate = testingLabDAO.getById(atlId);
        toUpdate.setRetired(false);
        toUpdate.setRetirementDate(null);
        result = testingLabDAO.update(toUpdate);

        String activityMsg = "Unretired atl " + toUpdate.getName();
        activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_ATL, result.getId(), activityMsg,
                toUpdate, result);
        return result;
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ONC') "
            + "or hasPermission(#atl, admin) or hasPermission(#atl, read)")
    public List<UserDTO> getAllUsersOnAtl(final TestingLabDTO atl) {
        ObjectIdentity oid = new ObjectIdentityImpl(TestingLabDTO.class, atl.getId());
        MutableAcl acl = (MutableAcl) mutableAclService.readAclById(oid);

        Set<String> userNames = new HashSet<String>();
        List<AccessControlEntry> entries = acl.getEntries();
        for (int i = 0; i < entries.size(); i++) {
            Sid sid = entries.get(i).getSid();
            if (sid instanceof PrincipalSid) {
                PrincipalSid psid = (PrincipalSid) sid;
                userNames.add(psid.getPrincipal());
            } else {
                userNames.add(sid.toString());
            }
        }

        // pull back the userdto for the sids
        List<UserDTO> users = new ArrayList<UserDTO>();
        if (userNames != null && userNames.size() > 0) {
            List<String> usernameList = new ArrayList<String>(userNames);
            users.addAll(userDAO.findByNames(usernameList));
        }
        return users;
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ONC') "
            + "or hasPermission(#atl, read) or hasPermission(#atl, admin)")
    public List<Permission> getPermissionsForUser(final TestingLabDTO atl, final Sid recipient) {
        ObjectIdentity oid = new ObjectIdentityImpl(TestingLabDTO.class, atl.getId());
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
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ONC', 'ROLE_INVITED_USER_CREATOR') "
            + "or (hasRole('ROLE_ATL') and hasPermission(#atl, admin))")
    public void addPermission(final TestingLabDTO atl, final Long userId, final Permission permission)
            throws UserRetrievalException {
        MutableAcl acl;
        ObjectIdentity oid = new ObjectIdentityImpl(TestingLabDTO.class, atl.getId());

        try {
            acl = (MutableAcl) mutableAclService.readAclById(oid);
        } catch (final NotFoundException nfe) {
            acl = mutableAclService.createAcl(oid);
        }

        UserDTO user = userDAO.getById(userId);
        if (user == null || user.getSubjectName() == null) {
            throw new UserRetrievalException("Could not find user with id " + userId);
        }

        Sid recipient = new PrincipalSid(user.getSubjectName());
        if (permissionExists(acl, recipient, permission)) {
            LOGGER.debug("User " + recipient + " already has permission on the testing lab " + atl.getName());
        } else {
            acl.insertAce(acl.getEntries().size(), permission, recipient, true);
            mutableAclService.updateAcl(acl);
            LOGGER.debug("Added permission " + permission + " for Sid " + recipient + " testing lab " + atl.getName());
        }
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ONC') "
            + "or (hasRole('ROLE_ATL') and hasPermission(#atl, admin))")
    public void deletePermission(final TestingLabDTO atl, final Sid recipient, final Permission permission) {
        ObjectIdentity oid = new ObjectIdentityImpl(TestingLabDTO.class, atl.getId());
        MutableAcl acl = (MutableAcl) mutableAclService.readAclById(oid);

        List<AccessControlEntry> entries = acl.getEntries();

        // if the current size is only 1 we shouldn't be able to delete the last
        // one right??
        // then nobody would be able to ever add or delete or read from the atl
        // again
        // in fact the spring code will throw runtime errors if we try to access
        // the ACLs for this ATL.
        if (entries != null && entries.size() > 1) {
            for (int i = 0; i < entries.size(); i++) {
                if (entries.get(i).getSid().equals(recipient) && entries.get(i).getPermission().equals(permission)) {
                    acl.deleteAce(i);
                }
            }
            mutableAclService.updateAcl(acl);
        }
        LOGGER.debug("Deleted testing lab " + atl.getName() + " ACL permission " + permission + " for recipient "
                + recipient);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ONC') "
            + "or (hasRole('ROLE_ATL') and hasPermission(#atl, admin))")
    public void deleteAllPermissionsOnAtl(final TestingLabDTO atl, final Sid recipient) {
        ObjectIdentity oid = new ObjectIdentityImpl(TestingLabDTO.class, atl.getId());
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
        LOGGER.debug("Deleted all testing lab " + atl.getName() + " ACL permissions for recipient " + recipient);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ONC', 'ROLE_ATL')")
    public void deletePermissionsForUser(final UserDTO userDto) throws UserRetrievalException {
        UserDTO foundUser = userDto;
        if (foundUser.getSubjectName() == null) {
            foundUser = userDAO.getById(userDto.getId());
        }

        List<TestingLabDTO> atls = testingLabDAO.findAll();
        for (TestingLabDTO atl : atls) {
            ObjectIdentity oid = new ObjectIdentityImpl(TestingLabDTO.class, atl.getId());
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

    private boolean permissionExists(final MutableAcl acl, final Sid recipient, final Permission permission) {
        boolean permissionExists = false;
        List<AccessControlEntry> entries = acl.getEntries();

        for (int i = 0; i < entries.size(); i++) {
            AccessControlEntry currEntry = entries.get(i);
            if (currEntry.getSid().equals(recipient) && currEntry.getPermission().equals(permission)) {
                permissionExists = true;
            }
        }
        return permissionExists;
    }

    @Transactional(readOnly = true)
    public List<TestingLabDTO> getAll() {
        return testingLabDAO.findAll();
    }

    @Transactional(readOnly = true)
    public List<TestingLabDTO> getAllActive() {
        return testingLabDAO.findAllActive();
    }

    @Transactional(readOnly = true)
    @PostFilter("hasAnyRole('ROLE_ADMIN', 'ROLE_ONC') "
            + "or hasPermission(filterObject, 'read') or hasPermission(filterObject, admin)")
    public List<TestingLabDTO> getAllForUser() {
        return testingLabDAO.findAll();
    }

    @Transactional(readOnly = true)
    public TestingLabDTO getById(final Long id) throws EntityRetrievalException {
        return testingLabDAO.getById(id);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ONC', 'ROLE_INVITED_USER_CREATOR') or "
            + "hasPermission(#id, 'gov.healthit.chpl.dto.TestingLabDTO', read) or "
            + "hasPermission(#id, 'gov.healthit.chpl.dto.TestingLabDTO', admin)")
    public TestingLabDTO getIfPermissionById(final Long id) throws EntityRetrievalException {
        return testingLabDAO.getById(id);
    }

}

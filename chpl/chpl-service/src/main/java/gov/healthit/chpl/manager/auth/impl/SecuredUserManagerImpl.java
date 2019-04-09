package gov.healthit.chpl.manager.auth.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.NotFoundException;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.Sid;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.auth.entity.UserEntity;
import gov.healthit.chpl.dao.auth.UserContactDAO;
import gov.healthit.chpl.dao.auth.UserDAO;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.exception.UserCreationException;
import gov.healthit.chpl.exception.UserManagementException;
import gov.healthit.chpl.exception.UserPermissionRetrievalException;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.manager.auth.SecuredUserManager;
import gov.healthit.chpl.manager.impl.SecuredManager;

@Service
public class SecuredUserManagerImpl extends SecuredManager implements SecuredUserManager {

    private UserDAO userDAO;
    private UserContactDAO userContactDAO;
    private MutableAclService mutableAclService;

    @Autowired
    public SecuredUserManagerImpl(UserDAO userDAO, UserContactDAO userContactDAO,
            MutableAclService mutableAclService) {
        this.userDAO = userDAO;
        this.userContactDAO = userContactDAO;
        this.mutableAclService = mutableAclService;
    }

    @Override
    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SECURED_USER, "
            + "T(gov.healthit.chpl.permissions.domains.SecuredUserDomainPermissions).CREATE)")
    public UserDTO create(final UserDTO user, final String encodedPassword)
            throws UserCreationException, UserRetrievalException {

        UserDTO newUser = userDAO.create(user, encodedPassword);

        // Grant the user administrative permission over itself.
        addAclPermission(newUser, new PrincipalSid(newUser.getSubjectName()), BasePermission.ADMINISTRATION);

        return newUser;
    }

    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SECURED_USER, "
            + "T(gov.healthit.chpl.permissions.domains.SecuredUserDomainPermissions).UPDATE, #user)")
    public UserDTO update(final UserDTO user) throws UserRetrievalException {
        return userDAO.update(user);
    }

    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SECURED_USER, "
            + "T(gov.healthit.chpl.permissions.domains.SecuredUserDomainPermissions).UPDATE_CONTACT_INFO, #user)")
    public void updateContactInfo(final UserEntity user) {
        userContactDAO.update(user.getContact());
    }

    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SECURED_USER, "
            + "T(gov.healthit.chpl.permissions.domains.SecuredUserDomainPermissions).DELETE)")
    @Transactional
    public void delete(final UserDTO user)
            throws UserRetrievalException, UserPermissionRetrievalException, UserManagementException {

        // remove all ACLs for this user
        //should only be one - for themselves
        ObjectIdentity oid = new ObjectIdentityImpl(UserDTO.class, user.getId());
        mutableAclService.deleteAcl(oid, false);

        // now delete the user
        userDAO.delete(user.getId());
    }

    @PostFilter("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SECURED_USER, "
            + "T(gov.healthit.chpl.permissions.domains.SecuredUserDomainPermissions).GET_ALL, filterObject)")
    public List<UserDTO> getAll() {
        return userDAO.findAll();
    }

    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SECURED_USER, "
            + "T(gov.healthit.chpl.permissions.domains.SecuredUserDomainPermissions).GET_BY_PERMISSION)")
    public List<UserDTO> getUsersWithPermission(final String permissionName) {
        return userDAO.getUsersWithPermission(permissionName);
    }

    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SECURED_USER, "
            + "T(gov.healthit.chpl.permissions.domains.SecuredUserDomainPermissions).GET_BY_ID, #id)")
    public UserDTO getById(final Long id) throws UserRetrievalException {
        return userDAO.getById(id);
    }

    private void addAclPermission(final UserDTO user, final Sid recipient, final Permission permission) {

        MutableAcl acl;
        ObjectIdentity oid = new ObjectIdentityImpl(UserDTO.class, user.getId());

        try {
            acl = (MutableAcl) mutableAclService.readAclById(oid);
        } catch (NotFoundException nfe) {
            acl = mutableAclService.createAcl(oid);
        }

        acl.insertAce(acl.getEntries().size(), permission, recipient, true);
        mutableAclService.updateAcl(acl);

    }

    @Override
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SECURED_USER, "
            + "T(gov.healthit.chpl.permissions.domains.SecuredUserDomainPermissions).UPDATE_PASSWORD, #user)")
    public void updatePassword(final UserDTO user, final String encodedPassword) throws UserRetrievalException {
        userDAO.updatePassword(user.getSubjectName(), encodedPassword);
    }

    @Override
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SECURED_USER, "
            + "T(gov.healthit.chpl.permissions.domains.SecuredUserDomainPermissions).FAILED_LOGIN_COUNT)")
    public void updateFailedLoginCount(final UserDTO user) throws UserRetrievalException {
        userDAO.updateFailedLoginCount(user.getSubjectName(), user.getFailedLoginCount());
    }

    @Override
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SECURED_USER, "
            + "T(gov.healthit.chpl.permissions.domains.SecuredUserDomainPermissions).LOCKED_STATUS)")
    public void updateAccountLockedStatus(final UserDTO user) throws UserRetrievalException {
        userDAO.updateAccountLockedStatus(user.getSubjectName(), user.isAccountLocked());
    }

    @Override
    @PostAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SECURED_USER, "
            + "T(gov.healthit.chpl.permissions.domains.SecuredUserDomainPermissions).GET_BY_USER_NAME, returnObject)")
    public UserDTO getBySubjectName(final String userName) throws UserRetrievalException {
        return userDAO.getByName(userName);
    }
}

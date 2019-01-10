package gov.healthit.chpl.auth.manager.impl;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostAuthorize;
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

import gov.healthit.chpl.auth.dao.UserContactDAO;
import gov.healthit.chpl.auth.dao.UserDAO;
import gov.healthit.chpl.auth.dao.UserPermissionDAO;
import gov.healthit.chpl.auth.domain.Authority;
import gov.healthit.chpl.auth.dto.UserDTO;
import gov.healthit.chpl.auth.dto.UserPermissionDTO;
import gov.healthit.chpl.auth.entity.UserEntity;
import gov.healthit.chpl.auth.manager.SecuredUserManager;
import gov.healthit.chpl.auth.permission.UserPermissionRetrievalException;
import gov.healthit.chpl.auth.user.UserCreationException;
import gov.healthit.chpl.auth.user.UserManagementException;
import gov.healthit.chpl.auth.user.UserRetrievalException;

@Service
public class SecuredUserManagerImpl implements SecuredUserManager {

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private UserContactDAO userContactDAO;

    @Autowired
    private UserPermissionDAO userPermissionDAO;

    @Autowired
    private MutableAclService mutableAclService;

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ONC', 'ROLE_INVITED_USER_CREATOR', "
            + "'ROLE_ACB', 'ROLE_ATL', 'ROLE_USER_CREATOR')")
    public UserDTO create(UserDTO user, final String encodedPassword)
            throws UserCreationException, UserRetrievalException {

        user = userDAO.create(user, encodedPassword);

        // Grant the user administrative permission over itself.
        addAclPermission(user, new PrincipalSid(user.getSubjectName()), BasePermission.ADMINISTRATION);

        return user;
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ONC', 'ROLE_ACB', 'ROLE_ATL') or hasPermission(#user, admin)")
    public UserDTO update(final UserDTO user) throws UserRetrievalException {
        return userDAO.update(user);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ONC', 'ROLE_ACB', 'ROLE_ATL') or hasPermission(#user, admin)")
    public void updateContactInfo(final UserEntity user) {
        userContactDAO.update(user.getContact());
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ONC')")
    @Transactional
    public void delete(final UserDTO user) throws UserRetrievalException,
    UserPermissionRetrievalException, UserManagementException {
        //find the granted permissions for this user and remove them
        Set<UserPermissionDTO> permissions = getGrantedPermissionsForUser(user);
        for (UserPermissionDTO permission : permissions) {
            if (permission.getAuthority().equals(Authority.ROLE_ADMIN)) {
                removeAdmin(user.getSubjectName());
            } else {
                removeRole(user, permission.getAuthority());
            }
        }

        // remove all ACLs for the user for all users and acbs
        ObjectIdentity oid = new ObjectIdentityImpl(UserDTO.class, user.getId());
        mutableAclService.deleteAcl(oid, false);

        // now delete the user
        userDAO.delete(user.getId());
    }

    @PostFilter("hasAnyRole('ROLE_ADMIN', 'ROLE_ONC') or hasPermission(filterObject, 'read') or hasPermission(filterObject, admin)")
    public List<UserDTO> getAll() {
        return userDAO.findAll();
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ONC', 'ROLE_ACB', 'ROLE_ATL', 'ROLE_CMS_STAFF')")
    public List<UserDTO> getUsersWithPermission(final String permissionName) {
        return userDAO.getUsersWithPermission(permissionName);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ACB', 'ROLE_ATL', 'ROLE_ONC') or "
            + "hasPermission(#id, 'gov.healthit.chpl.auth.dto.UserDTO', admin)")
    public UserDTO getById(final Long id) throws UserRetrievalException {
        return userDAO.getById(id);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ONC', 'ROLE_INVITED_USER_CREATOR', "
            + "'ROLE_ACB', 'ROLE_ATL', 'ROLE_USER_CREATOR') or hasPermission(#user, admin)")
    public void addAclPermission(final UserDTO user, final Sid recipient, final Permission permission) {

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

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ONC') or hasPermission(#user, admin)")
    public void deleteAclPermission(final UserDTO user, final Sid recipient, final Permission permission) {

        ObjectIdentity oid = new ObjectIdentityImpl(UserDTO.class, user.getId());
        MutableAcl acl = (MutableAcl) mutableAclService.readAclById(oid);

        List<AccessControlEntry> entries = acl.getEntries();

        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i).getSid().equals(recipient) && entries.get(i).getPermission().equals(permission)) {
                acl.deleteAce(i);
            }
        }
        mutableAclService.updateAcl(acl);
    }

    @Override
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ONC', 'ROLE_ACB', 'ROLE_ATL', 'ROLE_INVITED_USER_CREATOR')")
    public void grantRole(final String userName, final String role)
            throws UserRetrievalException, UserManagementException, UserPermissionRetrievalException {
        if (role.equals(Authority.ROLE_ADMIN)
                || role.equals("ROLE_ACL_ADMIN") || role.equals("ROLE_ADMINISTRATOR")
                || role.equals("ROLE_USER_AUTHENTICATOR")) {
            throw new UserManagementException("This role cannot be granted using the grant role functionality");
        }

        userDAO.addPermission(userName, role);
    }

    @Override
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_INVITED_USER_CREATOR')")
    public void grantAdmin(final String userName)
            throws UserPermissionRetrievalException, UserRetrievalException, UserManagementException {
        userDAO.addPermission(userName, Authority.ROLE_ADMIN);
    }

    @Override
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ONC', 'ROLE_ACB', 'ROLE_ATL') or hasPermission(#user, admin)")
    public void removeRole(final UserDTO user, final String role)
            throws UserManagementException, UserRetrievalException, UserPermissionRetrievalException {
        removeRole(user.getSubjectName(), role);
    }

    @Override
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ONC', 'ROLE_ACB', 'ROLE_ATL') or hasPermission(#user, admin)")
    public void removeRole(final String userName, final String role)
            throws UserManagementException, UserRetrievalException, UserPermissionRetrievalException {
        if (role.equals(Authority.ROLE_ADMIN) || role.equals("ROLE_ACL_ADMIN") || role.equals("ROLE_ADMINISTRATOR")
                || role.equals("ROLE_USER_AUTHENTICATOR")) {
            throw new UserManagementException("This role cannot be removed using the remove role functionality");
        }

        userDAO.removePermission(userName, role);
    }

    @Override
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void removeAdmin(final String userName)
            throws UserPermissionRetrievalException, UserRetrievalException, UserManagementException {
        userDAO.removePermission(userName, Authority.ROLE_ADMIN);
    }

    @Override
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ONC') or hasPermission(#user, admin)")
    public void updatePassword(final UserDTO user, final String encodedPassword) throws UserRetrievalException {
        userDAO.updatePassword(user.getSubjectName(), encodedPassword);
    }

    @Override
    @PreAuthorize("hasRole('ROLE_USER_AUTHENTICATOR')")
    public void updateFailedLoginCount(final UserDTO user) throws UserRetrievalException {
        userDAO.updateFailedLoginCount(user.getSubjectName(), user.getFailedLoginCount());
    }

    @Override
    @PreAuthorize("hasRole('ROLE_USER_AUTHENTICATOR')")
    public void updateAccountLockedStatus(final UserDTO user) throws UserRetrievalException {
        userDAO.updateAccountLockedStatus(user.getSubjectName(), user.isAccountLocked());
    }

    @Override
    @PreAuthorize("hasAnyRole('ROLE_USER_AUTHENTICATOR', 'ROLE_ADMIN', 'ROLE_ONC', 'ROLE_ACB', 'ROLE_ATL') "
            + " or hasPermission(#user, 'read') or hasPermission(#user, admin)")
    public Set<UserPermissionDTO> getGrantedPermissionsForUser(final UserDTO user) {
        return this.userPermissionDAO.findPermissionsForUser(user.getId());
    }

    @Override
    @PostAuthorize("hasAnyRole('ROLE_INVITED_USER_CREATOR', 'ROLE_USER_AUTHENTICATOR', "
            + "'ROLE_ADMIN', 'ROLE_ONC', 'ROLE_ACB', 'ROLE_ATL') or hasPermission(returnObject, 'read') "
            + "or hasPermission(returnObject, admin)")
    public UserDTO getBySubjectName(final String userName) throws UserRetrievalException {
        return userDAO.getByName(userName);
    }
}

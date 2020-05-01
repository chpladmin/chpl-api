package gov.healthit.chpl.manager.auth;

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

import gov.healthit.chpl.dao.auth.UserContactDAO;
import gov.healthit.chpl.dao.auth.UserDAO;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.entity.auth.UserEntity;
import gov.healthit.chpl.exception.UserCreationException;
import gov.healthit.chpl.exception.UserManagementException;
import gov.healthit.chpl.exception.UserPermissionRetrievalException;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.manager.impl.SecuredManager;

@Service
public class SecuredUserManager extends SecuredManager {

    private UserDAO userDAO;
    private UserContactDAO userContactDAO;
    private MutableAclService mutableAclService;

    @Autowired
    public SecuredUserManager(UserDAO userDAO, UserContactDAO userContactDAO, MutableAclService mutableAclService) {
        this.userDAO = userDAO;
        this.userContactDAO = userContactDAO;
        this.mutableAclService = mutableAclService;
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SECURED_USER, "
            + "T(gov.healthit.chpl.permissions.domains.SecuredUserDomainPermissions).CREATE)")
    public UserDTO create(UserDTO user, String encodedPassword)
            throws UserCreationException, UserRetrievalException {

        UserDTO newUser = userDAO.create(user, encodedPassword);

        // Grant the user administrative permission over itself.
        addAclPermission(newUser, new PrincipalSid(newUser.getSubjectName()), BasePermission.ADMINISTRATION);

        return newUser;
    }

    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SECURED_USER, "
            + "T(gov.healthit.chpl.permissions.domains.SecuredUserDomainPermissions).UPDATE, #user)")
    public UserDTO update(UserDTO user) throws UserRetrievalException {
        return userDAO.update(user);
    }

    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SECURED_USER, "
            + "T(gov.healthit.chpl.permissions.domains.SecuredUserDomainPermissions).UPDATE_CONTACT_INFO, #user)")
    public void updateContactInfo(UserEntity user) {
        userContactDAO.update(user.getContact());
    }

    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SECURED_USER, "
            + "T(gov.healthit.chpl.permissions.domains.SecuredUserDomainPermissions).DELETE)")
    @Transactional
    public void delete(UserDTO user)
            throws UserRetrievalException, UserPermissionRetrievalException, UserManagementException {

        // remove all ACLs for this user
        // should only be one - for themselves
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
    public List<UserDTO> getUsersWithPermission(String permissionName) {
        return userDAO.getUsersWithPermission(permissionName);
    }

    @PostAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SECURED_USER, "
            + "T(gov.healthit.chpl.permissions.domains.SecuredUserDomainPermissions).GET_BY_ID, returnObject)")
    public UserDTO getById(Long id) throws UserRetrievalException {
        return userDAO.getById(id);
    }

    private void addAclPermission(UserDTO user, Sid recipient, Permission permission) {

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

    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SECURED_USER, "
            + "T(gov.healthit.chpl.permissions.domains.SecuredUserDomainPermissions).UPDATE_PASSWORD, #user)")
    public void updatePassword(UserDTO user, String encodedPassword) throws UserRetrievalException {
        userDAO.updatePassword(user.getSubjectName(), encodedPassword);
    }

    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SECURED_USER, "
            + "T(gov.healthit.chpl.permissions.domains.SecuredUserDomainPermissions).FAILED_LOGIN_COUNT)")
    public void updateFailedLoginCount(UserDTO user) throws UserRetrievalException {
        userDAO.updateFailedLoginCount(user.getSubjectName(), user.getFailedLoginCount());
    }

    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SECURED_USER, "
            + "T(gov.healthit.chpl.permissions.domains.SecuredUserDomainPermissions).LOCKED_STATUS)")
    public void updateAccountLockedStatus(UserDTO user) throws UserRetrievalException {
        userDAO.updateAccountLockedStatus(user.getSubjectName(), user.isAccountLocked());
    }

    @PostAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SECURED_USER, "
            + "T(gov.healthit.chpl.permissions.domains.SecuredUserDomainPermissions).GET_BY_USER_NAME, returnObject)")
    public UserDTO getBySubjectName(String userName) throws UserRetrievalException {
        return userDAO.getByName(userName);
    }
}

package gov.healthit.chpl.auth.dao.impl;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.persistence.Query;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.auth.BaseDAOImpl;
import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.auth.dao.UserContactDAO;
import gov.healthit.chpl.auth.dao.UserDAO;
import gov.healthit.chpl.auth.dao.UserPermissionDAO;
import gov.healthit.chpl.auth.dto.UserDTO;
import gov.healthit.chpl.auth.dto.UserPermissionDTO;
import gov.healthit.chpl.auth.entity.UserContactEntity;
import gov.healthit.chpl.auth.entity.UserEntity;
import gov.healthit.chpl.auth.permission.UserPermissionRetrievalException;
import gov.healthit.chpl.auth.user.UserCreationException;
import gov.healthit.chpl.auth.user.UserRetrievalException;

@Repository(value="userDAO")
public class UserDAOImpl extends BaseDAOImpl implements UserDAO {
    private static final Logger LOGGER = LogManager.getLogger(UserDAOImpl.class);

    @Autowired
    private UserPermissionDAO userPermissionDAO;

    @Autowired
    private UserContactDAO userContactDAO;

    @Override
    @Transactional
    public UserDTO create(final UserDTO user, final String encodedPassword) throws UserCreationException {

        UserEntity userEntity = null;
        try {
            userEntity = getEntityByName(user.getSubjectName());
        } catch (UserRetrievalException ignore) { }

        if (userEntity != null) {
            throw new UserCreationException("user name: " + user.getSubjectName() + " already exists.");
        } else {

            userEntity = new UserEntity(user.getSubjectName(), encodedPassword);

            userEntity.setFullName(user.getFullName());
            userEntity.setFriendlyName(user.getFriendlyName());
            userEntity.setComplianceSignature(user.getComplianceSignatureDate());
            userEntity.setFailedLoginCount(0);
            userEntity.setAccountEnabled(user.isAccountEnabled());
            userEntity.setAccountExpired(user.isAccountExpired());
            userEntity.setAccountLocked(user.isAccountLocked());
            userEntity.setCredentialsExpired(!user.isCredentialsNonExpired());
            userEntity.setPasswordResetRequired(user.getPasswordResetRequired());
            userEntity.setLastModifiedUser(Util.getCurrentUser().getId());
            userEntity.setLastModifiedDate(new Date());
            userEntity.setDeleted(false);

            UserContactEntity contact = new UserContactEntity();
            contact.setEmail(user.getEmail());
            contact.setFullName(user.getFullName());
            contact.setFriendlyName(user.getFriendlyName());
            contact.setPhoneNumber(user.getPhoneNumber());
            contact.setTitle(user.getTitle());
            contact.setLastModifiedUser(Util.getCurrentUser().getId());
            contact.setLastModifiedDate(new Date());
            contact.setDeleted(false);
            contact.setSignatureDate(null); //null for new user, must confirm email to get it filled in

            userContactDAO.create(contact);
            userEntity.setContact(contact);
            create(userEntity);
        }

        return new UserDTO(userEntity);
    }


    @Override
    @Transactional
    public UserDTO update(final UserDTO user) throws UserRetrievalException {

        UserEntity userEntity = getEntityByName(user.getSubjectName());

        userEntity.setFullName(user.getFullName());
        userEntity.setFriendlyName(user.getFriendlyName());
        userEntity.setComplianceSignature(user.getComplianceSignatureDate());
        userEntity.setFailedLoginCount(user.getFailedLoginCount());
        userEntity.getContact().setEmail(user.getEmail());
        userEntity.getContact().setPhoneNumber(user.getPhoneNumber());
        userEntity.getContact().setSignatureDate(user.getSignatureDate());
        userEntity.getContact().setTitle(user.getTitle());
        userEntity.setAccountEnabled(user.isAccountEnabled());
        userEntity.setAccountExpired(user.isAccountExpired());
        userEntity.setAccountLocked(user.isAccountLocked());
        userEntity.setCredentialsExpired(!user.isCredentialsNonExpired());
        userEntity.setPasswordResetRequired(user.getPasswordResetRequired());
        userEntity.setLastModifiedUser(Util.getCurrentUser().getId());
        userEntity.getContact().setLastModifiedUser(Util.getCurrentUser().getId());

        update(userEntity);
        return new UserDTO(userEntity);
    }


    @Override
    @Transactional
    public void delete(final String uname) throws UserRetrievalException {

        // First delete the user / permission mappings for this user.
        userPermissionDAO.deleteMappingsForUser(uname);

        UserEntity toDelete = getEntityByName(uname);
        if (toDelete == null) {
            throw new UserRetrievalException("Could not find user with name " + uname);
        }

        delete(toDelete);
    }


    @Override
    @Transactional
    public void delete(final Long userId) throws UserRetrievalException {

        // First delete the user / permission mappings for this user.
        userPermissionDAO.deleteMappingsForUser(userId);

        UserEntity toDelete = getEntityById(userId);
        if (toDelete == null) {
            throw new UserRetrievalException("Could not find user with id " + userId);
        }

        delete(toDelete);
    }

    private void delete(final UserEntity toDelete) {
        //delete the contact
        if (toDelete.getContact() != null) {
            userContactDAO.delete(toDelete.getContact());
        }

        //delete the user
        toDelete.setLastModifiedUser(Util.getCurrentUser().getId());
        toDelete.setLastModifiedDate(new Date());
        toDelete.setDeleted(true);
        update(toDelete);
    }

    public List<UserDTO> findAll() {

        List<UserEntity> entities = getAllEntities();
        List<UserDTO> users = new ArrayList<>();

        for (UserEntity entity : entities) {
            UserDTO user = new UserDTO(entity);
            users.add(user);
        }
        return users;
    }

    public List<UserDTO> findByNames(final List<String> names) {
        List<UserDTO> result = new ArrayList<UserDTO>();

        if (names == null || names.size() == 0) {
            return result;
        }

        Query query = entityManager.createQuery("from UserEntity where deleted <> true AND user_name in (:names)",
                UserEntity.class);
        query.setParameter("names", names);
        List<UserEntity> queryResult = query.getResultList();

        if (queryResult != null) {
            for (UserEntity entity : queryResult) {
                result.add(new UserDTO(entity));
            }
        }
        return result;
    }

    public UserDTO findUserByNameAndEmail(final String username, final String email) {
        UserDTO foundUser = null;

        String userQuery = "from UserEntity u"
                + " where (NOT u.deleted = true) "
                + " AND (u.subjectName = :subjectName) "
                + " AND (u.contact.email = :email)";

        Query query = entityManager.createQuery(userQuery, UserEntity.class);
        query.setParameter("subjectName", username);
        query.setParameter("email", email);
        List<UserEntity> result = query.getResultList();
        if (result.size() >= 1) {
            UserEntity entity = result.get(0);
            foundUser = new UserDTO(entity);
        }

        return foundUser;
    }

    public UserDTO findUser(final UserDTO toSearch) {
        UserDTO foundUser = null;

        String userQuery = "from UserEntity u"
                + " where (NOT u.deleted = true) "
                + " AND (u.subjectName = :subjectName) "
                + " AND (u.contact.fullName = :fullName)"
                + " AND (u.contact.friendlyName = :friendlyName)"
                + " AND (u.contact.email = :email)"
                + " AND (u.contact.phoneNumber = :phoneNumber)";
        if (toSearch.getTitle() != null) {
            userQuery += " AND (u.contact.title = :title)";
        } else {
            userQuery += " AND (u.contact.title IS NULL)";
        }
        Query query = entityManager.createQuery(userQuery, UserEntity.class);
        query.setParameter("subjectName", toSearch.getSubjectName());
        query.setParameter("fullName", toSearch.getFullName());
        query.setParameter("friendlyName", toSearch.getFriendlyName());
        query.setParameter("email", toSearch.getEmail());
        query.setParameter("phoneNumber", toSearch.getPhoneNumber());
        if (toSearch.getTitle() != null) {
            query.setParameter("title", toSearch.getTitle());
        }

        List<UserEntity> result = query.getResultList();
        if (result.size() >= 1) {
            UserEntity entity = result.get(0);
            foundUser = new UserDTO(entity);
        }

        return foundUser;
    }

    private void create(final UserEntity user) {

        entityManager.persist(user);
    }

    private void update(final UserEntity user) {

        entityManager.merge(user);

    }

    private List<UserEntity> getAllEntities() {

        List<UserEntity> result = entityManager.createQuery("from UserEntity where (NOT deleted = true) ",
                UserEntity.class).getResultList();

        return result;
    }

    private UserEntity getEntityById(final Long userId) throws UserRetrievalException {

        UserEntity user = null;

        Query query = entityManager.createQuery("from UserEntity where (NOT deleted = true) "
                + "AND (user_id = :userid) ", UserEntity.class);
        query.setParameter("userid", userId);
        List<UserEntity> result = query.getResultList();

        if (result == null || result.size() == 0) {
            String msg = String.format(messageSource.getMessage(
                    new DefaultMessageSourceResolvable("user.notFound"), LocaleContextHolder.getLocale()));
            throw new UserRetrievalException(msg);
        } else if (result.size() > 1) {
            throw new UserRetrievalException("Data error. Duplicate user id in database.");
        }

        if (result.size() == 0) {
            return null;
        }
        return result.get(0);
    }


    private UserEntity getEntityByName(final String uname) throws UserRetrievalException {
        UserEntity user = null;

        Query query = entityManager.createQuery("from UserEntity where ((NOT deleted = true) "
                + "AND (user_name = (:uname))) ", UserEntity.class);
        query.setParameter("uname", uname);
        List<UserEntity> result = query.getResultList();

        if (result == null || result.size() == 0) {
            String msg = String.format(messageSource.getMessage(
                    new DefaultMessageSourceResolvable("user.notFound"), LocaleContextHolder.getLocale()));
            throw new UserRetrievalException(msg);
        } else if (result.size() > 1) {
            throw new UserRetrievalException("Data error. Duplicate user name in database.");
        }
        return result.get(0);
    }

    @Override
    @Transactional
    public void addPermission(final String uname, final String authority)
            throws UserPermissionRetrievalException, UserRetrievalException {

        UserEntity userEntity = this.getEntityByName(uname);
        if (userEntity != null) {
            Set<UserPermissionDTO> permissions = userPermissionDAO.findPermissionsForUser(userEntity.getId());
            boolean permissionExists = false;
            for (UserPermissionDTO permission : permissions) {
                if (permission.getAuthority().equals(authority)) {
                    permissionExists = true;
                }
            }

            if (!permissionExists) {
                userPermissionDAO.createMapping(userEntity, authority);
            } else {
                LOGGER.error("Permission " + authority + " already exists for " + uname + ". Not adding anything.");
            }
        }
    }

    @Override
    public UserDTO getById(final Long userId) throws UserRetrievalException {

        UserEntity userEntity = this.getEntityById(userId);
        if (userEntity == null) {
            return null;
        }
        return new UserDTO(userEntity);
    }

    @Override
    public UserDTO getByName(final String uname) throws UserRetrievalException {
        UserEntity userEntity = this.getEntityByName(uname);
        if (userEntity == null) {
            return null;
        }
        return new UserDTO(userEntity);
    }

    /**
     * Get Users with permission.
     * @param permissionName the ROLE_SOMETHING that we care about
     * @return
     */
    @Override
    public List<UserDTO> getUsersWithPermission(final String permissionName) {
        String hql = "SELECT u "
                + "FROM UserEntity u "
                + "JOIN FETCH u.permissionMappings userPermissionMap "
                + "JOIN FETCH userPermissionMap.permission permission "
                + "WHERE permission.authority = :permissionName";
        Query query = entityManager.createQuery(hql);
        query.setParameter("permissionName", permissionName);
        List<UserEntity> usersWithPermission = query.getResultList();
        List<UserDTO> results = new ArrayList<UserDTO>();
        if (usersWithPermission != null && usersWithPermission.size() > 0) {
            for (UserEntity result : usersWithPermission) {
                results.add(new UserDTO(result));
            }
        }
        return results;
    }

    @Override
    @Transactional
    public void removePermission(final String uname, final String authority) throws UserRetrievalException,
    UserPermissionRetrievalException {
        userPermissionDAO.deleteMapping(uname, authority);
    }

    @Override
    @Transactional
    public void updatePassword(final String uname, final String encodedPassword) throws UserRetrievalException {

        UserEntity userEntity = this.getEntityByName(uname);
        userEntity.setPassword(encodedPassword);
        userEntity.setPasswordResetRequired(false);
        update(userEntity);

    }

    @Override
    @Transactional
    public void updateFailedLoginCount(final String uname, final int failedLoginCount) throws UserRetrievalException {

        UserEntity userEntity = this.getEntityByName(uname);
        userEntity.setFailedLoginCount(failedLoginCount);
        update(userEntity);

    }

    @Override
    @Transactional
    public void updateAccountLockedStatus(final String uname, final boolean locked) throws UserRetrievalException {

        UserEntity userEntity = this.getEntityByName(uname);
        userEntity.setAccountLocked(locked);
        update(userEntity);

    }

    public String getEncodedPassword(final UserDTO user) throws UserRetrievalException {
        UserEntity userEntity = getEntityByName(user.getUsername());
        return userEntity.getPassword();
    }
}

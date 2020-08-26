package gov.healthit.chpl.dao.auth;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.entity.auth.UserContactEntity;
import gov.healthit.chpl.entity.auth.UserEntity;
import gov.healthit.chpl.entity.auth.UserPermissionEntity;
import gov.healthit.chpl.exception.MultipleUserAccountsException;
import gov.healthit.chpl.exception.UserCreationException;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.util.AuthUtil;
import gov.healthit.chpl.util.UserMapper;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Repository(value = "userDAO")
public class UserDAO extends BaseDAOImpl {
    private UserContactDAO userContactDAO;
    private UserMapper userMapper;

    @Autowired
    public UserDAO(UserContactDAO userContactDAO, UserMapper userMapper) {
       this.userContactDAO = userContactDAO;
       this.userMapper = userMapper;
    }

    @Transactional
    public UserDTO create(UserDTO user, String encodedPassword) throws UserCreationException {
        UserEntity userEntity = null;
        try {
            userEntity = getEntityByName(user.getSubjectName());
        } catch (UserRetrievalException ignore) {
        }

        if (userEntity != null) {
            throw new UserCreationException(msgUtil.getMessage("user.duplicateName", user.getSubjectName()));
        } else {
            userEntity = new UserEntity();
            userEntity.setSubjectName(user.getSubjectName());
            if (user.getPermission() != null) {
                userEntity.setUserPermissionId(user.getPermission().getId());
                userEntity.setPermission(entityManager.find(UserPermissionEntity.class, user.getPermission().getId()));
            } else {
                throw new UserCreationException(msgUtil.getMessage("user.missingPermission", user.getSubjectName()));
            }
            userEntity.setPassword(encodedPassword);
            userEntity.setFailedLoginCount(0);
            userEntity.setAccountEnabled(user.isAccountEnabled());
            userEntity.setAccountExpired(user.isAccountExpired());
            userEntity.setAccountLocked(user.isAccountLocked());
            userEntity.setCredentialsExpired(!user.isCredentialsNonExpired());
            userEntity.setPasswordResetRequired(user.isPasswordResetRequired());
            userEntity.setLastModifiedUser(AuthUtil.getAuditId());
            userEntity.setLastModifiedDate(new Date());
            userEntity.setDeleted(false);

            UserContactEntity contact = new UserContactEntity();
            contact.setEmail(user.getEmail());
            contact.setFullName(user.getFullName());
            contact.setFriendlyName(user.getFriendlyName());
            contact.setPhoneNumber(user.getPhoneNumber());
            contact.setTitle(user.getTitle());
            contact.setLastModifiedUser(AuthUtil.getAuditId());
            contact.setLastModifiedDate(new Date());
            contact.setDeleted(false);
            contact.setSignatureDate(null); // null for new user, must confirm
            // email to get it filled in

            userContactDAO.create(contact);
            userEntity.setContact(contact);
            super.create(userEntity);
        }

        return userMapper.from(userEntity);
    }

    @Transactional
    public UserDTO update(UserDTO user) throws UserRetrievalException {
        UserEntity userEntity = getEntityByName(user.getSubjectName());
        userEntity.setFailedLoginCount(user.getFailedLoginCount());
        userEntity.getContact().setEmail(user.getEmail());
        userEntity.getContact().setPhoneNumber(user.getPhoneNumber());
        userEntity.getContact().setSignatureDate(user.getSignatureDate());
        userEntity.getContact().setTitle(user.getTitle());
        userEntity.getContact().setFullName(user.getFullName());
        userEntity.getContact().setFriendlyName(user.getFriendlyName());
        userEntity.setAccountEnabled(user.isAccountEnabled());
        userEntity.setAccountExpired(user.isAccountExpired());
        userEntity.setAccountLocked(user.isAccountLocked());
        userEntity.setCredentialsExpired(!user.isCredentialsNonExpired());
        userEntity.setPasswordResetRequired(user.isPasswordResetRequired());
        userEntity.setLastLoggedInDate(user.getLastLoggedInDate());
        userEntity.setLastModifiedUser(AuthUtil.getAuditId());
        userEntity.getContact().setLastModifiedUser(AuthUtil.getAuditId());

        super.update(userEntity);
        return userMapper.from(userEntity);
    }

    @Transactional
    public void delete(String uname) throws UserRetrievalException {
        UserEntity toDelete = getEntityByName(uname);
        if (toDelete == null) {
            throw new UserRetrievalException(msgUtil.getMessage("user.notFound"));
        }
        delete(toDelete);
    }

    @Transactional
    public void delete(Long userId) throws UserRetrievalException {
        UserEntity toDelete = getEntityById(userId);
        if (toDelete == null) {
            throw new UserRetrievalException(msgUtil.getMessage("user.notFound"));
        }
        delete(toDelete);
    }

    private void delete(UserEntity toDelete) {
        // things related to the user are deleted via triggers
        toDelete.setLastModifiedUser(AuthUtil.getAuditId());
        toDelete.setLastModifiedDate(new Date());
        toDelete.setDeleted(true);
        super.update(toDelete);
    }

    public List<UserDTO> findAll() {
        List<UserEntity> entities = getAllEntities();
        List<UserDTO> users = new ArrayList<>();

        for (UserEntity entity : entities) {
            UserDTO user = userMapper.from(entity);
            users.add(user);
        }
        return users;
    }

    public UserDTO findUserByNameAndEmail(String username, String email) {
        UserDTO foundUser = null;

        String userQuery = "from UserEntity u " + "JOIN FETCH u.contact " + "JOIN FETCH u.permission "
                + "WHERE (NOT u.deleted = true) " + "AND (u.subjectName = :subjectName) "
                + "AND (u.contact.email = :email)";

        Query query = entityManager.createQuery(userQuery, UserEntity.class);
        query.setParameter("subjectName", username);
        query.setParameter("email", email);
        List<UserEntity> result = query.getResultList();
        if (result.size() >= 1) {
            UserEntity entity = result.get(0);
            foundUser = userMapper.from(entity);
        }

        return foundUser;
    }

    public UserDTO findUser(UserDTO toSearch) {
        UserDTO foundUser = null;

        String userQuery = "from UserEntity u "
                + " JOIN FETCH u.permission "
                + " JOIN FETCH u.contact "
                + " WHERE (NOT u.deleted = true) "
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
            foundUser = userMapper.from(entity);
        }

        return foundUser;
    }

    private List<UserEntity> getAllEntities() {
        List<UserEntity> result = entityManager.createQuery("from UserEntity u "
                + "JOIN FETCH u.contact "
                + "JOIN FETCH u.permission "
                + "WHERE (NOT u.deleted = true) ", UserEntity.class).getResultList();

        return result;
    }

    private UserEntity getEntityById(Long userId) throws UserRetrievalException {
        Query query = entityManager.createQuery("from UserEntity u "
                + "JOIN FETCH u.contact "
                + "JOIN FETCH u.permission "
                + "WHERE (NOT u.deleted = true) "
                + "AND (u.id = :userid) ",
                UserEntity.class);
        query.setParameter("userid", userId);
        List<UserEntity> result = query.getResultList();

        if (result == null || result.size() == 0) {
            String msg = msgUtil.getMessage("user.notFound");
            throw new UserRetrievalException(msg);
        } else if (result.size() > 1) {
            throw new UserRetrievalException("Data error. Duplicate user id in database.");
        }

        if (result.size() == 0) {
            return null;
        }
        return result.get(0);
    }

    private UserEntity getEntityByName(String uname) throws UserRetrievalException {
        Query query = entityManager
                .createQuery("from UserEntity u "
                        + "JOIN FETCH u.contact "
                        + "JOIN FETCH u.permission "
                        + "where ((NOT u.deleted = true) "
                        + "AND (u.subjectName = (:uname))) ",
                        UserEntity.class);
        query.setParameter("uname", uname);
        List<UserEntity> result = query.getResultList();

        if (result == null || result.size() == 0) {
            String msg = msgUtil.getMessage("user.notFound");
            throw new UserRetrievalException(msg);
        } else if (result.size() > 1) {
            throw new UserRetrievalException("Data error. Duplicate user name in database.");
        }
        return result.get(0);
    }

    public UserDTO getById(Long userId) throws UserRetrievalException {
        UserEntity userEntity = this.getEntityById(userId);
        if (userEntity == null) {
            return null;
        }
        return userMapper.from(userEntity);
    }

    public UserDTO getByName(String uname) throws UserRetrievalException {
        UserEntity userEntity = this.getEntityByName(uname);
        if (userEntity == null) {
            return null;
        }
        return userMapper.from(userEntity);
    }

    public UserDTO getByNameOrEmail(String username) throws MultipleUserAccountsException {
        Query query = entityManager
                .createQuery("SELECT DISTINCT u "
                        + "FROM UserEntity u "
                        + "JOIN FETCH u.contact c "
                        + "JOIN FETCH u.permission "
                        + "WHERE u.deleted <> true "
                        + "AND ((u.subjectName = (:username)) OR c.email = (:username)) ",
                        UserEntity.class);
        query.setParameter("username", username);
        List<UserEntity> userEntities = query.getResultList();
        UserDTO user = null;
        if (userEntities != null) {
            if (userEntities.size() > 1) {
                throw new MultipleUserAccountsException(msgUtil.getMessage("user.multipleAccountsFound", username));
            } else if (userEntities.size() == 1) {
                user = userMapper.from(userEntities.get(0));
            }
        }
        return user;
    }

    public List<UserDTO> getUsersWithPermission(String permissionName) {
        String hql = "SELECT u "
                + "FROM UserEntity u "
                + "JOIN FETCH u.contact contact "
                + "JOIN FETCH u.permission permission "
                + "WHERE permission.authority = :permissionName";
        Query query = entityManager.createQuery(hql);
        query.setParameter("permissionName", permissionName);
        List<UserEntity> usersWithPermission = query.getResultList();
        List<UserDTO> results = new ArrayList<UserDTO>();
        if (usersWithPermission != null && usersWithPermission.size() > 0) {
            for (UserEntity result : usersWithPermission) {
                results.add(userMapper.from(result));
            }
        }
        return results;
    }

    @Transactional
    public void updatePassword(String uname, String encodedPassword) throws UserRetrievalException {
        UserEntity userEntity = this.getEntityByName(uname);
        userEntity.setPassword(encodedPassword);
        userEntity.setPasswordResetRequired(false);
        super.update(userEntity);
    }

    @Transactional
    public void updateFailedLoginCount(String uname, int failedLoginCount) throws UserRetrievalException {
        UserEntity userEntity = this.getEntityByName(uname);
        userEntity.setFailedLoginCount(failedLoginCount);
        super.update(userEntity);
    }

    @Transactional
    public void updateAccountLockedStatus(String uname, boolean locked) throws UserRetrievalException {
        UserEntity userEntity = this.getEntityByName(uname);
        userEntity.setAccountLocked(locked);
        super.update(userEntity);
    }

    public String getEncodedPassword(UserDTO user) throws UserRetrievalException {
        UserEntity userEntity = getEntityByName(user.getUsername());
        return userEntity.getPassword();
    }
}

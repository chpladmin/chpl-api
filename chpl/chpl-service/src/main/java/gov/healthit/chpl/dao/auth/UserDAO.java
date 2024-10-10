package gov.healthit.chpl.dao.auth;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.caching.CacheNames;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.entity.auth.UserContactEntity;
import gov.healthit.chpl.entity.auth.UserEntity;
import gov.healthit.chpl.entity.auth.UserPermissionEntity;
import gov.healthit.chpl.exception.MultipleUserAccountsException;
import gov.healthit.chpl.exception.UserCreationException;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.util.UserMapper;
import jakarta.persistence.Query;

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
            userEntity = getEntityByNameOrEmail(user.getEmail());
        } catch (MultipleUserAccountsException | UserRetrievalException ignore) {
        }

        if (userEntity != null) {
            throw new UserCreationException(msgUtil.getMessage("user.duplicateName", user.getEmail()));
        } else {
            userEntity = new UserEntity();
            if (user.getPermission() != null) {
                userEntity.setUserPermissionId(user.getPermission().getId());
                userEntity.setPermission(entityManager.find(UserPermissionEntity.class, user.getPermission().getId()));
            } else {
                throw new UserCreationException(msgUtil.getMessage("user.missingPermission", user.getUsername()));
            }
            userEntity.setPassword(encodedPassword);
            userEntity.setFailedLoginCount(0);
            userEntity.setAccountEnabled(user.isAccountEnabled());
            userEntity.setAccountExpired(user.isAccountExpired());
            userEntity.setAccountLocked(user.isAccountLocked());
            userEntity.setCredentialsExpired(!user.isCredentialsNonExpired());
            userEntity.setPasswordResetRequired(user.isPasswordResetRequired());
            userEntity.setDeleted(false);

            UserContactEntity contact = new UserContactEntity();
            contact.setEmail(StringUtils.normalizeSpace(user.getEmail()));
            contact.setFullName(StringUtils.normalizeSpace(user.getFullName()));
            contact.setFriendlyName(StringUtils.normalizeSpace(user.getFriendlyName()));
            contact.setPhoneNumber(StringUtils.normalizeSpace(user.getPhoneNumber()));
            contact.setTitle(StringUtils.normalizeSpace(user.getTitle()));
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
    @CacheEvict(value = CacheNames.CHPL_USERS, key = "#user.id")
    public UserDTO update(UserDTO user) throws UserRetrievalException, MultipleUserAccountsException {
        UserEntity userEntity = getEntityById(user.getId());
        userEntity.setFailedLoginCount(user.getFailedLoginCount());
        userEntity.getContact().setPhoneNumber(StringUtils.normalizeSpace(user.getPhoneNumber()));
        userEntity.getContact().setSignatureDate(user.getSignatureDate());
        userEntity.getContact().setTitle(StringUtils.normalizeSpace(user.getTitle()));
        userEntity.getContact().setFullName(StringUtils.normalizeSpace(user.getFullName()));
        userEntity.getContact().setFriendlyName(StringUtils.normalizeSpace(user.getFriendlyName()));
        userEntity.setAccountEnabled(user.isAccountEnabled());
        userEntity.setAccountExpired(user.isAccountExpired());
        userEntity.setAccountLocked(user.isAccountLocked());
        userEntity.setCredentialsExpired(!user.isCredentialsNonExpired());
        userEntity.setPasswordResetRequired(user.isPasswordResetRequired());
        userEntity.setLastLoggedInDate(user.getLastLoggedInDate());

        super.update(userEntity);
        return userMapper.from(userEntity);
    }

    @Transactional
    @CacheEvict(value = CacheNames.CHPL_USERS, key = "#userId")
    public void delete(Long userId) throws UserRetrievalException {
        UserEntity toDelete = getEntityById(userId);
        if (toDelete == null) {
            throw new UserRetrievalException(msgUtil.getMessage("user.notFound"));
        }
        delete(toDelete);
    }

    private void delete(UserEntity toDelete) {
        // things related to the user are deleted via triggers
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

    public UserDTO findUserByEmail(String email) {
        UserDTO foundUser = null;

        String userQuery = "from UserEntity u " + "JOIN FETCH u.contact " + "JOIN FETCH u.permission "
                + "WHERE (NOT u.deleted = true) " + "AND (u.contact.email = :email)";

        Query query = entityManager.createQuery(userQuery, UserEntity.class);
        query.setParameter("email", email);
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
                + "WHERE NOT u.deleted = true "
                + "AND u.id >= 0 ", UserEntity.class).getResultList();

        return result;
    }

    private UserEntity getEntityById(Long userId) throws UserRetrievalException {
        return getEntityById(userId, false);
    }

    private UserEntity getEntityById(Long userId, boolean includeDeleted) throws UserRetrievalException {
        Query query = entityManager.createQuery("from UserEntity u "
                + "JOIN FETCH u.contact "
                + "JOIN FETCH u.permission "
                + "WHERE (u.id = :userid)  "
                + (includeDeleted ? "" : "AND (u.deleted = false) "),
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

    private UserEntity getEntityByNameOrEmail(String email) throws MultipleUserAccountsException, UserRetrievalException {
        Query query = entityManager
                .createQuery("SELECT DISTINCT u "
                        + "FROM UserEntity u "
                        + "JOIN FETCH u.contact c "
                        + "JOIN FETCH u.permission "
                        + "WHERE u.deleted <> true "
                        + "AND ((u.subjectName = (:email)) OR c.email = (:email)) ",
                        UserEntity.class);
        query.setParameter("email", email);
        List<UserEntity> result = query.getResultList();

        if (result == null || result.size() == 0) {
            String msg = msgUtil.getMessage("user.notFound");
            throw new UserRetrievalException(msg);
        } else if (result.size() > 1) {
            throw new MultipleUserAccountsException(msgUtil.getMessage("user.multipleAccountsFound", email));
        }
        return result.get(0);
    }

    public UserDTO getById(Long userId) throws UserRetrievalException {
        return getById(userId, false);
    }

    @Cacheable(value = CacheNames.CHPL_USERS, key = "#userId")
    public UserDTO getById(Long userId, boolean includeDelete) throws UserRetrievalException {
        UserEntity userEntity = this.getEntityById(userId, includeDelete);
        if (userEntity == null) {
            return null;
        }
        return userMapper.from(userEntity);
    }

    public UserDTO getByNameOrEmail(String username) throws MultipleUserAccountsException, UserRetrievalException {
        UserEntity userEntity = this.getEntityByNameOrEmail(username);
        if (userEntity == null) {
            return null;
        }
        return userMapper.from(userEntity);
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
    public void updatePassword(String usernameOrEmail, String encodedPassword) throws UserRetrievalException,
    MultipleUserAccountsException {
        UserEntity userEntity = this.getEntityByNameOrEmail(usernameOrEmail);
        userEntity.setPassword(encodedPassword);
        userEntity.setPasswordResetRequired(false);
        super.update(userEntity);
    }

    @Transactional
    public void updateFailedLoginCount(String usernameOrEmail, int failedLoginCount) throws UserRetrievalException,
    MultipleUserAccountsException {
        UserEntity userEntity = this.getEntityByNameOrEmail(usernameOrEmail);
        userEntity.setFailedLoginCount(failedLoginCount);
        super.update(userEntity);
    }

    @Transactional
    public void updateAccountLockedStatus(String usernameOrEmail, boolean locked) throws UserRetrievalException,
    MultipleUserAccountsException {
        UserEntity userEntity = this.getEntityByNameOrEmail(usernameOrEmail);
        userEntity.setAccountLocked(locked);
        super.update(userEntity);
    }

    public String getEncodedPassword(UserDTO user) throws UserRetrievalException, MultipleUserAccountsException {
        UserEntity userEntity = getEntityByNameOrEmail(user.getEmail());
        return userEntity.getPassword();
    }
}

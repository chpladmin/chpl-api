package gov.healthit.chpl.dao.auth;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.caching.CacheNames;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.auth.User;
import gov.healthit.chpl.entity.auth.UserEntity;
import gov.healthit.chpl.exception.UserRetrievalException;
import jakarta.persistence.Query;

@Repository(value = "userDAO")
public class UserDAO extends BaseDAOImpl {

    public User getById(Long userId) throws UserRetrievalException {
        return getById(userId, false);
    }

    //This should only be used for populating pre-cognito activity records at this point
    @Cacheable(value = CacheNames.CHPL_USERS, key = "#userId")
    public User getById(Long userId, boolean includeDelete) throws UserRetrievalException {
        UserEntity userEntity = this.getEntityById(userId, includeDelete);
        if (userEntity == null) {
            return null;
        }
        return userEntity.toDomain();
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
}

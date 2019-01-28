package gov.healthit.chpl.auth.dao.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.auth.BaseDAOImpl;
import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.auth.dao.UserResetTokenDAO;
import gov.healthit.chpl.auth.dto.UserResetTokenDTO;
import gov.healthit.chpl.auth.entity.UserResetTokenEntity;

@Repository(value = "userResetTokenDAO")
public class UserResetTokenDAOImpl extends BaseDAOImpl implements UserResetTokenDAO {
    public UserResetTokenDTO create(final String resetToken, final Long userId) {
        UserResetTokenEntity entity = new UserResetTokenEntity();
        entity.setUserResetToken(resetToken);
        entity.setUserId(userId);
        if (Util.getCurrentUser() != null) {
            entity.setLastModifiedUser(Util.getCurrentUser().getId());
        } else {
            entity.setLastModifiedUser(userId);
        }
        entity.setDeleted(false);

        create(entity);
        return new UserResetTokenDTO(entity);
    }

    public void deletePreviousUserTokens(final Long userId) {
        List<UserResetTokenEntity> entities = getAllEntitiesByUserId(userId);

        for (UserResetTokenEntity entity : entities) {
            entity.setDeleted(true);
            update(entity);
        }
    }

    public UserResetTokenDTO findByAuthToken(final String authToken) {
        UserResetTokenDTO userResetToken = null;
        String userQuery = "from UserResetTokenEntity urt" + " where (NOT urt.deleted = true) "
                + " AND (urt.userResetToken = :userResetToken) ";

        Query query = entityManager.createQuery(userQuery, UserResetTokenEntity.class);
        query.setParameter("userResetToken", authToken);
        List<UserResetTokenEntity> result = query.getResultList();
        if (result.size() == 1) {
            UserResetTokenEntity entity = result.get(0);
            userResetToken = new UserResetTokenDTO(entity);
        }
        return userResetToken;
    }

    private List<UserResetTokenEntity> getAllEntitiesByUserId(final Long id) {
        String userQuery = "from UserResetTokenEntity urt" + " where (NOT urt.deleted = true) "
                + " AND (urt.userId = :userId) ";

        Query query = entityManager.createQuery(userQuery, UserResetTokenEntity.class);
        query.setParameter("userId", id);

        List<UserResetTokenEntity> entities = new ArrayList<UserResetTokenEntity>();
        if (query.getResultList() != null) {
            entities = query.getResultList();
        }
        return entities;
    }

    private void create(final UserResetTokenEntity userResetToken) {
        entityManager.persist(userResetToken);
        entityManager.flush();
    }

    private void update(final UserResetTokenEntity userResetToken) {
        entityManager.merge(userResetToken);
        entityManager.flush();
    }

}

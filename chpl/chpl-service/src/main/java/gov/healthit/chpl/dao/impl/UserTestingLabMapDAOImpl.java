package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.auth.entity.UserEntity;
import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.dao.UserTestingLabMapDAO;
import gov.healthit.chpl.dto.UserTestingLabMapDTO;
import gov.healthit.chpl.entity.TestingLabEntity;
import gov.healthit.chpl.entity.UserTestingLabMapEntity;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Repository(value = "userTestingLabMapDAO")
public class UserTestingLabMapDAOImpl extends BaseDAOImpl implements UserTestingLabMapDAO {
    private ErrorMessageUtil errorMessageUtil;

    @Autowired
    public UserTestingLabMapDAOImpl(final ErrorMessageUtil errorMessageUtil) {
        this.errorMessageUtil = errorMessageUtil;
    }

    @Override
    public UserTestingLabMapDTO create(UserTestingLabMapDTO dto) throws EntityRetrievalException {
        UserTestingLabMapEntity entity = new UserTestingLabMapEntity();
        entity = create(getUserTestingLabMapEntity(dto));
        return new UserTestingLabMapDTO(entity);
    }

    @Override
    public UserTestingLabMapDTO update(UserTestingLabMapDTO dto) throws EntityRetrievalException {
        UserTestingLabMapEntity entity = new UserTestingLabMapEntity();
        entity = update(getEntityById(dto.getId()));
        return new UserTestingLabMapDTO(entity);
    }

    public void delete(UserTestingLabMapDTO dto) throws EntityRetrievalException {
        update(getEntityById(dto.getId()));
    }

    @Override
    public List<UserTestingLabMapDTO> getByUserId(Long userId) {
        Query query = entityManager
                .createQuery(
                        "from UserTestingLabMapEntity utlm " + "join fetch utlm.testingLab tl "
                                + "join fetch utlm.user u " + "where (utlm.deleted != true) AND (u.id = :userId) ",
                        UserTestingLabMapEntity.class);
        query.setParameter("userId", userId);
        List<UserTestingLabMapEntity> result = query.getResultList();

        List<UserTestingLabMapDTO> dtos = new ArrayList<UserTestingLabMapDTO>();
        if (result != null) {
            for (UserTestingLabMapEntity entity : result) {
                dtos.add(new UserTestingLabMapDTO(entity));
            }
        }
        return dtos;
    }

    @Override
    public List<UserTestingLabMapDTO> getByAtlId(Long atlId) {
        Query query = entityManager.createQuery(
                "from UserTestingLabMapEntity utlm " + "join fetch utlm.testingLab tl "
                        + "join fetch utlm.user u where (utlm.deleted != true) AND (tl.id = :atlId) ",
                UserTestingLabMapEntity.class);
        query.setParameter("atlId", atlId);
        List<UserTestingLabMapEntity> result = query.getResultList();

        List<UserTestingLabMapDTO> dtos = new ArrayList<UserTestingLabMapDTO>();
        for (UserTestingLabMapEntity entity : result) {
            dtos.add(new UserTestingLabMapDTO(entity));
        }
        return dtos;
    }

    @Override
    public UserTestingLabMapDTO getById(Long id) {
        UserTestingLabMapEntity result = getEntityById(id);

        if (result == null) {
            return null;
        }
        return new UserTestingLabMapDTO(result);
    }

    private UserTestingLabMapEntity getEntityById(Long id) {
        Query query = entityManager.createQuery(
                "from UserTestingLabMapEntity utlm " + "join fetch utlm.testingLab TestingLabEntity "
                        + "join fetch utlm.user UserEntity " + "where (utlm.deleted != true) AND (utlm.id = :id) ",
                UserTestingLabMapEntity.class);
        query.setParameter("id", id);
        List<UserTestingLabMapEntity> result = query.getResultList();

        if (result.size() == 0) {
            return null;
        }
        return (UserTestingLabMapEntity) result.get(0);
    }

    private UserTestingLabMapEntity create(UserTestingLabMapEntity entity) {
        entity.setLastModifiedUser(getUserId(User.SYSTEM_USER_ID));
        if (entity.getRetired() == null) {
            entity.setRetired(false);
        }
        if (entity.getDeleted() == null) {
            entity.setDeleted(false);
        }
        entityManager.persist(entity);
        return entity;
    }

    private UserTestingLabMapEntity update(UserTestingLabMapEntity entity) {
        entity.setLastModifiedUser(getUserId(User.SYSTEM_USER_ID));
        entityManager.persist(entity);
        return entity;
    }

    private TestingLabEntity getAtlEntityById(final Long entityId) throws EntityRetrievalException {
        TestingLabEntity entity = null;

        String queryStr = "SELECT atl from TestingLabEntity atl " + "LEFT OUTER JOIN FETCH atl.address "
                + "WHERE (atl.id = :entityid)" + " AND (atl.deleted = false)";

        Query query = entityManager.createQuery(queryStr, TestingLabEntity.class);
        query.setParameter("entityid", entityId);
        List<TestingLabEntity> result = query.getResultList();

        if (result == null || result.size() == 0) {
            String msg = errorMessageUtil.getMessage("atl.notFound");
            throw new EntityRetrievalException(msg);
        } else if (result.size() > 1) {
            throw new EntityRetrievalException("Data error. Duplicate certificaiton body id in database.");
        } else if (result.size() == 1) {
            entity = result.get(0);
        }
        return entity;
    }

    private UserEntity getUserEntityById(final Long userId) throws EntityRetrievalException {

        UserEntity user = null;

        Query query = entityManager.createQuery(
                "from UserEntity where (NOT deleted = true) " + "AND (user_id = :userid) ", UserEntity.class);
        query.setParameter("userid", userId);
        List<UserEntity> result = query.getResultList();

        if (result == null || result.size() == 0) {
            String msg = errorMessageUtil.getMessage("user.notFound");
            throw new EntityRetrievalException(msg);
        } else if (result.size() > 1) {
            throw new EntityRetrievalException("Data error. Duplicate user id in database.");
        }

        if (result.size() == 0) {
            return null;
        }
        return result.get(0);
    }

    private UserTestingLabMapEntity getUserTestingLabMapEntity(UserTestingLabMapDTO dto)
            throws EntityRetrievalException {
        UserTestingLabMapEntity entity = new UserTestingLabMapEntity();
        entity.setId(dto.getId());
        entity.setTestingLab(getAtlEntityById(dto.getTestingLab().getId()));
        entity.setUser(getUserEntityById(dto.getUser().getId()));
        entity.setRetired(dto.getRetired());
        return entity;
    }

}

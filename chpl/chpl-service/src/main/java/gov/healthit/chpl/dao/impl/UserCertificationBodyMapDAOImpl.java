package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.auth.entity.UserEntity;
import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.dao.UserCertificationBodyMapDAO;
import gov.healthit.chpl.dto.UserCertificationBodyMapDTO;
import gov.healthit.chpl.entity.CertificationBodyEntity;
import gov.healthit.chpl.entity.UserCertificationBodyMapEntity;
import gov.healthit.chpl.exception.EntityRetrievalException;

@Repository(value = "userCertificationBodyMapDAO")
public class UserCertificationBodyMapDAOImpl extends BaseDAOImpl implements UserCertificationBodyMapDAO {

    @Override
    public UserCertificationBodyMapDTO create(UserCertificationBodyMapDTO dto) throws EntityRetrievalException {
        UserCertificationBodyMapEntity entity = new UserCertificationBodyMapEntity();
        entity = create(getUserCertificationBodyMapEntity(dto));
        return new UserCertificationBodyMapDTO(entity);
    }

    @Override
    public void delete(UserCertificationBodyMapDTO dto) throws EntityRetrievalException {
        UserCertificationBodyMapEntity entity = getEntityById(dto.getId());
        entity.setDeleted(true);
        entity.setLastModifiedUser(getUserId(User.SYSTEM_USER_ID));
        update(entity);
    }

    @Override
    public List<UserCertificationBodyMapDTO> getByUserId(Long userId) {
        Query query = entityManager.createQuery(
                "from UserCertificationBodyMapEntity ucbm " + "join fetch ucbm.certificationBody cb "
                        + "join fetch ucbm.user u " + "where (ucbm.deleted != true) AND (u.id = :userId) ",
                UserCertificationBodyMapEntity.class);
        query.setParameter("userId", userId);
        List<UserCertificationBodyMapEntity> result = query.getResultList();

        List<UserCertificationBodyMapDTO> dtos = new ArrayList<UserCertificationBodyMapDTO>();
        if (result != null) {
            for (UserCertificationBodyMapEntity entity : result) {
                dtos.add(new UserCertificationBodyMapDTO(entity));
            }
        }
        return dtos;
    }

    @Override
    public List<UserCertificationBodyMapDTO> getByAcbId(Long acbId) {
        Query query = entityManager.createQuery(
                "from UserCertificationBodyMapEntity ucbm " + "join fetch ucbm.certificationBody cb "
                        + "join fetch ucbm.user u where (ucbm.deleted != true) AND (cb.id = :acbId) ",
                UserCertificationBodyMapEntity.class);
        query.setParameter("acbId", acbId);
        List<UserCertificationBodyMapEntity> result = query.getResultList();

        List<UserCertificationBodyMapDTO> dtos = new ArrayList<UserCertificationBodyMapDTO>();
        for (UserCertificationBodyMapEntity entity : result) {
            dtos.add(new UserCertificationBodyMapDTO(entity));
        }
        return dtos;
    }

    @Override
    public UserCertificationBodyMapDTO getById(Long id) {
        Query query = entityManager.createQuery(
                "from UserCertificationBodyMapEntity ucbm "
                        + "join fetch ucbm.certificationBody CertificationBodyEntity "
                        + "join fetch ucbm.user UserEntity" + "where (ucbm.deleted != true) AND (ucbm.id = :id) ",
                UserCertificationBodyMapEntity.class);
        query.setParameter("id", id);
        List<UserCertificationBodyMapEntity> result = query.getResultList();

        if (result.size() == 0) {
            return null;
        }
        return new UserCertificationBodyMapDTO(result.get(0));
    }

    private UserCertificationBodyMapEntity getEntityById(Long id) {
        Query query = entityManager.createQuery(
                "from UserCertificationBodyMapEntity ucbm "
                        + "join fetch ucbm.certificationBody CertificationBodyEntity "
                        + "join fetch ucbm.user UserEntity " + "where (ucbm.deleted != true) AND (ucbm.id = :id) ",
                UserCertificationBodyMapEntity.class);
        query.setParameter("id", id);
        List<UserCertificationBodyMapEntity> result = query.getResultList();

        if (result.size() == 0) {
            return null;
        }
        return (UserCertificationBodyMapEntity) result.get(0);
    }

    private UserCertificationBodyMapEntity create(UserCertificationBodyMapEntity entity) {
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

    private UserCertificationBodyMapEntity update(UserCertificationBodyMapEntity entity) {
        entity.setLastModifiedUser(getUserId(User.SYSTEM_USER_ID));
        entityManager.persist(entity);
        return entity;
    }

    private CertificationBodyEntity getAcbEntityById(final Long entityId) throws EntityRetrievalException {
        CertificationBodyEntity entity = null;

        String queryStr = "SELECT acb from CertificationBodyEntity acb " + "LEFT OUTER JOIN FETCH acb.address "
                + "WHERE (acb.id = :entityid)" + " AND (acb.deleted = false)";

        Query query = entityManager.createQuery(queryStr, CertificationBodyEntity.class);
        query.setParameter("entityid", entityId);
        List<CertificationBodyEntity> result = query.getResultList();

        if (result == null || result.size() == 0) {
            String msg = String.format(messageSource.getMessage(new DefaultMessageSourceResolvable("acb.notFound"),
                    LocaleContextHolder.getLocale()));
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
            String msg = String.format(messageSource.getMessage(new DefaultMessageSourceResolvable("user.notFound"),
                    LocaleContextHolder.getLocale()));
            throw new EntityRetrievalException(msg);
        } else if (result.size() > 1) {
            throw new EntityRetrievalException("Data error. Duplicate user id in database.");
        }

        if (result.size() == 0) {
            return null;
        }
        return result.get(0);
    }

    private UserCertificationBodyMapEntity getUserCertificationBodyMapEntity(UserCertificationBodyMapDTO dto)
            throws EntityRetrievalException {
        UserCertificationBodyMapEntity entity = new UserCertificationBodyMapEntity();
        entity.setId(dto.getId());
        entity.setCertificationBody(getAcbEntityById(dto.getCertificationBody().getId()));
        entity.setUser(getUserEntityById(dto.getUser().getId()));
        entity.setRetired(dto.getRetired());
        return entity;
    }

}

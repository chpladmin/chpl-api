package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.dao.ParticipantAgeStatisticsDAO;
import gov.healthit.chpl.dto.ParticipantAgeStatisticsDTO;
import gov.healthit.chpl.entity.statistics.ParticipantAgeStatisticsEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;

/**
 * The implementation for ParticipantAgeStatisticsDAO.
 * @author TYoung
 *
 */
@Repository("participantAgeStatisticsDAO")
public class ParticipantAgeStatisticsDAOImpl extends BaseDAOImpl implements ParticipantAgeStatisticsDAO {

    @Override
    public List<ParticipantAgeStatisticsDTO> findAll() {
        List<ParticipantAgeStatisticsEntity> result = this.findAllEntities();
        List<ParticipantAgeStatisticsDTO> dtos = new ArrayList<ParticipantAgeStatisticsDTO>(result.size());
        for (ParticipantAgeStatisticsEntity entity : result) {
            dtos.add(new ParticipantAgeStatisticsDTO(entity));
        }
        return dtos;
    }

    @Override
    @Transactional
    public void delete(final Long id) throws EntityRetrievalException {
        ParticipantAgeStatisticsEntity toDelete = getEntityById(id);

        if (toDelete != null) {
            toDelete.setDeleted(true);
            toDelete.setLastModifiedUser(getUserId(User.SYSTEM_USER_ID));
            entityManager.merge(toDelete);
        }
    }

    @Override
    @Transactional
    public ParticipantAgeStatisticsEntity create(final ParticipantAgeStatisticsDTO dto)
            throws EntityCreationException, EntityRetrievalException {
        ParticipantAgeStatisticsEntity entity = new ParticipantAgeStatisticsEntity();
        entity.setAgeCount(dto.getAgeCount());
        entity.setTestParticipantAgeId(dto.getTestParticipantAgeId());

        if (dto.getDeleted() != null) {
            entity.setDeleted(dto.getDeleted());
        } else {
            entity.setDeleted(false);
        }

        if (dto.getLastModifiedUser() != null) {
            entity.setLastModifiedUser(dto.getLastModifiedUser());
        } else {
            entity.setLastModifiedUser(getUserId(User.SYSTEM_USER_ID));
        }

        entityManager.persist(entity);
        entityManager.flush();
        return entity;
    }

    private List<ParticipantAgeStatisticsEntity> findAllEntities() {
        Query query = entityManager
                .createQuery("SELECT a from ParticipantAgeStatisticsEntity a where (NOT a.deleted = true)");
        return query.getResultList();
    }

    private ParticipantAgeStatisticsEntity getEntityById(final Long id) throws EntityRetrievalException {
        ParticipantAgeStatisticsEntity entity = null;

        Query query = entityManager.createQuery(
                "from ParticipantAgeStatisticsEntity a where (NOT deleted = true) AND (id = :entityid) ",
                ParticipantAgeStatisticsEntity.class);
        query.setParameter("entityid", id);
        List<ParticipantAgeStatisticsEntity> result = query.getResultList();

        if (result.size() > 1) {
            throw new EntityRetrievalException("Data error. Duplicate id in database.");
        } else if (result.size() == 1) {
            entity = result.get(0);
        }

        return entity;
    }
}

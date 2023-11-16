package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.dao.SedParticipantStatisticsCountDAO;
import gov.healthit.chpl.dto.SedParticipantStatisticsCountDTO;
import gov.healthit.chpl.entity.statistics.SedParticipantStatisticsCountEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;

/**
 * The implementation for SedParticipantStatisticsCountDAO.
 * @author TYoung
 *
 */
@Repository("sedParticipantStatisticsCountDAO")
public class SedParticipantsStatisticsCountDAOImpl extends BaseDAOImpl implements SedParticipantStatisticsCountDAO {

    @Override
    public List<SedParticipantStatisticsCountDTO> findAll() {
        List<SedParticipantStatisticsCountEntity> result = this.findAllEntities();
        List<SedParticipantStatisticsCountDTO> dtos = new ArrayList<SedParticipantStatisticsCountDTO>(result.size());
        for (SedParticipantStatisticsCountEntity entity : result) {
            dtos.add(new SedParticipantStatisticsCountDTO(entity));
        }
        return dtos;
    }

    @Override
    @Transactional
    public void delete(final Long id) throws EntityRetrievalException {
        SedParticipantStatisticsCountEntity toDelete = getEntityById(id);

        if (toDelete != null) {
            toDelete.setDeleted(true);
            toDelete.setLastModifiedUser(getUserId(User.SYSTEM_USER_ID));
            entityManager.merge(toDelete);
        }
    }

    @Override
    @Transactional
    public SedParticipantStatisticsCountEntity create(final SedParticipantStatisticsCountDTO dto)
            throws EntityCreationException, EntityRetrievalException {

        SedParticipantStatisticsCountEntity entity = new SedParticipantStatisticsCountEntity();
        entity.setSedCount(dto.getSedCount());
        entity.setParticipantCount(dto.getParticipantCount());

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

    private SedParticipantStatisticsCountEntity getEntityById(final Long id) throws EntityRetrievalException {
        SedParticipantStatisticsCountEntity entity = null;

        Query query = entityManager.createQuery(
                "from SedParticipantStatisticsCountEntity a where (NOT deleted = true) AND (id = :entityid) ",
                SedParticipantStatisticsCountEntity.class);
        query.setParameter("entityid", id);
        List<SedParticipantStatisticsCountEntity> result = query.getResultList();

        if (result.size() > 1) {
            throw new EntityRetrievalException("Data error. Duplicate address id in database.");
        } else if (result.size() == 1) {
            entity = result.get(0);
        }

        return entity;
    }

    private List<SedParticipantStatisticsCountEntity> findAllEntities() {
        Query query = entityManager
                .createQuery("SELECT a from SedParticipantStatisticsCountEntity a where (NOT a.deleted = true)");
        return query.getResultList();
    }
}

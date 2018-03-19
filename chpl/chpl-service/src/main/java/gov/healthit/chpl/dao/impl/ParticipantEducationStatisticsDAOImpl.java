package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.ParticipantEducationStatisticsDAO;
import gov.healthit.chpl.dto.ParticipantEducationStatisticsDTO;
import gov.healthit.chpl.entity.ParticipantEducationStatisticsEntity;

/**
 * The implementation for ParticipantEducationStatisticsDAO.
 * @author TYoung
 *
 */
@Repository("participantEducationStatisticsDAO")
public class ParticipantEducationStatisticsDAOImpl extends BaseDAOImpl implements ParticipantEducationStatisticsDAO {
    private static final long MODIFIED_USER_ID = -3L;

    @Override
    public List<ParticipantEducationStatisticsDTO> findAll() {
        List<ParticipantEducationStatisticsEntity> result = this.findAllEntities();
        List<ParticipantEducationStatisticsDTO> dtos = new ArrayList<ParticipantEducationStatisticsDTO>(result.size());
        for (ParticipantEducationStatisticsEntity entity : result) {
            dtos.add(new ParticipantEducationStatisticsDTO(entity));
        }
        return dtos;
    }

    @Override
    public void delete(final Long id) throws EntityRetrievalException {
        ParticipantEducationStatisticsEntity toDelete = getEntityById(id);

        if (toDelete != null) {
            toDelete.setDeleted(true);
            toDelete.setLastModifiedUser(getUserId());
            entityManager.merge(toDelete);
        }

    }

    @Override
    public ParticipantEducationStatisticsEntity create(final ParticipantEducationStatisticsDTO dto)
            throws EntityCreationException, EntityRetrievalException {
        ParticipantEducationStatisticsEntity entity = new ParticipantEducationStatisticsEntity();
        entity.setEducationCount(dto.getEducationCount());
        entity.setEducationTypeId(dto.getEducationTypeId());

        if (dto.getDeleted() != null) {
            entity.setDeleted(dto.getDeleted());
        } else {
            entity.setDeleted(false);
        }

        if (dto.getLastModifiedUser() != null) {
            entity.setLastModifiedUser(dto.getLastModifiedUser());
        } else {
            entity.setLastModifiedUser(getUserId());
        }
        if (dto.getLastModifiedDate() != null) {
            entity.setLastModifiedDate(dto.getLastModifiedDate());
        } else {
            entity.setLastModifiedDate(new Date());
        }
        if (dto.getCreationDate() != null) {
            entity.setCreationDate(dto.getCreationDate());
        } else {
            entity.setCreationDate(new Date());
        }

        entityManager.persist(entity);
        entityManager.flush();
        return entity;
    }

    private List<ParticipantEducationStatisticsEntity> findAllEntities() {
        Query query = entityManager
                .createQuery("SELECT a from ParticipantEducationStatisticsEntity a where (NOT a.deleted = true)");
        return query.getResultList();
    }

    private ParticipantEducationStatisticsEntity getEntityById(final Long id) throws EntityRetrievalException {
        ParticipantEducationStatisticsEntity entity = null;

        Query query = entityManager.createQuery(
                "from ParticipantEducationStatisticsEntity a where (NOT deleted = true) AND (id = :entityid) ",
                ParticipantEducationStatisticsEntity.class);
        query.setParameter("entityid", id);
        List<ParticipantEducationStatisticsEntity> result = query.getResultList();

        if (result.size() > 1) {
            throw new EntityRetrievalException("Data error. Duplicate id in database.");
        } else if (result.size() == 1) {
            entity = result.get(0);
        }

        return entity;
    }

    private Long getUserId() {
        // If there is no user the current context, assume this is a system
        // process
        if (Util.getCurrentUser() == null || Util.getCurrentUser().getId() == null) {
            return MODIFIED_USER_ID;
        } else {
            return Util.getCurrentUser().getId();
        }
    }

}

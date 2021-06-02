package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.dao.ParticipantExperienceStatisticsDAO;
import gov.healthit.chpl.dto.ParticipantExperienceStatisticsDTO;
import gov.healthit.chpl.entity.statistics.ParticipantExperienceStatisticsEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;

/**
 * The implementation for ParticipantExperienceStatisticsDAO.
 * @author TYoung
 *
 */
@Repository("participantExperienceStatisticsDAO")
public class ParticipantExperienceStatisticsDAOImpl extends BaseDAOImpl implements ParticipantExperienceStatisticsDAO {

    @Override
    public List<ParticipantExperienceStatisticsDTO> findAll(final Long experienceTypeId) {
        List<ParticipantExperienceStatisticsEntity> result = this.findAllEntities(experienceTypeId);
        List<ParticipantExperienceStatisticsDTO> dtos =
                new ArrayList<ParticipantExperienceStatisticsDTO>(result.size());
        for (ParticipantExperienceStatisticsEntity entity : result) {
            dtos.add(new ParticipantExperienceStatisticsDTO(entity));
        }
        return dtos;
    }

    @Override
    @Transactional
    public void delete(final Long id) throws EntityRetrievalException {
        ParticipantExperienceStatisticsEntity toDelete = getEntityById(id);

        if (toDelete != null) {
            toDelete.setDeleted(true);
            toDelete.setLastModifiedUser(getUserId(User.SYSTEM_USER_ID));
            entityManager.merge(toDelete);
        }
    }

    @Override
    @Transactional
    public ParticipantExperienceStatisticsEntity create(final ParticipantExperienceStatisticsDTO dto)
            throws EntityCreationException, EntityRetrievalException {
        ParticipantExperienceStatisticsEntity entity = new ParticipantExperienceStatisticsEntity();

        entity.setParticipantCount(dto.getParticipantCount());
        entity.setExperienceTypeId(dto.getExperienceTypeId());
        entity.setExperienceMonths(dto.getExperienceMonths());

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

    private List<ParticipantExperienceStatisticsEntity> findAllEntities(final Long experienceTypeId) {
        Query query = entityManager.createQuery(
                "from ParticipantExperienceStatisticsEntity a where (NOT deleted = true) "
                + "AND (experience_type_id = :experienceTypeId)");
        query.setParameter("experienceTypeId", experienceTypeId);
        return query.getResultList();
    }

    private ParticipantExperienceStatisticsEntity getEntityById(final Long id) throws EntityRetrievalException {
        ParticipantExperienceStatisticsEntity entity = null;

        Query query = entityManager.createQuery(
                "from ParticipantExperienceStatisticsEntity a where (NOT deleted = true) AND (id = :entityid) ",
                ParticipantExperienceStatisticsEntity.class);
        query.setParameter("entityid", id);
        List<ParticipantExperienceStatisticsEntity> result = query.getResultList();

        if (result.size() > 1) {
            throw new EntityRetrievalException("Data error. Duplicate id in database.");
        } else if (result.size() == 1) {
            entity = result.get(0);
        }

        return entity;
    }
}

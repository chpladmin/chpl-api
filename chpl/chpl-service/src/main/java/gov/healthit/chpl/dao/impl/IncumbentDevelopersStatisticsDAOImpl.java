package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.dao.IncumbentDevelopersStatisticsDAO;
import gov.healthit.chpl.dto.IncumbentDevelopersStatisticsDTO;
import gov.healthit.chpl.entity.IncumbentDevelopersStatisticsEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;

/**
 * The implementation for IncumbentDevelopersStatisticsDAO.
 * @author alarned
 *
 */
@Repository("incumbentDevelopersStatisticsDAO")
public class IncumbentDevelopersStatisticsDAOImpl extends BaseDAOImpl implements IncumbentDevelopersStatisticsDAO {

    @Override
    public List<IncumbentDevelopersStatisticsDTO> findAll() {
        List<IncumbentDevelopersStatisticsEntity> result = this.findAllEntities();
        List<IncumbentDevelopersStatisticsDTO> dtos = new ArrayList<IncumbentDevelopersStatisticsDTO>(result.size());
        for (IncumbentDevelopersStatisticsEntity entity : result) {
            dtos.add(new IncumbentDevelopersStatisticsDTO(entity));
        }
        return dtos;
    }

    @Override
    @Transactional
    public void delete(final Long id) throws EntityRetrievalException {
        IncumbentDevelopersStatisticsEntity toDelete = getEntityById(id);

        if (toDelete != null) {
            toDelete.setDeleted(true);
            toDelete.setLastModifiedUser(getUserId(User.SYSTEM_USER_ID));
            entityManager.merge(toDelete);
        }
    }

    @Override
    @Transactional
    public IncumbentDevelopersStatisticsEntity create(final IncumbentDevelopersStatisticsDTO dto)
            throws EntityCreationException, EntityRetrievalException {
        IncumbentDevelopersStatisticsEntity entity = new IncumbentDevelopersStatisticsEntity();
        entity.setNewCount(dto.getNewCount());
        entity.setIncumbentCount(dto.getIncumbentCount());
        entity.setOldCertificationEditionId(dto.getOldCertificationEditionId());
        entity.setNewCertificationEditionId(dto.getNewCertificationEditionId());

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

    private List<IncumbentDevelopersStatisticsEntity> findAllEntities() {
        Query query = entityManager.createQuery("from IncumbentDevelopersStatisticsEntity idse "
                + "LEFT OUTER JOIN FETCH idse.oldCertificationEdition "
                + "LEFT OUTER JOIN FETCH idse.newCertificationEdition "
                + "where (idse.deleted = false)",
                IncumbentDevelopersStatisticsEntity.class);
        return query.getResultList();
    }

    private IncumbentDevelopersStatisticsEntity getEntityById(final Long id) throws EntityRetrievalException {
        IncumbentDevelopersStatisticsEntity entity = null;

        Query query = entityManager.createQuery("from IncumbentDevelopersStatisticsEntity idse "
                + "LEFT OUTER JOIN FETCH idse.oldCertificationEdition "
                + "LEFT OUTER JOIN FETCH idse.newCertificationEdition "
                + "where (idse.deleted = false) AND (idse.id = :entityid) ",
                IncumbentDevelopersStatisticsEntity.class);
        query.setParameter("entityid", id);
        List<IncumbentDevelopersStatisticsEntity> result = query.getResultList();

        if (result.size() > 1) {
            throw new EntityRetrievalException("Data error. Duplicate address id in database.");
        } else if (result.size() == 1) {
            entity = result.get(0);
        }

        return entity;
    }
}

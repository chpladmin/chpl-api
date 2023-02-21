package gov.healthit.chpl.dao;

import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.dto.CuresUpdateEventDTO;
import gov.healthit.chpl.entity.listing.CuresUpdateEventEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.AuthUtil;

@Repository("curesUpdateEventDAO")
public class CuresUpdateEventDAO  extends BaseDAOImpl {

    public Long create(CuresUpdateEventDTO curesUpdateEvent) throws EntityCreationException {
        try {
            CuresUpdateEventEntity entity = new CuresUpdateEventEntity();
            entity.setCertifiedProductId(curesUpdateEvent.getCertifiedProductId());
            entity.setCuresUpdate(curesUpdateEvent.getCuresUpdate());
            entity.setEventDate(curesUpdateEvent.getEventDate());
            entity.setLastModifiedUser(AuthUtil.getAuditId());
            entity.setDeleted(false);
            create(entity);
            return entity.getId();
        } catch (Exception ex) {
            throw new EntityCreationException(ex);
        }
    }

    public CuresUpdateEventDTO getById(Long id) throws EntityRetrievalException {
        CuresUpdateEventDTO dto = null;
        CuresUpdateEventEntity entity = getEntityById(id);

        if (entity != null) {
            dto = new CuresUpdateEventDTO(entity);
        }
        return dto;
    }

    private CuresUpdateEventEntity getEntityById(Long id) throws EntityRetrievalException {
        CuresUpdateEventEntity entity = null;

        Query query = entityManager.createQuery(
                "FROM CuresUpdateEventEntity cue "
                        + "WHERE cue.id = :entityid " + "AND (NOT cue.deleted = true)",
                        CuresUpdateEventEntity.class);
        query.setParameter("entityid", id);
        @SuppressWarnings("unchecked") List<CuresUpdateEventEntity> result = query.getResultList();

        if (result.size() > 1) {
            throw new EntityRetrievalException("Data error. Duplicate certification event id in database.");
        }

        if (result.size() > 0) {
            entity = result.get(0);
        }

        return entity;
    }

    private List<CuresUpdateEventEntity> getEntitiesByCertifiedProductId(Long id) {
        Query query = entityManager.createQuery("FROM CuresUpdateEventEntity cue "
                + "WHERE cue.certifiedProductId = :cpId "
                + "AND (NOT cue.deleted = true) ", CuresUpdateEventEntity.class);
        query.setParameter("cpId", id);
        @SuppressWarnings("unchecked") List<CuresUpdateEventEntity> result = query.getResultList();

        return result;
    }
}

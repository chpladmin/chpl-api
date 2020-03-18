package gov.healthit.chpl.dao;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.dto.CuresUpdateEventDTO;
import gov.healthit.chpl.entity.listing.CuresUpdateEventEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.AuthUtil;

public class CuresUpdateEventDAO  extends BaseDAOImpl {

    public CuresUpdateEventDTO create(CuresUpdateEventDTO dto)
            throws EntityCreationException, EntityRetrievalException {
        CuresUpdateEventEntity entity = null;
        try {
            if (dto.getId() != null) {
                entity = this.getEntityById(dto.getId());
            }
        } catch (final EntityRetrievalException e) {
            throw new EntityCreationException(e);
        }

        if (entity != null) {
            throw new EntityCreationException("An entity with this ID already exists.");
        } else {
            entity = new CuresUpdateEventEntity();
            entity.setCertifiedProductId(dto.getCertifiedProductId());
            entity.setCuresUpdate(dto.getCuresUpdate());
            entity.setEventDate(dto.getEventDate());
            entity.setLastModifiedUser(AuthUtil.getAuditId());
            entity.setDeleted(false);
            create(entity);
            return new CuresUpdateEventDTO(entity);
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

    public List<CuresUpdateEventDTO> findByCertifiedProductId(Long certifiedProductId) {
        List<CuresUpdateEventEntity> entities = getEntitiesByCertifiedProductId(certifiedProductId);
        List<CuresUpdateEventDTO> dtos = new ArrayList<>();

        for (CuresUpdateEventEntity entity : entities) {
            CuresUpdateEventDTO dto = new CuresUpdateEventDTO(entity);
            dtos.add(dto);
        }
        return dtos;
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

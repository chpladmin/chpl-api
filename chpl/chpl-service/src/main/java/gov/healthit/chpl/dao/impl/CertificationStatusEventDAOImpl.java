package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.dao.CertificationStatusEventDAO;
import gov.healthit.chpl.dto.CertificationStatusEventDTO;
import gov.healthit.chpl.entity.listing.CertificationStatusEventEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;

@Repository("certificationStatusEventDAO")
public class CertificationStatusEventDAOImpl extends BaseDAOImpl implements CertificationStatusEventDAO {

    @Override
    public CertificationStatusEventDTO create(CertificationStatusEventDTO dto)
            throws EntityCreationException, EntityRetrievalException {

        CertificationStatusEventEntity entity = null;
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
            entity = new CertificationStatusEventEntity();
            entity.setCertifiedProductId(dto.getCertifiedProductId());
            entity.setCertificationStatusId(dto.getStatus().getId());
            entity.setEventDate(dto.getEventDate());
            entity.setReason(dto.getReason());
            entity.setLastModifiedUser(Util.getAuditId());
            entity.setDeleted(false);
            create(entity);
            return new CertificationStatusEventDTO(entity);
        }
    }

    @Override
    public CertificationStatusEventDTO update(CertificationStatusEventDTO dto) throws EntityRetrievalException {
        CertificationStatusEventEntity entity = this.getEntityById(dto.getId());

        if (entity == null) {
            throw new EntityRetrievalException("Entity with id " + dto.getId() + " does not exist");
        }
        entity.setCertifiedProductId(dto.getCertifiedProductId());
        entity.setCertifiedProductId(dto.getCertifiedProductId());
        entity.setCertificationStatusId(dto.getStatus().getId());
        entity.setEventDate(dto.getEventDate());
        entity.setReason(dto.getReason());
        entity.setLastModifiedUser(Util.getAuditId());

        update(entity);
        return new CertificationStatusEventDTO(entity);
    }

    @Override
    public void delete(Long id) throws EntityRetrievalException {

        CertificationStatusEventEntity toDelete = getEntityById(id);

        if (toDelete != null) {
            toDelete.setDeleted(true);
            toDelete.setLastModifiedUser(Util.getAuditId());
            update(toDelete);
        }
    }

    @Override
    public CertificationStatusEventDTO getById(Long id) throws EntityRetrievalException {

        CertificationStatusEventDTO dto = null;
        CertificationStatusEventEntity entity = getEntityById(id);

        if (entity != null) {
            dto = new CertificationStatusEventDTO(entity);
        }
        return dto;
    }

    @Override
    public List<CertificationStatusEventDTO> findAll() {

        List<CertificationStatusEventEntity> entities = getAllEntities();
        List<CertificationStatusEventDTO> dtos = new ArrayList<>();

        for (CertificationStatusEventEntity entity : entities) {
            CertificationStatusEventDTO dto = new CertificationStatusEventDTO(entity);
            dtos.add(dto);
        }
        return dtos;

    }

    @Override
    public List<CertificationStatusEventDTO> findByCertifiedProductId(Long certifiedProductId) {

        List<CertificationStatusEventEntity> entities = getEntitiesByCertifiedProductId(certifiedProductId);
        List<CertificationStatusEventDTO> dtos = new ArrayList<>();

        for (CertificationStatusEventEntity entity : entities) {
            CertificationStatusEventDTO dto = new CertificationStatusEventDTO(entity);
            dtos.add(dto);
        }
        return dtos;
    }

    @Override
    public CertificationStatusEventDTO findInitialCertificationEventForCertifiedProduct(Long certifiedProductId) {
        CertificationStatusEventEntity certificationDateEvent = getOldestActiveEventForCertifiedProduct(
                certifiedProductId);
        CertificationStatusEventDTO certificationDateDto = new CertificationStatusEventDTO(certificationDateEvent);
        return certificationDateDto;
    }

    private void create(CertificationStatusEventEntity entity) {

        entityManager.persist(entity);
        entityManager.flush();

    }

    private void update(CertificationStatusEventEntity entity) {

        entityManager.merge(entity);
        entityManager.flush();
    }

    private List<CertificationStatusEventEntity> getAllEntities() {

        List<CertificationStatusEventEntity> result = entityManager.createQuery(
                "FROM CertificationStatusEventEntity cse " + "LEFT JOIN FETCH cse.certificationStatus s "
                        + "WHERE (NOT cse.deleted = true) " + "AND (NOT s.deleted = true)",
                CertificationStatusEventEntity.class).getResultList();
        return result;

    }

    private CertificationStatusEventEntity getEntityById(Long id) throws EntityRetrievalException {

        CertificationStatusEventEntity entity = null;

        Query query = entityManager.createQuery(
                "FROM CertificationStatusEventEntity cse " + "LEFT JOIN FETCH cse.certificationStatus s "
                        + "WHERE cse.id = :entityid " + "AND (NOT cse.deleted = true) " + "AND (NOT s.deleted = true)",
                CertificationStatusEventEntity.class);
        query.setParameter("entityid", id);
        List<CertificationStatusEventEntity> result = query.getResultList();

        if (result.size() > 1) {
            throw new EntityRetrievalException("Data error. Duplicate certification event id in database.");
        }

        if (result.size() > 0) {
            entity = result.get(0);
        }

        return entity;
    }

    private List<CertificationStatusEventEntity> getEntitiesByCertifiedProductId(Long id) {

        Query query = entityManager.createQuery("FROM CertificationStatusEventEntity cse "
                + "LEFT JOIN FETCH cse.certificationStatus s " + "WHERE cse.certifiedProductId = :cpId "
                + "AND (NOT cse.deleted = true) " + "AND (NOT s.deleted = true)", CertificationStatusEventEntity.class);
        query.setParameter("cpId", id);
        List<CertificationStatusEventEntity> result = query.getResultList();

        return result;
    }

    private CertificationStatusEventEntity getOldestActiveEventForCertifiedProduct(Long cpId) {
        Query query = entityManager.createQuery("FROM CertificationStatusEventEntity cse "
                + "LEFT JOIN FETCH cse.certificationStatus s " + "WHERE cse.certifiedProductId = :cpId "
                + "AND cse.certificationStatusId = 1 " + "AND (NOT cse.deleted = true) " + "AND (NOT s.deleted = true) "
                + "ORDER BY cse.eventDate ASC", CertificationStatusEventEntity.class);
        query.setParameter("cpId", cpId);
        List<CertificationStatusEventEntity> result = query.getResultList();
        if (result == null || result.size() == 0) {
            return null;
        }
        return result.get(0);
    }
}

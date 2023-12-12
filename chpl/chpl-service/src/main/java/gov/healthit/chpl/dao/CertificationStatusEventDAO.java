package gov.healthit.chpl.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.CertificationStatusEvent;
import gov.healthit.chpl.entity.listing.CertificationStatusEventEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;

@Repository("certificationStatusEventDAO")
public class CertificationStatusEventDAO extends BaseDAOImpl {

    public Long create(Long listingId, CertificationStatusEvent cse) throws EntityCreationException {
        try {
            CertificationStatusEventEntity entity = new CertificationStatusEventEntity();
            entity.setCertifiedProductId(listingId);
            entity.setCertificationStatusId(cse.getStatus().getId());
            entity.setEventDate(new Date(cse.getEventDate()));
            entity.setReason(cse.getReason());
            entity.setDeleted(false);
            create(entity);
            return entity.getId();
        } catch (Exception ex) {
            throw new EntityCreationException(ex);
        }
    }

    public CertificationStatusEvent update(Long listingId, CertificationStatusEvent cse) throws EntityRetrievalException {
        CertificationStatusEventEntity entity = this.getEntityById(cse.getId());

        if (entity == null) {
            throw new EntityRetrievalException("Entity with id " + cse.getId() + " does not exist");
        }
        entity.setCertifiedProductId(listingId);
        entity.setCertificationStatusId(cse.getStatus().getId());
        entity.setEventDate(new Date(cse.getEventDate()));
        entity.setReason(cse.getReason());

        update(entity);
        return entity.toDomain();
    }

    public void delete(Long id) throws EntityRetrievalException {
        CertificationStatusEventEntity toDelete = getEntityById(id);
        if (toDelete != null) {
            toDelete.setDeleted(true);
            update(toDelete);
        }
    }

    public CertificationStatusEvent getById(Long id) throws EntityRetrievalException {
        CertificationStatusEvent cse = null;
        CertificationStatusEventEntity entity = getEntityById(id);
        if (entity != null) {
            cse = entity.toDomain();
        }
        return cse;
    }

    public List<CertificationStatusEvent> findAll() {
        List<CertificationStatusEventEntity> entities = getAllEntities();
        List<CertificationStatusEvent> cses = new ArrayList<>();
        for (CertificationStatusEventEntity entity : entities) {
            cses.add(entity.toDomain());
        }
        return cses;
    }

    public Map<Long, List<CertificationStatusEvent>> findAllByListing() {
        List<CertificationStatusEventEntity> entities = getAllEntities();
        Map<Long, List<CertificationStatusEventEntity>> entityMap = entities.stream()
                .collect(Collectors.groupingBy(CertificationStatusEventEntity::getCertifiedProductId));

        Map<Long, List<CertificationStatusEvent>> syncdMap = new Hashtable<Long, List<CertificationStatusEvent>>();
        for (Long key : entityMap.keySet()) {
            List<CertificationStatusEvent> listingEvents = entityMap.get(key).stream()
                    .map(entity -> entity.toDomain())
                    .collect(Collectors.toList());
            syncdMap.put(key, listingEvents);
        }
        return syncdMap;
    }

    public List<CertificationStatusEvent> findByCertifiedProductId(Long certifiedProductId) {
        List<CertificationStatusEventEntity> entities = getEntitiesByCertifiedProductId(certifiedProductId);
        List<CertificationStatusEvent> cses = new ArrayList<>();

        for (CertificationStatusEventEntity entity : entities) {
            cses.add(entity.toDomain());
        }
        return cses;
    }

    public CertificationStatusEvent findInitialCertificationEventForCertifiedProduct(Long certifiedProductId) {
        CertificationStatusEventEntity certificationDateEvent = getOldestActiveEventForCertifiedProduct(
                certifiedProductId);
        return certificationDateEvent.toDomain();
    }

    private List<CertificationStatusEventEntity> getAllEntities() {
        List<CertificationStatusEventEntity> result = entityManager.createQuery(
                "FROM CertificationStatusEventEntity cse "
                        + "LEFT JOIN FETCH cse.certificationStatus s "
                        + "WHERE (NOT cse.deleted = true) "
                        + "AND (NOT s.deleted = true)",
                CertificationStatusEventEntity.class).getResultList();
        return result;

    }

    private CertificationStatusEventEntity getEntityById(Long id) throws EntityRetrievalException {
        CertificationStatusEventEntity entity = null;

        Query query = entityManager.createQuery(
                "FROM CertificationStatusEventEntity cse "
                        + "LEFT JOIN FETCH cse.certificationStatus s "
                        + "WHERE cse.id = :entityid "
                        + "AND (NOT cse.deleted = true) "
                        + "AND (NOT s.deleted = true)",
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
                + "LEFT JOIN FETCH cse.certificationStatus s "
                + "WHERE cse.certifiedProductId = :cpId "
                + "AND (NOT cse.deleted = true) "
                + "AND (NOT s.deleted = true)", CertificationStatusEventEntity.class);
        query.setParameter("cpId", id);
        List<CertificationStatusEventEntity> result = query.getResultList();

        return result;
    }

    private CertificationStatusEventEntity getOldestActiveEventForCertifiedProduct(Long cpId) {
        Query query = entityManager.createQuery("FROM CertificationStatusEventEntity cse "
                + "LEFT JOIN FETCH cse.certificationStatus s "
                + "WHERE cse.certifiedProductId = :cpId "
                + "AND cse.certificationStatusId = 1 "
                + "AND (NOT cse.deleted = true) "
                + "AND (NOT s.deleted = true) "
                + "ORDER BY cse.eventDate ASC", CertificationStatusEventEntity.class);
        query.setParameter("cpId", cpId);
        List<CertificationStatusEventEntity> result = query.getResultList();
        if (result == null || result.size() == 0) {
            return null;
        }
        return result.get(0);
    }
}

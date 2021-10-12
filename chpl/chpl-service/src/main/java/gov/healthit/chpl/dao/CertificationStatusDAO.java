package gov.healthit.chpl.dao;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.CertificationStatus;
import gov.healthit.chpl.entity.CertificationStatusEntity;
import gov.healthit.chpl.exception.EntityRetrievalException;

@Repository("certificationStatusDAO")
public class CertificationStatusDAO extends BaseDAOImpl {

    public List<CertificationStatus> findAll() {
        List<CertificationStatusEntity> entities = getAllEntities();
        List<CertificationStatus> result = new ArrayList<CertificationStatus>();
        for (CertificationStatusEntity entity : entities) {
            result.add(entity.toDomain());
        }
        return result;
    }

    public CertificationStatus getById(Long id) throws EntityRetrievalException {
        CertificationStatus cs = null;
        CertificationStatusEntity entity = getEntityById(id);
        if (entity != null) {
            cs = entity.toDomain();
        }
        return cs;
    }

    public CertificationStatus getByStatusName(String statusName) {
        CertificationStatusEntity entity = getEntityByName(statusName);
        if (entity == null) {
            return null;
        }
        return entity.toDomain();
    }

    private List<CertificationStatusEntity> getAllEntities() {
        List<CertificationStatusEntity> result = entityManager
                .createQuery("from CertificationStatusEntity where (NOT deleted = true) ",
                        CertificationStatusEntity.class)
                .getResultList();
        return result;

    }

    public CertificationStatusEntity getEntityById(Long id) throws EntityRetrievalException {
        CertificationStatusEntity entity = null;

        Query query = entityManager.createQuery(
                "from CertificationStatusEntity where (NOT deleted = true) AND (certification_status_id = :entityid) ",
                CertificationStatusEntity.class);
        query.setParameter("entityid", id);
        List<CertificationStatusEntity> result = query.getResultList();

        if (result.size() > 1) {
            throw new EntityRetrievalException("Data error. Duplicate status id in database.");
        }

        if (result.size() > 0) {
            entity = result.get(0);
        }

        return entity;
    }

    public CertificationStatusEntity getEntityByName(String name) {
        CertificationStatusEntity entity = null;

        Query query = entityManager.createQuery(
                "from CertificationStatusEntity where (NOT deleted = true) AND (certification_status = :name) ",
                CertificationStatusEntity.class);
        query.setParameter("name", name);
        List<CertificationStatusEntity> result = query.getResultList();

        if (result.size() > 0) {
            entity = result.get(0);
        }

        return entity;
    }
}

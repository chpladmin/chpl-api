package gov.healthit.chpl.dao;

import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.CQMCriterion;
import gov.healthit.chpl.entity.CQMCriterionEntity;
import gov.healthit.chpl.entity.CQMVersionEntity;
import gov.healthit.chpl.exception.EntityRetrievalException;

@Repository(value = "cqmCriterionDAO")
public class CQMCriterionDAO extends BaseDAOImpl {

    public List<CQMCriterion> findAll() {
        List<CQMCriterionEntity> entities = getAllEntities();
        return entities.stream()
                .map(entity -> entity.toDomain())
                .collect(Collectors.toList());
    }

    public CQMCriterion getById(Long criterionId) throws EntityRetrievalException {
        CQMCriterionEntity entity = getEntityById(criterionId);
        if (entity != null) {
            return entity.toDomain();
        }
        return null;
    }

    public CQMCriterion getCMSByNumber(String number) {
        CQMCriterionEntity entity = getCMSEntityByNumber(number);
        if (entity == null) {
            return null;
        }
        return entity.toDomain();
    }

    public CQMCriterion getCMSByNumberAndVersion(String number, String version) {
        CQMCriterionEntity entity = getCMSEntityByNumberAndVersion(number, version);
        if (entity == null) {
            return null;
        }
        return entity.toDomain();
    }

    public CQMCriterion getNQFByNumber(String number) {
        CQMCriterionEntity entity = getNQFEntityByNumber(number);
        if (entity == null) {
            return null;
        }
        return entity.toDomain();
    }

    private List<CQMCriterionEntity> getAllEntities() {
        List<CQMCriterionEntity> result = entityManager
                .createQuery("from CQMCriterionEntity where (NOT deleted = true) ", CQMCriterionEntity.class)
                .getResultList();
        return result;

    }

    private CQMCriterionEntity getEntityById(Long id) throws EntityRetrievalException {
        CQMCriterionEntity entity = null;

        Query query = entityManager.createQuery(
                "from CQMCriterionEntity where (NOT deleted = true) AND (cqm_criterion_id = :entityid) ",
                CQMCriterionEntity.class);
        query.setParameter("entityid", id);
        List<CQMCriterionEntity> result = query.getResultList();

        if (result.size() > 1) {
            throw new EntityRetrievalException("Data error. Duplicate criterion id in database.");
        }

        if (result.size() > 0) {
            entity = result.get(0);
        }

        return entity;
    }

    public CQMCriterionEntity getNQFEntityByNumber(String number) {
        CQMCriterionEntity entity = null;

        Query query = entityManager.createQuery(
                "from CQMCriterionEntity where (NOT deleted = true) AND (nqf_number = :number) ",
                CQMCriterionEntity.class);
        query.setParameter("number", number);
        List<CQMCriterionEntity> result = query.getResultList();

        if (result.size() > 0) {
            entity = result.get(0);
        }

        return entity;
    }

    public CQMCriterionEntity getCMSEntityByNumber(String number) {
        CQMCriterionEntity entity = null;

        Query query = entityManager.createQuery("SELECT cqmCrit "
                + "FROM CQMCriterionEntity cqmCrit "
                + "WHERE (NOT deleted = true) "
                + "AND (cqmCrit.cmsId = :number) ", CQMCriterionEntity.class);
        query.setParameter("number", number);
        List<CQMCriterionEntity> result = query.getResultList();

        if (result.size() > 0) {
            entity = result.get(0);
        }

        return entity;
    }

    public CQMCriterionEntity getCMSEntityByNumberAndVersion(String number, String version) {
        // first find the version id
        CQMVersionEntity versionEntity = null;
        Query versionQuery = entityManager.createQuery(
                "from CQMVersionEntity where (NOT deleted = true) AND (version = :version)", CQMVersionEntity.class);
        versionQuery.setParameter("version", version);
        List<CQMVersionEntity> versionResult = versionQuery.getResultList();
        if (versionResult.size() > 0) {
            versionEntity = versionResult.get(0);
        }

        CQMCriterionEntity entity = null;
        if (versionEntity != null && versionEntity.getId() != null) {

            Query query = entityManager.createQuery(
                    "from CQMCriterionEntity where (NOT deleted = true) AND (cms_id = :number) AND (cqm_version_id = :versionId) ",
                    CQMCriterionEntity.class);
            query.setParameter("number", number);
            query.setParameter("versionId", versionEntity.getId());

            List<CQMCriterionEntity> result = query.getResultList();
            if (result.size() > 0) {
                entity = result.get(0);
            }
        }
        return entity;
    }
}

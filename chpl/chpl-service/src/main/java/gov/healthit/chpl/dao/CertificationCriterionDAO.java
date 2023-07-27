package gov.healthit.chpl.dao;

import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.entity.CertificationCriterionEntity;
import gov.healthit.chpl.exception.EntityRetrievalException;

@Repository("certificationCriterionDAO")
public class CertificationCriterionDAO extends BaseDAOImpl {

    @Transactional
    public void markAsRemoved(CertificationCriterion criterion) throws EntityRetrievalException {
        CertificationCriterionEntity entity = this.getEntityById(criterion.getId());
        entity.setRemoved(true);
        update(entity);
    }

    @Transactional
    public void delete(final Long criterionId) {
        Query query = entityManager.createQuery(
                "UPDATE CertificationCriterionEntity SET deleted = true WHERE certification_criterion_id = :entityid");
        query.setParameter("entityid", criterionId);
        query.executeUpdate();

    }

    public List<CertificationCriterion> findAll() {

        List<CertificationCriterionEntity> entities = getAllEntities();
        return entities.stream()
                .map(entity -> entity.toDomain())
                .toList();
    }

    @Deprecated
    public List<CertificationCriterion> findByCertificationEditionYear(String year) {
        List<CertificationCriterionEntity> entities = getEntitiesByCertificationEditionYear(year);
        return entities.stream()
                .map(entity -> entity.toDomain())
                .toList();
    }

    public List<CertificationCriterion> getAllByNumber(String criterionName) {
        List<CertificationCriterionEntity> entities = getEntitiesByNumber(criterionName);
        return entities.stream()
                .map(entity -> entity.toDomain())
                .toList();
    }

    public CertificationCriterion getById(Long criterionId) throws EntityRetrievalException {

        CertificationCriterion result = null;
        CertificationCriterionEntity entity = getEntityById(criterionId);
        if (entity != null) {
            result = entity.toDomain();
        }
        return result;
    }

    @Transactional
    public CertificationCriterion getByNumberAndTitle(String criterionNumber, String criterionTitle) {
        CertificationCriterion result = null;
        CertificationCriterionEntity entity = getEntityByNumberAndTitle(criterionNumber, criterionTitle);
        if (entity != null) {
            result = entity.toDomain();
        }
        return result;
    }

    private List<CertificationCriterionEntity> getAllEntities() {
        Query query = entityManager
                .createQuery(
                        "SELECT cce "
                                + "FROM CertificationCriterionEntity cce "
                                + "JOIN FETCH cce.certificationEdition "
                                + "WHERE cce.deleted = false",
                                CertificationCriterionEntity.class);
        @SuppressWarnings("unchecked") List<CertificationCriterionEntity> result = query.getResultList();

        return result;
    }

    @Deprecated
    @SuppressWarnings("unchecked")
    private List<CertificationCriterionEntity> getEntitiesByCertificationEditionYear(String year) {
        Query query = entityManager.createQuery("SELECT cce "
                + "FROM CertificationCriterionEntity cce "
                + "JOIN FETCH cce.certificationEdition edition "
                + "WHERE (NOT cce.deleted = true) "
                + "AND (edition.year = :year)", CertificationCriterionEntity.class);
        query.setParameter("year", year);
        return query.getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<CertificationCriterionEntity> getEntitiesByNumber(String number) {
        Query query = entityManager.createQuery("SELECT cce "
                + "FROM CertificationCriterionEntity cce "
                + "JOIN FETCH cce.certificationEdition "
                + "WHERE (NOT cce.deleted = true) "
                + "AND (cce.number = :number)", CertificationCriterionEntity.class);
        query.setParameter("number", number);
        return query.getResultList();
    }

    public CertificationCriterionEntity getEntityById(Long id) throws EntityRetrievalException {
        CertificationCriterionEntity entity = null;
        if (id != null) {
            Query query = entityManager.createQuery(
                    "SELECT cce "
                            + "FROM CertificationCriterionEntity cce "
                            + "JOIN FETCH cce.certificationEdition "
                            + "WHERE (cce.deleted <> true) AND (cce.id = :entityid) ",
                            CertificationCriterionEntity.class);
            query.setParameter("entityid", id);
            @SuppressWarnings("unchecked") List<CertificationCriterionEntity> result = query.getResultList();

            if (result.size() > 1) {
                throw new EntityRetrievalException("Data error. Duplicate criterion id in database.");
            }

            if (result.size() > 0) {
                entity = result.get(0);
            }
        }

        return entity;
    }

    public CertificationCriterionEntity getEntityByNumberAndTitle(String criterionNumber, String criterionTitle) {
        Query query = entityManager
                .createQuery(
                        "SELECT cce " + "FROM CertificationCriterionEntity cce "
                                + "JOIN FETCH cce.certificationEdition "
                                + "WHERE (NOT cce.deleted = true) "
                                + "AND (cce.number = :number) "
                                + "AND (cce.title = :title) ",
                                CertificationCriterionEntity.class);
        query.setParameter("number", criterionNumber);
        query.setParameter("title", criterionTitle);
        @SuppressWarnings("unchecked") List<CertificationCriterionEntity> results = query.getResultList();

        CertificationCriterionEntity entity = null;
        if (results.size() > 0) {
            entity = results.get(0);
        }
        return entity;
    }
}

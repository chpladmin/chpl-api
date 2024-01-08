package gov.healthit.chpl.dao;

import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.certificationCriteria.CertificationCriterionEntity;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;

@Repository("certificationCriterionDAO")
public class CertificationCriterionDAO extends BaseDAOImpl {

    @Transactional
    public CertificationCriterion update(CertificationCriterion criterion)
            throws EntityRetrievalException, EntityCreationException {
        CertificationCriterionEntity entity = this.getEntityById(criterion.getId());
        entity.setCertificationEditionId(criterion.getCertificationEditionId());
        entity.setDescription(criterion.getDescription());
        entity.setStartDay(criterion.getStartDay());
        entity.setEndDay(criterion.getEndDay());
        entity.setRuleId(criterion.getRule() != null ? criterion.getRule().getId() : null);
        entity.setId(criterion.getId());
        entity.setNumber(criterion.getNumber());
        entity.setTitle(criterion.getTitle());
        update(entity);

        return entity.toDomain();
    }

    public List<CertificationCriterion> findAll() {
        List<CertificationCriterionEntity> entities = getAllEntities();
        return entities.stream()
                .map(entity -> entity.toDomain())
                .collect(Collectors.toList());
    }

    public List<CertificationCriterion> findByCertificationEditionYear(String year) {
        List<CertificationCriterionEntity> entities = getEntitiesByCertificationEditionYear(year);
        return entities.stream()
                .map(entity -> entity.toDomain())
                .collect(Collectors.toList());
    }

    public List<CertificationCriterion> getAllByNumber(String criterionName) {
        List<CertificationCriterionEntity> entities = getEntitiesByNumber(criterionName);
        return entities.stream()
                .map(entity -> entity.toDomain())
                .collect(Collectors.toList());
    }

    public CertificationCriterion getById(Long criterionId) throws EntityRetrievalException {
        CertificationCriterionEntity entity = getEntityById(criterionId);
        return entity.toDomain();
    }

    @Transactional
    public CertificationCriterion getByNumberAndTitle(String criterionNumber, String criterionTitle) {
        CertificationCriterionEntity entity = getEntityByNumberAndTitle(criterionNumber, criterionTitle);
        return entity.toDomain();
    }

    public List<CertificationCriterion> getByWebsite(String companionGuideLink) {
        Query query = entityManager.createQuery("SELECT cc "
                + "FROM CertificationCriterionEntity cc "
                + "LEFT JOIN FETCH cc.certificationEdition "
                + "LEFT JOIN FETCH cc.rule "
                + "WHERE cc.deleted = false "
                + "AND cc.companionGuideLink = :companionGuideLink");
        query.setParameter("companionGuideLink", companionGuideLink);
        List<CertificationCriterionEntity> results = query.getResultList();
        return results.stream()
                .map(result -> result.toDomain())
                .collect(Collectors.toList());
    }

    private List<CertificationCriterionEntity> getAllEntities() {
        Query query = entityManager
                .createQuery(
                        "SELECT cce "
                                + "FROM CertificationCriterionEntity cce "
                                + "LEFT JOIN FETCH cce.certificationEdition "
                                + "LEFT JOIN FETCH cce.rule "
                                + "WHERE cce.deleted = false",
                                CertificationCriterionEntity.class);
        @SuppressWarnings("unchecked") List<CertificationCriterionEntity> result = query.getResultList();

        return result;
    }

    @SuppressWarnings("unchecked")
    private List<CertificationCriterionEntity> getEntitiesByCertificationEditionYear(String year) {
        Query query = entityManager.createQuery("SELECT cce "
                + "FROM CertificationCriterionEntity cce "
                + "JOIN FETCH cce.certificationEdition edition "
                + "LEFT JOIN FETCH cce.rule "
                + "WHERE (NOT cce.deleted = true) "
                + "AND (edition.year = :year)", CertificationCriterionEntity.class);
        query.setParameter("year", year);
        return query.getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<CertificationCriterionEntity> getEntitiesByNumber(String number) {
        Query query = entityManager.createQuery("SELECT cce "
                + "FROM CertificationCriterionEntity cce "
                + "LEFT JOIN FETCH cce.certificationEdition "
                + "LEFT JOIN FETCH cce.rule "
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
                            + "LEFT JOIN FETCH cce.certificationEdition "
                            + "LEFT JOIN FETCH cce.rule "
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
                                + "LEFT JOIN FETCH cce.certificationEdition "
                                + "LEFT JOIN FETCH cce.rule "
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

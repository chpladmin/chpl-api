package gov.healthit.chpl.conformanceMethod;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.caching.CacheNames;
import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.certificationCriteria.CertificationCriterionComparator;
import gov.healthit.chpl.conformanceMethod.domain.ConformanceMethod;
import gov.healthit.chpl.conformanceMethod.domain.ConformanceMethodCriteriaMap;
import gov.healthit.chpl.conformanceMethod.entity.ConformanceMethodCriteriaMapEntity;
import gov.healthit.chpl.conformanceMethod.entity.ConformanceMethodEntity;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.entity.listing.CertificationResultEntity;
import gov.healthit.chpl.exception.EntityRetrievalException;
import jakarta.persistence.Query;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Repository("conformanceMethodDAO")
public class ConformanceMethodDAO extends BaseDAOImpl {

    private CertifiedProductDAO certifiedProductDao;
    private CertificationCriterionComparator criteriaComparator;

    @Autowired
    public ConformanceMethodDAO(CertifiedProductDAO certifiedProductDao,
            CertificationCriterionComparator criteriaComparator) {
        this.certifiedProductDao = certifiedProductDao;
        this.criteriaComparator = criteriaComparator;
    }

    public ConformanceMethod getById(Long id) {
        ConformanceMethodEntity entity = getEntityById(id);
        if (entity != null) {
            return entity.toDomainWithCriteria(criteriaComparator);
        } else {
            return null;
        }
    }

    public List<ConformanceMethod> getAllWithCriteria() {
        Query query = entityManager.createQuery("SELECT DISTINCT cm "
                + "FROM ConformanceMethodEntity cm "
                + "LEFT JOIN FETCH cm.criteria crit "
                + "LEFT JOIN FETCH crit.certificationEdition "
                + "LEFT JOIN FETCH crit.rule "
                + "WHERE cm.deleted <> true ",
                ConformanceMethodEntity.class);
        List<ConformanceMethodEntity> entities = query.getResultList();
        return entities.stream()
                .map(entity -> entity.toDomainWithCriteria(criteriaComparator))
                .collect(Collectors.toList());
    }

    public List<ConformanceMethodCriteriaMap> getAllConformanceMethodCriteriaMap() throws EntityRetrievalException {
        List<ConformanceMethodCriteriaMapEntity> entities = entityManager.createQuery("SELECT DISTINCT mapping "
                + "FROM ConformanceMethodCriteriaMapEntity mapping "
                + "JOIN FETCH mapping.certificationCriterion mappingCrit "
                + "LEFT JOIN FETCH mappingCrit.certificationEdition "
                + "LEFT JOIN FETCH mappingCrit.rule "
                + "JOIN FETCH mapping.conformanceMethod mappingCm "
                + "WHERE mapping.deleted <> true "
                + "AND mappingCm.deleted <> true ",
                ConformanceMethodCriteriaMapEntity.class)
        .getResultList();

        return entities.stream()
                .map(e -> e.toDomain())
                .collect(Collectors.toList());
    }

    public List<CertifiedProductDetailsDTO> getCertifiedProductsByConformanceMethod(ConformanceMethod conformanceMethod)
            throws EntityRetrievalException {
        List<Long> certifiedProductIds = getCertifiedProductIdsUsingConformanceMethodId(conformanceMethod.getId());
        return certifiedProductDao.getDetailsByIds(certifiedProductIds);
    }

    public List<CertifiedProductDetailsDTO> getCertifiedProductsByConformanceMethodAndCriteria(ConformanceMethod conformanceMethod,
            CertificationCriterion criterion) throws EntityRetrievalException {
        List<Long> certifiedProductIds = getCertifiedProductIdsUsingConformanceMethodIdWithCriterion(
                conformanceMethod.getId(), criterion.getId());
        return certifiedProductDao.getDetailsByIds(certifiedProductIds);
    }

    @CacheEvict(value = CacheNames.CONFORMANCE_METHODS, allEntries = true)
    public Long create(ConformanceMethod conformanceMethod) {
        ConformanceMethodEntity entity = ConformanceMethodEntity.builder()
                .name(conformanceMethod.getName())
                .removalDate(conformanceMethod.getRemovalDate())
                .build();

        create(entity);
        return entity.getId();
    }

    @CacheEvict(value = CacheNames.CONFORMANCE_METHODS, allEntries = true)
    public void createConformanceMethodCriteriaMap(Long conformanceMethodId, CertificationCriterion criterion) {
        ConformanceMethodCriteriaMapEntity entity = ConformanceMethodCriteriaMapEntity.builder()
                .certificationCriterionId(criterion.getId())
                .conformanceMethodId(conformanceMethodId)
                .build();
        create(entity);
    }

    @CacheEvict(value = CacheNames.CONFORMANCE_METHODS, allEntries = true)
    public void update(ConformanceMethod conformanceMethod) throws EntityRetrievalException {
        ConformanceMethodEntity entity = getEntityById(conformanceMethod.getId());
        entity.setName(conformanceMethod.getName());
        entity.setRemovalDate(conformanceMethod.getRemovalDate());
        update(entity);
    }

    @CacheEvict(value = CacheNames.CONFORMANCE_METHODS, allEntries = true)
    public void remove(ConformanceMethod conformanceMethod) throws EntityRetrievalException {
        ConformanceMethodEntity entity = getEntityById(conformanceMethod.getId());
        entity.setDeleted(true);
        update(entity);
    }

    @CacheEvict(value = CacheNames.CONFORMANCE_METHODS, allEntries = true)
    public void removeConformanceMethodCriteriaMap(Long conformanceMethodId, CertificationCriterion criterion) {
        try {
            ConformanceMethodCriteriaMapEntity entity = getConformanceMethodCriteriaMapByConformanceMethodAndCriterion(
                    conformanceMethodId, criterion.getId());
            entity.setDeleted(true);
            update(entity);
        } catch (EntityRetrievalException e) {
            LOGGER.catching(e);
            return;
        }
    }

    private List<Long> getCertifiedProductIdsUsingConformanceMethodId(Long conformanceMethodId) {
        List<CertificationResultEntity> certResultsWithConformanceMethod =
                entityManager.createQuery("SELECT cr "
                        + "FROM CertificationResultConformanceMethodEntity crcm, CertificationResultEntity cr "
                        + "WHERE crcm.certificationResultId = cr.id "
                        + "AND crcm.conformanceMethod.id = :conformanceMethodId "
                        + "AND crcm.deleted <> true "
                        + "AND cr.deleted <> true ",
                        CertificationResultEntity.class)
                .setParameter("conformanceMethodId", conformanceMethodId)
                .getResultList();

        return certResultsWithConformanceMethod.stream()
                .map(certResult -> certResult.getCertifiedProductId())
                .distinct()
                .collect(Collectors.toList());
    }

    private List<Long> getCertifiedProductIdsUsingConformanceMethodIdWithCriterion(Long conformanceMethodId, Long criterionId) {
        List<CertificationResultEntity> certResultsWithConformanceMethod =
                entityManager.createQuery("SELECT cr "
                        + "FROM CertificationResultConformanceMethodEntity crcm, CertificationResultEntity cr "
                        + "WHERE crcm.certificationResultId = cr.id "
                        + "AND crcm.conformanceMethod.id = :conformanceMethodId "
                        + "AND cr.certificationCriterionId= :criterionId "
                        + "AND crcm.deleted <> true "
                        + "AND cr.deleted <> true ",
                        CertificationResultEntity.class)
                .setParameter("conformanceMethodId", conformanceMethodId)
                .setParameter("criterionId", criterionId)
                .getResultList();

        return certResultsWithConformanceMethod.stream()
                .map(certResult -> certResult.getCertifiedProductId())
                .distinct()
                .collect(Collectors.toList());
    }

    private ConformanceMethodCriteriaMapEntity getConformanceMethodCriteriaMapByConformanceMethodAndCriterion(
            Long conformanceMethodId, Long certificationCriterionId) throws EntityRetrievalException {
        List<ConformanceMethodCriteriaMapEntity> result = entityManager.createQuery("SELECT DISTINCT cmcm "
                        + "FROM ConformanceMethodCriteriaMapEntity cmcm "
                        + "JOIN FETCH cmcm.certificationCriterion c "
                        + "JOIN FETCH cmcm.conformanceMethod cm "
                        + "WHERE c.id = :certificationCriterionId "
                        + "AND cm.id= :conformanceMethodId "
                        + "AND cmcm.deleted <> true "
                        + "AND cm.deleted <> true "
                        + "AND c.deleted <> true",
                        ConformanceMethodCriteriaMapEntity.class)
                .setParameter("conformanceMethodId", conformanceMethodId)
                .setParameter("certificationCriterionId", certificationCriterionId)
                .getResultList();

        if (result.size() > 1) {
            throw new EntityRetrievalException("Data error. Duplicate conformance method criteria map id in database.");
        } else if (result.size() == 0) {
            throw new EntityRetrievalException("Data error. Could not locate conformance method criteria map {" + conformanceMethodId + ", " + certificationCriterionId + "} in database.");
        }

        return result.get(0);
    }

    private ConformanceMethodEntity getEntityById(Long id) {
        ConformanceMethodEntity entity = null;

        Query query = entityManager
                .createQuery("SELECT DISTINCT cm "
                        + "FROM ConformanceMethodEntity cm "
                        + "LEFT OUTER JOIN FETCH cm.criteria criterion "
                        + "LEFT OUTER JOIN FETCH criterion.certificationEdition "
                        + "LEFT JOIN FETCH criterion.rule "
                        + "WHERE (NOT cm.deleted = true) "
                        + "AND (cm.id = :id) ",
                        ConformanceMethodEntity.class);
        query.setParameter("id", id);
        List<ConformanceMethodEntity> result = query.getResultList();

        if (result.size() > 0) {
            entity = result.get(0);
        }

        return entity;
    }
}

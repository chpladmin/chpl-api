package gov.healthit.chpl.standard;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.caching.CacheNames;
import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.criteriaattribute.rule.RuleDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.entity.listing.CertificationResultEntity;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.functionalitytested.FunctionalityTested;
import gov.healthit.chpl.functionalitytested.FunctionalityTestedCriteriaMapEntity;
import gov.healthit.chpl.util.AuthUtil;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Repository
public class StandardDAO extends BaseDAOImpl {
    private RuleDAO ruleDAO;
    private CertifiedProductDAO certifiedProductDAO;

    @Autowired
    public StandardDAO(RuleDAO ruleDAO, CertifiedProductDAO certifiedProductDAO) {
        this.ruleDAO = ruleDAO;
        this.certifiedProductDAO = certifiedProductDAO;
    }

    public Standard getById(Long id) {
        try {
            StandardEntity entity = getEntityById(id);
            if (entity != null) {
                return entity.toDomainWithCriteria();
            } else {
                return null;
            }
        } catch (EntityRetrievalException e) {
            LOGGER.error("Error retrieving Standard: {}", e.getMessage(), e);
            return null;
        }
    }

    @CacheEvict(value = CacheNames.STANDARDS, allEntries = true)
    public Standard add(Standard standard) {
        StandardEntity entity = StandardEntity.builder()
                .value(standard.getValue())
                .regulatoryTextCitation(standard.getRegulatoryTextCitation())
                .additionalInformation(standard.getAdditionalInformation())
                .startDay(standard.getStartDay())
                .endDay(standard.getEndDay())
                .requiredDay(standard.getRequiredDay())
                .rule(standard.getRule() != null && standard.getRule().getId() != null
                        ? ruleDAO.getRuleEntityById(standard.getRule().getId())
                        : null)
                .creationDate(new Date())
                .lastModifiedDate(new Date())
                .lastModifiedUser(AuthUtil.getAuditId())
                .deleted(false)
                .build();

        create(entity);

        return getById(entity.getId());
    }

    public List<CertifiedProductDetailsDTO> getCertifiedProductsByStandardAndCriteria(Standard standard, CertificationCriterion criterion)
            throws EntityRetrievalException {
        List<Long> certifiedProductIds = getCertifiedProductIdsUsingStandardIdWithCriterion(standard.getId(), criterion.getId());
        return certifiedProductDAO.getDetailsByIds(certifiedProductIds);
    }

    @CacheEvict(value = CacheNames.STANDARDS, allEntries = true)
    public void update(Standard standard) throws EntityRetrievalException {
        StandardEntity entity = getEntityById(standard.getId());

        entity.setValue(standard.getValue());
        entity.setRegulatoryTextCitation(standard.getRegulatoryTextCitation());
        entity.setStartDay(standard.getStartDay());
        entity.setEndDay(standard.getEndDay());
        entity.setRequiredDay(standard.getRequiredDay());

        if (standard.getRule() != null) {
            entity.setRule(ruleDAO.getRuleEntityById(standard.getRule().getId()));
        } else {
            entity.setRule(null);
        }

        entity.setAdditionalInformation(standard.getAdditionalInformation());
        entity.setLastModifiedUser(AuthUtil.getAuditId());
        entity.setLastModifiedDate(new Date());

        update(entity);
    }

    @CacheEvict(value = CacheNames.FUNCTIONALITY_TESTED_MAPS, allEntries = true)
    public void addFunctionalityTestedCriteriaMap(FunctionalityTested functionalityTested, CertificationCriterion criterion) {
        FunctionalityTestedCriteriaMapEntity entity = FunctionalityTestedCriteriaMapEntity.builder()
                .certificationCriterionId(criterion.getId())
                .functionalityTestedId(functionalityTested.getId())
                .creationDate(new Date())
                .lastModifiedDate(new Date())
                .lastModifiedUser(AuthUtil.getAuditId())
                .deleted(false)
                .build();

        create(entity);
    }

    @CacheEvict(value = CacheNames.STANDARDS, allEntries = true)
    public void removeStandardCriteriaMap(Standard standard, CertificationCriterion criterion) {
        try {
            StandardCriteriaMapEntity entity = getStandardCriteriaMapByStandardAndCriterion(standard.getId(), criterion.getId());
            entity.setDeleted(true);
            entity.setLastModifiedDate(new Date());
            entity.setLastModifiedUser(AuthUtil.getAuditId());

            update(entity);
        } catch (EntityRetrievalException e) {
            LOGGER.catching(e);
            return;
        }
    }

    public List<CertifiedProductDetailsDTO> getCertifiedProductsByStandard(Standard standard) throws EntityRetrievalException {
        List<Long> certifiedProductIds = getCertifiedProductIdsUsingStandardId(standard.getId());
        return certifiedProductDAO.getDetailsByIds(certifiedProductIds);
    }

    @CacheEvict(value = CacheNames.STANDARDS, allEntries = true)
    public void remove(Standard standard) throws EntityRetrievalException {
        StandardEntity entity = getEntityById(standard.getId());
        entity.setDeleted(true);
        entity.setLastModifiedUser(AuthUtil.getAuditId());
        entity.setLastModifiedDate(new Date());
        update(entity);
    }

    public List<Standard> findAll() {
        List<StandardEntity> entities = getAllEntities();
        return entities.stream()
                .map(entity -> entity.toDomainWithCriteria())
                .collect(Collectors.toList());
    }

    @Cacheable(CacheNames.STANDARDS)
    public Map<Long, List<Standard>> getStandardCriteriaMaps() {
        List<Standard> allStandards = findAll();
        Map<Long, List<Standard>> mapping = new HashMap<Long, List<Standard>>();
        allStandards.stream()
            .forEach(standard -> updateMapping(mapping, standard));
        return mapping;
    }

    @Transactional
    public List<StandardCriteriaMap> getAllStandardCriteriaMap() throws EntityRetrievalException {
        return getAllStandardCriteriaMapEntities().stream()
                .map(e -> e.toDomain())
                .collect(Collectors.toList());
    }


    private void updateMapping(Map<Long, List<Standard>> mapping, Standard standard) {
        standard.getCriteria().stream()
            .forEach(standardCriterion -> {
                if (!mapping.containsKey(standardCriterion.getId())) {
                    mapping.put(standardCriterion.getId(), new ArrayList<Standard>());
                }
                mapping.get(standardCriterion.getId()).add(standard);
            });
    }

    private List<StandardEntity> getAllEntities() {
        return entityManager
                .createQuery("SELECT DISTINCT s "
                            + "FROM StandardEntity s "
                            + "LEFT OUTER JOIN FETCH s.mappedCriteria criteriaMapping "
                            + "LEFT OUTER JOIN FETCH criteriaMapping.criterion criterion "
                            + "LEFT OUTER JOIN FETCH criterion.certificationEdition "
                            + "LEFT JOIN FETCH criterion.rule "
                            + "WHERE (NOT s.deleted = true) ",StandardEntity.class)
                .getResultList();
    }

    private StandardEntity getEntityById(Long id) throws EntityRetrievalException {
        StandardEntity entity = null;

        Query query = entityManager
                .createQuery("SELECT DISTINCT s "
                        + "FROM StandardEntity s "
                        + "LEFT OUTER JOIN FETCH s.mappedCriteria criteriaMapping "
                        + "LEFT OUTER JOIN FETCH criteriaMapping.criterion criterion "
                        + "LEFT OUTER JOIN FETCH criterion.certificationEdition "
                        + "LEFT JOIN FETCH criterion.rule "
                        + "WHERE (NOT s.deleted = true) "
                        + "AND (s.id = :entityid) ",
                        StandardEntity.class);
        query.setParameter("entityid", id);
        List<StandardEntity> result = query.getResultList();

        if (result.size() > 1) {
            throw new EntityRetrievalException("Data error. Duplicate standard id in database.");
        }

        if (result.size() > 0) {
            entity = result.get(0);
        }

        return entity;
    }

    private List<StandardCriteriaMapEntity> getAllStandardCriteriaMapEntities() throws EntityRetrievalException {
        return entityManager.createQuery("SELECT DISTINCT scm "
                        + "FROM StandardCriteriaMapEntity scm "
                        + "JOIN FETCH scm.criterion c "
                        + "JOIN FETCH c.certificationEdition "
                        + "JOIN FETCH c.rule "
                        + "JOIN FETCH scm.standard ft "
                        + "WHERE scm.deleted <> true "
                        + "AND s.deleted <> true ",
                        StandardCriteriaMapEntity.class)
                .getResultList();
    }

    private List<Long> getCertifiedProductIdsUsingStandardIdWithCriterion(Long standardId, Long criterionId) {
        List<CertificationResultEntity> certResultsWithStandard =
                entityManager.createQuery("SELECT cr "
                        + "FROM CertificationResultStandardEntity crs, CertificationResultEntity cr "
                        + "WHERE crs.certificationResultId = cr.id "
                        + "AND crs.standard.id = :standardId "
                        + "AND cr.certificationCriterionId= :criterionId "
                        + "AND crs.deleted <> true "
                        + "AND cr.deleted <> true ",
                        CertificationResultEntity.class)
                .setParameter("standardId", standardId)
                .setParameter("criterionId", criterionId)
                .getResultList();

        return certResultsWithStandard.stream()
                .map(certResult -> certResult.getCertifiedProductId())
                .distinct()
                .collect(Collectors.toList());
    }

    private StandardCriteriaMapEntity getStandardCriteriaMapByStandardAndCriterion(Long standardId, Long certificationCriterionId) throws EntityRetrievalException {
        List<StandardCriteriaMapEntity> result = entityManager.createQuery("SELECT DISTINCT scm "
                        + "FROM StandardCriteriaMapEntity scm "
                        + "JOIN FETCH scm.criterion c "
                        + "JOIN FETCH scm.standard s "
                        + "WHERE c.id = :certificationCriterionId "
                        + "AND s.id= :standardId "
                        + "AND scm.deleted <> true "
                        + "AND s.deleted <> true "
                        + "AND c.deleted <> true",
                        StandardCriteriaMapEntity.class)
                .setParameter("standardId", standardId)
                .setParameter("certificationCriterionId", certificationCriterionId)
                .getResultList();

        if (result.size() > 1) {
            throw new EntityRetrievalException("Data error. Duplicate standard criteria map id in database.");
        } else if (result.size() == 0) {
            throw new EntityRetrievalException("Data error. Could not locate standard criteria map {" + standardId + ", " + certificationCriterionId + "} in database.");
        }

        return result.get(0);
    }

    private List<Long> getCertifiedProductIdsUsingStandardId(Long standardId) {
        List<CertificationResultEntity> certResultsWithTestTool =
                entityManager.createQuery("SELECT cr "
                        + "FROM CertificationResultStandardEntity crs, CertificationResultEntity cr "
                        + "WHERE crs.certificationResultId = cr.id "
                        + "AND crs.stanadrd.dd = :standardId "
                        + "AND crs.deleted <> true "
                        + "AND cr.deleted <> true ",
                        CertificationResultEntity.class)
                .setParameter("standardId", standardId)
                .getResultList();

        return certResultsWithTestTool.stream()
                .map(certResult -> certResult.getCertifiedProductId())
                .distinct()
                .collect(Collectors.toList());
    }

}

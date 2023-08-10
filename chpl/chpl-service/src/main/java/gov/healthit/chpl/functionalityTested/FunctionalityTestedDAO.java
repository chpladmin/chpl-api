package gov.healthit.chpl.functionalityTested;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.caching.CacheNames;
import gov.healthit.chpl.criteriaattribute.CriteriaAttribute;
import gov.healthit.chpl.criteriaattribute.CriteriaAttributeCriteriaMap;
import gov.healthit.chpl.criteriaattribute.CriteriaAttributeDAO;
import gov.healthit.chpl.criteriaattribute.rule.RuleDAO;
import gov.healthit.chpl.criteriaattribute.testtool.TestToolCriteriaMapEntity;
import gov.healthit.chpl.criteriaattribute.testtool.TestToolEntity;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.AuthUtil;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Repository("functionalityTestedDAO")
public class FunctionalityTestedDAO extends BaseDAOImpl implements CriteriaAttributeDAO {

    private RuleDAO ruleDAO;

    @Autowired
    public FunctionalityTestedDAO(RuleDAO ruleDAO) {
        this.ruleDAO = ruleDAO;
    }

    public FunctionalityTested getById(Long id) throws EntityRetrievalException {
        FunctionalityTestedEntity entity = getEntityById(id);
        if (entity != null) {
            return entity.toDomain();
        }
        return null;
    }



    @Override
    public CriteriaAttribute add(CriteriaAttribute criteriaAttribute) {
        FunctionalityTestedEntity entity = FunctionalityTestedEntity.builder()
                .name(criteriaAttribute.getValue())
                .value(criteriaAttribute.getValue())
                .regulatoryTextCitation(criteriaAttribute.getRegulatoryTextCitation())
                .startDay(criteriaAttribute.getStartDay())
                .endDay(criteriaAttribute.getEndDay())
                .requiredDay(criteriaAttribute.getRequiredDay())
                .rule(criteriaAttribute.getRule() != null ? ruleDAO.getRuleEntityById(criteriaAttribute.getRule().getId()) : null)
                .creationDate(new Date())
                .lastModifiedDate(new Date())
                .lastModifiedUser(AuthUtil.getAuditId())
                .deleted(false)
                .build();
        create(entity);

        return getById(entity.getId());
    }

    @Override
    public CriteriaAttribute getCriteriaAttributeById(Long id) {
        return getById(id);
    }


    @Override
    public List<CriteriaAttributeCriteriaMap> getAllAssociatedCriteriaMaps() throws EntityRetrievalException {
        return getAllTestToolCriteriaMap().stream()
                .map(map -> CriteriaAttributeCriteriaMap.builder()
                        .criterion(map.getCriterion())
                        .criteriaAttribute(map.getTestTool())
                        .build())
                .toList();
    }

    @Override
    public List<CertifiedProductDetailsDTO> getCertifiedProductsByCriteriaAttributeAndCriteria(CriteriaAttribute criteriaAttribute, CertificationCriterion criterion)
            throws EntityRetrievalException {
        List<Long> certifiedProductIds = getCertifiedProductIdsUsingTestToolIdWithCriterion(criteriaAttribute.getId(), criterion.getId());
        return certifiedProductDAO.getDetailsByIds(certifiedProductIds);
    }

    @Override
    public void update(CriteriaAttribute criteriaAttribute) throws EntityRetrievalException {
        TestToolEntity entity = getEntityById(criteriaAttribute.getId());

        entity.setValue(criteriaAttribute.getValue());
        entity.setRegulatoryTextCitation(criteriaAttribute.getRegulatoryTextCitation());
        entity.setStartDay(criteriaAttribute.getStartDay());
        entity.setEndDay(criteriaAttribute.getEndDay());
        entity.setRequiredDay(criteriaAttribute.getRequiredDay());
        if (criteriaAttribute.getRule() != null) {
            entity.setRule(ruleDAO.getRuleEntityById(criteriaAttribute.getRule().getId()));
        } else {
            entity.setRule(null);
        }

        entity.setLastModifiedUser(AuthUtil.getAuditId());
        entity.setLastModifiedDate(new Date());

        update(entity);
    }

    @Override
    public void addCriteriaAttributeCriteriaMap(CriteriaAttribute criteriaAttribute, CertificationCriterion criterion) {
        TestToolCriteriaMapEntity entity = TestToolCriteriaMapEntity.builder()
                .certificationCriterionId(criterion.getId())
                .testToolId(criteriaAttribute.getId())
                .creationDate(new Date())
                .lastModifiedDate(new Date())
                .lastModifiedUser(AuthUtil.getAuditId())
                .deleted(false)
                .build();

        create(entity);
    }

    @Override
    public void removeCriteriaAttributeCriteriaMap(CriteriaAttribute criteriaAttribute, CertificationCriterion criterion) {
        try {
            TestToolCriteriaMapEntity entity = getTestToolCriteriaMapByTestToolAndCriterionEntity(criteriaAttribute.getId(), criterion.getId());
            entity.setDeleted(true);
            entity.setLastModifiedDate(new Date());
            entity.setLastModifiedUser(AuthUtil.getAuditId());

            update(entity);
        } catch (EntityRetrievalException e) {
            LOGGER.catching(e);
            return;
        }
    }

    @Override
    public List<CertifiedProductDetailsDTO> getCertifiedProductsByCriteriaAttribute(CriteriaAttribute criteriaAttribute) throws EntityRetrievalException {
        List<Long> certifiedProductIds = getCertifiedProductIdsUsingTestToolId(criteriaAttribute.getId());
        return certifiedProductDAO.getDetailsByIds(certifiedProductIds);
    }

    @Override
    public void remove(CriteriaAttribute criteriaAttribute) throws EntityRetrievalException {
        FunctionalityTestedEntity entity = getEntityById(criteriaAttribute.getId());
        entity.setDeleted(true);
        entity.setLastModifiedUser(AuthUtil.getAuditId());
        entity.setLastModifiedDate(new Date());
        update(entity);
    }

    public List<FunctionalityTested> findAll() {
        List<FunctionalityTestedEntity> entities = getAllEntities();
        return entities.stream()
                .map(entity -> entity.toDomain())
                .collect(Collectors.toList());
    }

    @Cacheable(CacheNames.FUNCTIONALITY_TESTED_MAPS)
    public Map<Long, List<FunctionalityTested>> getFunctionalitiesTestedCriteriaMaps() {
        List<FunctionalityTested> allFunctionalityTested = findAll();
        Map<Long, List<FunctionalityTested>> mapping = new HashMap<Long, List<FunctionalityTested>>();
        allFunctionalityTested.stream()
            .forEach(funcTest -> updateMapping(mapping, funcTest));
        return mapping;
    }

    private void updateMapping(Map<Long, List<FunctionalityTested>> mapping, FunctionalityTested functionalityTested) {
        functionalityTested.getCriteria().stream()
            .forEach(funcTestCriterion -> {
                if (!mapping.containsKey(funcTestCriterion.getId())) {
                    mapping.put(funcTestCriterion.getId(), new ArrayList<FunctionalityTested>());
                }
                mapping.get(funcTestCriterion.getId()).add(functionalityTested);
            });
    }

    private List<FunctionalityTestedEntity> getAllEntities() {
        return entityManager
                .createQuery("SELECT ft "
                            + "FROM FunctionalityTestedEntity ft "
                            + "LEFT OUTER JOIN FETCH ft.practiceType "
                            + "LEFT OUTER JOIN FETCH ft.mappedCriteria criteriaMapping "
                            + "LEFT OUTER JOIN FETCH criteriaMapping.criterion criterion "
                            + "LEFT OUTER JOIN FETCH criterion.certificationEdition "
                            + "WHERE (NOT ft.deleted = true) ", FunctionalityTestedEntity.class)
                .getResultList();
    }

    private FunctionalityTestedEntity getEntityById(Long id) throws EntityRetrievalException {
        FunctionalityTestedEntity entity = null;

        Query query = entityManager
                .createQuery("SELECT ft "
                        + "FROM FunctionalityTestedEntity ft "
                        + "LEFT OUTER JOIN FETCH ft.practiceType "
                        + "LEFT OUTER JOIN FETCH ft.mappedCriteria criteriaMapping "
                        + "LEFT OUTER JOIN FETCH criteriaMapping.criterion criterion "
                        + "LEFT OUTER JOIN FETCH criterion.certificationEdition "
                        + "WHERE (NOT ft.deleted = true) "
                        + "AND (ft.id = :entityid) ",
                        FunctionalityTestedEntity.class);
        query.setParameter("entityid", id);
        List<FunctionalityTestedEntity> result = query.getResultList();

        if (result.size() > 1) {
            throw new EntityRetrievalException("Data error. Duplicate functionality tested id in database.");
        }

        if (result.size() > 0) {
            entity = result.get(0);
        }

        return entity;
    }
}

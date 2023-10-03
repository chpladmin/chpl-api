package gov.healthit.chpl.testtool;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Query;

import org.apache.commons.collections.CollectionUtils;
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
import gov.healthit.chpl.domain.TestToolCriteriaMap;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.entity.listing.CertificationResultEntity;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.AuthUtil;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Repository("testToolDAO")
public class TestToolDAO extends BaseDAOImpl {
    private CertifiedProductDAO certifiedProductDAO;
    private RuleDAO ruleDAO;

    @Autowired
    public TestToolDAO(CertifiedProductDAO certifiedProductDAO, RuleDAO ruleDAO) {
        this.certifiedProductDAO = certifiedProductDAO;
        this.ruleDAO = ruleDAO;
    }

    @CacheEvict(value = CacheNames.TEST_TOOL_MAPS, allEntries = true)
    public TestTool add(TestTool testTool) {
        TestToolEntity entity = TestToolEntity.builder()
                .value(testTool.getValue())
                .regulatoryTextCitation(testTool.getRegulatoryTextCitation())
                .startDay(testTool.getStartDay())
                .endDay(testTool.getEndDay())
                .rule(testTool.getRule() != null ? ruleDAO.getRuleEntityById(testTool.getRule().getId()) : null)
                .creationDate(new Date())
                .lastModifiedDate(new Date())
                .lastModifiedUser(AuthUtil.getAuditId())
                .deleted(false)
                .build();
        create(entity);

        return getById(entity.getId());
    }

    public List<TestTool> getAll() {
        return getAllEntities().stream()
                .map(entity -> entity.toDomainWithCriteria())
                .toList();
    }

    public TestTool getById(Long id) {
        TestToolEntity entity = getEntityById(id);
        if (entity == null) {
            return null;
        }
        return entity.toDomainWithCriteria();
    }


    public TestTool getByName(String name) {
        List<TestToolEntity> entities = getEntitiesByName(name);
        if (CollectionUtils.isEmpty(entities)) {
            return null;
        }
        return entities.get(0).toDomainWithCriteria();
    }

    @Transactional
    @Cacheable(CacheNames.TEST_TOOL_MAPS)
    public List<TestToolCriteriaMap> getAllTestToolCriteriaMaps() throws EntityRetrievalException {
        return getAllTestToolCriteriaMapEntities().stream()
                .map(e -> e.toDomain())
                .collect(Collectors.toList());
    }

    public List<CertifiedProductDetailsDTO> getCertifiedProductsByTestToolAndCriteria(TestTool testTool, CertificationCriterion criterion)
            throws EntityRetrievalException {
        List<Long> certifiedProductIds = getCertifiedProductIdsUsingTestToolIdWithCriterion(testTool.getId(), criterion.getId());
        return certifiedProductDAO.getDetailsByIds(certifiedProductIds);
    }

    @CacheEvict(value = CacheNames.TEST_TOOL_MAPS, allEntries = true)
    public void update(TestTool testTool) throws EntityRetrievalException {
        TestToolEntity entity = getEntityById(testTool.getId());
        entity.setValue(testTool.getValue());
        entity.setRegulatoryTextCitation(testTool.getRegulatoryTextCitation());
        entity.setStartDay(testTool.getStartDay());
        entity.setEndDay(testTool.getEndDay());
        if (testTool.getRule() != null) {
            entity.setRule(ruleDAO.getRuleEntityById(testTool.getRule().getId()));
        } else {
            entity.setRule(null);
        }

        entity.setLastModifiedUser(AuthUtil.getAuditId());
        entity.setLastModifiedDate(new Date());

        update(entity);
    }

    @CacheEvict(value = CacheNames.TEST_TOOL_MAPS, allEntries = true)
    public void addTestToolCriteriaMap(TestTool testTool, CertificationCriterion criterion) {
        TestToolCriteriaMapEntity entity = TestToolCriteriaMapEntity.builder()
                .certificationCriterionId(criterion.getId())
                .testToolId(testTool.getId())
                .creationDate(new Date())
                .lastModifiedDate(new Date())
                .lastModifiedUser(AuthUtil.getAuditId())
                .deleted(false)
                .build();

        create(entity);
    }

    @CacheEvict(value = CacheNames.TEST_TOOL_MAPS, allEntries = true)
    public void removeCriteriaAttributeCriteriaMap(TestTool testTool, CertificationCriterion criterion) {
        try {
            TestToolCriteriaMapEntity entity = getTestToolCriteriaMapByTestToolAndCriterionEntity(testTool.getId(), criterion.getId());
            entity.setDeleted(true);
            entity.setLastModifiedDate(new Date());
            entity.setLastModifiedUser(AuthUtil.getAuditId());

            update(entity);
        } catch (EntityRetrievalException e) {
            LOGGER.catching(e);
            return;
        }
    }

    public List<CertifiedProductDetailsDTO> getCertifiedProductsByTestTool(TestTool testTool) throws EntityRetrievalException {
        List<Long> certifiedProductIds = getCertifiedProductIdsUsingTestToolId(testTool.getId());
        return certifiedProductDAO.getDetailsByIds(certifiedProductIds);
    }

    @CacheEvict(value = CacheNames.TEST_TOOL_MAPS, allEntries = true)
    public void remove(TestTool testTool) throws EntityRetrievalException {
        TestToolEntity entity = getEntityById(testTool.getId());
        entity.setDeleted(true);
        entity.setLastModifiedUser(AuthUtil.getAuditId());
        entity.setLastModifiedDate(new Date());
        update(entity);
    }

    public TestToolEntity getEntityById(Long id) {
        TestToolEntity entity = null;
        Query query = entityManager.createQuery("SELECT DISTINCT tt "
                + "FROM TestToolEntity tt "
                + "LEFT JOIN FETCH tt.criteria crit "
                + "LEFT JOIN FETCH crit.certificationEdition "
                + "LEFT JOIN FETCH crit.rule "
                + "WHERE (NOT tt.deleted = true) "
                + "AND (tt.id = :entityid) ", TestToolEntity.class);
        query.setParameter("entityid", id);
        List<TestToolEntity> result = query.getResultList();
        if (result.size() > 0) {
            entity = result.get(0);
        }
        return entity;
    }

    private List<TestToolEntity> getEntitiesByName(String name) {
        Query query = entityManager.createQuery("SELECT DISTINCT tt "
                + "FROM TestToolEntity tt "
                + "INNER JOIN FETCH tt.criteria c "
                + "LEFT JOIN FETCH c.certificationEdition "
                + "LEFT JOIN FETCH c.rule "
                + "WHERE (NOT tt.deleted = true) "
                + "AND (UPPER(tt.value) = :name) ", TestToolEntity.class);
        query.setParameter("name", name.toUpperCase());
        List<TestToolEntity> result = query.getResultList();

        return result;
    }

    private List<TestToolCriteriaMapEntity> getAllTestToolCriteriaMapEntities() throws EntityRetrievalException {
        return entityManager.createQuery("SELECT DISTINCT ttm "
                        + "FROM TestToolCriteriaMapEntity ttm "
                        + "JOIN FETCH ttm.criteria c "
                        + "LEFT JOIN FETCH c.certificationEdition "
                        + "LEFT JOIN FETCH c.rule "
                        + "JOIN FETCH ttm.testTool tt "
                        + "WHERE ttm.deleted <> true "
                        + "AND tt.deleted <> true ",
                        TestToolCriteriaMapEntity.class)
                .getResultList();
    }

    private List<TestToolEntity> getAllEntities() {
        return entityManager.createQuery("SELECT DISTINCT tt "
                + "FROM TestToolEntity tt "
                + "LEFT JOIN FETCH tt.criteria crit "
                + "LEFT JOIN FETCH crit.certificationEdition "
                + "LEFT JOIN FETCH crit.rule "
                + "WHERE tt.deleted <> true ", TestToolEntity.class)
                .getResultList();
    }

    private List<Long> getCertifiedProductIdsUsingTestToolIdWithCriterion(Long testToolId, Long criterionId) {
        List<CertificationResultEntity> certResultsWithTestTool =
                entityManager.createQuery("SELECT cr "
                        + "FROM CertificationResultTestToolEntity crt, CertificationResultEntity cr "
                        + "WHERE crt.certificationResultId = cr.id "
                        + "AND crt.testTool.id = :testToolId "
                        + "AND cr.certificationCriterionId= :criterionId "
                        + "AND crt.deleted <> true "
                        + "AND cr.deleted <> true ",
                        CertificationResultEntity.class)
                .setParameter("testToolId", testToolId)
                .setParameter("criterionId", criterionId)
                .getResultList();

        return certResultsWithTestTool.stream()
                .map(certResult -> certResult.getCertifiedProductId())
                .distinct()
                .collect(Collectors.toList());
    }

    private TestToolCriteriaMapEntity getTestToolCriteriaMapByTestToolAndCriterionEntity(Long testToolId, Long certificationCriterionId) throws EntityRetrievalException {
        List<TestToolCriteriaMapEntity> result = entityManager.createQuery("SELECT DISTINCT ttcm "
                        + "FROM TestToolCriteriaMapEntity ttcm "
                        + "JOIN FETCH ttcm.criteria c "
                        + "JOIN FETCH ttcm.testTool tt "
                        + "WHERE c.id = :certificationCriterionId "
                        + "AND tt.id= :testToolId "
                        + "AND ttcm.deleted <> true "
                        + "AND tt.deleted <> true "
                        + "AND c.deleted <> true",
                        TestToolCriteriaMapEntity.class)
                .setParameter("testToolId", testToolId)
                .setParameter("certificationCriterionId", certificationCriterionId)
                .getResultList();

        if (result.size() > 1) {
            throw new EntityRetrievalException("Data error. Duplicate test tool criteria map id in database.");
        } else if (result.size() == 0) {
            throw new EntityRetrievalException("Data error. Could not locate test tool criteria map {" + testToolId + ", " + certificationCriterionId + "} in database.");
        }

        return result.get(0);
    }

    private List<Long> getCertifiedProductIdsUsingTestToolId(Long testToolId) {
        List<CertificationResultEntity> certResultsWithTestTool =
                entityManager.createQuery("SELECT cr "
                        + "FROM CertificationResultTestToolEntity crtt, CertificationResultEntity cr "
                        + "WHERE crtt.certificationResultId = cr.id "
                        + "AND crtt.testTool.id = :testToolId "
                        + "AND crtt.deleted <> true "
                        + "AND cr.deleted <> true ",
                        CertificationResultEntity.class)
                .setParameter("testToolId", testToolId)
                .getResultList();

        return certResultsWithTestTool.stream()
                .map(certResult -> certResult.getCertifiedProductId())
                .distinct()
                .collect(Collectors.toList());
    }
}

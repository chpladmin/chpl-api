package gov.healthit.chpl.criteriaattribute.testtool;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Query;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.criteriaattribute.CriteriaAttribute;
import gov.healthit.chpl.criteriaattribute.CriteriaAttributeCriteriaMap;
import gov.healthit.chpl.criteriaattribute.CriteriaAttributeDAO;
import gov.healthit.chpl.criteriaattribute.rule.RuleDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.TestToolCriteriaMap;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.entity.listing.CertificationResultEntity;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.AuthUtil;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Repository("testToolDAO")
public class TestToolDAO extends BaseDAOImpl implements CriteriaAttributeDAO {
    private CertifiedProductDAO certifiedProductDAO;
    private RuleDAO ruleDAO;

    @Autowired
    public TestToolDAO(CertifiedProductDAO certifiedProductDAO, RuleDAO ruleDAO) {
        this.certifiedProductDAO = certifiedProductDAO;
        this.ruleDAO = ruleDAO;
    }

    @Override
    public CriteriaAttribute add(CriteriaAttribute criteriaAttribute) {
        TestToolEntity entity = TestToolEntity.builder()
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

    public List<TestTool> getAll() {
        return getAllEntities().stream()
                .map(entity -> entity.toDomain())
                .toList();
    }

    public TestTool getById(Long id) {
        TestToolEntity entity = getEntityById(id);
        if (entity == null) {
            return null;
        }
        return entity.toDomain();
    }


    public TestTool getByName(String name) {
        List<TestToolEntity> entities = getEntitiesByName(name);
        if (CollectionUtils.isEmpty(entities)) {
            return null;
        }
        return entities.get(0).toDomain();
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

    @Transactional
    public List<TestToolCriteriaMap> getAllTestToolCriteriaMap() throws EntityRetrievalException {
        return getAllTestToolCriteriaMapEntities().stream()
                .map(e -> e.toDomain())
                .collect(Collectors.toList());
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
        TestToolEntity entity = getEntityById(criteriaAttribute.getId());
        entity.setDeleted(true);
        entity.setLastModifiedUser(AuthUtil.getAuditId());
        entity.setLastModifiedDate(new Date());
        update(entity);
    }

    public TestToolEntity getEntityById(Long id) {
        TestToolEntity entity = null;
        Query query = entityManager.createQuery(
                "FROM TestToolEntity "
                + "WHERE (NOT deleted = true) "
                + "AND (test_tool_id = :entityid) ", TestToolEntity.class);
        query.setParameter("entityid", id);
        List<TestToolEntity> result = query.getResultList();
        if (result.size() > 0) {
            entity = result.get(0);
        }
        return entity;
    }

    private List<TestToolEntity> getEntitiesByName(String name) {
        Query query = entityManager.createQuery(
                "FROM TestToolEntity tt "
                + "INNER JOIN FETCH tt.criteria c "
                + "LEFT JOIN FETCH c.certificationEdition "
                + "LEFT JOIN FETCH c.rule "
                + "WHERE (NOT tt.deleted = true) "
                + "AND (UPPER(tt.name) = :name) ", TestToolEntity.class);
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
                + "LEFT JOIN FETCH tt.criteria "
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

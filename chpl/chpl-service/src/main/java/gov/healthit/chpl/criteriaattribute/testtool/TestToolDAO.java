package gov.healthit.chpl.criteriaattribute.testtool;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.criteriaattribute.CriteriaAttribute;
import gov.healthit.chpl.criteriaattribute.CriteriaAttributeCriteriaMap;
import gov.healthit.chpl.criteriaattribute.CriteriaAttributeDAO;
import gov.healthit.chpl.criteriaattribute.RuleEntity;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.TestToolCriteriaMap;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.entity.listing.CertificationResultEntity;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.AuthUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Repository("testToolDAO")
public class TestToolDAO extends BaseDAOImpl implements CriteriaAttributeDAO {
    private ErrorMessageUtil msgUtil;
    private CertifiedProductDAO certifiedProductDAO;

    @Autowired
    public TestToolDAO(ErrorMessageUtil msgUtil, CertifiedProductDAO certifiedProductDAO) {
        this.msgUtil = msgUtil;
        this.certifiedProductDAO = certifiedProductDAO;
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

    @Override
    public CriteriaAttribute getCriteriaAttributeById(Long id) {
        return getById(id);
    }

    public TestTool getByName(String name) {
        List<TestToolEntity> entities = getEntitiesByName(name);
        if (entities == null || entities.size() > 0) {
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

    //TODO OCD-4242
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

    public TestTool update(TestTool testTool) throws EntityRetrievalException {
        TestToolEntity entity = getEntityById(testTool.getId());

        entity.setValue(testTool.getValue());
        entity.setRegulatoryTextCitation(testTool.getRegulatoryTextCitation());
        entity.setStartDay(testTool.getStartDay());
        entity.setEndDay(testTool.getEndDay());
        entity.setRequiredDay(testTool.getRequiredDay());
        if (testTool.getRule() != null) {
            entity.setRule(getRuleEntityById(testTool.getRule().getId()));
        } else {
            entity.setRule(null);
        }

        entity.setLastModifiedUser(AuthUtil.getAuditId());
        entity.setLastModifiedDate(new Date());

        update(entity);

        return getById(entity.getId());
    }

    public void addTestToolCriteriMap(TestTool testTool, CertificationCriterion criterion) {
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

    public void removeTestToolCriteriaMap(TestTool testTool, CertificationCriterion criterion) {
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

    private TestToolEntity getEntityById(Long id) {
        TestToolEntity entity = null;
        Query query = entityManager.createQuery(
                "from TestToolEntity where (NOT deleted = true) AND (test_tool_id = :entityid) ", TestToolEntity.class);
        query.setParameter("entityid", id);
        List<TestToolEntity> result = query.getResultList();
        if (result.size() > 0) {
            entity = result.get(0);
        }
        return entity;
    }

    private List<TestToolEntity> getEntitiesByName(String name) {
        Query query = entityManager.createQuery(
                "from TestToolEntity where " + "(NOT deleted = true) AND (UPPER(name) = :name) ", TestToolEntity.class);
        query.setParameter("name", name.toUpperCase());
        List<TestToolEntity> result = query.getResultList();

        return result;
    }

    private List<TestToolCriteriaMapEntity> getAllTestToolCriteriaMapEntities() throws EntityRetrievalException {
        return entityManager.createQuery("SELECT DISTINCT ttm "
                        + "FROM TestToolCriteriaMapEntity ttm "
                        + "JOIN FETCH ttm.criteria c "
                        + "JOIN FETCH c.certificationEdition "
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
                        + "AND crt.testToolId = :testToolId "
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

    private RuleEntity getRuleEntityById(Long id) {
        RuleEntity entity = null;
        Query query = entityManager.createQuery(
                "FROM RuleEntity "
                + "WHERE (NOT deleted = true) "
                + "AND (id = :entityid) ", RuleEntity.class);
        query.setParameter("entityid", id);
        List<RuleEntity> result = query.getResultList();
        if (result.size() > 0) {
            entity = result.get(0);
        }
        return entity;
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
}

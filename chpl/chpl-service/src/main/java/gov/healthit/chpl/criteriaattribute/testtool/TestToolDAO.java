package gov.healthit.chpl.criteriaattribute.testtool;

import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.TestToolCriteriaMap;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Repository("testToolDAO")
public class TestToolDAO extends BaseDAOImpl {
    private ErrorMessageUtil msgUtil;

    @Autowired
    public TestToolDAO(ErrorMessageUtil msgUtil) {
        this.msgUtil = msgUtil;
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
        if (entities == null || entities.size() > 0) {
            return null;
        }
        return entities.get(0).toDomain();
    }

    //TODO OCD-4242
    @Transactional
    public List<TestToolCriteriaMap> getAllTestToolCriteriaMap() throws EntityRetrievalException {
        return getAllTestToolCriteriaMapEntities().stream()
                .map(e -> e.toDomain())
                .collect(Collectors.toList());
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
}

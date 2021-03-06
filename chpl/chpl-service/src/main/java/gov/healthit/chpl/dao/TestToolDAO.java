package gov.healthit.chpl.dao;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.dto.TestToolDTO;
import gov.healthit.chpl.entity.TestToolEntity;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Repository("testToolDAO")
public class TestToolDAO extends BaseDAOImpl {
    private ErrorMessageUtil msgUtil;

    @Autowired
    public TestToolDAO(ErrorMessageUtil msgUtil) {
        this.msgUtil = msgUtil;
    }

    public TestToolDTO getById(Long id) {
        TestToolDTO dto = null;
        TestToolEntity entity = getEntityById(id);

        if (entity != null) {
            dto = new TestToolDTO(entity);
        }
        return dto;
    }

    public TestToolDTO getByName(String name) {
        TestToolDTO dto = null;
        List<TestToolEntity> entities = getEntitiesByName(name);

        if (entities != null && entities.size() > 0) {
            dto = new TestToolDTO(entities.get(0));
        }
        return dto;
    }

    public List<TestToolDTO> findAll() {
        List<TestToolEntity> entities = getAllEntities();
        List<TestToolDTO> dtos = new ArrayList<TestToolDTO>();

        for (TestToolEntity entity : entities) {
            TestToolDTO dto = new TestToolDTO(entity);
            dtos.add(dto);
        }
        return dtos;

    }

    private List<TestToolEntity> getAllEntities() {
        return entityManager.createQuery("from TestToolEntity where (NOT deleted = true) ", TestToolEntity.class)
                .getResultList();
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
}

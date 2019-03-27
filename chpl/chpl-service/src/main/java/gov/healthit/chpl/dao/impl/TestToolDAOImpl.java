package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.dao.TestToolDAO;
import gov.healthit.chpl.dto.TestToolDTO;
import gov.healthit.chpl.entity.TestToolEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;

@Repository("testToolDAO")
public class TestToolDAOImpl extends BaseDAOImpl implements TestToolDAO {
    private static final Logger LOGGER = LogManager.getLogger(TestToolDAOImpl.class);
    @Autowired
    MessageSource messageSource;

    @Override
    public TestToolDTO create(TestToolDTO dto) throws EntityCreationException, EntityRetrievalException {

        TestToolEntity entity = null;
        if (dto.getId() != null) {
            entity = this.getEntityById(dto.getId());
        }

        if (entity != null) {
            throw new EntityCreationException("An entity with this ID already exists.");
        } else {
            entity = new TestToolEntity();
            entity.setCreationDate(new Date());
            entity.setDeleted(false);
            entity.setLastModifiedDate(new Date());
            entity.setLastModifiedUser(Util.getAuditId());
            entity.setName(dto.getName());
            entity.setDescription(dto.getDescription());
            entity.setRetired(false);

            try {
                create(entity);
            } catch (Exception ex) {
                String msg = String.format(
                        messageSource.getMessage(new DefaultMessageSourceResolvable("listing.criteria.badTestTool"),
                                LocaleContextHolder.getLocale()),
                        dto.getName());
                LOGGER.error(msg, ex);
                throw new EntityCreationException(msg);
            }
            return new TestToolDTO(entity);
        }
    }

    @Override
    public TestToolDTO update(TestToolDTO dto) throws EntityRetrievalException {
        TestToolEntity entity = this.getEntityById(dto.getId());

        if (entity == null) {
            throw new EntityRetrievalException("Entity with id " + dto.getId() + " does not exist");
        }

        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setRetired(dto.isRetired());
        entity.setLastModifiedUser(Util.getAuditId());
        entity.setLastModifiedDate(new Date());

        update(entity);
        return new TestToolDTO(entity);
    }

    @Override
    public void delete(Long id) throws EntityRetrievalException {

        TestToolEntity toDelete = getEntityById(id);

        if (toDelete != null) {
            toDelete.setDeleted(true);
            toDelete.setLastModifiedDate(new Date());
            toDelete.setLastModifiedUser(Util.getAuditId());
            update(toDelete);
        }
    }

    @Override
    public TestToolDTO getById(Long id) {

        TestToolDTO dto = null;
        TestToolEntity entity = getEntityById(id);

        if (entity != null) {
            dto = new TestToolDTO(entity);
        }
        return dto;
    }

    @Override
    public TestToolDTO getByName(String name) {

        TestToolDTO dto = null;
        List<TestToolEntity> entities = getEntitiesByName(name);

        if (entities != null && entities.size() > 0) {
            dto = new TestToolDTO(entities.get(0));
        }
        return dto;
    }

    @Override
    public List<TestToolDTO> findAll() {

        List<TestToolEntity> entities = getAllEntities();
        List<TestToolDTO> dtos = new ArrayList<TestToolDTO>();

        for (TestToolEntity entity : entities) {
            TestToolDTO dto = new TestToolDTO(entity);
            dtos.add(dto);
        }
        return dtos;

    }

    private void create(TestToolEntity entity) {

        entityManager.persist(entity);
        entityManager.flush();

    }

    private void update(TestToolEntity entity) {

        entityManager.merge(entity);
        entityManager.flush();
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

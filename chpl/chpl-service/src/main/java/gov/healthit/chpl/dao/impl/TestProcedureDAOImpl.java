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
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.TestProcedureDAO;
import gov.healthit.chpl.dto.TestProcedureDTO;
import gov.healthit.chpl.entity.TestProcedureEntity;

@Repository("testProcedureDAO")
public class TestProcedureDAOImpl extends BaseDAOImpl implements TestProcedureDAO {
    private static final Logger LOGGER = LogManager.getLogger(TestProcedureDAOImpl.class);
    @Autowired
    MessageSource messageSource;

    @Override
    public TestProcedureDTO create(TestProcedureDTO dto) throws EntityCreationException {

        TestProcedureEntity entity = null;
        if (dto.getId() != null) {
            entity = this.getEntityById(dto.getId());
        }

        if (entity != null) {
            throw new EntityCreationException("An entity with this ID already exists.");
        } else {
            entity = new TestProcedureEntity();
            entity.setCreationDate(new Date());
            entity.setDeleted(false);
            entity.setLastModifiedDate(new Date());
            entity.setLastModifiedUser(Util.getCurrentUser().getId());
            entity.setVersion(dto.getVersion());

            try {
                create(entity);
            } catch (Exception ex) {
                String msg = String.format(messageSource.getMessage(
                        new DefaultMessageSourceResolvable("listing.criteria.badTestProcedure"),
                        LocaleContextHolder.getLocale()), dto.getVersion());
                LOGGER.error(msg, ex);
                throw new EntityCreationException(msg);
            }
            return new TestProcedureDTO(entity);
        }
    }

    @Override
    public TestProcedureDTO update(TestProcedureDTO dto) throws EntityRetrievalException {
        TestProcedureEntity entity = this.getEntityById(dto.getId());

        if (entity == null) {
            throw new EntityRetrievalException("Entity with id " + dto.getId() + " does not exist");
        }

        entity.setVersion(dto.getVersion());
        entity.setLastModifiedUser(Util.getCurrentUser().getId());
        entity.setLastModifiedDate(new Date());

        update(entity);
        return new TestProcedureDTO(entity);
    }

    @Override
    public void delete(Long id) throws EntityRetrievalException {

        TestProcedureEntity toDelete = getEntityById(id);

        if (toDelete != null) {
            toDelete.setDeleted(true);
            toDelete.setLastModifiedDate(new Date());
            toDelete.setLastModifiedUser(Util.getCurrentUser().getId());
            update(toDelete);
        }
    }

    @Override
    public TestProcedureDTO getById(Long id) {

        TestProcedureDTO dto = null;
        TestProcedureEntity entity = getEntityById(id);

        if (entity != null) {
            dto = new TestProcedureDTO(entity);
        }
        return dto;
    }

    @Override
    public TestProcedureDTO getByName(String versionName) {

        TestProcedureDTO dto = null;
        List<TestProcedureEntity> entities = getEntitiesByVersion(versionName);

        if (entities != null && entities.size() > 0) {
            dto = new TestProcedureDTO(entities.get(0));
        }
        return dto;
    }

    @Override
    public List<TestProcedureDTO> findAll() {

        List<TestProcedureEntity> entities = getAllEntities();
        List<TestProcedureDTO> dtos = new ArrayList<TestProcedureDTO>();

        for (TestProcedureEntity entity : entities) {
            TestProcedureDTO dto = new TestProcedureDTO(entity);
            dtos.add(dto);
        }
        return dtos;

    }

    private void create(TestProcedureEntity entity) {

        entityManager.persist(entity);
        entityManager.flush();

    }

    private void update(TestProcedureEntity entity) {

        entityManager.merge(entity);
        entityManager.flush();
    }

    private List<TestProcedureEntity> getAllEntities() {
        return entityManager
                .createQuery("from TestProcedureEntity where (NOT deleted = true) ", TestProcedureEntity.class)
                .getResultList();
    }

    private TestProcedureEntity getEntityById(Long id) {

        TestProcedureEntity entity = null;

        Query query = entityManager.createQuery(
                "from TestProcedureEntity where (NOT deleted = true) AND (id = :entityid) ", TestProcedureEntity.class);
        query.setParameter("entityid", id);
        List<TestProcedureEntity> result = query.getResultList();

        if (result.size() > 0) {
            entity = result.get(0);
        }

        return entity;
    }

    private List<TestProcedureEntity> getEntitiesByVersion(String versionName) {

        TestProcedureEntity entity = null;

        Query query = entityManager.createQuery(
                "from TestProcedureEntity where " + "(NOT deleted = true) AND (UPPER(version) = :versionName) ",
                TestProcedureEntity.class);
        query.setParameter("versionName", versionName.toUpperCase());
        return query.getResultList();
    }
}

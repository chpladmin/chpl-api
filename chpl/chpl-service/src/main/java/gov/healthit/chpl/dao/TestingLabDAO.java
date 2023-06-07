package gov.healthit.chpl.dao;

import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Query;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.TestingLab;
import gov.healthit.chpl.entity.AddressEntity;
import gov.healthit.chpl.entity.TestingLabEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.AuthUtil;

@Repository("testingLabDAO")
public class TestingLabDAO extends BaseDAOImpl {
    private static final Logger LOGGER = LogManager.getLogger(TestingLabDAO.class);

    private AddressDAO addressDao;

    @Autowired
    public TestingLabDAO(AddressDAO addressDao) {
        this.addressDao = addressDao;
    }


    @Transactional
    public TestingLab create(TestingLab atlToCreate) throws EntityCreationException, EntityRetrievalException {

        TestingLabEntity entity = null;
        try {
            if (atlToCreate.getId() != null) {
                entity = this.getEntityById(atlToCreate.getId());
            }
        } catch (final EntityRetrievalException e) {
            throw new EntityCreationException(e);
        }

        if (entity != null) {
            throw new EntityCreationException("An entity with this ID already exists.");
        } else {
            entity = new TestingLabEntity();

            if (atlToCreate.getAddress() != null) {
                Long addressId = addressDao.saveAddress(atlToCreate.getAddress());
                AddressEntity addressEntity = addressDao.getEntityById(addressId);
                entity.setAddress(addressEntity);
            }

            entity.setName(atlToCreate.getName());
            entity.setWebsite(atlToCreate.getWebsite());
            entity.setTestingLabCode(atlToCreate.getAtlCode());
            entity.setRetired(atlToCreate.isRetired());
            entity.setRetirementDate(atlToCreate.getRetirementDay());
            entity.setLastModifiedUser(AuthUtil.getAuditId());
            create(entity);
            return entity.toDomain();
        }
    }

    @Transactional
    public TestingLab update(TestingLab atlToUpdate) throws EntityRetrievalException {
        TestingLabEntity entity = this.getEntityById(atlToUpdate.getId());

        if (entity == null) {
            throw new EntityRetrievalException("Entity with id " + atlToUpdate.getId() + " does not exist");
        }

        if (atlToUpdate.getAddress() != null) {
            try {
                Long addressId = addressDao.saveAddress(atlToUpdate.getAddress());
                AddressEntity addressEntity = addressDao.getEntityById(addressId);
                entity.setAddress(addressEntity);
            } catch (final EntityCreationException ex) {
                LOGGER.error("Could not create new address in the database.", ex);
                entity.setAddress(null);
            }
        } else {
            entity.setAddress(null);
        }

        entity.setWebsite(atlToUpdate.getWebsite());
        entity.setRetired(atlToUpdate.isRetired());
        entity.setRetirementDate(atlToUpdate.getRetirementDay());

        if (atlToUpdate.getName() != null) {
            entity.setName(atlToUpdate.getName());
        }

        if (atlToUpdate.getAtlCode() != null) {
            entity.setTestingLabCode(atlToUpdate.getAtlCode());
        }
        entity.setLastModifiedUser(AuthUtil.getAuditId());
        update(entity);
        return entity.toDomain();
    }

    public List<TestingLab> findAll() {

        List<TestingLabEntity> entities = getAllEntities();
        return entities.stream()
                .map(entity -> entity.toDomain())
                .collect(Collectors.toList());
    }

    public TestingLab getById(Long id) throws EntityRetrievalException {
        TestingLabEntity entity = getEntityById(id);
        if (entity != null) {
            return entity.toDomain();
        }
        return null;
    }

    public TestingLab getByName(String name) {
        TestingLabEntity entity = getEntityByName(name);
        if (entity != null) {
            return entity.toDomain();
        }
        return null;
    }

    public TestingLab getByCode(String code) {
        TestingLabEntity entity = null;
        Query query = entityManager
                .createQuery(
                        "SELECT atl from TestingLabEntity atl "
                                + "LEFT OUTER JOIN FETCH atl.address "
                                + "WHERE (atl.deleted = false) "
                                + "AND (UPPER(atl.testingLabCode) = :code) ",
                        TestingLabEntity.class);
        query.setParameter("code", code.toUpperCase());
        List<TestingLabEntity> result = query.getResultList();

        if (result.size() > 0) {
            entity = result.get(0);
        } else {
            return null;
        }
        return entity.toDomain();
    }

    public List<TestingLab> getByWebsite(String website) {
        Query query = entityManager.createQuery("SELECT atl "
                + "FROM TestingLabEntity atl "
                + "LEFT OUTER JOIN FETCH atl.address "
                + "WHERE atl.deleted = false "
                + "AND atl.website = :website");
        query.setParameter("website", website);
        List<TestingLabEntity> entities = query.getResultList();
        return entities.stream()
                .map(entity -> entity.toDomain())
                .collect(Collectors.toList());
    }

    public String getMaxCode() {
        String maxCode = null;
        Query query = entityManager.createQuery(
                "SELECT atl.testingLabCode "
                        + "from TestingLabEntity atl "
                        + "ORDER BY atl.testingLabCode DESC",
                String.class);
        List<String> result = query.getResultList();

        if (result != null && result.size() > 0) {
            maxCode = result.get(0);
        }
        return maxCode;
    }

    private List<TestingLabEntity> getAllEntities() {
        return entityManager.createQuery("SELECT atl from TestingLabEntity atl "
                + "LEFT OUTER JOIN FETCH atl.address "
                + "where (atl.deleted = false)", TestingLabEntity.class)
                    .getResultList();
    }

    private TestingLabEntity getEntityById(Long id) throws EntityRetrievalException {
        TestingLabEntity entity = null;

        String queryStr = "SELECT atl from TestingLabEntity atl "
                + "LEFT OUTER JOIN FETCH atl.address "
                + "WHERE (atl.id = :entityid) "
                + " AND (atl.deleted = false)";
        Query query = entityManager.createQuery(queryStr, TestingLabEntity.class);
        query.setParameter("entityid", id);
        List<TestingLabEntity> result = query.getResultList();

        if (result == null || result.size() == 0) {
            String msg = msgUtil.getMessage("atl.notFound");
            throw new EntityRetrievalException(msg);
        } else if (result.size() > 1) {
            throw new EntityRetrievalException("Data error. Duplicate testing lab id in database.");
        } else if (result.size() == 1) {
            entity = result.get(0);
        }
        return entity;
    }

    private TestingLabEntity getEntityByName(String name) {
        TestingLabEntity entity = null;
        Query query = entityManager
                .createQuery(
                        "SELECT atl from TestingLabEntity atl "
                                + "LEFT OUTER JOIN FETCH atl.address "
                                + "WHERE (atl.deleted = false) "
                                + "AND (UPPER(atl.name) = :name) ",
                        TestingLabEntity.class);
        query.setParameter("name", name.toUpperCase());
        List<TestingLabEntity> result = query.getResultList();

        if (result.size() > 0) {
            entity = result.get(0);
        }

        return entity;
    }
}

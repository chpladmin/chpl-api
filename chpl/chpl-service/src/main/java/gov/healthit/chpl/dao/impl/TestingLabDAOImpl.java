package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.AddressDAO;
import gov.healthit.chpl.dao.TestingLabDAO;
import gov.healthit.chpl.dto.TestingLabDTO;
import gov.healthit.chpl.entity.TestingLabEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.AuthUtil;

/**
 * Data access methods for testing labs (ATLs).
 * @author kekey
 *
 */
@Repository("testingLabDAO")
public class TestingLabDAOImpl extends BaseDAOImpl implements TestingLabDAO {

    private static final Logger LOGGER = LogManager.getLogger(TestingLabDAOImpl.class);
    @Autowired
    AddressDAO addressDao;

    /**
     * Create an ATL.
     */
    @Override
    @Transactional
    public TestingLabDTO create(final TestingLabDTO dto) throws EntityCreationException, EntityRetrievalException {

        TestingLabEntity entity = null;
        try {
            if (dto.getId() != null) {
                entity = this.getEntityById(dto.getId());
            }
        } catch (final EntityRetrievalException e) {
            throw new EntityCreationException(e);
        }

        if (entity != null) {
            throw new EntityCreationException("An entity with this ID already exists.");
        } else {
            entity = new TestingLabEntity();

            if (dto.getAddress() != null) {
                entity.setAddress(addressDao.saveAddress(dto.getAddress()));
            }

            entity.setName(dto.getName());
            entity.setWebsite(dto.getWebsite());
            entity.setAccredidationNumber(dto.getAccredidationNumber());
            entity.setTestingLabCode(dto.getTestingLabCode());
            entity.setRetired(dto.isRetired());
            entity.setRetirementDate(dto.getRetirementDate());
            entity.setLastModifiedUser(AuthUtil.getAuditId());
            create(entity);
            return new TestingLabDTO(entity);
        }
    }

    /**
     * Update an ATL.
     */
    @Override
    @Transactional
    public TestingLabDTO update(final TestingLabDTO dto) throws EntityRetrievalException {
        TestingLabEntity entity = this.getEntityById(dto.getId());

        if (entity == null) {
            throw new EntityRetrievalException("Entity with id " + dto.getId() + " does not exist");
        }

        if (dto.getAddress() != null) {
            try {
                entity.setAddress(addressDao.saveAddress(dto.getAddress()));
            } catch (final EntityCreationException ex) {
                LOGGER.error("Could not create new address in the database.", ex);
                entity.setAddress(null);
            }
        } else {
            entity.setAddress(null);
        }

        entity.setWebsite(dto.getWebsite());
        entity.setAccredidationNumber(dto.getAccredidationNumber());
        entity.setRetired(dto.isRetired());
        entity.setRetirementDate(dto.getRetirementDate());

        if (dto.getName() != null) {
            entity.setName(dto.getName());
        }

        if (dto.getTestingLabCode() != null) {
            entity.setTestingLabCode(dto.getTestingLabCode());
        }
        entity.setLastModifiedUser(AuthUtil.getAuditId());
        update(entity);
        return new TestingLabDTO(entity);
    }

    /**
     * Get all ATLs.
     */
    @Override
    public List<TestingLabDTO> findAll() {

        List<TestingLabEntity> entities = getAllEntities();
        List<TestingLabDTO> dtos = new ArrayList<>();

        for (TestingLabEntity entity : entities) {
            TestingLabDTO dto = new TestingLabDTO(entity);
            dtos.add(dto);
        }
        return dtos;
    }

    /**
     * Get all activeATLs.
     */
    @Override
    public List<TestingLabDTO> findAllActive() {

        List<TestingLabEntity> entities = entityManager.createQuery(
                "SELECT atl from TestingLabEntity atl "
                + "LEFT OUTER JOIN FETCH atl.address "
                + "WHERE atl.retired = false AND atl.deleted = false", TestingLabEntity.class)
                    .getResultList();
        List<TestingLabDTO> dtos = new ArrayList<>();

        for (TestingLabEntity entity : entities) {
            TestingLabDTO dto = new TestingLabDTO(entity);
            dtos.add(dto);
        }
        return dtos;
    }

    /**
     * Gets a single ATL by ID.
     */
    @Override
    public TestingLabDTO getById(final Long id) throws EntityRetrievalException {

        TestingLabEntity entity = getEntityById(id);
        TestingLabDTO dto = null;
        if (entity != null) {
            dto = new TestingLabDTO(entity);
        }
        return dto;
    }

    /**
     * Get a single ATL by name.
     */
    @Override
    public TestingLabDTO getByName(final String name) {
        TestingLabEntity entity = getEntityByName(name);
        TestingLabDTO dto = null;
        if (entity != null) {
            dto = new TestingLabDTO(entity);
        }
        return dto;
    }

    /**
     * Find any ATLs with the given website.
     *
     * @param website
     * @return the ATLs
     */
    @Override
    public List<TestingLabDTO> getByWebsite(final String website) {
        Query query = entityManager.createQuery("SELECT atl "
                + "FROM TestingLabEntity atl "
                + "LEFT OUTER JOIN FETCH atl.address "
                + "WHERE atl.deleted = false "
                + "AND atl.website = :website");
        query.setParameter("website", website);
        List<TestingLabEntity> results = query.getResultList();
        List<TestingLabDTO> resultDtos = new ArrayList<TestingLabDTO>();
        for (TestingLabEntity entity : results) {
            resultDtos.add(new TestingLabDTO(entity));
        }
        return resultDtos;
    }

    /**
     * Get the current larget ATL code in the database.
     */
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

    private void create(final TestingLabEntity entity) {

        entityManager.persist(entity);
        entityManager.flush();
    }

    private void update(final TestingLabEntity entity) {

        entityManager.merge(entity);
        entityManager.flush();
    }

    private List<TestingLabEntity> getAllEntities() {
        return entityManager.createQuery("SELECT atl from TestingLabEntity atl "
                + "LEFT OUTER JOIN FETCH atl.address "
                + "where (atl.deleted = false)", TestingLabEntity.class)
                    .getResultList();
    }

    private TestingLabEntity getEntityById(final Long id) throws EntityRetrievalException {

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

    private TestingLabEntity getEntityByName(final String name) {
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

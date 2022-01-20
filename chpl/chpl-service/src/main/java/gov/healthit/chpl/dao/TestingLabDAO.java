package gov.healthit.chpl.dao;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.dto.TestingLabDTO;
import gov.healthit.chpl.entity.TestingLabEntity;
import gov.healthit.chpl.entity.UserTestingLabMapEntity;
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
    public TestingLabDTO create(TestingLabDTO dto) throws EntityCreationException, EntityRetrievalException {

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

    @Transactional
    public TestingLabDTO update(TestingLabDTO dto) throws EntityRetrievalException {
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

    public List<TestingLabDTO> findAll() {

        List<TestingLabEntity> entities = getAllEntities();
        List<TestingLabDTO> dtos = new ArrayList<>();

        for (TestingLabEntity entity : entities) {
            TestingLabDTO dto = new TestingLabDTO(entity);
            dtos.add(dto);
        }
        return dtos;
    }

    public TestingLabDTO getById(Long id) throws EntityRetrievalException {

        TestingLabEntity entity = getEntityById(id);
        TestingLabDTO dto = null;
        if (entity != null) {
            dto = new TestingLabDTO(entity);
        }
        return dto;
    }

    public TestingLabDTO getByName(String name) {
        TestingLabEntity entity = getEntityByName(name);
        TestingLabDTO dto = null;
        if (entity != null) {
            dto = new TestingLabDTO(entity);
        }
        return dto;
    }

    public TestingLabDTO getByCode(String code) {
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
        return new TestingLabDTO(entity);
    }

    public List<TestingLabDTO> getByWebsite(String website) {
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

    public List<TestingLabDTO> getTestingLabsByUserId(Long userId) {
        Query query = entityManager
                .createQuery("FROM UserTestingLabMapEntity utlm "
                        + "join fetch utlm.testingLab tl "
                        + "left join fetch tl.address "
                        + "join fetch utlm.user u "
                        + "join fetch u.permission perm "
                        + "join fetch u.contact contact "
                        + "where (utlm.deleted != true) AND (u.id = :userId) ",
                        UserTestingLabMapEntity.class);
        query.setParameter("userId", userId);
        List<UserTestingLabMapEntity> result = query.getResultList();

        List<TestingLabDTO> dtos = new ArrayList<TestingLabDTO>();
        if (result != null) {
            for (UserTestingLabMapEntity entity : result) {
                dtos.add(new TestingLabDTO(entity.getTestingLab()));
            }
        }
        return dtos;
    }

    private void create(TestingLabEntity entity) {

        entityManager.persist(entity);
        entityManager.flush();
    }

    private void update(TestingLabEntity entity) {

        entityManager.merge(entity);
        entityManager.flush();
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

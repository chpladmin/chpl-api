package gov.healthit.chpl.dao;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityNotFoundException;
import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.dto.CertificationEditionDTO;
import gov.healthit.chpl.dto.TestStandardDTO;
import gov.healthit.chpl.entity.TestStandardEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.util.AuthUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;
import lombok.extern.log4j.Log4j2;

@Repository("testStandardDAO")
@Log4j2
public class TestStandardDAO extends BaseDAOImpl {
    private ErrorMessageUtil msgUtil;
    private CertificationEditionDAO editionDao;

    @Autowired
    public TestStandardDAO(ErrorMessageUtil msgUtil, CertificationEditionDAO editionDao) {
        this.msgUtil = msgUtil;
        this.editionDao = editionDao;
    }

    public TestStandardDTO create(TestStandardDTO dto) throws EntityCreationException {
        TestStandardEntity entity = null;
        if (dto.getId() != null) {
            entity = this.getEntityById(dto.getId());
        }

        if (entity != null) {
            throw new EntityCreationException("An entity with this ID already exists.");
        } else {
            entity = new TestStandardEntity();
            entity.setName(dto.getName());
            entity.setDescription(dto.getDescription());
            if (dto.getCertificationEditionId() != null) {
                entity.setCertificationEditionId(dto.getCertificationEditionId());
            } else if (StringUtils.isNotEmpty(dto.getYear())) {
                CertificationEditionDTO editionByYear = editionDao.getByYear(dto.getYear());
                if (editionByYear != null) {
                    entity.setCertificationEditionId(editionByYear.getId());
                } else {
                    throw new EntityNotFoundException("No certification edition was found for year " + dto.getYear());
                }
            }
            entity.setDeleted(false);
            entity.setLastModifiedUser(AuthUtil.getAuditId());

            try {
                entityManager.persist(entity);
                entityManager.flush();
            } catch (Exception ex) {
                String msg = msgUtil.getMessage("listing.criteria.badTestStandard", dto.getName());
                LOGGER.error(msg, ex);
                throw new EntityCreationException(msg);
            }
            return new TestStandardDTO(entity);
        }
    }

    public TestStandardDTO getById(Long id) {
        TestStandardDTO dto = null;
        TestStandardEntity entity = getEntityById(id);

        if (entity != null) {
            dto = new TestStandardDTO(entity);
        }
        return dto;
    }

    public List<TestStandardDTO> findAll() {
        List<TestStandardEntity> entities = getAllEntities();
        List<TestStandardDTO> dtos = new ArrayList<TestStandardDTO>();

        for (TestStandardEntity entity : entities) {
            TestStandardDTO dto = new TestStandardDTO(entity);
            dtos.add(dto);
        }
        return dtos;

    }

    public TestStandardDTO getByNumberAndEdition(String number, Long editionId) {
        TestStandardDTO dto = null;
        List<TestStandardEntity> entities = getEntitiesByNumberAndYear(number, editionId);

        if (entities != null && entities.size() > 0) {
            dto = new TestStandardDTO(entities.get(0));
        }
        return dto;
    }

    private List<TestStandardEntity> getAllEntities() {
        return entityManager
                .createQuery("from TestStandardEntity where (NOT deleted = true) ", TestStandardEntity.class)
                .getResultList();
    }

    private TestStandardEntity getEntityById(Long id) {
        TestStandardEntity entity = null;

        Query query = entityManager.createQuery("SELECT ts " + "FROM TestStandardEntity ts "
                + "WHERE (NOT deleted = true) " + "AND (ts.id = :entityid) ", TestStandardEntity.class);
        query.setParameter("entityid", id);
        List<TestStandardEntity> result = query.getResultList();

        if (result.size() > 0) {
            entity = result.get(0);
        }
        return entity;
    }

    private List<TestStandardEntity> getEntitiesByNumberAndYear(String number, Long editionId) {
        TestStandardEntity entity = null;
        String tsQuery = "SELECT ts " + "FROM TestStandardEntity ts " + "JOIN FETCH ts.certificationEdition edition "
                + "WHERE ts.deleted <> true " + "AND UPPER(ts.name) = :number " + "AND edition.id = :editionId ";
        Query query = entityManager.createQuery(tsQuery, TestStandardEntity.class);
        query.setParameter("number", number.toUpperCase());
        query.setParameter("editionId", editionId);

        List<TestStandardEntity> matches = query.getResultList();
        if (matches == null || matches.size() == 0) {
            // if this didn't find anything try again with spaces removed from
            // the number
            query = entityManager.createQuery(tsQuery, TestStandardEntity.class);
            String numberWithoutSpaces = number.replaceAll("\\s", "");
            query.setParameter("number", numberWithoutSpaces.toUpperCase());
            query.setParameter("editionId", editionId);
            matches = query.getResultList();
        }
        return matches;
    }
}

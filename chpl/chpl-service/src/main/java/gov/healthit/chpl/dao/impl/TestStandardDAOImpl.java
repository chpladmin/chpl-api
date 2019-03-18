package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityNotFoundException;
import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.dao.CertificationEditionDAO;
import gov.healthit.chpl.dao.TestStandardDAO;
import gov.healthit.chpl.dto.CertificationEditionDTO;
import gov.healthit.chpl.dto.TestStandardDTO;
import gov.healthit.chpl.entity.TestStandardEntity;
import gov.healthit.chpl.exception.EntityCreationException;

@Repository("testStandardDAO")
public class TestStandardDAOImpl extends BaseDAOImpl implements TestStandardDAO {
    private static final Logger LOGGER = LogManager.getLogger(TestStandardDAOImpl.class);
    @Autowired
    MessageSource messageSource;
    @Autowired
    CertificationEditionDAO editionDao;

    @Override
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
            entity.setLastModifiedUser(Util.getAuditId());

            try {
                entityManager.persist(entity);
                entityManager.flush();
            } catch (Exception ex) {
                String msg = String.format(
                        messageSource.getMessage(new DefaultMessageSourceResolvable("listing.criteria.badTestStandard"),
                                LocaleContextHolder.getLocale()),
                        dto.getName());
                LOGGER.error(msg, ex);
                throw new EntityCreationException(msg);
            }
            return new TestStandardDTO(entity);
        }
    }

    @Override
    public TestStandardDTO getById(Long id) {
        TestStandardDTO dto = null;
        TestStandardEntity entity = getEntityById(id);

        if (entity != null) {
            dto = new TestStandardDTO(entity);
        }
        return dto;
    }

    @Override
    public List<TestStandardDTO> findAll() {
        List<TestStandardEntity> entities = getAllEntities();
        List<TestStandardDTO> dtos = new ArrayList<TestStandardDTO>();

        for (TestStandardEntity entity : entities) {
            TestStandardDTO dto = new TestStandardDTO(entity);
            dtos.add(dto);
        }
        return dtos;

    }

    @Override
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

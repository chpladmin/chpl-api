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

import gov.healthit.chpl.dao.TestParticipantDAO;
import gov.healthit.chpl.dto.TestParticipantDTO;
import gov.healthit.chpl.entity.TestParticipantEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.AuthUtil;

@Repository("testParticipantDAO")
public class TestParticipantDAOImpl extends BaseDAOImpl implements TestParticipantDAO {
    private static final Logger LOGGER = LogManager.getLogger(TestParticipantDAOImpl.class);
    @Autowired
    MessageSource messageSource;

    @Override
    public TestParticipantDTO create(TestParticipantDTO dto) throws EntityCreationException {

        TestParticipantEntity entity = null;
        if (dto.getId() != null) {
            entity = this.getEntityById(dto.getId());
        }

        if (entity == null) {
            entity = new TestParticipantEntity();
            entity.setCreationDate(new Date());
            entity.setDeleted(false);
            entity.setLastModifiedDate(new Date());
            entity.setLastModifiedUser(AuthUtil.getAuditId());
            entity.setAgeRangeId(dto.getAgeRangeId());
            entity.setAssistiveTechnologyNeeds(dto.getAssistiveTechnologyNeeds());
            entity.setComputerExperienceMonths(dto.getComputerExperienceMonths());
            entity.setEducationTypeId(dto.getEducationTypeId());
            entity.setGender(dto.getGender());
            entity.setOccupation(dto.getOccupation());
            entity.setProductExperienceMonths(dto.getProductExperienceMonths());
            entity.setProfessionalExperienceMonths(dto.getProfessionalExperienceMonths());

            try {
                create(entity);
            } catch (Exception ex) {
                String msg = String.format(messageSource.getMessage(
                        new DefaultMessageSourceResolvable("listing.criteria.badTestParticipant"),
                        LocaleContextHolder.getLocale()), dto.getGender() + ": " + dto.getOccupation());
                LOGGER.error(msg, ex);
                throw new EntityCreationException(msg);
            }
        }
        return new TestParticipantDTO(entity);
    }

    @Override
    public TestParticipantDTO update(TestParticipantDTO dto) throws EntityRetrievalException {
        TestParticipantEntity entity = this.getEntityById(dto.getId());

        if (entity == null) {
            throw new EntityRetrievalException("Entity with id " + dto.getId() + " does not exist");
        }

        entity.setAgeRangeId(dto.getAgeRangeId());
        entity.setAssistiveTechnologyNeeds(dto.getAssistiveTechnologyNeeds());
        entity.setComputerExperienceMonths(dto.getComputerExperienceMonths());
        entity.setEducationTypeId(dto.getEducationTypeId());
        entity.setGender(dto.getGender());
        entity.setOccupation(dto.getOccupation());
        entity.setProductExperienceMonths(dto.getProductExperienceMonths());
        entity.setProfessionalExperienceMonths(dto.getProfessionalExperienceMonths());
        entity.setLastModifiedUser(AuthUtil.getAuditId());
        entity.setLastModifiedDate(new Date());

        entity = update(entity);
        return new TestParticipantDTO(entity);
    }

    @Override
    public void delete(Long id) throws EntityRetrievalException {

        TestParticipantEntity toDelete = getEntityById(id);

        if (toDelete != null) {
            toDelete.setDeleted(true);
            toDelete.setLastModifiedDate(new Date());
            toDelete.setLastModifiedUser(AuthUtil.getAuditId());
            update(toDelete);
        }
    }

    @Override
    public TestParticipantDTO getById(Long id) {

        TestParticipantDTO dto = null;
        TestParticipantEntity entity = getEntityById(id);

        if (entity != null) {
            dto = new TestParticipantDTO(entity);
        }
        return dto;
    }

    @Override
    public List<TestParticipantDTO> findAll() {

        List<TestParticipantEntity> entities = getAllEntities();
        List<TestParticipantDTO> dtos = new ArrayList<TestParticipantDTO>();

        for (TestParticipantEntity entity : entities) {
            TestParticipantDTO dto = new TestParticipantDTO(entity);
            dtos.add(dto);
        }
        return dtos;

    }

    private void create(TestParticipantEntity entity) {

        entityManager.persist(entity);
        entityManager.flush();

    }

    private TestParticipantEntity update(TestParticipantEntity entity) {

        TestParticipantEntity result = entityManager.merge(entity);
        entityManager.flush();
        return result;
    }

    private List<TestParticipantEntity> getAllEntities() {
        return entityManager.createQuery(
                "SELECT tpe from TestParticipantEntity tpe " + "LEFT OUTER JOIN FETCH tpe.ageRange "
                        + "LEFT OUTER JOIN FETCH tpe.education " + "where (NOT tpe.deleted = true) ",
                TestParticipantEntity.class).getResultList();
    }

    private TestParticipantEntity getEntityById(Long id) {

        TestParticipantEntity entity = null;

        Query query = entityManager.createQuery("SELECT tpe from TestParticipantEntity tpe "
                + "LEFT OUTER JOIN FETCH tpe.ageRange " + "LEFT OUTER JOIN FETCH tpe.education "
                + "where (NOT tpe.deleted = true) AND (tpe.id = :entityid)", TestParticipantEntity.class);
        query.setParameter("entityid", id);
        List<TestParticipantEntity> result = query.getResultList();

        if (result.size() > 0) {
            entity = result.get(0);
        }

        return entity;
    }
}

package gov.healthit.chpl.dao;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.TestParticipant;
import gov.healthit.chpl.dto.TestParticipantDTO;
import gov.healthit.chpl.entity.TestParticipantEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.ErrorMessageUtil;
import lombok.extern.log4j.Log4j2;

@Repository("testParticipantDAO")
@Log4j2
public class TestParticipantDAO extends BaseDAOImpl {
    private ErrorMessageUtil msgUtil;

    @Autowired
    public TestParticipantDAO(ErrorMessageUtil msgUtil) {
        this.msgUtil = msgUtil;
    }

    public Long create(TestParticipant participant) throws EntityCreationException {
        TestParticipantEntity entity = new TestParticipantEntity();
        try {
            entity.setAgeRangeId(participant.getAgeRangeId());
            entity.setAssistiveTechnologyNeeds(participant.getAssistiveTechnologyNeeds());
            entity.setComputerExperienceMonths(participant.getComputerExperienceMonths());
            entity.setEducationTypeId(participant.getEducationTypeId());
            entity.setGender(participant.getGender());
            entity.setOccupation(participant.getOccupation());
            entity.setProductExperienceMonths(participant.getProductExperienceMonths());
            entity.setProfessionalExperienceMonths(participant.getProfessionalExperienceMonths());
            create(entity);
        } catch (Exception ex) {
            String msg = msgUtil.getMessage("listing.criteria.badTestParticipant",
                    participant.getGender() + ": " + participant.getOccupation());
            LOGGER.error(msg, ex);
            throw new EntityCreationException(msg);
        }
        return entity.getId();
    }

    public TestParticipantDTO create(TestParticipantDTO dto) throws EntityCreationException {
        TestParticipantEntity entity = null;
        if (dto.getId() != null) {
            entity = this.getEntityById(dto.getId());
        }

        if (entity == null) {
            entity = new TestParticipantEntity();
            entity.setDeleted(false);
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
                String msg = msgUtil.getMessage("listing.criteria.badTestParticipant",
                        dto.getGender() + ": " + dto.getOccupation());
                LOGGER.error(msg, ex);
                throw new EntityCreationException(msg);
            }
        }
        return new TestParticipantDTO(entity);
    }

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

        update(entity);
        return new TestParticipantDTO(entity);
    }

    public void delete(Long id) throws EntityRetrievalException {
        TestParticipantEntity toDelete = getEntityById(id);
        if (toDelete != null) {
            toDelete.setDeleted(true);
            update(toDelete);
        }
    }

    public TestParticipantDTO getById(Long id) {
        TestParticipantDTO dto = null;
        TestParticipantEntity entity = getEntityById(id);

        if (entity != null) {
            dto = new TestParticipantDTO(entity);
        }
        return dto;
    }

    public List<TestParticipantDTO> findAll() {
        List<TestParticipantEntity> entities = getAllEntities();
        List<TestParticipantDTO> dtos = new ArrayList<TestParticipantDTO>();

        for (TestParticipantEntity entity : entities) {
            TestParticipantDTO dto = new TestParticipantDTO(entity);
            dtos.add(dto);
        }
        return dtos;

    }

    private List<TestParticipantEntity> getAllEntities() {
        return entityManager.createQuery("SELECT tpe from TestParticipantEntity tpe "
                        + "LEFT OUTER JOIN FETCH tpe.ageRange "
                        + "LEFT OUTER JOIN FETCH tpe.education "
                        + "where (NOT tpe.deleted = true) ",
                TestParticipantEntity.class).getResultList();
    }

    private TestParticipantEntity getEntityById(Long id) {
        TestParticipantEntity entity = null;

        Query query = entityManager.createQuery("SELECT tpe from TestParticipantEntity tpe "
                + "LEFT OUTER JOIN FETCH tpe.ageRange "
                + "LEFT OUTER JOIN FETCH tpe.education "
                + "where (NOT tpe.deleted = true) "
                + "AND (tpe.id = :entityid)", TestParticipantEntity.class);
        query.setParameter("entityid", id);
        List<TestParticipantEntity> result = query.getResultList();

        if (result.size() > 0) {
            entity = result.get(0);
        }

        return entity;
    }
}

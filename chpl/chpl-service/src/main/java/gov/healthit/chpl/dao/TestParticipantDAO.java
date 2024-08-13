package gov.healthit.chpl.dao;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.TestParticipant;
import gov.healthit.chpl.entity.TestParticipantEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.ErrorMessageUtil;
import jakarta.persistence.Query;
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
            entity.setFriendlyId(participant.getFriendlyId());
            entity.setAgeRangeId(participant.getAge() == null || participant.getAge().getId() == null ? participant.getAgeRangeId() : participant.getAge().getId());
            entity.setAssistiveTechnologyNeeds(participant.getAssistiveTechnologyNeeds());
            entity.setComputerExperienceMonths(participant.getComputerExperienceMonths());
            entity.setEducationTypeId(participant.getEducationType() == null || participant.getEducationType().getId() == null ? participant.getEducationTypeId() : participant.getEducationType().getId());
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

    public void update(TestParticipant participant) throws EntityRetrievalException {
        TestParticipantEntity entity = this.getEntityById(participant.getId());

        if (entity == null) {
            throw new EntityRetrievalException("Entity with id " + participant.getId() + " does not exist");
        }

        entity.setFriendlyId(participant.getFriendlyId());
        entity.setAgeRangeId(participant.getAge() != null ? participant.getAge().getId() : participant.getAgeRangeId());
        entity.setAssistiveTechnologyNeeds(participant.getAssistiveTechnologyNeeds());
        entity.setComputerExperienceMonths(participant.getComputerExperienceMonths());
        entity.setEducationTypeId(participant.getEducationType() != null ? participant.getEducationType().getId() : participant.getEducationTypeId());
        entity.setGender(participant.getGender());
        entity.setOccupation(participant.getOccupation());
        entity.setProductExperienceMonths(participant.getProductExperienceMonths());
        entity.setProfessionalExperienceMonths(participant.getProfessionalExperienceMonths());
        update(entity);
    }

    public void delete(Long id) throws EntityRetrievalException {
        TestParticipantEntity toDelete = getEntityById(id);
        if (toDelete != null) {
            toDelete.setDeleted(true);
            update(toDelete);
        }
    }

    public TestParticipant getById(Long id) {
        TestParticipantEntity entity = getEntityById(id);
        if (entity != null) {
            return entity.toDomain();
        }
        return null;
    }

    public List<TestParticipant> findAll() {
        List<TestParticipantEntity> entities = getAllEntities();
        return entities.stream()
                .map(entity -> entity.toDomain())
                .collect(Collectors.toList());
    }

    private List<TestParticipantEntity> getAllEntities() {
        return entityManager.createQuery("SELECT tpe from TestParticipantEntity tpe "
                        + "LEFT OUTER JOIN FETCH tpe.ageRange "
                        + "LEFT OUTER JOIN FETCH tpe.education "
                        + "WHERE (NOT tpe.deleted = true) ",
                TestParticipantEntity.class).getResultList();
    }

    private TestParticipantEntity getEntityById(Long id) {
        TestParticipantEntity entity = null;
        Query query = entityManager.createQuery("SELECT tpe from TestParticipantEntity tpe "
                + "LEFT OUTER JOIN FETCH tpe.ageRange "
                + "LEFT OUTER JOIN FETCH tpe.education "
                + "WHERE (NOT tpe.deleted = true) "
                + "AND (tpe.id = :entityid)", TestParticipantEntity.class);
        query.setParameter("entityid", id);
        List<TestParticipantEntity> result = query.getResultList();

        if (result.size() > 0) {
            entity = result.get(0);
        }

        return entity;
    }
}

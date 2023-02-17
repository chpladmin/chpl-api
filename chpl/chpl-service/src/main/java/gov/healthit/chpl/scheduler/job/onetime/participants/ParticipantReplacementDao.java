package gov.healthit.chpl.scheduler.job.onetime.participants;

import static gov.healthit.chpl.util.LambdaExceptionUtil.rethrowConsumer;

import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Query;
import javax.transaction.Transactional;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.TestParticipant;
import gov.healthit.chpl.domain.TestTask;
import gov.healthit.chpl.entity.TestParticipantEntity;
import gov.healthit.chpl.entity.listing.TestTaskParticipantMapEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.util.AuthUtil;
import lombok.extern.log4j.Log4j2;

@Component("participantReplacementDao")
@Log4j2(topic = "updateParticipantsJobLogger")
public class ParticipantReplacementDao extends BaseDAOImpl {

    @Transactional(rollbackOn = Exception.class)
    public void replaceParticipants(CertifiedProductSearchDetails listing) throws EntityCreationException {
        //make sure no entities are saved in memory
        entityManager.clear();
        entityManager.flush();

        //Delete existing participant mappings for all tasks
        List<Long> testTaskIds = listing.getSed().getTestTasks().stream().map(testTask -> testTask.getId()).toList();
        int numParticipantMappingsDeleted = deleteExistingParticipantMappings(testTaskIds);
        LOGGER.info("Deleted " + numParticipantMappingsDeleted + " participants for test tasks "
                + testTaskIds.stream().map(id -> id.toString()).collect(Collectors.joining(", ")));

        //Create the new task/participant mappings
        listing.getSed().getTestTasks().stream()
            .forEach(rethrowConsumer(testTask -> updateTaskParticipants(testTask, listing.getSed().getTestTasks())));

        //Deleting a bunch of task-participant mappings probably left some participants
        //that are no longer being used in any tasks.
        //Find those and delete them.
        int numParticipantsDeleted = cleanupParticipants();
        LOGGER.info("Deleted " + numParticipantsDeleted + " participants no longer mapped to any test tasks.");
    }

    private int deleteExistingParticipantMappings(List<Long> testTaskIds) {
        Query query = entityManager.createQuery("UPDATE TestTaskParticipantMapEntity mapping "
                + "SET mapping.deleted = true, "
                + "mapping.lastModifiedUser = :systemUserId "
                + "WHERE mapping.testTaskId IN (:testTaskIds)");
        query.setParameter("systemUserId", User.SYSTEM_USER_ID);
        query.setParameter("testTaskIds", testTaskIds);
        return query.executeUpdate();
    }

    private void updateTaskParticipants(TestTask testTask, List<TestTask> allTestTasks) throws EntityCreationException {
        if (!CollectionUtils.isEmpty(testTask.getTestParticipants())) {
            testTask.getTestParticipants().stream()
                .forEach(rethrowConsumer(participant -> addTestParticipantMapping(testTask, participant, allTestTasks)));
        }
        LOGGER.info("Saved test participants for task " + testTask.getId());
    }

    private void addTestParticipantMapping(TestTask testTask, TestParticipant participant, List<TestTask> allTestTasks)
            throws EntityCreationException {
        boolean createMapping = false;
        if (participant.getId() == null || participant.getId() < 0) {
            //Not using the TestParticipantDAO method because I think we want to specify the last modified user ID
            Long participantId = createParticipant(participant);
            participant.setId(participantId);

            allTestTasks.stream()
                .flatMap(currTestTask -> currTestTask.getTestParticipants().stream())
                .filter(currParticipant -> currParticipant.getId() == null || currParticipant.getId() < 0)
                .filter(currParticipant -> currParticipant.getUniqueId().equals(participant.getUniqueId()))
                .forEach(currParticipant -> currParticipant.setId(participant.getId()));

            createMapping = true;
        } else {
            createMapping = !doesTaskParticipantMappingExist(testTask.getId(), participant.getId());
        }

        if (createMapping) {
            TestTaskParticipantMapEntity mapping = new TestTaskParticipantMapEntity();
            mapping.setTestParticipantId(participant.getId());
            mapping.setTestTaskId(testTask.getId());
            mapping.setLastModifiedUser(AuthUtil.getAuditId());
            create(mapping);
        }
    }

    private Long createParticipant(TestParticipant participant) throws EntityCreationException {
        TestParticipantEntity entity = new TestParticipantEntity();
        try {
            entity.setLastModifiedUser(User.SYSTEM_USER_ID);
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
            LOGGER.error("Error creating test participant: " + participant.toString(), ex);
            throw new EntityCreationException("Error creating test participant " + participant.toString());
        }
        return entity.getId();
    }

    private boolean doesTaskParticipantMappingExist(Long testTaskId, Long participantId) {
        Query query = entityManager.createQuery(
                "SELECT participantMap "
                        + "FROM TestTaskParticipantMapEntity participantMap "
                        + "WHERE participantMap.deleted <> true "
                        + "AND participantMap.testTaskId = :testTaskId "
                        + "AND participantMap.testParticipantId = :testParticipantId",
                TestTaskParticipantMapEntity.class);
        query.setParameter("testTaskId", testTaskId);
        query.setParameter("testParticipantId", participantId);
        List<TestTaskParticipantMapEntity> existingMappings = query.getResultList();
        return !CollectionUtils.isEmpty(existingMappings);
    }

    private int cleanupParticipants() {
        Query query = entityManager.createNativeQuery("UPDATE openchpl.test_participant tp "
                + "SET deleted = true, "
                + "last_modified_user = :systemUserId "
                + "WHERE tp.deleted = false "
                + "AND NOT EXISTS ( "
                    + "SELECT 1 "
                    + "FROM openchpl.test_task_participant_map ttpm "
                    + "WHERE tp.test_participant_id = ttpm.test_participant_id "
                    + "AND ttpm.deleted = false "
                + ")");
        query.setParameter("systemUserId", User.SYSTEM_USER_ID);
        return query.executeUpdate();
    }
}

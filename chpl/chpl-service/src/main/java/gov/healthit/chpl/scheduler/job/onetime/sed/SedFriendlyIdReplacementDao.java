package gov.healthit.chpl.scheduler.job.onetime.sed;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.TestParticipant;
import gov.healthit.chpl.domain.TestTask;
import gov.healthit.chpl.entity.TestParticipantEntity;
import gov.healthit.chpl.entity.TestTaskEntity;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2(topic = "updatedSedFriendlyIdsJobLogger")
public class SedFriendlyIdReplacementDao extends BaseDAOImpl {

    @Transactional(rollbackOn = Exception.class)
    public void updateSedFriendlyIds(CertifiedProductSearchDetails listing) {
        //make sure no entities are saved in memory
        entityManager.clear();
        entityManager.flush();

        listing.getSed().getTestTasks().stream()
            .forEach(testTask -> updateTestTaskFriendlyId(testTask));

        Set<TestParticipant> uniqueTestParticipants = listing.getSed().getTestTasks().stream()
            .flatMap(tt -> tt.getTestParticipants().stream())
            .collect(Collectors.toSet());

        uniqueTestParticipants.stream()
            .forEach(participant -> updateTestParticipantFriendlyId(participant));
    }

    private void updateTestTaskFriendlyId(TestTask testTask) {
        Query query = entityManager.createQuery("SELECT tt "
                + "FROM TestTaskEntity tt "
                + "LEFT OUTER JOIN FETCH tt.testParticipants participantMappings "
                + "LEFT OUTER JOIN FETCH participantMappings.testParticipant participant "
                + "LEFT JOIN FETCH participant.education "
                + "LEFT JOIN FETCH participant.ageRange "
                + "WHERE (NOT tt.deleted = true) "
                + "AND (tt.id = :entityid) ", TestTaskEntity.class);
        query.setParameter("entityid", testTask.getId());
        List<TestTaskEntity> result = query.getResultList();

        TestTaskEntity entity = null;
        if (result.size() > 0) {
            entity = result.get(0);
        } else {
            LOGGER.error("Error querying for test task with ID " + testTask.getId() + ". More than one record was found. Friendly ID will not be updated.");
        }

        if (entity == null) {
            LOGGER.error("No test task with ID " + testTask.getId() + " was found. Friendly ID will not be updated.");
        } else {
            entity.setFriendlyId(testTask.getFriendlyId());
            update(entity);
        }
    }

    private void updateTestParticipantFriendlyId(TestParticipant participant) {
        Query query = entityManager.createQuery("SELECT tpe from TestParticipantEntity tpe "
                + "LEFT OUTER JOIN FETCH tpe.ageRange "
                + "LEFT OUTER JOIN FETCH tpe.education "
                + "WHERE (NOT tpe.deleted = true) "
                + "AND (tpe.id = :entityid)", TestParticipantEntity.class);
        query.setParameter("entityid", participant.getId());
        List<TestParticipantEntity> result = query.getResultList();

        TestParticipantEntity entity = null;
        if (result.size() > 0) {
            entity = result.get(0);
        } else {
            LOGGER.error("Error querying for test participant with ID " + participant.getId() + ". More than one record was found. Friendly ID will not be updated.");
        }

        if (entity == null) {
            LOGGER.error("No test participant with ID " + participant.getId() + " was found. Friendly ID will not be updated.");
        } else {
            entity.setFriendlyId(participant.getFriendlyId());
            update(entity);
        }
    }
}
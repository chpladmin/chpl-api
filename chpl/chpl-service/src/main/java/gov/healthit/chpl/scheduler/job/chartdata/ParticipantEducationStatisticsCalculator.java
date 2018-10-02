package gov.healthit.chpl.scheduler.job.chartdata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.dao.ParticipantEducationStatisticsDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.TestParticipant;
import gov.healthit.chpl.domain.TestTask;
import gov.healthit.chpl.dto.ParticipantEducationStatisticsDTO;
import gov.healthit.chpl.entity.ParticipantEducationStatisticsEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;

/**
 * Populates the participant_education_statistics table with summarized count
 * information.
 * 
 * @author TYoung
 *
 */
public class ParticipantEducationStatisticsCalculator {
    private static final Logger LOGGER = LogManager.getLogger("chartDataCreatorJobLogger");

    @Autowired
    private ParticipantEducationStatisticsDAO participantEducationStatisticsDAO;

    public ParticipantEducationStatisticsCalculator() {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
    }

    /**
     * This method calculates the participant education counts and saves them to
     * the participant_education_statistics table.
     * 
     * @param certifiedProductSearchDetails
     *            List of CertifiedProductSearchDetails objects
     */
    public void run(final List<CertifiedProductSearchDetails> certifiedProductSearchDetails) {

        Map<Long, Long> educationCounts = getCounts(certifiedProductSearchDetails);

        logCounts(educationCounts);

        save(convertEducationCountMapToListOfParticipantEducationStatistics(educationCounts));
    }

    private void logCounts(final Map<Long, Long> educationCounts) {
        for (Entry<Long, Long> entry : educationCounts.entrySet()) {
            LOGGER.info("Education Count: [" + entry.getKey() + " : " + entry.getValue() + "]");
        }
    }

    private Map<Long, Long> getCounts(final List<CertifiedProductSearchDetails> certifiedProductSearchDetails) {
        // The key = testParticpantAgeId
        // The value = count of participants that fall into the associated
        // testParticipantAgeId
        Map<Long, Long> educationMap = new HashMap<Long, Long>();
        List<TestParticipant> uniqueParticipants = getUniqueParticipants(certifiedProductSearchDetails);
        for (TestParticipant participant : uniqueParticipants) {
            Long updatedCount = 1L;
            if (educationMap.containsKey(participant.getEducationTypeId())) {
                updatedCount = educationMap.get(participant.getEducationTypeId());
                updatedCount++;
            }
            educationMap.put(participant.getEducationTypeId(), updatedCount);
        }
        return educationMap;
    }

    private List<TestParticipant> getUniqueParticipants(
            final List<CertifiedProductSearchDetails> certifiedProductSearchDetails) {
        Map<Long, TestParticipant> participants = new HashMap<Long, TestParticipant>();
        for (CertifiedProductSearchDetails detail : certifiedProductSearchDetails) {
            for (TestTask task : detail.getSed().getTestTasks()) {
                for (TestParticipant participant : task.getTestParticipants()) {
                    if (!participants.containsKey(participant.getId())) {
                        participants.put(participant.getId(), participant);
                    }
                }
            }
        }
        return new ArrayList<TestParticipant>(participants.values());
    }

    private void save(final List<ParticipantEducationStatisticsEntity> entities) {
        try {
            deleteExistingPartcipantEducationStatistics();
        } catch (EntityRetrievalException e) {
            LOGGER.error("Error occured while deleting existing ParticipantEducationStatistics.", e);
            return;
        }

        for (ParticipantEducationStatisticsEntity entity : entities) {
            saveParticipantEducationStatistic(entity);
        }
    }

    private List<ParticipantEducationStatisticsEntity> convertEducationCountMapToListOfParticipantEducationStatistics(
            final Map<Long, Long> educationCounts) {
        List<ParticipantEducationStatisticsEntity> entities = new ArrayList<ParticipantEducationStatisticsEntity>();
        for (Entry<Long, Long> entry : educationCounts.entrySet()) {
            ParticipantEducationStatisticsEntity entity = new ParticipantEducationStatisticsEntity();
            entity.setEducationCount(entry.getValue());
            entity.setEducationTypeId(entry.getKey());
            entities.add(entity);
        }
        return entities;
    }

    private void saveParticipantEducationStatistic(final ParticipantEducationStatisticsEntity entity) {
        try {
            ParticipantEducationStatisticsDTO dto = new ParticipantEducationStatisticsDTO(entity);
            participantEducationStatisticsDAO.create(dto);
            LOGGER.info("Saved ParticipantEducationStatisticsDTO [Education Type Id: " + dto.getEducationTypeId()
                    + ", Count:" + dto.getEducationCount() + "]");
        } catch (EntityCreationException | EntityRetrievalException e) {
            LOGGER.error("Error occured while inserting counts.", e);
            return;
        }
    }

    private void deleteExistingPartcipantEducationStatistics() throws EntityRetrievalException {
        List<ParticipantEducationStatisticsDTO> dtos = participantEducationStatisticsDAO.findAll();
        for (ParticipantEducationStatisticsDTO dto : dtos) {
            participantEducationStatisticsDAO.delete(dto.getId());
            LOGGER.info("Deleted: " + dto.getId());
        }
    }
}

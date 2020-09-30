package gov.healthit.chpl.scheduler.job.chartdata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.dao.ParticipantAgeStatisticsDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.TestParticipant;
import gov.healthit.chpl.domain.TestTask;
import gov.healthit.chpl.dto.ParticipantAgeStatisticsDTO;
import gov.healthit.chpl.entity.ParticipantAgeStatisticsEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;

/**
 * Populates the participant_age_statistics table with summarized count
 * information.
 *
 * @author TYoung
 *
 */
public class ParticipantAgeStatisticsCalculator {
    private static final Logger LOGGER = LogManager.getLogger("chartDataCreatorJobLogger");

    @Autowired
    private ParticipantAgeStatisticsDAO participantAgeStatisticsDAO;

    @Autowired
    private JpaTransactionManager txManager;

    public ParticipantAgeStatisticsCalculator() {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
    }

    /**
     * This method calculates the participant counts and saves them to the
     * participant_age_statistics table.
     *
     * @param certifiedProductSearchDetails
     *            List of CertifiedProductSearchDetails objects
     */
    public void run(final List<CertifiedProductSearchDetails> certifiedProductSearchDetails) {

        Map<Long, Long> ageCounts = getCounts(certifiedProductSearchDetails);

        logCounts(ageCounts);

        save(convertAgeCountMapToListOfParticipantAgeStatistics(ageCounts));
    }

    private void logCounts(final Map<Long, Long> ageCounts) {
        for (Entry<Long, Long> entry : ageCounts.entrySet()) {
            LOGGER.info("Age Count: [" + entry.getKey() + " : " + entry.getValue() + "]");
        }
    }

    private Map<Long, Long> getCounts(final List<CertifiedProductSearchDetails> certifiedProductSearchDetails) {
        // The key = testParticpantAgeId
        // The value = count of participants that fall into the associated
        // testParticipantAgeId
        Map<Long, Long> ageMap = new HashMap<Long, Long>();

        List<TestParticipant> uniqueParticipants = getUniqueParticipants(certifiedProductSearchDetails);

        for (TestParticipant participant : uniqueParticipants) {
            Long updatedCount = 1L;
            if (ageMap.containsKey(participant.getAgeRangeId())) {
                updatedCount = ageMap.get(participant.getAgeRangeId());
                updatedCount++;
            }
            ageMap.put(participant.getAgeRangeId(), updatedCount);
        }
        return ageMap;
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

    private void save(final List<ParticipantAgeStatisticsEntity> entities) {
        // We need to manually create a transaction in this case because of how AOP works. When a method is
        // annotated with @Transactional, the transaction wrapper is only added if the object's proxy is called.
        // The object's proxy is not called when the method is called from within this class. The object's proxy
        // is called when the method is public and is called from a different object.
        // https://stackoverflow.com/questions/3037006/starting-new-transaction-in-spring-bean
        TransactionTemplate txTemplate = new TransactionTemplate(txManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        txTemplate.execute(new TransactionCallbackWithoutResult() {

            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                try {
                    deleteExistingPartcipantAgeStatistics();

                    for (ParticipantAgeStatisticsEntity entity : entities) {
                        saveParticipantAgeStatistic(entity);
                    }
                } catch (Exception e) {
                    LOGGER.error("Error saving ParticipantAgeStatistics.", e);
                    status.setRollbackOnly();
                }
            }
        });
    }

    private List<ParticipantAgeStatisticsEntity> convertAgeCountMapToListOfParticipantAgeStatistics(
            final Map<Long, Long> ageCounts) {
        List<ParticipantAgeStatisticsEntity> entities = new ArrayList<ParticipantAgeStatisticsEntity>();
        for (Entry<Long, Long> entry : ageCounts.entrySet()) {
            ParticipantAgeStatisticsEntity entity = new ParticipantAgeStatisticsEntity();
            entity.setAgeCount(entry.getValue());
            entity.setTestParticipantAgeId(entry.getKey());
            entities.add(entity);
        }
        return entities;
    }

    private void saveParticipantAgeStatistic(final ParticipantAgeStatisticsEntity entity) {
        try {
            ParticipantAgeStatisticsDTO dto = new ParticipantAgeStatisticsDTO(entity);
            participantAgeStatisticsDAO.create(dto);
            LOGGER.info("Saved ParticipantAgeStatisticsDTO [Age Id: " + dto.getTestParticipantAgeId() + ", Count:"
                    + dto.getAgeCount() + "]");
        } catch (EntityCreationException | EntityRetrievalException e) {
            LOGGER.error("Error occured while inserting counts.", e);
            return;
        }
    }

    private void deleteExistingPartcipantAgeStatistics() throws EntityRetrievalException {
        List<ParticipantAgeStatisticsDTO> dtos = participantAgeStatisticsDAO.findAll();
        for (ParticipantAgeStatisticsDTO dto : dtos) {
            participantAgeStatisticsDAO.delete(dto.getId());
            LOGGER.info("Deleted: " + dto.getId());
        }
    }
}

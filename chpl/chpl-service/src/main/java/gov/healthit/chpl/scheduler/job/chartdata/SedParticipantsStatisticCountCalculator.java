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

import gov.healthit.chpl.dao.SedParticipantStatisticsCountDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.TestParticipant;
import gov.healthit.chpl.domain.TestTask;
import gov.healthit.chpl.dto.SedParticipantStatisticsCountDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;

/**
 * Populates the sed_participant_statistics_count table with summarized count
 * information.
 *
 * @author TYoung
 *
 */
public class SedParticipantsStatisticCountCalculator {
    private static final Logger LOGGER = LogManager.getLogger("chartDataCreatorJobLogger");

    @Autowired
    private SedParticipantStatisticsCountDAO sedParticipantStatisticsCountDAO;

    @Autowired
    private JpaTransactionManager txManager;

    public SedParticipantsStatisticCountCalculator() {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
    }

    /**
     * This method calculates the participant counts and saves them to the
     * sed_participant_statisitics_count table.
     *
     * @param certifiedProductSearchDetails
     *            List of CertifiedProductSearchDetails objects
     */
    public void run(final List<CertifiedProductSearchDetails> certifiedProductSearchDetails) {

        Map<Long, Long> counts = getCounts(certifiedProductSearchDetails);

        logCounts(counts);

        save(counts);
    }

    private void save(final Map<Long, Long> counts) {
        // We need to manually create a transaction in this case because of how AOP works. When a method is
        // annotated with @Transactional, the transaction wrapper is only added if the object's proxy is called.
        // The object's proxy is not called when the method is called from thin this class. The object's proxy
        // is called when the method is public and is called from a different object.
        // https://stackoverflow.com/questions/3037006/starting-new-transaction-in-spring-bean
        TransactionTemplate txTemplate = new TransactionTemplate(txManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        txTemplate.execute(new TransactionCallbackWithoutResult() {

            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                try {
                    deleteExistingSedParticipantCounts();

                    for (Entry<Long, Long> entry : counts.entrySet()) {
                        SedParticipantStatisticsCountDTO dto = new SedParticipantStatisticsCountDTO();
                        dto.setSedCount(entry.getValue());
                        dto.setParticipantCount(entry.getKey());
                        sedParticipantStatisticsCountDAO.create(dto);
                        LOGGER.info("Saved [" + dto.getSedCount() + ":" + dto.getParticipantCount() + "]");
                    }
                } catch (Exception e) {
                    LOGGER.error("Error saving SedParticipantsStatisticsCounts.", e);
                    status.setRollbackOnly();
                }

            }
        });
    }

    private void deleteExistingSedParticipantCounts() throws EntityRetrievalException {
        List<SedParticipantStatisticsCountDTO> dtos = sedParticipantStatisticsCountDAO.findAll();
        for (SedParticipantStatisticsCountDTO dto : dtos) {
            sedParticipantStatisticsCountDAO.delete(dto.getId());
            LOGGER.info("Deleted: " + dto.getId());
        }
    }

    private void logCounts(final Map<Long, Long> counts) {
        for (Entry<Long, Long> entry : counts.entrySet()) {
            LOGGER.info("Participant Count: " + entry.getKey() + "  SED Count: " + entry.getValue());
        }
    }

    private Map<Long, Long> getCounts(final List<CertifiedProductSearchDetails> certifiedProducts) {
        // In this map, the key is the PARTICPANT COUNT and the value is SED
        // COUNT
        Map<Long, Long> counts = new HashMap<Long, Long>();

        for (CertifiedProductSearchDetails product : certifiedProducts) {
            Long participantCount = getParticipantCount(product).longValue();
            Long updatedSedCount;
            // Is this count in the map?
            if (counts.containsKey(participantCount)) {
                updatedSedCount = counts.get(participantCount) + 1L;
            } else {
                updatedSedCount = 1L;
            }
            counts.put(participantCount, updatedSedCount);
        }
        return counts;
    }

    private Integer getParticipantCount(final CertifiedProductSearchDetails product) {
        List<Long> uniqueParticipants = new ArrayList<Long>();

        for (TestTask testTask : product.getSed().getTestTasks()) {
            for (TestParticipant participant : testTask.getTestParticipants()) {
                if (!uniqueParticipants.contains(participant.getId())) {
                    uniqueParticipants.add(participant.getId());
                }
            }
        }

        return uniqueParticipants.size();
    }
}

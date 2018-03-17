package gov.healthit.chpl.app.chartdata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.SedParticipantStatisticsCountDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.TestParticipant;
import gov.healthit.chpl.domain.TestTask;
import gov.healthit.chpl.dto.SedParticipantStatisticsCountDTO;


/**
 * Populates the sed_participant_statistics_count table with summarized count information.
 * @author TYoung
 *
 */
public class SedParticipantsStatisticCountCalculator {
    private static final Logger LOGGER = LogManager.getLogger(SedParticipantsStatisticCountCalculator.class);
    private ChartDataApplicationEnvironment appEnvironment;
    private SedParticipantStatisticsCountDAO sedParticipantStatisticsCountDAO;
    private JpaTransactionManager txnManager;
    private TransactionTemplate txnTemplate;

    /**
     * This method calculates the participant counts and saves them to the sed_participant_statisitics_count table.
     * @param certifiedProductSearchDetails List of CertifiedProductSearchDetails objects
     * @param appEnvironment the ChartDataApplicationEnvironment (provides access to Spring managed beans)
     */
    public void run(final List<CertifiedProductSearchDetails> certifiedProductSearchDetails,
            final ChartDataApplicationEnvironment appEnvironment) {
        this.appEnvironment = appEnvironment;
        initialize();

        Map<Long, Long> counts = getCounts(certifiedProductSearchDetails);

        logCounts(counts);

        save(counts);
    }

    private void initialize() {
        sedParticipantStatisticsCountDAO = (SedParticipantStatisticsCountDAO) appEnvironment
                .getSpringManagedObject("sedParticipantStatisticsCountDAO");
        txnManager = (JpaTransactionManager) appEnvironment.getSpringManagedObject("transactionManager");
        txnTemplate = new TransactionTemplate(txnManager);
    }

    private void save(final Map<Long, Long> counts) {
        txnTemplate.execute(new TransactionCallbackWithoutResult() {

            @Override
            protected void doInTransactionWithoutResult(final TransactionStatus arg0) {
                saveSedParticipantCounts(counts);
            }
        });
    }

    private void saveSedParticipantCounts(final Map<Long, Long> counts) {

        try {
            deleteExistingSedParticipantCounts();
        } catch (EntityRetrievalException e) {
            LOGGER.error("Error occured while deleting existing counts.", e);
            return;
        }

        try {
            for (Entry<Long, Long> entry : counts.entrySet()) {
                SedParticipantStatisticsCountDTO dto = new SedParticipantStatisticsCountDTO();
                dto.setSedCount(entry.getValue());
                dto.setParticipantCount(entry.getKey());
                sedParticipantStatisticsCountDAO.create(dto);
                LOGGER.info("Saved [" + dto.getSedCount() + ":" + dto.getParticipantCount() + "]");
            }
        } catch (EntityCreationException | EntityRetrievalException e) {
            LOGGER.error("Error occured while inserting counts.", e);
            return;
        }
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

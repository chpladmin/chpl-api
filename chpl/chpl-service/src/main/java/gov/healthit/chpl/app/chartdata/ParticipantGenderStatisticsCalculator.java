package gov.healthit.chpl.app.chartdata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.ParticipantGenderStatisticsDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.TestParticipant;
import gov.healthit.chpl.domain.TestTask;
import gov.healthit.chpl.dto.ParticipantGenderStatisticsDTO;
import gov.healthit.chpl.entity.ParticipantGenderStatisticsEntity;

public class ParticipantGenderStatisticsCalculator {
    private static final Logger LOGGER = LogManager.getLogger(ParticipantGenderStatisticsCalculator.class);
    
    private ChartDataApplicationEnvironment appEnvironment;
    private ParticipantGenderStatisticsDAO participantGenderStatisticsDAO;
    private JpaTransactionManager txnManager;
    private TransactionTemplate txnTemplate;

    public void run(final List<CertifiedProductSearchDetails> certifiedProductSearchDetails,  final ChartDataApplicationEnvironment appEnvironment) {
        this.appEnvironment = appEnvironment;
        initialize();

        ParticipantGenderStatisticsEntity entity = getCounts(certifiedProductSearchDetails);

        logCounts(entity);

        save(entity);
    }

    private void logCounts(ParticipantGenderStatisticsEntity entity) {
        LOGGER.info("Total Female Count: " + entity.getFemaleCount());
        LOGGER.info("Total Male Count: " + entity.getMaleCount());
    }
    private void initialize() {
        participantGenderStatisticsDAO = (ParticipantGenderStatisticsDAO) appEnvironment.getSpringManagedObject("participantGenderStatisticsDAO");
        txnManager = (JpaTransactionManager) appEnvironment.getSpringManagedObject("transactionManager");
        txnTemplate = new TransactionTemplate(txnManager);
    }
    
    private ParticipantGenderStatisticsEntity getCounts(List<CertifiedProductSearchDetails> certifiedProductSearchDetails) {
        ParticipantGenderStatisticsEntity entity = new ParticipantGenderStatisticsEntity();
        entity.setFemaleCount(0L);
        entity.setMaleCount(0L);
        
        List<TestParticipant> uniqueParticipants = getUniqueParticipants(certifiedProductSearchDetails);
        for (TestParticipant participant : uniqueParticipants) {
            if (isParticipantFemale(participant)) {
                entity.setFemaleCount(entity.getFemaleCount() + 1L);
            }
            else if (isParticipantMale(participant)) {
                entity.setMaleCount(entity.getMaleCount() + 1L);
            }
        }
        return entity;
    }
    
    private boolean isParticipantFemale(TestParticipant participant) {
        return participant.getGender().equalsIgnoreCase("F") 
                || participant.getGender().equalsIgnoreCase("Female");
    }
    
    private boolean isParticipantMale(TestParticipant participant) {
        return participant.getGender().equalsIgnoreCase("M") 
                || participant.getGender().equalsIgnoreCase("Male");
    }
    
    private List<TestParticipant> getUniqueParticipants(List<CertifiedProductSearchDetails> certifiedProductSearchDetails) {
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
    
    private void save(final ParticipantGenderStatisticsEntity entity) {
        txnTemplate.execute(new TransactionCallbackWithoutResult() {

            @Override
            protected void doInTransactionWithoutResult(final TransactionStatus arg0) {
                saveSedParticipantGenderStatistics(entity);
            }
        });
    }

    private void saveSedParticipantGenderStatistics(final ParticipantGenderStatisticsEntity entity) {

        try {
            deleteExistingPartcipantStatistics();
        } catch (EntityRetrievalException e) {
            LOGGER.error("Error occured while deleting existing ParticipantGenderStatistics.", e);
            return;
        }

        try {
            ParticipantGenderStatisticsDTO dto = new ParticipantGenderStatisticsDTO(entity);
            participantGenderStatisticsDAO.create(dto);
            LOGGER.info("Saved ParticipantGenderStatisticsDTO [Female: " + dto.getFemaleCount() + ", Male:" + dto.getMaleCount() + "]");
            
        } catch (EntityCreationException | EntityRetrievalException e) {
            LOGGER.error("Error occured while inserting counts.", e);
            return;
        }
    }

    private void deleteExistingPartcipantStatistics() throws EntityRetrievalException {
        List<ParticipantGenderStatisticsDTO> dtos = participantGenderStatisticsDAO.findAll();
        for (ParticipantGenderStatisticsDTO dto : dtos) {
            participantGenderStatisticsDAO.delete(dto.getId());
            LOGGER.info("Deleted: " + dto.getId());
        }
    }

}

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

import gov.healthit.chpl.dao.ParticipantExperienceStatisticsDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.TestParticipant;
import gov.healthit.chpl.domain.TestTask;
import gov.healthit.chpl.dto.ParticipantExperienceStatisticsDTO;
import gov.healthit.chpl.entity.ParticipantExperienceStatisticsEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;

/**
 * Populates the participant_experience_statistics table with summarized count
 * information.
 * 
 * @author TYoung
 *
 */
public class ParticipantExperienceStatisticsCalculator {
    private static final Logger LOGGER = LogManager.getLogger("chartDataCreatorJobLogger");

    @Autowired
    private ParticipantExperienceStatisticsDAO participantExperienceStatisticsDAO;
    private Long experienceTypeId;

    public ParticipantExperienceStatisticsCalculator() {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
    }

    /**
     * This method calculates the participant experience counts and saves them
     * to the participant_experience_statistics table.
     * 
     * @param certifiedProductSearchDetails
     *            List of CertifiedProductSearchDetails objects
     * @param experienceTypeId
     *            1 - Professional Experience, 2 - Product Experience, 3 -
     *            Computer Experience. These values have constants defined in
     *            ExperienceTypes.
     */
    public void run(final List<CertifiedProductSearchDetails> certifiedProductSearchDetails,
            final Long experienceTypeId) {

        this.experienceTypeId = experienceTypeId;

        Map<Integer, Long> experienceCounts = getCounts(certifiedProductSearchDetails);

        logCounts(experienceCounts);

        save(convertExperienceCountMapToListOfParticipantExperienceStatistics(experienceCounts));
    }

    private void logCounts(final Map<Integer, Long> experienceCounts) {
        for (Entry<Integer, Long> entry : experienceCounts.entrySet()) {
            LOGGER.info("Experience Count: [" + entry.getKey() + " : " + entry.getValue() + "]");
        }
    }

    private Map<Integer, Long> getCounts(final List<CertifiedProductSearchDetails> certifiedProductSearchDetails) {
        // The key = Months of experience
        // The value = count of participants that fall into the associated
        // months of experience
        Map<Integer, Long> experienceMap = new HashMap<Integer, Long>();
        List<TestParticipant> uniqueParticipants = getUniqueParticipants(certifiedProductSearchDetails);
        for (TestParticipant participant : uniqueParticipants) {
            Long updatedCount = 1L;
            Integer experienceMonths = getExperienceMonthBasedOnExperienceType(participant);
            if (experienceMap.containsKey(experienceMonths)) {
                updatedCount = experienceMap.get(experienceMonths);
                updatedCount++;
            }
            experienceMap.put(experienceMonths, updatedCount);
        }
        return experienceMap;
    }

    private Integer getExperienceMonthBasedOnExperienceType(final TestParticipant participant) {
        if (experienceTypeId.equals(ExperienceType.COMPUTER_EXPERIENCE)) {
            return participant.getComputerExperienceMonths();
        } else if (experienceTypeId.equals(ExperienceType.PRODUCT_EXPERIENCE)) {
            return participant.getProductExperienceMonths();
        } else if (experienceTypeId.equals(ExperienceType.PROFESSIONAL_EXPERIENCE)) {
            return participant.getProfessionalExperienceMonths();
        } else {
            return 0;
        }
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

    private void save(final List<ParticipantExperienceStatisticsEntity> entities) {
        try {
            deleteExistingPartcipantExperienceStatistics();
        } catch (EntityRetrievalException e) {
            LOGGER.error("Error occured while deleting existing ParticipantExperienceStatistics.", e);
            return;
        }
        for (ParticipantExperienceStatisticsEntity entity : entities) {
            saveParticipantExperienceStatistic(entity);
        }
    }

    private List<ParticipantExperienceStatisticsEntity> convertExperienceCountMapToListOfParticipantExperienceStatistics(
            final Map<Integer, Long> experienceCounts) {
        List<ParticipantExperienceStatisticsEntity> entities = new ArrayList<ParticipantExperienceStatisticsEntity>();
        for (Entry<Integer, Long> entry : experienceCounts.entrySet()) {
            ParticipantExperienceStatisticsEntity entity = new ParticipantExperienceStatisticsEntity();
            entity.setParticipantCount(entry.getValue());
            entity.setExperienceMonths(entry.getKey());
            entity.setExperienceTypeId(experienceTypeId);
            entities.add(entity);
        }
        return entities;
    }

    private void saveParticipantExperienceStatistic(final ParticipantExperienceStatisticsEntity entity) {
        try {
            ParticipantExperienceStatisticsDTO dto = new ParticipantExperienceStatisticsDTO(entity);
            participantExperienceStatisticsDAO.create(dto);
            LOGGER.info("Saved ParticipantEsxperienceStatisticsDTO [Experience Months: " + dto.getExperienceMonths()
                    + ", Count:" + dto.getParticipantCount() + "]");
        } catch (EntityCreationException | EntityRetrievalException e) {
            LOGGER.error("Error occured while inserting counts.", e);
            return;
        }
    }

    private void deleteExistingPartcipantExperienceStatistics() throws EntityRetrievalException {
        List<ParticipantExperienceStatisticsDTO> dtos = participantExperienceStatisticsDAO.findAll(experienceTypeId);
        for (ParticipantExperienceStatisticsDTO dto : dtos) {
            participantExperienceStatisticsDAO.delete(dto.getId());
            LOGGER.info("Deleted: " + dto.getId());
        }
    }

}

package gov.healthit.chpl.scheduler.job.chartdata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.certifiedproduct.CertifiedProductDetailsManager;
import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.dao.ParticipantExperienceStatisticsDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.TestParticipant;
import gov.healthit.chpl.domain.TestTask;
import gov.healthit.chpl.dto.ParticipantExperienceStatisticsDTO;
import gov.healthit.chpl.entity.statistics.ParticipantExperienceStatisticsEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.search.domain.ListingSearchResult;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2(topic = "chartDataCreatorJobLogger")
public class ParticipantExperienceStatisticsCalculator extends SedDataCollector {

    private ParticipantExperienceStatisticsDAO participantExperienceStatisticsDAO;

    private Long experienceTypeId;

    @Autowired
    public ParticipantExperienceStatisticsCalculator(ParticipantExperienceStatisticsDAO participantExperienceStatisticsDAO,
            CertifiedProductDetailsManager certifiedProductDetailsManager, CertificationCriterionDAO criteriaDao) {
        super(certifiedProductDetailsManager, criteriaDao);
        this.participantExperienceStatisticsDAO = participantExperienceStatisticsDAO;
    }

    public void run(List<ListingSearchResult> listingSearchResults, Long experienceTypeId) {
        this.experienceTypeId = experienceTypeId;

        Map<Integer, Long> experienceCounts = getCounts(getSedListings(listingSearchResults));
        logCounts(experienceCounts);
        save(convertExperienceCountMapToListOfParticipantExperienceStatistics(experienceCounts));
    }

    private void logCounts(final Map<Integer, Long> experienceCounts) {
        for (Entry<Integer, Long> entry : experienceCounts.entrySet()) {
            LOGGER.info("Experience Count: [" + entry.getKey() + " : " + entry.getValue() + "]");
        }
    }

    private Map<Integer, Long> getCounts(List<ListingSearchResult> listingSearchResults) {
        // The key = Months of experience
        // The value = count of participants that fall into the associated
        // months of experience
        Map<Integer, Long> experienceMap = new HashMap<Integer, Long>();
        List<TestParticipant> uniqueParticipants = getUniqueParticipants(listingSearchResults);
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

    private List<TestParticipant> getUniqueParticipants(List<ListingSearchResult> listingSearchResults) {
        Map<Long, TestParticipant> participants = new HashMap<Long, TestParticipant>();
        for (ListingSearchResult listingSearchResult : listingSearchResults) {
            CertifiedProductSearchDetails listing = getListingDetails(listingSearchResult.getId());
            if (listing != null) {
                for (TestTask task : listing.getSed().getTestTasks()) {
                    for (TestParticipant participant : task.getTestParticipants()) {
                        if (!participants.containsKey(participant.getId())) {
                            participants.put(participant.getId(), participant);
                        }
                    }
                }
            }
        }
        return new ArrayList<TestParticipant>(participants.values());
    }

    @Transactional
    private void save(final List<ParticipantExperienceStatisticsEntity> entities) {
        try {
            deleteExistingPartcipantExperienceStatistics();

            for (ParticipantExperienceStatisticsEntity entity : entities) {
                saveParticipantExperienceStatistic(entity);
            }
        } catch (Exception e) {
            LOGGER.error("Error saving ParticipantExperienceStatistics.", e);
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

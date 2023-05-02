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
import gov.healthit.chpl.dao.ParticipantAgeStatisticsDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.TestParticipant;
import gov.healthit.chpl.domain.TestTask;
import gov.healthit.chpl.dto.ParticipantAgeStatisticsDTO;
import gov.healthit.chpl.entity.statistics.ParticipantAgeStatisticsEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.search.domain.ListingSearchResult;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2(topic = "chartDataCreatorJobLogger")
public class ParticipantAgeStatisticsCalculator extends SedDataCollector {

    private ParticipantAgeStatisticsDAO participantAgeStatisticsDAO;

    @Autowired
    public ParticipantAgeStatisticsCalculator(ParticipantAgeStatisticsDAO participantAgeStatisticsDAO,
            CertifiedProductDetailsManager certifiedProductDetailsManager, CertificationCriterionDAO criteriaDao) {
        super(certifiedProductDetailsManager, criteriaDao);
        this.participantAgeStatisticsDAO = participantAgeStatisticsDAO;
    }

    public void run(List<ListingSearchResult> listingSearchResults) {
        Map<Long, Long> ageCounts = getCounts(getSedListings(listingSearchResults));
        logCounts(ageCounts);
        save(convertAgeCountMapToListOfParticipantAgeStatistics(ageCounts));
    }

    private void logCounts(Map<Long, Long> ageCounts) {
        for (Entry<Long, Long> entry : ageCounts.entrySet()) {
            LOGGER.info("Age Count: [" + entry.getKey() + " : " + entry.getValue() + "]");
        }
    }

    private Map<Long, Long> getCounts(List<ListingSearchResult> listingSearchResults) {
        // The key = testParticpantAgeId
        // The value = count of participants that fall into the associated
        // testParticipantAgeId
        Map<Long, Long> ageMap = new HashMap<Long, Long>();

        List<TestParticipant> uniqueParticipants = getUniqueParticipants(listingSearchResults);

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
    private void save(List<ParticipantAgeStatisticsEntity> entities) {
        try {
            deleteExistingPartcipantAgeStatistics();

            for (ParticipantAgeStatisticsEntity entity : entities) {
                saveParticipantAgeStatistic(entity);
            }
        } catch (Exception e) {
            LOGGER.error("Error saving ParticipantAgeStatistics.", e);
        }
    }

    private List<ParticipantAgeStatisticsEntity> convertAgeCountMapToListOfParticipantAgeStatistics(Map<Long, Long> ageCounts) {
        List<ParticipantAgeStatisticsEntity> entities = new ArrayList<ParticipantAgeStatisticsEntity>();
        for (Entry<Long, Long> entry : ageCounts.entrySet()) {
            ParticipantAgeStatisticsEntity entity = new ParticipantAgeStatisticsEntity();
            entity.setAgeCount(entry.getValue());
            entity.setTestParticipantAgeId(entry.getKey());
            entities.add(entity);
        }
        return entities;
    }

    private void saveParticipantAgeStatistic(ParticipantAgeStatisticsEntity entity) {
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

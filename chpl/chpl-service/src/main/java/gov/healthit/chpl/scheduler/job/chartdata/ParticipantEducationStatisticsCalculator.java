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
import gov.healthit.chpl.dao.ParticipantEducationStatisticsDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.TestParticipant;
import gov.healthit.chpl.domain.TestTask;
import gov.healthit.chpl.dto.ParticipantEducationStatisticsDTO;
import gov.healthit.chpl.entity.statistics.ParticipantEducationStatisticsEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.search.domain.ListingSearchResult;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2(topic = "chartDataCreatorJobLogger")
public class ParticipantEducationStatisticsCalculator extends SedDataCollector {

    private ParticipantEducationStatisticsDAO participantEducationStatisticsDAO;

    @Autowired
    public ParticipantEducationStatisticsCalculator(ParticipantEducationStatisticsDAO participantEducationStatisticsDAO,
            CertifiedProductDetailsManager certifiedProductDetailsManager, CertificationCriterionDAO criteriaDao) {
        super(certifiedProductDetailsManager, criteriaDao);
        this.participantEducationStatisticsDAO = participantEducationStatisticsDAO;
    }

    public void run(List<ListingSearchResult> listingSearchResults) {
        Map<Long, Long> educationCounts = getCounts(getSedListings(listingSearchResults));
        logCounts(educationCounts);
        save(convertEducationCountMapToListOfParticipantEducationStatistics(educationCounts));
    }

    private void logCounts(final Map<Long, Long> educationCounts) {
        for (Entry<Long, Long> entry : educationCounts.entrySet()) {
            LOGGER.info("Education Count: [" + entry.getKey() + " : " + entry.getValue() + "]");
        }
    }

    private Map<Long, Long> getCounts(List<ListingSearchResult> listingSearchResults) {
        // The key = testParticpantAgeId
        // The value = count of participants that fall into the associated
        // testParticipantAgeId
        Map<Long, Long> educationMap = new HashMap<Long, Long>();
        List<TestParticipant> uniqueParticipants = getUniqueParticipants(listingSearchResults);
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
    private void save(List<ParticipantEducationStatisticsEntity> entities) {
        try {
            deleteExistingPartcipantEducationStatistics();

            for (ParticipantEducationStatisticsEntity entity : entities) {
                saveParticipantEducationStatistic(entity);
            }
        } catch (Exception e) {
            LOGGER.error("Error saving ParticipantEducationStatistics.", e);
        }
    }

    private List<ParticipantEducationStatisticsEntity> convertEducationCountMapToListOfParticipantEducationStatistics(Map<Long, Long> educationCounts) {
        List<ParticipantEducationStatisticsEntity> entities = new ArrayList<ParticipantEducationStatisticsEntity>();
        for (Entry<Long, Long> entry : educationCounts.entrySet()) {
            ParticipantEducationStatisticsEntity entity = new ParticipantEducationStatisticsEntity();
            entity.setEducationCount(entry.getValue());
            entity.setEducationTypeId(entry.getKey());
            entities.add(entity);
        }
        return entities;
    }

    private void saveParticipantEducationStatistic(ParticipantEducationStatisticsEntity entity) {
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

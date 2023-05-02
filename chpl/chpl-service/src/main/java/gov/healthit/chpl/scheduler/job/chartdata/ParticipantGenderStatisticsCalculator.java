package gov.healthit.chpl.scheduler.job.chartdata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.certifiedproduct.CertifiedProductDetailsManager;
import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.dao.ParticipantGenderStatisticsDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.TestParticipant;
import gov.healthit.chpl.domain.TestTask;
import gov.healthit.chpl.dto.ParticipantGenderStatisticsDTO;
import gov.healthit.chpl.entity.statistics.ParticipantGenderStatisticsEntity;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.search.domain.ListingSearchResult;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2(topic = "chartDataCreatorJobLogger")
public class ParticipantGenderStatisticsCalculator extends SedDataCollector {

    private ParticipantGenderStatisticsDAO participantGenderStatisticsDAO;

    @Autowired
    public ParticipantGenderStatisticsCalculator(ParticipantGenderStatisticsDAO participantGenderStatisticsDAO,
            CertifiedProductDetailsManager certifiedProductDetailsManager, CertificationCriterionDAO criteriaDao) {
        super(certifiedProductDetailsManager, criteriaDao);
        this.participantGenderStatisticsDAO = participantGenderStatisticsDAO;
    }

    public void run(List<ListingSearchResult> listingSearchResults) {
        ParticipantGenderStatisticsEntity entity = getCounts(getSedListings(listingSearchResults));
        logCounts(entity);
        save(entity);
    }

    private void logCounts(final ParticipantGenderStatisticsEntity entity) {
        LOGGER.info("Total Female Count: " + entity.getFemaleCount());
        LOGGER.info("Total Male Count: " + entity.getMaleCount());
    }

    private ParticipantGenderStatisticsEntity getCounts(List<ListingSearchResult> listingSearchResults) {
        ParticipantGenderStatisticsEntity entity = new ParticipantGenderStatisticsEntity();
        entity.setFemaleCount(0L);
        entity.setMaleCount(0L);
        entity.setUnknownCount(0L);

        List<TestParticipant> uniqueParticipants = getUniqueParticipants(listingSearchResults);
        for (TestParticipant participant : uniqueParticipants) {
            if (isParticipantFemale(participant)) {
                entity.setFemaleCount(entity.getFemaleCount() + 1L);
            } else if (isParticipantMale(participant)) {
                entity.setMaleCount(entity.getMaleCount() + 1L);
            } else {
                entity.setUnknownCount(entity.getUnknownCount() + 1L);
            }

        }
        return entity;
    }

    private boolean isParticipantFemale(TestParticipant participant) {
        return participant.getGender().equalsIgnoreCase("F") || participant.getGender().equalsIgnoreCase("Female");
    }

    private boolean isParticipantMale(TestParticipant participant) {
        return participant.getGender().equalsIgnoreCase("M") || participant.getGender().equalsIgnoreCase("Male");
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

    private void save(ParticipantGenderStatisticsEntity entity) {
        saveSedParticipantGenderStatistics(entity);
    }

    @Transactional
    private void saveSedParticipantGenderStatistics(ParticipantGenderStatisticsEntity entity) {
        try {
            deleteExistingPartcipantStatistics();

            ParticipantGenderStatisticsDTO dto = new ParticipantGenderStatisticsDTO(entity);
            participantGenderStatisticsDAO.create(dto);
        } catch (Exception e) {
            LOGGER.error("Error saving ParticipantGenderStatistics.", e);
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

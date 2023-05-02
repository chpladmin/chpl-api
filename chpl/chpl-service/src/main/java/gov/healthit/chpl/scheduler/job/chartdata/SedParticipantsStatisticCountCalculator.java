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
import gov.healthit.chpl.dao.SedParticipantStatisticsCountDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.TestParticipant;
import gov.healthit.chpl.domain.TestTask;
import gov.healthit.chpl.dto.SedParticipantStatisticsCountDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.search.domain.ListingSearchResult;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2(topic = "chartDataCreatorJobLogger")
public class SedParticipantsStatisticCountCalculator extends SedDataCollector {

    private SedParticipantStatisticsCountDAO sedParticipantStatisticsCountDAO;

    @Autowired
    public SedParticipantsStatisticCountCalculator(SedParticipantStatisticsCountDAO sedParticipantStatisticsCountDAO,
            CertifiedProductDetailsManager certifiedProductDetailsManager, CertificationCriterionDAO criteriaDao) {
        super(certifiedProductDetailsManager, criteriaDao);
        this.sedParticipantStatisticsCountDAO = sedParticipantStatisticsCountDAO;
    }

    @Transactional
    public void run(List<ListingSearchResult> listingSearchResults) {
        Map<Long, Long> counts = getCounts(getSedListings(listingSearchResults));
        logCounts(counts);
        save(counts);
    }

    @Transactional
    private void save(Map<Long, Long> counts) {
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
            }
    }

    private void deleteExistingSedParticipantCounts() throws EntityRetrievalException {
        List<SedParticipantStatisticsCountDTO> dtos = sedParticipantStatisticsCountDAO.findAll();
        for (SedParticipantStatisticsCountDTO dto : dtos) {
            sedParticipantStatisticsCountDAO.delete(dto.getId());
            LOGGER.info("Deleted: " + dto.getId());
        }
    }

    private void logCounts(Map<Long, Long> counts) {
        for (Entry<Long, Long> entry : counts.entrySet()) {
            LOGGER.info("Participant Count: " + entry.getKey() + "  SED Count: " + entry.getValue());
        }
    }

    private Map<Long, Long> getCounts(List<ListingSearchResult> listingSearchResults) {
        // In this map, the key is the PARTICPANT COUNT and the value is SED
        // COUNT
        Map<Long, Long> counts = new HashMap<Long, Long>();

        for (ListingSearchResult listingSearchResult : listingSearchResults) {
            CertifiedProductSearchDetails listing = getListingDetails(listingSearchResult.getId());
            if (listing != null) {
                Long participantCount = getParticipantCount(listing).longValue();
                Long updatedSedCount;
                // Is this count in the map?
                if (counts.containsKey(participantCount)) {
                    updatedSedCount = counts.get(participantCount) + 1L;
                } else {
                    updatedSedCount = 1L;
                }
                counts.put(participantCount, updatedSedCount);
            }
        }
        return counts;
    }

    private Integer getParticipantCount(CertifiedProductSearchDetails product) {
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

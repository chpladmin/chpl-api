package gov.healthit.chpl.scheduler.job.chartdata;

import java.util.List;

import gov.healthit.chpl.dao.statistics.NonconformityTypeStatisticsDAO;
import gov.healthit.chpl.dao.statistics.SurveillanceStatisticsDAO;
import gov.healthit.chpl.dto.NonconformityTypeStatisticsDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NonconformityTypeChartCalculator {

    private static final Logger LOGGER = LogManager.getLogger(NonconformityTypeChartCalculator.class);

    private SurveillanceStatisticsDAO statisticsDAO;
    private NonconformityTypeStatisticsDAO nonconformityTypeStatisticsDAO;

    public void logCounts(List<NonconformityTypeStatisticsDTO> dtos) {
        for (NonconformityTypeStatisticsDTO dto : dtos) {
            LOGGER.info("Crtieria: " + dto.getNonconformityType() + " Number of NCs: " + dto.getNonconformityCount());
        }
    }

    public List<NonconformityTypeStatisticsDTO> getCounts() {
        List<NonconformityTypeStatisticsDTO> dtos = statisticsDAO.getAllNonconformitiesByCriterion();
        return dtos;
    }

    private void deleteExistingNonconformityStatistics() throws EntityRetrievalException {
        nonconformityTypeStatisticsDAO.deleteAllOldNonConformityStatistics();
    }

    public void saveCounts(List<NonconformityTypeStatisticsDTO> dtos) {
        try {
            deleteExistingNonconformityStatistics();
        } catch (EntityRetrievalException e) {
            LOGGER.error("Error occured while deleting existing ParticipantExperienceStatistics.", e);
            return;
        }
        for (NonconformityTypeStatisticsDTO dto : dtos) {
            nonconformityTypeStatisticsDAO.create(dto);
        }
    }

}

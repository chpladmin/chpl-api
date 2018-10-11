package gov.healthit.chpl.scheduler.job.chartdata;

import java.util.List;

import gov.healthit.chpl.dao.statistics.NonconformityTypeStatisticsDAO;
import gov.healthit.chpl.dao.statistics.SurveillanceStatisticsDAO;
import gov.healthit.chpl.dto.NonconformityTypeStatisticsDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

@Component("nonconformityTypeChartCalculator")
@EnableAsync
public class NonconformityTypeChartCalculator {

    private static final Logger LOGGER = LogManager.getLogger("chartDataCreatorJobLogger");

    @Autowired
    private SurveillanceStatisticsDAO statisticsDAO;
    @Autowired
    private NonconformityTypeStatisticsDAO nonconformityTypeStatisticsDAO;

    public NonconformityTypeChartCalculator() {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
    }

    public void logCounts(List<NonconformityTypeStatisticsDTO> dtos) {
        for (NonconformityTypeStatisticsDTO dto : dtos) {
            LOGGER.info("Crtieria: " + dto.getNonconformityType() + " Number of NCs: " + dto.getNonconformityCount());
        }
    }

    @Transactional
    @Async
    public List<NonconformityTypeStatisticsDTO> getCounts() {
        List<NonconformityTypeStatisticsDTO> dtos = statisticsDAO.getAllNonconformitiesByCriterion();
        return dtos;
    }

    @Transactional
    @Async
    private void deleteExistingNonconformityStatistics() throws EntityRetrievalException {
        nonconformityTypeStatisticsDAO.deleteAllOldNonConformityStatistics();
    }

    @Transactional
    @Async
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

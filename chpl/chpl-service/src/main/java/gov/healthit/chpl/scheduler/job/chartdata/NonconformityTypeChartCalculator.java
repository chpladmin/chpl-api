package gov.healthit.chpl.scheduler.job.chartdata;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.dao.statistics.NonconformityTypeStatisticsDAO;
import gov.healthit.chpl.dao.statistics.SurveillanceStatisticsDAO;
import gov.healthit.chpl.dto.NonconformityTypeStatisticsDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;

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
            if (!StringUtils.isEmpty(dto.getNonconformityType())) {
                LOGGER.info("Criteria: " + dto.getNonconformityType() + " Number of NCs: " + dto.getNonconformityCount());
            } else if (dto.getCriterion() != null) {
                LOGGER.info("Criteria: " + dto.getCriterion().getNumber()
                        + "(" + dto.getCriterion().getTitle() + ") " + " Number of NCs: " + dto.getNonconformityCount());
            }
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

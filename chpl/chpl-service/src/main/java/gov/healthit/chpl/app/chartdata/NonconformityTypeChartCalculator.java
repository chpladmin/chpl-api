package gov.healthit.chpl.app.chartdata;

import java.util.List;

import gov.healthit.chpl.dao.statistics.NonconformityTypeStatisticsDAO;
import gov.healthit.chpl.dao.statistics.SurveillanceStatisticsDAO;
import gov.healthit.chpl.dto.NonconformityTypeStatisticsDTO;
import gov.healthit.chpl.dto.ParticipantEducationStatisticsDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

public class NonconformityTypeChartCalculator {

    private static final Logger LOGGER = LogManager.getLogger(NonconformityTypeChartCalculator.class);

    private SurveillanceStatisticsDAO statisticsDAO;
    private NonconformityTypeStatisticsDAO nonconformityTypeStatisticsDAO;
    private JpaTransactionManager txnManager;
    private TransactionTemplate txnTemplate;

    NonconformityTypeChartCalculator(final ChartDataApplicationEnvironment appEnvironment) {
        statisticsDAO = (SurveillanceStatisticsDAO) appEnvironment.getSpringManagedObject("surveillanceStatisticsDAO");
        nonconformityTypeStatisticsDAO = (NonconformityTypeStatisticsDAO) appEnvironment
                .getSpringManagedObject("nonconformityTypeStatisticsDAO");
        txnManager = (JpaTransactionManager) appEnvironment.getSpringManagedObject("transactionManager");
        txnTemplate = new TransactionTemplate(txnManager);
    }

    NonconformityTypeChartCalculator(final SurveillanceStatisticsDAO statisticsDAO,
            NonconformityTypeStatisticsDAO nonconformityTypeStatisticsDAO, JpaTransactionManager txnManager) {
        this.statisticsDAO = (SurveillanceStatisticsDAO) statisticsDAO;
        this.nonconformityTypeStatisticsDAO = nonconformityTypeStatisticsDAO;
        this.txnManager = txnManager;
        this.txnTemplate = new TransactionTemplate(this.txnManager);
    }

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
        List<NonconformityTypeStatisticsDTO> dtos = nonconformityTypeStatisticsDAO.getAllNonconformityStatistics();
        for (NonconformityTypeStatisticsDTO dto : dtos) {
            nonconformityTypeStatisticsDAO.delete(dto.getId());
            LOGGER.info("Deleted: " + dto.getId());
        }
    }

    public void saveCounts(List<NonconformityTypeStatisticsDTO> dtos) {
        txnTemplate.execute(new TransactionCallbackWithoutResult() {

            @Override
            protected void doInTransactionWithoutResult(final TransactionStatus arg0) {
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
        });
    }

}

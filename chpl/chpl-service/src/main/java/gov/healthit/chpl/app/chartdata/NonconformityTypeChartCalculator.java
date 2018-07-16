package gov.healthit.chpl.app.chartdata;

import java.util.List;

import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.dao.CriterionProductStatisticsDAO;
import gov.healthit.chpl.dao.statistics.NonconformityTypeStatisticsDAO;
import gov.healthit.chpl.dao.statistics.SurveillanceStatisticsDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.NonconformityTypeStatisticsDTO;

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
    	statisticsDAO = (SurveillanceStatisticsDAO) appEnvironment
                .getSpringManagedObject("surveillanceStatisticsDAO");
    	nonconformityTypeStatisticsDAO = (NonconformityTypeStatisticsDAO) appEnvironment
                .getSpringManagedObject("nonconformityTypeStatisticsDAO");
        txnManager = (JpaTransactionManager) appEnvironment.getSpringManagedObject("transactionManager");
        txnTemplate = new TransactionTemplate(txnManager);
    }
    
    public void logCounts(List<NonconformityTypeStatisticsDTO> dtos) {
        for (NonconformityTypeStatisticsDTO dto : dtos) {
            LOGGER.info("Crtieria: " + dto.getNonconformityType() + " Number of NCs: " + dto.getNonconformityCount());
        }
    }
    
    public List<NonconformityTypeStatisticsDTO> getCounts() {
    	List<NonconformityTypeStatisticsDTO> dtos = statisticsDAO.getAllNonconformitiesByCriterion(null);
    	return dtos;
    }
    
   public void saveCounts(List<NonconformityTypeStatisticsDTO> dtos) {
        txnTemplate.execute(new TransactionCallbackWithoutResult() {

            @Override
            protected void doInTransactionWithoutResult(final TransactionStatus arg0) {
            	for(NonconformityTypeStatisticsDTO dto : dtos){
            		nonconformityTypeStatisticsDAO.create(dto);
            	}
            }
        });
    }

}

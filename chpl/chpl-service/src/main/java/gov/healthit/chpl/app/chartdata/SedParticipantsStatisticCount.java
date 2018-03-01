package gov.healthit.chpl.app.chartdata;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.SedParticipantStatisticsCountDAO;
import gov.healthit.chpl.dao.search.CertifiedProductSearchDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.TestParticipant;
import gov.healthit.chpl.domain.TestTask;
import gov.healthit.chpl.domain.search.CertifiedProductFlatSearchResult;
import gov.healthit.chpl.dto.SedParticipantStatisticsCountDTO;
import gov.healthit.chpl.manager.CertifiedProductDetailsManager;


@Repository("SedParticipantsStatisticCount")
public class SedParticipantsStatisticCount {
	private static final Logger LOGGER = LogManager.getLogger(SedParticipantsStatisticCount.class);
	
	private static String EDITION_2015 = "2015";
	
	private ChartDataApplicationEnvironment appEnvironment;
	private CertifiedProductDetailsManager certifiedProductDetailsManager;
	private SedParticipantStatisticsCountDAO sedParticipantStatisticsCountDAO;
	private JpaTransactionManager txnManager;
	private TransactionTemplate txnTemplate;
	
	public void run(ChartDataApplicationEnvironment appEnvironment) {
		this.appEnvironment = appEnvironment;
		initialize();
				
		List<CertifiedProductFlatSearchResult> certifiedProducts = getCertifiedProducts();
		LOGGER.info("Certified Product Count: " + certifiedProducts.size());
		
		certifiedProducts = filterByEdition(certifiedProducts, EDITION_2015);
		LOGGER.info("2015 Certified Product Count: " + certifiedProducts.size());
		
		List<CertifiedProductSearchDetails> certifiedProductsWithDetails = getCertifiedProductDetailsForAll(certifiedProducts);
		
		certifiedProductsWithDetails = filterBySed(certifiedProductsWithDetails);
		LOGGER.info("2015/SED Certified Product Count: " + certifiedProductsWithDetails.size());
		
		Map<Long, Long> counts = getCounts(certifiedProductsWithDetails);
		
		logCounts(counts);
		
		save(counts);
 	}
	
	private void initialize() {
		certifiedProductDetailsManager = (CertifiedProductDetailsManager)appEnvironment.getSpringManagedObject("certifiedProductDetailsManager");
		sedParticipantStatisticsCountDAO = (SedParticipantStatisticsCountDAO)appEnvironment.getSpringManagedObject("sedParticipantStatisticsCountDAO");
		txnManager = (JpaTransactionManager)appEnvironment.getSpringManagedObject("transactionManager");
		txnTemplate = new TransactionTemplate(txnManager);
	}
	
	private void save(Map<Long, Long> counts) {
		txnTemplate.execute(new TransactionCallbackWithoutResult() {
			
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus arg0) {
				saveSedParticipantCounts(counts);
			}
		});
	}
		
	private void saveSedParticipantCounts(Map<Long, Long> counts) {
		
		try {
			deleteExistingSedParticipantCounts();
		} catch (EntityRetrievalException e) {
			LOGGER.error("Error occured while deleting existing counts.", e);
			return;
		}
		
		try {
			for (Entry<Long, Long> entry : counts.entrySet()) {
				SedParticipantStatisticsCountDTO dto = new SedParticipantStatisticsCountDTO();
				dto.setSedCount(entry.getValue());
				dto.setParticipantCount(entry.getKey());
				sedParticipantStatisticsCountDAO.create(dto);
				LOGGER.info("Saved [" +  dto.getSedCount() + ":" + dto.getParticipantCount() + "]");
			}
		} catch (EntityCreationException | EntityRetrievalException e) {
			LOGGER.error("Error occured while inserting counts.", e);
			return;
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
	
	private Map<Long, Long> getCounts(List<CertifiedProductSearchDetails> certifiedProducts) {
		//In this map, the key is the PARTICPANT COUNT and the value is SED COUNT
		Map<Long, Long> counts = new HashMap<Long, Long>();
		
		for (CertifiedProductSearchDetails product : certifiedProducts) {
			Long participantCount = getParticipantCount(product).longValue();
			Long updatedCount;
			//Is this count in the map?
			if (counts.containsKey(participantCount)) {
				updatedCount = counts.get(participantCount) + 1l;
			}	
			else {
				updatedCount = 1L;
			}
			counts.put(participantCount,  updatedCount);
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
	
	private List<CertifiedProductSearchDetails> getCertifiedProductDetailsForAll(List<CertifiedProductFlatSearchResult> certifiedProducts) { 
		
		List<CertifiedProductSearchDetails> details =  new ArrayList<CertifiedProductSearchDetails>();
		List<Future<CertifiedProductSearchDetails>> futures = new ArrayList<Future<CertifiedProductSearchDetails>>();
		SedParticipantsStatisticsCountAsyncHelper sedParticipantsStatisticsCountAsyncHelper = 
				(SedParticipantsStatisticsCountAsyncHelper)appEnvironment.getSpringManagedObject("sedParticipantsStatisticsCountAsyncHelper");
		
		for (CertifiedProductFlatSearchResult certifiedProduct : certifiedProducts) {
			try {
				futures.add(
						sedParticipantsStatisticsCountAsyncHelper.getCertifiedProductDetail(
								certifiedProduct.getId(), certifiedProductDetailsManager));
			} catch (EntityRetrievalException e) {
				LOGGER.error("Could not retrieve certified product details for id: " + certifiedProduct.getId(), e) ;
			}
		}
		
		Date startTime = new Date();
		for (Future<CertifiedProductSearchDetails> future : futures) {
			try {
				details.add(future.get());
			} catch (InterruptedException | ExecutionException e) {
				LOGGER.error("Could not retrieve certified product details for unknown id.", e) ;
			}
		}

		Date endTime = new Date();
		LOGGER.info("Time to retrieve details: " + (endTime.getTime() - startTime.getTime()));
		
		return details;
	}
	
	private List<CertifiedProductFlatSearchResult> getCertifiedProducts() {
		CertifiedProductSearchDAO certifiedProductSearchDAO  = 
				(CertifiedProductSearchDAO) appEnvironment.getSpringManagedObject("certifiedProductSearchDAO");
		
 		List<CertifiedProductFlatSearchResult> results = certifiedProductSearchDAO.getAllCertifiedProducts();
		
		return results;
	}
	
	private List<CertifiedProductFlatSearchResult> filterByEdition(List<CertifiedProductFlatSearchResult> certifiedProducts, String edition) {
		List<CertifiedProductFlatSearchResult> results = new ArrayList<CertifiedProductFlatSearchResult>();
		for (CertifiedProductFlatSearchResult result : certifiedProducts) {
			if (result.getEdition().equals(edition)) {
				results.add(result);
			}
		}
		return results;
	}
	
	private List<CertifiedProductSearchDetails> filterBySed(List<CertifiedProductSearchDetails> certifiedProductDetails) {
		List<CertifiedProductSearchDetails> results = new ArrayList<CertifiedProductSearchDetails>();
		for (CertifiedProductSearchDetails detail : certifiedProductDetails) {
			if (isCertifiedProductAnSed(detail)) {
				results.add(detail);
			}
		}
		return results;
	}
		
	private Boolean isCertifiedProductAnSed(CertifiedProductSearchDetails certifiedProductDetail) {
		return certifiedProductDetail.getSed().getTestTasks().size() > 0;
	}
}

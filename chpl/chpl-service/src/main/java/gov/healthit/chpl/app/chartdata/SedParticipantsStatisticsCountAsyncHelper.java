package gov.healthit.chpl.app.chartdata;

import java.util.concurrent.Future;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.manager.CertifiedProductDetailsManager;

@Service("sedParticipantsStatisticsCountAsyncHelper")
public class SedParticipantsStatisticsCountAsyncHelper {
	private static final Logger LOGGER = LogManager.getLogger(SedParticipantsStatisticsCountAsyncHelper.class);
	
	@Async("chartDataExecutor")
	public Future<CertifiedProductSearchDetails> getCertifiedProductDetail(Long id, CertifiedProductDetailsManager certifiedProductDetailsManager) throws EntityRetrievalException {
		CertifiedProductSearchDetails dto = certifiedProductDetailsManager.getCertifiedProductDetails(id);
		LOGGER.info("Finishing Details for: " + id);
		return new AsyncResult<CertifiedProductSearchDetails>(dto);
	}
}

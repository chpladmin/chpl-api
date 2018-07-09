package gov.healthit.chpl.app.chartdata;

import java.util.concurrent.Future;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.CertifiedProductDetailsManager;

/**
 * Provides asynchronous support for retrieving certified product details.
 * @author TYoung
 *
 */
@Service("dataCollectorAsyncHelper")
public class DataCollectorAsyncHelper {
    private static final Logger LOGGER = LogManager.getLogger(DataCollectorAsyncHelper.class);

    /**
     * Retrieves the associated CertifiedProductionSearchDetails object as a Future<>.
     * @param id id associated to the requested CertifiedProductionSearchDetails object
     * @param certifiedProductDetailsManager provides access to retrieve CertifiedProductionSearchDetails object
     * @return CertifiedProductionSearchDetails object
     * @throws EntityRetrievalException when the CertifiedProductionSearchDetails object could not be retrieved
     */
    @Async("chartDataExecutor")
    public Future<CertifiedProductSearchDetails> getCertifiedProductDetail(
            final Long id, final CertifiedProductDetailsManager certifiedProductDetailsManager)
                throws EntityRetrievalException {
        CertifiedProductSearchDetails dto = certifiedProductDetailsManager.getCertifiedProductDetails(id);
        LOGGER.info("Finishing retrieving Details for: " + id);
        return new AsyncResult<CertifiedProductSearchDetails>(dto);
    }
}

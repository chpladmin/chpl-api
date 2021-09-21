package gov.healthit.chpl.scheduler;

import java.util.concurrent.Future;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import gov.healthit.chpl.certifiedproduct.CertifiedProductDetailsManager;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.exception.EntityRetrievalException;

/**
 * Provides asynchronous support for retrieving certified product details.
 * @author TYoung
 *
 */
@Service("dataCollectorAsyncSchedulerHelper")
public class DataCollectorAsyncSchedulerHelper {
    private Logger logger;

    /**
     * Retrieves the associated CertifiedProductionSearchDetails object as a Future<>.
     * @param id id associated to the requested CertifiedProductionSearchDetails object
     * @param certifiedProductDetailsManager provides access to retrieve CertifiedProductionSearchDetails object
     * @return CertifiedProductionSearchDetails object
     * @throws EntityRetrievalException when the CertifiedProductionSearchDetails object could not be retrieved
     */
    @Async("jobAsyncDataExecutor")
    public Future<CertifiedProductSearchDetails> getCertifiedProductDetail(
            final Long id, final CertifiedProductDetailsManager certifiedProductDetailsManager)
                throws EntityRetrievalException {
        CertifiedProductSearchDetails dto = certifiedProductDetailsManager.getCertifiedProductDetailsUsingCache(id);
        getLogger().info("Finishing retrieving Details for: " + id);
        return new AsyncResult<CertifiedProductSearchDetails>(dto);
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public Logger getLogger() {
        if (logger == null) {
            logger = LogManager.getLogger(SchedulerCertifiedProductSearchDetailsAsync.class);
        }
        return logger;
    }
}

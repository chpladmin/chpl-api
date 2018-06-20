package gov.healthit.chpl.app.resource;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeansException;

import gov.healthit.chpl.app.CertifiedProductSearchDetailsAsync;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.CertifiedProductDownloadResponse;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.manager.CertifiedProductDetailsManager;

public abstract class CertifiedProductDownloadableResourceCreatorApp extends DownloadableResourceCreatorApp {
    private static final Logger LOGGER = LogManager.getLogger(CertifiedProductDownloadableResourceCreatorApp.class);

    protected abstract List<CertifiedProductDetailsDTO> getRelevantListings() throws EntityRetrievalException;

    protected abstract void writeToFile(File downloadFolder, CertifiedProductDownloadResponse results)
            throws IOException;

    @Override
    protected void runJob(final String[] args) throws Exception {
        Date start = new Date();
        try {
            List<CertifiedProductDetailsDTO> listings = getRelevantListings();

            List<Future<CertifiedProductSearchDetails>> futures = getCertifiedProductSearchDetailsFutures(listings);
            Map<Long, CertifiedProductSearchDetails> cpMap = getMapFromFutures(futures);

            CertifiedProductDownloadResponse results = new CertifiedProductDownloadResponse();
            results.setListings(createOrderedListOfCertifiedProducts(
                    cpMap,
                    getOriginalCertifiedProductOrder(listings)));

            File downloadFolder = getDownloadFolder();
            writeToFile(downloadFolder, results);
        } catch (Exception e) {
            LOGGER.error(e);
        }
        Date end = new Date();
        LOGGER.info("Time to create file(s): "
                + (end.getTime() - start.getTime()) / 1000 + " seconds");
    }

    private List<Future<CertifiedProductSearchDetails>> getCertifiedProductSearchDetailsFutures(
            final List<CertifiedProductDetailsDTO> listings) throws Exception {

        List<Future<CertifiedProductSearchDetails>> futures = new ArrayList<Future<CertifiedProductSearchDetails>>();
        CertifiedProductSearchDetailsAsync cpsdAsync = getCertifiedProductDetailsAsyncRetrievalHelper();

        for (CertifiedProductDetailsDTO currListing : listings) {
            try {
                futures.add(cpsdAsync.getCertifiedProductDetail(currListing.getId(),
                        getCertifiedProductDetailsManager()));
            } catch (EntityRetrievalException e) {
                LOGGER.error("Could not retrieve certified product details for id: " + currListing.getId(), e);
            }
        }
        return futures;
    }

    private Map<Long, CertifiedProductSearchDetails> getMapFromFutures(
            final List<Future<CertifiedProductSearchDetails>> futures) {
        Map<Long, CertifiedProductSearchDetails> cpMap = new HashMap<Long, CertifiedProductSearchDetails>();
        for (Future<CertifiedProductSearchDetails> future : futures) {
            try {
                cpMap.put(future.get().getId(), future.get());
            } catch (InterruptedException | ExecutionException e) {
                LOGGER.error("Could not retrieve certified product details for unknown id.", e);
            }
        }
        return cpMap;
    }

    private List<CertifiedProductSearchDetails> createOrderedListOfCertifiedProducts(
            final Map<Long, CertifiedProductSearchDetails> certifiedProducts, final List<Long> orderedIds) {

        List<CertifiedProductSearchDetails> ordered = new ArrayList<CertifiedProductSearchDetails>();

        for (Long id : orderedIds) {
            if (certifiedProducts.containsKey(id)) {
                ordered.add(certifiedProducts.get(id));
            }
        }

        return ordered;
    }

    private List<Long> getOriginalCertifiedProductOrder(final List<CertifiedProductDetailsDTO> listings) {
        List<Long> order = new ArrayList<Long>();

        for (CertifiedProductDetailsDTO cp : listings) {
            order.add(cp.getId());
        }

        return order;
    }

    private CertifiedProductDetailsManager getCertifiedProductDetailsManager() throws BeansException {
        return (CertifiedProductDetailsManager) getApplicationContext().getBean("certifiedProductDetailsManager");
    }

    private CertifiedProductSearchDetailsAsync getCertifiedProductDetailsAsyncRetrievalHelper() throws BeansException {
        return (CertifiedProductSearchDetailsAsync) 
                getApplicationContext().getBean("certifiedProductSearchDetailsAsync");
    }
}

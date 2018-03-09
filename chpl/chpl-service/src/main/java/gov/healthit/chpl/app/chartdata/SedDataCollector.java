package gov.healthit.chpl.app.chartdata;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.search.CertifiedProductSearchDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.search.CertifiedProductFlatSearchResult;
import gov.healthit.chpl.manager.CertifiedProductDetailsManager;

public class SedDataCollector {
    private ChartDataApplicationEnvironment appEnvironment;
    private static final Logger LOGGER = LogManager.getLogger(SedDataCollector.class);
    private static final String EDITION_2015 = "2015";
    
    private CertifiedProductDetailsManager certifiedProductDetailsManager;
    
    public List<CertifiedProductSearchDetails> retreiveData(final ChartDataApplicationEnvironment appEnvironment) {
        this.appEnvironment = appEnvironment;
        initialize();

        List<CertifiedProductFlatSearchResult> certifiedProducts = getCertifiedProducts();
        LOGGER.info("Certified Product Count: " + certifiedProducts.size());

        certifiedProducts = filterByEdition(certifiedProducts, EDITION_2015);
        LOGGER.info("2015 Certified Product Count: " + certifiedProducts.size());

        List<CertifiedProductSearchDetails> certifiedProductsWithDetails = getCertifiedProductDetailsForAll(
                certifiedProducts);

        certifiedProductsWithDetails = filterBySed(certifiedProductsWithDetails);
        LOGGER.info("2015/SED Certified Product Count: " + certifiedProductsWithDetails.size());
        
        return certifiedProductsWithDetails;
    }
    
    private void initialize() {
        certifiedProductDetailsManager = (CertifiedProductDetailsManager) appEnvironment
                .getSpringManagedObject("certifiedProductDetailsManager");
    }
    
    private List<CertifiedProductFlatSearchResult> getCertifiedProducts() {
        CertifiedProductSearchDAO certifiedProductSearchDAO = (CertifiedProductSearchDAO) appEnvironment
                .getSpringManagedObject("certifiedProductSearchDAO");

        List<CertifiedProductFlatSearchResult> results = certifiedProductSearchDAO.getAllCertifiedProducts();

        return results;
    }
    
    private List<CertifiedProductSearchDetails> getCertifiedProductDetailsForAll(
            final List<CertifiedProductFlatSearchResult> certifiedProducts) {

        List<CertifiedProductSearchDetails> details = new ArrayList<CertifiedProductSearchDetails>();
        List<Future<CertifiedProductSearchDetails>> futures = new ArrayList<Future<CertifiedProductSearchDetails>>();
        SedDataCollectorAsyncHelper sedDataCollectorAsyncHelper =
                (SedDataCollectorAsyncHelper) appEnvironment.getSpringManagedObject("sedDataCollectorAsyncHelper");

        for (CertifiedProductFlatSearchResult certifiedProduct : certifiedProducts) {
            try {
                futures.add(sedDataCollectorAsyncHelper
                        .getCertifiedProductDetail(certifiedProduct.getId(), certifiedProductDetailsManager));
            } catch (EntityRetrievalException e) {
                LOGGER.error("Could not retrieve certified product details for id: " + certifiedProduct.getId(), e);
            }
        }

        Date startTime = new Date();
        for (Future<CertifiedProductSearchDetails> future : futures) {
            try {
                details.add(future.get());
            } catch (InterruptedException | ExecutionException e) {
                LOGGER.error("Could not retrieve certified product details for unknown id.", e);
            }
        }

        Date endTime = new Date();
        LOGGER.info("Time to retrieve details: " + (endTime.getTime() - startTime.getTime()));

        return details;
    }

    private List<CertifiedProductFlatSearchResult> filterByEdition(
            final List<CertifiedProductFlatSearchResult> certifiedProducts, final String edition) {
        List<CertifiedProductFlatSearchResult> results = new ArrayList<CertifiedProductFlatSearchResult>();
        for (CertifiedProductFlatSearchResult result : certifiedProducts) {
            if (result.getEdition().equals(edition)) {
                results.add(result);
            }
        }
        return results;
    }
    
    private List<CertifiedProductSearchDetails> filterBySed(
            final List<CertifiedProductSearchDetails> certifiedProductDetails) {
        List<CertifiedProductSearchDetails> results = new ArrayList<CertifiedProductSearchDetails>();
        for (CertifiedProductSearchDetails detail : certifiedProductDetails) {
            if (isCertifiedProductAnSed(detail)) {
                results.add(detail);
            }
        }
        return results;
    }

    private Boolean isCertifiedProductAnSed(final CertifiedProductSearchDetails certifiedProductDetail) {
        return certifiedProductDetail.getSed().getTestTasks().size() > 0;
    }

}

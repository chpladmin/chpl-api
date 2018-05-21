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

/**
 * Retrieves all of the 2014 and 2015 Listings and their details.  Details are retrieved asynchronously according
 * to the chartDataExecutor defined in AppConfig.
 * @author alarned
 *
 */
public class ProductDataCollector {
    private ChartDataApplicationEnvironment appEnvironment;
    private static final Logger LOGGER = LogManager.getLogger(ProductDataCollector.class);
    private static final String EDITION_2014 = "2014";
    private static final String EDITION_2015 = "2015";
    private CertifiedProductDetailsManager certifiedProductDetailsManager;

    ProductDataCollector(final ChartDataApplicationEnvironment appEnvironment) {
        this.appEnvironment = appEnvironment;
        initialize();
    }

    /**
     * This method runs the data retrieval process for the 2014/2015 Criterion - Product count.
     * @return List of CertifiedProductSearchDetails
     */
    public List<CertifiedProductSearchDetails> retreiveData() {
        List<CertifiedProductFlatSearchResult> certifiedProducts = getCertifiedProducts();
        LOGGER.info("Certified Product Count: " + certifiedProducts.size());

        certifiedProducts = filterByEditions(certifiedProducts, EDITION_2014, EDITION_2015);
        LOGGER.info("2014/2015 Certified Product Count: " + certifiedProducts.size());

        List<CertifiedProductSearchDetails> certifiedProductsWithDetails = getCertifiedProductDetailsForAll(
                certifiedProducts);

        return certifiedProductsWithDetails;
    }

    private List<CertifiedProductFlatSearchResult> filterByEditions(
            final List<CertifiedProductFlatSearchResult> certifiedProducts,
            final String edition1, final String edition2) {
        List<CertifiedProductFlatSearchResult> results = new ArrayList<CertifiedProductFlatSearchResult>();
        for (CertifiedProductFlatSearchResult result : certifiedProducts) {
            if (result.getEdition().equals(edition1) || result.getEdition().equals(edition2)) {
                results.add(result);
            }
        }
        return results;
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
        DataCollectorAsyncHelper dataCollectorAsyncHelper =
                (DataCollectorAsyncHelper) appEnvironment.getSpringManagedObject("dataCollectorAsyncHelper");

        for (CertifiedProductFlatSearchResult certifiedProduct : certifiedProducts) {
            try {
                futures.add(dataCollectorAsyncHelper
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

    private void initialize() {
        certifiedProductDetailsManager = (CertifiedProductDetailsManager) appEnvironment
                .getSpringManagedObject("certifiedProductDetailsManager");
    }
}

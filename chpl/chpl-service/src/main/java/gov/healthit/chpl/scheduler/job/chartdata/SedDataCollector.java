package gov.healthit.chpl.scheduler.job.chartdata;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.certifiedproduct.CertifiedProductDetailsManager;
import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.concept.CertificationEditionConcept;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.scheduler.SchedulerCertifiedProductSearchDetailsAsync;
import gov.healthit.chpl.search.domain.ListingSearchResult;

/**
 * Retrieves all of the 2015 SED Products and their details. Details are
 * retrieved asynchronously according to the chartDataExecutor defined in
 * AppConfig.
 *
 * @author TYoung
 *
 */
public class SedDataCollector {
    private static final Logger LOGGER = LogManager.getLogger("chartDataCreatorJobLogger");
    private static final String CRITERION_G_3 = "170.315 (g)(3)";

    @Autowired
    private CertificationCriterionDAO criteriaDao;

    @Autowired
    private CertifiedProductDetailsManager certifiedProductDetailsManager;

    @Autowired
    private SchedulerCertifiedProductSearchDetailsAsync cpsdAsync;

    public SedDataCollector() {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
    }

    /**
     * This method runs the data retrieval process for the 2015 SED products and
     * their details.
     *
     * @param listings
     *            initial set of Listings
     * @return List of CertifiedProductSearchDetails
     */
    public List<CertifiedProductSearchDetails> retreiveData(List<ListingSearchResult> listings) {

        List<ListingSearchResult> certifiedProducts = filterData(listings);
        LOGGER.info("2015/SED Certified Product Count: " + certifiedProducts.size());

        List<CertifiedProductSearchDetails> certifiedProductsWithDetails = getCertifiedProductDetailsForAll(certifiedProducts);
        return certifiedProductsWithDetails;
    }

    private List<ListingSearchResult> filterData(List<ListingSearchResult> certifiedProducts) {
        List<CertificationCriterionDTO> g3Criteria = criteriaDao.getAllByNumber(CRITERION_G_3);
        List<ListingSearchResult> results = new ArrayList<ListingSearchResult>();
        for (ListingSearchResult result : certifiedProducts) {
            if (result.getEdition().getName().equalsIgnoreCase(CertificationEditionConcept.CERTIFICATION_EDITION_2015.getYear())
                    && containsAnyCriterion(result, g3Criteria)) {
                results.add(result);
            }
        }
        return results;
    }

    private boolean containsAnyCriterion(ListingSearchResult listing, List<CertificationCriterionDTO> criteria) {
        boolean result = false;
        ListingSearchResult.CertificationCriterion[] certs = listing.getCriteriaMet().toArray(new ListingSearchResult.CertificationCriterion[0]);
        for (CertificationCriterionDTO criterion : criteria) {
            for (int i = 0; i < certs.length && !result; i++) {
                Long certId = certs[i].getId();
                if (criterion.getId().equals(certId)) {
                    result = true;
                }
            }
        }
        return result;
    }

    private List<CertifiedProductSearchDetails> getCertifiedProductDetailsForAll(List<ListingSearchResult> certifiedProducts) {

        List<CertifiedProductSearchDetails> details = new ArrayList<CertifiedProductSearchDetails>();
        List<Future<CertifiedProductSearchDetails>> futures = new ArrayList<Future<CertifiedProductSearchDetails>>();

        for (ListingSearchResult certifiedProduct : certifiedProducts) {
            try {
                    futures.add(cpsdAsync.getCertifiedProductDetail(certifiedProduct.getId(),
                            certifiedProductDetailsManager));
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
}

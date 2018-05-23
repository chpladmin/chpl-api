package gov.healthit.chpl.app.chartdata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import gov.healthit.chpl.dao.ListingCountStatisticsDAO;
import gov.healthit.chpl.dao.CertificationEditionDAO;
import gov.healthit.chpl.dao.CertificationStatusDAO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.search.CertifiedProductFlatSearchResult;
import gov.healthit.chpl.dto.ListingCountStatisticsDTO;

/**
 * Populates the listing_count_statistics table with summarized count information.
 * @author alarned
 *
 */
public class ListingCountStatisticsCalculator {
    private static final Logger LOGGER = LogManager.getLogger(ListingCountStatisticsCalculator.class);

    private ChartDataApplicationEnvironment appEnvironment;
    private ListingCountStatisticsDAO statisticsDAO;
    private CertificationEditionDAO certificationEditionDAO;
    private CertificationStatusDAO certificationStatusDAO;
    private JpaTransactionManager txnManager;
    private TransactionTemplate txnTemplate;

    ListingCountStatisticsCalculator(final ChartDataApplicationEnvironment appEnvironment) {
        this.appEnvironment = appEnvironment;
        initialize();
    }

    /**
     * This method calculates the listing count and saves them to the
     * listing_count_statistics table.
     * @param listings List of CertifiedProductFlatSearchResult objects
     */
    public void run(final List<CertifiedProductFlatSearchResult> listings) {
        List<ListingCountStatisticsDTO> productCounts = getCounts(listings);

        logCounts(productCounts);

        save(productCounts);
    }

    private void deleteExistingStatistics() throws EntityRetrievalException {
        List<ListingCountStatisticsDTO> dtos = statisticsDAO.findAll();
        for (ListingCountStatisticsDTO dto : dtos) {
            statisticsDAO.delete(dto.getId());
            LOGGER.info("Deleted: " + dto.getId());
        }
    }

    private List<ListingCountStatisticsDTO> getCounts(final List<CertifiedProductFlatSearchResult> listings) {
        /**
         * Loop through every Listing. For each Listing, the Developer and Product to a HashMap.
         * The key is of the form <EDITION>-<DEVELOPER NAME> for Developer,
         * and <EDITION>-<DEVELOPER NAME>-<PRODUCT NAME> for a Product.
         * The key maps to a HashSet of the Statuses of all of the Listings owned.
         *
         * Then, loop through each item in the sets and add counts as necessary to the Statistics DTOs.
         */
        HashMap<String, HashSet<String>> developers = new HashMap<String, HashSet<String>>();
        HashMap<String, HashSet<String>> products = new HashMap<String, HashSet<String>>();
        for (CertifiedProductFlatSearchResult listing: listings) {
            String devKey = listing.getEdition() + "\u263A" + listing.getDeveloper();
            String prodKey = listing.getEdition() + "\u263A" + listing.getDeveloper() + "\u263A" + listing.getProduct();
            if (!developers.containsKey(devKey)) {
                developers.put(devKey, new HashSet<String>());
            }
            if (!products.containsKey(prodKey)) {
                products.put(prodKey, new HashSet<String>());
            }
            developers.get(devKey).add(listing.getCertificationStatus());
            products.get(prodKey).add(listing.getCertificationStatus());
        }

        HashMap<String, ListingCountStatisticsDTO> results = new HashMap<String, ListingCountStatisticsDTO>();
        for (String developer : developers.keySet()) {
            String edition = developer.split("\u263A")[0];
            for (String status : developers.get(developer)) {
                String key = edition + "-" + status;
                if (!results.containsKey(key)) {
                    results.put(key, new ListingCountStatisticsDTO(
                            certificationEditionDAO.getByYear(edition),
                            certificationStatusDAO.getByStatusName(status)));
                }
                results.get(key).setDeveloperCount(results.get(key).getDeveloperCount() + 1L);
            }
        }
        for (String product : products.keySet()) {
            String edition = product.split("\u263A")[0];
            for (String status : products.get(product)) {
                String key = edition + "-" + status;
                if (!results.containsKey(key)) {
                    results.put(key, new ListingCountStatisticsDTO(
                            certificationEditionDAO.getByYear(edition),
                            certificationStatusDAO.getByStatusName(status)));
                }
                results.get(key).setProductCount(results.get(key).getDeveloperCount() + 1L);
            }
        }
        ArrayList<ListingCountStatisticsDTO> ret = new ArrayList<ListingCountStatisticsDTO>();
        for (ListingCountStatisticsDTO dto : results.values()) {
            ret.add(dto);
        }
        return ret;
    }

    private void initialize() {
        statisticsDAO = (ListingCountStatisticsDAO)
                appEnvironment.getSpringManagedObject("listingCountStatisticsDAO");
        certificationEditionDAO = (CertificationEditionDAO)
                appEnvironment.getSpringManagedObject("certificationEditionDAO");
        certificationStatusDAO = (CertificationStatusDAO)
                appEnvironment.getSpringManagedObject("certificationStatusDAO");
        txnManager = (JpaTransactionManager) appEnvironment.getSpringManagedObject("transactionManager");
        txnTemplate = new TransactionTemplate(txnManager);
    }

    private void logCounts(final List<ListingCountStatisticsDTO> dtos) {
        for (ListingCountStatisticsDTO dto : dtos) {
            LOGGER.info("Listing Count statistics: [" + dto.toString() + "]");
        }
    }

    private void save(final List<ListingCountStatisticsDTO> dtos) {
        txnTemplate.execute(new TransactionCallbackWithoutResult() {

            @Override
            protected void doInTransactionWithoutResult(final TransactionStatus arg0) {
                try {
                    deleteExistingStatistics();
                } catch (EntityRetrievalException e) {
                    LOGGER.error("Error occured while deleting existing ListingCountStatistics.", e);
                    return;
                }
                try {
                    for (ListingCountStatisticsDTO dto : dtos) {
                        statisticsDAO.create(dto);
                        LOGGER.info("Saved ListingCountStatisticsDTO"
                                + dto.toString());
                    }
                } catch (EntityCreationException | EntityRetrievalException e) {
                    LOGGER.error("Error occured while inserting counts.", e);
                    return;
                }
            }
        });
    }
}

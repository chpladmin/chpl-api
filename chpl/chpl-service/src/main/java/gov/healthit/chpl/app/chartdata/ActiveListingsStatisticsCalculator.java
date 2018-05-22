package gov.healthit.chpl.app.chartdata;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import gov.healthit.chpl.dao.ActiveListingsStatisticsDAO;
import gov.healthit.chpl.dao.CertificationEditionDAO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.search.CertifiedProductFlatSearchResult;
import gov.healthit.chpl.dto.ActiveListingsStatisticsDTO;

/**
 * Populates the active_listings_statistics table with summarized count information.
 * @author alarned
 *
 */
public class ActiveListingsStatisticsCalculator {
    private static final Logger LOGGER = LogManager.getLogger(ActiveListingsStatisticsCalculator.class);

    private ChartDataApplicationEnvironment appEnvironment;
    private ActiveListingsStatisticsDAO activeListingsStatisticsDAO;
    private CertificationEditionDAO certificationEditionDAO;
    private JpaTransactionManager txnManager;
    private TransactionTemplate txnTemplate;

    ActiveListingsStatisticsCalculator(final ChartDataApplicationEnvironment appEnvironment) {
        this.appEnvironment = appEnvironment;
        initialize();
    }

    /**
     * This method calculates the criterion-product counts and saves them to the
     * criterion_product_statistics table.
     * @param listings List of CertifiedProductFlatSearchResult objects
     */
    public void run(final List<CertifiedProductFlatSearchResult> listings) {
        List<ActiveListingsStatisticsDTO> productCounts = getCounts(listings);

        logCounts(productCounts);

        save(productCounts);
    }

    private void deleteExistingActiveListingsStatistics() throws EntityRetrievalException {
        List<ActiveListingsStatisticsDTO> dtos = activeListingsStatisticsDAO.findAll();
        for (ActiveListingsStatisticsDTO dto : dtos) {
            activeListingsStatisticsDAO.delete(dto.getId());
            LOGGER.info("Deleted: " + dto.getId());
        }
    }

    private List<ActiveListingsStatisticsDTO> getCounts(final List<CertifiedProductFlatSearchResult> listings) {
        /**
         * Loop through every Listing. For each Listing, the Developer and Product to a set.
         * The key is of the form <EDITION>-<DEVELOPER NAME> for Developer,
         * and <EDITION>-<DEVELOPER NAME>-<PRODUCT NAME> for a Product.
         *
         * Then, loop through each item in the sets and add counts as necessary to the Statistics DTOs.
         */
        HashSet<String> developers = new HashSet<String>();
        HashSet<String> products = new HashSet<String>();
        for (CertifiedProductFlatSearchResult listing: listings) {
            String devKey = listing.getEdition() + "\u263A" + listing.getDeveloper();
            String prodKey = listing.getEdition() + "\u263A" + listing.getDeveloper() + "\u263A" + listing.getProduct();
            developers.add(devKey);
            products.add(prodKey);
        }

        ActiveListingsStatisticsDTO edition2014 = new ActiveListingsStatisticsDTO();
        ActiveListingsStatisticsDTO edition2015 = new ActiveListingsStatisticsDTO();
        edition2014.setCertificationEditionId(certificationEditionDAO.getByYear("2014").getId());
        edition2014.setDeveloperCount(0L);
        edition2014.setProductCount(0L);
        edition2015.setCertificationEditionId(certificationEditionDAO.getByYear("2015").getId());
        edition2015.setDeveloperCount(0L);
        edition2015.setProductCount(0L);

        for (String entry : developers) {
            String[] values = entry.split("\u263A");
            switch (values[0]) {
            case "2014":
                edition2014.setDeveloperCount(edition2014.getDeveloperCount() + 1);
                break;
            case "2015":
                edition2015.setDeveloperCount(edition2015.getDeveloperCount() + 1);
                break;
            default:
                LOGGER.info("Listing has no edition");
            }
        }
        for (String entry : products) {
            String[] values = entry.split("\u263A");
            switch (values[0]) {
            case "2014":
                edition2014.setProductCount(edition2014.getProductCount() + 1);
                break;
            case "2015":
                edition2015.setProductCount(edition2015.getProductCount() + 1);
                break;
            default:
                LOGGER.info("Listing has no edition");
            }
        }
        ArrayList<ActiveListingsStatisticsDTO> result = new ArrayList<ActiveListingsStatisticsDTO>();
        result.add(edition2014);
        result.add(edition2015);
        return result;
    }

    private void initialize() {
        activeListingsStatisticsDAO = (ActiveListingsStatisticsDAO)
                appEnvironment.getSpringManagedObject("activeListingsStatisticsDAO");
        certificationEditionDAO = (CertificationEditionDAO)
                appEnvironment.getSpringManagedObject("certificationEditionDAO");
        txnManager = (JpaTransactionManager) appEnvironment.getSpringManagedObject("transactionManager");
        txnTemplate = new TransactionTemplate(txnManager);
    }

    private void logCounts(final List<ActiveListingsStatisticsDTO> dtos) {
        for (ActiveListingsStatisticsDTO dto : dtos) {
            LOGGER.info("Active Listings statistics: [" + dto.toString() + "]");
        }
    }

    private void save(final List<ActiveListingsStatisticsDTO> dtos) {
        txnTemplate.execute(new TransactionCallbackWithoutResult() {

            @Override
            protected void doInTransactionWithoutResult(final TransactionStatus arg0) {
                try {
                    deleteExistingActiveListingsStatistics();
                } catch (EntityRetrievalException e) {
                    LOGGER.error("Error occured while deleting existing ActiveListingsStatistics.", e);
                    return;
                }
                try {
                    for (ActiveListingsStatisticsDTO dto : dtos) {
                        activeListingsStatisticsDAO.create(dto);
                        LOGGER.info("Saved ActiveListingsStatisticsDTO"
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

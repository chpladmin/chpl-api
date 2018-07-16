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

import gov.healthit.chpl.dao.CertificationEditionDAO;
import gov.healthit.chpl.dao.CertificationStatusDAO;
import gov.healthit.chpl.dao.IncumbentDevelopersStatisticsDAO;
import gov.healthit.chpl.domain.search.CertifiedProductFlatSearchResult;
import gov.healthit.chpl.dto.IncumbentDevelopersStatisticsDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;

/**
 * Populates the criterion_product_statistics table with summarized count information.
 * @author alarned
 *
 */
public class IncumbentDevelopersStatisticsCalculator {
    private static final Logger LOGGER = LogManager.getLogger(IncumbentDevelopersStatisticsCalculator.class);

    private IncumbentDevelopersStatisticsDAO incumbentDevelopersStatisticsDAO;
    private CertificationEditionDAO certificationEditionDAO;
    private JpaTransactionManager txnManager;
    private TransactionTemplate txnTemplate;

    IncumbentDevelopersStatisticsCalculator(final ChartDataApplicationEnvironment appEnvironment) {
        incumbentDevelopersStatisticsDAO = (IncumbentDevelopersStatisticsDAO)
                appEnvironment.getSpringManagedObject("incumbentDevelopersStatisticsDAO");
        certificationEditionDAO = (CertificationEditionDAO)
                appEnvironment.getSpringManagedObject("certificationEditionDAO");
        txnManager = (JpaTransactionManager) appEnvironment.getSpringManagedObject("transactionManager");
        txnTemplate = new TransactionTemplate(txnManager);
    }

    IncumbentDevelopersStatisticsCalculator(final IncumbentDevelopersStatisticsDAO statisticsDAO,
            final CertificationEditionDAO certificationEditionDAO, final CertificationStatusDAO certificationStatusDAO,
            final JpaTransactionManager txnManager) {
        this.incumbentDevelopersStatisticsDAO = statisticsDAO;
        this.certificationEditionDAO = certificationEditionDAO;
        this.txnManager = txnManager;
        this.txnTemplate = new TransactionTemplate(txnManager);
    }

    /**
     * Loop through every Listing. For each Listing, add that Listing's Developer Name to
     * an edition specific set of Names depending on which edition that Listing is.
     *
     * Then, loop through each "later" set, comparing to each "earlier" set. If the "later"
     * name is not found in the "earlier" set, add it to the "new" component of the entity,
     * otherwise add it to the "incumbent" component.
     * @param certifiedProducts incoming data
     * @return statistics objects
     */
    public List<IncumbentDevelopersStatisticsDTO> getCounts(
            final List<CertifiedProductFlatSearchResult> certifiedProducts) {

        /**
         * Loop through every Listing. For each Listing, add that Listing's Developer Name to
         * an edition specific set of Names depending on which edition that Listing is.
         *
         * Then, loop through each "later" set, comparing to each "earlier" set. If the "later"
         * name is not found in the "earlier" set, add it to the "new" component of the entity,
         * otherwise add it to the "incumbent" component.
         */
        HashSet<String> developers2011 = new HashSet<String>();
        HashSet<String> developers2014 = new HashSet<String>();
        HashSet<String> developers2015 = new HashSet<String>();
        for (CertifiedProductFlatSearchResult listing: certifiedProducts) {
            switch (listing.getEdition()) {
            case "2011":
                developers2011.add(listing.getDeveloper());
                break;
            case "2014":
                developers2014.add(listing.getDeveloper());
                break;
            case "2015":
                developers2015.add(listing.getDeveloper());
                break;
            default:
                LOGGER.info("Listing has no edition");
            }
        }

        IncumbentDevelopersStatisticsDTO from2011To2014 = new IncumbentDevelopersStatisticsDTO();
        IncumbentDevelopersStatisticsDTO from2011To2015 = new IncumbentDevelopersStatisticsDTO();
        IncumbentDevelopersStatisticsDTO from2014To2015 = new IncumbentDevelopersStatisticsDTO();
        from2011To2014.setNewCount(0L);
        from2011To2014.setIncumbentCount(0L);
        from2011To2014.setOldCertificationEditionId(certificationEditionDAO.getByYear("2011").getId());
        from2011To2014.setNewCertificationEditionId(certificationEditionDAO.getByYear("2014").getId());
        from2011To2015.setNewCount(0L);
        from2011To2015.setIncumbentCount(0L);
        from2011To2015.setOldCertificationEditionId(certificationEditionDAO.getByYear("2011").getId());
        from2011To2015.setNewCertificationEditionId(certificationEditionDAO.getByYear("2015").getId());
        from2014To2015.setNewCount(0L);
        from2014To2015.setIncumbentCount(0L);
        from2014To2015.setOldCertificationEditionId(certificationEditionDAO.getByYear("2014").getId());
        from2014To2015.setNewCertificationEditionId(certificationEditionDAO.getByYear("2015").getId());
        LOGGER.info("Total 2011 Developers: " + developers2011.size());
        LOGGER.info("Total 2014 Developers: " + developers2014.size());
        LOGGER.info("Total 2015 Developers: " + developers2015.size());
        for (String name : developers2014) {
            if (developers2011.contains(name)) {
                from2011To2014.setIncumbentCount(from2011To2014.getIncumbentCount() + 1);
            } else {
                from2011To2014.setNewCount(from2011To2014.getNewCount() + 1);
            }
        }
        for (String name : developers2015) {
            if (developers2011.contains(name)) {
                from2011To2015.setIncumbentCount(from2011To2015.getIncumbentCount() + 1);
            } else {
                from2011To2015.setNewCount(from2011To2015.getNewCount() + 1);
            }
            if (developers2014.contains(name)) {
                from2014To2015.setIncumbentCount(from2014To2015.getIncumbentCount() + 1);
            } else {
                from2014To2015.setNewCount(from2014To2015.getNewCount() + 1);
            }
        }
        ArrayList<IncumbentDevelopersStatisticsDTO> result = new ArrayList<IncumbentDevelopersStatisticsDTO>();
        result.add(from2011To2015);
        result.add(from2014To2015);
        result.add(from2011To2014);
        return result;
    }

    /**
     * Log DTO statistics to LOGGER.
     * @param dtos statistics objects
     */
    public void logCounts(final List<IncumbentDevelopersStatisticsDTO> dtos) {
        for (IncumbentDevelopersStatisticsDTO dto : dtos) {
            LOGGER.info("Incumbent Developer statistics: [" + dto.toString() + "]");
        }
    }

    /**
     * Save statistics objects to database.
     * @param dtos statistics objects
     */
    public void save(final List<IncumbentDevelopersStatisticsDTO> dtos) {
        txnTemplate.execute(new TransactionCallbackWithoutResult() {

            @Override
            protected void doInTransactionWithoutResult(final TransactionStatus arg0) {
                try {
                    deleteExistingIncumbentDevelopersStatistics();
                } catch (EntityRetrievalException e) {
                    LOGGER.error("Error occured while deleting existing CriterionProductStatistics.", e);
                    return;
                }
                try {
                    for (IncumbentDevelopersStatisticsDTO dto : dtos) {
                        incumbentDevelopersStatisticsDAO.create(dto);
                        LOGGER.info("Saved IncumbentDevelopersStatisticsDTO"
                                + dto.toString());
                    }
                } catch (EntityCreationException | EntityRetrievalException e) {
                    LOGGER.error("Error occured while inserting counts.", e);
                    return;
                }
            }
        });
    }

    private void deleteExistingIncumbentDevelopersStatistics() throws EntityRetrievalException {
        List<IncumbentDevelopersStatisticsDTO> dtos = incumbentDevelopersStatisticsDAO.findAll();
        for (IncumbentDevelopersStatisticsDTO dto : dtos) {
            incumbentDevelopersStatisticsDAO.delete(dto.getId());
            LOGGER.info("Deleted: " + dto.getId());
        }
    }
}

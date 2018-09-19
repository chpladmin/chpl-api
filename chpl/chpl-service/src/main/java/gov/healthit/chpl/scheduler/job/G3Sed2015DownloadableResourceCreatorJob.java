package gov.healthit.chpl.scheduler.job;

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
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.scheduler.SchedulerCertifiedProductSearchDetailsAsync;
import gov.healthit.chpl.scheduler.presenter.Sed2015CsvPresenter;

/**
 * Quartz job to generate download files for SED.
 * @author kekey
 *
 */
@DisallowConcurrentExecution
public class G3Sed2015DownloadableResourceCreatorJob extends DownloadableResourceCreatorJob {
    private static final Logger LOGGER = LogManager.getLogger("g3Sed2015DownloadableResourceCreatorJobLogger");
    private static final String CRITERIA_NAME = "170.315 (g)(3)";
    private static final String EDITION = "2015";

    @Autowired
    private SchedulerCertifiedProductSearchDetailsAsync schedulerCertifiedProductSearchDetailsAsync;
    
    /**
     * Default constructor.
     * @throws Exception if issue with context
     */
    public G3Sed2015DownloadableResourceCreatorJob() throws Exception {
        super();
    }

    @Override
    public void execute(final JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        schedulerCertifiedProductSearchDetailsAsync.setLogger(LOGGER);

        LOGGER.info("********* Starting the G3 SED 2015 Downloadable Resource Creator job. *********");
        try {
            List<Long> listingIds = getRelevantListingIds();

            List<Future<CertifiedProductSearchDetails>> futures = getCertifiedProductSearchDetailsFutures(listingIds);
            Map<Long, CertifiedProductSearchDetails> cpMap = getMapFromFutures(futures);
            List<CertifiedProductSearchDetails> orderedListings = 
                    createOrderedListOfCertifiedProducts(cpMap, listingIds);

            File downloadFolder = getDownloadFolder();
            writeToFile(downloadFolder, orderedListings);
        } catch (Exception e) {
            LOGGER.error(e);
        }
        LOGGER.info("********* Completed the G3 SED 2015 Downloadable Resource Creator job. *********");
    }

    /**
     * Gets all listings that have certified to 170.315 (g)(3)
     * @return
     * @throws EntityRetrievalException
     */
    private List<Long> getRelevantListingIds() throws EntityRetrievalException {
        LOGGER.info("Finding all listings attesting to " + CRITERIA_NAME + ".");
        CertificationCriterionDTO certCrit = getCriteriaDao().getByNameAndYear(CRITERIA_NAME, EDITION);
        List<Long> listingIds = getCertificationResultDao().getCpIdsByCriterionId(certCrit.getId());
        LOGGER.info("Found " + listingIds.size() + " listings attesting to " + CRITERIA_NAME + ".");
        return listingIds;
    }

    private List<Future<CertifiedProductSearchDetails>> getCertifiedProductSearchDetailsFutures(
            final List<Long> listingIds) throws Exception {

        List<Future<CertifiedProductSearchDetails>> futures = new ArrayList<Future<CertifiedProductSearchDetails>>();
        SchedulerCertifiedProductSearchDetailsAsync cpsdAsync = getCertifiedProductDetailsAsyncRetrievalHelper();
        
        for (Long currListingId : listingIds) {
            try {
                futures.add(cpsdAsync.getCertifiedProductDetail(currListingId, getCpdManager()));
            } catch (EntityRetrievalException e) {
                LOGGER.error("Could not retrieve certified product details for id: " + currListingId, e);
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

    private void writeToFile(final File downloadFolder, final List<CertifiedProductSearchDetails> results)
            throws IOException {
        String csvFilename = getFileName(downloadFolder.getAbsolutePath(),
                getTimestampFormat().format(new Date()));
        File csvFile = getFile(csvFilename);
        Sed2015CsvPresenter csvPresenter = new Sed2015CsvPresenter();
        csvPresenter.presentAsFile(csvFile, results);
        LOGGER.info("Wrote G3 SED 2015 CSV file.");
    }

    private String getFileName(final String path, final String timeStamp) {
        return path + File.separator + "chpl-sed-all-details-" + timeStamp + ".csv";
    }

    private File getFile(final String fileName) throws IOException {
        File file = new File(fileName);
        if (file.exists()) {
            if (!file.delete()) {
                throw new IOException("File exists; cannot delete");
            }
        }
        if (!file.createNewFile()) {
            throw new IOException("File can not be created");
        }
        return file;
    }

    private SchedulerCertifiedProductSearchDetailsAsync getCertifiedProductDetailsAsyncRetrievalHelper()
            throws BeansException {
        return this.schedulerCertifiedProductSearchDetailsAsync;
    }
}

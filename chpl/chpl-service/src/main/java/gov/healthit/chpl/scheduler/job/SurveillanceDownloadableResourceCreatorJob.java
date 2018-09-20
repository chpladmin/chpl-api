package gov.healthit.chpl.scheduler.job;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.scheduler.SchedulerCertifiedProductSearchDetailsAsync;
import gov.healthit.chpl.scheduler.presenter.NonconformityCsvPresenter;
import gov.healthit.chpl.scheduler.presenter.SurveillanceCsvPresenter;
import gov.healthit.chpl.scheduler.presenter.SurveillanceReportCsvPresenter;

/**
 * Quartz job to generate download files for SED.
 * @author kekey
 *
 */
@DisallowConcurrentExecution
public class SurveillanceDownloadableResourceCreatorJob extends DownloadableResourceCreatorJob {
    private static final Logger LOGGER = LogManager.getLogger("surveillanceDownloadableResourceCreatorJobLogger");

    @Autowired
    private SchedulerCertifiedProductSearchDetailsAsync schedulerCertifiedProductSearchDetailsAsync;
    
    /**
     * Default constructor.
     * @throws Exception if issue with context
     */
    public SurveillanceDownloadableResourceCreatorJob() throws Exception {
        super();
    }

    @Override
    public void execute(final JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        schedulerCertifiedProductSearchDetailsAsync.setLogger(LOGGER);

        LOGGER.info("********* Starting the Surveillance Downloadable Resource Creator job. *********");
        try {
            List<CertifiedProductDetailsDTO> listings = getRelevantListings();

            List<Future<CertifiedProductSearchDetails>> futures = getCertifiedProductSearchDetailsFutures(listings);
            Map<Long, CertifiedProductSearchDetails> cpMap = getMapFromFutures(futures);
            List<CertifiedProductSearchDetails> orderedListings = 
                    createOrderedListOfCertifiedProducts(cpMap, listings);

            File downloadFolder = getDownloadFolder();
            writeSurveillanceAllFile(downloadFolder, orderedListings);
            writeSurveillanceWithNonconformitiesFile(downloadFolder, orderedListings);
            writeSurveillanceBasicReportFile(downloadFolder, orderedListings);
        } catch (Exception e) {
            LOGGER.error(e);
        }
        LOGGER.info("********* Completed the Surveillance Downloadable Resource Creator job. *********");
    }

    /**
     * Gets all listings that have surveillance
     * @return
     * @throws EntityRetrievalException
     */
    private List<CertifiedProductDetailsDTO> getRelevantListings() throws EntityRetrievalException {
        LOGGER.info("Finding all listings with surveillance.");
        List<CertifiedProductDetailsDTO> listings = getCertifiedProductDao().findWithSurveillance();
        LOGGER.info("Found " + listings.size() + " listings with surveillance.");
        return listings;
    }

    private List<Future<CertifiedProductSearchDetails>> getCertifiedProductSearchDetailsFutures(
            final List<CertifiedProductDetailsDTO> listings) throws Exception {

        List<Future<CertifiedProductSearchDetails>> futures = new ArrayList<Future<CertifiedProductSearchDetails>>();
        SchedulerCertifiedProductSearchDetailsAsync cpsdAsync = getCertifiedProductDetailsAsyncRetrievalHelper();
        
        for (CertifiedProductDetailsDTO currListing : listings) {
            try {
                futures.add(cpsdAsync.getCertifiedProductDetail(currListing.getId(), getCpdManager()));
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
            final Map<Long, CertifiedProductSearchDetails> certifiedProducts, 
            final List<CertifiedProductDetailsDTO> orderedListings) {

        List<CertifiedProductSearchDetails> ordered = new ArrayList<CertifiedProductSearchDetails>();

        for (CertifiedProductDetailsDTO listing : orderedListings) {
            if (certifiedProducts.containsKey(listing.getId())) {
                ordered.add(certifiedProducts.get(listing.getId()));
            }
        }

        return ordered;
    }

    private void writeSurveillanceAllFile(final File downloadFolder, final List<CertifiedProductSearchDetails> results)
            throws IOException {
        String csvFilename = downloadFolder.getAbsolutePath() + 
                File.separator + 
                "surveillance-all.csv";
        File csvFile = getFile(csvFilename);
        SurveillanceCsvPresenter csvPresenter = new SurveillanceCsvPresenter(getProperties());
        csvPresenter.presentAsFile(csvFile, results);
        LOGGER.info("Wrote Surveillance-All CSV file.");
    }

    private void writeSurveillanceWithNonconformitiesFile(final File downloadFolder, final List<CertifiedProductSearchDetails> results)
            throws IOException {
        String csvFilename = downloadFolder.getAbsolutePath() + 
                File.separator + 
                "surveillance-with-nonconformities.csv";
        File csvFile = getFile(csvFilename);
        NonconformityCsvPresenter csvPresenter = new NonconformityCsvPresenter(getProperties());
        csvPresenter.presentAsFile(csvFile, results);
        LOGGER.info("Wrote Surveillance With Nonconformities CSV file.");
    }

    private void writeSurveillanceBasicReportFile(final File downloadFolder, final List<CertifiedProductSearchDetails> results)
            throws IOException {
        String csvFilename = downloadFolder.getAbsolutePath() + 
                File.separator + 
                "surveillance-basic-report.csv";
        File csvFile = getFile(csvFilename);
        SurveillanceReportCsvPresenter csvPresenter = new SurveillanceReportCsvPresenter(getProperties());
        csvPresenter.presentAsFile(csvFile, results);
        LOGGER.info("Wrote Surveillance Basic Report CSV file.");
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

package gov.healthit.chpl.scheduler.job;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.scheduler.presenter.NonconformityCsvPresenter;
import gov.healthit.chpl.scheduler.presenter.SurveillanceCsvPresenter;
import gov.healthit.chpl.scheduler.presenter.SurveillanceReportCsvPresenter;

/**
 * Quartz job to generate downloadable files for surveillance reports.
 * 
 * @author kekey
 *
 */
@DisallowConcurrentExecution
public class SurveillanceDownloadableResourceCreatorJob extends DownloadableResourceCreatorJob {
    private static final Logger LOGGER = LogManager.getLogger("surveillanceDownloadableResourceCreatorJobLogger");

    @Autowired
    private Environment env;

    /**
     * Default constructor.
     * 
     * @throws Exception
     *             if issue with context
     */
    public SurveillanceDownloadableResourceCreatorJob() throws Exception {
        super(LOGGER);
    }

    @Override
    public void execute(final JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

        LOGGER.info("********* Starting the Surveillance Downloadable Resource Creator job. *********");
        try {
            List<CertifiedProductDetailsDTO> listings = getRelevantListings();

            List<Future<CertifiedProductSearchDetails>> futures = getCertifiedProductSearchDetailsFutures(listings);
            Map<Long, CertifiedProductSearchDetails> cpMap = getMapFromFutures(futures);
            List<CertifiedProductSearchDetails> orderedListings = createOrderedListOfCertifiedProducts(cpMap, listings);

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
     * 
     * @return
     * @throws EntityRetrievalException
     */
    private List<CertifiedProductDetailsDTO> getRelevantListings() throws EntityRetrievalException {
        LOGGER.info("Finding all listings with surveillance.");
        List<CertifiedProductDetailsDTO> listings = getCertifiedProductDao().findWithSurveillance();
        LOGGER.info("Found " + listings.size() + " listings with surveillance.");
        return listings;
    }

    private void writeSurveillanceAllFile(final File downloadFolder, final List<CertifiedProductSearchDetails> results)
            throws IOException {
        String csvFilename = downloadFolder.getAbsolutePath()
                + File.separator
                + env.getProperty("surveillanceAllReportName") + "-"
                + getFilenameTimestampFormat().format(new Date())
                + ".csv";
        File csvFile = getFile(csvFilename);
        SurveillanceCsvPresenter csvPresenter = new SurveillanceCsvPresenter(env);
        csvPresenter.presentAsFile(csvFile, results);
        LOGGER.info("Wrote Surveillance-All CSV file.");
    }

    private void writeSurveillanceWithNonconformitiesFile(final File downloadFolder,
            final List<CertifiedProductSearchDetails> results) throws IOException {
        String csvFilename = downloadFolder.getAbsolutePath()
                + File.separator
                + env.getProperty("surveillanceNonconformitiesReportName") + "-"
                + getFilenameTimestampFormat().format(new Date())
                + ".csv";
        File csvFile = getFile(csvFilename);
        NonconformityCsvPresenter csvPresenter = new NonconformityCsvPresenter(env);
        csvPresenter.presentAsFile(csvFile, results);
        LOGGER.info("Wrote Surveillance With Nonconformities CSV file.");
    }

    private void writeSurveillanceBasicReportFile(final File downloadFolder,
            final List<CertifiedProductSearchDetails> results) throws IOException {
        String csvFilename = downloadFolder.getAbsolutePath()
                + File.separator
                + env.getProperty("surveillanceBasicReportName") + "-"
                + getFilenameTimestampFormat().format(new Date())
                + ".csv";
        File csvFile = getFile(csvFilename);
        SurveillanceReportCsvPresenter csvPresenter = new SurveillanceReportCsvPresenter(env);
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
}

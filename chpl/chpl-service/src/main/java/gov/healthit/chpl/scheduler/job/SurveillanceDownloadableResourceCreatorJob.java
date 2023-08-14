package gov.healthit.chpl.scheduler.job;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
import gov.healthit.chpl.scheduler.presenter.CertifiedProductPresenter;
import gov.healthit.chpl.scheduler.presenter.SurveillanceAllCsvPresenter;

@DisallowConcurrentExecution
public class SurveillanceDownloadableResourceCreatorJob extends DownloadableResourceCreatorJob {
    private static final Logger LOGGER = LogManager.getLogger("surveillanceDownloadableResourceCreatorJobLogger");

    private File tempDirectory, tempSurveillanceAllCsvFile;
    private ExecutorService executorService;

    @Autowired
    private Environment env;

    public SurveillanceDownloadableResourceCreatorJob() throws Exception {
        super(LOGGER);
    }

    @Override
    public void execute(final JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

        LOGGER.info("********* Starting the Surveillance Downloadable Resource Creator job. *********");

        try (SurveillanceAllCsvPresenter surveillanceAllPresenter = new SurveillanceAllCsvPresenter(env)) {
            initializeTempFiles();
            //if (tempCsvFile != null && tempXmlFile != null) {
            if (tempSurveillanceAllCsvFile != null) {
                //initializeWritingToFiles(xmlPresenter, csvPresenter);
                initializeWritingToFiles(surveillanceAllPresenter);
                initializeExecutorService();

                List<CertifiedProductPresenter> presenters = List.of(surveillanceAllPresenter);
                List<CompletableFuture<Void>> futures = getCertifiedProductSearchFutures(getRelevantListings(), presenters);
                CompletableFuture<Void> combinedFutures = CompletableFuture
                        .allOf(futures.toArray(new CompletableFuture[futures.size()]));

                // This is not blocking - presumably because the job executes using it's own ExecutorService
                // This is necessary so that the system can indicate that the job and it's threads are still running
                combinedFutures.get();
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            LOGGER.info("All processes have completed");
        }

        try {
            //has to happen in a separate try block because of the presenters
            //using auto-close - can't move the files until they are closed by the presenters
            swapFiles();
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            cleanupTempFiles();
        } finally {
            cleanupTempFiles();
            executorService.shutdown();
            LOGGER.info("********* Completed the Surveillance Downloadable Resource Creator job. *********");
        }
    }

    private List<CompletableFuture<Void>> getCertifiedProductSearchFutures(List<CertifiedProductDetailsDTO> listings,
            List<CertifiedProductPresenter> presenters) {

        List<CompletableFuture<Void>> futures = new ArrayList<CompletableFuture<Void>>();
        for (CertifiedProductDetailsDTO certifiedProductDetails : listings) {
            futures.add(CompletableFuture
                    .supplyAsync(() -> getCertifiedProductSearchDetails(certifiedProductDetails.getId()), executorService)
                    .thenAccept(listing -> listing.ifPresent(cp -> addToPresenters(presenters, cp))));
        }
        return futures;
    }

    private void addToPresenters(List<CertifiedProductPresenter> presenters, CertifiedProductSearchDetails listing) {
        presenters.stream()
                .forEach(p -> {
                    try {
                        p.add(listing);
                    } catch (IOException e) {
                        LOGGER.error(String.format("Could not write listing to presenters: %s", listing.getId()), e);
                    }
                });
    }

   private void initializeWritingToFiles(SurveillanceAllCsvPresenter surveillanceCsvPresenter) throws IOException {
        surveillanceCsvPresenter.setLogger(LOGGER);
        surveillanceCsvPresenter.open(tempSurveillanceAllCsvFile);
    }

    private void initializeTempFiles() throws IOException {
        File downloadFolder = getDownloadFolder();
        Path tempDirBasePath = Paths.get(downloadFolder.getAbsolutePath());
        Path tempDir = Files.createTempDirectory(tempDirBasePath, TEMP_DIR_NAME);
        this.tempDirectory = tempDir.toFile();

        Path csvPath = Files.createTempFile(tempDir, env.getProperty("surveillanceAllReportName") + "-" + getFilenameTimestampFormat().format(new Date()), ".csv");
        tempSurveillanceAllCsvFile = csvPath.toFile();

    }

    private List<CertifiedProductDetailsDTO> getRelevantListings() throws EntityRetrievalException {
        LOGGER.info("Finding all listings with surveillance.");
        List<CertifiedProductDetailsDTO> listings = getCertifiedProductDao().findWithSurveillance();
        LOGGER.info("Found " + listings.size() + " listings with surveillance.");
        return listings;
    }

    private void swapFiles() throws IOException {
        LOGGER.info("Swapping temporary files.");
        File downloadFolder = getDownloadFolder();

        if (tempSurveillanceAllCsvFile != null) {
            String csvFilename = getFileName(downloadFolder.getAbsolutePath(),
                    getFilenameTimestampFormat().format(new Date()), "csv");
            LOGGER.info("Moving " + tempSurveillanceAllCsvFile.getAbsolutePath() + " to " + csvFilename);
            Path targetFile = Files.move(tempSurveillanceAllCsvFile.toPath(), Paths.get(csvFilename), StandardCopyOption.ATOMIC_MOVE);
            if (targetFile == null) {
                LOGGER.warn("Surveillance All CSV file move may not have succeeded. Check file system.");
            }
        } else {
            LOGGER.warn("Temp Surveillance All CSV File was null and could not be moved.");
        }
    }

    private void cleanupTempFiles() {
        LOGGER.info("Deleting temporary files.");
        if (tempSurveillanceAllCsvFile != null && tempSurveillanceAllCsvFile.exists()) {
            tempSurveillanceAllCsvFile.delete();
        } else {
            LOGGER.warn("Temp CSV File was null and could not be deleted.");
        }
    }

    private String getFileName(String path, String timeStamp, String extension) {
        return path + File.separator + env.getProperty("surveillanceAllReportName") + "-"
                + getFilenameTimestampFormat().format(new Date()) + extension;
    }

    private Integer getThreadCountForJob() throws NumberFormatException {
        return Integer.parseInt(env.getProperty("executorThreadCountForQuartzJobs"));
    }

    private void initializeExecutorService() {
        executorService = Executors.newFixedThreadPool(getThreadCountForJob());
    }

    /*
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
    */
}

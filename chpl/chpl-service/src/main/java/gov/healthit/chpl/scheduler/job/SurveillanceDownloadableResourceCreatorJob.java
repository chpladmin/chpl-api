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
import gov.healthit.chpl.scheduler.presenter.NonconformityCsvPresenter;
import gov.healthit.chpl.scheduler.presenter.SurveillanceAllCsvPresenter;
import gov.healthit.chpl.scheduler.presenter.SurveillanceBasicCsvPresenter;
import gov.healthit.chpl.scheduler.presenter.SurveillanceCsvPresenter;

@DisallowConcurrentExecution
public class SurveillanceDownloadableResourceCreatorJob extends DownloadableResourceCreatorJob {
    private static final Logger LOGGER = LogManager.getLogger("surveillanceDownloadableResourceCreatorJobLogger");

    private File tempDirectory; //, tempSurveillanceAllCsvFile, tempNonconformityCsvFile, tempSurveillanceCsvFile;
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
        List<SurveillanceCsvPresenter> presenters = new ArrayList<SurveillanceCsvPresenter>();

        try (SurveillanceAllCsvPresenter surveillanceAllPresenter = new SurveillanceAllCsvPresenter(env);
                NonconformityCsvPresenter nonConformityPresenter = new NonconformityCsvPresenter(env);
                SurveillanceBasicCsvPresenter surveillanceBasicPresenter = new SurveillanceBasicCsvPresenter(env)) {

            presenters = List.of(surveillanceAllPresenter,
                    nonConformityPresenter,
                    surveillanceBasicPresenter);

            initializeWritingToFiles(presenters);
            initializeExecutorService();

            List<CompletableFuture<Void>> futures = getCertifiedProductSearchFutures(getRelevantListings(), presenters);
            CompletableFuture<Void> combinedFutures = CompletableFuture
                    .allOf(futures.toArray(new CompletableFuture[futures.size()]));

            // This is not blocking - presumably because the job executes using it's own ExecutorService
            // This is necessary so that the system can indicate that the job and it's threads are still running
            combinedFutures.get();

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            LOGGER.info("All processes have completed");
        }

        try {
            //has to happen in a separate try block because of the presenters
            //using auto-close - can't move the files until they are closed by the presenters
            swapFiles(presenters);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            cleanupTempFiles(presenters);
        } finally {
            cleanupTempFiles(presenters);
            executorService.shutdown();
            LOGGER.info("********* Completed the Surveillance Downloadable Resource Creator job. *********");
        }
    }

    private List<CompletableFuture<Void>> getCertifiedProductSearchFutures(List<CertifiedProductDetailsDTO> listings,
            List<SurveillanceCsvPresenter> presenters) {

        List<CompletableFuture<Void>> futures = new ArrayList<CompletableFuture<Void>>();
        for (CertifiedProductDetailsDTO certifiedProductDetails : listings) {
            futures.add(CompletableFuture
                    .supplyAsync(() -> getCertifiedProductSearchDetails(certifiedProductDetails.getId()), executorService)
                    .thenAccept(listing -> listing.ifPresent(cp -> addToPresenters(presenters, cp))));
        }
        return futures;
    }

    private void addToPresenters(List<SurveillanceCsvPresenter> presenters, CertifiedProductSearchDetails listing) {
        presenters.stream()
                .forEach(p -> {
                    try {
                        p.add(listing);
                    } catch (IOException e) {
                        LOGGER.error(String.format("Could not write listing to presenters: %s", listing.getId()), e);
                    }
                });
    }

    private void initializeWritingToFiles(List<SurveillanceCsvPresenter> presenters) throws IOException {
        File downloadFolder = getDownloadFolder();
        Path tempDirBasePath = Paths.get(downloadFolder.getAbsolutePath());
        Path tempDir = Files.createTempDirectory(tempDirBasePath, TEMP_DIR_NAME);
        this.tempDirectory = tempDir.toFile();

        presenters.forEach(presenter -> {
            try {
                presenter.setLogger(LOGGER);
                presenter.setTempFile(Files.createTempFile(tempDir, presenter.getFileName() + "-"
                    + getFilenameTimestampFormat().format(new Date()), ".csv").toFile());
                presenter.open();
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
        });
    }

    private List<CertifiedProductDetailsDTO> getRelevantListings() throws EntityRetrievalException {
        LOGGER.info("Finding all listings with surveillance.");
        List<CertifiedProductDetailsDTO> listings = getCertifiedProductDao().findWithSurveillance();
        LOGGER.info("Found " + listings.size() + " listings with surveillance.");
        return listings;
    }

    private void swapFiles(List<SurveillanceCsvPresenter> presenters) throws IOException {
        LOGGER.info("Swapping temporary files.");
        File downloadFolder = getDownloadFolder();

        presenters.forEach(presenter -> {
            try {
                if (presenter.getTempFile() != null) {
                    String csvFilename = getFileName(downloadFolder.getAbsolutePath(), presenter.getFileName(),
                            getFilenameTimestampFormat().format(new Date()), "csv");
                    LOGGER.info("Moving " + presenter.getTempFile().getAbsolutePath() + " to " + csvFilename);
                    Path targetFile = Files.move(presenter.getTempFile().toPath(), Paths.get(csvFilename), StandardCopyOption.ATOMIC_MOVE);
                    if (targetFile == null) {
                        LOGGER.warn(presenter.getPresenterName() + " CSV file move may not have succeeded. Check file system.");
                    }
                } else {
                    LOGGER.warn("Temp " + presenter.getPresenterName() + " Surveillance All CSV File was null and could not be moved.");
                }
            } catch (IOException ex) {

            }
        });
    }

    private void cleanupTempFiles(List<SurveillanceCsvPresenter> presenters) {
        LOGGER.info("Deleting temporary files.");
        presenters.forEach(presenter -> {
            if (presenter.getTempFile() != null && presenter.getTempFile().exists()) {
                presenter.getTempFile().delete();
            } else {
                LOGGER.warn("Temp " + presenter.getPresenterName() + " CSV File was null and could not be deleted.");
            }

        });
    }

    private String getFileName(String path, String name, String timeStamp, String extension) {
        return path + File.separator + name + "-" + getFilenameTimestampFormat().format(new Date()) + "." + extension;
    }

    private Integer getThreadCountForJob() throws NumberFormatException {
        return Integer.parseInt(env.getProperty("executorThreadCountForQuartzJobs"));
    }

    private void initializeExecutorService() {
        executorService = Executors.newFixedThreadPool(getThreadCountForJob());
    }
}

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

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.scheduler.presenter.CertifiedProductJsonPresenter;
import gov.healthit.chpl.scheduler.presenter.CertifiedProductPresenter;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "jsonListingDownloadCreatorJobLogger")
@DisallowConcurrentExecution
public class JsonListingDownloadCreatorJob extends DownloadableResourceCreatorJob {
    private static final int MILLIS_PER_SECOND = 1000;
    private File tempDirectory, tempJsonFile;
    private ExecutorService executorService;

    @Autowired
    private Environment env;

    public JsonListingDownloadCreatorJob() throws Exception {
        super(LOGGER);
    }

    @Override
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

        LOGGER.info("********* Starting the Json Listing Download Creator Job *********");
        try (CertifiedProductJsonPresenter jsonPresenter = new CertifiedProductJsonPresenter()) {

            initializeTempFile();
            if (tempJsonFile != null) {
                initializeWritingToFiles(jsonPresenter);
                initializeExecutorService();

                List<CertifiedProductPresenter> presenters = List.of(jsonPresenter);
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
            cleanupTempFile();
        } finally {
            cleanupTempFile();
            executorService.shutdown();
            LOGGER.info("********* Completed the Json Listing Download Creator Job *********");
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

    private void initializeWritingToFiles(CertifiedProductJsonPresenter jsonPresenter) throws IOException {
        jsonPresenter.setLogger(LOGGER);
        jsonPresenter.open(tempJsonFile);
    }

    private List<CertifiedProductDetailsDTO> getRelevantListings() throws EntityRetrievalException {
        LOGGER.info("Finding all listings.");
        Date start = new Date();
        List<CertifiedProductDetailsDTO> listingsForEdition = getCertifiedProductDao().findAll();
        Date end = new Date();
        LOGGER.info("Found the " + listingsForEdition.size() + " listings in "
                + ((end.getTime() - start.getTime()) / MILLIS_PER_SECOND) + " seconds");
        return listingsForEdition;
    }

    private void initializeTempFile() throws IOException {
        File downloadFolder = getDownloadFolder();
        Path tempDirBasePath = Paths.get(downloadFolder.getAbsolutePath());
        Path tempDir = Files.createTempDirectory(tempDirBasePath, TEMP_DIR_NAME);
        this.tempDirectory = tempDir.toFile();

        Path jsonPath = Files.createTempFile(tempDir, "chpl", ".json");
        tempJsonFile = jsonPath.toFile();
    }

    private void swapFiles() throws IOException {
        LOGGER.info("Swapping temporary files.");
        File downloadFolder = getDownloadFolder();

        if (tempJsonFile != null) {
            String jsonFilename = getFileName(downloadFolder.getAbsolutePath(),
                    getFilenameTimestampFormat().format(new Date()), "json");
            LOGGER.info("Moving " + tempJsonFile.getAbsolutePath() + " to " + jsonFilename);
            Path targetFile = Files.move(tempJsonFile.toPath(), Paths.get(jsonFilename), StandardCopyOption.ATOMIC_MOVE);
            if (targetFile == null) {
                LOGGER.warn("JSON file move may not have succeeded. Check file system.");
            }
        } else {
            LOGGER.warn("Temp JSON File was null and could not be moved.");
        }

    }

    private void cleanupTempFile() {
        LOGGER.info("Deleting temporary files.");
            if (tempJsonFile != null && tempJsonFile.exists()) {
            tempJsonFile.delete();
        } else {
            LOGGER.warn("Temp JSON File was null and could not be deleted.");
        }

        if (tempDirectory != null && tempDirectory.exists()) {
            tempDirectory.delete();
        } else {
            LOGGER.warn("Temp directory for download files was null and could not be deleted.");
        }
    }

    private String getFileName(String path, String timeStamp, String extension) {
        return path + File.separator + "chpl-" + timeStamp + "." + extension;
    }

    private Integer getThreadCountForJob() throws NumberFormatException {
        return Integer.parseInt(env.getProperty("executorThreadCountForQuartzJobs"));
    }

    private void initializeExecutorService() {
        executorService = Executors.newFixedThreadPool(getThreadCountForJob());
    }

}

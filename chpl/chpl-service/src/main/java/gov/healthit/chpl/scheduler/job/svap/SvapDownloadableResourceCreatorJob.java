package gov.healthit.chpl.scheduler.job.svap;

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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.dao.ActivityDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.scheduler.job.DownloadableResourceCreatorJob;
import gov.healthit.chpl.scheduler.presenter.CertifiedProductCsvPresenter;
import gov.healthit.chpl.scheduler.presenter.CertifiedProductPresenter;
import gov.healthit.chpl.scheduler.presenter.CertifiedProductXmlPresenter;
import lombok.extern.log4j.Log4j2;

@DisallowConcurrentExecution
@Log4j2(topic = "svapDownloadableResourceCreatorJobLogger")
public class SvapDownloadableResourceCreatorJob extends DownloadableResourceCreatorJob {
    private static final int MILLIS_PER_SECOND = 1000;
    private static final String TEMP_DIR_NAME = "temp";
    private File tempDirectory, tempCsvFile;
    private ExecutorService executorService;

    @Autowired
    private ActivityDAO activityDao;

    @Autowired
    private CertifiedProductDAO cpDao;

    @Autowired
    private Environment env;

    public SvapDownloadableResourceCreatorJob() throws Exception {
        super(LOGGER);
    }

    @Override
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

        LOGGER.info("********* Starting the SVAP Downloadable Resource Creator job. *********");
        try (CertifiedProductXmlPresenter xmlPresenter = new CertifiedProductXmlPresenter();
                CertifiedProductCsvPresenter csvPresenter = getCsvPresenter()) {
            initializeTempFile();
            if (tempCsvFile != null) {
                initializeWritingToFile(csvPresenter);
                initializeExecutorService();
                List<Long> listingIdsWithSvap = getRelevantListingIds();
                List<CertifiedProductPresenter> presenters = Stream.of(csvPresenter).collect(Collectors.toList());
                List<CompletableFuture<Void>> futures = getCertifiedProductSearchFutures(listingIdsWithSvap, presenters);
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
            LOGGER.info("********* Completed the SVAP Downloadable Resource Creator job. *********");
        }
    }

    private List<CompletableFuture<Void>> getCertifiedProductSearchFutures(List<Long> listingIds,
            List<CertifiedProductPresenter> presenters) {

        List<CompletableFuture<Void>> futures = new ArrayList<CompletableFuture<Void>>();
        for (Long listingId : listingIds) {
            futures.add(CompletableFuture
                    .supplyAsync(() -> getCertifiedProductSearchDetails(listingId), executorService)
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

    private void initializeWritingToFile(CertifiedProductCsvPresenter csvPresenter)
            throws IOException {
        csvPresenter.setLogger(LOGGER);
        csvPresenter.open(tempCsvFile);
    }

    private List<Long> getRelevantListingIds() throws EntityRetrievalException {
        LOGGER.info("Finding all listings with SVAP data.");
        Date start = new Date();
        List<Long> listingIdsWithSvap = cpDao.findListingIdsWithSvap();
        Date end = new Date();
        LOGGER.info("Found " + listingIdsWithSvap.size() + " listings with SVAP data in "
                + ((end.getTime() - start.getTime()) / MILLIS_PER_SECOND) + " seconds");
        return listingIdsWithSvap;
    }

    private CertifiedProductCsvPresenter getCsvPresenter() {
        CertifiedProductCsvPresenter presenter = new CertifiedProductCsvPresenter();
        return presenter;
    }

    private void initializeTempFile() throws IOException {
        File downloadFolder = getDownloadFolder();
        Path tempDirBasePath = Paths.get(downloadFolder.getAbsolutePath());
        Path tempDir = Files.createTempDirectory(tempDirBasePath, TEMP_DIR_NAME);
        this.tempDirectory = tempDir.toFile();

        Path csvPath = Files.createTempFile(tempDir, "chpl-svap", ".csv");
        tempCsvFile = csvPath.toFile();
    }

    private void swapFiles() throws IOException {
        LOGGER.info("Swapping temporary files.");
        File downloadFolder = getDownloadFolder();

        if (tempCsvFile != null) {
            String csvFilename = getFileName(downloadFolder.getAbsolutePath(),
                    getFilenameTimestampFormat().format(new Date()), "csv");
            LOGGER.info("Moving " + tempCsvFile.getAbsolutePath() + " to " + csvFilename);
            Path targetFile = Files.move(tempCsvFile.toPath(), Paths.get(csvFilename), StandardCopyOption.ATOMIC_MOVE);
            if (targetFile == null) {
                LOGGER.warn("CSV file move may not have succeeded. Check file system.");
            }
        } else {
            LOGGER.warn("Temp CSV File was null and could not be moved.");
        }
    }

    private void cleanupTempFiles() {
        LOGGER.info("Deleting temporary files.");
        if (tempCsvFile != null && tempCsvFile.exists()) {
            tempCsvFile.delete();
        } else {
            LOGGER.warn("Temp CSV File was null and could not be deleted.");
        }

        if (tempDirectory != null && tempDirectory.exists()) {
            tempDirectory.delete();
        } else {
            LOGGER.warn("Temp directory for download files was null and could not be deleted.");
        }
    }

    private String getFileName(String path, String timeStamp, String extension) {
        return path + File.separator + "chpl-svap" + "-" + timeStamp + "." + extension;
    }

    private Integer getThreadCountForJob() throws NumberFormatException {
        return Integer.parseInt(env.getProperty("executorThreadCountForQuartzJobs"));
    }

    private void initializeExecutorService() {
        executorService = Executors.newFixedThreadPool(getThreadCountForJob());
    }
}

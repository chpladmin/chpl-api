package gov.healthit.chpl.scheduler.job.snapshot;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ff4j.FF4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.fasterxml.jackson.databind.ObjectMapper;

import gov.healthit.chpl.activity.history.ListingActivityUtil;
import gov.healthit.chpl.activity.history.explorer.ListingExistsOnDateActivityExplorer;
import gov.healthit.chpl.activity.history.query.ListingOnDateActivityQuery;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.scheduler.job.DownloadableResourceCreatorJob;
import gov.healthit.chpl.scheduler.presenter.CertifiedProductCsvPresenter;
import gov.healthit.chpl.scheduler.presenter.CertifiedProductPresenter;
import gov.healthit.chpl.service.CertificationCriterionService;

@DisallowConcurrentExecution
public class ListingSnapshotDownloadableResourceCreatorJob extends DownloadableResourceCreatorJob {
    private static final Logger LOGGER = LogManager.getLogger("listingSnapshotDownloadableResourceCreatorJobLogger");
    private static final int MILLIS_PER_SECOND = 1000;
    private static final String TEMP_DIR_NAME = "temp";

    private String edition;
    private LocalDate snapshotDate;
    private ObjectMapper jsonMapper;
    private File tempDirectory, tempCsvFile;
    private ExecutorService executorService;

    @Autowired
    private CertificationCriterionService criterionService;

    @Autowired
    private FF4j ff4j;

    @Autowired
    private ListingActivityUtil listingActivityUtil;

    @Autowired
    private ListingExistsOnDateActivityExplorer activityExplorer;

    @Autowired
    private Environment env;

    public ListingSnapshotDownloadableResourceCreatorJob() throws Exception {
        super(LOGGER);
        jsonMapper = new ObjectMapper();
    }

    @Override
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        parseParametersFromContext(jobContext);
        if (StringUtils.isEmpty(edition) || snapshotDate == null) {
            LOGGER.info("Not running job due to missing 'edition' or 'snapshotDate' parameter.");
            return;
        }

        LOGGER.info("********* Starting the Listing Snapshot Downloadable Resource Creator job for {} on {}. *********", edition, snapshotDate);
        try (CertifiedProductCsvPresenter csvPresenter = getCsvPresenter()) {
            initializeTempFiles();
            if (tempCsvFile != null) {
                initializeWritingToFiles(csvPresenter);
                initializeExecutorService();

                List<CertifiedProductPresenter> presenters = new ArrayList<CertifiedProductPresenter>(
                        Arrays.asList(csvPresenter));
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
            LOGGER.info("********* Completed the Listing Snapshot Downloadable Resource Creator job for {} on {}. *********", edition, snapshotDate);
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

    @Override
    protected Optional<CertifiedProductSearchDetails> getCertifiedProductSearchDetails(Long listingId) {
        ListingOnDateActivityQuery query = ListingOnDateActivityQuery.builder()
                .day(snapshotDate)
                .listingId(listingId)
                .build();
        ActivityDTO activityWithListingOnDay = activityExplorer.getActivity(query);
        if (activityWithListingOnDay == null) {
            return Optional.empty();
        }

        return getDetailsFromJson(activityWithListingOnDay.getNewData());
    }

    private Optional<CertifiedProductSearchDetails> getDetailsFromJson(String json) {
        CertifiedProductSearchDetails listing = listingActivityUtil.getListing(json);
        if (listing != null) {
            return Optional.of(listing);
        }
        return Optional.empty();
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

    private void initializeWritingToFiles(CertifiedProductCsvPresenter csvPresenter)
            throws IOException {
        csvPresenter.setLogger(LOGGER);
        csvPresenter.setFf4j(ff4j);
        List<CertificationCriterionDTO> criteria = getCriteriaDao().findByCertificationEditionYear(edition)
                .stream()
                .filter(cr -> !cr.getRemoved())
                .sorted((crA, crB) -> criterionService.sortCriteria(crA, crB))
                .collect(Collectors.<CertificationCriterionDTO>toList());
        csvPresenter.setApplicableCriteria(criteria);
        csvPresenter.open(tempCsvFile);
    }

    private List<Long> getRelevantListings() throws EntityRetrievalException {
        List<Long> relevantListingIds = getCertifiedProductDao().findIdsByEditionAndCreatedBeforeDate(edition, snapshotDate);
        LOGGER.info("Found the " + relevantListingIds.size() + " listings from edition " + edition
                + " and created before " + snapshotDate);
        return relevantListingIds;
    }

    private CertifiedProductCsvPresenter getCsvPresenter() {
       return new CertifiedProductCsvPresenter();
    }

    private void initializeTempFiles() throws IOException {
        File downloadFolder = getDownloadFolder();
        Path tempDirBasePath = Paths.get(downloadFolder.getAbsolutePath());
        Path tempDir = Files.createTempDirectory(tempDirBasePath, TEMP_DIR_NAME);
        this.tempDirectory = tempDir.toFile();

        Path csvPath = Files.createTempFile(tempDir, "chpl-" + edition + "-" + snapshotDate, ".csv");
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

    private String getFileName(String path, String timestamp, String extension) {
        return path + File.separator + "chpl-" + edition + "-" + snapshotDate + "-" + timestamp + "." + extension;
    }

    private void parseParametersFromContext(JobExecutionContext jobContext) {
        edition = jobContext.getMergedJobDataMap().getString("edition");
        String snapshotDateStr = jobContext.getMergedJobDataMap().getString("snapshotDate");
        try {
            snapshotDate = LocalDate.parse(snapshotDateStr);
        } catch (DateTimeParseException ex) {
            LOGGER.error("Unable to parse snapshot date: " + snapshotDateStr);
        }
    }

    private Integer getThreadCountForJob() throws NumberFormatException {
        return Integer.parseInt(env.getProperty("executorThreadCountForQuartzJobs"));
    }

    private void initializeExecutorService() {
        executorService = Executors.newFixedThreadPool(getThreadCountForJob());
    }
}

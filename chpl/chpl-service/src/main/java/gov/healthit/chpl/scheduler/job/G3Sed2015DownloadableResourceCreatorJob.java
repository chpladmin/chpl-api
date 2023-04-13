package gov.healthit.chpl.scheduler.job;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.scheduler.presenter.CertifiedProductPresenter;
import gov.healthit.chpl.scheduler.presenter.Sed2015CsvPresenter;
import gov.healthit.chpl.search.ListingSearchService;
import gov.healthit.chpl.search.domain.ListingSearchResult;
import gov.healthit.chpl.search.domain.SearchRequest;
import gov.healthit.chpl.search.domain.SearchSetOperator;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.service.CertificationCriterionService.Criteria2015;
import gov.healthit.chpl.util.Util;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "g3Sed2015DownloadableResourceCreatorJobLogger")
@DisallowConcurrentExecution
public class G3Sed2015DownloadableResourceCreatorJob extends DownloadableResourceCreatorJob {
    private static final int MILLIS_PER_SECOND = 1000;

    @Autowired
    private CertificationCriterionService criterionService;

    @Autowired
    private ListingSearchService listingSearchService;

    @Autowired
    private Environment env;

    @Value("${SEDDownloadName}")
    private String sedDownloadFilename;

    private ExecutorService executorService;
    private File tempDirectory, tempCsvFile;
    private CertificationCriterion g3;

    public G3Sed2015DownloadableResourceCreatorJob() throws Exception {
        super(LOGGER);
    }

    @Override
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the G3 SED 2015 Downloadable Resource Creator job. *********");
        g3 = criterionService.get(Criteria2015.G_3);

        try (Sed2015CsvPresenter csvPresenter = new Sed2015CsvPresenter()) {
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
            LOGGER.info("********* Completed the G3 SED 2015 Downloadable Resource Creator job. *********");
        }
    }

    private List<CompletableFuture<Void>> getCertifiedProductSearchFutures(List<ListingSearchResult> listings,
            List<CertifiedProductPresenter> presenters) {

        List<CompletableFuture<Void>> futures = new ArrayList<CompletableFuture<Void>>();
        for (ListingSearchResult listing : listings) {
            futures.add(CompletableFuture
                    .supplyAsync(() -> getCertifiedProductSearchDetails(listing.getId()), executorService)
                    .thenAccept(listingDetailsOpt -> listingDetailsOpt.ifPresent(cp -> addToPresenters(presenters, cp))));
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

    private void initializeWritingToFiles(Sed2015CsvPresenter csvPresenter)
            throws IOException {
        csvPresenter.setLogger(LOGGER);
        csvPresenter.open(tempCsvFile);
    }

    private List<ListingSearchResult> getRelevantListings() throws EntityRetrievalException {
        LOGGER.info("Finding all listings attesting to " + Util.formatCriteriaNumber(g3));
        List<ListingSearchResult> relevantListings = listingSearchService.getAllPagesOfSearchResults(
                SearchRequest.builder()
                .certificationCriteriaIds(Stream.of(g3.getId()).collect(Collectors.toSet()))
                .certificationCriteriaOperator(SearchSetOperator.AND)
                .build(), LOGGER);
        LOGGER.info(relevantListings.size() + " listing attest to " + Util.formatCriteriaNumber(g3));

        return relevantListings;
    }

    private void initializeTempFiles() throws IOException {
        File downloadFolder = getDownloadFolder();
        Path tempDirBasePath = Paths.get(downloadFolder.getAbsolutePath());
        Path tempDir = Files.createTempDirectory(tempDirBasePath, TEMP_DIR_NAME);
        this.tempDirectory = tempDir.toFile();

        Path csvPath = Files.createTempFile(tempDir, sedDownloadFilename, ".csv");
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
        return path + File.separator + sedDownloadFilename + "-" + timeStamp + "." + extension;
    }
//
//    private Optional<CertifiedProductSearchDetails> getListingFromFuture(
//            CompletableFuture<Optional<CertifiedProductSearchDetails>> future) {
//
//        try {
//            Optional<CertifiedProductSearchDetails> optionalListing = future.get();
//            LOGGER.info("Completed retrieving listing: " + optionalListing.get().getId());
//            return optionalListing;
//        } catch (InterruptedException | ExecutionException e) {
//            LOGGER.info(e.getMessage(), e);
//            return Optional.empty();
//        }
//    }
//
//    private List<CompletableFuture<Optional<CertifiedProductSearchDetails>>> getCertifiedProductSearchDetails(
//            List<Long> listingIds) {
//
//        List<CompletableFuture<Optional<CertifiedProductSearchDetails>>> futures =
//                new ArrayList<CompletableFuture<Optional<CertifiedProductSearchDetails>>>();
//
//        for (Long currListingId : listingIds) {
//            futures.add(
//                    CompletableFuture.supplyAsync(() -> getCertifiedProductDetails(currListingId), executorService));
//        }
//        return futures;
//    }
//
//    private Optional<CertifiedProductSearchDetails> getCertifiedProductDetails(Long id) {
//        try {
//            return Optional.of(certifiedProductDetailsManager.getCertifiedProductDetails(id));
//        } catch (EntityRetrievalException e) {
//            LOGGER.error("Could not retrieve certified product details for id: " + id, e);
//            return Optional.empty();
//        }
//    }
//
//    private List<Long> getRelevantListingIds() throws EntityRetrievalException {
//        LOGGER.info("Finding all listings attesting to " + CRITERIA_NAME + ".");
//        CertificationCriterionDTO certCrit = getCriteriaDao().getByNumberAndTitle(CRITERIA_NAME, TITLE);
//        List<Long> listingIds = getCertificationResultDao().getCpIdsByCriterionId(certCrit.getId());
//        LOGGER.info("Found " + listingIds.size() + " listings attesting to " + CRITERIA_NAME + ".");
//        return listingIds;
//    }
//
//    private void writeToFile(File downloadFolder, List<CertifiedProductSearchDetails> results) throws IOException {
//        String csvFilename = downloadFolder.getAbsolutePath()
//                + File.separator
//                + env.getProperty("SEDDownloadName")
//                + "-" + getFilenameTimestampFormat().format(new Date())
//                + ".csv";
//        File csvFile = getFile(csvFilename);
//        Sed2015CsvPresenter csvPresenter = new Sed2015CsvPresenter();
//        csvPresenter.presentAsFile(csvFile, results, criterionService);
//        LOGGER.info("Wrote G3 SED 2015 CSV file.");
//    }

//    private File getFile(String fileName) throws IOException {
//        File file = new File(fileName);
//        if (file.exists()) {
//            if (!file.delete()) {
//                throw new IOException("File exists; cannot delete");
//            }
//        }
//        if (!file.createNewFile()) {
//            throw new IOException("File can not be created");
//        }
//        return file;
//    }

    private Integer getThreadCountForJob() throws NumberFormatException {
        return Integer.parseInt(env.getProperty("executorThreadCountForQuartzJobs"));
    }

    private void initializeExecutorService() {
        this.executorService = Executors.newFixedThreadPool(getThreadCountForJob());
    }
}

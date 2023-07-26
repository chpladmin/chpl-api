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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.scheduler.presenter.CertifiedProduct2014CsvPresenter;
import gov.healthit.chpl.scheduler.presenter.CertifiedProductCsvPresenter;
import gov.healthit.chpl.scheduler.presenter.CertifiedProductPresenter;
import gov.healthit.chpl.scheduler.presenter.CertifiedProductXmlPresenter;
import gov.healthit.chpl.service.CertificationCriterionService;

@DisallowConcurrentExecution
public class CertifiedProductDownloadableResourceCreatorJob extends DownloadableResourceCreatorJob {
    private static final Logger LOGGER = LogManager.getLogger("certifiedProductDownloadableResourceCreatorJobLogger");
    private static final int MILLIS_PER_SECOND = 1000;
    private String edition;
    private File tempDirectory, tempCsvFile, tempXmlFile;
    private ExecutorService executorService;

    @Autowired
    private CertificationCriterionService criterionService;

    @Autowired
    private Environment env;

    public CertifiedProductDownloadableResourceCreatorJob() throws Exception {
        super(LOGGER);
        edition = "";
    }

    @Override
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        edition = jobContext.getMergedJobDataMap().getString("edition");

        LOGGER.info("********* Starting the Certified Product Downloadable Resource Creator job for {}. *********", edition);
        try (CertifiedProductXmlPresenter xmlPresenter = new CertifiedProductXmlPresenter();
                CertifiedProductCsvPresenter csvPresenter = getCsvPresenter()) {
            initializeTempFiles();
            if (tempCsvFile != null && tempXmlFile != null) {
                initializeWritingToFiles(xmlPresenter, csvPresenter);
                initializeExecutorService();

                List<CertifiedProductPresenter> presenters = new ArrayList<CertifiedProductPresenter>(
                        Arrays.asList(xmlPresenter, csvPresenter));
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
            LOGGER.info("********* Completed the Certified Product Downloadable Resource Creator job for {}. *********", edition);
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

    private void initializeWritingToFiles(CertifiedProductXmlPresenter xmlPresenter, CertifiedProductCsvPresenter csvPresenter)
            throws IOException {
        xmlPresenter.setLogger(LOGGER);
        xmlPresenter.open(tempXmlFile);

        csvPresenter.setLogger(LOGGER);
        List<CertificationCriterion> criteria = getCriteriaDao().findByCertificationEditionYear(edition)
                .stream()
                .filter(cr -> !cr.getRemoved())
                .sorted((crA, crB) -> criterionService.sortCriteria(crA, crB))
                .collect(Collectors.<CertificationCriterion>toList());
        csvPresenter.setApplicableCriteria(criteria);
        csvPresenter.open(tempCsvFile);
    }

    private List<CertifiedProductDetailsDTO> getRelevantListings() throws EntityRetrievalException {
        LOGGER.info("Finding all listings for edition " + edition + ".");
        Date start = new Date();
        List<CertifiedProductDetailsDTO> listingsForEdition = getCertifiedProductDao().findByEdition(edition);
        Date end = new Date();
        LOGGER.info("Found the " + listingsForEdition.size() + " listings from " + edition + " in "
                + ((end.getTime() - start.getTime()) / MILLIS_PER_SECOND) + " seconds");
        return listingsForEdition;
    }

    private CertifiedProductCsvPresenter getCsvPresenter() {
        CertifiedProductCsvPresenter presenter = null;
        if (edition.equals("2014")) {
            presenter = new CertifiedProduct2014CsvPresenter();
        } else {
            presenter = new CertifiedProductCsvPresenter();
        }
        return presenter;
    }

    private void initializeTempFiles() throws IOException {
        File downloadFolder = getDownloadFolder();
        Path tempDirBasePath = Paths.get(downloadFolder.getAbsolutePath());
        Path tempDir = Files.createTempDirectory(tempDirBasePath, TEMP_DIR_NAME);
        this.tempDirectory = tempDir.toFile();

        Path csvPath = Files.createTempFile(tempDir, "chpl-" + edition, ".csv");
        tempCsvFile = csvPath.toFile();

        Path xmlPath = Files.createTempFile(tempDir, "chpl-" + edition, ".xml");
        tempXmlFile = xmlPath.toFile();
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

        if (tempXmlFile != null) {
            String xmlFilename = getFileName(downloadFolder.getAbsolutePath(),
                    getFilenameTimestampFormat().format(new Date()), "xml");
            LOGGER.info("Moving " + tempXmlFile.getAbsolutePath() + " to " + tempXmlFile);
            Path targetFile = Files.move(tempXmlFile.toPath(), Paths.get(xmlFilename), StandardCopyOption.ATOMIC_MOVE);
            if (targetFile == null) {
                LOGGER.warn("XML file move may not have succeeded. Check file system.");
            }
        } else {
            LOGGER.warn("Temp XML File was null and could not be moved.");
        }
    }

    private void cleanupTempFiles() {
        LOGGER.info("Deleting temporary files.");
        if (tempCsvFile != null && tempCsvFile.exists()) {
            tempCsvFile.delete();
        } else {
            LOGGER.warn("Temp CSV File was null and could not be deleted.");
        }

        if (tempXmlFile != null && tempXmlFile.exists()) {
            tempXmlFile.delete();
        } else {
            LOGGER.warn("Temp XML File was null and could not be deleted.");
        }

        if (tempDirectory != null && tempDirectory.exists()) {
            tempDirectory.delete();
        } else {
            LOGGER.warn("Temp directory for download files was null and could not be deleted.");
        }
    }

    private String getFileName(String path, String timeStamp, String extension) {
        return path + File.separator + "chpl-" + edition + "-" + timeStamp + "." + extension;
    }

    private Integer getThreadCountForJob() throws NumberFormatException {
        return Integer.parseInt(env.getProperty("executorThreadCountForQuartzJobs"));
    }

    private void initializeExecutorService() {
        executorService = Executors.newFixedThreadPool(getThreadCountForJob());
    }
}

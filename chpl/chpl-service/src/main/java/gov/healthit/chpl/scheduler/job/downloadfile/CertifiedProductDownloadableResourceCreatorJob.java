package gov.healthit.chpl.scheduler.job.downloadfile;

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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.concept.CertificationEditionConcept;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.entity.CertificationStatusType;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.scheduler.presenter.CertifiedProduct2014CsvPresenter;
import gov.healthit.chpl.scheduler.presenter.CertifiedProductCsvPresenter;
import gov.healthit.chpl.scheduler.presenter.CertifiedProductJsonPresenter;
import gov.healthit.chpl.scheduler.presenter.CertifiedProductPresenter;
import gov.healthit.chpl.scheduler.presenter.CertifiedProductXmlPresenter;
import gov.healthit.chpl.util.Util;

@DisallowConcurrentExecution
public class CertifiedProductDownloadableResourceCreatorJob extends DownloadableResourceCreatorJob {
    private static final Logger LOGGER = LogManager.getLogger("certifiedProductDownloadableResourceCreatorJobLogger");
    private static final int MILLIS_PER_SECOND = 1000;

    private CertificationEditionConcept edition;
    private List<CertificationStatusType> certificationStatuses;
    private File tempDirectory, tempCsvFile, tempXmlFile, tempJsonFile;
    private ExecutorService executorService;

    @Autowired
    private Environment env;

    public CertifiedProductDownloadableResourceCreatorJob() throws Exception {
        super(LOGGER);
        edition = null;
    }

    @Override
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        initializeEdition(jobContext);
        initializeCertificationStatuses(jobContext);

        LOGGER.info("********* Starting the Certified Product Downloadable Resource Creator job for {}. *********", getDownloadFileType());
        try (CertifiedProductXmlPresenter xmlPresenter = new CertifiedProductXmlPresenter();
                CertifiedProductCsvPresenter csvPresenter = getCsvPresenter();
                CertifiedProductJsonPresenter jsonPresenter = new CertifiedProductJsonPresenter()) {
            initializeTempFiles();
            if (tempCsvFile != null && tempXmlFile != null && tempJsonFile != null) {
                initializeWritingToFiles(xmlPresenter, csvPresenter, jsonPresenter);
                initializeExecutorService();

                List<CertifiedProductPresenter> presenters = List.of(xmlPresenter, csvPresenter, jsonPresenter);
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

    private void initializeWritingToFiles(CertifiedProductXmlPresenter xmlPresenter, CertifiedProductCsvPresenter csvPresenter, CertifiedProductJsonPresenter jsonPresenter)
            throws IOException {
        initializeXmlFiles(xmlPresenter);
        initializeCsvFiles(csvPresenter);
        initializeJsonFiles(jsonPresenter);
    }

    private void initializeXmlFiles(CertifiedProductXmlPresenter xmlPresenter) throws IOException  {
        xmlPresenter.setLogger(LOGGER);
        xmlPresenter.open(tempXmlFile);
    }

    private void initializeCsvFiles(CertifiedProductCsvPresenter csvPresenter) throws IOException  {
        csvPresenter.setLogger(LOGGER);
        List<CertificationCriterion> criteria = new ArrayList<CertificationCriterion>();
        if (edition != null) {
            //include all criteria from that edition
            criteria = getCriteriaManager().getAll().stream()
                .filter(criterion -> criterion.getCertificationEdition() != null
                    && criterion.getCertificationEdition().equals(edition.getYear()))
                .sorted((crA, crB) -> getCriteriaService().sortCriteria(crA, crB))
                .collect(Collectors.<CertificationCriterion>toList());
        } else if (!CollectionUtils.isEmpty(certificationStatuses)) {
            //include all of today's active criteria
            criteria = getCriteriaManager().getActiveToday().stream()
                    .sorted((crA, crB) -> getCriteriaService().sortCriteria(crA, crB))
                    .collect(Collectors.<CertificationCriterion>toList());
        } else {
            LOGGER.warn("Either an edition or certification status(es) must be provided. No applicable criteria found for CSV file.");
        }
        csvPresenter.setApplicableCriteria(criteria);
        csvPresenter.open(tempCsvFile);
    }

    private void initializeJsonFiles(CertifiedProductJsonPresenter jsonPresenter) throws IOException  {
        jsonPresenter.setLogger(LOGGER);
        jsonPresenter.open(tempJsonFile);
    }

    private List<CertifiedProductDetailsDTO> getRelevantListings() throws EntityRetrievalException {
        List<CertifiedProductDetailsDTO> relevantListings = new ArrayList<CertifiedProductDetailsDTO>();
        if (edition != null) {
            LOGGER.info("Finding all listings for edition " + edition.getYear() + ".");
            Date start = new Date();
            relevantListings = getCertifiedProductDao().findByEdition(edition.getYear());
            Date end = new Date();
            LOGGER.info("Found the " + relevantListings.size() + " listings from " + edition.getYear() + " in "
                    + ((end.getTime() - start.getTime()) / MILLIS_PER_SECOND) + " seconds");
        } else if (!CollectionUtils.isEmpty(certificationStatuses)) {
            LOGGER.info("Finding all listings with status(es) "
                    + Util.joinListGrammatically(certificationStatuses.stream().map(status -> status.getName()).collect(Collectors.toList()))
                    + ".");
            Date start = new Date();
            relevantListings = getCertifiedProductDao().getListingsByStatus(certificationStatuses);
            Date end = new Date();
            LOGGER.info("Found the " + relevantListings.size() + " listings with status(es) "
                    + Util.joinListGrammatically(certificationStatuses.stream().map(status -> status.getName()).collect(Collectors.toList()))
                    + " in " + ((end.getTime() - start.getTime()) / MILLIS_PER_SECOND) + " seconds");
        } else {
            LOGGER.warn("Either an edition or certification status(es) must be provided. No relevant listings found.");
        }
        return relevantListings;
    }

    private CertifiedProductCsvPresenter getCsvPresenter() {
        CertifiedProductCsvPresenter presenter = null;
        if (edition.equals(CertificationEditionConcept.CERTIFICATION_EDITION_2014)) {
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
        Path jsonPath = Files.createTempFile(tempDir, "chpl-" + edition, ".json");
        tempJsonFile = jsonPath.toFile();
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

        if (tempJsonFile != null) {
            String jsonFilename = getFileName(downloadFolder.getAbsolutePath(),
                    getFilenameTimestampFormat().format(new Date()), "json");
            LOGGER.info("Moving " + tempJsonFile.getAbsolutePath() + " to " + tempJsonFile);
            Path targetFile = Files.move(tempJsonFile.toPath(), Paths.get(jsonFilename), StandardCopyOption.ATOMIC_MOVE);
            if (targetFile == null) {
                LOGGER.warn("JSON file move may not have succeeded. Check file system.");
            }
        } else {
            LOGGER.warn("Temp JSON File was null and could not be moved.");
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
        return path + File.separator + "chpl-" + edition + "-" + timeStamp + "." + extension;
    }

    private Integer getThreadCountForJob() throws NumberFormatException {
        return Integer.parseInt(env.getProperty("executorThreadCountForQuartzJobs"));
    }

    private void initializeExecutorService() {
        executorService = Executors.newFixedThreadPool(getThreadCountForJob());
    }

    private void initializeEdition(JobExecutionContext jobContext) {
        String editionYear = jobContext.getMergedJobDataMap().getString("edition");
        if (!StringUtils.isEmpty(editionYear)) {
            edition = CertificationEditionConcept.getByYear(editionYear);
        }
    }

    private void initializeCertificationStatuses(JobExecutionContext jobContext) {
        String certificationStatusesDelimited = jobContext.getMergedJobDataMap().getString("certificationStatuses");
        if (!StringUtils.isEmpty(certificationStatusesDelimited)) {
            String[] certificationStatusesSplit = certificationStatusesDelimited.split(",");
            if (certificationStatusesSplit != null && certificationStatusesSplit.length > 0) {
                Stream.of(certificationStatusesSplit)
                    .forEach(status -> {
                        CertificationStatusType certificationStatus = CertificationStatusType.getValue(status);
                        if (certificationStatus == null) {
                            LOGGER.warn("No matching certification status found for " + status);
                        } else {
                            certificationStatuses.add(certificationStatus);
                        }
                    });
            }
        }
    }

    private String getDownloadFileType() {
        String type = "";
        if (edition != null) {
            type += " edition " + edition.getYear();
        }
        if (!CollectionUtils.isEmpty(certificationStatuses)) {
            type += " certification statuses "
                    + Util.joinListGrammatically(certificationStatuses.stream()
                                .map(status -> status.getName())
                                .collect(Collectors.toList()));
        }

        if (StringUtils.isEmpty(type)) {
            type = "?";
        }
        return type;
    }
}

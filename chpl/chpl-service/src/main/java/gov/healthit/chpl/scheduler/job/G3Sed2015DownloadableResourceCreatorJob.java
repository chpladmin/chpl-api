package gov.healthit.chpl.scheduler.job;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
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

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.CertifiedProductDetailsManager;
import gov.healthit.chpl.scheduler.presenter.Sed2015CsvPresenter;
import gov.healthit.chpl.service.CertificationCriterionService;

@DisallowConcurrentExecution
public class G3Sed2015DownloadableResourceCreatorJob extends DownloadableResourceCreatorJob {
    private static final Logger LOGGER = LogManager.getLogger("g3Sed2015DownloadableResourceCreatorJobLogger");
    private static final String CRITERIA_NAME = "170.315 (g)(3)";
    private static final String TITLE = "Safety-Enhanced Design";
    private static final int MILLIS_PER_SECOND = 1000;
    private static final int SECONDS_PER_MINUTE = 60;

    @Autowired
    private CertificationCriterionService criterionService;

    @Autowired
    private Environment env;

    @Autowired
    private CertifiedProductDetailsManager certifiedProductDetailsManager;

    private ExecutorService executorService;

    public G3Sed2015DownloadableResourceCreatorJob() throws Exception {
        super(LOGGER);
    }

    @Override
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

        Date start = new Date();
        LOGGER.info("********* Starting the G3 SED 2015 Downloadable Resource Creator job. *********");
        try {
            initializeExecutorService();

            List<CompletableFuture<Optional<CertifiedProductSearchDetails>>> futureOptionals = getCertifiedProductSearchDetails(
                    getRelevantListingIds());

            List<Optional<CertifiedProductSearchDetails>> optionals = futureOptionals.stream()
                    .map(fo -> get(fo))
                    .collect(Collectors.toList());

            List<CertifiedProductSearchDetails> orderedListings = optionals.stream()
                    .filter(o -> o.isPresent())
                    .map(o -> o.get())
                    .sorted(Comparator.comparing(CertifiedProductSearchDetails::getId))
                    .collect(Collectors.toList());

            File downloadFolder = getDownloadFolder();
            writeToFile(downloadFolder, orderedListings);
        } catch (Exception e) {
            LOGGER.error(e);
        }
        Date end = new Date();
        LOGGER.info("Time to create download file for G3 SED: {} seconds, or {} minutes",
                (end.getTime() - start.getTime()) / MILLIS_PER_SECOND,
                (end.getTime() - start.getTime()) / MILLIS_PER_SECOND / SECONDS_PER_MINUTE);
        LOGGER.info("********* Completed the G3 SED 2015 Downloadable Resource Creator job. *********");
    }

    private Optional<CertifiedProductSearchDetails> get(CompletableFuture<Optional<CertifiedProductSearchDetails>> future) {
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return Optional.empty();
        }
    }

    private List<CompletableFuture<Optional<CertifiedProductSearchDetails>>> getCertifiedProductSearchDetails(
            List<Long> listingIds) throws Exception {

        List<CompletableFuture<Optional<CertifiedProductSearchDetails>>> futures = new ArrayList<CompletableFuture<Optional<CertifiedProductSearchDetails>>>();
        for (Long currListingId : listingIds) {
            futures.add(
                    CompletableFuture.supplyAsync(() -> getCertifiedProductDetails(currListingId), executorService));
        }
        return futures;
    }

    private Optional<CertifiedProductSearchDetails> getCertifiedProductDetails(Long id) {
        try {
            return Optional.of(certifiedProductDetailsManager.getCertifiedProductDetails(id));
        } catch (EntityRetrievalException e) {
            LOGGER.error("Could not retrieve certified product details for id: " + id, e);
            return Optional.empty();
        }
    }

    private List<Long> getRelevantListingIds() throws EntityRetrievalException {
        LOGGER.info("Finding all listings attesting to " + CRITERIA_NAME + ".");
        CertificationCriterionDTO certCrit = getCriteriaDao().getByNumberAndTitle(CRITERIA_NAME, TITLE);
        List<Long> listingIds = getCertificationResultDao().getCpIdsByCriterionId(certCrit.getId());
        LOGGER.info("Found " + listingIds.size() + " listings attesting to " + CRITERIA_NAME + ".");
        return listingIds;
    }

    private void writeToFile(File downloadFolder, List<CertifiedProductSearchDetails> results) throws IOException {
        String csvFilename = downloadFolder.getAbsolutePath() + File.separator + "chpl-sed-all-details-"
                + getFilenameTimestampFormat().format(new Date()) + ".csv";
        File csvFile = getFile(csvFilename);
        Sed2015CsvPresenter csvPresenter = new Sed2015CsvPresenter();
        csvPresenter.presentAsFile(csvFile, results, criterionService);
        LOGGER.info("Wrote G3 SED 2015 CSV file.");
    }

    private File getFile(String fileName) throws IOException {
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

    private Integer getThreadCountForJob() throws NumberFormatException {
        return Integer.parseInt(env.getProperty("executorThreadCountForQuartzJobs"));
    }

    private void initializeExecutorService() {
        executorService = Executors.newFixedThreadPool(getThreadCountForJob());
    }
}

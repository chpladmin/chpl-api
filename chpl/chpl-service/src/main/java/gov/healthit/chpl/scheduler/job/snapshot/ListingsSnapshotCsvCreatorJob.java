package gov.healthit.chpl.scheduler.job.snapshot;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.activity.history.ListingActivityUtil;
import gov.healthit.chpl.activity.history.explorer.ListingOnDateActivityExplorer;
import gov.healthit.chpl.activity.history.query.ListingOnDateActivityQuery;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.email.ChplEmailFactory;
import gov.healthit.chpl.email.ChplHtmlEmailBuilder;
import gov.healthit.chpl.exception.EmailNotSentException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.scheduler.job.DownloadableResourceCreatorJob;
import gov.healthit.chpl.scheduler.presenter.CertifiedProductCsvPresenter;
import gov.healthit.chpl.scheduler.presenter.CertifiedProductPresenter;
import gov.healthit.chpl.service.CertificationCriterionService;

@DisallowConcurrentExecution
public class ListingsSnapshotCsvCreatorJob extends DownloadableResourceCreatorJob {
    private static final Logger LOGGER = LogManager.getLogger("listingsSnapshotCsvCreatorJobLogger");

    private String edition;
    private LocalDate snapshotDate;
    private ExecutorService executorService;

    @Autowired
    private CertificationCriterionService criterionService;

    @Autowired
    private ListingActivityUtil listingActivityUtil;

    @Autowired
    private ListingOnDateActivityExplorer activityExplorer;

    @Autowired
    private ChplHtmlEmailBuilder chplHtmlEmailBuilder;

    @Autowired
    private Environment env;

    @Autowired
    private ChplEmailFactory chplEmailFactory;

    public ListingsSnapshotCsvCreatorJob() throws Exception {
        super(LOGGER);
    }

    @Override
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        parseParametersFromContext(jobContext);
        if (StringUtils.isEmpty(edition) || snapshotDate == null) {
            LOGGER.info("Not running job due to missing 'edition' or 'snapshotDate' parameter.");
            return;
        }

        LOGGER.info("********* Starting the Listings Snapshot CSV Creator job for {} on {}. *********", edition, snapshotDate);
        try (CertifiedProductCsvPresenter csvPresenter = getCsvPresenter()) {
            File downloadFolder = getDownloadFolder();
            String csvFilename = downloadFolder.getAbsolutePath()
                    + File.separator
                    + "chpl-snapshot-" + this.edition + "-" + this.snapshotDate + ".csv";
            File csvFile = getFile(csvFilename);
            initializeWritingToFiles(csvPresenter, csvFile);

            initializeExecutorService();
            List<CertifiedProductPresenter> presenters = new ArrayList<CertifiedProductPresenter>(
                    Arrays.asList(csvPresenter));
            List<CompletableFuture<Void>> futures = getCertifiedProductSearchFutures(getRelevantListings(), presenters);
            CompletableFuture<Void> combinedFutures = CompletableFuture
                    .allOf(futures.toArray(new CompletableFuture[futures.size()]));

            // This is not blocking - presumably because the job executes using it's own ExecutorService
            // This is necessary so that the system can indicate that the job and it's threads are still running
            combinedFutures.get();
            sendEmail(jobContext, Stream.of(csvFile).collect(Collectors.toList()));
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            LOGGER.info("All processes have completed");
        }

        executorService.shutdown();
        LOGGER.info("********* Completed the Listings Snapshot CSV Creator job for {} on {}. *********", edition, snapshotDate);
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
        CertifiedProductSearchDetails listing = listingActivityUtil.getListing(json, true);
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

    private void initializeWritingToFiles(CertifiedProductCsvPresenter csvPresenter, File csvFile)
            throws IOException {
        csvPresenter.setLogger(LOGGER);
        List<CertificationCriterionDTO> criteria = getCriteriaDao().findByCertificationEditionYear(edition)
                .stream()
                .sorted((crA, crB) -> criterionService.sortCriteria(crA, crB))
                .collect(Collectors.<CertificationCriterionDTO>toList());
        csvPresenter.setApplicableCriteria(criteria);
        csvPresenter.open(csvFile);
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

    private void sendEmail(JobExecutionContext context, List<File> attachments) throws EmailNotSentException {
        String emailAddress = context.getMergedJobDataMap().getString(JOB_DATA_KEY_EMAIL);
        LOGGER.info("Sending email to: " + emailAddress);
        chplEmailFactory.emailBuilder()
                .recipient(emailAddress)
                .subject(env.getProperty("listingsSnapshot.subject"))
                .htmlMessage(createHtmlMessage())
                .fileAttachments(attachments)
                .sendEmail();
        LOGGER.info("Completed Sending email to: " + emailAddress);
    }

    private String createHtmlMessage() {
        return chplHtmlEmailBuilder.initialize()
                .heading("Listings Snapshot")
                .paragraph(null,
                        String.format(env.getProperty("listingsSnapshot.body"),
                                this.edition, this.snapshotDate.toString()))
                .footer(true)
                .build();
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

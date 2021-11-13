package gov.healthit.chpl.scheduler.job.snapshot;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.commons.lang3.BooleanUtils;
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
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.scheduler.job.DownloadableResourceCreatorJob;
import gov.healthit.chpl.scheduler.presenter.ListingsWithCriterionCSVPresenter;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.util.Util;

@DisallowConcurrentExecution
public class ListingsWithCriterionSnapshotCsvCreatorJob extends DownloadableResourceCreatorJob {
    private static final Logger LOGGER = LogManager.getLogger("listingsWithCriterionSnapshotCsvCreatorJobLogger");
    private static final String TEMP_DIR_NAME = "temp";

    private String edition;
    private CertificationCriterion criterion;
    private LocalDate snapshotDate;
    private File tempDirectory, tempCsvFile;
    private ExecutorService executorService;

    @Autowired
    private CertificationCriterionService criterionService;

    @Autowired
    private ListingActivityUtil listingActivityUtil;

    @Autowired
    private ListingOnDateActivityExplorer activityExplorer;

    @Autowired
    private Environment env;

    public ListingsWithCriterionSnapshotCsvCreatorJob() throws Exception {
        super(LOGGER);
    }

    @Override
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        parseParametersFromContext(jobContext);

        if (StringUtils.isEmpty(edition) || criterion == null || snapshotDate == null) {
            LOGGER.info("Not running job due to missing 'edition', 'criterionId', or 'snapshotDate' parameter.");
            return;
        }

        LOGGER.info("********* Starting the Listings With Criterion Snapshot CSV Creator job for {} on {}. *********", Util.formatCriteriaNumber(criterion), snapshotDate);
        try {
            ListingsWithCriterionCSVPresenter csvPresenter = getCsvPresenter();
            csvPresenter.setLogger(LOGGER);
            initializeExecutorService();

            List<CertifiedProductSearchDetails> listings = getListingDetails(getRelevantListings());
            File downloadFolder = getDownloadFolder();
            String csvFilename = downloadFolder.getAbsolutePath()
                    + File.separator
                    + "chpl-snapshot-" + this.snapshotDate + ".csv";
            File csvFile = getFile(csvFilename);
            csvPresenter.presentAsFile(csvFile, listings);
            LOGGER.info("Wrote listings snapshot CSV file.");
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            LOGGER.info("All processes have completed");
        }

        executorService.shutdown();
        LOGGER.info("********* Completed the Listing Snapshot Downloadable Resource Creator job for {} on {}. *********", Util.formatCriteriaNumber(criterion), snapshotDate);
    }

    private List<CertifiedProductSearchDetails> getListingDetails(List<Long> listingIds) {
        List<CertifiedProductSearchDetails> listings = new ArrayList<CertifiedProductSearchDetails>();
        for (Long listingId : listingIds) {
            Optional<CertifiedProductSearchDetails> listing = getCertifiedProductSearchDetails(listingId);
            if (listing.isPresent() && attestedToCriterion(listing.get())) {
                listings.add(listing.get());
            }
        }
        return listings;
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

        Optional<CertifiedProductSearchDetails> listingOnDateOpt = getDetailsFromJson(activityWithListingOnDay.getNewData());
        return listingOnDateOpt;
    }

    private Optional<CertifiedProductSearchDetails> getDetailsFromJson(String json) {
        CertifiedProductSearchDetails listing = listingActivityUtil.getListing(json, true);
        if (listing != null) {
            return Optional.of(listing);
        }
        return Optional.empty();
    }

    private boolean attestedToCriterion(CertifiedProductSearchDetails listing) {
        boolean result  = listing.getCertificationResults().stream()
            .filter(certResult -> certResult.getCriterion().getId().equals(criterion.getId())
                    && BooleanUtils.isTrue(certResult.isSuccess()))
            .findAny().isPresent();
        LOGGER.info("Listing attested to " + Util.formatCriteriaNumber(criterion) + ": " + result);
        return result;
    }

    private List<Long> getRelevantListings() throws EntityRetrievalException {
        List<Long> relevantListingIds = getCertifiedProductDao().findIdsByEditionAndCreatedBeforeDate(edition, snapshotDate);
        LOGGER.info("Found the " + relevantListingIds.size() + " listings from edition " + edition
                + " and created before " + snapshotDate);
        return relevantListingIds;
    }

    private ListingsWithCriterionCSVPresenter getCsvPresenter() {
       return new ListingsWithCriterionCSVPresenter(this.criterion);
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

    private void parseParametersFromContext(JobExecutionContext jobContext) {
        edition = jobContext.getMergedJobDataMap().getString("edition");
        Long criterionId = Long.parseLong(jobContext.getMergedJobDataMap().getString("criterionId"));
        this.criterion = criterionService.get(criterionId);

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

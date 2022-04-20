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
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.activity.history.ListingActivityUtil;
import gov.healthit.chpl.activity.history.explorer.CertificationResultContainsSvapActivityExplorer;
import gov.healthit.chpl.activity.history.explorer.SvapNoticeUrlLastUpdateActivityExplorer;
import gov.healthit.chpl.activity.history.query.CertificationResultContainsSvapActivityQuery;
import gov.healthit.chpl.activity.history.query.SvapNoticeUrlLastUpdateActivityQuery;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.scheduler.job.DownloadableResourceCreatorJob;
import gov.healthit.chpl.scheduler.presenter.SvapActivityPresenter;
import gov.healthit.chpl.svap.domain.Svap;
import gov.healthit.chpl.util.DateUtil;
import gov.healthit.chpl.util.Util;
import lombok.extern.log4j.Log4j2;

@DisallowConcurrentExecution
@Log4j2(topic = "svapActivityDownloadableResourceCreatorJobLogger")
public class SvapDownloadableResourceCreatorJob extends DownloadableResourceCreatorJob {
    private static final int MILLIS_PER_SECOND = 1000;
    private static final String TEMP_DIR_NAME = "temp";
    private File tempDirectory, tempCsvFile;
    private ExecutorService executorService;

    @Autowired
    private SvapNoticeUrlLastUpdateActivityExplorer svapNoticeUrlActivityExplorer;

    @Autowired
    private CertificationResultContainsSvapActivityExplorer certResultSvapActivityExplorer;

    @Autowired
    private CertifiedProductDAO cpDao;

    @Autowired
    private Environment env;

    @Autowired
    private ListingActivityUtil activityUtil;

    @Value("${svapReportName}")
    private String svapReportName;

    public SvapDownloadableResourceCreatorJob() throws Exception {
        super(LOGGER);
    }

    @Override
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

        LOGGER.info("********* Starting the SVAP Downloadable Resource Creator job. *********");
        try (SvapActivityPresenter csvPresenter = getCsvPresenter()) {
            initializeTempFile();
            if (tempCsvFile != null) {
                initializeWritingToFile(csvPresenter);
                initializeExecutorService();
                List<Long> listingIdsWithSvap = getRelevantListingIds();
                List<SvapActivityPresenter> presenters = Stream.of(csvPresenter).collect(Collectors.toList());
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
            List<SvapActivityPresenter> presenters) {

        List<CompletableFuture<Void>> futures = new ArrayList<CompletableFuture<Void>>();
        for (Long listingId : listingIds) {
            futures.add(CompletableFuture
                    .supplyAsync(() -> getListingSvapActivities(listingId), executorService)
                    .thenAccept(svapActivity -> svapActivity.ifPresent(sa -> addAllToPresenters(presenters, sa))));
        }
        return futures;
    }

    protected Optional<List<ListingSvapActivity>> getListingSvapActivities(Long listingId) {
        CertifiedProductSearchDetails listing = null;
        try {
            listing = getCertifiedProductDetailsManager().getCertifiedProductDetailsUsingCache(listingId);
        } catch (EntityRetrievalException e) {
            LOGGER.error(String.format("Could not retrieve listing: %s", listingId), e);
            return Optional.empty();
        }

        List<ListingSvapActivity> activities = buildSvapActivities(listing);
        return Optional.of(activities);
    }

    private List<ListingSvapActivity> buildSvapActivities(CertifiedProductSearchDetails listing) {
        SvapNoticeUrlLastUpdateActivityQuery svapNoticeUrlQuery = SvapNoticeUrlLastUpdateActivityQuery.builder()
                .listingId(listing.getId())
                .svapNoticeUrl(listing.getSvapNoticeUrl())
                .build();
        List<ActivityDTO> svapNoticeUrlLastUpdateActivities = svapNoticeUrlActivityExplorer.getActivities(svapNoticeUrlQuery);
        ListingSvapActivity baseSvapActivity = ListingSvapActivity.builder()
            .listing(listing)
            .svapNoticeLastUpdated(!CollectionUtils.isEmpty(svapNoticeUrlLastUpdateActivities)
                    ? DateUtil.toLocalDate(svapNoticeUrlLastUpdateActivities.get(0).getActivityDate().getTime()) : null)
            .build();
        List<ListingSvapActivity> listingSvapActivities = new ArrayList<ListingSvapActivity>();
        if (!hasCertificationResultSvapData(listing)) {
            listingSvapActivities.add(baseSvapActivity);
        } else {
            listingSvapActivities = listing.getCertificationResults().stream()
                .filter(certResult -> BooleanUtils.isTrue(certResult.isSuccess()))
                .filter(attestedCertResult -> attestedCertResult.getSvaps() != null && attestedCertResult.getSvaps().size() > 0)
                .map(attestedCertResult -> createCertificationResultSvapActivities(attestedCertResult, baseSvapActivity))
                .flatMap(List::stream)
                .collect(Collectors.toList());
        }

        listingSvapActivities.stream()
            .filter(listingSvapActivity -> listingSvapActivity.getCriterion() != null && listingSvapActivity.getCriterionSvap() != null)
            .forEach(listingSvapActivity -> updateCertificationResultActivityData(listing, listingSvapActivity));
        return listingSvapActivities;
    }

    private boolean hasCertificationResultSvapData(CertifiedProductSearchDetails listing) {
        return listing.getCertificationResults().stream()
                .filter(certResult -> BooleanUtils.isTrue(certResult.isSuccess()))
                .filter(attestedCertResult -> attestedCertResult.getSvaps() != null && attestedCertResult.getSvaps().size() > 0)
                .count() > 0;
    }

    private List<ListingSvapActivity> createCertificationResultSvapActivities(CertificationResult certResult, ListingSvapActivity baseSvapActivity) {
        return certResult.getSvaps().stream()
            .map(certResultSvap -> ListingSvapActivity.builder()
                    .listing(baseSvapActivity.getListing())
                    .svapNoticeLastUpdated(baseSvapActivity.getSvapNoticeLastUpdated())
                    .criterion(certResult.getCriterion())
                    .criterionSvap(Svap.builder()
                            .approvedStandardVersion(certResultSvap.getApprovedStandardVersion())
                            .regulatoryTextCitation(certResultSvap.getRegulatoryTextCitation())
                            .replaced(certResultSvap.getReplaced())
                            .svapId(certResultSvap.getSvapId())
                            .build())
                    .build())
            .collect(Collectors.toList());
    }

    private void updateCertificationResultActivityData(CertifiedProductSearchDetails listing, ListingSvapActivity listingSvapActivity) {
        CertificationResultContainsSvapActivityQuery query = CertificationResultContainsSvapActivityQuery.builder()
                .listingId(listing.getId())
                .criterion(listingSvapActivity.getCriterion())
                .svap(listingSvapActivity.getCriterionSvap())
                .build();
        List<ActivityDTO> activities = certResultSvapActivityExplorer.getActivities(query);
        if (CollectionUtils.isEmpty(activities)) {
            LOGGER.warn("No activity was found where " + listingSvapActivity.getCriterionSvap().getRegulatoryTextCitation() + " was added to " + Util.formatCriteriaNumber(listingSvapActivity.getCriterion()) + " for listing ID " + listing.getId());
            return;
        }
        listingSvapActivity.setCriterionSvapLastUpdated(DateUtil.toLocalDate(activities.get(0).getActivityDate().getTime()));
        if (activities.get(0).getOriginalData() == null
                || !listingAttestsToCriteria(activityUtil.getListing(activities.get(0).getOriginalData()), listingSvapActivity.getCriterion())) {
            listingSvapActivity.setWasCriterionAttestedToBeforeSvapAdded(false);
        } else {
            listingSvapActivity.setWasCriterionAttestedToBeforeSvapAdded(true);
        }
    }

    private boolean listingAttestsToCriteria(CertifiedProductSearchDetails listing, CertificationCriterion criterion) {
        return listing.getCertificationResults().stream()
                .filter(certResult -> BooleanUtils.isTrue(certResult.isSuccess()) && certResult.getCriterion().getId().equals(criterion.getId()))
                .count() > 0;
    }

    private void addAllToPresenters(List<SvapActivityPresenter> presenters, List<ListingSvapActivity> svapActivity) {
        presenters.stream().forEach(p -> p.addAll(svapActivity));
    }

    private void initializeWritingToFile(SvapActivityPresenter csvPresenter)
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

    private SvapActivityPresenter getCsvPresenter() {
        SvapActivityPresenter presenter = new SvapActivityPresenter();
        return presenter;
    }

    private void initializeTempFile() throws IOException {
        File downloadFolder = getDownloadFolder();
        Path tempDirBasePath = Paths.get(downloadFolder.getAbsolutePath());
        Path tempDir = Files.createTempDirectory(tempDirBasePath, TEMP_DIR_NAME);
        this.tempDirectory = tempDir.toFile();

        Path csvPath = Files.createTempFile(tempDir, svapReportName, ".csv");
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
        return path + File.separator + svapReportName + "-" + timeStamp + "." + extension;
    }

    private Integer getThreadCountForJob() throws NumberFormatException {
        return Integer.parseInt(env.getProperty("executorThreadCountForQuartzJobs"));
    }

    private void initializeExecutorService() {
        executorService = Executors.newFixedThreadPool(getThreadCountForJob());
    }
}

package gov.healthit.chpl.scheduler.job;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.auth.user.SystemUsers;
import gov.healthit.chpl.changerequest.dao.ChangeRequestDAO;
import gov.healthit.chpl.changerequest.dao.ChangeRequestStatusTypeDAO;
import gov.healthit.chpl.changerequest.domain.ChangeRequestStatusType;
import gov.healthit.chpl.changerequest.manager.ChangeRequestManager;
import gov.healthit.chpl.changerequest.search.ChangeRequestSearchRequest;
import gov.healthit.chpl.changerequest.search.ChangeRequestSearchResponse;
import gov.healthit.chpl.changerequest.search.ChangeRequestSearchResult;
import gov.healthit.chpl.changerequest.search.ChangeRequestSearchService;
import gov.healthit.chpl.changerequest.search.OrderByOption;
import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.email.ChplEmailFactory;
import gov.healthit.chpl.email.ChplHtmlEmailBuilder;
import gov.healthit.chpl.email.footer.AdminFooter;
import gov.healthit.chpl.exception.EmailNotSentException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.SchedulerManager;
import gov.healthit.chpl.scheduler.job.changerequest.presenter.ChangeRequestCsvPresenter;
import gov.healthit.chpl.scheduler.job.changerequest.presenter.ChangeRequestDetailsPresentationService;
import gov.healthit.chpl.scheduler.job.changerequest.presenter.PendingChangeRequestPresenter;

/**
 * The PendingChangeRequestEmailJob implements a Quartz job and is available to ROLE_ADMIN and ROLE_ONC. When invoked it
 * emails configured individuals with the Change Requests that are in a pending state.
 */
public class PendingChangeRequestEmailJob extends QuartzJob {
    private static final Logger LOGGER = LogManager.getLogger("pendingChangeRequestEmailJobLogger");

    private File tempDirectory, tempFile;
    private ChangeRequestDetailsPresentationService crPresentationService;

    @Autowired
    private CertificationBodyDAO certificationBodyDAO;

    @Autowired
    private ChangeRequestDAO changeRequestDao;

    @Autowired
    private ChangeRequestStatusTypeDAO changeRequestStatusTypeDao;

    @Autowired
    private ChangeRequestSearchService changeRequestSearchService;

    @Autowired
    private ChangeRequestManager changeRequestManager;

    @Autowired
    private ChplHtmlEmailBuilder chplHtmlEmailBuilder;

    @Autowired
    private ChplEmailFactory chplEmailFactory;

    @Value("${executorThreadCountForQuartzJobs}")
    private String executorThreadCountForQuartzJobs;

    @Value("${pendingChangeRequestReportFilename}")
    private String reportFilename;

    @Value("${pendingChangeRequestEmailSubject}")
    private String pendingChangeRequestEmailSubject;

    @Value("${pendingChangeRequestHasDataEmailBody}")
    private String pendingChangeRequestHasDataEmailBody;

    @Value("${pendingChangeRequestNoDataEmailBody}")
    private String pendingChangeRequestNoDataEmailBody;

    public PendingChangeRequestEmailJob() throws Exception {
        super();
    }

    @Override
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the Pending Change Request job. *********");
        LOGGER.info("Creating pending change request email for: " + jobContext.getMergedJobDataMap().getString("email"));

        List<ChangeRequestSearchResult> searchResults = null;
        List<CertificationBody> acbs = null;
        try {
            setSecurityContext();
            acbs = getAppropriateAcbs(jobContext);
            searchResults = getPendingChangeRequestSearchResults(getSearchRequest(acbs));
        } catch (ValidationException ex) {
            LOGGER.catching(ex);
        }

        LOGGER.info("Found " + searchResults.size() + " pending change requests.");
        List<String> acbNames = acbs.stream().map(acb -> acb.getName()).collect(Collectors.toList());
        try (ChangeRequestCsvPresenter pendingCrPresenter = new PendingChangeRequestPresenter(LOGGER, acbNames);) {
            initializeTempFiles();
            pendingCrPresenter.open(tempFile);

            Integer threadCount = 1;
            try {
                threadCount = Integer.parseInt(executorThreadCountForQuartzJobs);
            } catch (NumberFormatException ex) {
                LOGGER.error("Could not initialize thread count from '" + executorThreadCountForQuartzJobs + "'. Defaulting to 1.");
            }
            crPresentationService = new ChangeRequestDetailsPresentationService(changeRequestManager, threadCount, LOGGER);
            crPresentationService.present(searchResults,
                    Stream.of(pendingCrPresenter).toList());

        } catch (Exception e) {
            LOGGER.catching(e);
        }

        try {
            sendEmail(jobContext, searchResults);
        } catch (Exception ex) {
            LOGGER.error("Unable to send email.", ex);
        }

        LOGGER.info("********* Completed the Pending Change Request Email job. *********");
    }

    private List<CertificationBody> getAppropriateAcbs(JobExecutionContext jobContext) {
        List<CertificationBody> acbs = certificationBodyDAO.findAllActive();
        if (jobContext.getMergedJobDataMap().getBooleanValue("acbSpecific")) {
            List<Long> acbsFromJob = getAcbsFromJobContext(jobContext);
            acbs = acbs.stream()
                    .filter(acb -> acbsFromJob.contains(acb.getId()))
                    .collect(Collectors.toList());
        }
        return acbs;
    }

    private List<Long> getAcbsFromJobContext(JobExecutionContext jobContext) {
        return Arrays.asList(jobContext.getMergedJobDataMap().getString("acb").split(SchedulerManager.DATA_DELIMITER)).stream()
                .map(acbIdAsString -> Long.parseLong(acbIdAsString))
                .collect(Collectors.toList());
    }

    private ChangeRequestSearchRequest getSearchRequest(List<CertificationBody> acbs) {
        List<ChangeRequestStatusType> statusTypes = changeRequestStatusTypeDao.getChangeRequestStatusTypes();
        List<Long> updatableStatusIds = changeRequestDao.getUpdatableStatusIds();
        Set<String> updatableStatusTypeNames = statusTypes.stream()
                .filter(statusType -> updatableStatusIds.contains(statusType.getId()))
                .map(statusType -> statusType.getName())
                .collect(Collectors.toSet());
        return ChangeRequestSearchRequest.builder()
                .currentStatusNames(updatableStatusTypeNames)
                .acbIds(acbs.stream().map(acb -> acb.getId()).collect(Collectors.toSet()))
                .orderBy(OrderByOption.SUBMITTED_DATE_TIME)
                .sortDescending(Boolean.TRUE)
                .build();
    }

    private List<ChangeRequestSearchResult> getPendingChangeRequestSearchResults(ChangeRequestSearchRequest searchRequest)
        throws ValidationException {
        LOGGER.info("Getting all change requests...");
        List<ChangeRequestSearchResult> searchResults = new ArrayList<ChangeRequestSearchResult>();
        LOGGER.info(searchRequest.toString());
        ChangeRequestSearchResponse searchResponse = changeRequestSearchService.searchChangeRequests(searchRequest);
        searchResults.addAll(searchResponse.getResults());
        while (searchResponse.getRecordCount() > searchResults.size()) {
            searchRequest.setPageSize(searchResponse.getPageSize());
            searchRequest.setPageNumber(searchResponse.getPageNumber() + 1);
            LOGGER.info(searchRequest.toString());
            searchResponse = changeRequestSearchService.searchChangeRequests(searchRequest);
            searchResults.addAll(searchResponse.getResults());
        }
        LOGGER.info("Got " + searchResults.size() + " total change requests.");
        return searchResults;
    }

    private void sendEmail(JobExecutionContext jobContext, List<ChangeRequestSearchResult> searchResults)
            throws EmailNotSentException {
        LOGGER.info("Sending email to {} with a total of {} pending change requests",
                getEmailRecipients(jobContext).get(0), searchResults.size());

        chplEmailFactory.emailBuilder()
                .recipients(getEmailRecipients(jobContext))
                .subject(pendingChangeRequestEmailSubject)
                .htmlMessage(createHtmlMessage(searchResults.size(), getAcbNamesAsCommaSeparatedList(jobContext)))
                .fileAttachments(Stream.of(tempFile).toList())
                .sendEmail();
    }

    private String createHtmlMessage(Integer rowCount, String acbList) {
        String body = "";
        if (rowCount > 0) {
            body = String.format(pendingChangeRequestHasDataEmailBody, acbList, rowCount);
        } else {
            body = String.format(pendingChangeRequestNoDataEmailBody, acbList);
        }

        return chplHtmlEmailBuilder.initialize()
                .heading(pendingChangeRequestEmailSubject)
                .paragraph("", body)
                .footer(AdminFooter.class)
                .build();
    }

    private List<String> getEmailRecipients(JobExecutionContext jobContext) {
        return Arrays.asList(jobContext.getMergedJobDataMap().getString("email"));
    }

    private String getAcbNamesAsCommaSeparatedList(JobExecutionContext jobContext) {
        if (Objects.nonNull(jobContext.getMergedJobDataMap().getString("acb"))) {
            return Arrays.asList(
                    jobContext.getMergedJobDataMap().getString("acb").split(SchedulerManager.DATA_DELIMITER)).stream()
                    .map(acbId -> {
                        try {
                            return certificationBodyDAO.getById(Long.parseLong(acbId)).getName();
                        } catch (NumberFormatException | EntityRetrievalException e) {
                            LOGGER.error("Could not retreive ACB name based on value: " + acbId, e);
                            return "";
                        }
                    })
                    .collect(Collectors.joining(", "));
        } else {
            return "";
        }
    }

    private void initializeTempFiles() throws IOException {
        File downloadFolder = getDownloadFolder();
        Path tempDirBasePath = Paths.get(downloadFolder.getAbsolutePath());
        Path tempDir = Files.createTempDirectory(tempDirBasePath, TEMP_DIR_NAME);

        this.tempDirectory = tempDir.toFile();
        this.tempDirectory.deleteOnExit();

        Path tempFilePath = Files.createTempFile(tempDir, reportFilename, ".csv");
        tempFile = tempFilePath.toFile();
    }

    private void setSecurityContext() {
        JWTAuthenticatedUser adminUser = new JWTAuthenticatedUser();
        adminUser.setFullName("Administrator");
        adminUser.setId(SystemUsers.ADMIN_USER_ID);
        adminUser.setFriendlyName("Admin");
        adminUser.setSubjectName("admin");
        adminUser.getPermissions().add(new GrantedPermission("ROLE_ADMIN"));

        SecurityContextHolder.getContext().setAuthentication(adminUser);
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
    }
}

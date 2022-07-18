package gov.healthit.chpl.scheduler.job.changerequest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.changerequest.dao.ChangeRequestStatusTypeDAO;
import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.changerequest.domain.ChangeRequestStatusType;
import gov.healthit.chpl.changerequest.manager.ChangeRequestManager;
import gov.healthit.chpl.changerequest.search.ChangeRequestSearchManager;
import gov.healthit.chpl.changerequest.search.ChangeRequestSearchRequest;
import gov.healthit.chpl.changerequest.search.ChangeRequestSearchResponse;
import gov.healthit.chpl.changerequest.search.ChangeRequestSearchResult;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.email.ChplEmailFactory;
import gov.healthit.chpl.email.ChplHtmlEmailBuilder;
import gov.healthit.chpl.exception.EmailNotSentException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.SchedulerManager;
import gov.healthit.chpl.scheduler.job.QuartzJob;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "changeRequestReportJobLogger")
public class ChangeRequestReportEmailJob  extends QuartzJob {
    public static final String JOB_NAME = "Change Request Report";
    public static final String SEARCH_REQUEST = "searchRequest";
    public static final String USER_KEY = "user";

    private File tempDirectory, tempCsvFile;
    private ExecutorService executorService;

    @Autowired
    private ChangeRequestManager changeRequestManager;

    @Autowired
    private ChangeRequestSearchManager changeRequestSearchManager;

    @Autowired
    private ChangeRequestStatusTypeDAO changeRequestStatusTypeDao;

    @Autowired
    private DeveloperDAO developerDao;

    @Autowired
    private ChplHtmlEmailBuilder chplHtmlEmailBuilder;

    @Autowired
    private ChplEmailFactory chplEmailFactory;

    @Autowired
    private Environment env;

    @Value("${changeRequests.report.filename}")
    private String changeRequestsReportFilename;

    @Value("${changeRequests.report.subject}")
    private String changeRequestsReportMessageSubject;

    @Value("${changeRequests.report.heading}")
    private String changeRequestsReportMessageHeading;

    @Value("${changeRequests.report.paragraph1}")
    private String changeRequestsReportMessageBody;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the Change Request Report Email job *********");
        JobDataMap jobDataMap = context.getMergedJobDataMap();
        UserDTO user = (UserDTO) jobDataMap.get(USER_KEY);
        ChangeRequestSearchRequest searchRequest = (ChangeRequestSearchRequest) jobDataMap.get(SEARCH_REQUEST);
        String email = (String) jobDataMap.get(JOB_DATA_KEY_EMAIL);
        List<Long> acbIds = getAcbsFromJobContext(context);
        List<ChangeRequestSearchResult> searchResults = null;

        if (!StringUtils.isEmpty(email) && !CollectionUtils.isEmpty(acbIds)) {
            LOGGER.info("No user specified. Job will send email to " + email
                    + " with change requests for ACBs " + acbIds.stream().map(acbId -> acbId.toString()).collect(Collectors.joining(",")));
            searchRequest = getDefaultSearchRequest();
            searchRequest.setAcbIds(acbIds.stream().collect(Collectors.toSet()));
        } else if (user != null) {
            LOGGER.info("Change Requests matching the search request will be sent to " + user.getEmail());
            if (searchRequest == null) {
                searchRequest = getDefaultSearchRequest();
            }
        } else {
            LOGGER.fatal("The provided job context did not have enough information to run the job. "
                    + "An email and ACBs must be provided OR a User object.");
        }

        try {
            setSecurityContext(user);
            searchResults = getAllChangeRequestSearchResults(searchRequest);
        } catch (ValidationException ex) {
            LOGGER.catching(ex);
        }

        if (!CollectionUtils.isEmpty(searchResults)) {
            LOGGER.info("Found " + searchResults.size() + " change requests matching the search parameters for the job.");
            //get all change request details for the search results and stream them to a file
            initializeExecutorService();
            try (ChangeRequestPresenter crPresenter = new ChangeRequestPresenter(LOGGER)) {
                initializeTempFile();
                crPresenter.open(tempCsvFile);
                List<CompletableFuture<Void>> crFutures = getAllChangeRequestFutures(searchResults, crPresenter);
                CompletableFuture<Void> combinedFutures = CompletableFuture
                        .allOf(crFutures.toArray(new CompletableFuture[crFutures.size()]));

                //TODO: is the .get() still necessary?
                // This is not blocking - presumably because the job executes using it's own ExecutorService
                // This is necessary so that the system can indicate that the job and it's threads are still running
                combinedFutures.get();

                //send the email with the file attached
                sendEmail(context, searchRequest);
            } catch (Exception e) {
                LOGGER.catching(e);
            }

            try {
                //has to happen in a separate try block because of the presenter
                //using auto-close - can't delete the file until it is closed by the presenter
                cleanupTempFiles();
            } catch (Exception ex) {
                LOGGER.error(ex.getMessage(), ex);
            } finally {
                executorService.shutdown();
            }
        } else {
            LOGGER.info("Found 0 change requests matching the search parameters for the job.");
        }
        LOGGER.info("********* Completed the Change Request Report Email job *********");
    }

    private ChangeRequestSearchRequest getDefaultSearchRequest() {
        List<ChangeRequestStatusType> statusTypes = changeRequestStatusTypeDao.getChangeRequestStatusTypes();
        Set<String> pendingStatusTypeNames = statusTypes.stream()
                .map(statusType -> statusType.getName())
                .filter(statusTypeName -> statusTypeName.toLowerCase().contains("pending"))
                .collect(Collectors.toSet());
        return ChangeRequestSearchRequest.builder()
                .currentStatusNames(pendingStatusTypeNames)
                .build();
    }
    private List<ChangeRequestSearchResult> getAllChangeRequestSearchResults(ChangeRequestSearchRequest searchRequest)
        throws ValidationException {
        LOGGER.info("Getting all change requests...");
        List<ChangeRequestSearchResult> searchResults = new ArrayList<ChangeRequestSearchResult>();
        ChangeRequestSearchResponse searchResponse = changeRequestSearchManager.searchChangeRequests(searchRequest);
        searchResults.addAll(searchResponse.getResults());
        while (searchResponse.getRecordCount() > searchResponse.getResults().size()) {
            searchRequest.setPageSize(searchResponse.getPageSize());
            searchRequest.setPageNumber(searchResponse.getPageNumber() + 1);
            searchResponse = changeRequestSearchManager.searchChangeRequests(searchRequest);
            searchResults.addAll(searchResponse.getResults());
        }
        LOGGER.info("Got " + searchResults.size() + " total change requests.");
        return searchResults;
    }

    private List<CompletableFuture<Void>> getAllChangeRequestFutures(List<ChangeRequestSearchResult> changeRequests,
            ChangeRequestPresenter crPresenter) {
        List<CompletableFuture<Void>> futures = new ArrayList<CompletableFuture<Void>>();
        for (ChangeRequestSearchResult changeRequest : changeRequests) {
            futures.add(CompletableFuture
                    .supplyAsync(() -> getChangeRequestDetails(changeRequest.getId()), executorService)
                    .thenAccept(crDetails -> crDetails.ifPresent(cr -> addToCsvFile(crPresenter, cr))));
        }
        return futures;
    }

    private void addToCsvFile(ChangeRequestPresenter crPresenter, ChangeRequest changeRequest) {
        try {
            crPresenter.add(changeRequest);
        } catch (IOException e) {
            LOGGER.error(String.format("Could not write change request to CSV file: %s", changeRequest.getId()), e);
        }
    }

    private Optional<ChangeRequest> getChangeRequestDetails(Long changeRequestId) {
        try {
            return Optional.of(changeRequestManager.getChangeRequest(changeRequestId));
        } catch (EntityRetrievalException e) {
            LOGGER.error(String.format("Could not retrieve changeRequest: %s", changeRequestId), e);
            return Optional.empty();
        }
    }

    private void sendEmail(JobExecutionContext context, ChangeRequestSearchRequest searchRequest) throws EmailNotSentException, IOException {
        String email = context.getMergedJobDataMap().getString(JOB_DATA_KEY_EMAIL);
        LOGGER.info("Sending email to: " + email);
        chplEmailFactory.emailBuilder()
                .recipient(email)
                .subject(changeRequestsReportMessageSubject)
                .htmlMessage(createHtmlMessage(context, searchRequest))
                .fileAttachments(Arrays.asList(tempCsvFile))
                .sendEmail();
        LOGGER.info("Completed Sending email to: " + email);
    }

    private String createHtmlMessage(JobExecutionContext context, ChangeRequestSearchRequest searchRequest) {
        return chplHtmlEmailBuilder.initialize()
                .heading(changeRequestsReportMessageHeading)
                .paragraph("",
                        String.format(changeRequestsReportMessageBody, getSearchRequestAsHtml(searchRequest)))
                .footer(true)
                .build();
    }

    private String getSearchRequestAsHtml(ChangeRequestSearchRequest searchRequest) {
        if (searchRequest == null) {
            return "";
        }
        String html = "<ul>";
        if (searchRequest.getDeveloperId() != null) {
            String devName = searchRequest.getDeveloperId().toString();
            try {
                Developer dev = developerDao.getById(searchRequest.getDeveloperId());
                if (dev != null) {
                    devName = dev.getName();
                }
            } catch (EntityRetrievalException ex) {
                LOGGER.warn("Developer ID " + searchRequest.getDeveloperId() + " not found.", ex);
            }
            html += "<li>Developer: " + devName + "</li>";
        }
        if (!StringUtils.isEmpty(searchRequest.getSearchTerm())) {
            html += "<li>Search Term: " + searchRequest.getSearchTerm() + "</li>";
        }
        if (!CollectionUtils.isEmpty(searchRequest.getCurrentStatusNames())) {
            html += "<li>Status";
            if (searchRequest.getCurrentStatusNames().size() > 1) {
                html += "es";
            }
            html += ": " + searchRequest.getCurrentStatusNames().stream().collect(Collectors.joining(","))
                    + "</li>";
        }

        if (!StringUtils.isEmpty(searchRequest.getSubmittedDateTimeEnd())
                && StringUtils.isEmpty(searchRequest.getSubmittedDateTimeStart())) {
            html += "<li>Created Before: " + searchRequest.getSubmittedDateTimeEnd() + "</li>";
        } else if (!StringUtils.isEmpty(searchRequest.getSubmittedDateTimeStart())
                && StringUtils.isEmpty(searchRequest.getSubmittedDateTimeEnd())) {
            html += "<li>Created After: " + searchRequest.getSubmittedDateTimeStart() + "</li>";
        } else if (!StringUtils.isEmpty(searchRequest.getSubmittedDateTimeStart())
                && !StringUtils.isEmpty(searchRequest.getSubmittedDateTimeEnd())) {
            html += "<li>Created Between: " + searchRequest.getSubmittedDateTimeEnd()
                        + " and " + searchRequest.getSubmittedDateTimeStart() + "</li>";
        }

        if (!StringUtils.isEmpty(searchRequest.getCurrentStatusChangeDateTimeEnd())
                && StringUtils.isEmpty(searchRequest.getCurrentStatusChangeDateTimeStart())) {
            html += "<li>Last Updated Before: " + searchRequest.getCurrentStatusChangeDateTimeEnd() + "</li>";
        } else if (!StringUtils.isEmpty(searchRequest.getCurrentStatusChangeDateTimeStart())
                && StringUtils.isEmpty(searchRequest.getCurrentStatusChangeDateTimeEnd())) {
            html += "<li>Last Updated After: " + searchRequest.getCurrentStatusChangeDateTimeStart() + "</li>";
        } else if (!StringUtils.isEmpty(searchRequest.getCurrentStatusChangeDateTimeStart())
                && !StringUtils.isEmpty(searchRequest.getCurrentStatusChangeDateTimeEnd())) {
            html += "<li>Last Updated Between: " + searchRequest.getCurrentStatusChangeDateTimeEnd()
            + " and " + searchRequest.getCurrentStatusChangeDateTimeStart() + "</li>";
        }
        html += "</ul>";
        return html;
    }

    private void initializeTempFile() throws IOException {
        File downloadFolder = getDownloadFolder();
        Path tempDirBasePath = Paths.get(downloadFolder.getAbsolutePath());
        Path tempDir = Files.createTempDirectory(tempDirBasePath, TEMP_DIR_NAME);
        this.tempDirectory = tempDir.toFile();

        Path csvPath = Files.createTempFile(tempDir, changeRequestsReportFilename, ".csv");
        tempCsvFile = csvPath.toFile();
    }

    private void cleanupTempFiles() {
        LOGGER.info("Deleting temporary files.");
        if (tempCsvFile != null && tempCsvFile.exists()) {
            tempCsvFile.delete();
        } else {
            LOGGER.warn("Temp change request report file was null and could not be deleted.");
        }

        if (tempDirectory != null && tempDirectory.exists()) {
            tempDirectory.delete();
        } else {
            LOGGER.warn("Temp directory for change request report file was null and could not be deleted.");
        }
    }

    private void setSecurityContext(UserDTO user) {
        if (user == null) {
            JWTAuthenticatedUser adminUser = new JWTAuthenticatedUser();
            adminUser.setFullName("Administrator");
            adminUser.setId(User.ADMIN_USER_ID);
            adminUser.setFriendlyName("Admin");
            adminUser.setSubjectName("admin");
            adminUser.getPermissions().add(new GrantedPermission("ROLE_ADMIN"));

            SecurityContextHolder.getContext().setAuthentication(adminUser);
            SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
        } else {
            JWTAuthenticatedUser jobUser = new JWTAuthenticatedUser();
            jobUser.setFullName(user.getFullName());
            jobUser.setId(user.getId());
            jobUser.setFriendlyName(user.getFriendlyName());
            jobUser.setSubjectName(user.getUsername());
            jobUser.getPermissions().add(user.getPermission().getGrantedPermission());

            SecurityContextHolder.getContext().setAuthentication(jobUser);
            SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
        }
    }

    private List<Long> getAcbsFromJobContext(JobExecutionContext jobContext) {
        return Arrays.asList(jobContext.getMergedJobDataMap().getString("acb")
                .split(SchedulerManager.DATA_DELIMITER)).stream()
                .map(acbIdAsString -> Long.parseLong(acbIdAsString))
                .collect(Collectors.toList());
    }

    private void initializeExecutorService() {
        executorService = Executors.newFixedThreadPool(getThreadCountForJob());
    }

    private Integer getThreadCountForJob() throws NumberFormatException {
        return Integer.parseInt(env.getProperty("executorThreadCountForQuartzJobs"));
    }
}
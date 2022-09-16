package gov.healthit.chpl.scheduler.job.changerequest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.ff4j.FF4j;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.changerequest.dao.ChangeRequestDAO;
import gov.healthit.chpl.changerequest.dao.ChangeRequestStatusTypeDAO;
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
import gov.healthit.chpl.scheduler.job.QuartzJob;
import gov.healthit.chpl.scheduler.job.changerequest.presenter.ChangeRequestCsvPresenter;
import gov.healthit.chpl.scheduler.job.changerequest.presenter.ChangeRequestDetailsPresentationService;
import gov.healthit.chpl.scheduler.job.changerequest.presenter.DownloadableAttestationPresenter;
import gov.healthit.chpl.scheduler.job.changerequest.presenter.DownloadableDemographicsPresenter;
import gov.healthit.chpl.util.DateUtil;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "changeRequestsReportJobLogger")
public class ChangeRequestReportEmailJob  extends QuartzJob {
    public static final String JOB_NAME = "changeRequestsReport";
    public static final String SEARCH_REQUEST = "searchRequest";
    public static final String USER_KEY = "user";

    private File tempDirectory, tempAttestationFile, tempDemographicFile;

    @Autowired
    private ChangeRequestDAO changeRequestDao;

    @Autowired
    private ChangeRequestSearchManager changeRequestSearchManager;

    @Autowired
    private ChangeRequestManager changeRequestManager;

    @Autowired
    private ChangeRequestStatusTypeDAO changeRequestStatusTypeDao;

    @Autowired
    private DeveloperDAO developerDao;

    @Autowired
    private ChplHtmlEmailBuilder chplHtmlEmailBuilder;

    @Autowired
    private ChplEmailFactory chplEmailFactory;

    @Autowired
    private FF4j ff4j;

    @Value("${executorThreadCountForQuartzJobs}")
    private String executorThreadCountForQuartzJobs;

    @Value("${changeRequests.report.attestation.filename}")
    private String changeRequestsReportAttestationFilename;

    @Value("${changeRequests.report.demographic.filename}")
    private String changeRequestsReportDemographicFilename;

    @Value("${changeRequests.report.subject}")
    private String changeRequestsReportMessageSubject;

    @Value("${changeRequests.report.heading}")
    private String changeRequestsReportMessageHeading;

    @Value("${changeRequests.report.paragraph1}")
    private String changeRequestsReportMessageBody;

    private ChangeRequestDetailsPresentationService crPresentationService;
    private DateTimeFormatter dateTimeFormatter;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the Change Request Report Email job *********");
        JobDataMap jobDataMap = context.getMergedJobDataMap();
        UserDTO user = (UserDTO) jobDataMap.get(USER_KEY);
        ChangeRequestSearchRequest searchRequest = (ChangeRequestSearchRequest) jobDataMap.get(SEARCH_REQUEST);
        dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        List<ChangeRequestSearchResult> searchResults = null;

        if (user != null) {
            LOGGER.info("Change Requests matching the search request will be sent to " + user.getEmail());
            if (searchRequest == null) {
                searchRequest = getDefaultSearchRequest();
            }
        } else {
            LOGGER.fatal("The provided job context did not have enough information to run the job. "
                    + "A User object must be provided.");
        }

        try {
            setSecurityContext(user);
            searchResults = getAllChangeRequestSearchResults(searchRequest);
        } catch (ValidationException ex) {
            LOGGER.catching(ex);
        }

        LOGGER.info("Found " + searchResults.size() + " change requests matching the search parameters for the job.");
        try (ChangeRequestCsvPresenter attestationPresenter = new DownloadableAttestationPresenter(LOGGER);
                ChangeRequestCsvPresenter demographicPresenter = new DownloadableDemographicsPresenter(LOGGER)) {
            initializeTempFiles();
            attestationPresenter.open(tempAttestationFile);
            demographicPresenter.open(tempDemographicFile);

            Integer threadCount = 1;
            try {
                threadCount = Integer.parseInt(executorThreadCountForQuartzJobs);
            } catch (NumberFormatException ex) {
                LOGGER.error("Could not initialize thread count from '" + executorThreadCountForQuartzJobs + "'. Defaulting to 1.");
            }
            crPresentationService = new ChangeRequestDetailsPresentationService(changeRequestManager, threadCount, LOGGER);
            crPresentationService.present(searchResults,
                    Stream.of(attestationPresenter, demographicPresenter).toList());

        } catch (Exception e) {
            LOGGER.catching(e);
        }

        try {
            sendEmail(context, searchRequest);
        } catch (Exception ex) {
            LOGGER.error("Unable to send email.", ex);
        }

        LOGGER.info("********* Completed the Change Request Report Email job *********");
    }

    private ChangeRequestSearchRequest getDefaultSearchRequest() {
        List<ChangeRequestStatusType> statusTypes = changeRequestStatusTypeDao.getChangeRequestStatusTypes();
        List<Long> updatableStatusIds = changeRequestDao.getUpdatableStatusIds();
        Set<String> updatableStatusTypeNames = statusTypes.stream()
                .filter(statusType -> updatableStatusIds.contains(statusType.getId()))
                .map(statusType -> statusType.getName())
                .collect(Collectors.toSet());
        return ChangeRequestSearchRequest.builder()
                .currentStatusNames(updatableStatusTypeNames)
                .build();
    }

    private List<ChangeRequestSearchResult> getAllChangeRequestSearchResults(ChangeRequestSearchRequest searchRequest)
        throws ValidationException {
        LOGGER.info("Getting all change requests...");
        List<ChangeRequestSearchResult> searchResults = new ArrayList<ChangeRequestSearchResult>();
        LOGGER.info(searchRequest.toString());
        ChangeRequestSearchResponse searchResponse = changeRequestSearchManager.searchChangeRequests(searchRequest);
        searchResults.addAll(searchResponse.getResults());
        while (searchResponse.getRecordCount() > searchResults.size()) {
            searchRequest.setPageSize(searchResponse.getPageSize());
            searchRequest.setPageNumber(searchResponse.getPageNumber() + 1);
            LOGGER.info(searchRequest.toString());
            searchResponse = changeRequestSearchManager.searchChangeRequests(searchRequest);
            searchResults.addAll(searchResponse.getResults());
        }
        LOGGER.info("Got " + searchResults.size() + " total change requests.");
        return searchResults;
    }

    private void sendEmail(JobExecutionContext context, ChangeRequestSearchRequest searchRequest) throws EmailNotSentException, IOException {
        UserDTO user = (UserDTO) context.getMergedJobDataMap().get(USER_KEY);
        String email = user.getEmail();

        LOGGER.info("Sending email to: " + email);
        chplEmailFactory.emailBuilder()
                .recipient(email)
                .subject(changeRequestsReportMessageSubject)
                .htmlMessage(createHtmlMessage(context, searchRequest))
                .fileAttachments(getAttachments())
                .sendEmail();
        LOGGER.info("Completed Sending email to: " + email);
    }

    private List<File> getAttachments() {
        List<File> attachments = new ArrayList<File>();
        attachments.add(tempAttestationFile);
        if (ff4j.check(FeatureList.DEMOGRAPHIC_CHANGE_REQUEST)) {
            attachments.add(tempDemographicFile);
        }
        return attachments;
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
        html += getDeveloperSearchTermHtml(searchRequest);
        html += getSearchTermHtml(searchRequest);
        html += getCurrentStatusSearchTermsHtml(searchRequest);
        html += getSubmittedDateSearchTermsHtml(searchRequest);
        html += getCurrentStatusChangeSearchTermsHtml(searchRequest);
        html += "</ul>";
        return html;
    }

    private String getDeveloperSearchTermHtml(ChangeRequestSearchRequest searchRequest) {
        String html = "";
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
        return html;
    }

    private String getSearchTermHtml(ChangeRequestSearchRequest searchRequest) {
        String html = "";
        if (!StringUtils.isEmpty(searchRequest.getSearchTerm())) {
            html += "<li>Search Term: " + searchRequest.getSearchTerm() + "</li>";
        }
        return html;
    }

    private String getCurrentStatusSearchTermsHtml(ChangeRequestSearchRequest searchRequest) {
        String html = "";
        if (!CollectionUtils.isEmpty(searchRequest.getCurrentStatusNames())) {
            html += "<li>Status";
            if (searchRequest.getCurrentStatusNames().size() > 1) {
                html += "es";
            }
            html += ": " + searchRequest.getCurrentStatusNames().stream().collect(Collectors.joining(","))
                    + "</li>";
        }
        return html;
    }

    private String getSubmittedDateSearchTermsHtml(ChangeRequestSearchRequest searchRequest) {
        LocalDateTime startDateTime = null, endDateTime = null;
        if (!StringUtils.isEmpty(searchRequest.getSubmittedDateTimeStart())) {
            startDateTime = LocalDateTime.parse(searchRequest.getSubmittedDateTimeStart(), dateTimeFormatter);
        }
        if (!StringUtils.isEmpty(searchRequest.getSubmittedDateTimeEnd())) {
            endDateTime = LocalDateTime.parse(searchRequest.getSubmittedDateTimeEnd(), dateTimeFormatter);
        }

        String html = "";
        if (endDateTime != null && startDateTime == null) {
            html += "<li>Created Before: " + DateUtil.formatInEasternTime(endDateTime) + "</li>";
        } else if (endDateTime == null && startDateTime != null) {
            html += "<li>Created After: " + DateUtil.formatInEasternTime(startDateTime) + "</li>";
        } else if (endDateTime != null && startDateTime != null) {
            html += "<li>Created Between: " + DateUtil.formatInEasternTime(endDateTime)
                        + " and " + DateUtil.formatInEasternTime(startDateTime) + "</li>";
        }
        return html;
    }

    private String getCurrentStatusChangeSearchTermsHtml(ChangeRequestSearchRequest searchRequest) {
        LocalDateTime startDateTime = null, endDateTime = null;
        if (!StringUtils.isEmpty(searchRequest.getCurrentStatusChangeDateTimeStart())) {
            startDateTime = LocalDateTime.parse(searchRequest.getCurrentStatusChangeDateTimeStart(), dateTimeFormatter);
        }
        if (!StringUtils.isEmpty(searchRequest.getCurrentStatusChangeDateTimeEnd())) {
            endDateTime = LocalDateTime.parse(searchRequest.getCurrentStatusChangeDateTimeEnd(), dateTimeFormatter);
        }

        String html = "";
        if (endDateTime != null && startDateTime == null) {
            html += "<li>Last Updated Before: " + DateUtil.formatInEasternTime(endDateTime) + "</li>";
        } else if (endDateTime == null && startDateTime != null) {
            html += "<li>Last Updated After: " + DateUtil.formatInEasternTime(startDateTime) + "</li>";
        } else if (endDateTime != null && startDateTime != null) {
            html += "<li>Last Updated Between: " + DateUtil.formatInEasternTime(endDateTime)
                        + " and " + DateUtil.formatInEasternTime(startDateTime) + "</li>";
        }
        return html;
    }

    private void initializeTempFiles() throws IOException {
        File downloadFolder = getDownloadFolder();
        Path tempDirBasePath = Paths.get(downloadFolder.getAbsolutePath());
        Path tempDir = Files.createTempDirectory(tempDirBasePath, TEMP_DIR_NAME);

        this.tempDirectory = tempDir.toFile();
        this.tempDirectory.deleteOnExit();

        Path attestationFilePath = Files.createTempFile(tempDir, changeRequestsReportAttestationFilename, ".csv");
        tempAttestationFile = attestationFilePath.toFile();

        Path demographicFilePath = Files.createTempFile(tempDir, changeRequestsReportDemographicFilename, ".csv");
        tempDemographicFile = demographicFilePath.toFile();
    }

    private void setSecurityContext(UserDTO user) {
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

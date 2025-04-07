package gov.healthit.chpl.scheduler.job.developer.messaging;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.dao.auth.UserDAO;
import gov.healthit.chpl.developer.messaging.DeveloperMessageRequest;
import gov.healthit.chpl.developer.search.DeveloperSearchResult;
import gov.healthit.chpl.developer.search.DeveloperSearchService;
import gov.healthit.chpl.domain.auth.User;
import gov.healthit.chpl.email.ChplEmailFactory;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.permissions.ResourcePermissionsFactory;
import gov.healthit.chpl.scheduler.SchedulerSecurityContextService;
import gov.healthit.chpl.scheduler.job.QuartzJob;
import gov.healthit.chpl.user.cognito.CognitoApiWrapper;
import gov.healthit.chpl.util.Util;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "messageDevelopersJobLogger")
public class MessageDevelopersJob extends QuartzJob implements Job {
    public static final String JOB_NAME = "messageDevelopersJob";
    public static final String DEVELOPER_MESSAGE_REQUEST = "developerMessageRequest";
    public static final String PREVIEW = "preview";

    @Autowired
    private DeveloperSearchService developerSearchService;

    @Autowired
    private DeveloperMessageEmailGenerator messageGenerator;

    @Autowired
    private DeveloperMessagePreviewEmailService previewEmailService;

    @Autowired
    private DeveloperMessagingReportEmailGenerator messagingReportGenerator;

    @Autowired
    private ChplEmailFactory emailFactory;

    @Autowired
    private CognitoApiWrapper cognitoApiWrapper;

    @Autowired
    private ResourcePermissionsFactory resourcePermissionsFactory;

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private SchedulerSecurityContextService securityContextService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

        LOGGER.info("********* Starting Message Developers job. *********");
        try {
            User submittedByUser = getUserFromJobData(context);
            Boolean isPreview = getPreviewFromJobData(context);
            securityContextService.setSecurityContext(submittedByUser);
            LOGGER.info(String.format("Messaging developers on behalf of %s (%s)", submittedByUser.getFullName(), submittedByUser.getEmail()));

            JobDataMap jobDataMap = context.getMergedJobDataMap();
            DeveloperMessageRequest developerMessageRequest = (DeveloperMessageRequest) jobDataMap.get(DEVELOPER_MESSAGE_REQUEST);
            LOGGER.info("Developer search request: " + developerMessageRequest.getQuery());
            LOGGER.info("Additional Recipients:" + developerMessageRequest.getAdditionalRecipients());
            LOGGER.info("Message Subject: " + developerMessageRequest.getSubject());
            LOGGER.info("Message Body: " + developerMessageRequest.getBody());

            List<DeveloperSearchResult> developersToMessage = developerSearchService.getAllPagesOfSearchResults(
                        developerMessageRequest.getQuery(),
                        LOGGER);
            LOGGER.info("Messaging " + developersToMessage.size() + " developers.");

            List<User> enabledDeveloperUsers = resourcePermissionsFactory.get().getAllDeveloperUsers().stream()
                    .filter(devUser -> devUser.getAccountEnabled() != null && devUser.getAccountEnabled())
                    .collect(Collectors.toList());
            List<DeveloperSearchResult> developersWithoutUsers = getDevelopersWithoutUsers(developersToMessage, enabledDeveloperUsers);
            List<DeveloperEmail> developerEmails = developersToMessage.stream()
                    .map(developer -> messageGenerator.getDeveloperEmail(developer, developerMessageRequest, enabledDeveloperUsers))
                    .toList();

            sendEmails(developerEmails, isPreview, developersWithoutUsers, submittedByUser);

            if (!isPreview) {
                sendStatusReportEmail(developerEmails, developerMessageRequest.getSubject(), submittedByUser, developerMessageRequest.getAdditionalRecipients());
            }
        } catch (Exception e) {
            LOGGER.error(e);
        } finally {
            LOGGER.info("********* Completed Message Developers job. *********");
        }
    }

    private List<DeveloperSearchResult> getDevelopersWithoutUsers(List<DeveloperSearchResult> developersToMessage,
            List<User> enabledDeveloperUsers) {
        return developersToMessage.stream()
                .filter(dev -> CollectionUtils.isEmpty(getUsersForDeveloper(dev, enabledDeveloperUsers)))
                .toList();
    }

    private List<User> getUsersForDeveloper(DeveloperSearchResult developer, List<User> enabledDeveloperUsers) {
        return enabledDeveloperUsers.stream()
                .filter(user -> user.getOrganizations().stream().map(org -> org.getId()).toList()
                        .contains(developer.getId()))
                .toList();
    }

    private void sendEmails(List<DeveloperEmail> developerEmails, boolean isPreview,
            List<DeveloperSearchResult> developersWithoutUsers, User submittedByUser) {
        if (isPreview && !CollectionUtils.isEmpty(developerEmails)) {
            developerEmails = developerEmails.subList(0, 1);
            developerEmails.get(0).setRecipients(Stream.of(submittedByUser.getEmail()).collect(Collectors.toList()));
        }

        developerEmails.forEach(email -> {
            try {
                LOGGER.info("Sending email to developer : {}", email.getDeveloper().getName());
                emailFactory.emailBuilder()
                    .recipients(email.getRecipients())
                    .subject(email.getSubject())
                    .htmlMessage(buildMessage(email, isPreview, developersWithoutUsers))
                    .sendEmail();
            } catch (Exception e) {
                LOGGER.error("Error sending emails to developer : {}", email.toString());
                LOGGER.error(e);
            }
        });
    }

    private String buildMessage(DeveloperEmail email, boolean isPreview, List<DeveloperSearchResult> developersWithoutUsers) {
        if (isPreview) {
            String finalEmailMessage = email.getMessage();
            finalEmailMessage = previewEmailService.prependPreviewNotice(finalEmailMessage, email);
            finalEmailMessage = previewEmailService.appendMissingUsers(finalEmailMessage, developersWithoutUsers);
            email.setMessage(finalEmailMessage);
        }
        return email.getMessage();
    }

    private void sendStatusReportEmail(List<DeveloperEmail> developerEmails, String developerMessageSubject,
            User submittedUser, List<String> additionalRecipients) {
        MessagingReportEmail statusReportEmail = messagingReportGenerator.getStatusReportEmail(developerEmails, developerMessageSubject, submittedUser, additionalRecipients);

        try {
            emailFactory.emailBuilder()
                .recipients(statusReportEmail.getToRecipients())
                .ccRecipients(statusReportEmail.getCcRecipieints())
                .subject(statusReportEmail.getSubject())
                .htmlMessage(statusReportEmail.getMessage())
                .sendEmail();
        } catch (Exception e) {
            LOGGER.error("Error sending status report emails to: {}", statusReportEmail.getToRecipients().stream().collect(Collectors.joining("; ")));
            LOGGER.error(e);
        }
    }

    private User getUserFromJobData(JobExecutionContext context) throws UserRetrievalException {
        String id = context.getMergedJobDataMap().get(QuartzJob.JOB_DATA_KEY_SUBMITTED_BY_USER_ID).toString();

        if (NumberUtils.isParsable(id)) {
            return userDAO.getById(Long.valueOf(id)).toDomain();
        } else if (Util.isUUID(id)) {
            return cognitoApiWrapper.getUserInfo(UUID.fromString(id));
        } else {
            return null;
        }
    }

    private Boolean getPreviewFromJobData(JobExecutionContext context) throws UserRetrievalException {
        String previewFromContext = context.getMergedJobDataMap().get(PREVIEW).toString();
        if (!StringUtils.isEmpty(previewFromContext)) {
            return BooleanUtils.toBooleanObject(previewFromContext);
        }
        return false;
    }
}

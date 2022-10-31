package gov.healthit.chpl.scheduler.job;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.microsoft.graph.authentication.TokenCredentialAuthProvider;
import com.microsoft.graph.models.Attachment;
import com.microsoft.graph.models.BodyType;
import com.microsoft.graph.models.EmailAddress;
import com.microsoft.graph.models.FileAttachment;
import com.microsoft.graph.models.ItemBody;
import com.microsoft.graph.models.Message;
import com.microsoft.graph.models.Recipient;
import com.microsoft.graph.models.UserSendMailParameterSet;
import com.microsoft.graph.requests.AttachmentCollectionPage;
import com.microsoft.graph.requests.AttachmentCollectionResponse;
import com.microsoft.graph.requests.GraphServiceClient;

import gov.healthit.chpl.email.ChplEmailMessage;
import gov.healthit.chpl.email.EmailOverrider;
import lombok.extern.log4j.Log4j2;
import okhttp3.Request;

@Log4j2(topic = "sendEmailJobLogger")
@DisallowConcurrentExecution
public class SendEmailJob implements Job {
    public static final String JOB_NAME = "sendEmailJob";
    public static final String MESSAGE_KEY = "messageKey";
    private static final Integer UNLIMITED_RETRY_ATTEMPTS = -1;
    private static final String EMAIL_FILES_DIRECTORY = "emailFiles";
    private static final String GRAPH_DEFAULT_SCOPE = "https://graph.microsoft.com/.default";
    private static final String ODATA_TYPE = "#microsoft.graph.fileAttachment";

    private String azureUser;
    private ClientSecretCredential clientSecretCredential;
    private GraphServiceClient<Request> appClient;
    private EmailOverrider overrider;

    @Autowired
    private Environment env;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

        LOGGER.info("********* Starting the Send Email job. *********");
        overrider = new EmailOverrider(env);
        ChplEmailMessage message = (ChplEmailMessage) context.getMergedJobDataMap().get(MESSAGE_KEY);
        message.setRetryAttempts(message.getRetryAttempts() + 1);
        try {
            initGraphForAppOnlyAuth();
            Message graphMessage = getGraphMessage(message);
            sendMessage(graphMessage);
            LOGGER.info("Email successfully sent to: "
                    + message.getRecipients().stream().
                            map(addr -> addr.toString())
                            .collect(Collectors.joining(", ")));
            LOGGER.info("With subject: " + message.getSubject());
            deleteFiles(message);
        } catch (Exception ex) {
            String failureMessage = "Error sending email to "
                    + message.getRecipients().stream()
                            .map(addr -> addr.toString())
                            .collect(Collectors.joining(", "));
            LOGGER.info(failureMessage);
            LOGGER.info("With subject: " + message.getSubject());
            LOGGER.info("Number of attempts: " + message.getRetryAttempts());
            LOGGER.info("Max number of attempts: " + (getMaxRetryAttempts() == -1 ? "unlimited" : getMaxRetryAttempts().toString()));

            LOGGER.catching(ex);

            if (getMaxRetryAttempts().equals(UNLIMITED_RETRY_ATTEMPTS)
                    || message.getRetryAttempts() < getMaxRetryAttempts()) {
                rescheduleEmailToBeSent(context, message);
            } else {
                // This should trigger a Datadog alert
                String error = "Email could not be sent to "
                        + message.getRecipients().stream()
                                .map(addr -> addr.toString())
                                .collect(Collectors.joining(", "));
                LOGGER.error(error);
                deleteFiles(message);
            }
        }
        LOGGER.info("********* Completed the Send Email job. *********");
    }

    private void initGraphForAppOnlyAuth() {
        if (azureUser == null) {
            azureUser = env.getProperty("azure.user");
        }

        if (clientSecretCredential == null) {
            clientSecretCredential = new ClientSecretCredentialBuilder()
                .clientId(env.getProperty("azure.clientId"))
                .tenantId(env.getProperty("azure.tenantId"))
                .clientSecret(env.getProperty("azure.clientSecret"))
                .build();
        }

        if (appClient == null) {
            final TokenCredentialAuthProvider authProvider =
                new TokenCredentialAuthProvider(
                    List.of(GRAPH_DEFAULT_SCOPE), clientSecretCredential);

            appClient = GraphServiceClient.builder()
                .authenticationProvider(authProvider)
                .buildClient();
        }
    }

    private Message getGraphMessage(ChplEmailMessage message) {
        final Message graphMessage = new Message();
        graphMessage.subject = message.getSubject();
        graphMessage.body = new ItemBody();
        graphMessage.body.content = overrider.getBody(message.getBody(), message.getRecipients());
        graphMessage.body.contentType = BodyType.HTML;
        graphMessage.toRecipients = new ArrayList<Recipient>();

        List<String> recipientAddresses = overrider.getRecipients(message.getRecipients());
        recipientAddresses.stream()
            .forEach(recipientAddress -> {
                final Recipient recipient = new Recipient();
                recipient.emailAddress = new EmailAddress();
                recipient.emailAddress.address = recipientAddress;
                graphMessage.toRecipients.add(recipient);
            });

        if (message.getFileAttachments() != null && message.getFileAttachments().size() > 0) {
            List<Attachment> attachmentsList = new ArrayList<Attachment>();
            for (File file : message.getFileAttachments()) {
                try {
                    LOGGER.info("Attaching " + file.getAbsolutePath());
                    //TODO: This does not work for large attachments.
                    FileAttachment attachment = new FileAttachment();
                    attachment.name = file.getName();
                    attachment.contentBytes = Files.readAllBytes(file.toPath());
                    attachment.oDataType = ODATA_TYPE;
                    attachmentsList.add(attachment);
                } catch (Exception ex) {
                    LOGGER.error("Could not attach file " + file.getAbsolutePath() + " to email.", ex);
                }
            }
            AttachmentCollectionResponse attachmentCollectionResponse = new AttachmentCollectionResponse();
            attachmentCollectionResponse.value = attachmentsList;
            AttachmentCollectionPage attachmentCollectionPage = new AttachmentCollectionPage(attachmentCollectionResponse, null);
            graphMessage.attachments = attachmentCollectionPage;
        }
        return graphMessage;
    }

    private void sendMessage(Message message) {
        appClient.users(azureUser)
            .sendMail(UserSendMailParameterSet.newBuilder()
                .withMessage(message)
                .withSaveToSentItems(overrider.getSaveToSentItems())
                .build())
            .buildRequest()
            .post();
    }

    private void rescheduleEmailToBeSent(JobExecutionContext context, ChplEmailMessage message) {
        message.setFileAttachments(copyFilesOnFirstReschedule(message));

        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put(SendEmailJob.MESSAGE_KEY, message);

        Date retryTime = getRetryTime();
        Trigger retryTrigger = TriggerBuilder.newTrigger()
                .withDescription("Retry Email Trigger")
                .forJob(context.getJobDetail().getKey())
                .usingJobData(jobDataMap)
                .startAt(retryTime)
                .build();

        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
        LOGGER.info("Email rescheduled to be sent at: " + df.format(retryTime));
        try {
            context.getScheduler().scheduleJob(retryTrigger);
        } catch (SchedulerException e) {
            LOGGER.error("Could not reschedule trigger due to exception: ", e);
        }
    }

    private Date getRetryTime() {
        Integer retryInterval = Integer.valueOf(env.getProperty("emailRetryIntervalInMinutes"));
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, retryInterval);
        return cal.getTime();
    }

    private Integer getMaxRetryAttempts() {
        return Integer.valueOf(env.getProperty("emailRetryAttempts"));
    }

    private List<File> copyFilesOnFirstReschedule(ChplEmailMessage message) {
        if (message.getRetryAttempts().equals(1) && message.getFileAttachments() != null) {
            if (message.getFileAttachments() == null || message.getFileAttachments().size() == 0) {
                LOGGER.info("No files to move.");
            }
            return  message.getFileAttachments().stream()
                    .map(file -> copyTempFileToPermanentLocation(file))
                    .collect(Collectors.toList());
        } else {
            return message.getFileAttachments();
        }

    }

    private File copyTempFileToPermanentLocation(File originalFile) {
        Path newPath = Paths.get(env.getProperty("downloadFolderPath") + File.separator + EMAIL_FILES_DIRECTORY + File.separator + originalFile.getName());
        Path origPath = originalFile.toPath();
        try {
            Files.createDirectories(newPath.getParent());
            Files.copy(origPath, newPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            LOGGER.info("Could not copy original file: " + origPath.toString());
            LOGGER.catching(e);
        }
        LOGGER.info("Copied " + origPath.toString() + " to " + newPath.toString());
        return newPath.toFile();
    }

    private void deleteFiles(ChplEmailMessage message) {
        if (message.getFileAttachments() != null) {
            message.getFileAttachments().stream()
                    .forEach(file -> {
                        try {
                            Files.deleteIfExists(file.toPath());
                            LOGGER.info("Deleting file: " + file.getPath());
                        } catch (IOException e) {
                            LOGGER.info("Could not delete file after sending: " + file.getPath());
                            LOGGER.catching(e);
                        }
                    });
        }
    }
}

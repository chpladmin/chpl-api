package gov.healthit.chpl.scheduler.job;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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

import org.apache.commons.collections4.CollectionUtils;
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
import com.microsoft.graph.models.AttachmentCreateUploadSessionParameterSet;
import com.microsoft.graph.models.AttachmentItem;
import com.microsoft.graph.models.AttachmentType;
import com.microsoft.graph.models.BodyType;
import com.microsoft.graph.models.EmailAddress;
import com.microsoft.graph.models.FileAttachment;
import com.microsoft.graph.models.ItemBody;
import com.microsoft.graph.models.Message;
import com.microsoft.graph.models.Recipient;
import com.microsoft.graph.models.UploadSession;
import com.microsoft.graph.options.HeaderOption;
import com.microsoft.graph.requests.GraphServiceClient;
import com.microsoft.graph.tasks.IProgressCallback;
import com.microsoft.graph.tasks.LargeFileUploadTask;

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
        if (CollectionUtils.isEmpty(message.getRecipients())) {
            LOGGER.fatal("No recipients found in the message with subject: " + message.getSubject()
                + ". The message will not be sent.");
            //TODO: Send email to "Upload Errors Channel"
        } else {
            message.setRetryAttempts(message.getRetryAttempts() + 1);
            Message graphMessage = null;
            try {
                initGraphForAppOnlyAuth();
                graphMessage = getDraftMessage(message);
                uploadAttachments(graphMessage, message.getFileAttachments());
                sendMessage(graphMessage);
                LOGGER.info("Email successfully sent to: "
                        + message.getRecipients().stream().
                                map(addr -> addr.toString())
                                .collect(Collectors.joining(", ")));
                LOGGER.info("With subject: " + message.getSubject());
                deleteFiles(message);
                if (!overrider.getSaveToSentItems()) {
                    deleteMessage(graphMessage);
                }
            } catch (Exception ex) {
                //at this point the draft wasn't sent so we would be deleting it from the drafts folder
                deleteMessage(graphMessage);
                //log the failure
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

    private Message getDraftMessage(ChplEmailMessage message) {
        final Message draftMessage = new Message();
        draftMessage.subject = message.getSubject();
        draftMessage.body = new ItemBody();
        draftMessage.body.content = overrider.getBody(message.getBody(), message.getRecipients());
        draftMessage.body.contentType = BodyType.HTML;
        draftMessage.toRecipients = new ArrayList<Recipient>();


        //TODO: DO we want to keep this code??
        //IS there a header I can use to tell the message not to stick around in SEnt Items? I can't find a list of available headers.


        List<String> recipientAddresses = overrider.getRecipients(message.getRecipients());
        recipientAddresses.stream()
            .forEach(recipientAddress -> {
                final Recipient recipient = new Recipient();
                recipient.emailAddress = new EmailAddress();
                recipient.emailAddress.address = recipientAddress;
                draftMessage.toRecipients.add(recipient);
            });

        Message savedDraft = appClient.users(azureUser).messages()
            .buildRequest(new HeaderOption("Prefer", "IdType=\"ImmutableId\""))
            .post(draftMessage);

        return savedDraft;
    }

    private void uploadAttachments(Message message, List<File> attachments) {
        if (CollectionUtils.isEmpty(attachments)) {
            return;
        }
        attachments.stream()
            .forEach(attachment -> uploadAttachment(message, attachment));
    }

    private void uploadAttachment(Message message, File attachment) {
        AttachmentItem attachmentItem = new AttachmentItem();
        attachmentItem.attachmentType = AttachmentType.FILE;
        attachmentItem.name = attachment.getName();
        attachmentItem.size = attachment.length();

        UploadSession uploadSession = appClient.users(azureUser).messages(message.id)
            .attachments()
                .createUploadSession(AttachmentCreateUploadSessionParameterSet.newBuilder()
                    .withAttachmentItem(attachmentItem)
                    .build())
            .buildRequest()
            .post();

        //iteratively upload byte ranges of the file, in order
        IProgressCallback callback = new IProgressCallback() {
            @Override
            // Called after each slice of the file is uploaded
            public void progress(final long current, final long max) {
                LOGGER.debug(
                    String.format("Uploaded %d bytes of %d total bytes", current, max)
                );
            }
        };

        InputStream fileStream = null;
        LargeFileUploadTask<FileAttachment> uploadTask = null;
        try {
            fileStream = new FileInputStream(attachment);
            uploadTask = new LargeFileUploadTask<FileAttachment>(uploadSession, appClient,
                        fileStream, attachment.length(), FileAttachment.class);

            // Do the upload
            uploadTask.upload(0, null, callback);
        } catch (FileNotFoundException ex) {
            //the FileInputStream could not be created
            LOGGER.error("The file " + attachment.getAbsolutePath() + " could not be found and will not be sent as an attachment.", ex);
        } catch (IOException ex) {
            //the uploadTask.upload method had an error
            LOGGER.error("The upload of file attachment " + attachment.getAbsoluteFile() + " to message with ID " + message.id + " failed.", ex);
        } finally {
            try {
                fileStream.close();
            } catch (IOException io) {
                LOGGER.warn("Could not close the filestream for the attachment: " + io.getMessage());
            }
        }
    }

    private void sendMessage(Message message) {
        LOGGER.info("Sending message with ID " + message.id);
        appClient.users(azureUser).messages(message.id)
            .send()
            .buildRequest()
        .post();
    }

    private void deleteMessage(Message message) {
        //TODO:
        //The message object gets moved from Drafts to Sent Items, but it might not be available right away...
        //It can take time to appear there.
        //So we can have code to wait, to retry, a separate job perhaps to retry? Don't bother with it?
        //This will be really hard to test.
        LOGGER.info("Deleting message with ID " + message.id);
        try {
            appClient.users(azureUser).messages(message.id)
                .buildRequest()
            .delete();
        } catch (Exception ex) {
            LOGGER.error("Error deleting" + (message.isDraft ? " draft " : " ")
                    + "message with ID '" + message.id + "'. Message had subject '"
                    + message.subject + "' and is addressed to "
                    + message.toRecipients.stream()
                        .map(recip -> recip.emailAddress.address)
                        .collect(Collectors.joining(",")));
            LOGGER.catching(ex);
        }
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
                            LOGGER.info("Deleted file: " + file.getPath());
                        } catch (IOException e) {
                            LOGGER.info("Could not delete file after sending: " + file.getPath());
                            LOGGER.catching(e);
                        }
                    });
        }
    }
}

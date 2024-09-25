package gov.healthit.chpl.scheduler.job;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.microsoft.graph.core.models.IProgressCallback;
import com.microsoft.graph.core.models.UploadResult;
import com.microsoft.graph.core.tasks.LargeFileUploadTask;
import com.microsoft.graph.models.Attachment;
import com.microsoft.graph.models.AttachmentItem;
import com.microsoft.graph.models.AttachmentType;
import com.microsoft.graph.models.BodyType;
import com.microsoft.graph.models.EmailAddress;
import com.microsoft.graph.models.FileAttachment;
import com.microsoft.graph.models.ItemBody;
import com.microsoft.graph.models.Message;
import com.microsoft.graph.models.Recipient;
import com.microsoft.graph.models.UploadSession;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import com.microsoft.graph.users.item.messages.MessagesRequestBuilder;
import com.microsoft.graph.users.item.messages.item.attachments.createuploadsession.CreateUploadSessionPostRequestBody;

import gov.healthit.chpl.email.ChplEmailFactory;
import gov.healthit.chpl.email.ChplEmailMessage;
import gov.healthit.chpl.email.EmailOverrider;
import gov.healthit.chpl.exception.EmailNotSentException;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "sendEmailJobLogger")
@DisallowConcurrentExecution
public class SendEmailJob implements Job {
    public static final String JOB_NAME = "sendEmailJob";
    public static final String MESSAGE_KEY = "messageKey";
    private static final Integer UNLIMITED_RETRY_ATTEMPTS = -1;
    private static final String EMAIL_FILES_DIRECTORY = "emailFiles";
    private static final String HEADER_PREFER = "Prefer";
    private static final String HEADER_IMMUTABLE_ID = "IdType=\"ImmutableId\"";
    private static final int MAX_ATTACH_LARGE_FILE_ATTEMPTS = 5;
    private static final BigInteger THREE_MB_IN_BYTES = new BigInteger("3145728");

    private String azureUser;
    private EmailOverrider overrider;

    @Autowired
    private GraphServiceClient graphServiceClient;

    @Value("${internalErrorEmailRecipients}")
    private String internalErrorEmailRecipients;

    @Value("${noRecipientsErrorEmailSubject}")
    private String noRecipientsErrorEmailSubject;

    @Value("${noRecipientsErrorEmailBody}")
    private String noRecipientsErrorEmailBody;

    @Autowired
    private ChplEmailFactory chplEmailFactory;

    @Autowired
    private Environment env;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

        LOGGER.info("********* Starting the Send Email job. *********");
        overrider = new EmailOverrider(env);
        if (azureUser == null) {
            LOGGER.debug("Getting the Azure user");
            azureUser = env.getProperty("azure.user");
            LOGGER.debug("Azure user is " + azureUser);
        }

        ChplEmailMessage message = (ChplEmailMessage) context.getMergedJobDataMap().get(MESSAGE_KEY);
        if (CollectionUtils.isEmpty(message.getRecipients())) {
            LOGGER.fatal("No recipients found in the message with subject: " + message.getSubject()
                + ". The message will not be sent.");
            sendInternalErrorEmail(message);
        } else {
            message.setRetryAttempts(message.getRetryAttempts() + 1);
            Message graphMessage = null;
            try {
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

    private Message getDraftMessage(ChplEmailMessage message) {
        LOGGER.debug("Creating a draft message with subject '" + message.getSubject() + "'");
        final Message draftMessage = new Message();
        draftMessage.setSubject(message.getSubject());
        ItemBody body = new ItemBody();
        body.setContent(overrider.getBody(message.getBody(), message.getRecipients()));
        body.setContentType(BodyType.Html);
        draftMessage.setBody(body);

        draftMessage.setToRecipients(new ArrayList<Recipient>());
        List<String> recipientAddresses = overrider.getRecipients(message.getRecipients());
        recipientAddresses.stream()
            .forEach(recipientAddress -> {
                final Recipient recipient = new Recipient();
                EmailAddress emailAddress = new EmailAddress();
                emailAddress.setAddress(recipientAddress);
                recipient.setEmailAddress(emailAddress);
                draftMessage.getToRecipients().add(recipient);
            });

        LOGGER.debug("Saving the draft message");
        MessagesRequestBuilder messageBuilder = graphServiceClient
                .users().byUserId(azureUser)
                .messages();
        Message savedDraft = messageBuilder.post(draftMessage, new ImmutableIdHeaderRequestConfiguration());
        LOGGER.debug("Saved the draft message with ID " + savedDraft.getId());

        return savedDraft;
    }

    private void uploadAttachments(Message message, List<File> attachments) {
        if (CollectionUtils.isEmpty(attachments)) {
            LOGGER.debug("No attachments for message " + message.getId());
            return;
        }
        attachments.stream()
            .forEach(attachment -> uploadAttachment(message, attachment));
    }

    private void uploadAttachment(Message message, File attachment) {
        LOGGER.info("Uploading attachment " + attachment.getName() + " for message " + message.getId());
        long attachmentSize = FileUtils.sizeOfAsBigInteger(attachment).longValue();
        LOGGER.info("Attachment is " + attachmentSize + " bytes");
        if (attachmentSize < THREE_MB_IN_BYTES.longValue()) {
            uploadSmallAttachment(message, attachment);
        } else {
            LOGGER.info("Attaching file larger than 3MB: " + attachment.getName());
            uploadLargeAttachment(message, attachment);
        }
    }

    private void uploadSmallAttachment(Message message, File attachment) {
        try {
            FileAttachment fileAttachment = new FileAttachment();
            fileAttachment.setOdataType("#microsoft.graph.fileAttachment");
            fileAttachment.setName(attachment.getName());
            byte[] contentBytes = FileUtils.readFileToByteArray(attachment);
            fileAttachment.setContentBytes(contentBytes);
            Attachment attachedFile = graphServiceClient.users().byUserId(azureUser)
                    .messages().byMessageId(message.getId())
                    .attachments()
                    .post(fileAttachment);
            if (attachedFile != null) {
                LOGGER.info("Completed uploading attachment " + attachment.getName() + " for message " + message.getId());
            }
        } catch (IOException ex) {
            LOGGER.error("Exception attaching file " + attachment.getName(), ex);
        } catch (Exception ex) {
            LOGGER.error("Exception attaching file " + attachment.getName(), ex);
        }
    }

    private void uploadLargeAttachment(Message message, File attachment) {
        CreateUploadSessionPostRequestBody createUploadSessionPostRequestBody = new CreateUploadSessionPostRequestBody();
        AttachmentItem attachmentItem = new AttachmentItem();
        attachmentItem.setAttachmentType(AttachmentType.File);
        attachmentItem.setName(attachment.getName());
        attachmentItem.setSize(attachment.length());
        createUploadSessionPostRequestBody.setAttachmentItem(attachmentItem);
        UploadSession uploadSession = graphServiceClient
                .users().byUserId(azureUser)
                .messages().byMessageId(message.getId())
                .attachments()
                .createUploadSession()
                .post(createUploadSessionPostRequestBody);

        //iteratively upload byte ranges of the file, in order
        IProgressCallback callback = new IProgressCallback() {
            @Override
            public void report(long current, long max) {
                LOGGER.debug(
                        String.format("Uploaded %d bytes of %d total bytes", current, max)
                    );
            }
        };

        InputStream fileStream = null;
        LargeFileUploadTask<FileAttachment> uploadTask = null;
        try {
            fileStream = new FileInputStream(attachment);
            uploadTask = new LargeFileUploadTask<FileAttachment>(graphServiceClient.getRequestAdapter(),
                        uploadSession,
                        fileStream,
                        attachment.length(),
                        FileAttachment::createFromDiscriminatorValue);

            UploadResult<FileAttachment> uploadResult = uploadTask.upload(MAX_ATTACH_LARGE_FILE_ATTEMPTS, callback);
            if (uploadResult.isUploadSuccessful()) {
                LOGGER.debug("Upload successful");
            } else {
                LOGGER.error("Upload failed");
            }
            LOGGER.info("Completed uploading attachment " + attachment.getName() + " for message " + message.getId());
        } catch (FileNotFoundException ex) {
            //the FileInputStream could not be created
            LOGGER.error("The file " + attachment.getAbsolutePath() + " could not be found and will not be sent as an attachment.", ex);
        } catch (IOException ex) {
            //the uploadTask.upload method had an error
            LOGGER.error("The upload of file attachment " + attachment.getAbsoluteFile() + " to message with ID " + message.getId() + " failed.", ex);
        } catch (NoSuchMethodException ex) {
            LOGGER.error("The upload of file attachment " + attachment.getAbsoluteFile() + " to message with ID " + message.getId() + " failed.", ex);
        } catch (Exception ex) {
            LOGGER.error("The upload of file attachment " + attachment.getAbsoluteFile() + " to message with ID " + message.getId() + " failed.", ex);
        } finally {
            try {
                fileStream.close();
            } catch (IOException io) {
                LOGGER.error("Could not close the filestream for the attachment: " + io.getMessage());
            }
        }
    }

    private void sendMessage(Message message) {
        LOGGER.info("Sending message with ID " + message.getId());
        graphServiceClient
        .users()
            .byUserId(azureUser)
                .messages().byMessageId(message.getId())
        .send()
        .post();
    }

    private void deleteMessage(Message message) {
        //The message object gets moved from Drafts to Sent Items, but it might not be available right away...
        //It can take time to appear there.
        //If this turns out to be a problem, like if Sent Items fills up or something,
        //we can adjust the code here to to wait, to retry a configurable number of times, or schedule a separate job to retry.
        try {
            LOGGER.info("Deleting message with ID " + message.getId());
            graphServiceClient
            .users()
                .byUserId(azureUser)
                    .messages().byMessageId(message.getId())
                    .delete();
        } catch (Exception ex) {
            if (message != null) {
                LOGGER.warn("Error deleting" + (message.getIsDraft() ? " draft " : " ")
                    + "message with ID '" + message.getId() + "'. Message had subject '"
                    + message.getSubject() + "' and is addressed to "
                    + message.getToRecipients().stream()
                        .map(recip -> recip.getEmailAddress().getAddress())
                        .collect(Collectors.joining(",")));
            }
            LOGGER.catching(ex);
        }
    }

    private void sendInternalErrorEmail(ChplEmailMessage message) {
        try {
            chplEmailFactory.emailBuilder()
                    .recipients(internalErrorEmailRecipients.split(","))
                    .subject(noRecipientsErrorEmailSubject)
                    .htmlMessage(String.format(noRecipientsErrorEmailBody, message.getSubject(), message.getBody()))
                    .sendEmail();
        } catch (EmailNotSentException msgEx) {
            LOGGER.error("Could not send email about failed listing upload: " + msgEx.getMessage(), msgEx);
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

    private static final class ImmutableIdHeaderRequestConfiguration implements Consumer<MessagesRequestBuilder.PostRequestConfiguration> {
        @Override
        public void accept(com.microsoft.graph.users.item.messages.MessagesRequestBuilder.PostRequestConfiguration t) {
            t.headers.add(HEADER_PREFER, HEADER_IMMUTABLE_ID);
        }
    }
}

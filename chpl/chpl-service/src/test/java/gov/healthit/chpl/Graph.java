package gov.healthit.chpl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

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
import com.microsoft.graph.models.UserSendMailParameterSet;
import com.microsoft.graph.requests.GraphServiceClient;
import com.microsoft.graph.tasks.IProgressCallback;
import com.microsoft.graph.tasks.LargeFileUploadTask;

import okhttp3.Request;

public class Graph {
    private static final String GRAPH_DEFAULT_SCOPE = "https://graph.microsoft.com/.default";

    private String clientId, tenantId, clientSecret, userId;
    private ClientSecretCredential clientSecretCredential;
    private GraphServiceClient<Request> appClient;

    public Graph() {
        Properties oAuthProperties = new Properties();
        try {
            FileInputStream fis = new FileInputStream("C:\\Users\\kekey\\workspace\\Servers\\Tomcat v8.5 Server at localhost-config\\environment-override.properties");
            oAuthProperties.load(fis);
        } catch (IOException e) {
            System.out.println("Unable to read OAuth configuration. Make sure you have a properly formatted oAuth.properties file. See README for details.");
            return;
        }

        clientId = oAuthProperties.getProperty("azure.clientId");
        clientSecret = oAuthProperties.getProperty("azure.clientSecret");
        tenantId = oAuthProperties.getProperty("azure.tenantId");
        userId = oAuthProperties.getProperty("azure.user");
    }

    private void initGraphForAppOnlyAuth() throws Exception {
        if (clientSecretCredential == null) {
            clientSecretCredential = new ClientSecretCredentialBuilder()
                .clientId(clientId)
                .tenantId(tenantId)
                .clientSecret(clientSecret)
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

    public void sendMailAsApp(String subject, String body, String recipient) throws Exception {
        initGraphForAppOnlyAuth();

        // Ensure client isn't null
        if (appClient == null) {
            throw new Exception("Graph has not been initialized for app-only auth");
        }

        // Create a new message
        final Message draftMessage = new Message();
        draftMessage.subject = subject;
        draftMessage.body = new ItemBody();
        draftMessage.body.content = body;
        draftMessage.body.contentType = BodyType.TEXT;

        final Recipient toRecipient = new Recipient();
        toRecipient.emailAddress = new EmailAddress();
        toRecipient.emailAddress.address = recipient;
        draftMessage.toRecipients = List.of(toRecipient);

        //create draft message that the large file can be uploaded to.
        Message savedDraft = appClient.users(userId).messages()
            .buildRequest()
            .post(draftMessage);

        //create upload session referencing the draft message
        File file = new File("C:\\CHPL\\files\\chpl-2015-20220429_104242.xml");
        AttachmentItem attachmentItem = new AttachmentItem();
        attachmentItem.attachmentType = AttachmentType.FILE;
        attachmentItem.name = file.getName();
        attachmentItem.size = file.length();

        UploadSession uploadSession = appClient.users(userId).messages(savedDraft.id)
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
                System.out.println(
                    String.format("Uploaded %d bytes of %d total bytes", current, max)
                );
            }
        };

        InputStream fileStream = new FileInputStream(file);
        LargeFileUploadTask<FileAttachment> uploadTask =
            new LargeFileUploadTask<FileAttachment>(uploadSession, appClient,
                    fileStream, file.length(), FileAttachment.class);

        // Do the upload
        uploadTask.upload(0, null, callback);

        //send the draft message
        //TODO: do we have to send it this way? That leaves it in Sent Items and I don't think there is a way around it
//        appClient.users(userId).messages(draftMessage.id)
//            .send()
//            .buildRequest()
//        .post();

        //TODO: can we sent it this way and not save to sent items?
        appClient.users(userId)
        .sendMail(UserSendMailParameterSet.newBuilder()
            .withMessage(savedDraft)
            .withSaveToSentItems(false)
            .build())
        .buildRequest()
        .post();

//        List<Attachment> attachmentsList = new ArrayList<Attachment>();
//        File file = new File("C:\\Users\\kekey\\projects\\chpl-api\\chpl\\chpl-api\\src\\main\\resources\\2014 Listing CSV Data Dictionary.csv");
//        try {
//            FileAttachment attachment = new FileAttachment();
//            attachment.name = file.getName();
//            attachment.contentType = "text/csv";
//            //attachment.contentBytes = Base64.getDecoder().decode("SGVsbG8gV29ybGQh");
//            attachment.oDataType = "#microsoft.graph.fileAttachment";
//            attachment.contentBytes = Files.readAllBytes(file.toPath());
//            attachmentsList.add(attachment);
//        } catch (Exception ex) {
//            System.out.println("Could not attach file " + file.getAbsolutePath() + " to email.");
//        }
//        AttachmentCollectionResponse attachmentCollectionResponse = new AttachmentCollectionResponse();
//        attachmentCollectionResponse.value = attachmentsList;
//        AttachmentCollectionPage attachmentCollectionPage = new AttachmentCollectionPage(attachmentCollectionResponse, null);
//        message.attachments = attachmentCollectionPage;
//        appClient.users(userId)
//            .sendMail(UserSendMailParameterSet.newBuilder()
//                .withMessage(message)
//                .withSaveToSentItems(false)
//                .build())
//            .buildRequest()
//            .post();
    }
}

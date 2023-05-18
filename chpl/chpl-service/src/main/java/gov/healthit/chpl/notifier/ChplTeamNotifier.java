package gov.healthit.chpl.notifier;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.email.ChplEmailFactory;
import gov.healthit.chpl.exception.EmailNotSentException;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class ChplTeamNotifier {

    private ChplEmailFactory chplEmailFactory;

    private String notifcationAddress;

    public ChplTeamNotifier(ChplEmailFactory chplEmailFactory, @Value("${internalErrorEmailRecipients}") String notificationAddress) {
        this.chplEmailFactory = chplEmailFactory;
        this.notifcationAddress = notificationAddress;
    }

    public void sendNotification(ChplTeamNotifierMessage message) {
        try {
            chplEmailFactory.emailBuilder()
                    .recipients(List.of(notifcationAddress))
                    .subject(message.getSubject())
                    .fileAttachments(message.getFiles())
                    .htmlMessage(message.getMessage())
                    .sendEmail();
        } catch (EmailNotSentException msgEx) {
            LOGGER.error("Could not send email about failed listing upload: " + msgEx.getMessage(), msgEx);
        }
    }
}

package gov.healthit.chpl.notifier;

import java.io.File;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import org.springframework.core.env.Environment;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.email.ChplHtmlEmailBuilder;

public class FutureCertificationStatusNotifierMessage implements ChplTeamNotifierMessage {

    private CertifiedProductSearchDetails listing;
    private Long activityId;
    private LocalDate currentStatusUpdateDay;
    private String username;
    private ChplHtmlEmailBuilder chplHtmlEmailBuilder;
    private String subject;
    private String body;

    public FutureCertificationStatusNotifierMessage(CertifiedProductSearchDetails listing,
            Long activityId,
            LocalDate currentStatusUpdateDay,
            String username,
            Environment env,
            ChplHtmlEmailBuilder chplHtmlEmailBuilder) {
        this.listing = listing;
        this.activityId = activityId;
        this.currentStatusUpdateDay = currentStatusUpdateDay;
        this.username = username;
        this.chplHtmlEmailBuilder = chplHtmlEmailBuilder;
        this.subject = env.getProperty("futureCertificationStatusUsed.subject");
        this.body = env.getProperty("futureCertificationStatusUsed.body");
    }

    @Override
    public String getMessage() {
        String htmlBody = String.format(body, new Date().toString(),
                username, listing.getChplProductNumber(), listing.getId().toString(),
                currentStatusUpdateDay.toString(), activityId.toString());

        return chplHtmlEmailBuilder.initialize()
                .paragraph("", htmlBody)
                .build();
    }

    @Override
    public String getSubject() {
        return subject;
    }

    @Override
    public List<File> getFiles() {
        return null;
    }

}

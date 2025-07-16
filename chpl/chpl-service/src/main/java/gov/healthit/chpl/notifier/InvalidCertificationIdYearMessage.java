package gov.healthit.chpl.notifier;

import java.io.File;
import java.util.List;

import org.springframework.core.env.Environment;

import gov.healthit.chpl.email.ChplHtmlEmailBuilder;
import gov.healthit.chpl.util.Util;

public class InvalidCertificationIdYearMessage implements ChplTeamNotifierMessage {

    private String invalidYear;
    private List<String> allowedYears;
    private ChplHtmlEmailBuilder chplHtmlEmailBuilder;
    private String subject;
    private String body;

    public InvalidCertificationIdYearMessage(String invalidYear,
            List<String> allowedYears,
            Environment env,
            ChplHtmlEmailBuilder chplHtmlEmailBuilder) {
        this.invalidYear = invalidYear;
        this.allowedYears = allowedYears;
        this.chplHtmlEmailBuilder = chplHtmlEmailBuilder;
        this.subject = env.getProperty("invalidCertificationIdYearDetected.subject");
        this.body = env.getProperty("invalidCertificationIdYearDetected.body");
    }

    @Override
    public String getMessage() {
        String htmlBody = String.format(body,
                invalidYear,
                Util.joinListGrammatically(allowedYears, "and"));

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

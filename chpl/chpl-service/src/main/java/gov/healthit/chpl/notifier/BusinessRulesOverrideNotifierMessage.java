package gov.healthit.chpl.notifier;

import java.io.File;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.eclipse.collections.api.set.sorted.ImmutableSortedSet;
import org.springframework.core.env.Environment;

import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.email.ChplHtmlEmailBuilder;

public class BusinessRulesOverrideNotifierMessage implements ChplTeamNotifierMessage {

    private ImmutableSortedSet<String> overriddenBusinessErrors;
    private String chplProductNumber;
    private User user;
    private ChplHtmlEmailBuilder chplHtmlEmailBuilder;
    private String subject;
    private String body;
    private String tableHeader;

    public BusinessRulesOverrideNotifierMessage(String chplProductNumber, User user, ImmutableSortedSet<String> overriddenBusinessErrors, Environment env,
            ChplHtmlEmailBuilder chplHtmlEmailBuilder) {
        this.chplProductNumber = chplProductNumber;
        this.overriddenBusinessErrors = overriddenBusinessErrors;
        this.user = user;
        this.chplHtmlEmailBuilder = chplHtmlEmailBuilder;
        this.subject = env.getProperty("businessRulesOverride.subject");
        this.body = env.getProperty("businessRulesOverride.body");
        this.tableHeader = env.getProperty("businessRulesOverride.tableHeader");
    }

    @Override
    public String getMessage() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss");
        String populatedBody = String.format(body,
                chplProductNumber,
                user.getUsername(),
                dtf.withZone(ZoneId.of("UTC")).format(ZonedDateTime.now()));

        return chplHtmlEmailBuilder.initialize()
                .paragraph("", populatedBody)
                .table(List.of(tableHeader),
                        overriddenBusinessErrors.stream()
                                .map(err -> List.of(err))
                                .toList())
                .build();
    }

    @Override
    public String getSubject() {
        return String.format(subject, chplProductNumber);
    }

    @Override
    public List<File> getFiles() {
        return null;
    }

}

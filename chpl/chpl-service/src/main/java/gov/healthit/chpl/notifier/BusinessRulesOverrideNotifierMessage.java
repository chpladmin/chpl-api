package gov.healthit.chpl.notifier;

import java.io.File;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.collections.api.set.sorted.ImmutableSortedSet;

import gov.healthit.chpl.auth.user.User;

public class BusinessRulesOverrideNotifierMessage implements ChplTeamNotifierMessage {

    private ImmutableSortedSet<String> overriddenBusinessErrors;
    private String chplProductNumber;
    private User user;

    public BusinessRulesOverrideNotifierMessage(String chplProductNumber, User user, ImmutableSortedSet<String> overriddenBusinessErrors) {
        this.chplProductNumber = chplProductNumber;
        this.overriddenBusinessErrors = overriddenBusinessErrors;
        this.user = user;
    }

    @Override
    public String getMessage() {
        String allRules = overriddenBusinessErrors.stream()
                .collect(Collectors.joining("<br/>"));

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss");

        return String.format("The following rules were overridden when saving listing %s by %s at %s:</br/></br/>%s",
                chplProductNumber,
                user.getUsername(),
                dtf.withZone(ZoneId.of("UTC")).format(ZonedDateTime.now()),
                allRules);
    }

    @Override
    public String getSubject() {
        return chplProductNumber + " - Business Rules Overridden";
    }

    @Override
    public List<File> getFiles() {
        return null;
    }

}

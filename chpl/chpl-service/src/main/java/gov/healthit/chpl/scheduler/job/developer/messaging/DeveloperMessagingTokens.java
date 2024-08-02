package gov.healthit.chpl.scheduler.job.developer.messaging;

import java.util.List;
import java.util.stream.Stream;

import lombok.Getter;

@Getter
public enum DeveloperMessagingTokens {
    NAME("|DEVNAME|", "|DEVELOPERNAME|"),
    USERS("|DEVUSERS|", "|DEVELOPERUSERS|");

    private List<String> tokenRepresentations;
    DeveloperMessagingTokens(String... representations) {
        this.tokenRepresentations = Stream.of(representations).toList();
    }
}

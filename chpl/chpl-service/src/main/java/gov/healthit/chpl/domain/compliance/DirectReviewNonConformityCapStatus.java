package gov.healthit.chpl.domain.compliance;

import java.util.Optional;
import java.util.stream.Stream;

public enum DirectReviewNonConformityCapStatus {
    CAP_APPROVED("CAP approved"),
    CAP_NOT_APPROVED("CAP not approved"),
    CAP_NOT_PROVIDED("No CAP provided for approval"),
    FAILED_TO_COMPLETE("Failed to complete"),
    RESOLVED_WITHOUT_CAP("Resolved without CAP"),
    TBD("To be determined");

    private String prettyName;
    DirectReviewNonConformityCapStatus(String prettyName) {
        this.prettyName = prettyName;
    }

    public String getPrettyName() {
        return this.prettyName;
    }

    public static DirectReviewNonConformityCapStatus getByName(String name) {
        Optional<DirectReviewNonConformityCapStatus> optStatus = Stream.of(values())
            .filter(val -> val.getPrettyName().equalsIgnoreCase(name))
            .findAny();
        if (optStatus.isEmpty()) {
            return null;
        }
        return optStatus.get();
    }
}

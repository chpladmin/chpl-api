package gov.healthit.chpl.domain.surveillance;

import lombok.Getter;

@Getter
public enum NonconformityClassification {
    CRITERION("CRITERION"),
    REQUIREMENT("REQUIREMENT"),
    UNKNOWN("UNKNOWN");

    private String name;

    private NonconformityClassification(String name) {
        this.name = name;
    }
}

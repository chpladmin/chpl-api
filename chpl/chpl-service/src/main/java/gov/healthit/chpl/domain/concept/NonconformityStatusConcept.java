package gov.healthit.chpl.domain.concept;

import java.io.Serializable;

public enum NonconformityStatusConcept implements Serializable {

    OPEN(
            "Open"
    ),
    CLOSED(
            "Closed"
    );

    private final String name;

    NonconformityStatusConcept(final String input) {
        this.name = input;
    }

    public static NonconformityStatusConcept findByName(final String statusName) {
        NonconformityStatusConcept result = null;
        NonconformityStatusConcept[] availableValues = values();
        for (int i = 0; i < availableValues.length && result == null; i++) {
            if (availableValues[i].getName().equalsIgnoreCase(statusName)
                    || availableValues[i].name().equalsIgnoreCase(statusName)) {
                result = availableValues[i];
            }
        }
        return result;
    }

    public String getName() {
        return name;
    }
}

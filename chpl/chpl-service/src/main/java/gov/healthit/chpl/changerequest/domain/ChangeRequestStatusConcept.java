package gov.healthit.chpl.changerequest.domain;

import java.io.Serializable;

public enum ChangeRequestStatusConcept implements Serializable {
    ACCEPTED(
            "Accepted"
    ),
    PENDING_ONC_ACB_ACTION(
            "Pending ONC-ACB Action"
    ),
    PENDING_DEVELOPER_ACTION(
            "Pending Developer Action"
    ),
    REJECTED(
            "Rejected"
    ),
    CANCELLED_BY_REQUESTER(
            "Cancelled by Requester"
    ),;

    private final String name;

    ChangeRequestStatusConcept(final String input) {
        this.name = input;
    }

    public static ChangeRequestStatusConcept findByName(final String statusName) {
        ChangeRequestStatusConcept result = null;
        ChangeRequestStatusConcept[] availableValues = values();
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

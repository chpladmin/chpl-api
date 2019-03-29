package gov.healthit.chpl.entity;

import org.springframework.util.StringUtils;

/**
 * Enumerated type for statuses a Listing may be in.
 */
public enum CertificationStatusType {
    /** Listing is Active. */
    Active("Active"),
    /** Listing edition has been retired. */
    Retired("Retired"),
    /** Listing was withdrawn from CHPL by the Developer. */
    WithdrawnByDeveloper("Withdrawn by Developer"),
    /** Listing was withdrawn by the Developer while an ONC-ACB has it under surveillance and/or review. */
    WithdrawnByDeveloperUnderReview("Withdrawn by Developer Under Surveillance/Review"),
    /** Listing was withdrawn from CHPL by the owning ONC-ACB. */
    WithdrawnByAcb("Withdrawn by ONC-ACB"),
    /** Listing is suspended, and was suspended by the owning ONC-ACB. */
    SuspendedByAcb("Suspended by ONC-ACB"),
    /** Listing is suspended, and was suspended by ONC. */
    SuspendedByOnc("Suspended by ONC"),
    /** Listing has been terminated on the CHPL. */
    TerminatedByOnc("Terminated by ONC");

    private String name;

    CertificationStatusType() {

    }

    CertificationStatusType(final String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        if (!StringUtils.isEmpty(this.name)) {
            return this.name;
        }
        return name();
    }

    /**
     * Retrieve the Certification Status given a string.
     * @param value the string to search for
     * @return the CertificationStatus
     */
    public static CertificationStatusType getValue(final String value) {
        if (value == null) {
            return null;
        }

        CertificationStatusType result = null;
        CertificationStatusType[] values = CertificationStatusType.values();
        for (int i = 0; i < values.length && result == null; i++) {
            if (value.equalsIgnoreCase(values[i].toString())) {
                result = values[i];
            }
        }
        return result;
    }
}

package gov.healthit.chpl.entity;

import org.springframework.util.StringUtils;

public enum CertificationStatusType {
    Active("Active"),
    Retired("Retired"),
    Pending("Pending"),
    WithdrawnByDeveloper("Withdrawn by Developer"),
    WithdrawnByDeveloperUnderReview("Withdrawn by Developer Under Surveillance/Review"),
    WithdrawnByAcb("Withdrawn by ONC-ACB"),
    SuspendedByAcb("Suspended by ONC-ACB"),
    SuspendedByOnc("Suspended by ONC"),
    TerminatedByOnc("Terminated by ONC");

    private String name;

    CertificationStatusType() {

    }

    CertificationStatusType(String name) {
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

    public static CertificationStatusType getValue(String value) {
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

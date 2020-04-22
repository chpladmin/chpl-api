package gov.healthit.chpl.entity;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.util.StringUtils;

public enum CertificationStatusType {
    Active("Active"),
    Retired("Retired"),
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

    public static List<CertificationStatusType> getActiveAndSuspendedTypes() {
        return Arrays.asList(Active, SuspendedByAcb, SuspendedByOnc);
    }

    public static List<String> getActiveAndSuspendedNames() {
        return getActiveAndSuspendedTypes().stream()
                .map(type -> type.toString())
                .collect(Collectors.toList());
    }
}

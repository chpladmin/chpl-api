package gov.healthit.chpl.entity.developer;

import org.springframework.util.StringUtils;

public enum DeveloperStatusType {
    Active, SuspendedByOnc("Suspended by ONC"), UnderCertificationBanByOnc("Under certification ban by ONC");

    private String name;

    DeveloperStatusType() {

    }

    DeveloperStatusType(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        if (!StringUtils.isEmpty(this.name)) {
            return this.name;
        }
        return name();
    }

    public static DeveloperStatusType getValue(String value) {
        if (value == null) {
            return null;
        }

        DeveloperStatusType result = null;
        DeveloperStatusType[] values = DeveloperStatusType.values();
        for (int i = 0; i < values.length && result == null; i++) {
            if (value.equalsIgnoreCase(values[i].toString())) {
                result = values[i];
            }
        }
        return result;
    }
}

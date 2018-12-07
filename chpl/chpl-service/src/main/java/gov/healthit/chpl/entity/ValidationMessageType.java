package gov.healthit.chpl.entity;

import org.springframework.util.StringUtils;

public enum ValidationMessageType {
    Error, Warning;

    private String name;

    ValidationMessageType() {

    }

    ValidationMessageType(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        if (!StringUtils.isEmpty(this.name)) {
            return this.name;
        }
        return name();
    }

    public static ValidationMessageType getValue(String value) {
        if (value == null) {
            return null;
        }

        ValidationMessageType result = null;
        ValidationMessageType[] values = ValidationMessageType.values();
        for (int i = 0; i < values.length && result == null; i++) {
            if (value.equalsIgnoreCase(values[i].toString())) {
                result = values[i];
            }
        }
        return result;
    }
}

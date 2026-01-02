package gov.healthit.chpl.entity;

import org.apache.commons.lang3.ObjectUtils;

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
        if (!ObjectUtils.isEmpty(this.name)) {
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

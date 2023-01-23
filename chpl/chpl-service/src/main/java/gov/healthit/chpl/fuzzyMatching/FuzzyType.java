package gov.healthit.chpl.fuzzyMatching;

import org.apache.commons.lang3.StringUtils;

public enum FuzzyType {
    UCD_PROCESS("UCD Process"), QMS_STANDARD("QMS Standard"), ACCESSIBILITY_STANDARD("Accessibility Standard");

    private String fuzzyType;

    FuzzyType(String type) {
        this.fuzzyType = type;
    }

    public String fuzzyType() {
        return fuzzyType;
    }

    @Override
    public String toString() {
        if (!StringUtils.isEmpty(this.fuzzyType)) {
            return this.fuzzyType;
        }
        return name();
    }

    public static FuzzyType getValue(String value) {
        if (value == null) {
            return null;
        }

        FuzzyType result = null;
        FuzzyType[] values = FuzzyType.values();
        for (int i = 0; i < values.length && result == null; i++) {
            if (value.equalsIgnoreCase(values[i].toString())) {
                result = values[i];
            }
        }
        return result;
    }
}

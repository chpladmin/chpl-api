package gov.healthit.chpl.realworldtesting.domain;

import org.springframework.util.StringUtils;

public enum RealWorldTestingType {
    PLANS("PLANS"),
    RESULTS("RESULTS");

    private String name;

    RealWorldTestingType() { }

    RealWorldTestingType(String name) {
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

    public static RealWorldTestingType getValue(String value) {
        if (value == null) {
            return null;
        }

        RealWorldTestingType result = null;
        RealWorldTestingType[] values = RealWorldTestingType.values();
        for (int i = 0; i < values.length && result == null; i++) {
            if (value.equalsIgnoreCase(values[i].toString())) {
                result = values[i];
            }
        }
        return result;
    }

}

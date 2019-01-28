package gov.healthit.chpl.domain.concept;

import java.io.Serializable;

public enum PrivacyAndSecurityFrameworkConcept implements Serializable {
    APPROACH_1("Approach 1"), APPROACH_2("Approach 2"), APPROACH_1_AND_2("Approach 1;Approach 2");

    private String name;

    PrivacyAndSecurityFrameworkConcept(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static PrivacyAndSecurityFrameworkConcept getValue(String concept) {
        PrivacyAndSecurityFrameworkConcept result = null;
        PrivacyAndSecurityFrameworkConcept[] possibleValues = values();
        for (int i = 0; i < possibleValues.length && result == null; i++) {
            if (concept.equalsIgnoreCase(possibleValues[i].getName())) {
                result = possibleValues[i];
            }
        }
        return result;
    }

    public static String getFormattedValues() {
        StringBuilder buf = new StringBuilder();
        PrivacyAndSecurityFrameworkConcept[] possibleValues = values();
        for (int i = 0; i < possibleValues.length; i++) {
            if (buf.length() > 0) {
                buf.append(", ");
            }
            buf.append("'").append(possibleValues[i].getName()).append("'");
        }
        return buf.toString();
    }
}

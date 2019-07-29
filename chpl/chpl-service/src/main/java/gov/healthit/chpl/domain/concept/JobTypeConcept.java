package gov.healthit.chpl.domain.concept;

public enum JobTypeConcept {
    MUU_UPLOAD("MUU Upload"),
    SURV_UPLOAD("Surveillance Upload"),
    EXPORT_QUARTERLY("Export Quarterly Report"),
    EXPORT_ANNUAL("Export Annual Report");

    private String name;

    JobTypeConcept(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static JobTypeConcept findByName(final String jobTypeName) {
        JobTypeConcept result = null;
        JobTypeConcept[] availableJobTypes = values();
        for (int i = 0; i < availableJobTypes.length && result == null; i++) {
            if (availableJobTypes[i].getName().equalsIgnoreCase(jobTypeName)
                    || availableJobTypes[i].name().equalsIgnoreCase(jobTypeName)) {
                result = availableJobTypes[i];
            }
        }
        return result;
    }
}

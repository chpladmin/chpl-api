package gov.healthit.chpl.domain.concept;

import java.io.Serializable;

public enum ScheduleFrequencyConcept implements Serializable{
    DAILY("DAILY"),
    HOURLY("HOURLY"),
    WEEKLY("WEEKLY"),
    MONTHLY("MONTHLY");
    
    private String name;
    
    ScheduleFrequencyConcept(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}

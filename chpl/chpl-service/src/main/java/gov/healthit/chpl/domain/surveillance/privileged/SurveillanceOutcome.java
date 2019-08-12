package gov.healthit.chpl.domain.surveillance.privileged;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;

import gov.healthit.chpl.dto.surveillance.report.SurveillanceOutcomeDTO;

public class SurveillanceOutcome implements Serializable {
    private static final long serialVersionUID = 5788880200952752783L;

    /**
     * Surveillance outcome internal ID
     */
    private Long id;

    /**
     * Surveillance outcome name
     */
    private String name;

    public SurveillanceOutcome() {
    }

    public SurveillanceOutcome(final SurveillanceOutcomeDTO dto) {
        this.id = dto.getId();
        this.name = dto.getName();
    }

    /**
     * Checks the id and name fields to determine if the two
     * surveillance outcome fields are the same.
     * Expect one or both fields to be filled in always.
     * @param anotherOutcome
     * @return whether the two objects are the same
     */
    public boolean matches(final SurveillanceOutcome anotherOutcome) {
        if (this.id != null && anotherOutcome.id != null
                && this.id.longValue() == anotherOutcome.id.longValue()) {
            return true;
        } else if (!StringUtils.isEmpty(this.name) && !StringUtils.isEmpty(anotherOutcome.name)
                && this.name.equalsIgnoreCase(anotherOutcome.name)) {
            return true;
        }
        return false;
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }
}

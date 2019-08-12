package gov.healthit.chpl.domain.surveillance.privileged;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;

import gov.healthit.chpl.dto.surveillance.report.SurveillanceProcessTypeDTO;

public class SurveillanceProcessType implements Serializable {
    private static final long serialVersionUID = 5788880200952752783L;

    /**
     * Surveillance type internal ID
     */
    private Long id;

    /**
     * Surveillance type name (randomized, reactive)
     */
    private String name;

    public SurveillanceProcessType() {
    }

    public SurveillanceProcessType(final SurveillanceProcessTypeDTO dto) {
        this.id = dto.getId();
        this.name = dto.getName();
    }

    /**
     * Checks the id and name fields to determine if the two
     * surveillance type fields are the same.
     * Expect one or both fields to be filled in always.
     * @param anotherType
     * @return whether the two objects are the same
     */
    public boolean matches(final SurveillanceProcessType anotherType) {
        if (this.id != null && anotherType.id != null
                && this.id.longValue() == anotherType.id.longValue()) {
            return true;
        } else if (!StringUtils.isEmpty(this.name) && !StringUtils.isEmpty(anotherType.name)
                && this.name.equalsIgnoreCase(anotherType.name)) {
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

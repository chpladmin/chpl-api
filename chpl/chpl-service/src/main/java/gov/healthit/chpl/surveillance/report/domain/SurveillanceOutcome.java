package gov.healthit.chpl.surveillance.report.domain;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;

import gov.healthit.chpl.surveillance.report.dto.SurveillanceOutcomeDTO;
import lombok.Data;

@Data
public class SurveillanceOutcome implements Serializable {
    private static final long serialVersionUID = 5788880200952752783L;

    private Long id;
    private String name;

    public SurveillanceOutcome() {
    }

    public SurveillanceOutcome(SurveillanceOutcomeDTO dto) {
        this.id = dto.getId();
        this.name = dto.getName();
    }

    public boolean matches(SurveillanceOutcome anotherOutcome) {
        if (this.id != null && anotherOutcome.id != null
                && this.id.longValue() == anotherOutcome.id.longValue()) {
            return true;
        } else if (!StringUtils.isEmpty(this.name) && !StringUtils.isEmpty(anotherOutcome.name)
                && this.name.equalsIgnoreCase(anotherOutcome.name)) {
            return true;
        }
        return false;
    }
}

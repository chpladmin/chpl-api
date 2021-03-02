package gov.healthit.chpl.surveillance.report.domain;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;

import gov.healthit.chpl.surveillance.report.dto.SurveillanceProcessTypeDTO;
import lombok.Data;

@Data
public class SurveillanceProcessType implements Serializable {
    private static final long serialVersionUID = 5788880200952752783L;

    private Long id;
    private String name;

    public SurveillanceProcessType() {
    }

    public SurveillanceProcessType(SurveillanceProcessTypeDTO dto) {
        this.id = dto.getId();
        this.name = dto.getName();
    }

    public boolean matches(SurveillanceProcessType anotherType) {
        if (this.id != null && anotherType.id != null
                && this.id.longValue() == anotherType.id.longValue()) {
            return true;
        } else if (!StringUtils.isEmpty(this.name) && !StringUtils.isEmpty(anotherType.name)
                && this.name.equalsIgnoreCase(anotherType.name)) {
            return true;
        }
        return false;
    }
}

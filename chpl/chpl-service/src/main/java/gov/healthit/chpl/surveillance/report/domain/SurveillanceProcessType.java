package gov.healthit.chpl.surveillance.report.domain;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SurveillanceProcessType implements Serializable {
    private static final long serialVersionUID = 5788880200952752783L;

    private Long id;
    private String name;
    private String other;

    public boolean matches(SurveillanceProcessType anotherType) {
        if (this.id != null && anotherType.id != null
                && this.id.longValue() == anotherType.id.longValue()) {
            return true;
        } else if (StringUtils.isEmpty(other) && StringUtils.isEmpty(anotherType.other)
                && !StringUtils.isEmpty(this.name) && !StringUtils.isEmpty(anotherType.name)
                && this.name.equalsIgnoreCase(anotherType.name)) {
            return true;
        } else if (!StringUtils.isEmpty(other) && !StringUtils.isEmpty(anotherType.other)
                && this.other.equalsIgnoreCase(anotherType.other)
                && !StringUtils.isEmpty(this.name) && !StringUtils.isEmpty(anotherType.name)
                && this.name.equalsIgnoreCase(anotherType.name)) {
            return true;
        }
        return false;
    }
}

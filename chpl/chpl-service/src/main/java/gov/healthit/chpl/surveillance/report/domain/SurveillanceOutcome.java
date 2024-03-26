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
public class SurveillanceOutcome implements Serializable {
    private static final long serialVersionUID = 5788880200952752783L;

    private Long id;
    private String name;

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

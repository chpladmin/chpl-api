package gov.healthit.chpl.domain;

import java.io.Serializable;

import org.apache.commons.lang3.ObjectUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MeasureDomain implements Serializable {
    private static final long serialVersionUID = -745253265541448011L;
    private Long id;
    private String name;

    public boolean matches(MeasureDomain anotherDomain) {
        if (this.id == null && anotherDomain.id != null || this.id != null && anotherDomain.id == null) {
            return false;
        } else if (ObjectUtils.allNotNull(this.id, anotherDomain.id)
                && this.id.longValue() != anotherDomain.id.longValue()) {
            return false;
        }

        return true;
    }
}

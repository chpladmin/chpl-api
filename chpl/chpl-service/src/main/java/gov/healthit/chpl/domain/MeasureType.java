package gov.healthit.chpl.domain;

import java.io.Serializable;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MeasureType implements Serializable {
    private static final long serialVersionUID = -8391253265541448011L;
    private Long id;
    private String name;

    public boolean matches(MeasureType anotherType) {
        if (this.id == null && anotherType.id != null || this.id != null && anotherType.id == null) {
            return false;
        } else if (ObjectUtils.allNotNull(this.id, anotherType.id)
                && this.id.longValue() != anotherType.id.longValue()) {
            return false;
        }

        if (this.name == null && anotherType.name != null || this.name != null && anotherType.name == null) {
            return false;
        } else if (ObjectUtils.allNotNull(this.name, anotherType.name)
                && !StringUtils.equalsIgnoreCase(this.name, anotherType.name)) {
            return false;
        }
        return true;
    }
}

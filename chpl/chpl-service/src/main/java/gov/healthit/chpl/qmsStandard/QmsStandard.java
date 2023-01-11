package gov.healthit.chpl.qmsStandard;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The standard or mapping used to meet the quality management system certification criterion.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QmsStandard implements Serializable {
    private static final long serialVersionUID = 7248865611086710891L;

    private Long id;
    private String name;

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (!(other instanceof QmsStandard)) {
            return false;
        }
        QmsStandard otherQmsStandard = (QmsStandard) other;
        if (this.getId() == null) {
            if (otherQmsStandard.getId() != null) {
                return false;
            }
        } else if (!this.getId().equals(otherQmsStandard.getId())) {
            return false;
        }

        if (StringUtils.isEmpty(this.getName())) {
            if (!StringUtils.isEmpty(otherQmsStandard.getName())) {
                return false;
            }
        } else if (!this.getName().equals(otherQmsStandard.getName())) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }
}

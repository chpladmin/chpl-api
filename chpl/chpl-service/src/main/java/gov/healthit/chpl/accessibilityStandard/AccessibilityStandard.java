package gov.healthit.chpl.accessibilityStandard;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A standard  used to meet accessibility-centered design certification criterion.
 * Please see the 2015 Edition Certification Companion Guide for Accessibility Centered
 * Design for example accessibility standards:
 * https://www.healthit.gov/sites/default/files/2015Ed_CCG_g5-Accessibility-centered-design.pdf
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccessibilityStandard implements Serializable {
    private static final long serialVersionUID = 7248865611086710891L;

    private Long id;
    private String name;

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (!(other instanceof AccessibilityStandard)) {
            return false;
        }
        AccessibilityStandard otherAccessibilityStandard = (AccessibilityStandard) other;
        if (this.getId() == null) {
            if (otherAccessibilityStandard.getId() != null) {
                return false;
            }
        } else if (!this.getId().equals(otherAccessibilityStandard.getId())) {
            return false;
        }

        if (StringUtils.isEmpty(this.getName())) {
            if (!StringUtils.isEmpty(otherAccessibilityStandard.getName())) {
                return false;
            }
        } else if (!this.getName().equals(otherAccessibilityStandard.getName())) {
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

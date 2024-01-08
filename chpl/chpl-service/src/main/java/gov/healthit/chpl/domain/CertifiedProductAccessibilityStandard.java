package gov.healthit.chpl.domain;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.dto.CertifiedProductAccessibilityStandardDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CertifiedProductAccessibilityStandard implements Serializable {
    private static final long serialVersionUID = -676179466407109456L;

    @Schema(description = "Accessibility standard to listing mapping internal ID")
    private Long id;

    @Schema(description = "Accessibility standard internal ID")
    private Long accessibilityStandardId;

    @Schema(description = "Accessibility standard name")
    private String accessibilityStandardName;

    @JsonIgnore
    private String userEnteredAccessibilityStandardName;

    public CertifiedProductAccessibilityStandard(CertifiedProductAccessibilityStandardDTO dto) {
        this.id = dto.getId();
        this.accessibilityStandardId = dto.getAccessibilityStandardId();
        this.accessibilityStandardName = dto.getAccessibilityStandardName();
    }

    public boolean matches(CertifiedProductAccessibilityStandard other) {
        boolean result = false;
        if (this.getAccessibilityStandardId() != null && other.getAccessibilityStandardId() != null
                && this.getAccessibilityStandardId().longValue() == other.getAccessibilityStandardId().longValue()) {
            result = true;
        } else if (!StringUtils.isEmpty(this.getAccessibilityStandardName())
                && !StringUtils.isEmpty(other.getAccessibilityStandardName())
                && this.getAccessibilityStandardName().equals(other.getAccessibilityStandardName())) {
            result = true;
        }
        return result;
    }
}

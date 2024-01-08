package gov.healthit.chpl.domain;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.dto.CertificationResultTestStandardDTO;
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
public class CertificationResultTestStandard implements Serializable {
    private static final long serialVersionUID = -9182555768595891414L;

    @Schema(description = "Test standard to certification result mapping internal ID")
    private Long id;

    @Schema(description = "Test standard internal ID")
    private Long testStandardId;

    @Schema(description = "Description of test standard")
    private String testStandardDescription;

    @Schema(description = "Name of test standard")
    private String testStandardName;

    public CertificationResultTestStandard(CertificationResultTestStandardDTO dto) {
        this.id = dto.getId();
        this.testStandardId = dto.getTestStandardId();
        this.testStandardDescription = dto.getTestStandardDescription();
        this.testStandardName = dto.getTestStandardName();
    }

    public boolean matches(CertificationResultTestStandard anotherStd) {
        boolean result = false;
        if (this.getTestStandardId() != null && anotherStd.getTestStandardId() != null
                && this.getTestStandardId().longValue() == anotherStd.getTestStandardId().longValue()) {
            result = true;
        } else if (!StringUtils.isEmpty(this.getTestStandardName())
                && !StringUtils.isEmpty(anotherStd.getTestStandardName())
                && this.getTestStandardName().equalsIgnoreCase(anotherStd.getTestStandardName())) {
            result = true;
        }
        return result;
    }
}

package gov.healthit.chpl.domain;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.dto.CertificationResultTestProcedureDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificationResultTestProcedure implements Serializable {
    private static final long serialVersionUID = -8648559250833503194L;

    @Schema(description = "Test Procedure to certification result mapping internal ID")
    private Long id;

    @Schema(description = "This variable explains the test procedure being used to test the associated criteria.")
    private TestProcedure testProcedure;

    @Schema(description = "The test procedure version used for a given certification criteria. This "
            + "variable is a string variable that does not take any restrictions on formatting or values.")
    private String testProcedureVersion;

    public CertificationResultTestProcedure(CertificationResultTestProcedureDTO dto) {
        this.id = dto.getId();
        TestProcedure tp = new TestProcedure();
        if (dto.getTestProcedure() == null) {
            tp.setId(dto.getTestProcedureId());
        } else {
            tp.setId(dto.getTestProcedure().getId());
            tp.setName(dto.getTestProcedure().getName());
        }
        this.testProcedure = tp;
        this.testProcedureVersion = dto.getVersion();
    }

    public boolean matches(final CertificationResultTestProcedure anotherProc) {
        boolean result = false;
        if (this.getTestProcedure() != null && anotherProc.getTestProcedure() != null
                && this.getTestProcedure().getId() != null && anotherProc.getTestProcedure().getId() != null
                && this.getTestProcedure().getId().longValue() == anotherProc.getTestProcedure().getId().longValue()
                && !StringUtils.isEmpty(this.getTestProcedureVersion())
                && !StringUtils.isEmpty(anotherProc.getTestProcedureVersion())
                && this.getTestProcedureVersion().equalsIgnoreCase(anotherProc.getTestProcedureVersion())) {
            result = true;
        }
        return result;
    }
}

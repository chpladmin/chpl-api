package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.LinkedHashSet;

import org.apache.commons.lang3.ObjectUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@Data
@ToString
@AllArgsConstructor
public class Measure implements Serializable {
    private static final long serialVersionUID = 3070401446291821552L;

    @Schema(description = "An internal ID for each valid measure")
    private Long id;

    private MeasureDomain domain;

    @Schema(description = "Abbreviation of the Required Test. Examples are \"RT7\" or \"RT9\"")
    private String abbreviation;

    @Schema(description = "The required test associated with each measure. For example, \"Required Test 10: Stage 2 Objective 3 Measure 1 and "
            + "Stage 3 Objective 4 Measure 1\"")
    private String requiredTest;

    @Schema(description = "The name of the measure. For example, \"Computerized Provider Order Entry - Medications: Eligible Hospital/Critical\"")
    private String name;

    @Schema(description = "Whether or not this measure requires criteria to be designated as associated with it.")
    private Boolean requiresCriteriaSelection;

    @Schema(description = "A flag indicating whether or not the measure has been marked as removed.")
    private Boolean removed;

    @Builder.Default
    private LinkedHashSet<CertificationCriterion> allowedCriteria = new LinkedHashSet<CertificationCriterion>();

    public Measure() {
        super();
    }

    // not overriding equals on purpose
    // this is meant to determine if a user would think two measures
    // are the same, not as thorough as equals
    public boolean matches(Measure anotherMeasure) {
        if (this.id == null && anotherMeasure.id == null) {
            return false;
        } else if (this.id == null && anotherMeasure.id != null || this.id != null && anotherMeasure.id == null) {
            return false;
        } else if (ObjectUtils.allNotNull(this.id, anotherMeasure.id)
                && this.id.longValue() != anotherMeasure.id.longValue()) {
            return false;
        }

        return true;
    }
}

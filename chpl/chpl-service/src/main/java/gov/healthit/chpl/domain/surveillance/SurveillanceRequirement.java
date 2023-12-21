package gov.healthit.chpl.domain.surveillance;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.util.NullSafeEvaluator;
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
public class SurveillanceRequirement implements Serializable {
    private static final long serialVersionUID = -5658812197618734286L;

    @Schema(description = "Surveilled requirement internal ID")
    private Long id;

    @Schema(description = "For a given surveillance activity, details about the requirement and "
            + "the type of requirement being surveilled")
    private RequirementType requirementType;

    @Schema(description = "When the requirement type is \"Other\", the value of the requirement type")
    private String requirementTypeOther;

    @Schema(description = "The result for surveillance conducted on each surveillance requirement.",
            allowableValues = {"Non-Conformity", "No Non-Conformity"})
    private SurveillanceResultType result;

    @Schema(description = "List of nonconformities found for this surveilled requirement")
    @Builder.Default
    private List<SurveillanceNonconformity> nonconformities = new ArrayList<SurveillanceNonconformity>();

    public boolean matches(SurveillanceRequirement anotherRequirement) {
        if (!propertiesMatch(anotherRequirement)) {
            return false;
        }

        if (this.nonconformities == null && anotherRequirement.nonconformities != null
                || this.nonconformities != null && anotherRequirement.nonconformities == null) {
            return false;
        } else if (this.nonconformities != null && anotherRequirement.nonconformities != null
                && this.nonconformities.size() != anotherRequirement.nonconformities.size()) {
            //easy check if the sizes are different
            return false;
        } else {
            //nonconformities - were any removed?
            for (SurveillanceNonconformity thisNc : this.nonconformities) {
                boolean foundInOtherRequirement = false;
                for (SurveillanceNonconformity otherNc : anotherRequirement.nonconformities) {
                    if (thisNc.getId() != null && otherNc.getId() != null
                            && thisNc.getId().longValue() == otherNc.getId().longValue()) {
                        foundInOtherRequirement = true;
                    }
                }
                if (!foundInOtherRequirement) {
                    return false;
                }
            }
            //nonconformities - were any added?
            for (SurveillanceNonconformity otherNc : anotherRequirement.nonconformities) {
                boolean foundInThisRequirement = false;
                for (SurveillanceNonconformity thisNc : this.nonconformities) {
                    if (thisNc.getId().longValue() == otherNc.getId().longValue()) {
                        foundInThisRequirement = true;
                    }
                }
                if (!foundInThisRequirement) {
                    return false;
                }
            }
            //nonconformities - were any changed?
            for (SurveillanceNonconformity otherNc : anotherRequirement.nonconformities) {
                for (SurveillanceNonconformity thisNc : this.nonconformities) {
                    if (thisNc.getId().longValue() == otherNc.getId().longValue()) {
                        if (!thisNc.matches(otherNc)) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    public boolean propertiesMatch(SurveillanceRequirement anotherRequirement) {
        if (this.id == null && anotherRequirement.id != null
                || this.id != null && anotherRequirement.id == null) {
            return false;
        } else if (this.id != null && anotherRequirement.id != null
                && this.id.longValue() != anotherRequirement.id.longValue()) {
            return false;
        }

        if (!doRequirementTypesMatch(anotherRequirement)
                || !doResultTypesMatch(anotherRequirement)) {
            return false;
        }
        return true;
    }

    public boolean doRequirementTypesMatch(SurveillanceRequirement anotherRequirement) {
        return NullSafeEvaluator.eval(() -> this.getRequirementType().getId(), 0L).equals(
                NullSafeEvaluator.eval(() -> anotherRequirement.getRequirementType().getId(), 0L))
                && NullSafeEvaluator.eval(() -> this.getRequirementTypeOther(), "").equals(
                        NullSafeEvaluator.eval(() -> anotherRequirement.getRequirementTypeOther(), ""));
    }

    public boolean doResultTypesMatch(SurveillanceRequirement anotherRequirement) {
        return NullSafeEvaluator.eval(() -> this.getResult().getId(), 0L).equals(
                NullSafeEvaluator.eval(() -> anotherRequirement.getResult().getId(), 0L));
    }
}

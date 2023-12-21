package gov.healthit.chpl.domain.surveillance;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import gov.healthit.chpl.activity.ActivityExclude;
import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.util.LocalDateDeserializer;
import gov.healthit.chpl.util.LocalDateSerializer;
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
public class Surveillance implements Serializable {
    private static final long serialVersionUID = 7018071250912371691L;

    @Schema(description = "Surveillance internal ID")
    private Long id;

    private String surveillanceIdToReplace;

    @Schema(description = "The user-friendly ID of this surveillance relative to a listing. Ex: SURV01")
    private String friendlyId;

    @Schema(description = "The listing under surveillance")
    private CertifiedProduct certifiedProduct;

    @Schema(description = "Day surveillance began")
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate startDay;

    @Schema(description = "Day surveillance ended")
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate endDay;

    @Schema(description = "The type of surveillance conducted. Allowable values are \"Reactive\" or \"Randomized\".")
    private SurveillanceType type;

    @Schema(description = "Number of randomized sites used. Only applicable for randomized surveillance.")
    private Integer randomizedSitesUsed;

    @Schema(description = "For a given surveillance activity, the certification criteria or program "
            + "requirement being surveilled. Where applicable, the surveillance "
            + "requirement will be presented as the regulation text number (e.g. "
            + "170.315(a)(2) or 170.315(k)(1)). However, other values are allowed to "
            + "provide a brief description of the surveilled requirement.")
    @Builder.Default
    private LinkedHashSet<SurveillanceRequirement> requirements = new LinkedHashSet<SurveillanceRequirement>();

    @Builder.Default
    @ActivityExclude
    private Set<String> errorMessages = new HashSet<String>();

    @Builder.Default
    @ActivityExclude
    private Set<String> warningMessages = new HashSet<String>();

    @Schema(description = "Date of the last modification of the surveillance.")
    private Date lastModifiedDate;

    /**
     * Determines if this surveillance matches another surveillance.
     * Not overriding equals and hashCode out of fear of messing something up.
     * @param anotherSurveillance
     * @return whether the two surveillance objects are the same
     */
    public boolean matches(Surveillance anotherSurveillance) {
        if (!propertiesMatch(anotherSurveillance)) {
            return false;
        }

        if (this.requirements == null && anotherSurveillance.requirements != null
                || this.requirements != null && anotherSurveillance.requirements == null) {
            return false;
        } else if (this.requirements != null && anotherSurveillance.requirements != null
                && this.requirements.size() != anotherSurveillance.requirements.size()) {
            // easy check if the sizes are different
            return false;
        } else {
            // surveillance requirements - were any removed?
            for (SurveillanceRequirement thisReq : this.requirements) {
                boolean foundInOtherSurveillance = false;
                for (SurveillanceRequirement otherReq : anotherSurveillance.requirements) {
                    if (thisReq.getId() != null && otherReq.getId() != null
                            && thisReq.getId().longValue() == otherReq.getId().longValue()) {
                        foundInOtherSurveillance = true;
                    }
                }
                if (!foundInOtherSurveillance) {
                    return false;
                }
            }
            // surveillance requirements - were any added?
            for (SurveillanceRequirement otherReq : anotherSurveillance.requirements) {
                boolean foundInThisSurveillance = false;
                for (SurveillanceRequirement thisReq : this.requirements) {
                    if (thisReq.getId().longValue() == otherReq.getId().longValue()) {
                        foundInThisSurveillance = true;
                    }
                }
                if (!foundInThisSurveillance) {
                    return false;
                }
            }
            // surveillance requirements - were any changed?
            for (SurveillanceRequirement otherReq : anotherSurveillance.requirements) {
                for (SurveillanceRequirement thisReq : this.requirements) {
                    if (thisReq.getId().longValue() == otherReq.getId().longValue()) {
                        if (!thisReq.matches(otherReq)) {
                            return false;
                        }
                    }
                }
            }
        }
        // all checks passed and turned out to be matching
        // so the two surveillances must be identical
        return true;
    }

    public boolean propertiesMatch(Surveillance anotherSurveillance) {
        if (this.id == null && anotherSurveillance.id != null || this.id != null && anotherSurveillance.id == null) {
            return false;
        } else if (this.id != null && anotherSurveillance.id != null
                && this.id.longValue() != anotherSurveillance.id.longValue()) {
            return false;
        }
        if (StringUtils.isEmpty(this.friendlyId) && !StringUtils.isEmpty(anotherSurveillance.friendlyId)
                || !StringUtils.isEmpty(this.friendlyId) && StringUtils.isEmpty(anotherSurveillance.friendlyId)) {
            return false;
        } else if (!StringUtils.isEmpty(this.friendlyId) && !StringUtils.isEmpty(anotherSurveillance.friendlyId)
                && !this.friendlyId.equalsIgnoreCase(anotherSurveillance.friendlyId)) {
            return false;
        }
        if (this.startDay == null && anotherSurveillance.startDay != null
                || this.startDay != null && anotherSurveillance.startDay == null) {
            return false;
        } else if (this.startDay != null && anotherSurveillance.startDay != null
                && !this.startDay.equals(anotherSurveillance.startDay)) {
            return false;
        }
        if (this.endDay == null && anotherSurveillance.endDay != null
                || this.endDay != null && anotherSurveillance.endDay == null) {
            return false;
        } else if (this.endDay != null && anotherSurveillance.endDay != null
                && !this.endDay.equals(anotherSurveillance.endDay)) {
            return false;
        }
        if (this.randomizedSitesUsed == null && anotherSurveillance.randomizedSitesUsed != null
                || this.randomizedSitesUsed != null && anotherSurveillance.randomizedSitesUsed == null) {
            return false;
        } else if (this.randomizedSitesUsed != null && anotherSurveillance.randomizedSitesUsed != null
                && this.randomizedSitesUsed.intValue() != anotherSurveillance.randomizedSitesUsed.intValue()) {
            return false;
        }
        if (this.certifiedProduct == null && anotherSurveillance.certifiedProduct != null
                || this.certifiedProduct != null && anotherSurveillance.certifiedProduct == null) {
            return false;
        } else if (this.certifiedProduct != null && anotherSurveillance.certifiedProduct != null
                && !this.certifiedProduct.matches(anotherSurveillance.certifiedProduct)) {
            return false;
        }
        if (this.type == null && anotherSurveillance.type != null
                || this.type != null && anotherSurveillance.type == null) {
            return false;
        } else if (this.type != null && anotherSurveillance.type != null
                && !this.type.matches(anotherSurveillance.type)) {
            return false;
        }
        return true;
    }
}

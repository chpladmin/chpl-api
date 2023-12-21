package gov.healthit.chpl.domain.surveillance;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import gov.healthit.chpl.domain.NonconformityType;
import gov.healthit.chpl.util.LocalDateDeserializer;
import gov.healthit.chpl.util.LocalDateSerializer;
import gov.healthit.chpl.util.NullSafeEvaluator;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SurveillanceNonconformity implements Serializable {
    private static final long serialVersionUID = -1116153210791576784L;

    @Schema(description = "Non-conformity internal ID")
    private Long id;

    @Schema(description = "Type of non-conformity; this is either a certification criteria number or "
            + "a textual description")
    private NonconformityType type;

    @Schema(description = "The status of a non-conformity found as a result of a surveillance activity.",
            allowableValues = {"Open", "Closed"})
    private String nonconformityStatus;

    @Schema(description = "Date of determination of non-conformity")
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate dateOfDeterminationDay;

    @Schema(description = "Corrective action plan approval day")
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate capApprovalDay;

    @Schema(description = "Corrective action plan start day")
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate capStartDay;

    @Schema(description = "Corrective action plan end day")
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate capEndDay;

    @Schema(description = "Corrective action plan must complete date")
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate capMustCompleteDay;

    @Schema(description = "Date non-conformity was closed")
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate nonconformityCloseDay;

    @Schema(description = "Non-conformity summary")
    private String summary;

    @Schema(description = "Non-conformity findings.")
    private String findings;

    @Schema(description = "Number of sites passed")
    private Integer sitesPassed;

    @Schema(description = "Total number of sites tested")
    private Integer totalSites;

    @Schema(description = "Developer explanation for the non-conformity")
    private String developerExplanation;

    @Schema(description = "Resolution description of the non-conformity")
    private String resolution;

    @Schema(description = "Date of the last modification of the surveillance.")
    private Date lastModifiedDate;

    public boolean matches(SurveillanceNonconformity anotherNonconformity) {
        if (!propertiesMatch(anotherNonconformity)) {
            return false;
        }

        //all checks passed and turned out to be matching
        //so the two non-conformities must be identical
        return true;
    }

    public boolean propertiesMatch(SurveillanceNonconformity anotherNonconformity) {
        if (this.id == null && anotherNonconformity.id != null
                || this.id != null && anotherNonconformity.id == null) {
            return false;
        } else if (this.id != null && anotherNonconformity.id != null
                && this.id.longValue() != anotherNonconformity.id.longValue()) {
            return false;
        }
        if (!Objects.equals(
                NullSafeEvaluator.eval(() -> this.type.getId(), null),
                NullSafeEvaluator.eval(() -> anotherNonconformity.getType().getId(), null))) {
            return false;
        }
        if (this.dateOfDeterminationDay == null && anotherNonconformity.dateOfDeterminationDay != null
                || this.dateOfDeterminationDay != null && anotherNonconformity.dateOfDeterminationDay == null) {
            return false;
        } else if (this.dateOfDeterminationDay != null && anotherNonconformity.dateOfDeterminationDay != null
                && !this.dateOfDeterminationDay.equals(anotherNonconformity.dateOfDeterminationDay)) {
            return false;
        }
        if (this.capApprovalDay == null && anotherNonconformity.capApprovalDay != null
                || this.capApprovalDay != null && anotherNonconformity.capApprovalDay == null) {
            return false;
        } else if (this.capApprovalDay != null && anotherNonconformity.capApprovalDay != null
                && !this.capApprovalDay.equals(anotherNonconformity.capApprovalDay)) {
            return false;
        }
        if (this.capStartDay == null && anotherNonconformity.capStartDay != null
                || this.capStartDay != null && anotherNonconformity.capStartDay == null) {
            return false;
        } else if (this.capStartDay != null && anotherNonconformity.capStartDay != null
                && !this.capStartDay.equals(anotherNonconformity.capStartDay)) {
            return false;
        }
        if (this.capEndDay == null && anotherNonconformity.capEndDay != null
                || this.capEndDay != null && anotherNonconformity.capEndDay == null) {
            return false;
        } else if (this.capEndDay != null && anotherNonconformity.capEndDay != null
                && !this.capEndDay.equals(anotherNonconformity.capEndDay)) {
            return false;
        }
        if (this.capMustCompleteDay == null && anotherNonconformity.capMustCompleteDay != null
                || this.capMustCompleteDay != null && anotherNonconformity.capMustCompleteDay == null) {
            return false;
        } else if (this.capMustCompleteDay != null && anotherNonconformity.capMustCompleteDay != null
                && !this.capMustCompleteDay.equals(anotherNonconformity.capMustCompleteDay)) {
            return false;
        }
        if (this.nonconformityCloseDay == null && anotherNonconformity.nonconformityCloseDay != null
                || this.nonconformityCloseDay != null && anotherNonconformity.nonconformityCloseDay == null) {
            return false;
        } else if (this.nonconformityCloseDay != null && anotherNonconformity.nonconformityCloseDay != null
                && !this.nonconformityCloseDay.equals(anotherNonconformity.nonconformityCloseDay)) {
            return false;
        }
        if (StringUtils.isEmpty(this.summary) && !StringUtils.isEmpty(anotherNonconformity.summary)
                || !StringUtils.isEmpty(this.summary) && StringUtils.isEmpty(anotherNonconformity.summary)) {
            return false;
        } else if (!StringUtils.isEmpty(this.summary) && !StringUtils.isEmpty(anotherNonconformity.summary)
                && !this.summary.equals(anotherNonconformity.summary)) {
            return false;
        }
        if (StringUtils.isEmpty(this.findings) && !StringUtils.isEmpty(anotherNonconformity.findings)
                || !StringUtils.isEmpty(this.findings) && StringUtils.isEmpty(anotherNonconformity.findings)) {
            return false;
        } else if (!StringUtils.isEmpty(this.findings) && !StringUtils.isEmpty(anotherNonconformity.findings)
                && !this.findings.equals(anotherNonconformity.findings)) {
            return false;
        }
        if (this.sitesPassed == null && anotherNonconformity.sitesPassed != null
                || this.sitesPassed != null && anotherNonconformity.sitesPassed == null) {
            return false;
        } else if (this.sitesPassed != null && anotherNonconformity.sitesPassed != null
                && this.sitesPassed.intValue() != anotherNonconformity.sitesPassed.intValue()) {
            return false;
        }
        if (this.totalSites == null && anotherNonconformity.totalSites != null
                || this.totalSites != null && anotherNonconformity.totalSites == null) {
            return false;
        } else if (this.totalSites != null && anotherNonconformity.totalSites != null
                && this.totalSites.intValue() != anotherNonconformity.totalSites.intValue()) {
            return false;
        }
        if (StringUtils.isEmpty(this.developerExplanation) && !StringUtils.isEmpty(anotherNonconformity.developerExplanation)
                || !StringUtils.isEmpty(this.developerExplanation) && StringUtils.isEmpty(anotherNonconformity.developerExplanation)) {
            return false;
        } else if (!StringUtils.isEmpty(this.developerExplanation) && !StringUtils.isEmpty(anotherNonconformity.developerExplanation)
                && !this.developerExplanation.equalsIgnoreCase(anotherNonconformity.developerExplanation)) {
            return false;
        }
        if (StringUtils.isEmpty(this.resolution) && !StringUtils.isEmpty(anotherNonconformity.resolution)
                || !StringUtils.isEmpty(this.resolution) && StringUtils.isEmpty(anotherNonconformity.resolution)) {
            return false;
        } else if (!StringUtils.isEmpty(this.resolution) && !StringUtils.isEmpty(anotherNonconformity.resolution)
                && !this.resolution.equalsIgnoreCase(anotherNonconformity.resolution)) {
            return false;
        }
        if (this.lastModifiedDate == null && anotherNonconformity.lastModifiedDate != null
                || this.lastModifiedDate != null && anotherNonconformity.lastModifiedDate == null) {
            return false;
        } else if (this.lastModifiedDate != null && anotherNonconformity.lastModifiedDate != null
                && this.lastModifiedDate.getTime() != anotherNonconformity.lastModifiedDate.getTime()) {
            return false;
        }
        return true;
    }
}

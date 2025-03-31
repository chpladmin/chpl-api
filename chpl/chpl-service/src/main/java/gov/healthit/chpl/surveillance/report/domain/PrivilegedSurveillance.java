package gov.healthit.chpl.surveillance.report.domain;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import gov.healthit.chpl.api.deprecatedUsage.DeprecatedResponseField;
import gov.healthit.chpl.domain.surveillance.SurveillanceBasic;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PrivilegedSurveillance extends SurveillanceBasic {
    private static final long serialVersionUID = 2839806198924296871L;

    @JsonIgnore
    private Long mappingId;
    @JsonIgnore
    private QuarterlyReport quarterlyReport;
    private SurveillanceOutcome surveillanceOutcome;
    private String surveillanceOutcomeOther;
    @Builder.Default
    private List<SurveillanceProcessType> surveillanceProcessTypes = new ArrayList<SurveillanceProcessType>();
    private String surveillanceProcessTypeOther;
    @Builder.Default
    private List<SurveillanceGroundsForInitiating> surveillanceGroundsForInitiating = new ArrayList<SurveillanceGroundsForInitiating>();
    private String surveillanceGroundsForInitiatingOther;
    @Deprecated
    @DeprecatedResponseField(message = "Please use surveillanceGroundsForInitiating and surveillanceGroundsForInitiatingOther.",
        removalDate = "2025-10-01")
    private String groundsForInitiating;
    private String surveillanceFindings;
    private Boolean k1Reviewed;
    private String nonconformityCauses;
    private String nonconformityNature;
    private String stepsToSurveil;
    private String stepsToEngage;
    private String additionalCostsEvaluation;
    private String limitationsEvaluation;
    private String nondisclosureEvaluation;
    private String directionDeveloperResolution;

    @Builder.Default
    private List<SurveillanceCapStatus> capStatuses = new ArrayList<SurveillanceCapStatus>();
    private String capStatusOther;
    @Deprecated
    @DeprecatedResponseField(message = "Please use capStatus and capStatusOther.", removalDate = "2025-10-01")
    private String completedCapVerification;

    public void copyPrivilegedFields(PrivilegedSurveillance another) {
        this.mappingId = another.getMappingId();
        this.k1Reviewed = another.getK1Reviewed();
        this.nonconformityCauses = another.getNonconformityCauses();
        this.nonconformityNature = another.getNonconformityNature();
        this.stepsToSurveil = another.getStepsToSurveil();
        this.stepsToEngage = another.getStepsToEngage();
        this.additionalCostsEvaluation = another.getAdditionalCostsEvaluation();
        this.limitationsEvaluation = another.getLimitationsEvaluation();
        this.nondisclosureEvaluation = another.getNondisclosureEvaluation();
        this.directionDeveloperResolution = another.getDirectionDeveloperResolution();
        this.quarterlyReport = another.getQuarterlyReport();
        this.surveillanceOutcome = another.getSurveillanceOutcome();
        this.surveillanceOutcomeOther = another.getSurveillanceOutcomeOther();
        this.surveillanceProcessTypes = new ArrayList<SurveillanceProcessType>(
                another.getSurveillanceProcessTypes());
        this.surveillanceProcessTypeOther = another.getSurveillanceProcessTypeOther();
        this.surveillanceGroundsForInitiating = new ArrayList<SurveillanceGroundsForInitiating>(
                another.getSurveillanceGroundsForInitiating());
        this.surveillanceGroundsForInitiatingOther = another.getSurveillanceGroundsForInitiatingOther();
        this.capStatuses = new ArrayList<SurveillanceCapStatus>(another.getCapStatuses());
        this.capStatusOther = another.getCapStatusOther();
        this.surveillanceFindings = another.getSurveillanceFindings();
    }
}

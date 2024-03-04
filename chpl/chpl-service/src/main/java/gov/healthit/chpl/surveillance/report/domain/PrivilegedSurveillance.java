package gov.healthit.chpl.surveillance.report.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import gov.healthit.chpl.domain.surveillance.SurveillanceBasic;
import gov.healthit.chpl.dto.surveillance.SurveillanceBasicDTO;
import lombok.AllArgsConstructor;
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
    private SurveillanceProcessType surveillanceProcessType;
    private String surveillanceProcessTypeOther;
    private Boolean k1Reviewed;
    private String groundsForInitiating;
    private String nonconformityCauses;
    private String nonconformityNature;
    private String stepsToSurveil;
    private String stepsToEngage;
    private String additionalCostsEvaluation;
    private String limitationsEvaluation;
    private String nondisclosureEvaluation;
    private String directionDeveloperResolution;
    private String completedCapVerification;

    public PrivilegedSurveillance(SurveillanceBasicDTO dto) {
        super(dto);
    }

    public void clearPrivilegedFields() {
        this.mappingId = null;
        this.k1Reviewed = null;
        this.groundsForInitiating = null;
        this.nonconformityCauses = null;
        this.nonconformityNature = null;
        this.stepsToSurveil = null;
        this.stepsToEngage = null;
        this.additionalCostsEvaluation = null;
        this.limitationsEvaluation = null;
        this.nondisclosureEvaluation = null;
        this.directionDeveloperResolution = null;
        this.completedCapVerification = null;
        this.quarterlyReport = null;
        this.surveillanceOutcome = null;
        this.surveillanceOutcomeOther = null;
        this.surveillanceProcessType = null;
        this.surveillanceProcessTypeOther = null;
    }

    public void copyPrivilegedFields(PrivilegedSurveillance another) {
        this.mappingId = another.getMappingId();
        this.k1Reviewed = another.getK1Reviewed();
        this.groundsForInitiating = another.getGroundsForInitiating();
        this.nonconformityCauses = another.getNonconformityCauses();
        this.nonconformityNature = another.getNonconformityNature();
        this.stepsToSurveil = another.getStepsToSurveil();
        this.stepsToEngage = another.getStepsToEngage();
        this.additionalCostsEvaluation = another.getAdditionalCostsEvaluation();
        this.limitationsEvaluation = another.getLimitationsEvaluation();
        this.nondisclosureEvaluation = another.getNondisclosureEvaluation();
        this.directionDeveloperResolution = another.getDirectionDeveloperResolution();
        this.completedCapVerification = another.getCompletedCapVerification();
        this.quarterlyReport = another.getQuarterlyReport();
        this.surveillanceOutcome = another.getSurveillanceOutcome();
        this.surveillanceOutcomeOther = another.getSurveillanceOutcomeOther();
        this.surveillanceProcessType = another.getSurveillanceProcessType();
        this.surveillanceProcessTypeOther = another.getSurveillanceProcessTypeOther();
    }
}

package gov.healthit.chpl.domain.surveillance.privileged;

import gov.healthit.chpl.domain.surveillance.SurveillanceBasic;
import gov.healthit.chpl.dto.surveillance.report.QuarterlyReportSurveillanceMapDTO;

public class PrivilegedSurveillance extends SurveillanceBasic {
    private static final long serialVersionUID = 2839806198924296871L;

    private SurveillanceOutcome surveillanceOutcome;
    private SurveillanceProcessType surveillanceProcessType;
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

    public PrivilegedSurveillance() {
        super();
    }

    public PrivilegedSurveillance(final QuarterlyReportSurveillanceMapDTO dto) {
        super(dto);
        this.k1Reviewed = dto.getK1Reviewed();
        this.groundsForInitiating = dto.getGroundsForInitiating();
        this.nonconformityCauses = dto.getNonconformityCauses();
        this.nonconformityNature = dto.getNonconformityNature();
        this.stepsToSurveil = dto.getStepsToSurveil();
        this.stepsToEngage = dto.getStepsToEngage();
        this.additionalCostsEvaluation = dto.getAdditionalCostsEvaluation();
        this.limitationsEvaluation = dto.getLimitationsEvaluation();
        this.nondisclosureEvaluation = dto.getNondisclosureEvaluation();
        this.directionDeveloperResolution = dto.getDirectionDeveloperResolution();
        this.completedCapVerification = dto.getCompletedCapVerification();
        if (dto.getSurveillanceOutcome() != null) {
            this.surveillanceOutcome = new SurveillanceOutcome(dto.getSurveillanceOutcome());
        }
        if (dto.getSurveillanceProcessType() != null) {
            this.surveillanceProcessType = new SurveillanceProcessType(dto.getSurveillanceProcessType());
        }
    }

    public SurveillanceOutcome getSurveillanceOutcome() {
        return surveillanceOutcome;
    }

    public void setSurveillanceOutcome(SurveillanceOutcome surveillanceOutcome) {
        this.surveillanceOutcome = surveillanceOutcome;
    }

    public SurveillanceProcessType getSurveillanceProcessType() {
        return surveillanceProcessType;
    }

    public void setSurveillanceProcessType(SurveillanceProcessType surveillanceProcessType) {
        this.surveillanceProcessType = surveillanceProcessType;
    }

    public Boolean getK1Reviewed() {
        return k1Reviewed;
    }

    public void setK1Reviewed(Boolean k1Reviewed) {
        this.k1Reviewed = k1Reviewed;
    }

    public String getGroundsForInitiating() {
        return groundsForInitiating;
    }

    public void setGroundsForInitiating(String groundsForInitiating) {
        this.groundsForInitiating = groundsForInitiating;
    }

    public String getNonconformityCauses() {
        return nonconformityCauses;
    }

    public void setNonconformityCauses(String nonconformityCauses) {
        this.nonconformityCauses = nonconformityCauses;
    }

    public String getNonconformityNature() {
        return nonconformityNature;
    }

    public void setNonconformityNature(String nonconformityNature) {
        this.nonconformityNature = nonconformityNature;
    }

    public String getStepsToSurveil() {
        return stepsToSurveil;
    }

    public void setStepsToSurveil(String stepsToSurveil) {
        this.stepsToSurveil = stepsToSurveil;
    }

    public String getStepsToEngage() {
        return stepsToEngage;
    }

    public void setStepsToEngage(String stepsToEngage) {
        this.stepsToEngage = stepsToEngage;
    }

    public String getAdditionalCostsEvaluation() {
        return additionalCostsEvaluation;
    }

    public void setAdditionalCostsEvaluation(String additionalCostsEvaluation) {
        this.additionalCostsEvaluation = additionalCostsEvaluation;
    }

    public String getLimitationsEvaluation() {
        return limitationsEvaluation;
    }

    public void setLimitationsEvaluation(String limitationsEvaluation) {
        this.limitationsEvaluation = limitationsEvaluation;
    }

    public String getNondisclosureEvaluation() {
        return nondisclosureEvaluation;
    }

    public void setNondisclosureEvaluation(String nondisclosureEvaluation) {
        this.nondisclosureEvaluation = nondisclosureEvaluation;
    }

    public String getDirectionDeveloperResolution() {
        return directionDeveloperResolution;
    }

    public void setDirectionDeveloperResolution(String directionDeveloperResolution) {
        this.directionDeveloperResolution = directionDeveloperResolution;
    }

    public String getCompletedCapVerification() {
        return completedCapVerification;
    }

    public void setCompletedCapVerification(String completedCapVerification) {
        this.completedCapVerification = completedCapVerification;
    }
}

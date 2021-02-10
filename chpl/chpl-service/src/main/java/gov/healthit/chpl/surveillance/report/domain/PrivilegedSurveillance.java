package gov.healthit.chpl.surveillance.report.domain;

import gov.healthit.chpl.domain.surveillance.SurveillanceBasic;
import gov.healthit.chpl.dto.surveillance.SurveillanceBasicDTO;
import gov.healthit.chpl.surveillance.report.dto.PrivilegedSurveillanceDTO;

public class PrivilegedSurveillance extends SurveillanceBasic {
    private static final long serialVersionUID = 2839806198924296871L;

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

    public PrivilegedSurveillance() {
        super();
    }

    public PrivilegedSurveillance(final SurveillanceBasicDTO dto) {
        super(dto);
    }

    public PrivilegedSurveillance(final PrivilegedSurveillanceDTO dto) {
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
        this.surveillanceOutcomeOther = dto.getSurveillanceOutcomeOther();
        if (dto.getSurveillanceProcessType() != null) {
            this.surveillanceProcessType = new SurveillanceProcessType(dto.getSurveillanceProcessType());
        }
        this.surveillanceProcessTypeOther = dto.getSurveillanceProcessTypeOther();
    }

    public SurveillanceOutcome getSurveillanceOutcome() {
        return surveillanceOutcome;
    }

    public void setSurveillanceOutcome(final SurveillanceOutcome surveillanceOutcome) {
        this.surveillanceOutcome = surveillanceOutcome;
    }

    public SurveillanceProcessType getSurveillanceProcessType() {
        return surveillanceProcessType;
    }

    public void setSurveillanceProcessType(final SurveillanceProcessType surveillanceProcessType) {
        this.surveillanceProcessType = surveillanceProcessType;
    }

    public Boolean getK1Reviewed() {
        return k1Reviewed;
    }

    public void setK1Reviewed(final Boolean k1Reviewed) {
        this.k1Reviewed = k1Reviewed;
    }

    public String getGroundsForInitiating() {
        return groundsForInitiating;
    }

    public void setGroundsForInitiating(final String groundsForInitiating) {
        this.groundsForInitiating = groundsForInitiating;
    }

    public String getNonconformityCauses() {
        return nonconformityCauses;
    }

    public void setNonconformityCauses(final String nonconformityCauses) {
        this.nonconformityCauses = nonconformityCauses;
    }

    public String getNonconformityNature() {
        return nonconformityNature;
    }

    public void setNonconformityNature(final String nonconformityNature) {
        this.nonconformityNature = nonconformityNature;
    }

    public String getStepsToSurveil() {
        return stepsToSurveil;
    }

    public void setStepsToSurveil(final String stepsToSurveil) {
        this.stepsToSurveil = stepsToSurveil;
    }

    public String getStepsToEngage() {
        return stepsToEngage;
    }

    public void setStepsToEngage(final String stepsToEngage) {
        this.stepsToEngage = stepsToEngage;
    }

    public String getAdditionalCostsEvaluation() {
        return additionalCostsEvaluation;
    }

    public void setAdditionalCostsEvaluation(final String additionalCostsEvaluation) {
        this.additionalCostsEvaluation = additionalCostsEvaluation;
    }

    public String getLimitationsEvaluation() {
        return limitationsEvaluation;
    }

    public void setLimitationsEvaluation(final String limitationsEvaluation) {
        this.limitationsEvaluation = limitationsEvaluation;
    }

    public String getNondisclosureEvaluation() {
        return nondisclosureEvaluation;
    }

    public void setNondisclosureEvaluation(final String nondisclosureEvaluation) {
        this.nondisclosureEvaluation = nondisclosureEvaluation;
    }

    public String getDirectionDeveloperResolution() {
        return directionDeveloperResolution;
    }

    public void setDirectionDeveloperResolution(final String directionDeveloperResolution) {
        this.directionDeveloperResolution = directionDeveloperResolution;
    }

    public String getCompletedCapVerification() {
        return completedCapVerification;
    }

    public void setCompletedCapVerification(final String completedCapVerification) {
        this.completedCapVerification = completedCapVerification;
    }

    public String getSurveillanceOutcomeOther() {
        return surveillanceOutcomeOther;
    }

    public void setSurveillanceOutcomeOther(final String surveillanceOutcomeOther) {
        this.surveillanceOutcomeOther = surveillanceOutcomeOther;
    }

    public String getSurveillanceProcessTypeOther() {
        return surveillanceProcessTypeOther;
    }

    public void setSurveillanceProcessTypeOther(final String surveillanceProcessTypeOther) {
        this.surveillanceProcessTypeOther = surveillanceProcessTypeOther;
    }
}

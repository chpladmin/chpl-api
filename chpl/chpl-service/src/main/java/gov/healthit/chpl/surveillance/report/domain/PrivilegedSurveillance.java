package gov.healthit.chpl.surveillance.report.domain;

import gov.healthit.chpl.domain.surveillance.SurveillanceBasic;
import gov.healthit.chpl.dto.surveillance.SurveillanceBasicDTO;
import gov.healthit.chpl.surveillance.report.dto.PrivilegedSurveillanceDTO;
import lombok.Data;

@Data
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

    public PrivilegedSurveillance(SurveillanceBasicDTO dto) {
        super(dto);
    }

    public PrivilegedSurveillance(PrivilegedSurveillanceDTO dto) {
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
}

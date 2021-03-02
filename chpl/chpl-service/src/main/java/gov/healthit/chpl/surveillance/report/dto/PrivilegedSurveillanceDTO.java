package gov.healthit.chpl.surveillance.report.dto;

import gov.healthit.chpl.dto.surveillance.SurveillanceBasicDTO;
import gov.healthit.chpl.entity.surveillance.SurveillanceBasicEntity;
import gov.healthit.chpl.surveillance.report.entity.PrivilegedSurveillanceEntity;
import gov.healthit.chpl.surveillance.report.entity.QuarterlyReportSurveillanceMapEntity;
import lombok.Data;

@Data
public class PrivilegedSurveillanceDTO extends SurveillanceBasicDTO {
    private static final long serialVersionUID = 849149508008111347L;

    private Long mappingId;
    private QuarterlyReportDTO quarterlyReport;
    private SurveillanceOutcomeDTO surveillanceOutcome;
    private String surveillanceOutcomeOther;
    private SurveillanceProcessTypeDTO surveillanceProcessType;
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

    public PrivilegedSurveillanceDTO() {
        super();
    }

    public PrivilegedSurveillanceDTO(SurveillanceBasicEntity entity) {
        super(entity);
    }

    public PrivilegedSurveillanceDTO(PrivilegedSurveillanceEntity entity) {
        super(entity);
        if (entity.getPrivSurvMap() != null && entity.getPrivSurvMap().size() > 0) {
            //should only be 1 per report
            QuarterlyReportSurveillanceMapEntity privSurvMap = entity.getPrivSurvMap().iterator().next();
            if (privSurvMap != null) {
              this.k1Reviewed = privSurvMap.getK1Reviewed();
              this.groundsForInitiating = privSurvMap.getGroundsForInitiating();
              this.nonconformityCauses = privSurvMap.getNonconformityCauses();
              this.nonconformityNature = privSurvMap.getNonconformityNature();
              this.stepsToSurveil = privSurvMap.getStepsToSurveil();
              this.stepsToEngage = privSurvMap.getStepsToEngage();
              this.additionalCostsEvaluation = privSurvMap.getAdditionalCostsEvaluation();
              this.limitationsEvaluation = privSurvMap.getLimitationsEvaluation();
              this.nondisclosureEvaluation = privSurvMap.getNondisclosureEvaluation();
              this.directionDeveloperResolution = privSurvMap.getDirectionDeveloperResolution();
              this.completedCapVerification = privSurvMap.getCompletedCapVerification();

              if (privSurvMap.getQuarterlyReport() != null) {
                  this.quarterlyReport = new QuarterlyReportDTO(privSurvMap.getQuarterlyReport());
              } else {
                  this.quarterlyReport = new QuarterlyReportDTO();
                  this.quarterlyReport.setId(privSurvMap.getQuarterlyReportId());
              }

              if (privSurvMap.getSurveillanceOutcome() != null) {
                  this.surveillanceOutcome = new SurveillanceOutcomeDTO(privSurvMap.getSurveillanceOutcome());
              } else if (privSurvMap.getSurveillanceOutcomeId() != null) {
                  this.surveillanceOutcome = new SurveillanceOutcomeDTO();
                  this.surveillanceOutcome.setId(privSurvMap.getSurveillanceOutcomeId());
              }
              this.surveillanceOutcomeOther = privSurvMap.getSurveillanceOutcomeOther();

              if (privSurvMap.getSurveillanceProcessType() != null) {
                  this.surveillanceProcessType = new SurveillanceProcessTypeDTO(privSurvMap.getSurveillanceProcessType());
              } else if (privSurvMap.getSurveillanceProcessTypeId() != null){
                  this.surveillanceProcessType = new SurveillanceProcessTypeDTO();
                  this.surveillanceProcessType.setId(privSurvMap.getSurveillanceProcessTypeId());
              }
              this.surveillanceProcessTypeOther = privSurvMap.getSurveillanceProcessTypeOther();
            }
        }
    }

    public PrivilegedSurveillanceDTO(QuarterlyReportSurveillanceMapEntity entity) {
        super(entity.getSurveillance());

        this.mappingId = entity.getId();
        this.k1Reviewed = entity.getK1Reviewed();
        this.groundsForInitiating = entity.getGroundsForInitiating();
        this.nonconformityCauses = entity.getNonconformityCauses();
        this.nonconformityNature = entity.getNonconformityNature();
        this.stepsToSurveil = entity.getStepsToSurveil();
        this.stepsToEngage = entity.getStepsToEngage();
        this.additionalCostsEvaluation = entity.getAdditionalCostsEvaluation();
        this.limitationsEvaluation = entity.getLimitationsEvaluation();
        this.nondisclosureEvaluation = entity.getNondisclosureEvaluation();
        this.directionDeveloperResolution = entity.getDirectionDeveloperResolution();
        this.completedCapVerification = entity.getCompletedCapVerification();

        if (entity.getQuarterlyReport() != null) {
            this.quarterlyReport = new QuarterlyReportDTO(entity.getQuarterlyReport());
        } else {
            this.quarterlyReport = new QuarterlyReportDTO();
            this.quarterlyReport.setId(entity.getQuarterlyReportId());
        }

        if (entity.getSurveillanceOutcome() != null) {
            this.surveillanceOutcome = new SurveillanceOutcomeDTO(entity.getSurveillanceOutcome());
        } else if (entity.getSurveillanceOutcomeId() != null) {
            this.surveillanceOutcome = new SurveillanceOutcomeDTO();
            this.surveillanceOutcome.setId(entity.getSurveillanceOutcomeId());
        }
        this.surveillanceOutcomeOther = entity.getSurveillanceOutcomeOther();

        if (entity.getSurveillanceProcessType() != null) {
            this.surveillanceProcessType = new SurveillanceProcessTypeDTO(entity.getSurveillanceProcessType());
        } else if (entity.getSurveillanceProcessTypeId() != null){
            this.surveillanceProcessType = new SurveillanceProcessTypeDTO();
            this.surveillanceProcessType.setId(entity.getSurveillanceProcessTypeId());
        }
        this.surveillanceProcessTypeOther = entity.getSurveillanceProcessTypeOther();
    }

    @Override
    public boolean equals(Object anotherObject) {
        if (anotherObject == null || !(anotherObject instanceof PrivilegedSurveillanceDTO)) {
            return false;
        }
        PrivilegedSurveillanceDTO anotherSurv = (PrivilegedSurveillanceDTO) anotherObject;
        if (this.getId() == null && anotherSurv.getId() != null
                || this.getId() != null && anotherSurv.getId() == null
                || this.getId() == null && anotherSurv.getId() == null) {
            return false;
        }
        if (this.getId().longValue() == anotherSurv.getId().longValue()) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        if (this.getId() == null) {
            return -1;
        }
        return this.getId().hashCode();
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

    public void copyPrivilegedFields(PrivilegedSurveillanceDTO another) {
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

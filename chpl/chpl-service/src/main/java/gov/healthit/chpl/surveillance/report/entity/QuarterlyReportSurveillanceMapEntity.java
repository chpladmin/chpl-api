package gov.healthit.chpl.surveillance.report.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.apache.commons.beanutils.BeanUtils;

import gov.healthit.chpl.compliance.surveillance.entity.SurveillanceBasicEntity;
import gov.healthit.chpl.domain.surveillance.SurveillanceBasic;
import gov.healthit.chpl.entity.EntityAudit;
import gov.healthit.chpl.surveillance.report.domain.PrivilegedSurveillance;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Getter
@Setter
@ToString
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "quarterly_report_surveillance_map")
public class QuarterlyReportSurveillanceMapEntity extends EntityAudit {
    private static final long serialVersionUID = -3609479886974409515L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "quarterly_report_id")
    private Long quarterlyReportId;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "quarterly_report_id", insertable = false, updatable = false)
    private QuarterlyReportEntity quarterlyReport;

    @Column(name = "surveillance_id")
    private Long surveillanceId;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "surveillance_id", insertable = false, updatable = false)
    private SurveillanceBasicEntity surveillance;

    @Column(name = "surveillance_outcome_id")
    private Long surveillanceOutcomeId;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "surveillance_outcome_id", insertable = false, updatable = false)
    private SurveillanceOutcomeEntity surveillanceOutcome;

    @Column(name = "surveillance_outcome_other")
    private String surveillanceOutcomeOther;

    @Column(name = "surveillance_process_type_id")
    private Long surveillanceProcessTypeId;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "surveillance_process_type_id", insertable = false, updatable = false)
    private SurveillanceProcessTypeEntity surveillanceProcessType;

    @Column(name = "surveillance_process_type_other")
    private String surveillanceProcessTypeOther;

    @Column(name = "k1_reviewed")
    private Boolean k1Reviewed;

    @Column(name = "grounds_for_initiating")
    private String groundsForInitiating;

    @Column(name = "nonconformity_causes")
    private String nonconformityCauses;

    @Column(name = "nonconformity_nature")
    private String nonconformityNature;

    @Column(name = "steps_to_surveil")
    private String stepsToSurveil;

    @Column(name = "steps_to_engage")
    private String stepsToEngage;

    @Column(name = "additional_costs_evaluation")
    private String additionalCostsEvaluation;

    @Column(name = "limitations_evaluation")
    private String limitationsEvaluation;

    @Column(name = "nondisclosure_evaluation")
    private String nondisclosureEvaluation;

    @Column(name = "direction_developer_resolution")
    private String directionDeveloperResolution;

    @Column(name = "completed_cap_verification")
    private String completedCapVerification;

    public PrivilegedSurveillance toDomain() {
        PrivilegedSurveillance privilegedSurveillance = PrivilegedSurveillance.builder()
                .quarterlyReport(this.getQuarterlyReport().toDomain())
                .additionalCostsEvaluation(this.getAdditionalCostsEvaluation())
                .completedCapVerification(this.getCompletedCapVerification())
                .directionDeveloperResolution(this.getDirectionDeveloperResolution())
                .groundsForInitiating(this.getGroundsForInitiating())
                .k1Reviewed(this.getK1Reviewed())
                .limitationsEvaluation(this.getLimitationsEvaluation())
                .nonconformityCauses(this.getNonconformityCauses())
                .nonconformityNature(this.getNonconformityNature())
                .nondisclosureEvaluation(this.getNondisclosureEvaluation())
                .stepsToEngage(this.getStepsToEngage())
                .stepsToSurveil(this.getStepsToSurveil())
                .surveillanceOutcome(this.getSurveillanceOutcome().toDomain())
                .surveillanceOutcomeOther(this.getSurveillanceOutcomeOther())
                .surveillanceProcessType(this.getSurveillanceProcessType().toDomain())
                .surveillanceProcessTypeOther(this.getSurveillanceProcessTypeOther())
                .build();

        SurveillanceBasic relatedSurv = this.getSurveillance().buildSurveillanceBasic();
        try {
            BeanUtils.copyProperties(privilegedSurveillance, relatedSurv);
        } catch (Exception ex) {
            LOGGER.error("Cannot copy relatedSurv properties to PrivilegedSurveillance.", ex);
        }
        return privilegedSurveillance;
    }
}

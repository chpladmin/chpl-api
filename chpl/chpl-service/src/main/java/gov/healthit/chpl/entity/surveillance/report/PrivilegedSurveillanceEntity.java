package gov.healthit.chpl.entity.surveillance.report;


import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Where;

import gov.healthit.chpl.entity.surveillance.SurveillanceTypeEntity;

@Entity
@Immutable
@Table(name = "surveillance")
public class PrivilegedSurveillanceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

//    @Column(name = "surveillance_id")
//    private Long surveillanceId;

    @Column(name = "friendly_id", insertable = false, updatable = false)
    private String friendlyId;

    @Column(name = "certified_product_id")
    private Long certifiedProductId;

    @Column(name = "start_date")
    private Date startDate;

    @Column(name = "end_date")
    private Date endDate;

    @Column(name = "type_id")
    private Long surveillanceTypeId;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id", insertable = false, updatable = false)
    private SurveillanceTypeEntity surveillanceType;

    @Column(name = "randomized_sites_used")
    private Integer numRandomizedSites;

    @Column(name = "user_permission_id")
    private Long userPermissionId;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "surveillanceId")
    @Basic(optional = false)
    @Column(name = "surveillance_id", nullable = false)
    @Where(clause = "deleted <> 'true'")
    private Set<QuarterlyReportSurveillanceMapEntity> privSurvMap = new HashSet<QuarterlyReportSurveillanceMapEntity>();

//
//    @Column(name = "privileged_surveillance_id")
//    private Long privelegedSurveillanceId;
//
//    @Column(name = "quarterly_report_id")
//    private Long quarterlyReportId;
//
//    @OneToOne(optional = true, fetch = FetchType.LAZY)
//    @JoinColumn(name = "quarterly_report_id", insertable = false, updatable = false)
//    private QuarterlyReportEntity quarterlyReport;
//
//    @Column(name = "surveillance_outcome_id")
//    private Long surveillanceOutcomeId;
//
//    @OneToOne(optional = true, fetch = FetchType.LAZY)
//    @JoinColumn(name = "surveillance_outcome_id", insertable = false, updatable = false)
//    private SurveillanceOutcomeEntity surveillanceOutcome;
//
//    @Column(name = "surveillance_outcome_other")
//    private String surveillanceOutcomeOther;
//
//    @Column(name = "surveillance_process_type_id")
//    private Long surveillanceProcessTypeId;
//
//    @OneToOne(optional = true, fetch = FetchType.LAZY)
//    @JoinColumn(name = "surveillance_process_type_id", insertable = false, updatable = false)
//    private SurveillanceProcessTypeEntity surveillanceProcessType;
//
//    @Column(name = "surveillance_process_type_other")
//    private String surveillanceProcessTypeOther;
//
//    @Column(name = "k1_reviewed")
//    private Boolean k1Reviewed;
//
//    @Column(name = "grounds_for_initiating")
//    private String groundsForInitiating;
//
//    @Column(name = "nonconformity_causes")
//    private String nonconformityCauses;
//
//    @Column(name = "nonconformity_nature")
//    private String nonconformityNature;
//
//    @Column(name = "steps_to_surveil")
//    private String stepsToSurveil;
//
//    @Column(name = "steps_to_engage")
//    private String stepsToEngage;
//
//    @Column(name = "additional_costs_evaluation")
//    private String additionalCostsEvaluation;
//
//    @Column(name = "limitations_evaluation")
//    private String limitationsEvaluation;
//
//    @Column(name = "nondisclosure_evaluation")
//    private String nondisclosureEvaluation;
//
//    @Column(name = "direction_developer_resolution")
//    private String directionDeveloperResolution;
//
//    @Column(name = "completed_cap_verification")
//    private String completedCapVerification;

    @Column(name = "deleted")
    private Boolean deleted;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

//    public Long getSurveillanceId() {
//        return surveillanceId;
//    }
//
//    public void setSurveillanceId(final Long surveillanceId) {
//        this.surveillanceId = surveillanceId;
//    }

    public String getFriendlyId() {
        return friendlyId;
    }

    public void setFriendlyId(final String friendlyId) {
        this.friendlyId = friendlyId;
    }

    public Long getCertifiedProductId() {
        return certifiedProductId;
    }

    public void setCertifiedProductId(final Long certifiedProductId) {
        this.certifiedProductId = certifiedProductId;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(final Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(final Date endDate) {
        this.endDate = endDate;
    }

    public Long getSurveillanceTypeId() {
        return surveillanceTypeId;
    }

    public void setSurveillanceTypeId(final Long surveillanceTypeId) {
        this.surveillanceTypeId = surveillanceTypeId;
    }

    public SurveillanceTypeEntity getSurveillanceType() {
        return surveillanceType;
    }

    public void setSurveillanceType(final SurveillanceTypeEntity surveillanceType) {
        this.surveillanceType = surveillanceType;
    }

    public Integer getNumRandomizedSites() {
        return numRandomizedSites;
    }

    public void setNumRandomizedSites(final Integer numRandomizedSites) {
        this.numRandomizedSites = numRandomizedSites;
    }

    public Long getUserPermissionId() {
        return userPermissionId;
    }

    public void setUserPermissionId(final Long userPermissionId) {
        this.userPermissionId = userPermissionId;
    }
//
//    public Long getPrivelegedSurveillanceId() {
//        return privelegedSurveillanceId;
//    }
//
//    public void setPrivelegedSurveillanceId(final Long privelegedSurveillanceId) {
//        this.privelegedSurveillanceId = privelegedSurveillanceId;
//    }
//
//    public Long getQuarterlyReportId() {
//        return quarterlyReportId;
//    }
//
//    public void setQuarterlyReportId(final Long quarterlyReportId) {
//        this.quarterlyReportId = quarterlyReportId;
//    }
//
//    public QuarterlyReportEntity getQuarterlyReport() {
//        return quarterlyReport;
//    }
//
//    public void setQuarterlyReport(final QuarterlyReportEntity quarterlyReport) {
//        this.quarterlyReport = quarterlyReport;
//    }
//
//    public Long getSurveillanceOutcomeId() {
//        return surveillanceOutcomeId;
//    }
//
//    public void setSurveillanceOutcomeId(final Long surveillanceOutcomeId) {
//        this.surveillanceOutcomeId = surveillanceOutcomeId;
//    }
//
//    public SurveillanceOutcomeEntity getSurveillanceOutcome() {
//        return surveillanceOutcome;
//    }
//
//    public void setSurveillanceOutcome(final SurveillanceOutcomeEntity surveillanceOutcome) {
//        this.surveillanceOutcome = surveillanceOutcome;
//    }
//
//    public Long getSurveillanceProcessTypeId() {
//        return surveillanceProcessTypeId;
//    }
//
//    public void setSurveillanceProcessTypeId(final Long surveillanceProcessTypeId) {
//        this.surveillanceProcessTypeId = surveillanceProcessTypeId;
//    }
//
//    public SurveillanceProcessTypeEntity getSurveillanceProcessType() {
//        return surveillanceProcessType;
//    }
//
//    public void setSurveillanceProcessType(final SurveillanceProcessTypeEntity surveillanceProcessType) {
//        this.surveillanceProcessType = surveillanceProcessType;
//    }
//
//    public Boolean getK1Reviewed() {
//        return k1Reviewed;
//    }
//
//    public void setK1Reviewed(final Boolean k1Reviewed) {
//        this.k1Reviewed = k1Reviewed;
//    }
//
//    public String getGroundsForInitiating() {
//        return groundsForInitiating;
//    }
//
//    public void setGroundsForInitiating(final String groundsForInitiating) {
//        this.groundsForInitiating = groundsForInitiating;
//    }
//
//    public String getNonconformityCauses() {
//        return nonconformityCauses;
//    }
//
//    public void setNonconformityCauses(final String nonconformityCauses) {
//        this.nonconformityCauses = nonconformityCauses;
//    }
//
//    public String getNonconformityNature() {
//        return nonconformityNature;
//    }
//
//    public void setNonconformityNature(final String nonconformityNature) {
//        this.nonconformityNature = nonconformityNature;
//    }
//
//    public String getStepsToSurveil() {
//        return stepsToSurveil;
//    }
//
//    public void setStepsToSurveil(final String stepsToSurveil) {
//        this.stepsToSurveil = stepsToSurveil;
//    }
//
//    public String getStepsToEngage() {
//        return stepsToEngage;
//    }
//
//    public void setStepsToEngage(final String stepsToEngage) {
//        this.stepsToEngage = stepsToEngage;
//    }
//
//    public String getAdditionalCostsEvaluation() {
//        return additionalCostsEvaluation;
//    }
//
//    public void setAdditionalCostsEvaluation(final String additionalCostsEvaluation) {
//        this.additionalCostsEvaluation = additionalCostsEvaluation;
//    }
//
//    public String getLimitationsEvaluation() {
//        return limitationsEvaluation;
//    }
//
//    public void setLimitationsEvaluation(final String limitationsEvaluation) {
//        this.limitationsEvaluation = limitationsEvaluation;
//    }
//
//    public String getNondisclosureEvaluation() {
//        return nondisclosureEvaluation;
//    }
//
//    public void setNondisclosureEvaluation(final String nondisclosureEvaluation) {
//        this.nondisclosureEvaluation = nondisclosureEvaluation;
//    }
//
//    public String getDirectionDeveloperResolution() {
//        return directionDeveloperResolution;
//    }
//
//    public void setDirectionDeveloperResolution(final String directionDeveloperResolution) {
//        this.directionDeveloperResolution = directionDeveloperResolution;
//    }
//
//    public String getCompletedCapVerification() {
//        return completedCapVerification;
//    }
//
//    public void setCompletedCapVerification(final String completedCapVerification) {
//        this.completedCapVerification = completedCapVerification;
//    }
//
//    public String getSurveillanceOutcomeOther() {
//        return surveillanceOutcomeOther;
//    }
//
//    public void setSurveillanceOutcomeOther(final String surveillanceOutcomeOther) {
//        this.surveillanceOutcomeOther = surveillanceOutcomeOther;
//    }
//
//    public String getSurveillanceProcessTypeOther() {
//        return surveillanceProcessTypeOther;
//    }
//
//    public void setSurveillanceProcessTypeOther(final String surveillanceProcessTypeOther) {
//        this.surveillanceProcessTypeOther = surveillanceProcessTypeOther;
//    }

    public Set<QuarterlyReportSurveillanceMapEntity> getPrivSurvMap() {
        return privSurvMap;
    }

    public void setPrivSurvMap(final Set<QuarterlyReportSurveillanceMapEntity> privSurvMap) {
        this.privSurvMap = privSurvMap;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(final Boolean deleted) {
        this.deleted = deleted;
    }
}

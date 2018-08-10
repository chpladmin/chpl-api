package gov.healthit.chpl.entity.scheduler;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Entity containing needed data for Broken Surveillance Rules reports.
 * @author alarned
 *
 */
@Entity
@Table(name = "broken_surveillance_rules")
public class BrokenSurveillanceRulesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    private Long id;

    @Basic(optional = false)
    @Column(name = "developer")
    private String developer;

    @Basic(optional = false)
    @Column(name = "product")
    private String product;

    @Basic(optional = false)
    @Column(name = "version")
    private String version;

    @Basic(optional = false)
    @Column(name = "chpl_product_number")
    private String chplProductNumber;

    @Basic(optional = false)
    @Column(name = "url")
    private String url;

    @Basic(optional = false)
    @Column(name = "acb")
    private String acb;

    @Basic(optional = false)
    @Column(name = "certification_status")
    private String certificationStatus;

    @Basic(optional = false)
    @Column(name = "date_of_last_status_change")
    private String dateOfLastStatusChange;

    @Basic(optional = false)
    @Column(name = "surveillance_id")
    private String surveillanceId;

    @Basic(optional = false)
    @Column(name = "date_surveillance_began")
    private String dateSurveillanceBegan;

    @Basic(optional = true)
    @Column(name = "date_surveillance_ended")
    private String dateSurveillanceEnded;

    @Basic(optional = false)
    @Column(name = "surveillance_type")
    private String surveillanceType;

    @Basic(optional = true)
    @Column(name = "lengthy_suspension_rule")
    private String lengthySuspensionRule;

    @Basic(optional = true)
    @Column(name = "cap_not_approved_rule")
    private String capNotApprovedRule;

    @Basic(optional = true)
    @Column(name = "cap_not_started_rule")
    private String capNotStartedRule;

    @Basic(optional = true)
    @Column(name = "cap_not_completed_rule")
    private String capNotCompletedRule;

    @Basic(optional = true)
    @Column(name = "cap_not_closed_rule")
    private String capNotClosedRule;

    @Basic(optional = true)
    @Column(name = "closed_cap_with_open_nonconformity_rule")
    private String closedCapWithOpenNonconformityRule;

    @Basic(optional = false)
    @Column(name = "nonconformity")
    private Boolean nonconformity;

    @Basic(optional = true)
    @Column(name = "nonconformity_status")
    private String nonconformityStatus;

    @Basic(optional = true)
    @Column(name = "nonconformity_criteria")
    private String nonconformityCriteria;

    @Basic(optional = true)
    @Column(name = "date_of_determination_of_nonconformity")
    private String dateOfDeterminationOfNonconformity;

    @Basic(optional = true)
    @Column(name = "corrective_action_plan_approved_date")
    private String correctiveActionPlanApprovedDate;

    @Basic(optional = true)
    @Column(name = "date_corrective_action_began")
    private String dateCorrectiveActionBegan;

    @Basic(optional = true)
    @Column(name = "date_corrective_action_must_be_completed")
    private String dateCorrectiveActionMustBeCompleted;

    @Basic(optional = true)
    @Column(name = "date_corrective_action_was_completed")
    private String dateCorrectiveActionWasCompleted;

    @Basic(optional = true)
    @Column(name = "number_of_days_from_determination_to_cap_approval")
    private int numberOfDaysFromDeterminationToCapApproval;

    @Basic(optional = true)
    @Column(name = "number_of_days_from_determination_to_present")
    private int numberOfDaysFromDeterminationToPresent;

    @Basic(optional = true)
    @Column(name = "number_of_days_from_cap_approval_to_cap_began")
    private int numberOfDaysFromCapApprovalToCapBegan;

    @Basic(optional = true)
    @Column(name = "number_of_days_from_cap_approval_to_present")
    private int numberOfDaysFromCapApprovalToPresent;

    @Basic(optional = true)
    @Column(name = "number_of_days_from_cap_began_to_cap_completed")
    private int numberOfDaysFromCapBeganToCapCompleted;

    @Basic(optional = true)
    @Column(name = "number_of_days_from_cap_began_to_present")
    private int numberOfDaysFromCapBeganToPresent;

    @Basic(optional = true)
    @Column(name = "difference_from_cap_completed_and_cap_must_be_completed")
    private int differenceFromCapCompletedAndCapMustBeCompleted;

    @Basic(optional = false)
    @Column(name = "deleted")
    private Boolean deleted;

    @Basic(optional = false)
    @Column(name = "last_modified_user")
    private Long lastModifiedUser;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getDeveloper() {
        return developer;
    }

    public void setDeveloper(final String developer) {
        this.developer = developer;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(final String product) {
        this.product = product;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(final String version) {
        this.version = version;
    }

    public String getChplProductNumber() {
        return chplProductNumber;
    }

    public void setChplProductNumber(final String chplProductNumber) {
        this.chplProductNumber = chplProductNumber;
    }

    public String getAcb() {
        return acb;
    }

    public void setAcb(final String acb) {
        this.acb = acb;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    public String getCertificationStatus() {
        return certificationStatus;
    }

    public void setCertificationStatus(final String certificationStatus) {
        this.certificationStatus = certificationStatus;
    }

    public String getDateOfLastStatusChange() {
        return dateOfLastStatusChange;
    }

    public void setDateOfLastStatusChange(final String dateOfLastStatusChange) {
        this.dateOfLastStatusChange = dateOfLastStatusChange;
    }

    public String getSurveillanceId() {
        return surveillanceId;
    }

    public void setSurveillanceId(final String surveillanceId) {
        this.surveillanceId = surveillanceId;
    }

    public String getDateSurveillanceBegan() {
        return dateSurveillanceBegan;
    }

    public void setDateSurveillanceBegan(final String dateSurveillanceBegan) {
        this.dateSurveillanceBegan = dateSurveillanceBegan;
    }

    public String getDateSurveillanceEnded() {
        return dateSurveillanceEnded;
    }

    public void setDateSurveillanceEnded(final String dateSurveillanceEnded) {
        this.dateSurveillanceEnded = dateSurveillanceEnded;
    }

    public String getSurveillanceType() {
        return surveillanceType;
    }

    public void setSurveillanceType(final String surveillanceType) {
        this.surveillanceType = surveillanceType;
    }

    public String getLengthySuspensionRule() {
        return lengthySuspensionRule;
    }

    public void setLengthySuspensionRule(final String lengthySuspensionRule) {
        this.lengthySuspensionRule = lengthySuspensionRule;
    }

    public String getCapNotApprovedRule() {
        return capNotApprovedRule;
    }

    public void setCapNotApprovedRule(final String capNotApprovedRule) {
        this.capNotApprovedRule = capNotApprovedRule;
    }

    public String getCapNotStartedRule() {
        return capNotStartedRule;
    }

    public void setCapNotStartedRule(final String capNotStartedRule) {
        this.capNotStartedRule = capNotStartedRule;
    }

    public String getCapNotCompletedRule() {
        return capNotCompletedRule;
    }

    public void setCapNotCompletedRule(final String capNotCompletedRule) {
        this.capNotCompletedRule = capNotCompletedRule;
    }

    public String getCapNotClosedRule() {
        return capNotClosedRule;
    }

    public void setCapNotClosedRule(final String capNotClosedRule) {
        this.capNotClosedRule = capNotClosedRule;
    }

    public String getClosedCapWithOpenNonconformityRule() {
        return closedCapWithOpenNonconformityRule;
    }

    public void setClosedCapWithOpenNonconformityRule(final String closedCapWithOpenNonconformityRule) {
        this.closedCapWithOpenNonconformityRule = closedCapWithOpenNonconformityRule;
    }

    public Boolean getNonconformity() {
        return nonconformity;
    }

    public void setNonconformity(final Boolean nonconformity) {
        this.nonconformity = nonconformity;
    }

    public String getNonconformityStatus() {
        return nonconformityStatus;
    }

    public void setNonconformityStatus(final String nonconformityStatus) {
        this.nonconformityStatus = nonconformityStatus;
    }

    public String getNonconformityCriteria() {
        return nonconformityCriteria;
    }

    public void setNonconformityCriteria(final String nonconformityCriteria) {
        this.nonconformityCriteria = nonconformityCriteria;
    }

    public String getDateOfDeterminationOfNonconformity() {
        return dateOfDeterminationOfNonconformity;
    }

    public void setDateOfDeterminationOfNonconformity(final String dateOfDeterminationOfNonconformity) {
        this.dateOfDeterminationOfNonconformity = dateOfDeterminationOfNonconformity;
    }

    public String getCorrectiveActionPlanApprovedDate() {
        return correctiveActionPlanApprovedDate;
    }

    public void setCorrectiveActionPlanApprovedDate(final String correctiveActionPlanApprovedDate) {
        this.correctiveActionPlanApprovedDate = correctiveActionPlanApprovedDate;
    }

    public String getDateCorrectiveActionBegan() {
        return dateCorrectiveActionBegan;
    }

    public void setDateCorrectiveActionBegan(final String dateCorrectiveActionBegan) {
        this.dateCorrectiveActionBegan = dateCorrectiveActionBegan;
    }

    public String getDateCorrectiveActionMustBeCompleted() {
        return dateCorrectiveActionMustBeCompleted;
    }

    public void setDateCorrectiveActionMustBeCompleted(final String dateCorrectiveActionMustBeCompleted) {
        this.dateCorrectiveActionMustBeCompleted = dateCorrectiveActionMustBeCompleted;
    }

    public String getDateCorrectiveActionWasCompleted() {
        return dateCorrectiveActionWasCompleted;
    }

    public void setDateCorrectiveActionWasCompleted(final String dateCorrectiveActionWasCompleted) {
        this.dateCorrectiveActionWasCompleted = dateCorrectiveActionWasCompleted;
    }

    public int getNumberOfDaysFromDeterminationToCapApproval() {
        return numberOfDaysFromDeterminationToCapApproval;
    }

    public void setNumberOfDaysFromDeterminationToCapApproval(final int numberOfDaysFromDeterminationToCapApproval) {
        this.numberOfDaysFromDeterminationToCapApproval = numberOfDaysFromDeterminationToCapApproval;
    }

    public int getNumberOfDaysFromDeterminationToPresent() {
        return numberOfDaysFromDeterminationToPresent;
    }

    public void setNumberOfDaysFromDeterminationToPresent(final int numberOfDaysFromDeterminationToPresent) {
        this.numberOfDaysFromDeterminationToPresent = numberOfDaysFromDeterminationToPresent;
    }

    public int getNumberOfDaysFromCapApprovalToCapBegan() {
        return numberOfDaysFromCapApprovalToCapBegan;
    }

    public void setNumberOfDaysFromCapApprovalToCapBegan(final int numberOfDaysFromCapApprovalToCapBegan) {
        this.numberOfDaysFromCapApprovalToCapBegan = numberOfDaysFromCapApprovalToCapBegan;
    }

    public int getNumberOfDaysFromCapApprovalToPresent() {
        return numberOfDaysFromCapApprovalToPresent;
    }

    public void setNumberOfDaysFromCapApprovalToPresent(final int numberOfDaysFromCapApprovalToPresent) {
        this.numberOfDaysFromCapApprovalToPresent = numberOfDaysFromCapApprovalToPresent;
    }

    public int getNumberOfDaysFromCapBeganToCapCompleted() {
        return numberOfDaysFromCapBeganToCapCompleted;
    }

    public void setNumberOfDaysFromCapBeganToCapCompleted(final int numberOfDaysFromCapBeganToCapCompleted) {
        this.numberOfDaysFromCapBeganToCapCompleted = numberOfDaysFromCapBeganToCapCompleted;
    }

    public int getNumberOfDaysFromCapBeganToPresent() {
        return numberOfDaysFromCapBeganToPresent;
    }

    public void setNumberOfDaysFromCapBeganToPresent(final int numberOfDaysFromCapBeganToPresent) {
        this.numberOfDaysFromCapBeganToPresent = numberOfDaysFromCapBeganToPresent;
    }

    public int getDifferenceFromCapCompletedAndCapMustBeCompleted() {
        return differenceFromCapCompletedAndCapMustBeCompleted;
    }

    public void setDifferenceFromCapCompletedAndCapMustBeCompleted(
            final int differenceFromCapCompletedAndCapMustBeCompleted) {
        this.differenceFromCapCompletedAndCapMustBeCompleted = differenceFromCapCompletedAndCapMustBeCompleted;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(final Boolean deleted) {
        this.deleted = deleted;
    }

    public Long getLastModifiedUser() {
        return lastModifiedUser;
    }

    public void setLastModifiedUser(final Long lastModifiedUser) {
        this.lastModifiedUser = lastModifiedUser;
    }
}

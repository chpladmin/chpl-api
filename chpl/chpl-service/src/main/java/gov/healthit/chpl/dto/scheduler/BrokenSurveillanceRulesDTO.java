package gov.healthit.chpl.dto.scheduler;

import java.io.Serializable;

import gov.healthit.chpl.entity.scheduler.BrokenSurveillanceRulesEntity;

/**
 * Broken Surveillance Rules data transfer object.
 * @author alarned
 *
 */
public class BrokenSurveillanceRulesDTO implements Serializable {

    private static final long serialVersionUID = -1536844909545189801L;

    private Long id;
    private String chplProductNumber;
    private String developer;
    private String product;
    private String version;
    private String acb;
    private String url;
    private String certificationStatus;
    private String dateOfLastStatusChange;
    private String surveillanceId;
    private String dateSurveillanceBegan;
    private String dateSurveillanceEnded;
    private String surveillanceType;
    private String lengthySuspensionRule;
    private String capNotApprovedRule;
    private String capNotStartedRule;
    private String capNotCompletedRule;
    private String capNotClosedRule;
    private String closedCapWithOpenNonconformityRule;
    private Boolean nonconformity;
    private String nonconformityStatus;
    private String nonconformityCriteria;
    private String dateOfDeterminationOfNonconformity;
    private String correctiveActionPlanApprovedDate;
    private String dateCorrectiveActionBegan;
    private String dateCorrectiveActionMustBeCompleted;
    private String dateCorrectiveActionWasCompleted;
    private long numberOfDaysFromDeterminationToCapApproval;
    private long numberOfDaysFromDeterminationToPresent;
    private long numberOfDaysFromCapApprovalToCapBegan;
    private long numberOfDaysFromCapApprovalToPresent;
    private long numberOfDaysFromCapBeganToCapCompleted;
    private long numberOfDaysFromCapBeganToPresent;
    private long differenceFromCapCompletedAndCapMustBeCompleted;
    private Boolean deleted;


    /**
     * Default constructor.
     */
    public BrokenSurveillanceRulesDTO() {
        this.nonconformity = false;
        this.numberOfDaysFromDeterminationToCapApproval = Long.MIN_VALUE;
        this.numberOfDaysFromDeterminationToPresent = Long.MIN_VALUE;
        this.numberOfDaysFromCapApprovalToCapBegan = Long.MIN_VALUE;
        this.numberOfDaysFromCapApprovalToPresent = Long.MIN_VALUE;
        this.numberOfDaysFromCapBeganToCapCompleted = Long.MIN_VALUE;
        this.numberOfDaysFromCapBeganToPresent = Long.MIN_VALUE;
        this.differenceFromCapCompletedAndCapMustBeCompleted = Long.MIN_VALUE;
    }

    /**
     * Constructor that will populate the created object based on the entity
     * that is passed in as a parameter.
     * @param entity BrokenSurveillanceRulesEntity entity
     */
    public BrokenSurveillanceRulesDTO(final BrokenSurveillanceRulesEntity entity) {
        this.id = entity.getId();
        this.developer = entity.getDeveloper();
        this.product = entity.getProduct();
        this.version = entity.getVersion();
        this.acb = entity.getAcb();
        this.chplProductNumber = entity.getChplProductNumber();
        this.url = entity.getUrl();
        this.certificationStatus = entity.getCertificationStatus();
        this.dateOfLastStatusChange = entity.getDateOfLastStatusChange();
        this.surveillanceId = entity.getSurveillanceId();
        this.dateSurveillanceBegan = entity.getDateSurveillanceBegan();
        this.dateSurveillanceEnded = entity.getDateSurveillanceEnded();
        this.surveillanceType = entity.getSurveillanceType();
        this.lengthySuspensionRule = entity.getLengthySuspensionRule();
        this.capNotApprovedRule = entity.getCapNotApprovedRule();
        this.capNotStartedRule = entity.getCapNotStartedRule();
        this.capNotCompletedRule = entity.getCapNotCompletedRule();
        this.capNotClosedRule = entity.getCapNotClosedRule();
        this.closedCapWithOpenNonconformityRule = entity.getClosedCapWithOpenNonconformityRule();
        this.nonconformity = entity.getNonconformity();
        this.nonconformityStatus = entity.getNonconformityStatus();
        this.nonconformityCriteria = entity.getNonconformityCriteria();
        this.dateOfDeterminationOfNonconformity = entity.getDateOfDeterminationOfNonconformity();
        this.correctiveActionPlanApprovedDate = entity.getCorrectiveActionPlanApprovedDate();
        this.dateCorrectiveActionBegan = entity.getDateCorrectiveActionBegan();
        this.dateCorrectiveActionMustBeCompleted = entity.getDateCorrectiveActionMustBeCompleted();
        this.dateCorrectiveActionWasCompleted = entity.getDateCorrectiveActionWasCompleted();
        this.numberOfDaysFromDeterminationToCapApproval = entity.getNumberOfDaysFromDeterminationToCapApproval();
        this.numberOfDaysFromDeterminationToPresent = entity.getNumberOfDaysFromDeterminationToPresent();
        this.numberOfDaysFromCapApprovalToCapBegan = entity.getNumberOfDaysFromCapApprovalToCapBegan();
        this.numberOfDaysFromCapApprovalToPresent = entity.getNumberOfDaysFromCapApprovalToPresent();
        this.numberOfDaysFromCapBeganToCapCompleted = entity.getNumberOfDaysFromCapBeganToCapCompleted();
        this.numberOfDaysFromCapBeganToPresent = entity.getNumberOfDaysFromCapBeganToPresent();
        this.differenceFromCapCompletedAndCapMustBeCompleted
        = entity.getDifferenceFromCapCompletedAndCapMustBeCompleted();
        this.deleted = entity.getDeleted();
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getChplProductNumber() {
        return chplProductNumber;
    }

    public void setChplProductNumber(final String chplProductNumber) {
        this.chplProductNumber = chplProductNumber;
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

    public long getNumberOfDaysFromDeterminationToCapApproval() {
        return numberOfDaysFromDeterminationToCapApproval;
    }

    public void setNumberOfDaysFromDeterminationToCapApproval(final long numberOfDaysFromDeterminationToCapApproval) {
        this.numberOfDaysFromDeterminationToCapApproval = numberOfDaysFromDeterminationToCapApproval;
    }

    public long getNumberOfDaysFromDeterminationToPresent() {
        return numberOfDaysFromDeterminationToPresent;
    }

    public void setNumberOfDaysFromDeterminationToPresent(final long numberOfDaysFromDeterminationToPresent) {
        this.numberOfDaysFromDeterminationToPresent = numberOfDaysFromDeterminationToPresent;
    }

    public long getNumberOfDaysFromCapApprovalToCapBegan() {
        return numberOfDaysFromCapApprovalToCapBegan;
    }

    public void setNumberOfDaysFromCapApprovalToCapBegan(final long numberOfDaysFromCapApprovalToCapBegan) {
        this.numberOfDaysFromCapApprovalToCapBegan = numberOfDaysFromCapApprovalToCapBegan;
    }

    public long getNumberOfDaysFromCapApprovalToPresent() {
        return numberOfDaysFromCapApprovalToPresent;
    }

    public void setNumberOfDaysFromCapApprovalToPresent(final long numberOfDaysFromCapApprovalToPresent) {
        this.numberOfDaysFromCapApprovalToPresent = numberOfDaysFromCapApprovalToPresent;
    }

    public long getNumberOfDaysFromCapBeganToCapCompleted() {
        return numberOfDaysFromCapBeganToCapCompleted;
    }

    public void setNumberOfDaysFromCapBeganToCapCompleted(final long numberOfDaysFromCapBeganToCapCompleted) {
        this.numberOfDaysFromCapBeganToCapCompleted = numberOfDaysFromCapBeganToCapCompleted;
    }

    public long getNumberOfDaysFromCapBeganToPresent() {
        return numberOfDaysFromCapBeganToPresent;
    }

    public void setNumberOfDaysFromCapBeganToPresent(final long numberOfDaysFromCapBeganToPresent) {
        this.numberOfDaysFromCapBeganToPresent = numberOfDaysFromCapBeganToPresent;
    }

    public long getDifferenceFromCapCompletedAndCapMustBeCompleted() {
        return differenceFromCapCompletedAndCapMustBeCompleted;
    }

    public void setDifferenceFromCapCompletedAndCapMustBeCompleted(
            final long differenceFromCapCompletedAndCapMustBeCompleted) {
        this.differenceFromCapCompletedAndCapMustBeCompleted = differenceFromCapCompletedAndCapMustBeCompleted;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(final Boolean deleted) {
        this.deleted = deleted;
    }

    @Override
    public String toString() {
        return "Inheritance Errors Report DTO ["
                + "[Developer: " + this.developer + "]"
                + "[Product: " + this.product + "]"
                + "[Version: " + this.version + "]"
                + "[ACB: " + this.acb + "]"
                + "[CHPL Product Number: " + this.chplProductNumber + "]"
                + "[URL: " + this.url + "]"
                + "[CertificationStatus: " + this.certificationStatus + "]"
                + "[DateOfLastStatusChange: " + this.dateOfLastStatusChange + "]"
                + "[SurveillanceId: " + this.surveillanceId + "]"
                + "[DateSurveillanceBegan: " + this.dateSurveillanceBegan + "]"
                + "[DateSurveillanceEnded: " + this.dateSurveillanceEnded + "]"
                + "[SurveillanceType: " + this.surveillanceType + "]"
                + "[LengthySuspensionRule: " + this.lengthySuspensionRule + "]"
                + "[CapNotApprovedRule: " + this.capNotApprovedRule + "]"
                + "[CapNotStartedRule: " + this.capNotStartedRule + "]"
                + "[CapNotCompletedRule: " + this.capNotCompletedRule + "]"
                + "[CapNotClosedRule: " + this.capNotClosedRule + "]"
                + "[ClosedCapWithOpenNonconformityRule: " + this.closedCapWithOpenNonconformityRule + "]"
                + "[Nonconformity: " + this.nonconformity + "]"
                + "[NonconformityStatus: " + this.nonconformityStatus + "]"
                + "[NonconformityCriteria: " + this.nonconformityCriteria + "]"
                + "[DateOfDeterminationOfNonconformity: " + this.dateOfDeterminationOfNonconformity + "]"
                + "[CorrectiveActionPlanApprovedDate: " + this.correctiveActionPlanApprovedDate + "]"
                + "[DateCorrectiveActionBegan: " + this.dateCorrectiveActionBegan + "]"
                + "[DateCorrectiveActionMustBeCompleted: " + this.dateCorrectiveActionMustBeCompleted + "]"
                + "[DateCorrectiveActionWasCompleted: " + this.dateCorrectiveActionWasCompleted + "]"
                + "[NumberOfDaysFromDeterminationToCapApproval: "
                + this.numberOfDaysFromDeterminationToCapApproval + "]"
                + "[NumberOfDaysFromDeterminationToPresent: " + this.numberOfDaysFromDeterminationToPresent + "]"
                + "[NumberOfDaysFromCapApprovalToCapBegan: " + this.numberOfDaysFromCapApprovalToCapBegan + "]"
                + "[NumberOfDaysFromCapApprovalToPresent: " + this.numberOfDaysFromCapApprovalToPresent + "]"
                + "[NumberOfDaysFromCapBeganToCapCompleted: " + this.numberOfDaysFromCapBeganToCapCompleted + "]"
                + "[NumberOfDaysFromCapBeganToPresent: " + this.numberOfDaysFromCapBeganToPresent + "]"
                + "[DifferenceFromCapCompletedAndCapMustBeCompleted: "
                + this.differenceFromCapCompletedAndCapMustBeCompleted + "]"
                + "]";
    }
}

package gov.healthit.chpl.dto.scheduler;

import java.io.Serializable;
import java.time.LocalDate;

import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.entity.scheduler.BrokenSurveillanceRulesEntity;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class BrokenSurveillanceRulesDTO implements Serializable {

    private static final long serialVersionUID = -1536844909545189801L;

    private Long id;
    private String chplProductNumber;
    private String developer;
    private String product;
    private String version;
    private CertificationBodyDTO certificationBody;
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
    private String nonconformityCriteria;
    private String dateOfDeterminationOfNonconformity;
    private String correctiveActionPlanApprovedDate;
    private String dateCorrectiveActionBegan;
    private String dateCorrectiveActionMustBeCompleted;
    private String dateCorrectiveActionWasCompleted;
    private LocalDate nonConformityCloseDate;
    private long numberOfDaysFromDeterminationToCapApproval;
    private long numberOfDaysFromDeterminationToPresent;
    private long numberOfDaysFromCapApprovalToCapBegan;
    private long numberOfDaysFromCapApprovalToPresent;
    private long numberOfDaysFromCapBeganToCapCompleted;
    private long numberOfDaysFromCapBeganToPresent;
    private long differenceFromCapCompletedAndCapMustBeCompleted;
    private Boolean deleted;

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

    public BrokenSurveillanceRulesDTO(BrokenSurveillanceRulesEntity entity) {
        this.id = entity.getId();
        this.developer = entity.getDeveloper();
        this.product = entity.getProduct();
        this.version = entity.getVersion();
        this.certificationBody = new CertificationBodyDTO(entity.getCertificationBody());
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
        this.nonconformityCriteria = entity.getNonconformityCriteria();
        this.dateOfDeterminationOfNonconformity = entity.getDateOfDeterminationOfNonconformity();
        this.nonConformityCloseDate = entity.getNonConformityCloseDate();
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
        this.differenceFromCapCompletedAndCapMustBeCompleted = entity.getDifferenceFromCapCompletedAndCapMustBeCompleted();
        this.deleted = entity.getDeleted();
    }
}

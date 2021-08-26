package gov.healthit.chpl.scheduler.presenter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.springframework.core.env.Environment;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.domain.surveillance.SurveillanceNonconformity;
import gov.healthit.chpl.domain.surveillance.SurveillanceRequirement;
import gov.healthit.chpl.service.CertificationCriterionService;

public class SurveillanceReportCsvPresenter extends SurveillanceCsvPresenter {

    public SurveillanceReportCsvPresenter(Environment env) {
        super(env);
    }

    @Override
    protected List<String> generateHeaderValues() {
        List<String> result = new ArrayList<String>();
        result.add("Developer");
        result.add("Product");
        result.add("Version");
        result.add("CHPL ID");
        result.add("URL");
        result.add("ONC-ACB");
        result.add("Certification Status");
        result.add("Date of Last Status Change");
        result.add("Surveillance ID");
        result.add("Date Surveillance Began");
        result.add("Date Surveillance Ended");
        result.add("Surveillance Type");
        result.add("Date surveillance last updated");
        result.add("Non-conformity (Y/N)");
        result.add("Non-conformity Criteria");
        result.add("Date of Determination of Non-Conformity");
        result.add("Corrective Action Plan Approved Date");
        result.add("Date Corrective Action Began");
        result.add("Date Corrective Action Must Be Completed");
        result.add("Date Corrective Action Was Completed");
        result.add("Date Non-conformity Was Closed");
        result.add("Number of Days from Determination to CAP Approval");
        result.add("Number of Days from Determination to Present");
        result.add("Number of Days from CAP Approval to CAP Began");
        result.add("Number of Days from CAP Approval to Present");
        result.add("Number of Days from CAP Began to CAP Completed");
        result.add("Number of Days from CAP Began to Present");
        result.add("Difference from CAP Completed and CAP Must Be Completed");
        result.add("Date non-conformity last updated");
        return result;
    }

    @Override
    protected List<List<String>> generateMultiRowValue(final CertifiedProductSearchDetails data,
            final Surveillance surv) {
        List<List<String>> result = new ArrayList<List<String>>();

        List<String> survFields = getSurveillanceFields(data, surv);

        if (surv.getRequirements() == null || surv.getRequirements().size() == 0) {
            List<String> row = new ArrayList<String>();
            row.addAll(survFields);
            row.addAll(getNoNonconformityFields(data, surv));
            result.add(row);
        } else {
            for (SurveillanceRequirement req : surv.getRequirements()) {
                List<SurveillanceNonconformity> relevantNonconformities = getNonconformities(req);
                if (relevantNonconformities == null || relevantNonconformities.size() == 0) {
                    List<String> row = new ArrayList<String>();
                    row.addAll(survFields);
                    row.addAll(getNoNonconformityFields(data, surv));
                    result.add(row);
                } else {
                    for (SurveillanceNonconformity nc : relevantNonconformities) {
                        List<String> row = new ArrayList<String>();
                        row.addAll(survFields);
                        row.addAll(getNonconformityFields(data, surv, nc));
                        result.add(row);
                    }
                }
            }
        }
        return result;
    }

    protected List<SurveillanceNonconformity> getNonconformities(final SurveillanceRequirement req) {
        return req.getNonconformities();
    }

    protected List<String> getSurveillanceFields(final CertifiedProductSearchDetails data,
            final Surveillance surv) {
        List<String> survFields = new ArrayList<String>();
        survFields.add(data.getDeveloper().getName());
        survFields.add(data.getProduct().getName());
        survFields.add(data.getVersion().getVersion());
        survFields.add(data.getChplProductNumber());
        String productDetailsUrl = getEnv().getProperty("chplUrlBegin").trim() + getEnv().getProperty("listingDetailsUrl") + data.getId();
        survFields.add(productDetailsUrl);
        survFields.add(data.getCertifyingBody().get(CertifiedProductSearchDetails.ACB_NAME_KEY).toString());
        survFields.add(data.getCurrentStatus().getStatus().getName());
        Long lastCertificationChangeMillis = data.getCurrentStatus().getEventDate();
        if (lastCertificationChangeMillis.longValue() == data.getCertificationDate().longValue()) {
            survFields.add("No status change");
        } else {
            LocalDateTime lastStatusChangeDate = LocalDateTime
                    .ofInstant(Instant.ofEpochMilli(lastCertificationChangeMillis), ZoneId.systemDefault());
            survFields.add(getDateFormatter().format(lastStatusChangeDate));
        }

        if (surv.getFriendlyId() != null) {
            survFields.add(surv.getFriendlyId());
        } else {
            survFields.add("");
        }
        if (surv.getStartDay() != null) {
            survFields.add(getDateFormatter().format(surv.getStartDay()));
        } else {
            survFields.add("");
        }
        if (surv.getEndDay() != null) {
            survFields.add(getDateFormatter().format(surv.getEndDay()));
        } else {
            survFields.add("");
        }
        survFields.add(surv.getType().getName());
        survFields.add(surv.getLastModifiedDate().toInstant().atOffset(ZoneOffset.UTC).format(getDateTimeFormatter()));
        return survFields;
    }

    protected List<String> getNoNonconformityFields(final CertifiedProductSearchDetails data,
            final Surveillance surv) {
        List<String> ncFields = new ArrayList<String>();
        ncFields.add("N");
        ncFields.add("");
        ncFields.add("");
        ncFields.add("");
        ncFields.add("");
        ncFields.add("");
        ncFields.add("");
        ncFields.add("");
        ncFields.add("");
        ncFields.add("");
        ncFields.add("");
        ncFields.add("");
        ncFields.add("");
        ncFields.add("");
        ncFields.add("");
        return ncFields;
    }

    protected List<String> getNonconformityFields(final CertifiedProductSearchDetails data,
            final Surveillance surv,
            final SurveillanceNonconformity nc) {
        List<String> ncFields = new ArrayList<String>();
        ncFields.add("Y");
        if (nc.getCriterion() != null) {
            ncFields.add(CertificationCriterionService.formatCriteriaNumber(nc.getCriterion()));
        } else if (nc.getNonconformityType() != null) {
            ncFields.add(nc.getNonconformityType());
        }
        if (nc.getDateOfDeterminationDay() != null) {
            ncFields.add(getDateFormatter().format(nc.getDateOfDeterminationDay()));
        } else {
            ncFields.add("");
        }
        if (nc.getCapApprovalDay() != null) {
            ncFields.add(getDateFormatter().format(nc.getCapApprovalDay()));
        } else {
            ncFields.add("");
        }
        if (nc.getCapStartDay() != null) {
            ncFields.add(getDateFormatter().format(nc.getCapStartDay()));
        } else {
            ncFields.add("");
        }
        if (nc.getCapMustCompleteDay() != null) {
            ncFields.add(getDateFormatter().format(nc.getCapMustCompleteDay()));
        } else {
            ncFields.add("");
        }
        if (nc.getCapEndDay() != null) {
            ncFields.add(getDateFormatter().format(nc.getCapEndDay()));
        } else {
            ncFields.add("");
        }
        if (nc.getNonconformityCloseDate() != null) {
            ncFields.add(getDateFormatter().format(nc.getNonconformityCloseDate()));
        } else {
            ncFields.add("");
        }

        if (nc.getCapApprovalDay() != null) {
            // calculate number of days from nc determination to cap approval date
            long daysBetween = ChronoUnit.DAYS.between(nc.getDateOfDeterminationDay(), nc.getCapApprovalDay());
            ncFields.add(daysBetween + "");
            ncFields.add("");
        } else {
            ncFields.add("");
            // calculate number of days between nc determination date and present
            long daysBetween = ChronoUnit.DAYS.between(nc.getDateOfDeterminationDay(), LocalDateTime.now());
            ncFields.add(daysBetween + "");
        }

        if (nc.getCapApprovalDay() != null && nc.getCapStartDay() != null) {
            long daysBetween = ChronoUnit.DAYS.between(nc.getCapApprovalDay(), nc.getCapStartDay());
            ncFields.add(daysBetween + "");
            ncFields.add("");
        } else if (nc.getCapApprovalDay() != null) {
            long daysBetween = ChronoUnit.DAYS.between(nc.getCapApprovalDay(), LocalDateTime.now());
            ncFields.add("");
            ncFields.add(daysBetween + "");
        } else {
            ncFields.add("");
            ncFields.add("");
        }

        if (nc.getCapStartDay() != null && nc.getCapEndDay() != null) {
            long daysBetween = ChronoUnit.DAYS.between(nc.getCapStartDay(), nc.getCapEndDay());
            ncFields.add(daysBetween + "");
            ncFields.add("");
        } else if (nc.getCapStartDay() != null) {
            long daysBetween = ChronoUnit.DAYS.between(nc.getCapStartDay(), LocalDateTime.now());
            ncFields.add("");
            ncFields.add(daysBetween + "");
        } else {
            ncFields.add("");
            ncFields.add("");
        }

        if (nc.getCapEndDay() != null && nc.getCapMustCompleteDay() != null) {
            long daysBetween = ChronoUnit.DAYS.between(nc.getCapMustCompleteDay(), nc.getCapEndDay());
            ncFields.add(daysBetween + "");
        } else {
            ncFields.add("N/A");
        }
        ncFields.add(nc.getLastModifiedDate().toInstant().atOffset(ZoneOffset.UTC).format(getDateTimeFormatter()));
        return ncFields;
    }
}

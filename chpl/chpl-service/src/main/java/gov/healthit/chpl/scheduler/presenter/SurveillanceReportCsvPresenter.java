package gov.healthit.chpl.scheduler.presenter;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.Surveillance;
import gov.healthit.chpl.domain.SurveillanceNonconformity;
import gov.healthit.chpl.domain.SurveillanceRequirement;

/**
 * Write out surveillance report with values related to time between dates.
 *
 */
public class SurveillanceReportCsvPresenter extends SurveillanceCsvPresenter {

    /**
     * Constructor with properties.
     * @param props the properties
     */
    public SurveillanceReportCsvPresenter(final Properties props) {
        super(props);
    }

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
        String productDetailsUrl = getProps().getProperty("chplUrlBegin").trim();
        if (!productDetailsUrl.endsWith("/")) {
            productDetailsUrl += "/";
        }
        productDetailsUrl += "#/product/" + data.getId();
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
        if (surv.getStartDate() != null) {
            LocalDateTime survStartDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(surv.getStartDate().getTime()),
                    ZoneId.systemDefault());
            survFields.add(getDateFormatter().format(survStartDate));
        } else {
            survFields.add("");
        }
        if (surv.getEndDate() != null) {
            LocalDateTime survEndDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(surv.getEndDate().getTime()),
                    ZoneId.systemDefault());
            survFields.add(getDateFormatter().format(survEndDate));
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
        ncFields.add(nc.getNonconformityType());
        LocalDateTime ncDeterminationDate = null;
        if (nc.getDateOfDetermination() != null) {
            ncDeterminationDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(nc.getDateOfDetermination().getTime()),
                    ZoneId.systemDefault());
            ncFields.add(getDateFormatter().format(ncDeterminationDate));
        } else {
            ncFields.add("");
        }
        LocalDateTime capApprovalDate = null;
        if (nc.getCapApprovalDate() != null) {
            capApprovalDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(nc.getCapApprovalDate().getTime()),
                    ZoneId.systemDefault());
            ncFields.add(getDateFormatter().format(capApprovalDate));
        } else {
            ncFields.add("");
        }
        LocalDateTime capStartDate = null;
        if (nc.getCapStartDate() != null) {
            capStartDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(nc.getCapStartDate().getTime()),
                    ZoneId.systemDefault());
            ncFields.add(getDateFormatter().format(capStartDate));
        } else {
            ncFields.add("");
        }
        LocalDateTime capMustCompleteDate = null;
        if (nc.getCapMustCompleteDate() != null) {
            capMustCompleteDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(nc.getCapMustCompleteDate().getTime()),
                    ZoneId.systemDefault());
            ncFields.add(getDateFormatter().format(capMustCompleteDate));
        } else {
            ncFields.add("");
        }
        LocalDateTime capEndDate = null;
        if (nc.getCapEndDate() != null) {
            capEndDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(nc.getCapEndDate().getTime()),
                    ZoneId.systemDefault());
            ncFields.add(getDateFormatter().format(capEndDate));
        } else {
            ncFields.add("");
        }

        if (capApprovalDate != null) {
            // calculate number of days from nc determination to cap approval
            // date
            Duration timeBetween = Duration.between(ncDeterminationDate, capApprovalDate);
            ncFields.add(timeBetween.toDays() + "");
            ncFields.add("");
        } else {
            ncFields.add("");
            // calculate number of days between nc determination date and
            // present
            Duration timeBetween = Duration.between(ncDeterminationDate, LocalDateTime.now());
            ncFields.add(timeBetween.toDays() + "");
        }

        if (capApprovalDate != null && capStartDate != null) {
            Duration timeBetween = Duration.between(capApprovalDate, capStartDate);
            ncFields.add(timeBetween.toDays() + "");
            ncFields.add("");
        } else if (capApprovalDate != null) {
            Duration timeBetween = Duration.between(capApprovalDate, LocalDateTime.now());
            ncFields.add("");
            ncFields.add(timeBetween.toDays() + "");
        } else {
            ncFields.add("");
            ncFields.add("");
        }

        if (capStartDate != null && capEndDate != null) {
            Duration timeBetween = Duration.between(capStartDate, capEndDate);
            ncFields.add(timeBetween.toDays() + "");
            ncFields.add("");
        } else if (capStartDate != null) {
            Duration timeBetween = Duration.between(capStartDate, LocalDateTime.now());
            ncFields.add("");
            ncFields.add(timeBetween.toDays() + "");
        } else {
            ncFields.add("");
            ncFields.add("");
        }

        if (capEndDate != null && capMustCompleteDate != null) {
            Duration timeBetween = Duration.between(capMustCompleteDate, capEndDate);
            ncFields.add(timeBetween.toDays() + "");
        } else {
            ncFields.add("N/A");
        }
        ncFields.add(nc.getLastModifiedDate().toInstant().atOffset(ZoneOffset.UTC).format(getDateTimeFormatter()));
        return ncFields;
    }
}

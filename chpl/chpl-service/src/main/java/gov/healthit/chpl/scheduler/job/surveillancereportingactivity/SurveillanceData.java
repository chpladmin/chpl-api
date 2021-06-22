package gov.healthit.chpl.scheduler.job.surveillancereportingactivity;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;

import lombok.Data;

@Data
public class SurveillanceData {

    private RecordType recordType;
    private String chplProductNumber;
    private String url;
    private String acbName;
    private String certificationStatus;
    private String developerName;
    private String productName;
    private String version;
    private String surveillanceId;
    private LocalDate surveillanceBegan;
    private LocalDate surveillanceEnded;
    private String surveillanceType;
    private Integer randonmizedSitesUsed;
    private String surveilledRequirementType;
    private String surveilledRequirement;
    private String surveillanceResult;
    private String nonconformityType;
    private String nonconformityStatus;
    private LocalDate dateOfDetermination;
    private LocalDate capApprovalDate;
    private LocalDate actionBeganDate;
    private LocalDate mustCompleteDate;
    private LocalDate wasCompleteDate;
    private String nonconformitySummary;
    private String nonconformityFindings;
    private Integer sitesPassed;
    private Integer totalSites;
    private String developerExplanation;
    private String resolutionDescription;

    private Integer timeToAssessConformity;
    private Integer timeToApproveCap;
    private Integer durationOfCap;
    private Boolean mustCompleteMet;
    private Integer timeFromCapApprovalToSurveillanceClose;
    private Integer timeFromCapCloseToSurveillanceClose;
    private Integer durationOfClosedSurveillance;

    private String dateFormat = "yyyy/MM/dd";

    public SurveillanceData(CSVRecord record) {
        this.recordType = record.get(0).equalsIgnoreCase("Update") ? RecordType.UPDATE : RecordType.SUBELEMENT;
        this.chplProductNumber = getValueFromRecord(record, "UNIQUE_CHPL_ID__C");
        this.url = getValueFromRecord(record, "URL");
        this.acbName = getValueFromRecord(record, "ACB_NAME");
        this.certificationStatus = getValueFromRecord(record, "CERTIFICATION_STATUS");
        this.developerName = getValueFromRecord(record, "DEVELOPER_NAME");
        this.productName = getValueFromRecord(record, "PRODUCT_NAME");
        this.version = getValueFromRecord(record, "PRODUCT_VERSION");
        this.surveillanceId = getValueFromRecord(record, "SURVEILLANCE_ID");
        this.surveillanceBegan = convertStringToLocalDate(getValueFromRecord(record, "SURVEILLANCE_BEGAN"));
        this.surveillanceEnded = convertStringToLocalDate(getValueFromRecord(record, "SURVEILLANCE_ENDED"));
        this.surveillanceType = getValueFromRecord(record, "SURVEILLANCE_TYPE");
        this.randonmizedSitesUsed = convertStringToInteger(getValueFromRecord(record, "RANDOMIZED_SITES_USED"));
        this.surveilledRequirementType = getValueFromRecord(record, "SURVEILLED_REQUIREMENT_TYPE");
        this.surveilledRequirement = getValueFromRecord(record, "SURVEILLED_REQUIREMENT");
        this.surveillanceResult = getValueFromRecord(record, "SURVEILLANCE_RESULT");
        this.nonconformityType = getValueFromRecord(record, "NON_CONFORMITY_TYPE");
        this.nonconformityStatus = getValueFromRecord(record, "NON_CONFORMITY_STATUS");
        this.dateOfDetermination = convertStringToLocalDate(getValueFromRecord(record, "DATE_OF_DETERMINATION"));
        this.capApprovalDate = convertStringToLocalDate(getValueFromRecord(record, "CAP_APPROVAL_DATE"));
        this.actionBeganDate = convertStringToLocalDate(getValueFromRecord(record, "ACTION_BEGAN_DATE"));
        this.mustCompleteDate = convertStringToLocalDate(getValueFromRecord(record, "MUST_COMPLETE_DATE"));
        this.wasCompleteDate = convertStringToLocalDate(getValueFromRecord(record, "WAS_COMPLETE_DATE"));
        this.nonconformitySummary = getValueFromRecord(record, "NON_CONFORMITY_SUMMARY");
        this.nonconformityFindings = getValueFromRecord(record, "NON_CONFORMITY_FINDINGS");
        this.sitesPassed = convertStringToInteger(getValueFromRecord(record, "SITES_PASSED"));
        this.totalSites = convertStringToInteger(getValueFromRecord(record, "TOTAL_SITES"));
        this.developerExplanation = getValueFromRecord(record, "DEVELOPER_EXPLANATION");
        this.resolutionDescription = getValueFromRecord(record, "RESOLUTION_DESCRIPTION");

        this.timeToAssessConformity = getDateDiff(this.surveillanceBegan, this.dateOfDetermination);
        this.timeToApproveCap = getDateDiff(this.dateOfDetermination, this.capApprovalDate);
        this.durationOfCap = getDateDiff(this.capApprovalDate, this.wasCompleteDate);
        this.timeFromCapApprovalToSurveillanceClose = getDateDiff(this.capApprovalDate, this.surveillanceEnded);
        this.timeFromCapCloseToSurveillanceClose = getDateDiff(this.wasCompleteDate, this.surveillanceEnded);
        this.durationOfClosedSurveillance = getDateDiff(this.surveillanceBegan, this.surveillanceEnded);
    }

    private Integer getDateDiff(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return null;
        } else {
            return Statistics.getDateDiff(startDate, endDate);
        }
    }

    private String getValueFromRecord(CSVRecord record, String column) {
        if (record.toMap().containsKey(column)) {
            return record.get(column);
        } else {
            return "";
        }
    }

    private LocalDate convertStringToLocalDate(String dateAsString) {
        if (StringUtils.isEmpty(dateAsString)) {
            return null;
        } else {
            return LocalDate.parse(dateAsString, DateTimeFormatter.ofPattern(dateFormat));
        }
    }

    private Integer convertStringToInteger(String integerAsString) {
        if (StringUtils.isEmpty(integerAsString)) {
            return null;
        } else {
            return Integer.getInteger(integerAsString);
        }
    }

    public enum RecordType {
        UPDATE("Update"),
        SUBELEMENT("Subelement");

        private String recordType;

        RecordType(String recordType) {
            this.recordType = recordType;
        }

        @Override
        public String toString() {
            return recordType;
        }
    }
}

package gov.healthit.chpl.scheduler.job.complaints;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.complaint.ComplaintListingMap;
import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.util.DateUtil;
import gov.healthit.chpl.util.Util;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "complaintsReportJobLogger")
@Component
public class ComplaintsReportCsvCreator {

    private Environment env;

    @Autowired
    public ComplaintsReportCsvCreator(Environment env) {
        this.env = env;
    }

    private static final String NEW_LINE_SEPARATOR = "\n";

    public File createCsvFile(List<ComplaintsReportItem> reports) throws IOException {
        CSVFormat csvFileFormat = CSVFormat.DEFAULT.builder()
                .setRecordSeparator(NEW_LINE_SEPARATOR)
                .build();

        File csvFile = getOutputFile();
        try (FileWriter fileWriter = new FileWriter(csvFile);
                CSVPrinter csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat)) {

            fileWriter.write('\ufeff');
            csvFilePrinter.printRecord(getHeaderRow());

            reports.stream()
                .sorted((cri1, cri2) -> cri1.getComplaint().getId().compareTo(cri2.getComplaint().getId()))
                .forEach(report -> printRows(csvFilePrinter, report));
        }
        return csvFile;
    }

    private File getOutputFile() {
        File temp = null;
        try {
            temp = File.createTempFile(getFilename(), ".csv");
            temp.deleteOnExit();
        } catch (IOException ex) {
            LOGGER.error("Could not create temporary file " + ex.getMessage(), ex);
        }

        return temp;
    }

    private List<String> getHeaderRow() {
        return Arrays.asList(
                "Complaint ID",
                "ONC-ACB",
                "Complainant Type",
                "Complainant Type - Other",
                "ONC Complaint ID",
                "ONC-ACB Complaint ID",
                "Received Date",
                "Complaint Summary",
                "Actions/Responses",
                "Complainant Contacted",
                "Developer Contacted",
                "ONC-ATL Contacted",
                "Informed ONC Per §170.523(S)",
                "Closed Date",
                "Developer Name",
                "Product Name",
                "Version",
                "Associated Listings",
                "Associated Criteria",
                "Associated Surveillance",
                "Surveillance Result",
                "Non-Conformity Type",
                "Count of Non-Conformities");
    }

    private List<List<String>> getRows(ComplaintsReportItem report) {
        List<List<String>> rows = new ArrayList<List<String>>();
        List<String> complaintFields = getComplaintFields(report);
        if (!CollectionUtils.isEmpty(report.getComplaint().getListings())) {
            report.getComplaint().getListings().stream()
                .forEach(associatedListing -> rows.add(getRowForListing(complaintFields, associatedListing, report)));
        } else {
            rows.add(getRow(complaintFields, report));
        }
        return rows;
    }

    private List<String> getRowForListing(List<String> complaintFields, ComplaintListingMap associatedListing, ComplaintsReportItem report) {
        List<String> row = new ArrayList<String>();
        row.addAll(complaintFields);
        row.add(associatedListing.getDeveloperName());
        row.add(associatedListing.getProductName());
        row.add(associatedListing.getVersionName());
        row.add(associatedListing.getChplProductNumber());
        if (!CollectionUtils.isEmpty(report.getComplaint().getCriteria())) {
            row.add(report.getComplaint().getCriteria().stream()
                    .map(criterion -> Util.formatCriteriaNumber(criterion.getCertificationCriterion()))
                    .collect(Collectors.joining(", ")));
        } else {
            row.add("");
        }
        if (!CollectionUtils.isEmpty(report.getRelatedSurveillance())
                && hasSurveillanceRelatedToListing(report.getRelatedSurveillance(), associatedListing.getChplProductNumber())) {
            row.add(report.getRelatedSurveillance().stream()
                    .filter(surv -> isSurveillanceRelatedToListing(surv, associatedListing.getChplProductNumber()))
                    .sorted(Comparator.comparing(Surveillance::getFriendlyId))
                    .map(surv -> surv.getFriendlyId())
                    .collect(Collectors.joining(", ")));

            row.add(report.getRelatedSurveillance().stream()
                    .filter(surv -> isSurveillanceRelatedToListing(surv, associatedListing.getChplProductNumber()))
                    .sorted(Comparator.comparing(Surveillance::getFriendlyId))
                    .map(surv -> surv.getFriendlyId() + ":" + getSurveillanceResult(surv))
                    .collect(Collectors.joining(", ")));

            row.add(report.getRelatedSurveillance().stream()
                    .filter(surv -> isSurveillanceRelatedToListing(surv, associatedListing.getChplProductNumber()))
                    .filter(surv -> doesSurveillanceHaveNonconformities(surv))
                    .sorted(Comparator.comparing(Surveillance::getFriendlyId))
                    .map(surv -> surv.getFriendlyId() + ":" + getNonConformityTypes(surv))
                    .collect(Collectors.joining("; ")));

            long countNonconformities = report.getRelatedSurveillance().stream()
                    .filter(surv -> isSurveillanceRelatedToListing(surv, associatedListing.getChplProductNumber()))
                    .flatMap(surv -> surv.getRequirements().stream())
                    .filter(survReq -> !CollectionUtils.isEmpty(survReq.getNonconformities()))
                    .flatMap(survReq -> survReq.getNonconformities().stream())
                    .count();
            row.add(countNonconformities + "");
        } else {
            row.add("");
            row.add("");
            row.add("");
            row.add("");
        }
        return row;
    }

    private String getSurveillanceResult(Surveillance surv) {
        boolean anyRequirementHasNonConformity = surv.getRequirements().stream()
            .filter(req -> !CollectionUtils.isEmpty(req.getNonconformities()))
            .findAny().isPresent();
        if (anyRequirementHasNonConformity) {
            return "Non-Conformity";
        }
        return "No Non-Conformity";
    }

    private boolean doesSurveillanceHaveNonconformities(Surveillance surv) {
        return surv.getRequirements().stream()
        .filter(survReq -> !CollectionUtils.isEmpty(survReq.getNonconformities()))
        .flatMap(survReq -> survReq.getNonconformities().stream())
        .count() > 0;
    }

    private String getNonConformityTypes(Surveillance surv) {
        return surv.getRequirements().stream()
                .flatMap(req -> req.getNonconformities().stream())
                //TODO - TMY - Need to handle Cures criteria (OCD-4029)
                //.map(nc -> nc.getNonconformityType())
                .map(nc -> nc.getType().getNumber())
                .collect(Collectors.joining(","));
    }

    private List<String> getRow(List<String> complaintFields, ComplaintsReportItem report) {
        List<String> row = new ArrayList<String>();
        row.addAll(complaintFields);
        row.add("");
        row.add("");
        row.add("");
        row.add("");
        if (!CollectionUtils.isEmpty(report.getComplaint().getCriteria())) {
            row.add(report.getComplaint().getCriteria().stream()
                    .map(criterion -> Util.formatCriteriaNumber(criterion.getCertificationCriterion()))
                    .collect(Collectors.joining(", ")));
        } else {
            row.add("");
        }

        row.add("");
        row.add("");
        row.add("");
        row.add("");
        return row;
    }

    private boolean hasSurveillanceRelatedToListing(Set<Surveillance> surveillance, String chplProductNumber) {
        return surveillance.stream()
                .filter(surv -> isSurveillanceRelatedToListing(surv, chplProductNumber))
                .findAny().isPresent();
    }

    private boolean isSurveillanceRelatedToListing(Surveillance surv, String chplProductNumber) {
        return surv.getCertifiedProduct().getChplProductNumber().equals(chplProductNumber);
    }

    private List<String> getComplaintFields(ComplaintsReportItem report) {
        return Arrays.asList(report.getComplaint().getId().toString(),
                report.getComplaint().getCertificationBody().getName(),
                report.getComplaint().getComplainantType() != null ? report.getComplaint().getComplainantType().getName() : "",
                report.getComplaint().getComplainantTypeOther(),
                report.getComplaint().getOncComplaintId(),
                report.getComplaint().getAcbComplaintId(),
                printDate(report.getComplaint().getReceivedDate()),
                report.getComplaint().getSummary(),
                report.getComplaint().getActions(),
                printBoolean(report.getComplaint().isComplainantContacted()),
                printBoolean(report.getComplaint().isDeveloperContacted()),
                printBoolean(report.getComplaint().isOncAtlContacted()),
                printBoolean(report.getComplaint().isFlagForOncReview()),
                printDate(report.getComplaint().getClosedDate()));
    }

    private void printRows(CSVPrinter csvFilePrinter, ComplaintsReportItem reportItem) {
            List<List<String>> rowsForItem = getRows(reportItem);
            rowsForItem.stream()
                .forEach(row -> printRow(csvFilePrinter, row));
    }

    private String printDate(LocalDate value) {
        if (value != null) {
            return DateUtil.format(value);
        }
        return "";
    }

    private String printBoolean(boolean value) {
        if (BooleanUtils.isTrue(value)) {
            return "TRUE";
        }
        return "FALSE";
    }

    private void printRow(CSVPrinter csvFilePrinter, List<String> row) {
        try {
            csvFilePrinter.printRecord(row);
        } catch (IOException e) {
            LOGGER.catching(e);
        }
    }

    private String getFilename() {
        return env.getProperty("complaintsReport.filename") + LocalDate.now().toString();
    }

    private String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
        return sdf.format(date);
    }
}

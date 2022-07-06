package gov.healthit.chpl.scheduler.job.complaints;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.util.DateUtil;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "complaintsReportEmailJobLogger")
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
                "complaint_id",
                "ONC-ACB Complainant Type Name",
                "complainant_type_other",
                "onc_complaint_id",
                "acb_complaint_id",
                "received_date",
                "summary",
                "actions",
                "complainant_contacted",
                "developer_contacted",
                "onc_atl_contacted",
                "informed_onc_per_170.523(s)",
                "closed_date",
                "developer_name",
                "product_name",
                "version",
                "associated_listings",
                "associated_surveillance",
                "associated_criteria",
                "Surveillance result",
                "Nonconformity type",
                "Count of Non-Conformities");
    }

    private List<List<String>> getRows(ComplaintsReportItem report) {
        List<List<String>> rows = new ArrayList<List<String>>();
        if (CollectionUtils.isEmpty(report.getComplaint().getListings())) {
            rows.add(getRow(report));
        } else {
            report.getComplaint().getListings().stream()
                .forEach(listing -> rows.add(getRow(report, listing)));
        }
        return rows;
    }

    private List<String> getRow(ComplaintsReportItem report) {
        return Arrays.asList(report.getComplaint().getId().toString(),
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

package gov.healthit.chpl.scheduler.presenter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.healthit.chpl.domain.CertificationEdition;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.scheduler.job.svap.ListingSvapActivity;
import gov.healthit.chpl.svap.domain.Svap;
import gov.healthit.chpl.util.Util;
import lombok.Setter;

public class SvapActivityPresenter implements AutoCloseable {
    private static final String DATE_PATTERN = "yyyy-MM-dd";
    @Setter
    private Logger logger;
    private OutputStreamWriter writer = null;
    private CSVPrinter csvPrinter = null;

    public void open(File file) throws IOException {
        getLogger().info("Opening file, initializing CSV document");
        writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
        writer.write('\ufeff');
        csvPrinter = new CSVPrinter(writer, CSVFormat.EXCEL);
        csvPrinter.printRecord(generateHeaderValues());
        csvPrinter.flush();
    }

    public synchronized void addAll(List<ListingSvapActivity> activities) {
        activities.forEach(activity -> {
            try {
                add(activity);
            } catch (IOException ex) {
                getLogger().error("An activity was not added to the CSV.");
                getLogger().catching(ex);
            }
        });
    }

    public synchronized void add(ListingSvapActivity data) throws IOException {
        getLogger().info("Adding data to CSV file for listing ID: " + data.getListing().getId());
        List<String> rowValue = generateRowValue(data);
        if (rowValue != null) {
            csvPrinter.printRecord(rowValue);
            csvPrinter.flush();
        }
    }

    @Override
    public void close() throws IOException {
        getLogger().info("Closing the CSV file.");
        csvPrinter.close();
        writer.close();
    }

    public Logger getLogger() {
        if (logger == null) {
            logger = LogManager.getLogger(SvapActivityPresenter.class);
        }
        return logger;
    }

    protected List<String> generateHeaderValues() {
        return Stream.of(
                "Certification Edition",
                "CHPL ID",
                "Listing Database ID",
                "ONC-ACB Certification ID",
                "Certification Date",
                "Certification Status",
                "ACB Name",
                "Developer Name",
                "Developer Database ID",
                "Product Name",
                "Product Database ID",
                "Version",
                "Version Database ID",
                "SVAP Notice Current URL",
                "SVAP Notice Last Update",
                "Criterion",
                "Reg Text Citation",
                "SVAP Value",
                "SVAP Last Update",
                "SVAP Certification",
                "SVAP Value Status").collect(Collectors.toList());
    }

    @SuppressWarnings("checkstyle:linelength")
    protected List<String> generateRowValue(ListingSvapActivity svapActivity) {
        return Stream.of(formatEdition(svapActivity.getListing()),
                svapActivity.getListing().getChplProductNumber(),
                svapActivity.getListing().getId().toString(),
                svapActivity.getListing().getAcbCertificationId(),
                formatDate(svapActivity.getListing().getCertificationDate()),
                svapActivity.getListing().getCurrentStatus().getStatus().getName(),
                MapUtils.getString(svapActivity.getListing().getCertifyingBody(), CertifiedProductSearchDetails.ACB_NAME_KEY),
                svapActivity.getListing().getDeveloper().getName(),
                svapActivity.getListing().getDeveloper().getId().toString(),
                svapActivity.getListing().getProduct().getName(),
                svapActivity.getListing().getProduct().getId().toString(),
                svapActivity.getListing().getVersion().getVersion(),
                svapActivity.getListing().getVersion().getId().toString(),
                svapActivity.getListing().getSvapNoticeUrl(),
                formatDate(svapActivity.getSvapNoticeLastUpdated()),
                svapActivity.getCriterion() != null ? Util.formatCriteriaNumber(svapActivity.getCriterion()) : "",
                svapActivity.getCriterionSvap() != null ? svapActivity.getCriterionSvap().getRegulatoryTextCitation() : "",
                svapActivity.getCriterionSvap() != null ? svapActivity.getCriterionSvap().getApprovedStandardVersion() : "",
                formatDate(svapActivity.getCriterionSvapLastUpdated()),
                formatSvapNewOrUpdated(svapActivity),
                formatReplaced(svapActivity.getCriterionSvap()))
                .collect(Collectors.toList());
    }

    private String formatEdition(CertifiedProductSearchDetails listing) {
        String edition = MapUtils.getString(listing.getCertificationEdition(), CertifiedProductSearchDetails.EDITION_NAME_KEY);
        if (listing.getCuresUpdate() != null && listing.getCuresUpdate()) {
            edition = edition + CertificationEdition.CURES_SUFFIX;
        }
        return edition;
    }

    private String formatReplaced(Svap svap) {
        if (svap == null) {
            return "";
        }
        return svap.isReplaced() ? "Replaced" : "Current";
    }

    private String formatSvapNewOrUpdated(ListingSvapActivity svapActivity) {
        if (svapActivity.getWasCriterionAttestedToBeforeSvapAdded() == null) {
            return "";
        }
        return svapActivity.getWasCriterionAttestedToBeforeSvapAdded() ? "Updated" : "New";
    }

    private String formatDate(LocalDate date) {
        if (date == null) {
            return "";
        }
        return DateTimeFormatter.ofPattern(DATE_PATTERN).format(date);
    }

    private String formatDate(Long dateInMillis) {
        if (dateInMillis == null) {
            return "";
        }

        LocalDateTime localDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(dateInMillis), ZoneId.systemDefault());
        return DateTimeFormatter.ofPattern(DATE_PATTERN).format(localDate);
    }
}

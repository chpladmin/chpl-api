package gov.healthit.chpl.scheduler.presenter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.CertificationCriterionDTO;

/**
 * Present objects as CSV file.
 *
 * @author alarned
 *
 */
public class CertifiedProductCsvPresenter implements CertifiedProductPresenter, AutoCloseable {
    private Logger logger;
    private List<CertificationCriterionDTO> applicableCriteria = new ArrayList<CertificationCriterionDTO>();
    private OutputStreamWriter writer = null;
    private CSVPrinter csvPrinter = null;

    /**
     * Required to setCriteriaNames before calling this function.
     */
    @Override
    public void open(final File file) throws IOException {
        getLogger().info("Opening file, initializing CSV doc.");
        writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
        writer.write('\ufeff');
        csvPrinter = new CSVPrinter(writer, CSVFormat.EXCEL);
        csvPrinter.printRecord(generateHeaderValues());
        csvPrinter.flush();
    }

    @Override
    public synchronized void add(final CertifiedProductSearchDetails data) throws IOException {
        getLogger().info("Adding CP to CSV file: " + data.getId());
        List<String> rowValue = generateRowValue(data);
        if (rowValue != null) { // a subclass could return null to skip a row
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

    public void setLogger(final Logger logger) {
        this.logger = logger;
    }

    public Logger getLogger() {
        if (logger == null) {
            logger = LogManager.getLogger(CertifiedProductXmlPresenter.class);
        }
        return logger;
    }

    protected List<String> generateHeaderValues() {
        List<String> result = new ArrayList<String>();
        result.add("Certification Edition");
        result.add("CHPL ID");
        result.add("ONC-ACB Certification ID");
        result.add("Certification Date");
        result.add("Certification Status");
        result.add("ACB Name");
        result.add("Previous ACB Name");
        result.add("Developer Name");
        result.add("Vendor Street Address");
        result.add("Vendor City");
        result.add("Vendor State");
        result.add("Vendor Zip Code");
        result.add("Vendor Website");
        result.add("Self-developer");
        result.add("Vendor Contact Name");
        result.add("Vendor Contact Email");
        result.add("Vendor Contact Phone");
        result.add("Product Name");
        result.add("Version");
        result.add("Total Surveillance Activities");
        result.add("Total Nonconformities");
        result.add("Open Nonconformities");

        if (applicableCriteria != null) {
            for (CertificationCriterionDTO criteria : applicableCriteria) {
                result.add(criteria.getNumber() + ": " + criteria.getTitle());
            }
        }
        return result;
    }

    protected List<String> generateRowValue(final CertifiedProductSearchDetails data) {
        List<String> result = new ArrayList<String>();
        result.add(data.getCertificationEdition().get(CertifiedProductSearchDetails.EDITION_NAME_KEY).toString());
        result.add(data.getChplProductNumber());
        result.add(data.getAcbCertificationId());
        LocalDateTime date = LocalDateTime.ofInstant(Instant.ofEpochMilli(data.getCertificationDate()),
                ZoneId.systemDefault());
        result.add(DateTimeFormatter.ISO_LOCAL_DATE.format(date));
        result.add(data.getCurrentStatus().getStatus().getName());
        result.add(data.getCertifyingBody().get(CertifiedProductSearchDetails.ACB_NAME_KEY).toString());
        result.add(data.getOtherAcb());
        result.add(data.getDeveloper().getName());
        if (data.getDeveloper().getAddress() != null) {
            if (data.getDeveloper().getAddress().getLine1() != null
                    && data.getDeveloper().getAddress().getLine2() != null) {
                result.add(data.getDeveloper().getAddress().getLine1()
                        + data.getDeveloper().getAddress().getLine2());
            } else {
                result.add(data.getDeveloper().getAddress().getLine1() == null
                        ? ""
                        : data.getDeveloper().getAddress().getLine1());
            }
            result.add(data.getDeveloper().getAddress().getCity() == null
                    ? ""
                    : data.getDeveloper().getAddress().getCity());
            result.add(data.getDeveloper().getAddress().getState() == null
                    ? ""
                    : data.getDeveloper().getAddress().getState());
            result.add(data.getDeveloper().getAddress().getZipcode() == null
                    ? ""
                    : data.getDeveloper().getAddress().getZipcode());
        } else {
            result.add("");
            result.add("");
            result.add("");
            result.add("");
        }
        result.add(data.getDeveloper().getWebsite() == null
                ? ""
                : data.getDeveloper().getWebsite());
        result.add(data.getDeveloper().getSelfDeveloper() ? "Yes" : "No");
        if (data.getProduct().getContact() != null) {
            result.add(data.getProduct().getContact().getFullName() == null
                    ? ""
                    : data.getProduct().getContact().getFullName());
            result.add(data.getProduct().getContact().getEmail() == null
                    ? ""
                    : data.getProduct().getContact().getEmail());
            result.add(data.getProduct().getContact().getPhoneNumber() == null
                    ? ""
                    : data.getProduct().getContact().getPhoneNumber());
        } else if (data.getDeveloper().getContact() != null) {
            result.add(data.getDeveloper().getContact().getFullName() == null
                    ? ""
                    : data.getDeveloper().getContact().getFullName());
            result.add(data.getDeveloper().getContact().getEmail() == null
                    ? ""
                    : data.getDeveloper().getContact().getEmail());
            result.add(data.getDeveloper().getContact().getPhoneNumber() == null
                    ? ""
                    : data.getDeveloper().getContact().getPhoneNumber());
        } else {
            result.add("");
            result.add("");
            result.add("");
        }
        result.add(data.getProduct().getName());
        result.add(data.getVersion().getVersion());
        result.add(data.getCountSurveillance().toString());
        result.add((data.getCountOpenNonconformities() + data.getCountClosedNonconformities()) + "");
        result.add(data.getCountOpenNonconformities().toString());
        List<String> criteria = generateCriteriaValues(data);
        result.addAll(criteria);
        return result;
    }

    protected List<String> generateCriteriaValues(final CertifiedProductSearchDetails data) {
        List<String> result = new ArrayList<String>();

        for (CertificationCriterionDTO criteria : applicableCriteria) {
            boolean criteriaMatch = false;
            for (int i = 0; i < data.getCertificationResults().size() && !criteriaMatch; i++) {
                CertificationResult currCriteria = data.getCertificationResults().get(i);
                if (currCriteria.getCriterion().getId().equals(criteria.getId())) {
                    criteriaMatch = true;
                    result.add(currCriteria.isSuccess().toString());
                }
            }
        }
        return result;
    }

    public List<CertificationCriterionDTO> getApplicableCriteria() {
        return applicableCriteria;
    }

    public void setApplicableCriteria(final List<CertificationCriterionDTO> applicableCriteria) {
        this.applicableCriteria = applicableCriteria;
    }
}

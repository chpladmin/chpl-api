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
 * @author alarned
 *
 */
public class CertifiedProductCsvPresenter implements CertifiedProductPresenter {
    private Logger logger;
    private List<CertificationCriterionDTO> applicableCriteria = new ArrayList<CertificationCriterionDTO>();
    
    OutputStreamWriter writer = null;
    CSVPrinter csvPrinter = null;
    
    /**
     * Required to setCriteriaNames before calling this function.
     */
    @Override
    public void open(final File file) throws IOException {
        getLogger().info("Opening file, initializing CSV doc.");
        writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
        csvPrinter = new CSVPrinter(writer, CSVFormat.EXCEL);
        csvPrinter.printRecord(generateHeaderValues());
        csvPrinter.flush();
    }
    
    @Override
    public void add(final CertifiedProductSearchDetails data) throws IOException {
        getLogger().info("Adding CP to CSV file: " + data.getId());
        List<String> rowValue = generateRowValue(data);
        if (rowValue != null) { // a subclass could return null to skip a row
            csvPrinter.printRecord(rowValue);
            csvPrinter.flush();
        }
    }
    
    @Override
    public void close() throws IOException {
        getLogger().info("Closing the XML file.");
        csvPrinter.close();
        writer.close();
    }
    
    public void setLogger(Logger logger) {
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
        result.add("Product Name");
        result.add("Version");
        result.add("Total Surveillance Activities");
        result.add("Total Nonconformities");
        result.add("Open Nonconformities");

        if (applicableCriteria != null) {
            for (CertificationCriterionDTO criteria : applicableCriteria) {
                result.add(criteria.getNumber());
            }
        }
        return result;
    }

    protected List<String> generateRowValue(final CertifiedProductSearchDetails data) {
        List<String> result = new ArrayList<String>();
        result.add(data.getCertificationEdition().get("name").toString());
        result.add(data.getChplProductNumber());
        result.add(data.getAcbCertificationId());
        LocalDateTime date = LocalDateTime.ofInstant(Instant.ofEpochMilli(data.getCertificationDate()),
                ZoneId.systemDefault());
        result.add(DateTimeFormatter.ISO_LOCAL_DATE.format(date));
        result.add(data.getCurrentStatus().getStatus().getName());
        result.add(data.getCertifyingBody().get("name").toString());
        result.add(data.getOtherAcb());
        result.add(data.getDeveloper().getName());
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
                if (currCriteria.getNumber().equals(criteria.getNumber())) {
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

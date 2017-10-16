package gov.healthit.chpl.app.presenter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.CertifiedProductDownloadResponse;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.SEDRow;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.dto.CertificationResultDTO;
import gov.healthit.chpl.dto.CertificationResultTestTaskDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.dto.TestParticipantDTO;
import gov.healthit.chpl.dto.TestTaskDTO;

public class CertifiedProductCsvPresenter implements CertifiedProductPresenter {
    private static final Logger LOGGER = LogManager.getLogger(CertifiedProduct2014CsvPresenter.class);
    private List<CertificationCriterionDTO> applicableCriteria = new ArrayList<CertificationCriterionDTO>();
    Long id = -1L;
	int i;

    /**
     * Required to setCriteriaNames before calling this function. Returns number
     * of rows printed (minus the header)
     */
    @Override
    public int presentAsFile(File file, CertifiedProductDownloadResponse cpList) {
        int numRows = 0;
        FileWriter writer = null;
        CSVPrinter csvPrinter = null;
        try {
            writer = new FileWriter(file);
            csvPrinter = new CSVPrinter(writer, CSVFormat.EXCEL);
            
            csvPrinter.printRecord(generateHeaderValues());

            for (CertifiedProductSearchDetails data : cpList.getListings()) {
                List<String> rowValue = generateRowValue(data);
                if (rowValue != null) { // a subclass could return null to skip
                                        // a row
                    csvPrinter.printRecord(rowValue);
                    numRows++;
                }
            }
        } catch (final IOException ex) {
            LOGGER.error("Could not write file " + file.getName(), ex);
        } finally {
            try {
                writer.flush();
                writer.close();
                csvPrinter.flush();
                csvPrinter.close();
            } catch (Exception ignore) {
            }
        }
        return numRows;
    }
    
    @Override
    public int presentAsFileSED(File file, ArrayList<SEDRow> result) {
        int numRows = 0;
        FileWriter writer = null;
        CSVPrinter csvPrinter = null;
        try {
            writer = new FileWriter(file);
            csvPrinter = new CSVPrinter(writer, CSVFormat.EXCEL);
            
            csvPrinter.printRecord(generateHeaderValuesSED());

            for (SEDRow data : result) {
                List<String> rowValue = generateRowValueSED(data.getDetails(), data.getCertificationResult(), data.getTestTask(), data.getCriteria());
                if (rowValue != null) { // a subclass could return null to skip
                                        // a row
                    csvPrinter.printRecord(rowValue);
                    numRows++;
                }
            }
        } catch (final IOException ex) {
            LOGGER.error("Could not write file " + file.getName(), ex);
        } finally {
            try {
                writer.flush();
                writer.close();
                csvPrinter.flush();
                csvPrinter.close();
            } catch (Exception ignore) {
            }
        }
        return numRows;
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
    
    protected List<String> generateHeaderValuesSED() {
        List<String> result = new ArrayList<String>();
        result.add("Unique CHPL ID");
        result.add("Developer");
        result.add("Product");
        result.add("Version");
        result.add("Certification Criteria");
        result.add("Task Description");
        result.add("Rating Scale");
        result.add("Task Rating");
        result.add("Task Rating - Standard Deviation");
        result.add("Task Time Mean (s)");
        result.add("Task Time - Standard Deviation (s)");
        result.add("Task Time Deviation - Observed (s)");
        result.add("Task Time Deviation - Optimal (s)");
        result.add("Task Success");
        result.add("Task Success - Standard Deviation (%)");
        result.add("Task Errors - Mean (%)");
        result.add("Task Errors - Standard Deviation (%)");
        result.add("Task Path Deviation - Observed (# of Steps)");
        result.add("Task Path Deviation - Optimal (# of Steps)");
        result.add("Occupation");
        result.add("Education Type");
        result.add("Product Experience (Months)");
        result.add("Professional Experience (Months)");
        result.add("Computer Experience (Months)");
        result.add("Age (Years)");
        result.add("Gender");
        result.add("Assistive Technology Needs");
        return result;
    }

    protected List<String> generateRowValue(CertifiedProductSearchDetails data) {
        List<String> result = new ArrayList<String>();
        result.add(data.getCertificationEdition().get("name").toString());
        result.add(data.getChplProductNumber());
        result.add(data.getAcbCertificationId());
        LocalDateTime date = LocalDateTime.ofInstant(Instant.ofEpochMilli(data.getCertificationDate()),
                ZoneId.systemDefault());
        result.add(DateTimeFormatter.ISO_LOCAL_DATE.format(date));
        result.add(data.getCertificationStatus().get("name").toString());
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
    
    protected List<String> generateRowValueSED(CertifiedProductDetailsDTO details, CertificationResultDTO certificationResult, CertificationResultTestTaskDTO testTask, String criteria) {
    	if(id != testTask.getId()){
    		id = testTask.getId();
    		i = 0;
    	}
        List<String> result = new ArrayList<String>();
        CertifiedProduct cp = new CertifiedProduct(details);
        result.add(cp.getChplProductNumber());
        result.add(details.getDeveloper().getName());
        result.add(details.getProduct().getName());
        result.add(details.getVersion().getVersion());
        result.add(criteria);
        result.add(testTask.getTestTask().getDescription());
        result.add(testTask.getTestTask().getTaskRatingScale());
        result.add(String.valueOf(testTask.getTestTask().getTaskRating()));
        result.add(String.valueOf(testTask.getTestTask().getTaskRatingStddev()));
        result.add(String.valueOf(testTask.getTestTask().getTaskTimeAvg()));
        result.add(String.valueOf(testTask.getTestTask().getTaskTimeStddev()));
        result.add(String.valueOf(testTask.getTestTask().getTaskTimeDeviationObservedAvg()));
        result.add(String.valueOf(testTask.getTestTask().getTaskTimeDeviationOptimalAvg()));
        result.add(String.valueOf(testTask.getTestTask().getTaskSuccessAverage()));
        result.add(String.valueOf(testTask.getTestTask().getTaskSuccessStddev()));
        result.add(String.valueOf(testTask.getTestTask().getTaskErrors()));
        result.add(String.valueOf(testTask.getTestTask().getTaskErrorsStddev()));
        result.add(String.valueOf(testTask.getTestTask().getTaskPathDeviationObserved()));
        result.add(String.valueOf(testTask.getTestTask().getTaskPathDeviationOptimal()));
        result.add(testTask.getTestTask().getParticipants().get(i).getOccupation());
        result.add(testTask.getTestTask().getParticipants().get(i).getEducationType().getName());
        result.add(String.valueOf(testTask.getTestTask().getParticipants().get(i).getProductExperienceMonths()));
        result.add(String.valueOf(testTask.getTestTask().getParticipants().get(i).getProfessionalExperienceMonths()));
        result.add(String.valueOf(testTask.getTestTask().getParticipants().get(i).getComputerExperienceMonths()));
        result.add(String.valueOf(testTask.getTestTask().getParticipants().get(i).getAgeRange().getAge()));
        result.add(testTask.getTestTask().getParticipants().get(i).getGender());
        result.add(testTask.getTestTask().getParticipants().get(i).getAssistiveTechnologyNeeds());
        i++;
        return result;
    }

    protected List<String> generateCriteriaValues(CertifiedProductSearchDetails data) {
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

package gov.healthit.chpl.app.presenter;

import gov.healthit.chpl.app.resource.SEDRow;
import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.TestParticipant;
import gov.healthit.chpl.domain.TestTask;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SEDCsvPresenter {
	private static final Logger LOGGER = LogManager.getLogger(SEDCsvPresenter.class);

    public int presentAsFile(File file, ArrayList<SEDRow> result) {
        int numRows = 0;
        FileWriter writer = null;
        CSVPrinter csvPrinter = null;
        try {
            writer = new FileWriter(file);
            csvPrinter = new CSVPrinter(writer, CSVFormat.EXCEL);
            
            csvPrinter.printRecord(generateHeaderValuesSED());

            for (SEDRow data : result) {
                List<String> rowValue = generateRowValue(data.getDetails(), data.getTestTask(), data.getCriteria(), data.getTestParticipant());
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
        result.add("Task Success - Mean (%)");
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
	
	protected List<String> generateRowValue(CertifiedProductDetailsDTO details, TestTask testTask, String criteria, TestParticipant testParticipant) {
        List<String> result = new ArrayList<String>();
        CertifiedProduct cp = new CertifiedProduct(details);
        result.add(cp.getChplProductNumber());
        result.add(details.getDeveloper().getName());
        result.add(details.getProduct().getName());
        result.add(details.getVersion().getVersion());
        result.add(criteria);
        result.add(testTask.getDescription());
        result.add(testTask.getTaskRatingScale());
        result.add(String.valueOf(testTask.getTaskRating()));
        result.add(String.valueOf(testTask.getTaskRatingStddev()));
        result.add(String.valueOf(testTask.getTaskTimeAvg()));
        result.add(String.valueOf(testTask.getTaskTimeStddev()));
        result.add(String.valueOf(testTask.getTaskTimeDeviationObservedAvg()));
        result.add(String.valueOf(testTask.getTaskTimeDeviationOptimalAvg()));
        result.add(String.valueOf(testTask.getTaskSuccessAverage()));
        result.add(String.valueOf(testTask.getTaskSuccessStddev()));
        result.add(String.valueOf(testTask.getTaskErrors()));
        result.add(String.valueOf(testTask.getTaskErrorsStddev()));
        result.add(String.valueOf(testTask.getTaskPathDeviationObserved()));
        result.add(String.valueOf(testTask.getTaskPathDeviationOptimal()));
        result.add(testParticipant.getOccupation());
        result.add(testParticipant.getEducationTypeName());
        result.add(String.valueOf(testParticipant.getProductExperienceMonths()));
        result.add(String.valueOf(testParticipant.getProfessionalExperienceMonths()));
        result.add(String.valueOf(testParticipant.getComputerExperienceMonths()));
        result.add(String.valueOf(testParticipant.getAgeRange()));
        result.add(testParticipant.getGender());
        result.add(testParticipant.getAssistiveTechnologyNeeds());
        return result;
    }
}

package gov.healthit.chpl.scheduler.presenter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.TestParticipant;
import gov.healthit.chpl.domain.TestTask;
import gov.healthit.chpl.service.CertificationCriterionService;

public class Sed2015CsvPresenter {
    private static final Logger LOGGER = LogManager.getLogger(Sed2015CsvPresenter.class);

    /**
     * Returns number of rows printed (minus the header)
     */
    public int presentAsFile(File file, List<CertifiedProductSearchDetails> cpList,
            CertificationCriterionService criterionService) {
        int numRows = 0;
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
                CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.EXCEL)) {
            writer.write('\ufeff');
            csvPrinter.printRecord(generateHeaderValues());
            for (CertifiedProductSearchDetails currListing : cpList) {
                List<List<String>> rows = generateRows(currListing, criterionService);
                if (rows != null) { // can return null to skip a row
                    for (List<String> row : rows) {
                        csvPrinter.printRecord(row);
                        numRows++;
                    }
                }
            }
        } catch (final IOException ex) {
            LOGGER.error("Could not write file " + file.getName(), ex);
        }
        return numRows;
    }

    protected List<String> generateHeaderValues() {
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

    protected List<List<String>> generateRows(final CertifiedProductSearchDetails listing,
            CertificationCriterionService criterionService) {
        if (!hasTestTasks(listing)) {
            return null;
        }

        // each row corresponds to one participant of one test task
        // and can result in many rows for a single listing
        List<List<String>> result = new ArrayList<List<String>>();
        for (TestTask testTask : listing.getSed().getTestTasks()) {
            if (testTask.getTestParticipants() == null || testTask.getTestParticipants().size() == 0) {
                LOGGER.warn("No participants were found for listing " + listing.getChplProductNumber()
                        + " test task ID " + testTask.getId());
            } else {
                for (TestParticipant participant : testTask.getTestParticipants()) {
                    List<String> row = new ArrayList<String>();
                    row.add(listing.getChplProductNumber());
                    row.add(listing.getDeveloper().getName());
                    row.add(listing.getProduct().getName());
                    row.add(listing.getVersion().getVersion());
                    StringBuffer assocCriteriaStr = new StringBuffer();
                    for (CertificationCriterion criteria : testTask.getCriteria()) {
                        if (assocCriteriaStr.length() > 0) {
                            assocCriteriaStr.append(";");
                        }
                        assocCriteriaStr.append(criterionService.formatCriteriaNumber(criteria));
                    }
                    row.add(assocCriteriaStr.toString());
                    row.add(testTask.getDescription());
                    row.add(testTask.getTaskRatingScale());
                    row.add(String.valueOf(testTask.getTaskRating()));
                    row.add(String.valueOf(testTask.getTaskRatingStddev()));
                    row.add(String.valueOf(testTask.getTaskTimeAvg()));
                    row.add(String.valueOf(testTask.getTaskTimeStddev()));
                    row.add(String.valueOf(testTask.getTaskTimeDeviationObservedAvg()));
                    row.add(String.valueOf(testTask.getTaskTimeDeviationOptimalAvg()));
                    row.add(String.valueOf(testTask.getTaskSuccessAverage()));
                    row.add(String.valueOf(testTask.getTaskSuccessStddev()));
                    row.add(String.valueOf(testTask.getTaskErrors()));
                    row.add(String.valueOf(testTask.getTaskErrorsStddev()));
                    row.add(String.valueOf(testTask.getTaskPathDeviationObserved()));
                    row.add(String.valueOf(testTask.getTaskPathDeviationOptimal()));
                    row.add(participant.getOccupation());
                    row.add(participant.getEducationTypeName());
                    row.add(String.valueOf(participant.getProductExperienceMonths()));
                    row.add(String.valueOf(participant.getProfessionalExperienceMonths()));
                    row.add(String.valueOf(participant.getComputerExperienceMonths()));
                    row.add(participant.getAgeRange());
                    row.add(participant.getGender());
                    row.add(participant.getAssistiveTechnologyNeeds());
                    result.add(row);
                }
            }
        }
        return result;
    }

    private boolean hasTestTasks(final CertifiedProductSearchDetails listing) {
        boolean result = false;
        if (listing.getSed() != null && listing.getSed().getTestTasks() != null
                && listing.getSed().getTestTasks().size() > 0) {
            return true;
        }
        return result;
    }
}

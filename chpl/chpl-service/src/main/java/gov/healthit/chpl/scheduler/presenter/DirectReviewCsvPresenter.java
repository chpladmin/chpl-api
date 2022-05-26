package gov.healthit.chpl.scheduler.presenter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.core.env.Environment;

import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.compliance.DirectReview;
import gov.healthit.chpl.domain.compliance.DirectReviewNonConformity;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.DeveloperManager;
import gov.healthit.chpl.service.DirectReviewSearchService;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "directReviewDownloadableResourceCreatorJobLogger")
public class DirectReviewCsvPresenter {
    private Environment env;
    private DateTimeFormatter dateFormatter;
    private DateTimeFormatter dateTimeFormatter;
    private DeveloperManager developerManager;
    private DirectReviewSearchService drService;

    public DirectReviewCsvPresenter(Environment env, DeveloperManager developerManager,
            DirectReviewSearchService drService) {
        dateFormatter = DateTimeFormatter.ofPattern("uuuu/MM/dd");
        dateTimeFormatter = DateTimeFormatter.ofPattern("uuuu/MM/dd HH:mm");
        this.env = env;
        this.developerManager = developerManager;
        this.drService = drService;
    }

    public void presentAsFile(File file) {
        List<DirectReview> allDirectReviews = drService.getAll();

        try (FileWriter writer = new FileWriter(file);
                CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.EXCEL)) {
            csvPrinter.printRecord(generateHeaderValues());
            for (DirectReview directReview : allDirectReviews) {
                if (directReview.getDeveloperId() != null) {
                    try {
                        Developer developer = developerManager.getById(directReview.getDeveloperId());
                        List<List<String>> rowValues = generateMultiRowValue(developer, directReview);
                        for (List<String> rowValue : rowValues) {
                            csvPrinter.printRecord(rowValue);
                        }
                    } catch (EntityRetrievalException ex) {
                        LOGGER.error("Could not find developer with ID " + directReview.getDeveloperId() + ". Not writing its direct review " + directReview.getJiraKey() + ".");
                    }
                } else {
                    LOGGER.error("Developer ID for direct review " + directReview.getJiraKey() + " was null.");
                }
            }
        } catch (IOException ex) {
            LOGGER.error("Could not write file " + file.getName(), ex);
        }
    }

    protected List<String> generateHeaderValues() {
        List<String> result = new ArrayList<String>();
        result.add("Developer ID");
        result.add("Developer Name");
        result.add("Developer Link");
        result.add("Developer Status");
        result.add("Non-Conformity Type");
        result.add("Non-Conformity Status");
        result.add("CAP Approval Date");
        result.add("Must Complete Date");
        result.add("Was Complete Date");
        result.add("Developer-Associated Listings");
        result.add("Non-conformity Last Updated Date");
        result.add("Retrieved from Jira Time");
        return result;
    }

    protected List<List<String>> generateMultiRowValue(Developer developer, DirectReview directReview) {
        List<List<String>> csvRows = new ArrayList<List<String>>();
        if (directReview.getNonConformities() != null && directReview.getNonConformities().size() > 0) {
            for (DirectReviewNonConformity nc : directReview.getNonConformities()) {
                List<String> csvRow = new ArrayList<String>();
                addDeveloperFields(csvRow, developer);
                addNonconformityDirectReviewFields(csvRow, nc);
                csvRow.add(dateTimeFormatter.format(directReview.getFetched()));
                csvRows.add(csvRow);
            }
        } else {
            List<String> csvRow = new ArrayList<String>();
            addDeveloperFields(csvRow, developer);
            addNonconformityDirectReviewFields(csvRow, null);
            csvRow.add(dateTimeFormatter.format(directReview.getFetched()));
            csvRows.add(csvRow);
        }
        return csvRows;
    }

    private void addDeveloperFields(List<String> csvRow, Developer developer) {
        csvRow.add(developer.getDeveloperId().toString());
        csvRow.add(developer.getName());
        csvRow.add(env.getProperty("chplUrlBegin") + "/#/organizations/developers/" + developer.getDeveloperId());
        csvRow.add(developer.getStatus() == null || developer.getStatus().getStatus() == null
                ? "Unknown" : developer.getStatus().getStatus());
    }

    private void addNonconformityDirectReviewFields(List<String> csvRow, DirectReviewNonConformity nc) {
        csvRow.add(nc != null && nc.getNonConformityType() != null ? nc.getNonConformityType() : "");
        csvRow.add(nc != null && nc.getNonConformityStatus() != null ? nc.getNonConformityStatus() : "");

        if (nc != null && nc.getCapApprovalDate() != null) {
            csvRow.add(dateFormatter.format(nc.getCapApprovalDate()));
        } else {
            csvRow.add("");
        }
        if (nc != null && nc.getCapMustCompleteDate() != null) {
            csvRow.add(dateFormatter.format(nc.getCapMustCompleteDate()));
        } else {
            csvRow.add("");
        }
        if (nc != null && nc.getCapEndDate() != null) {
            csvRow.add(dateFormatter.format(nc.getCapEndDate()));
        } else {
            csvRow.add("");
        }
        if (nc != null && nc.getDeveloperAssociatedListings() != null && nc.getDeveloperAssociatedListings().size() > 0) {
            List<String> dalChplIds = nc.getDeveloperAssociatedListings().stream()
                    .map(dal -> dal.getChplProductNumber())
                    .collect(Collectors.toList());
            csvRow.add(String.join(";", dalChplIds));
        } else {
            csvRow.add("");
        }
        if (nc != null && nc.getLastUpdated() != null) {
            LocalDateTime ncLastUpdatedDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(nc.getLastUpdated().getTime()),
                    ZoneId.systemDefault());
            csvRow.add(dateTimeFormatter.format(ncLastUpdatedDate));
        } else {
            csvRow.add("");
        }
    }

    public final Environment getEnv() {
        return env;
    }

    public final DateTimeFormatter getDateFormatter() {
        return dateFormatter;
    }

    public final DateTimeFormatter getDateTimeFormatter() {
        return dateTimeFormatter;
    }
}

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

import gov.healthit.chpl.caching.CacheNames;
import gov.healthit.chpl.domain.compliance.DirectReview;
import gov.healthit.chpl.domain.compliance.DirectReviewNonConformity;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.DeveloperManager;
import lombok.extern.log4j.Log4j2;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

@Log4j2
public class DirectReviewCsvPresenter {
    private static final int NON_CONFORMITY_FIELD_COUNT = 14;
    private Environment env;
    private DateTimeFormatter dateFormatter;
    private DateTimeFormatter dateTimeFormatter;
    private DeveloperManager developerManager;

    public DirectReviewCsvPresenter(Environment env, DeveloperManager developerManager) {
        dateFormatter = DateTimeFormatter.ofPattern("uuuu/MM/dd");
        dateTimeFormatter = DateTimeFormatter.ofPattern("uuuu/MM/dd HH:mm");
        this.env = env;
        this.developerManager = developerManager;
    }

    public void presentAsFile(File file) {
        CacheManager manager = CacheManager.getInstance();
        Cache drCache = manager.getCache(CacheNames.DIRECT_REVIEWS);

        try (FileWriter writer = new FileWriter(file);
                CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.EXCEL)) {
            csvPrinter.printRecord(generateHeaderValues());
            for (Object developerIdKey : drCache.getKeys()) {
                if (developerIdKey instanceof Long) {
                    try {
                        Long developerId = (Long) developerIdKey;
                        DeveloperDTO developer = developerManager.getById(developerId);
                        List<List<String>> rowValues = generateMultiRowValue(developer, drCache.get(developerId));
                        for (List<String> rowValue : rowValues) {
                            csvPrinter.printRecord(rowValue);
                        }
                    } catch (EntityRetrievalException ex) {
                        LOGGER.error("Could not find developer with ID " + developerIdKey + ". Not writing direct reviews.");
                    }
                } else {
                    LOGGER.error("Unexpected cache key: " + developerIdKey);
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
        result.add("Direct Review Began");
        result.add("Direct Review Ended");
        result.add("Circumstances");
        result.add("Direct Review Requirements");
        result.add("Direct Review Result");
        result.add("Non-Conformity Type");
        result.add("Non-Conformity Status");
        result.add("Date of Determination");
        result.add("CAP Approval Date");
        result.add("Action Began Date");
        result.add("Must Complete Date");
        result.add("Was Complete Date");
        result.add("Non-Conformity Summary");
        result.add("Non-Conformity Findings");
        result.add("Developer Explanation");
        result.add("Resolution Description");
        result.add("Developer-Associated Listings");
        result.add("Non-conformity Last Updated Date");
        result.add("Retrieved from Jira Time");
        return result;
    }

    protected List<List<String>> generateMultiRowValue(DeveloperDTO developer, Element drCacheElement) {
        List<DirectReview> devDirectReviews = null;
        Object drCacheValue = drCacheElement.getObjectValue();
        if (drCacheValue instanceof List<?>) {
            devDirectReviews = (List<DirectReview>) drCacheValue;
        } else {
            LOGGER.error("Direct Review cache had element for developer " + developer.getId()
                + " with unexpected value type: " + drCacheValue.getClass());
        }

        List<List<String>> csvRows = new ArrayList<List<String>>();
        if (devDirectReviews != null && devDirectReviews.size() > 0) {
            for (DirectReview dr : devDirectReviews) {
                if (dr.getNonConformities() != null && dr.getNonConformities().size() > 0) {
                    for (DirectReviewNonConformity nc : dr.getNonConformities()) {
                        List<String> csvRow = new ArrayList<String>();
                        addDeveloperFields(csvRow, developer);
                        addBasicDirectReviewFields(csvRow, dr);
                        addNonconformityDirectReviewFields(csvRow, nc);
                        LocalDateTime jiraPullTime = LocalDateTime.ofInstant(
                                Instant.ofEpochMilli(drCacheElement.getCreationTime()), ZoneId.systemDefault());
                        csvRow.add(dateTimeFormatter.format(jiraPullTime));
                        csvRows.add(csvRow);
                    }
                } else {
                    List<String> csvRow = new ArrayList<String>();
                    addDeveloperFields(csvRow, developer);
                    addBasicDirectReviewFields(csvRow, dr);
                    addNonconformityDirectReviewFields(csvRow, null);
                    LocalDateTime jiraPullTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(drCacheElement.getCreationTime()),
                            ZoneId.systemDefault());
                    csvRow.add(dateTimeFormatter.format(jiraPullTime));
                    csvRows.add(csvRow);
                }
            }
        }
        return csvRows;
    }

    private void addDeveloperFields(List<String> csvRow, DeveloperDTO developer) {
        csvRow.add(developer.getId().toString());
        csvRow.add(developer.getName());
        csvRow.add(env.getProperty("chplUrlBegin") + "/#/organizations/" + developer.getId());
        csvRow.add(developer.getStatus() == null || developer.getStatus().getStatus() == null
                ? "Unknown" : developer.getStatus().getStatus().getStatusName());
    }

    private void addBasicDirectReviewFields(List<String> csvRow, DirectReview dr) {
        if (dr.getStartDate() != null) {
            LocalDateTime drStartDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(dr.getStartDate().getTime()),
                    ZoneId.systemDefault());
            csvRow.add(dateFormatter.format(drStartDate));
        } else {
            csvRow.add("");
        }
        if (dr.getEndDate() != null) {
            LocalDateTime drEndDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(dr.getEndDate().getTime()),
                    ZoneId.systemDefault());
            csvRow.add(dateFormatter.format(drEndDate));
        } else {
            csvRow.add("");
        }
        if (dr.getCircumstances() != null && dr.getCircumstances().size() > 0) {
            csvRow.add(String.join(";", dr.getCircumstances()));
        } else {
            csvRow.add("");
        }
    }

    private void addNonconformityDirectReviewFields(List<String> csvRow, DirectReviewNonConformity nc) {
        csvRow.add(nc != null && nc.getRequirement() != null ? nc.getRequirement() : "");
        csvRow.add(nc == null ? "No Non-Conformity" : "Non-Conformity");
        csvRow.add(nc != null && nc.getNonConformityType() != null ? nc.getNonConformityType() : "");
        csvRow.add(nc != null && nc.getNonConformityStatus() != null ? nc.getNonConformityStatus() : "");

        if (nc != null && nc.getDateOfDetermination() != null) {
            csvRow.add(dateFormatter.format(nc.getDateOfDetermination()));
        } else {
            csvRow.add("");
        }
        if (nc != null && nc.getCapApprovalDate() != null) {
            csvRow.add(dateFormatter.format(nc.getCapApprovalDate()));
        } else {
            csvRow.add("");
        }
        if (nc != null && nc.getCapStartDate() != null) {
            csvRow.add(dateFormatter.format(nc.getCapStartDate()));
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
        csvRow.add(nc != null && nc.getNonConformitySummary() != null ? nc.getNonConformitySummary() : "");
        csvRow.add(nc != null && nc.getNonConformityFindings() != null ? nc.getNonConformityFindings() : "");
        csvRow.add(nc != null && nc.getDeveloperExplanation() != null ? nc.getDeveloperExplanation() : "");
        csvRow.add(nc != null && nc.getResolution() != null ? nc.getResolution() : "");
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
            csvRow.add(dateFormatter.format(ncLastUpdatedDate));
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

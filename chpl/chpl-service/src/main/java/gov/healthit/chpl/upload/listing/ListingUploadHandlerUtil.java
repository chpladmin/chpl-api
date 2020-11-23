package gov.healthit.chpl.upload.listing;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.validation.ValidationException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang.math.IntRange;
import org.apache.commons.lang.math.Range;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.util.ErrorMessageUtil;
import lombok.extern.log4j.Log4j2;

@Component("listingUploadHandlerUtil")
@Log4j2
public class ListingUploadHandlerUtil {
    private static final String CRITERION_COL_HEADING_REGEX = "CRITERIA_(\\d+)_(\\d+)_([A-Z])_([0-9]+)([A-Z])?(_CURES)?__C";
    protected static final String CRITERIA_CURES_COL_HEADING = "CURES";
    private static final String UPLOAD_DATE_FORMAT = "yyyyMMdd";
    private DateFormat dateFormat;
    private Pattern criterionColHeadingPattern;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public ListingUploadHandlerUtil(ErrorMessageUtil msgUtil) {
        this.msgUtil = msgUtil;
        this.dateFormat = new SimpleDateFormat(UPLOAD_DATE_FORMAT);
        this.criterionColHeadingPattern = Pattern.compile(CRITERION_COL_HEADING_REGEX);
    }

    public int getHeadingRecordIndex(List<CSVRecord> allCsvRecords) {
        int headingIndex = -1;
        if (allCsvRecords != null) {
            Optional<CSVRecord> headingRecord =
                    allCsvRecords.stream().filter(currRecord -> hasHeading(currRecord))
                    .findFirst();
            if (headingRecord.isPresent() && headingRecord.get() != null) {
                headingIndex = (int) headingRecord.get().getRecordNumber() - 1;
            } else {
                LOGGER.warn("No heading row was found.");
            }
        }
        return headingIndex;
    }

    public CSVRecord getHeadingRecord(List<CSVRecord> allCsvRecords) {
        return allCsvRecords.get(getHeadingRecordIndex(allCsvRecords));
    }

    public int getNextIndexOfCertificationResult(int startIndex, CSVRecord headingRecord)
        throws ValidationException {
        int nextCertResultStartIndex = -1;
        for (int i = startIndex; i < headingRecord.size() && nextCertResultStartIndex < 0; i++) {
            String currHeading = headingRecord.get(i);
            if (looksLikeCriteriaStart(currHeading)) {
                nextCertResultStartIndex = i;
            }
        }
        return nextCertResultStartIndex;
    }

    public List<CSVRecord> getCertificationResultRecordsFromIndex(int startIndex, CSVRecord headingRecord,
            List<CSVRecord> dataRecords) {
        CSVRecord certResultHeading = null;
        List<CSVRecord> certResultRows = new ArrayList<CSVRecord>();
        Range certResultColumnRange = calculateCertificationResultColumnRangeFromIndex(startIndex, headingRecord);
        if (certResultColumnRange == null) {
            return null;
        } else if (certResultColumnRange.getMinimumInteger() >= 0
                && certResultColumnRange.getMaximumInteger() >= certResultColumnRange.getMinimumInteger()
                && certResultColumnRange.getMaximumInteger() < headingRecord.size()) {
            //splice the heading record columns between the integer ranges
            CSVParser csvParser = null;
            try {
                List<String> certResultHeadingValues = new ArrayList<String>();
                for (int i = certResultColumnRange.getMinimumInteger(); i <= certResultColumnRange.getMaximumInteger(); i++) {
                    if (isHeadingForCriteriaField(headingRecord.get(i))) {
                        certResultHeadingValues.add(headingRecord.get(i));
                    }
                }
                csvParser = CSVParser.parse(certResultHeadingValues.stream().collect(Collectors.joining(",")),
                        CSVFormat.EXCEL);
                certResultHeading = csvParser.getRecords().get(0);

                for (CSVRecord listingRecord : dataRecords) {
                    List<String> certResultColumnValues = new ArrayList<String>();
                    for (int i = certResultColumnRange.getMinimumInteger(); i <= certResultColumnRange.getMaximumInteger(); i++) {
                        if (isHeadingForCriteriaField(headingRecord.get(i))) {
                            certResultColumnValues.add(listingRecord.get(i));
                        }
                    }
                    csvParser = CSVParser.parse(certResultColumnValues.stream().collect(Collectors.joining(",")),
                            CSVFormat.EXCEL);
                    certResultRows.addAll(csvParser.getRecords());
                }
            } catch (IOException ex) {
                LOGGER.error("Could not splice heading record between " + certResultColumnRange.getMinimumInteger()
                    + " and " + certResultColumnRange.getMaximumInteger());
            } finally {
                try {
                    csvParser.close();
                } catch (Exception e) {
                    LOGGER.error("Error closing csv parser.", e);
                }
            }
        } else {
            throw new ValidationException(msgUtil.getMessage("certResult.upload.invalidRange",
                    certResultColumnRange.getMinimumInteger(), certResultColumnRange.getMaximumInteger()));
        }

        certResultRows.add(0, certResultHeading);
        return certResultRows;
    }

    @SuppressWarnings("checkstyle:magicnumber")
    public String parseCriteriaNumberFromHeading(String headingVal) {
        if (StringUtils.isEmpty(headingVal)) {
            return null;
        }
        headingVal = headingVal.trim().toUpperCase();
        String criterionNumber = null;
        Matcher m = criterionColHeadingPattern.matcher(headingVal);
        if (m.find()) {
            criterionNumber = m.group(1) + "." + m.group(2)
                + " (" + m.group(3).toLowerCase() + ")(" + m.group(4) + ")";
            if (m.group(5) != null) {
                criterionNumber += "(" + m.group(5).toUpperCase() + ")";
            }
         }
        return criterionNumber;
    }

    public boolean isCriteriaNumberHeadingCures(String headingVal) {
        return headingVal.toUpperCase().contains(CRITERIA_CURES_COL_HEADING);
    }

    @SafeVarargs
    public final boolean areCollectionsEmpty(Collection<String>... collections) {
        List<Collection<String>> nonEmptyNonNullCollections =
                Stream.of(collections)
                    .filter(Objects::nonNull)
                    .filter(CollectionUtils::isNotEmpty)
                    .collect(Collectors.toList());
        if (nonEmptyNonNullCollections.size() == 0) {
            return true;
        }
        long numCollectionsWithNonEmptyStrings = 0;
        for (Collection<String> collection : nonEmptyNonNullCollections) {
            if (collection.stream().filter(StringUtils::isNotEmpty).count() > 0) {
                numCollectionsWithNonEmptyStrings++;
            }
        }
        if (numCollectionsWithNonEmptyStrings > 0) {
            return false;
        }
        return true;
    }

    public String parseRequiredSingleRowField(Headings field, CSVRecord headingRecord, List<CSVRecord> listingRecords)
            throws ValidationException {
        String fieldValue = null;
        int fieldHeadingIndex = getColumnIndexOfHeading(field, headingRecord);
        if (fieldHeadingIndex < 0) {
            throw new ValidationException(msgUtil.getMessage("listing.upload.requiredHeadingNotFound",
                    field.getNamesAsString()));
        }
        for (CSVRecord listingRecord : listingRecords) {
            if (StringUtils.isEmpty(fieldValue) && fieldHeadingIndex < listingRecord.size()) {
                String parsedFieldValue = listingRecord.get(fieldHeadingIndex);
                if (parsedFieldValue != null) {
                    fieldValue = parsedFieldValue.trim();
                }
            }
        }
        return fieldValue;
    }

    public String parseRequiredSingleRowField(Headings field, CSVRecord headingRecord, CSVRecord listingRecord)
            throws ValidationException {
        List<CSVRecord> data = new ArrayList<CSVRecord>();
        data.add(listingRecord);
        return parseRequiredSingleRowField(field, headingRecord, data);
    }

    public String parseSingleRowField(Headings field, CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        String fieldValue = null;
        int fieldHeadingIndex = getColumnIndexOfHeading(field, headingRecord);
        if (fieldHeadingIndex < 0) {
            return null;
        }
        for (CSVRecord listingRecord : listingRecords) {
            if (StringUtils.isEmpty(fieldValue) && fieldHeadingIndex < listingRecord.size()) {
                String parsedFieldValue = listingRecord.get(fieldHeadingIndex);
                if (parsedFieldValue != null) {
                    fieldValue = parsedFieldValue.trim();
                }
            }
        }
        return fieldValue;
    }

    public String parseSingleRowFieldAtIndex(int fieldHeadingIndex, CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        String fieldValue = null;
        for (CSVRecord listingRecord : listingRecords) {
            if (StringUtils.isEmpty(fieldValue) && fieldHeadingIndex < listingRecord.size()) {
                String parsedFieldValue = listingRecord.get(fieldHeadingIndex);
                if (parsedFieldValue != null) {
                    fieldValue = parsedFieldValue.trim();
                }
            }
        }
        return fieldValue;
    }

    public String parseSingleRowField(Headings field, CSVRecord headingRecord, CSVRecord listingRecord) {
        List<CSVRecord> data = new ArrayList<CSVRecord>();
        data.add(listingRecord);
        return parseSingleRowField(field, headingRecord, data);
    }

    public Boolean parseSingleRowFieldAsBoolean(Headings field, CSVRecord headingRecord, List<CSVRecord> listingRecords)
            throws ValidationException {
        String fieldValue = parseSingleRowField(field, headingRecord, listingRecords);
        return parseBoolean(fieldValue);
    }

    public Date parseSingleRowFieldAsDate(Headings field, CSVRecord headingRecord, List<CSVRecord> listingRecords)
            throws ValidationException {
        String fieldValue = parseSingleRowField(field, headingRecord, listingRecords);
        return parseDate(fieldValue);
    }

    public List<String> parseMultiRowField(Headings field, CSVRecord headingRecord, List<CSVRecord> listingRecords)
            throws ValidationException {
            List<String> fieldValues = new ArrayList<String>();
            int fieldHeadingIndex = getColumnIndexOfHeading(field, headingRecord);
            if (fieldHeadingIndex < 0) {
                return null;
            }

            if (fieldHeadingIndex >= 0) {
                for (CSVRecord listingRecord : listingRecords) {
                    if (fieldHeadingIndex < listingRecord.size()) {
                        String parsedFieldValue = listingRecord.get(fieldHeadingIndex);
                        if (parsedFieldValue != null) {
                            fieldValues.add(parsedFieldValue.trim());
                        }
                    }
                }
            }
            return fieldValues;
    }

    public List<String> parseMultiRowFieldWithoutEmptyValues(
            Headings field, CSVRecord headingRecord, List<CSVRecord> listingRecords)
            throws ValidationException {
            List<String> fieldValues = parseMultiRowField(field, headingRecord, listingRecords);
            return fieldValues == null ? null : removeEmptyValues(fieldValues);
    }

    private List<String> removeEmptyValues(List<String> origList) {
        return origList.stream()
            .filter(str -> !StringUtils.isEmpty(str))
            .collect(Collectors.toList());
    }

    public boolean hasHeading(Headings heading, CSVRecord headingRecord) {
        return getColumnIndexOfHeading(heading, headingRecord) >= 0;
    }

    private Range calculateCertificationResultColumnRangeFromIndex(int startIndex, CSVRecord headingRecord) {
        int certResultStartIndex = -1, certResultEndIndex = -1;
        for (int i = startIndex; i < headingRecord.size() && (certResultStartIndex < 0 || certResultEndIndex < 0); i++) {
            String currHeading = headingRecord.get(i);
            if (looksLikeCriteriaStart(currHeading)) {
                if (certResultStartIndex < 0) {
                    certResultStartIndex = i;
                } else if (certResultEndIndex < 0) {
                    certResultEndIndex = i - 1;
                }
            }
            if (certResultStartIndex >= 0 && certResultEndIndex < 0 && i == headingRecord.size() - 1) {
                certResultEndIndex = i;
            }
        }

        if (certResultStartIndex == -1 && certResultEndIndex == -1) {
            return null;
        }

        return new IntRange(certResultStartIndex, certResultEndIndex);
    }

    private boolean looksLikeCriteriaStart(String headingVal) {
        return StringUtils.startsWithIgnoreCase(headingVal, "CRITERIA");
    }

    private boolean isHeadingForCriteriaField(String headingVal) {
        //The task id and participant id fields cannot be specified as CertResult level
        //because identical headings are used at the listing level.
        //Probably only want to call this method if you know the passed-in heading occurs AFTER
        //a CRITERIA_170_* field.
        Headings heading = Headings.getHeading(headingVal);
        return heading != null
                && (isHeadingLevel(headingVal, HeadingLevel.CERT_RESULT)
                || Headings.getHeading(headingVal).equals(Headings.TASK_ID)
                || Headings.getHeading(headingVal).equals(Headings.PARTICIPANT_ID));
    }
    private boolean isHeadingLevel(String headingVal, HeadingLevel level) {
        Headings heading = Headings.getHeading(headingVal);
        return heading != null && heading.getLevel().equals(level);
    }

    private boolean hasHeading(CSVRecord record) {
        Iterator<String> iter = record.iterator();
        while (iter.hasNext()) {
            String currRecordValue = iter.next();
            if (currRecordValue != null && !StringUtils.isEmpty(currRecordValue.trim())
                    && Headings.getHeading(currRecordValue.trim()) != null) {
                return true;
            }
        }
        return false;
    }

    private int getColumnIndexOfHeading(Headings heading, CSVRecord headingRecord) {
        int index = 0;
        Iterator<String> iter = headingRecord.iterator();
        while (iter.hasNext()) {
            String currHeadingValue = iter.next();
            if (currHeadingValue != null && !StringUtils.isEmpty(currHeadingValue.trim())
                    && Headings.getHeading(currHeadingValue.trim()) != null
                    && Headings.getHeading(currHeadingValue.trim()).equals(heading)) {
                return index;
            }
            index++;
        }
        return -1;
    }

    public Boolean parseBoolean(String value) throws ValidationException {
        if (value == null) {
            return null;
        } else if (StringUtils.isEmpty(value.trim())) {
            return false;
        }

        if (value.equalsIgnoreCase("t") || value.equalsIgnoreCase("true") || value.equalsIgnoreCase("yes")
                || value.equalsIgnoreCase("y")) {
            return true;
        } else if (value.equalsIgnoreCase("f") || value.equalsIgnoreCase("false") || value.equalsIgnoreCase("no")
                || value.equalsIgnoreCase("n")) {
            return false;
        }

        try {
            double numValue = Double.parseDouble(value);
            if (numValue > 0) {
                return true;
            }
        } catch (NumberFormatException ex) {
            LOGGER.error("Could not parse " + value + " as an integer. " + ex.getMessage());
            throw new ValidationException(msgUtil.getMessage("listing.upload.invalidBoolean", value));
        }
        return false;
    }

    private Date parseDate(String value) {
        if (value == null || StringUtils.isEmpty(value.trim())) {
            return null;
        }

        //TODO: look for more date formats
        Date certificationDate = null;
        try {
            certificationDate = dateFormat.parse(value);
        } catch (ParseException ex) {
            LOGGER.error("Could not parse " + value + " as a date. " + ex.getMessage());
            throw new ValidationException(msgUtil.getMessage("listing.upload.invalidDate", value));
        }
        return certificationDate;
    }
}

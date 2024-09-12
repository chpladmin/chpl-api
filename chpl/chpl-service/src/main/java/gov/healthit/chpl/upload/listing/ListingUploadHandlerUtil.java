package gov.healthit.chpl.upload.listing;

import java.io.IOException;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.csv.QuoteMode;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.upload.listing.ListingUploadHeadingUtil.Heading;
import gov.healthit.chpl.util.DateUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;
import jakarta.validation.ValidationException;
import lombok.extern.log4j.Log4j2;

@Component("listingUploadHandlerUtil")
@Log4j2
public class ListingUploadHandlerUtil {
    private static final String CRITERION_COL_HEADING_REGEX = "CRITERIA_(\\d+)_(\\d+)_([A-Z])_([0-9]+)([A-Z])?(_CURES)?_+C";
    protected static final String CRITERIA_CURES_COL_HEADING = "CURES";
    public static final String UPLOAD_DATE_FORMAT = "yyyyMMdd";
    private DateFormat dateFormat;
    private Pattern criterionColHeadingPattern;
    private ListingUploadHeadingUtil uploadHeadingUtil;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public ListingUploadHandlerUtil(ListingUploadHeadingUtil uploadHeadingUtil,
            ErrorMessageUtil msgUtil) {
        this.uploadHeadingUtil = uploadHeadingUtil;
        this.msgUtil = msgUtil;
        this.dateFormat = new SimpleDateFormat(UPLOAD_DATE_FORMAT);
        this.criterionColHeadingPattern = Pattern.compile(CRITERION_COL_HEADING_REGEX);
    }

    public int getHeadingRecordIndex(List<CSVRecord> allCsvRecords) {
        int headingIndex = -1;
        if (allCsvRecords != null) {
            Optional<CSVRecord> headingRecord = allCsvRecords.stream()
                .filter(currRecord -> hasHeading(currRecord))
                .findAny();
            if (headingRecord.isPresent() && headingRecord.get() != null) {
                headingIndex = (int) headingRecord.get().getRecordNumber() - 1;
            } else {
                LOGGER.warn("No heading row was found.");
            }
        }
        return headingIndex;
    }

    public CSVRecord getHeadingRecord(List<CSVRecord> allCsvRecords) {
        int headingRecordIndex = getHeadingRecordIndex(allCsvRecords);
        if (headingRecordIndex < 0) {
            return null;
        }
        return allCsvRecords.get(headingRecordIndex);
    }

    public int getNextIndexOfCertificationResult(int startIndex, CSVRecord headingRecord)
        throws ValidationException {
        int nextCertResultStartIndex = -1;
        for (int i = startIndex; i < headingRecord.size() && nextCertResultStartIndex < 0; i++) {
            String currHeading = headingRecord.get(i);
            if (uploadHeadingUtil.isCriterionHeading(currHeading)) {
                nextCertResultStartIndex = i;
            }
        }
        return nextCertResultStartIndex;
    }

    public List<CSVRecord> getCertificationResultRecordsFromIndex(int startIndex, CSVRecord headingRecord,
            List<CSVRecord> dataRecords) {
        CSVRecord certResultHeading = null;
        List<CSVRecord> certResultRows = new ArrayList<CSVRecord>();
        CellRangeAddress certResultRange = calculateCertificationResultColumnRangeFromIndex(startIndex, headingRecord, dataRecords);
        if (certResultRange == null) {
            return null;
        } else if (certResultRange.getFirstColumn() >= 0
                && certResultRange.getLastColumn() >= certResultRange.getFirstColumn()
                && certResultRange.getLastColumn() < headingRecord.size()) {
            //splice the heading record columns between the integer ranges
            List<String> certResultHeadingValues = new ArrayList<String>();
            for (int i = certResultRange.getFirstColumn(); i <= certResultRange.getLastColumn(); i++) {
                certResultHeadingValues.add(headingRecord.get(i));
            }
            certResultHeading = convertToCsvRecord(certResultHeadingValues);

            for (CSVRecord dataRecord : dataRecords) {
                List<String> certResultColumnValues = new ArrayList<String>();
                for (int i = certResultRange.getFirstColumn(); i <= certResultRange.getLastColumn(); i++) {
                    certResultColumnValues.add(dataRecord.get(i));
                }
                CSVRecord writtenDataRecord = convertToCsvRecord(certResultColumnValues);
                if (writtenDataRecord != null) {
                    certResultRows.add(writtenDataRecord);
                }
            }
        } else {
            throw new ValidationException(msgUtil.getMessage("certResult.upload.invalidRange",
                    certResultRange.getFirstColumn(), certResultRange.getLastColumn()));
        }

        certResultRows.add(0, certResultHeading);
        return certResultRows;
    }

    public CSVRecord convertToCsvRecord(List<String> values) {
        CSVFormat csvFormat = CSVFormat.EXCEL.builder()
                .setRecordSeparator(System.lineSeparator())
                .setQuoteMode(QuoteMode.ALL)
                .build();
        CSVRecord csvRecord = null;
        final StringWriter out = new StringWriter();
        try (CSVPrinter csvPrinter = new CSVPrinter(out, csvFormat)) {
            csvPrinter.printRecord(values);
            String value = out.toString().trim();
            csvRecord = CSVParser.parse(value, CSVFormat.EXCEL).getRecords().get(0);
        } catch (IOException ex) {
            LOGGER.error("Cannot write values as CSVRecord.", ex);
        }
        return csvRecord;
    }

    public List<String> convertToList(CSVRecord record) {
        List<String> items = new ArrayList<String>();
        Iterator<String> recordIter = record.iterator();
        while (recordIter.hasNext()) {
            items.add(recordIter.next());
        }
        return items;
    }

    @SuppressWarnings("checkstyle:magicnumber")
    public String parseCriteriaNumberFromHeading(String headingVal) {
        if (StringUtils.isEmpty(headingVal)) {
            return null;
        }
        headingVal = StringUtils.normalizeSpace(headingVal).toUpperCase();
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

    public String parseRequiredSingleRowField(Heading heading, CSVRecord headingRecord, List<CSVRecord> listingRecords)
            throws ValidationException {
        String fieldValue = null;
        int fieldHeadingIndex = getColumnIndexOfHeading(heading, headingRecord);
        if (fieldHeadingIndex < 0) {
            throw new ValidationException(msgUtil.getMessage("listing.upload.requiredHeadingNotFound",
                    heading.getNamesAsString()));
        }
        for (CSVRecord listingRecord : listingRecords) {
            if (StringUtils.isEmpty(fieldValue) && fieldHeadingIndex < listingRecord.size()) {
                String parsedFieldValue = listingRecord.get(fieldHeadingIndex);
                if (parsedFieldValue != null) {
                    fieldValue = StringUtils.normalizeSpace(parsedFieldValue);
                }
            }
        }
        return fieldValue;
    }

    public String parseRequiredSingleRowField(Heading heading, CSVRecord headingRecord, CSVRecord listingRecord)
            throws ValidationException {
        List<CSVRecord> data = new ArrayList<CSVRecord>();
        data.add(listingRecord);
        return parseRequiredSingleRowField(heading, headingRecord, data);
    }

    public String parseSingleRowField(Heading heading, CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        String fieldValue = null;
        int fieldHeadingIndex = getColumnIndexOfHeading(heading, headingRecord);
        if (fieldHeadingIndex < 0) {
            return null;
        }
        for (CSVRecord listingRecord : listingRecords) {
            if (StringUtils.isEmpty(fieldValue) && fieldHeadingIndex < listingRecord.size()) {
                String parsedFieldValue = listingRecord.get(fieldHeadingIndex);
                if (parsedFieldValue != null) {
                    fieldValue = StringUtils.normalizeSpace(parsedFieldValue);
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
                    fieldValue = StringUtils.normalizeSpace(parsedFieldValue);
                }
            }
        }
        return fieldValue;
    }

    public String parseSingleRowField(Heading heading, CSVRecord headingRecord, CSVRecord listingRecord) {
        List<CSVRecord> data = new ArrayList<CSVRecord>();
        data.add(listingRecord);
        return parseSingleRowField(heading, headingRecord, data);
    }

    public Boolean parseSingleRowFieldAsBoolean(Heading heading, CSVRecord headingRecord, List<CSVRecord> listingRecords)
            throws ValidationException {
        String fieldValue = parseSingleRowField(heading, headingRecord, listingRecords);
        return parseBoolean(fieldValue);
    }

    public Date parseSingleRowFieldAsDate(Heading heading, CSVRecord headingRecord, List<CSVRecord> listingRecords)
            throws ValidationException {
        String fieldValue = parseSingleRowField(heading, headingRecord, listingRecords);
        return parseDate(fieldValue);
    }

    public LocalDate parseSingleRowFieldAsLocalDate(Heading heading, CSVRecord headingRecord, List<CSVRecord> listingRecords)
            throws ValidationException {
        String fieldValue = parseSingleRowField(heading, headingRecord, listingRecords);
        return parseLocalDate(fieldValue);
    }

    public List<String> parseMultiRowField(Heading heading, CSVRecord headingRecord, List<CSVRecord> listingRecords)
            throws ValidationException {
            List<String> fieldValues = new ArrayList<String>();
            int fieldHeadingIndex = getColumnIndexOfHeading(heading, headingRecord);
            if (fieldHeadingIndex < 0) {
                return null;
            }

            if (fieldHeadingIndex >= 0) {
                for (CSVRecord listingRecord : listingRecords) {
                    if (fieldHeadingIndex < listingRecord.size()) {
                        String parsedFieldValue = listingRecord.get(fieldHeadingIndex);
                        if (parsedFieldValue != null) {
                            fieldValues.add(StringUtils.normalizeSpace(parsedFieldValue));
                        }
                    }
                }
            }
            return fieldValues;
    }

    public List<String> parseMultiRowFieldWithoutEmptyValues(Heading heading, CSVRecord headingRecord, List<CSVRecord> listingRecords)
            throws ValidationException {
        List<String> fieldValues = parseMultiRowField(heading, headingRecord, listingRecords);
        return fieldValues == null ? null : removeEmptyValues(fieldValues);
    }

    private List<String> removeEmptyValues(List<String> origList) {
        return origList.stream()
            .filter(str -> !StringUtils.isEmpty(str))
            .collect(Collectors.toList());
    }

    public boolean hasHeading(Heading heading, CSVRecord headingRecord) {
        return getColumnIndexOfHeading(heading, headingRecord) >= 0;
    }

    private CellRangeAddress calculateCertificationResultColumnRangeFromIndex(int startIndex, CSVRecord headingRecord,
            List<CSVRecord> dataRecords) {
        int certResultStartIndex = -1, certResultEndIndex = -1;
        for (int i = startIndex; i < headingRecord.size() && (certResultStartIndex < 0 || certResultEndIndex < 0); i++) {
            String currHeading = headingRecord.get(i);
            if (uploadHeadingUtil.isCriterionHeading(currHeading)) {
                if (certResultStartIndex < 0) {
                    certResultStartIndex = i;
                } else if (certResultEndIndex < 0) {
                    certResultEndIndex = i - 1;
                }
            }
            if (certResultStartIndex >= 0 && certResultEndIndex < 0 && i == (headingRecord.size() - 1)) {
                certResultEndIndex = i;
            }
        }

        if (certResultStartIndex == -1 && certResultEndIndex == -1) {
            return null;
        }

        return new CellRangeAddress(0, dataRecords.size() > 0 ? dataRecords.size() - 1 : 0, certResultStartIndex, certResultEndIndex);
    }

    private boolean hasHeading(CSVRecord record) {
        Iterator<String> iter = record.iterator();
        while (iter.hasNext()) {
            String currRecordValue = StringUtils.normalizeSpace(iter.next());
            if (currRecordValue != null && !StringUtils.isEmpty(currRecordValue)
                    && uploadHeadingUtil.isValidHeading(currRecordValue)) {
                return true;
            }
        }
        return false;
    }

    private int getColumnIndexOfHeading(Heading heading, CSVRecord headingRecord) {
        int index = 0;
        Iterator<String> iter = headingRecord.iterator();
        while (iter.hasNext()) {
            String currHeadingValue = iter.next();
            String normalizedCurrHeading = StringUtils.normalizeSpace(currHeadingValue);
            if (normalizedCurrHeading != null
                    && ListingUploadHeadingUtil.getHeading(normalizedCurrHeading) != null
                    && ListingUploadHeadingUtil.getHeading(normalizedCurrHeading).equals(heading)) {
                return index;
            }
            index++;
        }
        return -1;
    }

    public Boolean parseBoolean(String value) throws ValidationException {
        Boolean result = null;
        if (value == null) {
            return result;
        } else if (StringUtils.isBlank(value)) {
            result = false;
        }

        if (result == null) {
            if (value.equalsIgnoreCase("t") || value.equalsIgnoreCase("true") || value.equalsIgnoreCase("yes")
                    || value.equalsIgnoreCase("y")) {
                result = true;
            } else if (value.equalsIgnoreCase("f") || value.equalsIgnoreCase("false") || value.equalsIgnoreCase("no")
                    || value.equalsIgnoreCase("n")) {
                result = false;
            }
        }

        if (result == null) {
            try {
                double numValue = Double.parseDouble(value);
                if (numValue > 0) {
                    result = true;
                } else {
                    result = false;
                }
            } catch (NumberFormatException ex) {
                if (!StringUtils.isBlank(value)) {
                    LOGGER.error("Could not parse " + value + " as an integer. " + ex.getMessage());
                }
            }
        }

        if (result == null) {
            throw new ValidationException(msgUtil.getMessage("listing.upload.invalidBoolean", value));
        }
        return result;
    }

    public Date parseDate(String value) throws ValidationException {
        if (value == null || StringUtils.isBlank(value)) {
            return null;
        }

        //TODO: look for more date formats
        Date parsedDate = null;
        if (value.matches("[0-9]{8}")) {
            try {
                parsedDate = dateFormat.parse(value);
            } catch (ParseException ex) {
                LOGGER.error("Could not parse " + value + " as a date. " + ex.getMessage());
                throw new ValidationException(msgUtil.getMessage("listing.upload.invalidDate", value));
            }
        }
        return parsedDate;
    }

    public LocalDate parseLocalDate(String value) throws ValidationException {
        if (value == null || StringUtils.isEmpty(value.trim())) {
            return null;
        }

        LocalDate result = null;
        try {
            result = LocalDate.parse(value);
        } catch (Exception ex) {
            LOGGER.debug("LocalDateTime.parse did not work for " + value, ex);
        }

        if (result == null) {
            Date parsedDate = parseDate(value);
            if (parsedDate != null) {
                result = DateUtil.toLocalDate(parsedDate.getTime());
            }
        }
        return result;
    }
}

package gov.healthit.chpl.upload.listing;

import java.io.IOException;
import java.io.StringWriter;
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
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.csv.QuoteMode;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.util.CellRangeAddress;
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

    private CSVRecord convertToCsvRecord(List<String> values) {
        CSVFormat csvFormat = CSVFormat.EXCEL.withRecordSeparator(System.lineSeparator())
                .withQuoteMode(QuoteMode.ALL);
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

    private CellRangeAddress calculateCertificationResultColumnRangeFromIndex(int startIndex, CSVRecord headingRecord,
            List<CSVRecord> dataRecords) {
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
            if (certResultStartIndex >= 0 && certResultEndIndex < 0 && i == (headingRecord.size() - 1)) {
                certResultEndIndex = i;
            }
        }

        if (certResultStartIndex == -1 && certResultEndIndex == -1) {
            return null;
        }

        return new CellRangeAddress(0, dataRecords.size() - 1, certResultStartIndex, certResultEndIndex);
    }

    private boolean looksLikeCriteriaStart(String headingVal) {
        return StringUtils.startsWithIgnoreCase(headingVal, "CRITERIA");
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
        Boolean result = null;
        if (value == null) {
            return result;
        } else if (StringUtils.isEmpty(value.trim())) {
            result = false;
        }

        if (value.equalsIgnoreCase("t") || value.equalsIgnoreCase("true") || value.equalsIgnoreCase("yes")
                || value.equalsIgnoreCase("y")) {
            result = true;
        } else if (value.equalsIgnoreCase("f") || value.equalsIgnoreCase("false") || value.equalsIgnoreCase("no")
                || value.equalsIgnoreCase("n")) {
            result = false;
        }

        try {
            double numValue = Double.parseDouble(value);
            if (numValue > 0) {
                result = true;
            } else {
                result = false;
            }
        } catch (NumberFormatException ex) {
            LOGGER.error("Could not parse " + value + " as an integer. " + ex.getMessage());
        }

        if (result == null) {
            throw new ValidationException(msgUtil.getMessage("listing.upload.invalidBoolean", value));
        }
        return result;
    }

    public Date parseDate(String value) throws ValidationException {
        if (value == null || StringUtils.isEmpty(value.trim())) {
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
}

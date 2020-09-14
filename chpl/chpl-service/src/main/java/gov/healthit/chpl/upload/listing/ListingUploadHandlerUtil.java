package gov.healthit.chpl.upload.listing;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.validation.ValidationException;

import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.util.ErrorMessageUtil;
import lombok.extern.log4j.Log4j2;

@Component("listingUploadHandlerUtil")
@Log4j2
public class ListingUploadHandlerUtil {
    private static final String CERTIFICATION_DATE_FORMAT = "yyyyMMdd";
    private DateFormat dateFormat;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public ListingUploadHandlerUtil(ErrorMessageUtil msgUtil) {
        this.msgUtil = msgUtil;
        this.dateFormat = new SimpleDateFormat(CERTIFICATION_DATE_FORMAT);
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

    public String parseRequiredSingleValueField(Headings field, CSVRecord headingRecord, List<CSVRecord> listingRecords)
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

    public String parseRequiredSingleValueField(Headings field, CSVRecord headingRecord, CSVRecord listingRecord)
            throws ValidationException {
        List<CSVRecord> data = new ArrayList<CSVRecord>();
        data.add(listingRecord);
        return parseRequiredSingleValueField(field, headingRecord, data);
    }

    public String parseSingleValueField(Headings field, CSVRecord headingRecord, List<CSVRecord> listingRecords) {
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

    public String parseSingleValueField(Headings field, CSVRecord headingRecord, CSVRecord listingRecord) {
        List<CSVRecord> data = new ArrayList<CSVRecord>();
        data.add(listingRecord);
        return parseSingleValueField(field, headingRecord, data);
    }

    public Boolean parseSingleValueFieldAsBoolean(Headings field, CSVRecord headingRecord, List<CSVRecord> listingRecords)
            throws ValidationException {
        String fieldValue = parseSingleValueField(field, headingRecord, listingRecords);
        return parseBoolean(fieldValue);
    }

    public Date parseSingleValueFieldAsDate(Headings field, CSVRecord headingRecord, List<CSVRecord> listingRecords)
            throws ValidationException {
        String fieldValue = parseSingleValueField(field, headingRecord, listingRecords);
        return parseDate(fieldValue);
    }

    public List<String> parseRequiredMultiValueField(Headings field, CSVRecord headingRecord, List<CSVRecord> listingRecords)
            throws ValidationException {
            List<String> fieldValues = null;
            int fieldHeadingIndex = getColumnIndexOfHeading(field, headingRecord);
            if (fieldHeadingIndex < 0) {
                throw new ValidationException(msgUtil.getMessage("listing.upload.headingNotFound",
                        field.getNamesAsString()));
            }
            fieldValues = new ArrayList<String>();
            for (CSVRecord listingRecord : listingRecords) {
                if (fieldHeadingIndex < listingRecord.size()) {
                    String parsedFieldValue = listingRecord.get(fieldHeadingIndex);
                    if (parsedFieldValue != null && !StringUtils.isEmpty(parsedFieldValue.trim())) {
                        fieldValues.add(parsedFieldValue.trim());
                    }
                }
            }
            return removeEmptyValues(fieldValues);
    }

    public List<String> parseMultiValueField(Headings field, CSVRecord headingRecord, List<CSVRecord> listingRecords)
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
            return removeEmptyValues(fieldValues);
    }

    private List<String> removeEmptyValues(List<String> origList) {
        return origList.stream()
            .filter(str -> !StringUtils.isEmpty(str))
            .collect(Collectors.toList());
    }

    public boolean hasHeading(Headings heading, CSVRecord headingRecord) {
        return getColumnIndexOfHeading(heading, headingRecord) >= 0;
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
                    && Headings.getHeading(currHeadingValue.trim()).equals(heading)) {
                return index;
            }
            index++;
        }
        return -1;
    }

    private Boolean parseBoolean(String value) throws ValidationException {
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

package gov.healthit.chpl.upload.listing;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import javax.validation.ValidationException;

import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.util.ErrorMessageUtil;
import lombok.extern.log4j.Log4j2;

@Component("listingUploadHandler")
@Log4j2
public class ListingUploadHandler {
    private ErrorMessageUtil msgUtil;

    @Autowired
    public ListingUploadHandler(ErrorMessageUtil msgUtil) {
        this.msgUtil = msgUtil;
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

    public String parseSingleValueField(Headings field, CSVRecord headingRecord, List<CSVRecord> listingRecords)
        throws ValidationException {
        String fieldValue = null;
        int fieldHeadingIndex = getColumnIndexOfHeading(field, headingRecord);
        if (fieldHeadingIndex < 0) {
            throw new ValidationException(msgUtil.getMessage("listing.upload.requiredheadingNotFound",
                    field.getNamesAsString()));
        }
        for (CSVRecord listingRecord : listingRecords) {
            if (StringUtils.isEmpty(fieldValue)) {
                String parsedFieldValue = listingRecord.get(fieldHeadingIndex);
                if (parsedFieldValue != null && !StringUtils.isEmpty(parsedFieldValue.trim())) {
                    fieldValue = parsedFieldValue.trim();
                }
            }
        }
        return fieldValue;
    }

    public String parseSingleValueField(Headings field, CSVRecord headingRecord, CSVRecord listingRecord)
        throws ValidationException {
        List<CSVRecord> data = new ArrayList<CSVRecord>();
        data.add(listingRecord);
        return parseSingleValueField(field, headingRecord, data);
    }

    public List<String> parseMultiValueField(Headings field, CSVRecord headingRecord, List<CSVRecord> listingRecords)
            throws ValidationException {
            List<String> fieldValues = null;
            int fieldHeadingIndex = getColumnIndexOfHeading(field, headingRecord);
            if (fieldHeadingIndex < 0) {
                throw new ValidationException(msgUtil.getMessage("listing.upload.headingNotFound",
                        field.getNamesAsString()));
            }
            fieldValues = new ArrayList<String>();
            for (CSVRecord listingRecord : listingRecords) {
                String parsedFieldValue = listingRecord.get(fieldHeadingIndex);
                if (parsedFieldValue != null && !StringUtils.isEmpty(parsedFieldValue.trim())) {
                    fieldValues.add(parsedFieldValue.trim());
                }
            }
            return fieldValues;
        }

    public PendingCertifiedProductDTO parse(List<CSVRecord> records) {
        PendingCertifiedProductDTO pendingListing = new PendingCertifiedProductDTO();

        return pendingListing;
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
}

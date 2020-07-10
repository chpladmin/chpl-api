package gov.healthit.chpl.upload.listing;

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

    public String parseChplProductNumber(CSVRecord headingRecord, List<CSVRecord> listingRecords)
        throws ValidationException {
        String chplProductNumber = null;
        int chplProductNumberHeadingIndex = getColumnIndexOfHeading(Headings.UNIQUE_ID, headingRecord);
        if (chplProductNumberHeadingIndex < 0) {
            throw new ValidationException(msgUtil.getMessage("listing.upload.headingNotFound",
                    Headings.UNIQUE_ID.getNamesAsString()));
        }
        for (CSVRecord listingRecord : listingRecords) {
            if (StringUtils.isEmpty(chplProductNumber)
                    && !StringUtils.isEmpty(listingRecord.get(chplProductNumberHeadingIndex))) {
                chplProductNumber = listingRecord.get(chplProductNumberHeadingIndex);
            }
        }
        return chplProductNumber;
    }

    public String parseChplProductNumber(CSVRecord headingRecord, CSVRecord listingRecord)
        throws ValidationException {
        String chplProductNumber = null;
        int chplProductNumberHeadingIndex = getColumnIndexOfHeading(Headings.UNIQUE_ID, headingRecord);
        if (chplProductNumberHeadingIndex < 0) {
            throw new ValidationException(msgUtil.getMessage("listing.upload.headingNotFound",
                    Headings.UNIQUE_ID.getNamesAsString()));
        }
        if (!StringUtils.isEmpty(listingRecord.get(chplProductNumberHeadingIndex))) {
            chplProductNumber = listingRecord.get(chplProductNumberHeadingIndex);
        }
        return chplProductNumber;
    }

    public String parseStatus(CSVRecord headingRecord, CSVRecord listingRecord) {
        String status = null;
        int statusHeadingIndex = getColumnIndexOfHeading(Headings.RECORD_STATUS, headingRecord);
        if (statusHeadingIndex < 0) {
            throw new ValidationException(msgUtil.getMessage("listing.upload.headingNotFound",
                    Headings.RECORD_STATUS.getNamesAsString()));
        }
        if (!StringUtils.isEmpty(listingRecord.get(statusHeadingIndex))) {
            status = listingRecord.get(statusHeadingIndex);
        }
        return status;
    }

    public PendingCertifiedProductDTO parse(List<CSVRecord> records) {
        PendingCertifiedProductDTO pendingListing = new PendingCertifiedProductDTO();

        return pendingListing;
    }

    private boolean hasHeading(CSVRecord record) {
        Iterator<String> iter = record.iterator();
        while (iter.hasNext()) {
            if (Headings.getHeading(iter.next()) != null) {
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
            if (Headings.getHeading(currHeadingValue).equals(heading)) {
                return index;
            }
            index++;
        }
        return -1;
    }
}

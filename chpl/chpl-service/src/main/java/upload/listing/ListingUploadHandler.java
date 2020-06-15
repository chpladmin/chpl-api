package upload.listing;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import lombok.extern.log4j.Log4j2;

@Component("listingUploadHandler")
@Log4j2
public class ListingUploadHandler {

    public int getHeadingRecordIndex(List<CSVRecord> allCsvRecords) {
        int headingIndex = -1;
        Optional<CSVRecord> headingRecord =
                allCsvRecords.stream().filter(currRecord -> hasHeading(currRecord))
                .findFirst();
        if (headingRecord.isPresent() && headingRecord.get() != null) {
            headingIndex = (int) headingRecord.get().getRecordNumber();
        } else {
            LOGGER.warn("No heading row was found.");
        }
        return headingIndex;
    }

    public CSVRecord getHeadingRecord(List<CSVRecord> allCsvRecords) {
        return allCsvRecords.get(getHeadingRecordIndex(allCsvRecords));
    }

    public String parseChplProductNumber(CSVRecord headingRecord, CSVRecord dataRecord) {
        String chplProductNumber = null;
        //TODO:
        return chplProductNumber;
    }

    public String parseStatus(CSVRecord headingRecord, CSVRecord dataRecord) {
        String status = null;
        //TODO:
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
}

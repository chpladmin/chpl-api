package upload.listing;

import java.util.List;

import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import lombok.extern.log4j.Log4j2;

@Component("listingUploadHandler")
@Log4j2
public class ListingUploadHandler {

    public PendingCertifiedProductDTO parse(List<CSVRecord> records) {
        PendingCertifiedProductDTO pendingListing = new PendingCertifiedProductDTO();

        return pendingListing;
    }
}

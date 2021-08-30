package gov.healthit.chpl.caching.listing;

import java.time.LocalDateTime;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
class ListingCacheItem {
    private LocalDateTime timeAdded;
    private CertifiedProductSearchDetails listing;
}

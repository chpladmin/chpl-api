package gov.healthit.chpl.domain.complaint;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComplaintListingMap {
    private Long id;
    private Long complaintId;
    private Long listingId;
    private String chplProductNumber;
}

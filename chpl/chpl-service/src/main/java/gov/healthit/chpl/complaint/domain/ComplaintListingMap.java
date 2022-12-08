package gov.healthit.chpl.complaint.domain;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComplaintListingMap implements Serializable {
    private static final long serialVersionUID = 2818724922564807472L;

    private Long id;
    private Long complaintId;
    private Long listingId;
    private String chplProductNumber;
    private String developerName;
    private String productName;
    private String versionName;
}

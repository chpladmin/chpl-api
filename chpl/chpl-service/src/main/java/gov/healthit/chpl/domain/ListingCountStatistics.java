package gov.healthit.chpl.domain;

import java.io.Serializable;

import gov.healthit.chpl.dto.ListingCountStatisticsDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ListingCountStatistics implements Serializable {
    private static final long serialVersionUID = -1648513956784683632L;

    private Long id;
    private Long developerCount;
    private Long productCount;
    private CertificationEdition certificationEdition;
    private CertificationStatus certificationStatus;

    public ListingCountStatistics(ListingCountStatisticsDTO dto) {
        this.id = dto.getId();
        this.developerCount = dto.getDeveloperCount();
        this.productCount = dto.getProductCount();
        if (dto.getCertificationEdition() != null) {
            this.certificationEdition = dto.getCertificationEdition();
        }
        if (dto.getCertificationStatus() != null) {
            this.certificationStatus = dto.getCertificationStatus();
        }
    }
}

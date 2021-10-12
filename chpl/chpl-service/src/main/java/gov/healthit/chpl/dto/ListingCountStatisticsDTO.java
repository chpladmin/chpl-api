package gov.healthit.chpl.dto;

import java.io.Serializable;
import java.util.Date;

import gov.healthit.chpl.domain.CertificationStatus;
import gov.healthit.chpl.entity.statistics.ListingCountStatisticsEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ListingCountStatisticsDTO implements Serializable {

    private static final long serialVersionUID = -1536844909545189801L;

    private Long id;
    private Long developerCount;
    private Long productCount;
    private Long certificationEditionId;
    private CertificationEditionDTO certificationEdition;
    private Long certificationStatusId;
    private CertificationStatus certificationStatus;
    private Date creationDate;
    private Boolean deleted;
    private Date lastModifiedDate;
    private Long lastModifiedUser;

    public ListingCountStatisticsDTO(ListingCountStatisticsEntity entity) {
        this.id = entity.getId();
        this.developerCount = entity.getDeveloperCount();
        this.productCount = entity.getProductCount();
        this.certificationEditionId = entity.getCertificationEditionId();
        if (entity.getCertificationEdition() != null) {
            this.certificationEdition = new CertificationEditionDTO(entity.getCertificationEdition());
        }
        this.certificationStatusId = entity.getCertificationStatusId();
        if (entity.getCertificationStatus() != null) {
            this.certificationStatus = entity.getCertificationStatus().toDomain();
        }
        this.creationDate = entity.getCreationDate();
        this.deleted = entity.getDeleted();
        this.lastModifiedDate = entity.getLastModifiedDate();
        this.lastModifiedUser = entity.getLastModifiedUser();
    }

    public ListingCountStatisticsDTO(CertificationEditionDTO edition, CertificationStatus status) {
        this.developerCount = 0L;
        this.productCount = 0L;
        this.certificationEditionId = edition.getId();
        this.certificationStatusId = status.getId();
    }
}

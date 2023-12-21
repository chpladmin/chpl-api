package gov.healthit.chpl.domain;

import java.util.Date;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.dto.NonconformityTypeStatisticsDTO;
import lombok.Data;

@Data
public class NonconformityTypeStatistics {

    private Long id;
    private Long nonconformityCount;
    private String nonconformityType;
    private CertificationCriterion criterion;
    private Boolean deleted;
    private Long lastModifiedUser;
    private Date creationDate;
    private Date lastModifiedDate;

    public NonconformityTypeStatistics(NonconformityTypeStatisticsDTO dto) {
        this.nonconformityCount = dto.getNonconformityCount();
        this.nonconformityType = dto.getNonconformityType();
        this.criterion = dto.getCriterion();
        this.setCreationDate(dto.getCreationDate());
        this.setDeleted(dto.getDeleted());
        this.setLastModifiedUser(dto.getLastModifiedUser());
        this.setLastModifiedDate(dto.getLastModifiedDate());
    }
}

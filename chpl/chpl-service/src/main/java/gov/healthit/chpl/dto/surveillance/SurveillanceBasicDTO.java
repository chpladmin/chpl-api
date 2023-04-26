package gov.healthit.chpl.dto.surveillance;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;

import org.springframework.beans.BeanUtils;

import gov.healthit.chpl.compliance.surveillance.entity.SurveillanceBasicEntity;
import gov.healthit.chpl.domain.surveillance.SurveillanceBasic;
import gov.healthit.chpl.dto.SurveillanceTypeDTO;
import gov.healthit.chpl.surveillance.report.entity.PrivilegedSurveillanceEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SurveillanceBasicDTO implements Serializable {
    private static final long serialVersionUID = -2434007762463213735L;

    private Long id;
    private String friendlyId;
    private Long certifiedProductId;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long surveillanceTypeId;
    private SurveillanceTypeDTO surveillanceType;
    private Integer numRandomizedSites;
    private Integer numOpenNonconformities;
    private Integer numClosedNonconformities;
    private Boolean deleted;
    private Long lastModifiedUser;
    private Date creationDate;
    private Date lastModifiedDate;
    private Long userPermissionId;
    private String chplProductNumber;

    public SurveillanceBasicDTO(SurveillanceBasicEntity entity) {
        BeanUtils.copyProperties(entity, this);
        if (entity.getSurveillanceType() != null) {
            this.surveillanceType = new SurveillanceTypeDTO(entity.getSurveillanceType());
        }
    }

    public SurveillanceBasicDTO(PrivilegedSurveillanceEntity entity) {
        BeanUtils.copyProperties(entity, this);
        if (entity.getSurveillanceType() != null) {
            this.surveillanceType = new SurveillanceTypeDTO(entity.getSurveillanceType());
        }
    }

    public SurveillanceBasicDTO(SurveillanceBasic domain) {
        BeanUtils.copyProperties(domain, this);
        this.startDate = domain.getStartDay();
        this.endDate = domain.getEndDay();
        if (domain.getSurveillanceType() != null) {
            this.surveillanceType = new SurveillanceTypeDTO(domain.getSurveillanceType());
        }
    }
}

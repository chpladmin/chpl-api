package gov.healthit.chpl.domain.surveillance;

import java.io.Serializable;
import java.util.Date;

import org.springframework.beans.BeanUtils;

import gov.healthit.chpl.dto.surveillance.SurveillanceBasicDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SurveillanceBasic implements Serializable {
    private static final long serialVersionUID = 3750079664886758825L;

    private Long id;
    private String friendlyId;
    private Long certifiedProductId;
    private Date startDate;
    private Date endDate;
    private Long surveillanceTypeId;
    private SurveillanceType surveillanceType;
    private Integer numRandomizedSites;
    private Integer numOpenNonconformities;
    private Integer numClosedNonconformities;
    private Long userPermissionId;
    private String chplProductNumber;

    public SurveillanceBasic(SurveillanceBasicDTO dto) {
        BeanUtils.copyProperties(dto, this);
        this.surveillanceType = new SurveillanceType(dto.getSurveillanceType());
    }
}

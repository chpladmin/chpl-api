package gov.healthit.chpl.dto.listing.pending;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import gov.healthit.chpl.domain.MipsMeasure;
import gov.healthit.chpl.domain.MipsMeasurementType;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.listing.mipsMeasure.PendingListingMipsMeasureEntity;
import lombok.Data;

@Data
public class PendingCertifiedProductMipsMeasureDTO implements Serializable {
    private static final long serialVersionUID = 6800290521633648570L;
    private Long id;
    private Long pendingCertifiedProductId;
    private MipsMeasure measure;
    private MipsMeasurementType measurementType;
    private String uploadedValue;
    private List<CertificationCriterionDTO> associatedCriteria;

    public PendingCertifiedProductMipsMeasureDTO() {
        this.associatedCriteria = new ArrayList<CertificationCriterionDTO>();
    }

    public PendingCertifiedProductMipsMeasureDTO(PendingListingMipsMeasureEntity entity) {
        super();
        this.setId(entity.getId());
        this.setPendingCertifiedProductId(entity.getPendingCertifiedProductId());
        if (entity.getMeasure() != null) {
            this.measure = entity.getMeasure().convert();
        }
        if (entity.getType() != null) {
            this.measurementType = entity.getType().convert();
        }
        this.setUploadedValue(entity.getUploadedValue());
        entity.getAssociatedCriteria().stream().forEach(assocCriterionEntity -> {
            CertificationCriterionDTO criterionDto = new CertificationCriterionDTO();
            criterionDto.setId(assocCriterionEntity.getCertificationCriterionId());
        });

    }
}

package gov.healthit.chpl.scheduler.job.versionActivity;

import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.dto.ProductVersionDTO;
import lombok.Data;

@Data
public class ProductVersionActivityDTO extends ActivityDTO {
    private static final long serialVersionUID = -4601289583619405361L;

    private ProductVersionDTO originalVersion;
    private ProductVersionDTO updatedVersion;
}

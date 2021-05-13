package gov.healthit.chpl.domain;

import java.io.Serializable;

import gov.healthit.chpl.domain.surveillance.SurveillanceBasic;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComplaintSurveillanceMap implements Serializable {
    private static final long serialVersionUID = -751810206635865021L;

    private Long id;
    private Long complaintId;
    private Long surveillanceId;
    private SurveillanceBasic surveillance;
}

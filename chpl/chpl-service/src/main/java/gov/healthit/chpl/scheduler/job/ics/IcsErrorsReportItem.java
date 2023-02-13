package gov.healthit.chpl.scheduler.job.ics;

import java.io.Serializable;

import gov.healthit.chpl.domain.CertificationBody;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IcsErrorsReportItem implements Serializable {

    private static final long serialVersionUID = -1536844909545189801L;

    private Long id;
    private Long listingId;
    private CertificationBody certificationBody;
    private String reason;

    private String chplProductNumber;
    private String developer;
    private String product;
    private String version;
    private String certificationStatus;
}

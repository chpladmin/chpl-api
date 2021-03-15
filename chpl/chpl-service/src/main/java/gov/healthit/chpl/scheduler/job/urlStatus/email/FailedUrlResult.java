package gov.healthit.chpl.scheduler.job.urlStatus.email;

import java.util.Date;

import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.TestingLab;
import gov.healthit.chpl.dto.CertifiedProductSummaryDTO;
import gov.healthit.chpl.scheduler.job.urlStatus.data.UrlType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FailedUrlResult {
    private String url;
    private UrlType urlType;
    private Date lastChecked;
    private Integer responseCode;
    private String responseMessage;
    private CertificationBody acb;
    private TestingLab atl;
    private Developer developer;
    private CertifiedProductSummaryDTO listing;
    private CertificationResult certResult;
}

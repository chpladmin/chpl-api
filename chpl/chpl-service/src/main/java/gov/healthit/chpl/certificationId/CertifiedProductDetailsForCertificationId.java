package gov.healthit.chpl.certificationId;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import gov.healthit.chpl.cqm.CQMResultDetails;
import gov.healthit.chpl.domain.CertificationResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class CertifiedProductDetailsForCertificationId {
    private Long id;
    private String product;
    private String version;
    private String chplProductNumber;
    private String year;
    private Boolean curesUpdate;
    private String practiceType;
    private String acb;
    private String developer;
    private String classification;
    private String additionalSoftware;
    private List<CertificationResult> certificationResults;
    private List<CQMResultDetails> cqmResults;

    public String getAdditionalSoftware() {
        try {
            if (!StringUtils.isEmpty(additionalSoftware)) {
                return URLEncoder.encode(additionalSoftware, "UTF-8");
            }
        } catch (final UnsupportedEncodingException ex) {
            // Do nothing
        }
        return null;
    }
}

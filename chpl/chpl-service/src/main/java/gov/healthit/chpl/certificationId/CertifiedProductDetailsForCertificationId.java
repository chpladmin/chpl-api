package gov.healthit.chpl.certificationId;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
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
    private String acb;
    private String developer;
    private String additionalSoftware;
    private String practiceType;
    private String classification;
    private List<CertificationResultForCertId> certificationResults;
    private List<CqmForCertId> cqms;

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

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @Builder
    public static final class CertificationResultForCertId {
        private Long certResultId;
        private CertificationCriterion certificationCriterion;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @Builder
    public static final class CqmForCertId {
        private String cmsId;
        private String version;
        private String domain;
    }
}

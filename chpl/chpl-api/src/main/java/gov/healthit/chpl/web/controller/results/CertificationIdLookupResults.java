package gov.healthit.chpl.web.controller.results;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.util.StringUtils;

import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import lombok.Data;
import lombok.Singular;

@Data
public class CertificationIdLookupResults implements Serializable {
    private static final long serialVersionUID = 494982326653301352L;

    @Singular
    private List<Product> products = new ArrayList<Product>();
    private String ehrCertificationId;
    private String year;
    @Singular
    private Set<CertificationCriterionDTO> criteria = null;
    @Singular
    private Set<String> cqms = null;

    @Data
    public static class Product implements Serializable {
        private static final long serialVersionUID = 2970822527765944850L;
        private Long id;
        private String name;
        private String version;
        private String chplProductNumber;
        private String year;
        private Boolean curesUpdate;
        private String practiceType;
        private String acb;
        private String vendor;
        private String classification;
        private String additionalSoftware;

        /** Constructor.
         *
         * @param dto object to construct from
         */
        public Product(CertifiedProductDetailsDTO dto) {
            this.id = dto.getId();
            this.name = dto.getProduct().getName();
            this.version = dto.getVersion().getVersion();
            if (!StringUtils.isEmpty(dto.getChplProductNumber())) {
                this.setChplProductNumber(dto.getChplProductNumber());
            } else {
                this.setChplProductNumber(dto.getYearCode() + "." + dto.getTestingLabCode() + "."
                        + dto.getCertificationBodyCode() + "." + dto.getDeveloper().getDeveloperCode() + "."
                        + dto.getProductCode() + "." + dto.getVersionCode() + "." + dto.getIcsCode() + "."
                        + dto.getAdditionalSoftwareCode() + "." + dto.getCertifiedDateCode());
            }
            this.year = dto.getYear();
            this.curesUpdate = dto.getCuresUpdate();
            this.practiceType = dto.getPracticeTypeName();
            this.acb = dto.getCertificationBodyName();
            this.vendor = dto.getDeveloper().getName();
            this.classification = dto.getProductClassificationName();
            this.additionalSoftware = "";
            try {
                if (null != dto.getProductAdditionalSoftware()) {
                    this.additionalSoftware = URLEncoder.encode(dto.getProductAdditionalSoftware(), "UTF-8");
                }
            } catch (final UnsupportedEncodingException ex) {
                // Do nothing
            }
        }
    }
}

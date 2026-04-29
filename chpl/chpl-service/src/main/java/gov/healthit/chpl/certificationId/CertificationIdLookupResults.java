package gov.healthit.chpl.certificationId;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
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
    private Set<CertificationCriterion> criteria = null;
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

        public Product(CertifiedProductSearchDetails listing) {
            this.id = listing.getId();
            this.name = listing.getProduct().getName();
            this.version = listing.getVersion().getVersion();
            this.setChplProductNumber(listing.getChplProductNumber());
            this.year = listing.getEdition().getName();
            this.curesUpdate = listing.getCuresUpdate();
            this.practiceType = listing.getPracticeType().get(CertifiedProductSearchDetails.PRACTICE_TYPE_NAME_KEY) != null
                    ? listing.getPracticeType().get(CertifiedProductSearchDetails.PRACTICE_TYPE_NAME_KEY).toString() : null;
            this.acb = listing.getCertifyingBody().get(CertifiedProductSearchDetails.ACB_NAME_KEY) != null
                    ? listing.getCertifyingBody().get(CertifiedProductSearchDetails.ACB_NAME_KEY).toString() : null;
            this.vendor = listing.getDeveloper().getName();
            this.classification = listing.getClassificationType().get(CertifiedProductSearchDetails.CLASSIFICATION_TYPE_NAME_KEY) != null
                    ? listing.getClassificationType().get(CertifiedProductSearchDetails.CLASSIFICATION_TYPE_NAME_KEY).toString() : null;
            this.additionalSoftware = "";
            try {
                if (null != listing.getProductAdditionalSoftware()) {
                    this.additionalSoftware = URLEncoder.encode(listing.getProductAdditionalSoftware(), "UTF-8");
                }
            } catch (final UnsupportedEncodingException ex) {
                // Do nothing
            }
        }
    }
}

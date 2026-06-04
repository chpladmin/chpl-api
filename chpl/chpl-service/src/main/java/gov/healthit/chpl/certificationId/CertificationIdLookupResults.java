package gov.healthit.chpl.certificationId;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
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

        public Product(CertifiedProductDetailsForCertificationId listing) {
            this.id = listing.getId();
            this.name = listing.getProduct();
            this.version = listing.getVersion();
            this.setChplProductNumber(listing.getChplProductNumber());
            this.year = listing.getYear();
            this.curesUpdate = listing.getCuresUpdate();
            this.practiceType = listing.getPracticeType();
            this.acb = listing.getAcb();
            this.vendor = listing.getDeveloper();
            this.classification = listing.getClassification();
            this.additionalSoftware = listing.getAdditionalSoftware();
        }
    }
}

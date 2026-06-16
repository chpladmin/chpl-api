package gov.healthit.chpl.certificationId;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import lombok.Data;

@Data
public class CertificationIdResults implements Serializable {
    private static final long serialVersionUID = 4350936762994127624L;
    private List<Product> products;
    private String ehrCertificationId;
    private CertificationIdRequirements metCounts;
    private CertificationIdMetPercentages metPercentages;
    private ArrayList<String> missingAnd = new ArrayList<String>();
    private ArrayList<String> missingUpToDate = new ArrayList<String>();
    private List<ArrayList<String>> missingOr = new ArrayList<ArrayList<String>>();
    private List<ArrayList<String>> missingCombo = new ArrayList<ArrayList<String>>();
    private List<TreeMap<String, ArrayList<String>>> missingXOr = new ArrayList<TreeMap<String, ArrayList<String>>>();
    private String year;
    private boolean isValid;

    // Method @Data would normally generate
    public boolean isValid() {
        return isValid;
    }

    @Data
    public static class Product implements Serializable {
        private static final long serialVersionUID = 1487852426085184818L;
        private String name;
        private Long productId;
        private String version;
        private String chplProductNumber;

        public Product(CertifiedProductDetailsForCertificationId listing) {
            this.name = listing.getProduct();
            this.productId = listing.getId();
            this.version = listing.getVersion();
            this.chplProductNumber = listing.getChplProductNumber();
        }
    }
}

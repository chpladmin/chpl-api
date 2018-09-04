package gov.healthit.chpl.web.controller.results;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;

public class CertificationIdResults implements Serializable {
    private static final long serialVersionUID = 4350936762994127624L;

    static public class Product {
        private String name;
        private Long productId;
        private String version;

        public Product(CertifiedProductDetailsDTO dto) {
            this.name = dto.getProduct().getName();
            this.productId = dto.getId();
            this.version = dto.getVersion().getVersion();
        }

        public String getVersion() {
            return this.version;
        }

        public void setVersion(final String version) {
            this.version = version;
        }

        public String getName() {
            return this.name;
        }

        public void setName(final String name) {
            this.name = name;
        }

        public Long getProductId() {
            return this.productId;
        }

        public void setProductId(final Long id) {
            this.productId = id;
        }

    }

    private List<Product> products;
    private String ehrCertificationId;
    private Map<String, Integer> metCounts;
    private Map<String, Integer> metPercentages;
    protected ArrayList<String> missingAnd = new ArrayList<String>();
    protected List<ArrayList<String>> missingOr = new ArrayList<ArrayList<String>>();
    private List<ArrayList<String>> missingCombo = new ArrayList<ArrayList<String>>();
    private List<TreeMap<String,ArrayList<String>>> missingX = new ArrayList<TreeMap<String,ArrayList<String>>>();
    private String year;
    private boolean isValid;

    public String getYear() {
        return this.year;
    }

    public void setYear(final String year) {
        this.year = year;
    }

    public boolean getIsValid() {
        return isValid;
    }

    public void setIsValid(final boolean valid) {
        this.isValid = valid;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(final List<Product> products) {
        this.products = products;
    }

    public String getEhrCertificationId() {
        return this.ehrCertificationId;
    }

    public void setEhrCertificationId(final String ehrCertificationId) {
        this.ehrCertificationId = ehrCertificationId;
    }

    public Map<String, Integer> getMetPercentages() {
        return this.metPercentages;
    }

    public void setMetPercentages(final Map<String, Integer> metPercentages) {
        this.metPercentages = metPercentages;
    }

    public Map<String, Integer> getMetCounts() {
        return this.metCounts;
    }

    public void setMetCounts(final Map<String, Integer> metCounts) {
        this.metCounts = metCounts;
    }

    public ArrayList<String> getMissingAnd() {
        return missingAnd;
    }

    public void setMissingAnd(ArrayList<String> missingAnd) {
        this.missingAnd = missingAnd;
    }

    public List<ArrayList<String>> getMissingOr() {
        return missingOr;
    }

    public void setMissingOr(List<ArrayList<String>> missingOr) {
        this.missingOr = missingOr;
    }

    public List<ArrayList<String>> getMissingCombo() {
        return missingCombo;
    }

    public void setMissingCombo(List<ArrayList<String>> missingCombo) {
        this.missingCombo = missingCombo;
    }

    public List<TreeMap<String, ArrayList<String>>> getMissingX() {
        return missingX;
    }

    public void setMissingX(List<TreeMap<String, ArrayList<String>>> missingX) {
        this.missingX = missingX;
    }
    
}

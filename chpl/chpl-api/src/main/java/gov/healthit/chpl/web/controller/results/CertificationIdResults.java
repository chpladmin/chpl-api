package gov.healthit.chpl.web.controller.results;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;

/**
 * CMS Widget results object.
 */
public class CertificationIdResults implements Serializable {
    private static final long serialVersionUID = 4350936762994127624L;

    /**
     * CMS Widget specific version of a Listing.
     */
    public static class Product implements Serializable {
        private static final long serialVersionUID = 1487852426085184818L;
        private String name;
        private Long productId;
        private String version;

        /**
         * Constructor from DTO.
         * @param dto the DTO
         */
        public Product(final CertifiedProductDetailsDTO dto) {
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
    private ArrayList<String> missingAnd = new ArrayList<String>();
    private List<ArrayList<String>> missingOr = new ArrayList<ArrayList<String>>();
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

    public void setMissingAnd(final ArrayList<String> missingAnd) {
        this.missingAnd = missingAnd;
    }

    public List<ArrayList<String>> getMissingOr() {
        return missingOr;
    }

    public void setMissingOr(final List<ArrayList<String>> missingOr) {
        this.missingOr = missingOr;
    }
}

package gov.healthit.chpl.web.controller.results;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.util.StringUtils;

import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;

/**
 * Response object for Cert ID lookup results.
 */
public class CertificationIdLookupResults implements Serializable {
    private static final long serialVersionUID = 494982326653301352L;

    /**
     * Certified product embedded inside response object.
     */
    public static class Product implements Serializable {
        private static final long serialVersionUID = 2970822527765944850L;
        private Long id;
        private String name;
        private String version;
        private String chplProductNumber;
        private String year;
        private String practiceType;
        private String acb;
        private String vendor;
        private String classification;
        private String additionalSoftware;

        /** Constructor.
         *
         * @param dto object to construct from
         */
        public Product(final CertifiedProductDetailsDTO dto) {
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

        public Long getId() {
            return id;
        }

        public void setId(final Long id) {
            this.id = id;
        }

        public String getYear() {
            return this.year;
        }

        public void setYear(final String year) {
            this.year = year;
        }

        public String getVersion() {
            return this.version;
        }

        public void setVersion(final String version) {
            this.version = version;
        }

        public String getChplProductNumber() {
            return this.chplProductNumber;
        }

        public void setChplProductNumber(final String chplProductNumber) {
            this.chplProductNumber = chplProductNumber;
        }

        public String getName() {
            return this.name;
        }

        public void setName(final String name) {
            this.name = name;
        }

        public String getPracticeType() {
            return this.practiceType;
        }

        public void setPracticeType(final String practiceType) {
            this.practiceType = practiceType;
        }

        public String getAcb() {
            return this.acb;
        }

        public void setAcb(final String acb) {
            this.acb = acb;
        }

        public String getVendor() {
            return this.vendor;
        }

        public void setVendor(final String vendor) {
            this.vendor = vendor;
        }

        public String getClassification() {
            return this.classification;
        }

        public void setClassification(final String classification) {
            this.classification = classification;
        }

        public String getAdditionalSoftware() {
            return this.additionalSoftware;
        }

        public void setAdditionalSoftware(final String additionalSoftware) {
            this.additionalSoftware = additionalSoftware;
        }
    }

    private List<Product> products = new ArrayList<Product>();
    private String ehrCertificationId;
    private String year;
    private Set<String> criteria = null;
    private Set<String> cqms = null;

    public String getYear() {
        return this.year;
    }

    public void setYear(final String year) {
        this.year = year;
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

    public Set<String> getCriteria() {
        return this.criteria;
    }

    public void setCriteria(final Set<String> criteria) {
        this.criteria = criteria;
    }

    public Set<String> getCqms() {
        return this.cqms;
    }

    public void setCqms(final Set<String> cqms) {
        this.cqms = cqms;
    }
}

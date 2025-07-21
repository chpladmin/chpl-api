package gov.healthit.chpl.certificationId;

import java.io.Serializable;

public class SimpleCertificationIdWithProducts extends SimpleCertificationId implements Serializable {
    private static final long serialVersionUID = -2818214498196264669L;
    private String products;

    public SimpleCertificationIdWithProducts() {
        super();
    }

    public SimpleCertificationIdWithProducts(CertificationIdDTO dto) {
        super(dto);
    }

    public String getProducts() {
        return products;
    }

    public void setProducts(final String products) {
        this.products = products;
    }
}

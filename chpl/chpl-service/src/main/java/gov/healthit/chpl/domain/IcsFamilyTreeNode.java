package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import gov.healthit.chpl.util.Util;

@Deprecated
public class IcsFamilyTreeNode implements Serializable {

    private static final long serialVersionUID = 4170181178663367311L;

    private Long id;

    private Date certificationDate;

    private String chplProductNumber;

    private CertificationStatus certificationStatus;

    private List<CertifiedProduct> parents;

    private List<CertifiedProduct> children;

    private Developer developer;

    private ProductVersion version;

    private Product product;

    public IcsFamilyTreeNode() {
        parents = new ArrayList<CertifiedProduct>();
        children = new ArrayList<CertifiedProduct>();
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Developer getDeveloper() {
        return developer;
    }

    public void setDeveloper(final Developer developer) {
        this.developer = developer;
    }

    public ProductVersion getVersion() {
        return version;
    }

    public void setVersion(final ProductVersion version) {
        this.version = version;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(final Product product) {
        this.product = product;
    }

    public String getChplProductNumber() {
        return chplProductNumber;
    }

    public void setChplProductNumber(final String chplProductNumber) {
        this.chplProductNumber = chplProductNumber;
    }

    public CertificationStatus getCertificationStatus() {
        return certificationStatus;
    }

    public void setCertificationStatus(final CertificationStatus certificationStatus) {
        this.certificationStatus = certificationStatus;
    }

    public List<CertifiedProduct> getParents() {
        return parents;
    }

    public void setParents(final List<CertifiedProduct> parents) {
        this.parents = parents;
    }

    public List<CertifiedProduct> getChildren() {
        return children;
    }

    public void setChildren(final List<CertifiedProduct> children) {
        this.children = children;
    }

    public Date getCertificationDate() {
        return Util.getNewDate(certificationDate);
    }

    public void setCertificationDate(final Date certificationDate) {
        this.certificationDate = Util.getNewDate(certificationDate);
    }

}

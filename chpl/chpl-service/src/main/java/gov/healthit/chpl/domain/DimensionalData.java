package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.Set;

public class DimensionalData implements Serializable {
    private static final long serialVersionUID = 448737962707746739L;
    private Set<KeyValueModel> productClassifications;
    private Set<KeyValueModel> editions;
    private Set<KeyValueModel> certificationStatuses;
    private Set<KeyValueModel> practiceTypes;
    private Set<KeyValueModel> products;
    private Set<KeyValueModel> developers;
    private Set<CertificationBody> acbs;
    private Set<CertificationCriterion> certificationCriteria;
    private Set<DescriptiveModel> cqms;

    public Set<KeyValueModel> getProductClassifications() {
        return productClassifications;
    }
    public void setProductClassifications(Set<KeyValueModel> productClassifications) {
        this.productClassifications = productClassifications;
    }
    public Set<KeyValueModel> getEditions() {
        return editions;
    }
    public void setEditions(Set<KeyValueModel> editions) {
        this.editions = editions;
    }
    public Set<KeyValueModel> getCertificationStatuses() {
        return certificationStatuses;
    }
    public void setCertificationStatuses(Set<KeyValueModel> certificationStatuses) {
        this.certificationStatuses = certificationStatuses;
    }
    public Set<KeyValueModel> getPracticeTypes() {
        return practiceTypes;
    }
    public void setPracticeTypes(Set<KeyValueModel> practiceTypes) {
        this.practiceTypes = practiceTypes;
    }
    public Set<KeyValueModel> getProducts() {
        return products;
    }
    public void setProducts(Set<KeyValueModel> products) {
        this.products = products;
    }
    public Set<KeyValueModel> getDevelopers() {
        return developers;
    }
    public void setDevelopers(Set<KeyValueModel> developers) {
        this.developers = developers;
    }
    public Set<CertificationBody> getAcbs() {
        return acbs;
    }
    public void setAcbs(Set<CertificationBody> acbs) {
        this.acbs = acbs;
    }
    public Set<CertificationCriterion> getCertificationCriteria() {
        return certificationCriteria;
    }
    public void setCertificationCriteria(Set<CertificationCriterion> certificationCriteria) {
        this.certificationCriteria = certificationCriteria;
    }
    public Set<DescriptiveModel> getCqms() {
        return cqms;
    }
    public void setCqms(Set<DescriptiveModel> cqms) {
        this.cqms = cqms;
    }
}

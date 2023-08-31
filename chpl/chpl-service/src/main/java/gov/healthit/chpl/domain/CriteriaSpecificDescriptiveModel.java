package gov.healthit.chpl.domain;

import java.io.Serializable;

import org.springframework.util.StringUtils;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;

public class CriteriaSpecificDescriptiveModel extends DescriptiveModel implements Serializable {
    private static final long serialVersionUID = -1921571129798114254L;
    private CertificationCriterion criteria;

    public CriteriaSpecificDescriptiveModel() {
        super();
    }

    public CriteriaSpecificDescriptiveModel(Long id, String name, String title, CertificationCriterion criteria) {
        super(id, name, title);
        this.criteria = criteria;
    }

    public CriteriaSpecificDescriptiveModel(Long id, String name, String title, String description,
            CertificationCriterion criteria) {
        super(id, name, title, description);
        this.criteria = criteria;
    }

    public CertificationCriterion getCriteria() {
        return criteria;
    }

    public void setCriteria(final CertificationCriterion criteria) {
        this.criteria = criteria;
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof CriteriaSpecificDescriptiveModel)) {
            return false;
        }
        if (obj == this) {
            return true;
        }

        CriteriaSpecificDescriptiveModel rhs = (CriteriaSpecificDescriptiveModel) obj;

        if (StringUtils.isEmpty(rhs.getName()) != StringUtils.isEmpty(this.getName())) {
            return false;
        }

        if (this.criteria == null || rhs.criteria == null) {
            return false;
        }

        if (this.criteria.getId() == null || rhs.criteria.getId() == null) {
            return false;
        }

        return rhs.getName().equals(this.getName()) && this.criteria.getId().longValue() == rhs.criteria.getId().longValue();
    }

    @Override
    public int hashCode() {
        if (StringUtils.isEmpty(this.getName()) || this.criteria == null || this.getCriteria().getId() == null) {
            return 0;
        }
        return this.getName().hashCode() + this.getCriteria().getId().hashCode();
    }
}

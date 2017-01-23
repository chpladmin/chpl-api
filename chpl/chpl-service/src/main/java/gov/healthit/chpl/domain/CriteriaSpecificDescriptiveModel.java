package gov.healthit.chpl.domain;

import java.io.Serializable;

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
	
	public CriteriaSpecificDescriptiveModel(Long id, String name, String title, String description, CertificationCriterion criteria) {
		super(id, name, title, description);
		this.criteria = criteria;
	}

	public CertificationCriterion getCriteria() {
		return criteria;
	}

	public void setCriteria(CertificationCriterion criteria) {
		this.criteria = criteria;
	}
}

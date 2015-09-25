package gov.healthit.chpl.certifiedProduct.upload;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CQMCriterion;

@Component("newCertifiedProductHandler2014")
public class NewCertifiedProductHandler2014 extends NewCertifiedProductHandler {
	
	public List<CQMCriterion> getApplicableCqmCriterion(List<CQMCriterion> allCqms) {
		List<CQMCriterion> criteria = new ArrayList<CQMCriterion>();
		for (CQMCriterion criterion : allCqms) {
			if (criterion.getNumber().startsWith("CMS")) {
				criteria.add(criterion);
			}
		}
		return criteria;
	}
}

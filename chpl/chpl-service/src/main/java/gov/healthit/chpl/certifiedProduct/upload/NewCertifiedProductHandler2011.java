package gov.healthit.chpl.certifiedProduct.upload;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CQMCriterion;

@Component("newCertifiedProductHandler2011")
public class NewCertifiedProductHandler2011 extends NewCertifiedProductHandler {
	
	public List<CQMCriterion> getApplicableCqmCriterion(List<CQMCriterion> allCqms) {
		List<CQMCriterion> criteria = new ArrayList<CQMCriterion>();
		for (CQMCriterion criterion : allCqms) {
			if (criterion.getNumber().startsWith("NQF")) {
				criteria.add(criterion);
			}
		}
		return criteria;
	}
}

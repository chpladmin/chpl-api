package gov.healthit.chpl.certifiedProduct.validation;

import java.util.List;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.dto.PendingCertificationCriterionDTO;
import gov.healthit.chpl.dto.PendingCertifiedProductDTO;
import gov.healthit.chpl.dto.PendingCqmCriterionDTO;

@Component("ambulatoryComplete2014Validator")
public class AmbulatoryComplete2014Validator extends BaseEhr2014Validator {

	private static final String[] requiredCriteria = {"170.314(a)(2)", "170.314(a)(4)", "170.314(a)(9)",
			"170.314(a)(10)", "170.314(a)(11)", "170.314(a)(12)", "170.314(a)(13)", "170.314(a)(14)",
			"170.314(a)(15)", "170.314(b)(3)", "170.314(b)(4)", "170.314(b)(5)", "170.314(e)(1)", "170.314(e)(2)",
			"170.314(e)(3)", "170.314(f)(1)", "170.314(f)(2)", "170.314(f)(3)", "170.314(g)(2)", 
			"170.314(g)(3)", "170.314(g)(4)"};
	@Override
	public void validate(PendingCertifiedProductDTO product) {
		super.validate(product);
		
		List<PendingCertificationCriterionDTO> certificationCriterion = product.getCertificationCriterion();
		for(int i = 0; i < requiredCriteria.length; i++) {
			boolean hasCert = false;
			for(PendingCertificationCriterionDTO certCriteria : certificationCriterion) {
				if(certCriteria.getNumber().equals(requiredCriteria[i]) && certCriteria.isMeetsCriteria()) {
					hasCert = true;
				}
			}	
			if(!hasCert) {
				product.setValidationStatus(ValidationStatus.ERROR);
				product.getValidationMessages().add("Required certificaiton criteria " + requiredCriteria[i] + " was not found.");
			}
		}		
		
		int ambulatoryCqmCount = 0;
		for(PendingCqmCriterionDTO cqmCriterion : product.getCqmCriterion()) {
			if(cqmCriterion.getTypeId() == AMBULATORY_CQM_TYPE_ID && cqmCriterion.isMeetsCriteria()) {
				ambulatoryCqmCount++;
			}
		}
		
		if(ambulatoryCqmCount < 9) {
			product.setValidationStatus(ValidationStatus.ERROR);
			product.getValidationMessages().add(ambulatoryCqmCount + " Ambulatory CQM(s) were found but at least 9 are required.");
		}
		
		//TODO
		/*At least 6 Ambulatory CQMs from CMS’s recommended
		core set must be checked
		o At least 3 CQM Domains from the set selected by CMS for
		EPs must be checked*/
	}

}

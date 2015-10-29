package gov.healthit.chpl.certifiedProduct.validation;

import java.util.List;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.dto.PendingCertificationCriterionDTO;
import gov.healthit.chpl.dto.PendingCertifiedProductDTO;
import gov.healthit.chpl.dto.PendingCqmCriterionDTO;

@Component("inpatientComplete2014Validator")
public class InpatientComplete2014Validator extends BaseEhr2014Validator {

	private static final String[] requiredCriteria = {"170.314(a)(2)", "170.314(a)(4)", "170.314(a)(9)",
			"170.314(a)(10)", "170.314(a)(11)", "170.314(a)(12)", "170.314(a)(13)", "170.314(a)(14)",
			"170.314(a)(15)", "170.314(a)(16)", "170.314(a)(17)", "170.314(b)(3)", "170.314(b)(4)", 
			"170.314(b)(5)", "170.314(b)(6)", "170.314(e)(1)", "170.314(f)(1)", "170.314(f)(2)", 
			"170.314(f)(3)", "170.314(f)(4)", "170.314(g)(2)", "170.314(g)(3)", "170.314(g)(4)"};

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
		
		int inpatientCqmCount = 0;
		for(PendingCqmCriterionDTO cqmCriterion : product.getCqmCriterion()) {
			if(cqmCriterion.getTypeId() == INPATIENT_CQM_TYPE_ID && cqmCriterion.isMeetsCriteria()) {
				inpatientCqmCount++;
			}
		}
		
		if(inpatientCqmCount < 16) {
			product.setValidationStatus(ValidationStatus.ERROR);
			product.getValidationMessages().add(inpatientCqmCount + " Inpatient CQM(s) were found but at least 16 are required.");
		}
		
		//TODO
		/* At least 3 CQM Domains from the set selected by CMS for
EH/CAHs must be checked
*/
	}

}

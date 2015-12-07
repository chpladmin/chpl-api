package gov.healthit.chpl.certifiedProduct.validation;

import java.util.List;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.PendingCertificationCriterionDTO;
import gov.healthit.chpl.dto.PendingCertifiedProductDTO;

@Component("baseEhrValidator")
public class BaseEhr2014Validator implements CertifiedProductValidator {

	private static final String[] requiredCriteria = {"170.314 (a)(1)", "170.314 (a)(3)",
			"170.314 (a)(5)", "170.314 (a)(6)", "170.314 (a)(7)", "170.314 (a)(8)", "170.314 (b)(1)", 
			"170.314 (b)(2)", "170.314 (b)(7)", "170.314 (c)(1)", "170.314 (c)(2)", "170.314 (c)(3)",
			"170.314 (d)(1)", "170.314 (d)(2)", "170.314 (d)(3)", "170.314 (d)(4)", "170.314 (d)(5)",
			"170.314 (d)(6)", "170.314 (d)(7)", "170.314 (d)(8)"};
	
	@Override
	public void validate(PendingCertifiedProductDTO product) {
		List<PendingCertificationCriterionDTO> certificationCriterion = product.getCertificationCriterion();
		for(int i = 0; i < requiredCriteria.length; i++) {
			boolean hasCert = false;
			for(PendingCertificationCriterionDTO certCriteria : certificationCriterion) {
				if(certCriteria.getNumber().equals(requiredCriteria[i]) && certCriteria.isMeetsCriteria()) {
					hasCert = true;
				}
			}	
			if(!hasCert) {
				product.getWarningMessages().add("Required certification criteria " + requiredCriteria[i] + " was not found.");
			}
		}
	}
	
	@Override
	public void validate(CertifiedProductSearchDetails product) {
		List<CertificationResult> certificationCriterion = product.getCertificationResults();
		for(int i = 0; i < requiredCriteria.length; i++) {
			boolean hasCert = false;
			for(CertificationResult certCriteria : certificationCriterion) {
				if(certCriteria.getNumber().equals(requiredCriteria[i]) && certCriteria.isSuccess()) {
					hasCert = true;
				}
			}	
			if(!hasCert) {
				product.getWarningMessages().add("Required certification criteria " + requiredCriteria[i] + " was not found.");
			}
		}
	}
}

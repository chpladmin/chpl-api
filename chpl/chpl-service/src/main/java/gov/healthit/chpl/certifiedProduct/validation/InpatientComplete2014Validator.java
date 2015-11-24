package gov.healthit.chpl.certifiedProduct.validation;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.domain.CQMResultDetails;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.PendingCertificationCriterionDTO;
import gov.healthit.chpl.dto.PendingCertifiedProductDTO;
import gov.healthit.chpl.dto.PendingCqmCriterionDTO;

@Component("inpatientComplete2014Validator")
public class InpatientComplete2014Validator extends InpatientModular2014Validator {

	private static final String[] requiredCriteria = {"170.314 (a)(2)", "170.314 (a)(4)", "170.314 (a)(9)",
			"170.314 (a)(10)", "170.314 (a)(11)", "170.314 (a)(12)", "170.314 (a)(13)", "170.314 (a)(14)",
			"170.314 (a)(15)", "170.314 (a)(16)", "170.314 (a)(17)", "170.314 (b)(3)", "170.314 (b)(4)", 
			"170.314 (b)(5)(B)", "170.314 (b)(6)", "170.314 (e)(1)", "170.314 (f)(1)", "170.314 (f)(2)", 
			"170.314 (f)(3)", "170.314 (f)(4)", "170.314 (g)(2)", "170.314 (g)(3)", "170.314 (g)(4)"};

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
				product.getWarningMessages().add("Required certification criteria " + requiredCriteria[i] + " was not found.");
			}
		}		
		
		//must have at least 16 cqms
		int inpatientCqmCount = 0;
		for(PendingCqmCriterionDTO cqmCriterion : product.getCqmCriterion()) {
			if(cqmCriterion.getTypeId() == INPATIENT_CQM_TYPE_ID && cqmCriterion.isMeetsCriteria()) {
				inpatientCqmCount++;
			}
		}
		
		if(inpatientCqmCount < 16) {
			product.getWarningMessages().add(inpatientCqmCount + " Inpatient CQM(s) were found but at least 16 are required.");
		}
		
		//must check at least 3 domains
		Set<String> checkedDomains = new HashSet<String>();
		for(PendingCqmCriterionDTO cqmCriterion : product.getCqmCriterion()) {
			if(!StringUtils.isEmpty(cqmCriterion.getDomain()) && 
					cqmCriterion.getTypeId() == INPATIENT_CQM_TYPE_ID && cqmCriterion.isMeetsCriteria()) {
				checkedDomains.add(cqmCriterion.getDomain());
			}
		}
		if(checkedDomains.size() < 3) {
			product.getWarningMessages().add(checkedDomains.size() + " domains were found but at least 3 are required.");
		}
	}

	@Override
	public void validate(CertifiedProductSearchDetails product) {
		super.validate(product);
		
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
		
		//must have at least 16 cqms
		int inpatientCqmCount = 0;
		for(CQMResultDetails cqmCriterion : product.getCqmResults()) {
			if(cqmCriterion.getTypeId() == INPATIENT_CQM_TYPE_ID && cqmCriterion.isSuccess()) {
				inpatientCqmCount++;
			}
		}
		
		if(inpatientCqmCount < 16) {
			product.getWarningMessages().add(inpatientCqmCount + " Inpatient CQM(s) were found but at least 16 are required.");
		}
		
		//must check at least 3 domains
		Set<String> checkedDomains = new HashSet<String>();
		for(CQMResultDetails cqmCriterion : product.getCqmResults()) {
			if(!StringUtils.isEmpty(cqmCriterion.getDomain()) && 
					cqmCriterion.getTypeId() == INPATIENT_CQM_TYPE_ID && cqmCriterion.isSuccess()) {
				checkedDomains.add(cqmCriterion.getDomain());
			}
		}
		if(checkedDomains.size() < 3) {
			product.getWarningMessages().add(checkedDomains.size() + " domains were found but at least 3 are required.");
		}
	}
}

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

@Component("ambulatoryComplete2014Validator")
public class AmbulatoryComplete2014Validator extends AmbulatoryModular2014Validator {

	private static final String[] requiredCriteria = {"170.314 (a)(2)", "170.314 (a)(4)", "170.314 (a)(9)",
			"170.314 (a)(10)", "170.314 (a)(11)", "170.314 (a)(12)", "170.314 (a)(13)", "170.314 (a)(14)",
			"170.314 (a)(15)", "170.314 (b)(3)", "170.314 (b)(4)", "170.314 (b)(5)(A)", "170.314 (e)(1)", "170.314 (e)(2)",
			"170.314 (e)(3)", "170.314 (f)(1)", "170.314 (f)(2)", "170.314 (f)(3)", "170.314 (g)(2)", 
			"170.314 (g)(3)", "170.314 (g)(4)"};
	private static final String[] coreCqms = {"CMS146v1", "CMS155v1", "CMS153v1", "CMS126v1", "CMS117v1",
				"CMS154v1", "CMS136v1", "CMS2v1", "CMS75v1", "CMS165v1", "CMS156v1", "CMS138v1",
				"CMS166v1", "CMS68v1", "CMS69v1", "CMS50v1", "CMS90v1"};
	
	//TODO: only check g1-g4 validation and make sure we have a test report url that is a fully qualified URL
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
		
		//has at least 9 cqms
		int ambulatoryCqmCount = 0;
		for(PendingCqmCriterionDTO cqmCriterion : product.getCqmCriterion()) {
			if(cqmCriterion.getTypeId() == AMBULATORY_CQM_TYPE_ID && cqmCriterion.isMeetsCriteria()) {
				ambulatoryCqmCount++;
			}
		}
		
		if(ambulatoryCqmCount < 9) {
			product.getWarningMessages().add(ambulatoryCqmCount + " Ambulatory CQM(s) were found but at least 9 are required.");
		}
		
		//has at least 6 cqms from the core set
		int coreAmbulatoryCount = 0;
		for(int i = 0; i < coreCqms.length; i++) {
			String[] coreCqmParts = coreCqms[i].split("v");
			String cqmNumber = coreCqmParts[0];
			String cqmVersion = "v" + coreCqmParts[1];
			int cqmVersionMinimunm = new Integer(coreCqmParts[1]).intValue();
			boolean hasCurr = false;
			for(PendingCqmCriterionDTO cqmCriterion : product.getCqmCriterion()) {
				if(cqmCriterion.getTypeId() == AMBULATORY_CQM_TYPE_ID && cqmCriterion.isMeetsCriteria() && 
					cqmCriterion.getCmsId().equals(cqmNumber)) {
					
					String currCriterionVersion = cqmCriterion.getVersion();
					String currCriterionVersionNumber = currCriterionVersion.substring(currCriterionVersion.indexOf("v")+1);
					int versionNumber = new Integer(currCriterionVersionNumber).intValue();
					if(versionNumber >= cqmVersionMinimunm) {
						hasCurr = true;
					} 
				}
			}
			if(hasCurr) {
				coreAmbulatoryCount++;
			}
		}
		if(coreAmbulatoryCount < 6) {
			product.getWarningMessages().add(coreAmbulatoryCount + " Ambulatory CQM(s) were met from CMS's recommended core set were found but at least 6 are required.");
		}
		
		//checks 3 domains
		Set<String> checkedDomains = new HashSet<String>();
		for(PendingCqmCriterionDTO cqmCriterion : product.getCqmCriterion()) {
			if(!StringUtils.isEmpty(cqmCriterion.getDomain()) &&
					cqmCriterion.getTypeId() == AMBULATORY_CQM_TYPE_ID && cqmCriterion.isMeetsCriteria()) {
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
		
		//has at least 9 cqms
		int ambulatoryCqmCount = 0;
		for(CQMResultDetails cqmCriterion : product.getCqmResults()) {
			if(cqmCriterion.getTypeId() == AMBULATORY_CQM_TYPE_ID && cqmCriterion.isSuccess()) {
				ambulatoryCqmCount++;
			}
		}
		
		if(ambulatoryCqmCount < 9) {
			product.getWarningMessages().add(ambulatoryCqmCount + " Ambulatory CQM(s) were found but at least 9 are required.");
		}
		
		//has at least 6 cqms from the core set
		int coreAmbulatoryCount = 0;
		for(int i = 0; i < coreCqms.length; i++) {
			String[] coreCqmParts = coreCqms[i].split("v");
			String cqmNumber = coreCqmParts[0];
			int cqmVersionMinimunm = new Integer(coreCqmParts[1]).intValue();
			boolean hasCurr = false;
			for(CQMResultDetails cqmCriterion : product.getCqmResults()) {
				if(cqmCriterion.getTypeId() == AMBULATORY_CQM_TYPE_ID && cqmCriterion.isSuccess() && 
					cqmCriterion.getCmsId().equals(cqmNumber)) {
					
					for(String currCriterionVersion : cqmCriterion.getSuccessVersions()) {
						String currCriterionVersionNumber = currCriterionVersion.substring(currCriterionVersion.indexOf("v")+1);
						int versionNumber = new Integer(currCriterionVersionNumber).intValue();
						if(versionNumber >= cqmVersionMinimunm) {
							hasCurr = true;
							break;
						} 
					}
					
					if(hasCurr) {
						coreAmbulatoryCount++;
					}
				}
			}
		}
		if(coreAmbulatoryCount < 6) {
			product.getWarningMessages().add(coreAmbulatoryCount + " Ambulatory CQM(s) were met from CMS's recommended core set were found but at least 6 are required.");
		}
		
		//checks 3 domains
		Set<String> checkedDomains = new HashSet<String>();
		for(CQMResultDetails cqmCriterion : product.getCqmResults()) {
			if(!StringUtils.isEmpty(cqmCriterion.getDomain()) &&
					cqmCriterion.getTypeId() == AMBULATORY_CQM_TYPE_ID && cqmCriterion.isSuccess()) {
				checkedDomains.add(cqmCriterion.getDomain());
			}
		}
		if(checkedDomains.size() < 3) {
			product.getWarningMessages().add(checkedDomains.size() + " domains were found but at least 3 are required.");
		}
	}

}

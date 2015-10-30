package gov.healthit.chpl.certifiedProduct.validation;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.dto.PendingCertificationCriterionDTO;
import gov.healthit.chpl.dto.PendingCertifiedProductDTO;

@Component("ambulatoryModular2014Validator")
public class AmbulatoryModular2014Validator implements PendingCertifiedProductValidator {

	private static final String[] g1ComplimentaryCerts = {"170.314(a)(1)", "170.314(a)(3)", "170.314(a)(4)", 
			"170.314(a)(5)", "170.314(a)(6)", "170.314(a)(7)", "170.314(a)(9)", "170.314(a)(11)",
			"170.314(a)(12)", "170.314(a)(13)", "170.314(a)(14)", "170.314(a)(15)", "170.314(b)(2)",
			"170.314(b)(3)", "170.314(b)(4)", "170.314(b)(5)", "170.314(e)(1)", "170.314(e)(2)",
			"170.314(e)(3)"};
	private static final String[] g2ComplimentaryCerts = {"170.314(a)(1)", "170.314(a)(3)", "170.314(a)(4)", 
			"170.314(a)(5)", "170.314(a)(6)", "170.314(a)(7)", "170.314(a)(9)", "170.314(a)(11)",
			"170.314(a)(12)", "170.314(a)(13)", "170.314(a)(14)", "170.314(a)(15)", "170.314(b)(2)",
			"170.314(b)(3)", "170.314(b)(4)", "170.314(b)(5)", "170.314(e)(1)", "170.314(e)(2)",
			"170.314(e)(3)"};
	private static final String[] g3ComplimentaryCerts = {"170.314(a)(1)", "170.314(a)(2)", "170.314(a)(6)",
			"170.314(a)(7)", "170.314(a)(8)", "170.314(b)(3)", "170.314(b)(4)"};
	
	@Override
	public void validate(PendingCertifiedProductDTO product) {
		//TODO:
		//One less than all the mandatory Ambulatory certification criteria must be
		//met
		
		//check (g)(1)
		boolean hasG1Cert = false;
		for(PendingCertificationCriterionDTO certCriteria : product.getCertificationCriterion()) {
			if(certCriteria.getNumber().equals("170.314(g)(1)") && certCriteria.isMeetsCriteria()) {
				hasG1Cert = true;
			}
		}	
		if(hasG1Cert) {
			boolean hasAtLeastOneCertPartner = false;
			for(int i = 0; i < g1ComplimentaryCerts.length && !hasAtLeastOneCertPartner; i++) {
				for(PendingCertificationCriterionDTO certCriteria : product.getCertificationCriterion()) {
					if(certCriteria.getNumber().equals(g1ComplimentaryCerts[i]) &&
							certCriteria.isMeetsCriteria()) {
						hasAtLeastOneCertPartner = true;
					}
				}
			}
			
			if(!hasAtLeastOneCertPartner) {
				product.setValidationStatus(ValidationStatus.ERROR);
				product.getValidationMessages().add("Certification criterion 170.314(g)(1) exists but a required compliemtnary certification was not found.");
			}
		}
		
		//check (g)(2)
		boolean hasG2Cert = false;
		for(PendingCertificationCriterionDTO certCriteria : product.getCertificationCriterion()) {
			if(certCriteria.getNumber().equals("170.314(g)(2)") && certCriteria.isMeetsCriteria()) {
				hasG2Cert = true;
			}
		}	
		if(hasG2Cert) {
			boolean hasAtLeastOneCertPartner = false;
			for(int i = 0; i < g2ComplimentaryCerts.length && !hasAtLeastOneCertPartner; i++) {
				for(PendingCertificationCriterionDTO certCriteria : product.getCertificationCriterion()) {
					if(certCriteria.getNumber().equals(g2ComplimentaryCerts[i]) &&
							certCriteria.isMeetsCriteria()) {
						hasAtLeastOneCertPartner = true;
					}
				}
			}
			
			if(!hasAtLeastOneCertPartner) {
				product.setValidationStatus(ValidationStatus.ERROR);
				product.getValidationMessages().add("Certification criterion 170.314(g)(2) exists but a required compliemtnary certification was not found.");
			}
		}
		
		//TODO:
		/*
		 *  It is legitimate to permit an EHR Module to be certified to
			just (g)(2). If an EHR Module gets certified to just (g)(2), then it
			has to be able to meet the (g)(2) criteria requirements for all the
			MU %
			-based measures for a setting, but doesn’t have to included
			capabilities that support MU %
			-based measures like CPOE, etc. 
		 */
		
		//check presence of both certs
		if(hasG1Cert && hasG2Cert) {
			product.setValidationStatus(ValidationStatus.ERROR);
			product.getValidationMessages().add("Product cannot have both 170.314(g)(1) and 170.314(g)(2) certification");
		}
		
		//check (g)(3)
		boolean hasG3Cert = false;
		for(PendingCertificationCriterionDTO certCriteria : product.getCertificationCriterion()) {
			if(certCriteria.getNumber().equals("170.314(g)(3)") && certCriteria.isMeetsCriteria()) {
				hasG3Cert = true;
			}
		}	
		if(hasG3Cert) {
			boolean hasAtLeastOneCertPartner = false;
			for(int i = 0; i < g3ComplimentaryCerts.length && !hasAtLeastOneCertPartner; i++) {
				for(PendingCertificationCriterionDTO certCriteria : product.getCertificationCriterion()) {
					if(certCriteria.getNumber().equals(g3ComplimentaryCerts[i]) &&
							certCriteria.isMeetsCriteria()) {
						hasAtLeastOneCertPartner = true;
					}
				}
			}
			
			if(!hasAtLeastOneCertPartner) {
				product.setValidationStatus(ValidationStatus.ERROR);
				product.getValidationMessages().add("Certification criterion 170.314(g)(3) exists but a required compliemtnary certification was not found.");
			}
		}
		
		//check (g)(4)
		boolean hasG4Cert = false;
		for(PendingCertificationCriterionDTO certCriteria : product.getCertificationCriterion()) {
			if(certCriteria.getNumber().equals("170.314(g)(4)") && certCriteria.isMeetsCriteria()) {
				hasG4Cert = true;
			}
		}
		if(!hasG4Cert) {
			product.setValidationStatus(ValidationStatus.ERROR);
			product.getValidationMessages().add("Certification 170.314(g)(4) is required but was not found.");
		}
	}
}

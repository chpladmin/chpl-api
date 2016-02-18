package gov.healthit.chpl.certifiedProduct.validation;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.PendingCertificationCriterionDTO;
import gov.healthit.chpl.dto.PendingCertifiedProductDTO;

@Component("inpatientModular2014Validator")
public class InpatientModular2014Validator implements CertifiedProductValidator {

	private static final String[] g1ComplementaryCerts = {"170.314 (a)(1)", "170.314 (a)(3)", "170.314 (a)(4)", 
			"170.314 (a)(5)", "170.314 (a)(6)", "170.314 (a)(7)", "170.314 (a)(9)", "170.314 (a)(11)",
			"170.314 (a)(12)", "170.314 (a)(13)", "170.314 (a)(15)", "170.314 (a)(16)", 
			"170.314 (a)(17)", "170.314 (b)(2)", "170.314 (b)(3)", "170.314 (b)(4)", "170.314 (b)(5)(B)", 
			"170.314 (b)(6)", "170.314 (e)(1)"};
	private static final String[] g2ComplementaryCerts = {"170.314 (a)(1)", "170.314 (a)(3)", "170.314 (a)(4)", 
			"170.314 (a)(5)", "170.314 (a)(6)", "170.314 (a)(7)", "170.314 (a)(9)", "170.314 (a)(11)",
			"170.314 (a)(12)", "170.314 (a)(13)", "170.314 (a)(15)", "170.314 (a)(16)", 
			"170.314 (a)(17)", "170.314 (b)(2)", "170.314 (b)(3)", "170.314 (b)(4)", "170.314 (b)(5)(B)", 
			"170.314 (b)(6)", "170.314 (e)(1)"};
	private static final String[] g3ComplementaryCerts = {"170.314 (a)(1)", "170.314 (a)(2)", "170.314 (a)(6)",
			"170.314 (a)(7)", "170.314 (a)(8)", "170.314 (a)(16)", "170.314 (b)(3)", "170.314 (b)(4)"};
	
	@Override
	public void validate(PendingCertifiedProductDTO product) {
		//TODO:
		//One less than all the mandatory Inpatient certification criteria must be
		//met. i think i don't need to check this?
		
		//check (g)(1)
		boolean hasG1Cert = false;
		for(PendingCertificationCriterionDTO certCriteria : product.getCertificationCriterion()) {
			if(certCriteria.getNumber().equals("170.314 (g)(1)") && certCriteria.isMeetsCriteria()) {
				hasG1Cert = true;
			}
		}	
		if(hasG1Cert) {
			boolean hasAtLeastOneCertPartner = false;
			for(int i = 0; i < g1ComplementaryCerts.length && !hasAtLeastOneCertPartner; i++) {
				for(PendingCertificationCriterionDTO certCriteria : product.getCertificationCriterion()) {
					if(certCriteria.getNumber().equals(g1ComplementaryCerts[i]) && certCriteria.isMeetsCriteria()) {
						hasAtLeastOneCertPartner = true;
					}
				}
			}
			
			if(!hasAtLeastOneCertPartner) {
				product.getErrorMessages().add("Certification criterion 170.314 (g)(1) exists but a required complementary certification was not found.");
			}
		}
		
		//check (g)(2)
		boolean hasG2Cert = false;
		for(PendingCertificationCriterionDTO certCriteria : product.getCertificationCriterion()) {
			if(certCriteria.getNumber().equals("170.314 (g)(2)") && certCriteria.isMeetsCriteria()) {
				hasG2Cert = true;
			}
		}	
		if(hasG2Cert) {
			boolean hasAtLeastOneCertPartner = false;
			for(int i = 0; i < g2ComplementaryCerts.length && !hasAtLeastOneCertPartner; i++) {
				for(PendingCertificationCriterionDTO certCriteria : product.getCertificationCriterion()) {
					if(certCriteria.getNumber().equals(g2ComplementaryCerts[i]) && certCriteria.isMeetsCriteria()) {
						hasAtLeastOneCertPartner = true;
					}
				}
			}
			
			if(!hasAtLeastOneCertPartner) {
				product.getErrorMessages().add("Certification criterion 170.314 (g)(2) exists but a required complementary certification was not found.");
			}
		}
		
		//check presence of both certs
		if(hasG1Cert && hasG2Cert) {
			product.getErrorMessages().add("Product cannot have both 170.314 (g)(1) and 170.314 (g)(2) certification");
		}
		
		//check (g)(3)
		boolean hasG3Cert = false;
		for(PendingCertificationCriterionDTO certCriteria : product.getCertificationCriterion()) {
			if(certCriteria.getNumber().equals("170.314 (g)(3)") && certCriteria.isMeetsCriteria()) {
				hasG3Cert = true;
			}
		}	
		if(hasG3Cert) {
			boolean hasAtLeastOneCertPartner = false;
			for(int i = 0; i < g3ComplementaryCerts.length && !hasAtLeastOneCertPartner; i++) {
				for(PendingCertificationCriterionDTO certCriteria : product.getCertificationCriterion()) {
					if(certCriteria.getNumber().equals(g3ComplementaryCerts[i]) &&
							certCriteria.isMeetsCriteria()) {
						hasAtLeastOneCertPartner = true;
					}
				}
			}
			
			if(!hasAtLeastOneCertPartner) {
				product.getErrorMessages().add("Certification criterion 170.314 (g)(3) exists but a required complementary certification was not found.");
			}
		}
		
		//check (g)(4)
		boolean hasG4Cert = false;
		for(PendingCertificationCriterionDTO certCriteria : product.getCertificationCriterion()) {
			if(certCriteria.getNumber().equals("170.314 (g)(4)") && certCriteria.isMeetsCriteria()) {
				hasG4Cert = true;
			}
		}
		if(!hasG4Cert) {
			product.getErrorMessages().add("Certification 170.314 (g)(4) is required but was not found.");
		}
	}
	
	@Override
	public void validate(CertifiedProductSearchDetails product) {
		//TODO:
		//One less than all the mandatory Inpatient certification criteria must be
		//met. i think i don't need to check this?
		
		//check (g)(1)
		boolean hasG1Cert = false;
		for(CertificationResult certCriteria : product.getCertificationResults()) {
			if(certCriteria.getNumber().equals("170.314 (g)(1)") && certCriteria.isSuccess()) {
				hasG1Cert = true;
			}
		}	
		if(hasG1Cert) {
			boolean hasAtLeastOneCertPartner = false;
			for(int i = 0; i < g1ComplementaryCerts.length && !hasAtLeastOneCertPartner; i++) {
				for(CertificationResult certCriteria : product.getCertificationResults()) {
					if(certCriteria.getNumber().equals(g1ComplementaryCerts[i]) && certCriteria.isSuccess()) {
						hasAtLeastOneCertPartner = true;
					}
				}
			}
			
			if(!hasAtLeastOneCertPartner) {
				product.getErrorMessages().add("Certification criterion 170.314 (g)(1) exists but a required complementary certification was not found.");
			}
		}
		
		//check (g)(2)
		boolean hasG2Cert = false;
		for(CertificationResult certCriteria : product.getCertificationResults()) {
			if(certCriteria.getNumber().equals("170.314 (g)(2)") && certCriteria.isSuccess()) {
				hasG2Cert = true;
			}
		}	
		if(hasG2Cert) {
			boolean hasAtLeastOneCertPartner = false;
			for(int i = 0; i < g2ComplementaryCerts.length && !hasAtLeastOneCertPartner; i++) {
				for(CertificationResult certCriteria : product.getCertificationResults()) {
					if(certCriteria.getNumber().equals(g2ComplementaryCerts[i]) && certCriteria.isSuccess()) {
						hasAtLeastOneCertPartner = true;
					}
				}
			}
			
			if(!hasAtLeastOneCertPartner) {
				product.getErrorMessages().add("Certification criterion 170.314 (g)(2) exists but a required complementary certification was not found.");
			}
		}
		
		//check presence of both certs
		if(hasG1Cert && hasG2Cert) {
			product.getErrorMessages().add("Product cannot have both 170.314 (g)(1) and 170.314 (g)(2) certification");
		}
		
		//check (g)(3)
		boolean hasG3Cert = false;
		for(CertificationResult certCriteria : product.getCertificationResults()) {
			if(certCriteria.getNumber().equals("170.314 (g)(3)") && certCriteria.isSuccess()) {
				hasG3Cert = true;
			}
		}	
		if(hasG3Cert) {
			boolean hasAtLeastOneCertPartner = false;
			for(int i = 0; i < g3ComplementaryCerts.length && !hasAtLeastOneCertPartner; i++) {
				for(CertificationResult certCriteria : product.getCertificationResults()) {
					if(certCriteria.getNumber().equals(g3ComplementaryCerts[i]) &&
							certCriteria.isSuccess()) {
						hasAtLeastOneCertPartner = true;
					}
				}
			}
			
			if(!hasAtLeastOneCertPartner) {
				product.getErrorMessages().add("Certification criterion 170.314 (g)(3) exists but a required complementary certification was not found.");
			}
		}
		
		//check (g)(4)
		boolean hasG4Cert = false;
		for(CertificationResult certCriteria : product.getCertificationResults()) {
			if(certCriteria.getNumber().equals("170.314 (g)(4)") && certCriteria.isSuccess()) {
				hasG4Cert = true;
			}
		}
		if(!hasG4Cert) {
			product.getErrorMessages().add("Certification 170.314 (g)(4) is required but was not found.");
		}
	}
}
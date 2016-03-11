package gov.healthit.chpl.certifiedProduct.validation;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.PendingCertifiedProductDTO;

@Component("inpatientModular2014Validator")
public class InpatientModular2014Validator extends CertifiedProduct2014Validator {

	private static final String[] g1ComplementaryCerts = {"170.314 (b)(5)(B)", "170.314 (a)(16)", "170.314 (a)(17)",
			"170.314 (b)(6)"};
	
	private static final String[] g2ComplementaryCerts = {"170.314 (b)(5)(B)","170.314 (a)(16)", "170.314 (a)(17)",
			"170.314 (b)(6)"};
		
	@Override
	public void validate(PendingCertifiedProductDTO product) {
		super.validate(product);
		
		//check (g)(1)
		boolean hasG1Cert = false;
		for(PendingCertificationResultDTO certCriteria : product.getCertificationCriterion()) {
			if(certCriteria.getNumber().equals("170.314 (g)(1)") && certCriteria.getMeetsCriteria()) {
				hasG1Cert = true;
			}
		}	
		if(hasG1Cert) {
			boolean hasG1Complement = false;
			for(int i = 0; i < g1ComplementaryCerts.length && !hasG1Complement; i++) {
				for(PendingCertificationResultDTO certCriteria : product.getCertificationCriterion()) {
					if(certCriteria.getNumber().equals(g1ComplementaryCerts[i]) && certCriteria.getMeetsCriteria()) {
						hasG1Complement = true;
					}
				}
			}
			
			if(!hasG1Complement) {
				product.getErrorMessages().add("(g)(1) was found without a required related certification.");
			}
		}
		
		//check (g)(2)
		boolean hasG2Cert = false;
		for(PendingCertificationResultDTO certCriteria : product.getCertificationCriterion()) {
			if(certCriteria.getNumber().equals("170.314 (g)(2)") && certCriteria.getMeetsCriteria()) {
				hasG2Cert = true;
			}
		}	
		if(hasG2Cert) {
			boolean hasG2Complement = false;
			for(int i = 0; i < g2ComplementaryCerts.length && !hasG2Complement; i++) {
				for(PendingCertificationResultDTO certCriteria : product.getCertificationCriterion()) {
					if(certCriteria.getNumber().equals(g2ComplementaryCerts[i]) && certCriteria.getMeetsCriteria()) {
						hasG2Complement = true;
					}
				}
			}
			
			if(!hasG2Complement) {
				product.getErrorMessages().add("(g)(2) was found without a required related certification.");
			}
		}
		
		if(hasG1Cert && hasG2Cert) {
			product.getWarningMessages().add("Both (g)(1) and (g)(2) were found which is not typically permitted.");
		}
	}
	
	@Override
	public void validate(CertifiedProductSearchDetails product) {
		super.validate(product);
		
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
				product.getErrorMessages().add("(g)(1) was found without a required related certification.");
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
			boolean hasG2Complement = false;
			for(int i = 0; i < g2ComplementaryCerts.length && !hasG2Complement; i++) {
				for(CertificationResult certCriteria : product.getCertificationResults()) {
					if(certCriteria.getNumber().equals(g2ComplementaryCerts[i]) && certCriteria.isSuccess()) {
						hasG2Complement = true;
					}
				}
			}
			
			if(!hasG2Complement) {
				product.getErrorMessages().add("(g)(2) was found without a required related certification.");
			}
		}
		
		if(hasG1Cert && hasG2Cert) {
			product.getWarningMessages().add("Both (g)(1) and (g)(2) were found which is not typically permitted.");
		}
	}
}
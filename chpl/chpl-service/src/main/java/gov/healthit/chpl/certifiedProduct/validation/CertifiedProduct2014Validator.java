package gov.healthit.chpl.certifiedProduct.validation;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.domain.CQMResultDetails;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.PendingCertifiedProductDTO;
import gov.healthit.chpl.dto.PendingCqmCriterionDTO;

@Component("certifiedProduct2014Validator")
public class CertifiedProduct2014Validator extends CertifiedProductValidatorImpl {

	private static final String[] g1ComplementaryCerts = {"170.314 (a)(1)", "170.314 (a)(3)", "170.314 (a)(4)", 
			"170.314 (a)(5)", "170.314 (a)(6)", "170.314 (a)(7)", "170.314 (a)(9)", "170.314 (a)(11)",
			"170.314 (a)(12)", "170.314 (a)(13)", "170.314 (a)(14)", "170.314 (a)(15)", "170.314 (a)(18)", 
			"170.314 (a)(19)", "170.314 (a)(20)", "170.314 (b)(2)",
			"170.314 (b)(3)", "170.314 (b)(4)", "170.314 (e)(1)"};
	
	private static final String[] g2ComplementaryCerts = {"170.314 (a)(1)", "170.314 (a)(3)", "170.314 (a)(4)", 
			"170.314 (a)(5)", "170.314 (a)(6)", "170.314 (a)(7)", "170.314 (a)(9)", "170.314 (a)(11)",
			"170.314 (a)(12)", "170.314 (a)(13)", "170.314 (a)(14)", "170.314 (a)(15)",
			"170.314 (a)(18)", "170.314 (a)(19)", "170.314 (a)(20)", "170.314 (b)(2)",
			"170.314 (b)(3)", "170.314 (b)(4)", "170.314 (e)(1)"};
	
	private static final String[] cqmRequiredCerts = {"170.314 (c)(1)", "170.314 (c)(2)", "170.314 (c)(3)"};
	private static final String[] g3ComplementaryCerts = {"170.314 (a)(1)", "170.314 (a)(2)", "170.314 (a)(6)",
			"170.314 (a)(7)", "170.314 (a)(8)", "170.314 (a)(16)", "170.314 (a)(18)", "170.314 (a)(19)", 
			"170.314 (a)(20)", "170.314 (b)(3)", "170.314 (b)(4)", "170.314 (b)(9)"};
	
	@Override
	public void validate(PendingCertifiedProductDTO product) {
		super.validate(product);
		
		// check cqms
		boolean isCqmRequired = false;
		for(PendingCertificationResultDTO cert : product.getCertificationCriterion()) {
			for(int i = 0; i < cqmRequiredCerts.length; i++) {
				if(cert.getNumber().equals(cqmRequiredCerts[i]) && cert.getMeetsCriteria()) {
					isCqmRequired = true;
				}
			}
		}
		if(isCqmRequired) {
			boolean hasOneCqmWithVersion = false;
			for(PendingCqmCriterionDTO cqm : product.getCqmCriterion()) {
				if(cqm.isMeetsCriteria() && !StringUtils.isEmpty(cqm.getVersion())) {
					hasOneCqmWithVersion = true;
				}
			}
			if(!hasOneCqmWithVersion) {
				product.getErrorMessages().add("At least one CQM/version is required but was not found.");
			}
		}
		
		//g1 check
		boolean hasG1 = false;
		for(PendingCertificationResultDTO cert : product.getCertificationCriterion()) {
			if(cert.getNumber().equals("170.314 (g)(1)") && cert.getMeetsCriteria()) {
				hasG1 = true;
			}
		}
		boolean hasG1Complement = false;
		for(PendingCertificationResultDTO cert : product.getCertificationCriterion()) {
			for(int i = 0; i < g1ComplementaryCerts.length; i++) {
				if(cert.getNumber().equals(g1ComplementaryCerts[i]) && cert.getMeetsCriteria()) {
					hasG1Complement = true;
				}
			}
		}
		
		if(hasG1 && !hasG1Complement) {
			product.getErrorMessages().add("(g)(1) was found without a required related certification.");
		}
		
		//g2 check
		boolean hasG2 = false;
		for(PendingCertificationResultDTO cert : product.getCertificationCriterion()) {
			if(cert.getNumber().equals("170.314 (g)(2)") && cert.getMeetsCriteria()) {
				hasG2 = true;
			}
		}
		boolean hasG2Complement = false;
		for(PendingCertificationResultDTO cert : product.getCertificationCriterion()) {
			for(int i = 0; i < g2ComplementaryCerts.length; i++) {
				if(cert.getNumber().equals(g2ComplementaryCerts[i]) && cert.getMeetsCriteria()) {
					hasG2Complement = true;
				}
			}
		}
		
		if(hasG2 && !hasG2Complement) {
			product.getErrorMessages().add("(g)(2) was found without a required related certification.");
		}
				
		//g4 check
		boolean hasG4 = false;
		for(PendingCertificationResultDTO cert : product.getCertificationCriterion()) {
			if(cert.getNumber().equals("170.314 (g)(4)") && cert.getMeetsCriteria()) {
				hasG4 = true;
			}
		}
		if(!hasG4) {
			product.getErrorMessages().add("Required certification criteria 170.314 (g)(4) was not found.");
		}
		
		//g3 check
		boolean hasG3 = false;
		for(PendingCertificationResultDTO cert : product.getCertificationCriterion()) {
			if(cert.getNumber().equals("170.314 (g)(3)") && cert.getMeetsCriteria()) {
				hasG3 = true;
			}
		}
		boolean hasG3Complement = false;
		for(PendingCertificationResultDTO cert : product.getCertificationCriterion()) {
			for(int i = 0; i < g3ComplementaryCerts.length; i++) {
				if(cert.getNumber().equals(g3ComplementaryCerts[i]) && cert.getMeetsCriteria()) {
					hasG3Complement = true;
				}
			}
		}
		
		if(hasG3 && !hasG3Complement) {
			product.getErrorMessages().add("(g)(3) was found without a required related certification.");
		}
		if(hasG3Complement && !hasG3) {
			product.getErrorMessages().add("A certification that requires (g)(3) was found but (g)(3) was not.");
		}
	}
	
	@Override
	public void validate(CertifiedProductSearchDetails product) {
		
		// check cqms
		boolean isCqmRequired = false;
		for(CertificationResult cert : product.getCertificationResults()) {
			for(int i = 0; i < cqmRequiredCerts.length; i++) {
				if(cert.getNumber().equals(cqmRequiredCerts[i]) && cert.isSuccess()) {
					isCqmRequired = true;
				}
			}
		}
		if(isCqmRequired) {
			boolean hasOneCqmWithVersion = false;
			for(CQMResultDetails cqm : product.getCqmResults()) {
				if(cqm.isSuccess() && cqm.getSuccessVersions() != null && cqm.getSuccessVersions().size() > 0) {
					hasOneCqmWithVersion = true;
				}
			}
			if(!hasOneCqmWithVersion) {
				product.getErrorMessages().add("At least one CQM/version is required but was not found.");
			}
		}
		
		//g1 check
		boolean hasG1 = false;
		for(CertificationResult cert : product.getCertificationResults()) {
			if(cert.getNumber().equals("170.314 (g)(1)") && cert.isSuccess()) {
				hasG1 = true;
			}
		}
		boolean hasG1Complement = false;
		for(CertificationResult cert : product.getCertificationResults()) {
			for(int i = 0; i < g1ComplementaryCerts.length; i++) {
				if(cert.getNumber().equals(g1ComplementaryCerts[i]) && cert.isSuccess()) {
					hasG1Complement = true;
				}
			}
		}
		
		if(hasG1 && !hasG1Complement) {
			product.getErrorMessages().add("(g)(1) was found without a required related certification.");
		}
		
		//g2 check
		boolean hasG2 = false;
		for(CertificationResult cert : product.getCertificationResults()) {
			if(cert.getNumber().equals("170.314 (g)(2)") && cert.isSuccess()) {
				hasG2 = true;
			}
		}
		boolean hasG2Complement = false;
		for(CertificationResult cert : product.getCertificationResults()) {
			for(int i = 0; i < g2ComplementaryCerts.length; i++) {
				if(cert.getNumber().equals(g2ComplementaryCerts[i]) && cert.isSuccess()) {
					hasG2Complement = true;
				}
			}
		}
		
		if(hasG2 && !hasG2Complement) {
			product.getErrorMessages().add("(g)(2) was found without a required related certification.");
		}
		
		//g4 check
		boolean hasG4 = false;
		for(CertificationResult cert : product.getCertificationResults()) {
			if(cert.getNumber().equals("170.314 (g)(4)") && cert.isSuccess()) {
				hasG4 = true;
			}
		}
		if(!hasG4) {
			product.getErrorMessages().add("(g)(4) is required but was not found.");
		}
				
		//g3 check
		boolean hasG3 = false;
		for(CertificationResult cert : product.getCertificationResults()) {
			if(cert.getNumber().equals("170.314 (g)(3)") && cert.isSuccess()) {
				hasG3 = true;
			}
		}
		boolean hasG3Complement = false;
		for(CertificationResult cert : product.getCertificationResults()) {
			for(int i = 0; i < g3ComplementaryCerts.length; i++) {
				if(cert.getNumber().equals(g3ComplementaryCerts[i]) && cert.isSuccess()) {
					hasG3Complement = true;
				}
			}
		}
		
		if(hasG3 && !hasG3Complement) {
			product.getErrorMessages().add("(g)(3) was found without a required related certification.");
		}
		if(hasG3Complement && !hasG3) {
			product.getErrorMessages().add("A certification that requires (g)(3) was found but (g)(3) was not.");
		}
	}
}

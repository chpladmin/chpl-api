package gov.healthit.chpl.validation.certifiedProduct;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.domain.CQMResultDetails;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultTestTask;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.PendingCertifiedProductDTO;
import gov.healthit.chpl.dto.PendingCqmCriterionDTO;
import gov.healthit.chpl.util.CertificationResultRules;

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
	
	
	public String[] getG1ComplimentaryCerts() {
		return g1ComplementaryCerts;
	}
	
	public String[] getG2ComplimentaryCerts() {
		return g2ComplementaryCerts;
	}
	
	@Override
	public void validate(PendingCertifiedProductDTO product) {
		super.validate(product);
		
		if(product.getPracticeTypeId() == null) {
			product.getErrorMessages().add("Practice setting is required but was not found.");
		}
		if(product.getProductClassificationId() == null) {
			product.getErrorMessages().add("Product classification is required but was not found.");
		}
		if(StringUtils.isEmpty(product.getReportFileLocation())) {
			product.getErrorMessages().add("Test Report URL is required but was not found.");
		} 
//		else if(urlRegex.matcher(product.getReportFileLocation()).matches() == false) {
//			product.getErrorMessages().add("Test Report URL provided is not a valid URL format.");
//		}
		
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
	protected void validateDemographics(PendingCertifiedProductDTO product) {
		super.validateDemographics(product);
		
		if(StringUtils.isEmpty(product.getReportFileLocation())) {
			product.getErrorMessages().add("A test result summary URL is required.");
		}
		
		for(PendingCertificationResultDTO cert : product.getCertificationCriterion()) {
			if(cert.getMeetsCriteria() != null && cert.getMeetsCriteria() == Boolean.TRUE) {
				boolean gapEligibleAndTrue = false;
				if(certRules.hasCertOption(cert.getNumber(), CertificationResultRules.GAP) &&
						cert.getGap() == Boolean.TRUE) {
					gapEligibleAndTrue = true;
				}
				
				if(certRules.hasCertOption(cert.getNumber(), CertificationResultRules.SED) &&
						cert.getSed() == null) {
					product.getErrorMessages().add("SED is required for certification " + cert.getNumber() + ".");
				}
				if(!gapEligibleAndTrue && 
						certRules.hasCertOption(cert.getNumber(), CertificationResultRules.TEST_DATA) &&
						(cert.getTestData() == null || cert.getTestData().size() == 0)) {
					product.getErrorMessages().add("Test Data is required for certification " + cert.getNumber() + ".");
				}
			}
		}
	}
	
	@Override
	public void validate(CertifiedProductSearchDetails product) {
		super.validate(product);
		
		if(product.getPracticeType() == null || product.getPracticeType().get("id") == null) {
			product.getErrorMessages().add("Practice setting is required but was not found.");
		}
		if(product.getClassificationType() == null || product.getClassificationType().get("id") == null) {
			product.getErrorMessages().add("Product classification is required but was not found.");
		}
		if(StringUtils.isEmpty(product.getReportFileLocation())) {
			product.getErrorMessages().add("Test Report URL is required but was not found.");
		} 
//		else if(urlRegex.matcher(product.getReportFileLocation()).matches() == false) {
//			product.getErrorMessages().add("Test Report URL provided is not a valid URL format.");
//		}
		
		//check sed/ucd/tasks
		for(CertificationResult cert : product.getCertificationResults()) {
			if(cert.isSed() != null && cert.isSed().booleanValue() == true) {
				for(CertificationResult certCriteria : product.getCertificationResults()) {
					if (certCriteria.getTestTasks() != null && certCriteria.getTestTasks().size() > 0){
						for(CertificationResultTestTask task : certCriteria.getTestTasks()) {
							if(task.getTestParticipants() == null || task.getTestParticipants().size() < 5) {
								product.getWarningMessages().add("A test task for certification " + certCriteria.getNumber() + " requires at least 5 participants.");
							}
						}
					}
				}
			}
		}
		
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
	
	@Override
	protected void validateDemographics(CertifiedProductSearchDetails product) {
		super.validateDemographics(product);
		
		if(StringUtils.isEmpty(product.getReportFileLocation())) {
			product.getErrorMessages().add("A test result summary URL is required.");
		}
		
		//this is not supposed to matcht the list of things checked for pending products
	}
}

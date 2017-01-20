package gov.healthit.chpl.validation.certifiedProduct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.TestToolDAO;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultTestTool;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.PendingCertificationResultTestToolDTO;
import gov.healthit.chpl.dto.PendingCertifiedProductDTO;
import gov.healthit.chpl.dto.TestToolDTO;
import gov.healthit.chpl.manager.CertifiedProductDetailsManager;
import gov.healthit.chpl.util.CertificationResultRules;

@Component("inpatientModular2014Validator")
public class InpatientModular2014Validator extends CertifiedProduct2014Validator {
	private static final Logger logger = LogManager.getLogger(AmbulatoryModular2014Validator.class);

	private static final String[] g1ComplementaryCerts = {"170.314 (b)(5)(B)", "170.314 (a)(16)", "170.314 (a)(17)", "170.314 (b)(6)"};
	private static final String[] g2ComplementaryCerts = {"170.314 (b)(5)(B)","170.314 (a)(16)", "170.314 (a)(17)", "170.314 (b)(6)"};
		
	@Autowired private TestToolDAO ttDao;
	@Autowired CertifiedProductDetailsManager cpdManager;

	@Override
	public String[] getG1ComplimentaryCerts() {
		String[] certs = super.getG1ComplimentaryCerts();
		String[] allCerts = new String[certs.length + g1ComplementaryCerts.length];
		
		int allCertIndex = 0;

		for(int j = 0; j < certs.length; j++) {
			allCerts[allCertIndex] = new String(certs[j]);
			allCertIndex++;
		}
		for(int j = 0; j < g1ComplementaryCerts.length; j++) {
			allCerts[allCertIndex] = new String(g1ComplementaryCerts[j]);
			allCertIndex++;
		}
		return allCerts;
	}
	
	@Override
	public String[] getG2ComplimentaryCerts() {
		String[] certs = super.getG2ComplimentaryCerts();
		String[] allCerts = new String[certs.length + g2ComplementaryCerts.length];
		
		int allCertIndex = 0;
		for(int j = 0; j < certs.length; j++) {
			allCerts[allCertIndex] = new String(certs[j]);
			allCertIndex++;
		}
			
		for(int j = 0; j < g2ComplementaryCerts.length; j++) {
			allCerts[allCertIndex] = new String(g2ComplementaryCerts[j]);
			allCertIndex++;
		}
		
		return allCerts;
	}
	
	@Override
	protected void validateDemographics(PendingCertifiedProductDTO product) {
		super.validateDemographics(product);
		
		for(PendingCertificationResultDTO cert : product.getCertificationCriterion()) {
			if(cert.getMeetsCriteria() != null && cert.getMeetsCriteria() == Boolean.TRUE) {
				
				boolean gapEligibleAndTrue = false;
				if(certRules.hasCertOption(cert.getNumber(), CertificationResultRules.GAP) &&
						cert.getGap() == Boolean.TRUE) {
					gapEligibleAndTrue = true;
				}
				
				if(!gapEligibleAndTrue && 
						certRules.hasCertOption(cert.getNumber(), CertificationResultRules.TEST_TOOLS_USED) &&
						!cert.getNumber().equals("170.314 (g)(1)") && 
						!cert.getNumber().equals("170.314 (g)(2)") &&
						(cert.getTestTools() == null || cert.getTestTools().size() == 0)) {
						product.getErrorMessages().add("Test Tools are required for certification " + cert.getNumber() + ".");
				}
			}
		}
	}
	
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
			String[] g1Certs = getG1ComplimentaryCerts();
			boolean hasG1Complement = false;
			for(int i = 0; i < g1Certs.length && !hasG1Complement; i++) {
				for(PendingCertificationResultDTO certCriteria : product.getCertificationCriterion()) {
					if(certCriteria.getNumber().equals(g1Certs[i]) && certCriteria.getMeetsCriteria()) {
						hasG1Complement = true;
					}
				}
			}
			
			if(!hasG1Complement) {
				product.getWarningMessages().add("(g)(1) was found without a required related certification.");
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
			String[] g2Certs = getG2ComplimentaryCerts();
			boolean hasG2Complement = false;
			for(int i = 0; i < g2Certs.length && !hasG2Complement; i++) {
				for(PendingCertificationResultDTO certCriteria : product.getCertificationCriterion()) {
					if(certCriteria.getNumber().equals(g2Certs[i]) && certCriteria.getMeetsCriteria()) {
						hasG2Complement = true;
					}
				}
			}
			
			if(!hasG2Complement) {
				product.getWarningMessages().add("(g)(2) was found without a required related certification.");
			}
		}
		
		if(hasG1Cert && hasG2Cert) {
			product.getWarningMessages().add("Both (g)(1) and (g)(2) were found which is not typically permitted.");
		}
	}
	
	@Override
	protected void validateDemographics(CertifiedProductSearchDetails product) {
		super.validateDemographics(product);
		
		//NOTE: this is not supposed to match the list of things checked for pending products
		
		//we do have to check for retired test tools here though because users are not allowed to:
				//1. remove a retired test tool that was previously used, or 
				//2. add a retired test tool that was not previously used, or
				//3. change the test tool version of a retired test tool
				
				//have to get the old product to compare previous and current test tools
				CertifiedProductSearchDetails oldProduct = null;
				try {
						oldProduct = cpdManager.getCertifiedProductDetails(product.getId());
				} catch(EntityRetrievalException ex) {
					logger.error("Could not find certified product details with id " + product.getId(), ex);
				}
				
				//compare old with current - they cannot remove test tools
				for(CertificationResult oldCert : oldProduct.getCertificationResults()) {
					if(oldCert.isSuccess() != null && oldCert.isSuccess() == Boolean.TRUE && oldCert.getTestToolsUsed() != null) {
						for(CertificationResultTestTool oldTestTool : oldCert.getTestToolsUsed()) {
							TestToolDTO testTool = ttDao.getById(oldTestTool.getTestToolId());
							if(testTool.isRetired()) {
								boolean certStillExists = false;
								boolean certHasRetiredTool = false;
								//make sure this test tool still exists in the passed in product/cert
								//because users are not allowed to remove exisitng test tools if they are retired
								for(CertificationResult cert : product.getCertificationResults()) {
									if(cert.isSuccess() != null && cert.isSuccess() == Boolean.TRUE) {
										if(cert.getNumber().equals(oldCert.getNumber())) {
											certStillExists = true;
											for(CertificationResultTestTool newTestTool : cert.getTestToolsUsed()) {
												if(newTestTool.getTestToolId().equals(oldTestTool.getTestToolId())) {
													certHasRetiredTool = true;
												}
											}
										}
									}
								}
								if(certStillExists && !certHasRetiredTool) {
									product.getErrorMessages().add("Certification " + oldCert.getNumber() + " exists but is missing the required test tool '" + testTool.getName() + "'. This tool was present before and cannt be removed since it is retired.");
								}
							}
						}
					}
				}
				
				//compare current test tools with old ones - they cannot add any additional retired tools or change versions
				//now check all the new certs for whatever is required
				for(CertificationResult cert : product.getCertificationResults()) {
					if(cert.isSuccess() != null && cert.isSuccess() == Boolean.TRUE) {
						if(certRules.hasCertOption(cert.getNumber(), CertificationResultRules.TEST_TOOLS_USED) &&
								cert.getTestToolsUsed() != null && cert.getTestToolsUsed().size() > 0) {
							for(CertificationResultTestTool toolMap : cert.getTestToolsUsed()) {
								if(toolMap.getTestToolId() == null) {
									TestToolDTO foundTestTool = ttDao.getByName(toolMap.getTestToolName());
									if(foundTestTool == null || foundTestTool.getId() == null) {
										product.getErrorMessages().add("Certification " + cert.getNumber() + " contains an invalid test tool name: '" + toolMap.getTestToolName() + "'.");
									} 
								}
							}
						}
					}
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
			String[] g1Certs = getG1ComplimentaryCerts();
			boolean hasAtLeastOneCertPartner = false;
			for(int i = 0; i < g1Certs.length && !hasAtLeastOneCertPartner; i++) {
				for(CertificationResult certCriteria : product.getCertificationResults()) {
					if(certCriteria.getNumber().equals(g1Certs[i]) && certCriteria.isSuccess()) {
						hasAtLeastOneCertPartner = true;
					}
				}
			}
			
			if(!hasAtLeastOneCertPartner) {
				product.getWarningMessages().add("(g)(1) was found without a required related certification.");
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
			String[] g2Certs = getG2ComplimentaryCerts();
			boolean hasG2Complement = false;
			for(int i = 0; i < g2Certs.length && !hasG2Complement; i++) {
				for(CertificationResult certCriteria : product.getCertificationResults()) {
					if(certCriteria.getNumber().equals(g2Certs[i]) && certCriteria.isSuccess()) {
						hasG2Complement = true;
					}
				}
			}
			
			if(!hasG2Complement) {
				product.getWarningMessages().add("(g)(2) was found without a required related certification.");
			}
		}
		
		if(hasG1Cert && hasG2Cert) {
			product.getWarningMessages().add("Both (g)(1) and (g)(2) were found which is not typically permitted.");
		}
	}
}
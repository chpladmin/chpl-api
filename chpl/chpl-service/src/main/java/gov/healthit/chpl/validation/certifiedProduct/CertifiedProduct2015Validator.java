package gov.healthit.chpl.validation.certifiedProduct;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.AccessibilityStandardDAO;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.MacraMeasureDAO;
import gov.healthit.chpl.dao.TestFunctionalityDAO;
import gov.healthit.chpl.dao.TestToolDAO;
import gov.healthit.chpl.domain.CQMResultCertification;
import gov.healthit.chpl.domain.CQMResultDetails;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultMacraMeasure;
import gov.healthit.chpl.domain.CertificationResultTestFunctionality;
import gov.healthit.chpl.domain.CertificationResultTestTask;
import gov.healthit.chpl.domain.CertificationResultTestTool;
import gov.healthit.chpl.domain.CertifiedProductQmsStandard;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.MacraMeasure;
import gov.healthit.chpl.dto.MacraMeasureDTO;
import gov.healthit.chpl.dto.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.PendingCertificationResultMacraMeasureDTO;
import gov.healthit.chpl.dto.PendingCertificationResultTestFunctionalityDTO;
import gov.healthit.chpl.dto.PendingCertificationResultTestTaskDTO;
import gov.healthit.chpl.dto.PendingCertificationResultTestTaskParticipantDTO;
import gov.healthit.chpl.dto.PendingCertificationResultTestToolDTO;
import gov.healthit.chpl.dto.PendingCertifiedProductDTO;
import gov.healthit.chpl.dto.PendingCertifiedProductQmsStandardDTO;
import gov.healthit.chpl.dto.PendingCqmCertificationCriterionDTO;
import gov.healthit.chpl.dto.PendingCqmCriterionDTO;
import gov.healthit.chpl.dto.TestFunctionalityDTO;
import gov.healthit.chpl.dto.TestToolDTO;
import gov.healthit.chpl.entity.PendingCertificationResultG1MacraMeasureEntity;
import gov.healthit.chpl.manager.CertifiedProductDetailsManager;
import gov.healthit.chpl.util.CertificationResultRules;

@Component("certifiedProduct2015Validator")
public class CertifiedProduct2015Validator extends CertifiedProductValidatorImpl {
	private static final Logger logger = LogManager.getLogger(CertifiedProduct2015Validator.class);

	private static final String[] aComplimentaryCerts = {"170.315 (d)(1)", "170.315 (d)(2)", "170.315 (d)(3)",
			"170.315 (d)(4)", "170.315 (d)(5)", "170.315 (d)(6)", "170.315 (d)(7)"};
	
	private static final String[] bComplimentaryCerts = {"170.315 (d)(1)", "170.315 (d)(2)", "170.315 (d)(3)",
			"170.315 (d)(5)", "170.315 (d)(6)", "170.315 (d)(7)", "170.315 (d)(8)"};
	
	private static final String[] cComplimentaryCerts = {"170.315 (d)(1)", "170.315 (d)(2)", "170.315 (d)(3)",
			"170.315 (d)(5)"};
	
	private static final String[] e1ComplimentaryCerts = {"170.315 (d)(1)", "170.315 (d)(2)", "170.315 (d)(3)",
		"170.315 (d)(5)", "170.315 (d)(7)", "170.315 (d)(9)"};
	
	private static final String[] e2Ore3ComplimentaryCerts = {"170.315 (d)(1)", "170.315 (d)(2)", "170.315 (d)(3)",
		"170.315 (d)(5)", "170.315 (d)(9)"};
	
	private static final String[] fComplimentaryCerts = {"170.315 (d)(1)", "170.315 (d)(2)", "170.315 (d)(3)",
		"170.315 (d)(7)"};
	
	private static final String[] g7Org8Org9ComplimentaryCerts = {"170.315 (d)(1)", "170.315 (d)(9)"};

	private static final String[] hComplimentaryCerts = {"170.315 (d)(1)", "170.315 (d)(2)", "170.315 (d)(3)"};
	
	private static final String[] ucdRequiredCerts = {"170.315 (a)(1)", "170.315 (a)(2)", "170.315 (a)(3)", 
			"170.315 (a)(4)", "170.315 (a)(5)", "170.315 (a)(6)", "170.315 (a)(7)", "170.315 (a)(8)", 
			"170.315 (a)(9)", "170.315 (a)(14)", "170.315 (b)(2)", "170.315 (b)(3)"};

	private static final String[] g6CertsToCheck = {"170.315 (b)(1)", "170.315 (b)(2)", "170.315 (b)(4)",
			"170.315 (b)(6)", "170.315 (b)(9)", "170.315 (e)(1)", "170.315 (g)(9)"};
	
	@Autowired TestToolDAO testToolDao;
	@Autowired TestFunctionalityDAO testFuncDao;
	@Autowired AccessibilityStandardDAO asDao;
	@Autowired MacraMeasureDAO macraDao;
	@Autowired CertifiedProductDetailsManager cpdManager;
	
	@Override
	public void validate(PendingCertifiedProductDTO product) {
		super.validate(product);
		
		List<String> allMetCerts = new ArrayList<String>();
		for(PendingCertificationResultDTO certCriteria : product.getCertificationCriterion()) {
			if(certCriteria.getMeetsCriteria()) {
				allMetCerts.add(certCriteria.getNumber());
			}
		}
		
		List<String> errors = checkClassOfCriteriaForErrors("170.315 (a)", allMetCerts, Arrays.asList(aComplimentaryCerts));
		product.getErrorMessages().addAll(errors);
		
		errors = checkClassOfCriteriaForErrors("170.315 (b)", allMetCerts, Arrays.asList(bComplimentaryCerts));
		product.getErrorMessages().addAll(errors);
		
		errors = checkClassOfCriteriaForErrors("170.315 (c)", allMetCerts, Arrays.asList(cComplimentaryCerts));
		product.getErrorMessages().addAll(errors);

		errors = checkClassOfCriteriaForErrors("170.315 (f)", allMetCerts, Arrays.asList(fComplimentaryCerts));
		product.getErrorMessages().addAll(errors);
		
		errors = checkClassOfCriteriaForErrors("170.315 (h)", allMetCerts, Arrays.asList(hComplimentaryCerts));
		product.getErrorMessages().addAll(errors);
		
		errors = checkSpecificCriteriaForErrors("170.315 (e)(1)", allMetCerts, Arrays.asList(e1ComplimentaryCerts));
		product.getErrorMessages().addAll(errors);
		
        //check for (c)(1), (c)(2), (c)(3), (c)(4)
        boolean meetsC1Criterion = hasCert("170.315 (c)(1)", allMetCerts);
        boolean meetsC2Criterion = hasCert("170.315 (c)(2)", allMetCerts);
        boolean meetsC3Criterion = hasCert("170.315 (c)(3)", allMetCerts);
        boolean meetsC4Criterion = hasCert("170.315 (c)(4)", allMetCerts);
        boolean hasC1Cqm = false;
        boolean hasC2Cqm = false;
        boolean hasC3Cqm = false;
        boolean hasC4Cqm = false;
        for(PendingCqmCriterionDTO cqm : product.getCqmCriterion()) {
            List<String> cqmCerts = new ArrayList<String>();
            for(PendingCqmCertificationCriterionDTO criteria : cqm.getCertifications()) {
                cqmCerts.add(criteria.getCertificationCriteriaNumber());
            }
            hasC1Cqm = hasC1Cqm || hasCert("170.315 (c)(1)", cqmCerts);
            hasC2Cqm = hasC2Cqm || hasCert("170.315 (c)(2)", cqmCerts);
            hasC3Cqm = hasC3Cqm || hasCert("170.315 (c)(3)", cqmCerts);
            hasC4Cqm = hasC4Cqm || hasCert("170.315 (c)(4)", cqmCerts);
        }
        if (meetsC1Criterion && !hasC1Cqm) {
            product.getErrorMessages().add("Certification criterion 170.315 (c)(1) was found but no matching Clinical Quality Measurement was found.");
        } else if (!meetsC1Criterion && hasC1Cqm) {
            product.getErrorMessages().add("A Clinical Quality Measurement was found under Certification criterion 170.315 (c)(1), but the product does not attest to that criterion.");
        }
        if (meetsC2Criterion && !hasC2Cqm) {
            product.getErrorMessages().add("Certification criterion 170.315 (c)(2) was found but no matching Clinical Quality Measurement was found.");
        } else if (!meetsC2Criterion && hasC2Cqm) {
            product.getErrorMessages().add("A Clinical Quality Measurement was found under Certification criterion 170.315 (c)(2), but the product does not attest to that criterion.");
        }
        if (meetsC3Criterion && !hasC3Cqm) {
            product.getErrorMessages().add("Certification criterion 170.315 (c)(3) was found but no matching Clinical Quality Measurement was found.");
        } else if (!meetsC3Criterion && hasC3Cqm) {
            product.getErrorMessages().add("A Clinical Quality Measurement was found under Certification criterion 170.315 (c)(3), but the product does not attest to that criterion.");
        }
        if (meetsC4Criterion && !hasC4Cqm) {
            product.getErrorMessages().add("Certification criterion 170.315 (c)(4) was found but no matching Clinical Quality Measurement was found.");
        } else if (!meetsC4Criterion && hasC4Cqm) {
            product.getErrorMessages().add("A Clinical Quality Measurement was found under Certification criterion 170.315 (c)(4), but the product does not attest to that criterion.");
        }

        //check for (e)(2) or (e)(3) certs
		boolean meetsE2Criterion = hasCert("170.315 (e)(2)", allMetCerts);
		boolean meetsE3Criterion = hasCert("170.315 (e)(3)", allMetCerts);
		if(meetsE2Criterion || meetsE3Criterion) {
			for(int i = 0; i < e2Ore3ComplimentaryCerts.length; i++) {
				boolean hasComplimentaryCert = false;
				for(PendingCertificationResultDTO certCriteria : product.getCertificationCriterion()) {
					if(certCriteria.getNumber().equals(e2Ore3ComplimentaryCerts[i]) && certCriteria.getMeetsCriteria()) {
						hasComplimentaryCert = true;
					}
				}
				
				if(!hasComplimentaryCert) {
					product.getErrorMessages().add("Certification criterion 170.315 (e)(2) or 170.315 (e)(3) was found so " + e2Ore3ComplimentaryCerts[i] + " is required but was not found.");

				}
			}
		}
		
		//check for (g)(7) or (g)(8) or (g)(9) certs
		boolean meetsG7Criterion = hasCert("170.315 (g)(7)", allMetCerts);
		boolean meetsG8Criterion = hasCert("170.315 (g)(8)", allMetCerts);
		boolean meetsG9Criterion = hasCert("170.315 (g)(9)", allMetCerts);	
		if(meetsG7Criterion || meetsG8Criterion || meetsG9Criterion) {
			for(int i = 0; i < g7Org8Org9ComplimentaryCerts.length; i++) {
				boolean hasComplimentaryCert = false;
				for(PendingCertificationResultDTO certCriteria : product.getCertificationCriterion()) {
					if(certCriteria.getNumber().equals(g7Org8Org9ComplimentaryCerts[i]) && certCriteria.getMeetsCriteria()) {
						hasComplimentaryCert = true;
					}
				}
				
				if(!hasComplimentaryCert) {
					product.getErrorMessages().add("Certification criterion 170.315 (g)(7) or 170.315 (g)(8) or 170.315 (g)(9) was found so " + g7Org8Org9ComplimentaryCerts[i] + " is required but was not found.");

				}
			}
			
			boolean meetsD2Criterion = hasCert("170.315 (d)(2)", allMetCerts);
			boolean meetsD10Criterion = hasCert("170.315 (d)(10)", allMetCerts);
			if(!meetsD2Criterion && !meetsD10Criterion) {
				product.getErrorMessages().add("Certification criterion 170.315 (g)(7) or 170.315 (g)(8) or 170.315 (g)(9) was found so 170.315 (d)(2) or 170.315 (d)(10) is also required.");
			}
		}
		
		//g3 checks
		boolean needsG3 = false;
		for(int i = 0; i < ucdRequiredCerts.length; i++) {
			if(hasCert(ucdRequiredCerts[i], allMetCerts)) {
				needsG3 = true;
				
				//check for full set of UCD data
				for(PendingCertificationResultDTO certCriteria : product.getCertificationCriterion()) {
					if(certCriteria.getNumber().equals(ucdRequiredCerts[i])) {
						if(certCriteria.getUcdProcesses() == null || certCriteria.getUcdProcesses().size() == 0) {
							product.getErrorMessages().add("Certification " + certCriteria.getNumber() + " requires at least one UCD process.");
						} 
						if(certCriteria.getTestTasks() == null || certCriteria.getTestTasks().size() == 0) {
							product.getErrorMessages().add("Certification " + certCriteria.getNumber() + " requires at least one test task.");
						} else {
							for(PendingCertificationResultTestTaskDTO task : certCriteria.getTestTasks()) {
								if(task.getTaskParticipants() == null || task.getTaskParticipants().size() < 10) {
									product.getErrorMessages().add("A test task for certification " + certCriteria.getNumber() + " requires at least 10 participants.");
								}
								for(PendingCertificationResultTestTaskParticipantDTO part : task.getTaskParticipants()) {
									if(part.getTestParticipant().getEducationTypeId() == null) {
										product.getErrorMessages().add("Found no matching eduation level for test participant (gender: " + part.getTestParticipant().getGender() 
												+ ") related to " + certCriteria.getNumber() + ".");
									}
									if(part.getTestParticipant().getAgeRangeId() == null) {
										product.getErrorMessages().add("Found no matching age range for test participant (gender: " + part.getTestParticipant().getGender() 
												+ ") related to " + certCriteria.getNumber() + ".");
									}
								}
							}
						}
					}
				}
			}
		}
		if(needsG3) {
			boolean hasG3 = hasCert("170.315 (g)(3)", allMetCerts);
			if(!hasG3) {
				product.getErrorMessages().add("170.315 (g)(3) is required but was not found.");
			}
		}		
		
		//g3 inverse check
		boolean hasG3ComplimentaryCerts = false;
		for(int i = 0; i < ucdRequiredCerts.length; i++) {
			if(hasCert(ucdRequiredCerts[i], allMetCerts)) {
				hasG3ComplimentaryCerts = true;
			}
		}
		if(!hasG3ComplimentaryCerts) {
			//make sure it doesn't have g3
			boolean hasG3 = hasCert("170.315 (g)(3)", allMetCerts);
			if(hasG3) {
				product.getErrorMessages().add("170.315 (g)(3) is not allowed but was found.");
			}
		}
		
		//g4 check
		boolean hasG4 = hasCert("170.315 (g)(4)", allMetCerts);
		if(!hasG4) {
			product.getErrorMessages().add("170.315 (g)(4) is required but was not found.");
		}
		
		//g5 check
		boolean hasG5 = hasCert("170.315 (g)(5)", allMetCerts);
		if(!hasG5) {
			product.getErrorMessages().add("170.315 (g)(5) is required but was not found.");
		}
		
		//g6 checks
		boolean needsG6 = false;
		for(int i = 0; i < g6CertsToCheck.length && !needsG6; i++) {
			if(hasCert(g6CertsToCheck[i], allMetCerts)) {
				needsG6 = true;
			}
		}
		if(needsG6) {
			boolean hasG6 = hasCert("170.315 (g)(6)", allMetCerts);
			if(!hasG6) {
				product.getErrorMessages().add("170.315 (g)(6) is required but was not found.");
			}
		}
		
		//reverse g6 check
		boolean hasG6 = hasCert("170.315 (g)(6)", allMetCerts);
		if(hasG6) {
			boolean hasG6ComplimentaryCerts = false;
			for(int i = 0; i < g6CertsToCheck.length; i++) {
				if(hasCert(g6CertsToCheck[i], allMetCerts)) {
					hasG6ComplimentaryCerts = true;
				}
			}
			
			if(!hasG6ComplimentaryCerts) {
				product.getErrorMessages().add("170.315 (g)(6) was found but a related required cert was not found.");
			}
		}
				
		//TODO: detailed G6 check
		
		//h1 plus b1
		boolean hasH1 = hasCert("170.315 (h)(1)", allMetCerts);
		if(hasH1) {
			boolean hasB1 = hasCert("170.315 (b)(1)", allMetCerts);
			if(!hasB1) {
				product.getErrorMessages().add("170.315 (h)(1) was found so 170.315 (b)(1) is required but was not found.");
			}
		}
		
		// check that For 170.315(d)(3), GAP cannot = 1
		for(PendingCertificationResultDTO cert : product.getCertificationCriterion()) {
			if(cert.getNumber().equals("170.315 (d)(3)")) {
				if(cert.getGap() != null && cert.getGap().equals(Boolean.TRUE)) {
					product.getErrorMessages().add("170.315 (d)(3) cannot mark GAP as true.");
				}
			}
		}
	}

	protected void validateDemographics(PendingCertifiedProductDTO product) {
		super.validateDemographics(product);
		
		if(product.getQmsStandards() == null || product.getQmsStandards().size() == 0) {
			product.getErrorMessages().add("QMS Standards are required.");
		} else {
			for(PendingCertifiedProductQmsStandardDTO qms : product.getQmsStandards()) {
				if(StringUtils.isBlank(qms.getApplicableCriteria())) {
					product.getErrorMessages().add("Applicable criteria is required for each QMS Standard listed.");
				}
			}
		}
		
		if(product.getAccessibilityStandards() == null || product.getAccessibilityStandards().size() == 0) {
			product.getErrorMessages().add("Accessibility standards are required.");
		} //accessibility standards do not have to match the set list of standards. 
		
		for(PendingCertificationResultDTO cert : product.getCertificationCriterion()) {
			if(cert.getMeetsCriteria() != null && cert.getMeetsCriteria() == Boolean.TRUE) {
				boolean gapEligibleAndTrue = false;
				if(certRules.hasCertOption(cert.getNumber(), CertificationResultRules.GAP) &&
						cert.getGap() == Boolean.TRUE) {
					gapEligibleAndTrue = true;
				}
				
				if(certRules.hasCertOption(cert.getNumber(), CertificationResultRules.PRIVACY_SECURITY) &&
						StringUtils.isEmpty(cert.getPrivacySecurityFramework())) {
					product.getErrorMessages().add("Privacy and Security Framework is required for certification " + cert.getNumber() + ".");
				}
				if(certRules.hasCertOption(cert.getNumber(), CertificationResultRules.API_DOCUMENTATION) &&
						StringUtils.isEmpty(cert.getApiDocumentation())) {
					product.getErrorMessages().add("API Documentation is required for certification " + cert.getNumber() + ".");
				}
				//jennifer asked to not make functionality tested be a required field
//				if(certRules.hasCertOption(cert.getNumber(), CertificationResultRules.FUNCTIONALITY_TESTED) &&
//						(cert.getTestFunctionality() == null || cert.getTestFunctionality().size() == 0)) {
//					product.getErrorMessages().add("Functionality Tested is required for certification " + cert.getNumber() + ".");
//				}
				if(!gapEligibleAndTrue && 
						certRules.hasCertOption(cert.getNumber(), CertificationResultRules.TEST_TOOLS_USED) &&
						(cert.getTestTools() == null || cert.getTestTools().size() == 0)) {
						product.getErrorMessages().add("Test Tools are required for certification " + cert.getNumber() + ".");
				} else if(certRules.hasCertOption(cert.getNumber(), CertificationResultRules.TEST_TOOLS_USED) &&
						cert.getTestTools() != null && cert.getTestTools().size() > 0) {
					for(PendingCertificationResultTestToolDTO pendingToolMap : cert.getTestTools()) {
						if(pendingToolMap.getTestToolId() == null) {
							TestToolDTO foundTestTool = testToolDao.getByName(pendingToolMap.getName());
							if(foundTestTool == null || foundTestTool.getId() == null) {
								product.getErrorMessages().add("Certification " + cert.getNumber() + " contains an invalid test tool name: '" + pendingToolMap.getName() + "'.");
							} 
						}
					}
				}
				
				if(certRules.hasCertOption(cert.getNumber(), CertificationResultRules.FUNCTIONALITY_TESTED) &&
						cert.getTestFunctionality() != null && cert.getTestFunctionality().size() > 0) {
					for(PendingCertificationResultTestFunctionalityDTO pendingFuncMap : cert.getTestFunctionality()) {
						if(pendingFuncMap.getTestFunctionalityId() == null) {
							TestFunctionalityDTO foundTestFunc = testFuncDao.getByNumber(pendingFuncMap.getNumber());
							if(foundTestFunc == null || foundTestFunc.getId() == null) {
								product.getErrorMessages().add("Certification " + cert.getNumber() + " contains invalid test functionality: '" + pendingFuncMap.getNumber() + "'.");
							}
						}
					}
				}
			
				if(certRules.hasCertOption(cert.getNumber(), CertificationResultRules.G1_SUCCESS) &&
						cert.getG1MacraMeasures() != null && cert.getG1MacraMeasures().size() > 0) {
					for(PendingCertificationResultMacraMeasureDTO pendingMeasureMap : cert.getG1MacraMeasures()) {
						if(pendingMeasureMap.getMacraMeasureId() == null) {
							MacraMeasureDTO foundMeasure = macraDao.getByCriteriaNumberAndValue(cert.getNumber(), pendingMeasureMap.getEnteredValue());
							if(foundMeasure == null || foundMeasure.getId() == null) {
								product.getErrorMessages().add("Certification " + cert.getNumber() + " contains invalid G1 Macra Measure: '" + pendingMeasureMap.getEnteredValue() + "'.");
							} else {
								pendingMeasureMap.setMacraMeasure(foundMeasure);
							}
						}
					}
				}
				
				if(certRules.hasCertOption(cert.getNumber(), CertificationResultRules.G2_SUCCESS) &&
						cert.getG2MacraMeasures() != null && cert.getG2MacraMeasures().size() > 0) {
					for(PendingCertificationResultMacraMeasureDTO pendingMeasureMap : cert.getG2MacraMeasures()) {
						if(pendingMeasureMap.getMacraMeasureId() == null) {
							MacraMeasureDTO foundMeasure = macraDao.getByCriteriaNumberAndValue(cert.getNumber(), pendingMeasureMap.getEnteredValue());
							if(foundMeasure == null || foundMeasure.getId() == null) {
								product.getErrorMessages().add("Certification " + cert.getNumber() + " contains invalid G2 Macra Measure: '" + pendingMeasureMap.getEnteredValue() + "'.");
							} else {
								pendingMeasureMap.setMacraMeasure(foundMeasure);
							}
						}
					}
				}
				
				if(!gapEligibleAndTrue && 
					(cert.getNumber().equals("170.315 (g)(1)") || cert.getNumber().equals("170.315 (g)(2)")) &&
					(cert.getTestData() == null || cert.getTestData().size() == 0)) {
					product.getErrorMessages().add("Test Data is required for certification " + cert.getNumber() + ".");
				}
//				if(certRules.hasCertOption(cert.getNumber(), CertificationResultRules.SED)) {
//					if(cert.getUcdProcesses() == null || cert.getUcdProcesses().size() == 0) {
//						product.getErrorMessages().add("UCD Fields are required for certification " + cert.getNumber() + ".");
//					}
//					if(cert.getTestTasks() == null || cert.getTestTasks().size() == 0) {
//						product.getErrorMessages().add("Test tasks are required for certification " + cert.getNumber() + ".");
//					}
//				}	
			}
		}
		
		// Allow retired test tool only if CP ICS = true
		for(PendingCertificationResultDTO certResult : product.getCertificationCriterion()) {
			if(certResult.getTestTools() != null && certResult.getTestTools().size() > 0) {
				for(PendingCertificationResultTestToolDTO testTool : certResult.getTestTools()) {
					if(testTool.getTestToolId() == null) {
						product.getErrorMessages().add("There was no test tool found matching '" + testTool.getName() + "' for certification " + certResult.getNumber() + ".");
					} else {
						TestToolDTO tt = super.testToolDao.getById(testTool.getTestToolId());
						if(tt != null && tt.isRetired() && super.icsCode.equals("0")) {
							if(super.hasIcsConflict){
								product.getWarningMessages().add("Test Tool '" + testTool.getName() + "' can not be used for criteria '" + certResult.getNumber() 
								+ "', as it is a retired tool, and this Certified Product does not carry ICS.");
							}
							else {
								product.getErrorMessages().add("Test Tool '" + testTool.getName() + "' can not be used for criteria '" + certResult.getNumber() 
								+ "', as it is a retired tool, and this Certified Product does not carry ICS.");
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
		
		List<String> allMetCerts = new ArrayList<String>();
		for(CertificationResult certCriteria : product.getCertificationResults()) {
			if(certCriteria.isSuccess()) {
				allMetCerts.add(certCriteria.getNumber());
			}
		}
		
		List<String> errors = checkClassOfCriteriaForErrors("170.315 (a)", allMetCerts, Arrays.asList(aComplimentaryCerts));
		product.getErrorMessages().addAll(errors);
		
		errors = checkClassOfCriteriaForErrors("170.315 (b)", allMetCerts, Arrays.asList(bComplimentaryCerts));
		product.getErrorMessages().addAll(errors);
		
		errors = checkClassOfCriteriaForErrors("170.315 (c)", allMetCerts, Arrays.asList(cComplimentaryCerts));
		product.getErrorMessages().addAll(errors);

		errors = checkClassOfCriteriaForErrors("170.315 (f)", allMetCerts, Arrays.asList(fComplimentaryCerts));
		product.getErrorMessages().addAll(errors);
		
		errors = checkClassOfCriteriaForErrors("170.315 (h)", allMetCerts, Arrays.asList(hComplimentaryCerts));
		product.getErrorMessages().addAll(errors);
		
		errors = checkSpecificCriteriaForErrors("170.315 (e)(1)", allMetCerts, Arrays.asList(e1ComplimentaryCerts));
		product.getErrorMessages().addAll(errors);
		
        //check for (c)(1), (c)(2), (c)(3), (c)(4)
        boolean meetsC1Criterion = hasCert("170.315 (c)(1)", allMetCerts);
        boolean meetsC2Criterion = hasCert("170.315 (c)(2)", allMetCerts);
        boolean meetsC3Criterion = hasCert("170.315 (c)(3)", allMetCerts);
        boolean meetsC4Criterion = hasCert("170.315 (c)(4)", allMetCerts);
        boolean hasC1Cqm = false;
        boolean hasC2Cqm = false;
        boolean hasC3Cqm = false;
        boolean hasC4Cqm = false;

        for(CQMResultDetails cqm : product.getCqmResults()) {
            List<String> certifications = new ArrayList<String>();
            for(CQMResultCertification criteria : cqm.getCriteria()) {
                certifications.add(criteria.getCertificationNumber());
            }
            hasC1Cqm = hasC1Cqm || hasCert("170.315 (c)(1)", certifications);
            hasC2Cqm = hasC2Cqm || hasCert("170.315 (c)(2)", certifications);
            hasC3Cqm = hasC3Cqm || hasCert("170.315 (c)(3)", certifications);
            hasC4Cqm = hasC4Cqm || hasCert("170.315 (c)(4)", certifications);
        }
        if (meetsC1Criterion && !hasC1Cqm) {
            product.getErrorMessages().add("Certification criterion 170.315 (c)(1) was found but no matching Clinical Quality Measurement was found.");
        } else if (!meetsC1Criterion && hasC1Cqm) {
            product.getErrorMessages().add("A Clinical Quality Measurement was found under Certification criterion 170.315 (c)(1), but the product does not attest to that criterion.");
        }
        if (meetsC2Criterion && !hasC2Cqm) {
            product.getErrorMessages().add("Certification criterion 170.315 (c)(2) was found but no matching Clinical Quality Measurement was found.");
        } else if (!meetsC2Criterion && hasC2Cqm) {
            product.getErrorMessages().add("A Clinical Quality Measurement was found under Certification criterion 170.315 (c)(2), but the product does not attest to that criterion.");
        }
        if (meetsC3Criterion && !hasC3Cqm) {
            product.getErrorMessages().add("Certification criterion 170.315 (c)(3) was found but no matching Clinical Quality Measurement was found.");
        } else if (!meetsC3Criterion && hasC3Cqm) {
            product.getErrorMessages().add("A Clinical Quality Measurement was found under Certification criterion 170.315 (c)(3), but the product does not attest to that criterion.");
        }
        if (meetsC4Criterion && !hasC4Cqm) {
            product.getErrorMessages().add("Certification criterion 170.315 (c)(4) was found but no matching Clinical Quality Measurement was found.");
        } else if (!meetsC4Criterion && hasC4Cqm) {
            product.getErrorMessages().add("A Clinical Quality Measurement was found under Certification criterion 170.315 (c)(4), but the product does not attest to that criterion.");
        }

		//check for (e)(2) or (e)(3) certs
		boolean meetsE2Criterion = hasCert("170.315 (e)(2)", allMetCerts);
		boolean meetsE3Criterion = hasCert("170.315 (e)(3)", allMetCerts);
		if(meetsE2Criterion || meetsE3Criterion) {
			for(int i = 0; i < e2Ore3ComplimentaryCerts.length; i++) {
				boolean hasComplimentaryCert = false;
				for(CertificationResult certCriteria : product.getCertificationResults()) {
					if(certCriteria.getNumber().equals(e2Ore3ComplimentaryCerts[i]) && certCriteria.isSuccess()) {
						hasComplimentaryCert = true;
					}
				}
				
				if(!hasComplimentaryCert) {
					product.getErrorMessages().add("Certification criterion 170.315 (e)(2) or 170.315 (e)(3) was found so " + e2Ore3ComplimentaryCerts[i] + " is required but was not found.");

				}
			}
		}
		
		//check for (g)(7) or (g)(8) or (g)(9) certs
		boolean meetsG7Criterion = hasCert("170.315 (g)(7)", allMetCerts);;
		boolean meetsG8Criterion = hasCert("170.315 (g)(8)", allMetCerts);;
		boolean meetsG9Criterion = hasCert("170.315 (g)(9)", allMetCerts);;
	
		if(meetsG7Criterion || meetsG8Criterion || meetsG9Criterion) {
			for(int i = 0; i < g7Org8Org9ComplimentaryCerts.length; i++) {
				boolean hasComplimentaryCert = false;
				for(CertificationResult certCriteria : product.getCertificationResults()) {
					if(certCriteria.getNumber().equals(g7Org8Org9ComplimentaryCerts[i]) && certCriteria.isSuccess()) {
						hasComplimentaryCert = true;
					}
				}
				
				if(!hasComplimentaryCert) {
					product.getErrorMessages().add("Certification criterion 170.315 (g)(7) or 170.315 (g)(8) or 170.315 (g)(9) was found so " + g7Org8Org9ComplimentaryCerts[i] + " is required but was not found.");

				}
			}
			
			boolean meetsD2Criterion = hasCert("170.315 (d)(2)", allMetCerts);
			boolean meetsD10Criterion = hasCert("170.315 (d)(10)", allMetCerts);;
			if(!meetsD2Criterion && !meetsD10Criterion) {
				product.getErrorMessages().add("Certification criterion 170.315 (g)(7) or 170.315 (g)(8) or 170.315 (g)(9) was found so 170.315 (d)(2) or 170.315 (d)(10) is required.");
			}
		}
		
		//g3 checks
		boolean needsG3 = false;
		for(int i = 0; i < ucdRequiredCerts.length && !needsG3; i++) {
			if(hasCert(ucdRequiredCerts[i], allMetCerts)) {
				needsG3 = true;
				
				//check for full set of UCD data
				for(CertificationResult certCriteria : product.getCertificationResults()) {
					if(certCriteria.getNumber().equals(ucdRequiredCerts[i])) {
						if(certCriteria.getUcdProcesses() == null || certCriteria.getUcdProcesses().size() == 0) {
							product.getErrorMessages().add("Certification " + certCriteria.getNumber() + " requires at least one UCD process.");
						}
						if(certCriteria.getTestTasks() == null || certCriteria.getTestTasks().size() == 0) {
							product.getErrorMessages().add("Certification " + certCriteria.getNumber() + " requires at least one test task.");
						} else {
							for(CertificationResultTestTask task : certCriteria.getTestTasks()) {
								if(task.getTestParticipants() == null || task.getTestParticipants().size() < 10) {
									product.getWarningMessages().add("A test task for certification " + certCriteria.getNumber() + " requires at least 10 participants.");
								}
							}
						}
					}
				}
			}
		}
		if(needsG3) {
			boolean hasG3 = hasCert("170.315 (g)(3)", allMetCerts);
			if(!hasG3) {
				product.getErrorMessages().add("170.315 (g)(3) is required but was not found.");
			}
		}
				
		//g3 inverse check
		boolean hasG3ComplimentaryCerts = false;
		for(int i = 0; i < ucdRequiredCerts.length; i++) {
			if(hasCert(ucdRequiredCerts[i], allMetCerts)) {
				hasG3ComplimentaryCerts = true;
			}
		}
		if(!hasG3ComplimentaryCerts) {
			//make sure it doesn't have g3
			boolean hasG3 = hasCert("170.315 (g)(3)", allMetCerts);
			if(hasG3) {
				product.getErrorMessages().add("170.315 (g)(3) is not allowed but was found.");
			}
		}
		
		//g4 check
		boolean hasG4 = hasCert("170.315 (g)(4)", allMetCerts);
		if(!hasG4) {
			product.getErrorMessages().add("170.315 (g)(4) is required but was not found.");
		}
		
		//g5 check
		boolean hasG5 = hasCert("170.315 (g)(5)", allMetCerts);
		if(!hasG5) {
			product.getErrorMessages().add("170.315 (g)(5) is required but was not found.");
		}
		
		//g6 checks
		boolean needsG6 = false;
		for(int i = 0; i < g6CertsToCheck.length && !needsG6; i++) {
			if(hasCert(g6CertsToCheck[i], allMetCerts)) {
				needsG6 = true;
			}
		}
		if(needsG6) {
			boolean hasG6 = hasCert("170.315 (g)(6)", allMetCerts);
			if(!hasG6) {
				product.getErrorMessages().add("170.315 (g)(6) is required but was not found.");
			}
		}
		
		//TODO: detailed G6 check
		
		//h1 plus b1
		boolean hasH1 = hasCert("170.315 (h)(1)", allMetCerts);
		if(hasH1) {
			boolean hasB1 = hasCert("170.315 (b)(1)", allMetCerts);
			if(!hasB1) {
				product.getErrorMessages().add("170.315 (h)(1) was found so 170.315 (b)(1) is required but was not found.");
			}
		}
		
		// check that For 170.315(d)(3), GAP cannot = 1
		for(CertificationResult cert : product.getCertificationResults()) {
			if(cert.getNumber().equals("170.315 (d)(3)")) {
				if(cert.isGap() != null && cert.isGap().equals(Boolean.TRUE)) {
					product.getErrorMessages().add("170.315 (d)(3) cannot mark GAP as true.");
				}
			}
		}
	}

	protected void validateDemographics(CertifiedProductSearchDetails product) {
		super.validateDemographics(product);
		
		if(product.getIcs() == null) {
			product.getErrorMessages().add("ICS is required.");
		}
		
		if(product.getQmsStandards() == null || product.getQmsStandards().size() == 0) {
			product.getErrorMessages().add("QMS Standards are required.");
		} else {
			for(CertifiedProductQmsStandard qms : product.getQmsStandards()) {
				if(StringUtils.isBlank(qms.getApplicableCriteria())) {
					product.getErrorMessages().add("Applicable criteria is required for each QMS Standard listed.");
				}
			}
		}
		
		if(product.getAccessibilityStandards() == null || product.getAccessibilityStandards().size() == 0) {
			product.getErrorMessages().add("Accessibility standards are required.");
		}
		
		//have to get the old product to compare previous and current test tools
		CertifiedProductSearchDetails oldProduct = null;
		try {
				oldProduct = cpdManager.getCertifiedProductDetails(product.getId());
		} catch(EntityRetrievalException ex) {
			logger.error("Could not find certified product details with id " + product.getId(), ex);
		}
		
		//we do have to check for retired test tools here because users are not allowed to:
		//1. remove a retired test tool that was previously used, or 
		//2. add a retired test tool that was not previously used, or
		//3. change the test tool version of a retired test tool
		for(CertificationResult oldCert : oldProduct.getCertificationResults()) {
			if(oldCert.isSuccess() != null && oldCert.isSuccess() == Boolean.TRUE && oldCert.getTestToolsUsed() != null) {
				for(CertificationResultTestTool oldTestTool : oldCert.getTestToolsUsed()) {
					TestToolDTO testTool = testToolDao.getById(oldTestTool.getTestToolId());
					if(testTool.isRetired()) {
						boolean certStillExists = false;
						boolean certHasRetiredTool = false;
						//make sure this test tool still exists in the passed in product/cert
						//because users are not allowed to remove existing test tools if they are retired
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
		
		// Allow retired test tool only if CP ICS = true
		for(CertificationResult certResult : product.getCertificationResults()) {
			if(certResult.getTestToolsUsed() != null && certResult.getTestToolsUsed().size() > 0) {
				for(CertificationResultTestTool testTool : certResult.getTestToolsUsed()) {
					if(testTool.getTestToolId() == null) {
						product.getErrorMessages().add("There was no test tool found matching '" + testTool.getTestToolName() + "' for certification " + certResult.getNumber() + ".");
					} else {
						TestToolDTO tt = super.testToolDao.getById(testTool.getTestToolId());
						if(tt != null && tt.isRetired() && super.icsCode.equals("0")) {
							if(super.hasIcsConflict){
								product.getWarningMessages().add("Test Tool '" + testTool.getTestToolName() + "' can not be used for criteria '" + certResult.getNumber() 
								+ "', as it is a retired tool, and this Certified Product does not carry ICS.");
							}
							else {
								product.getErrorMessages().add("Test Tool '" + testTool.getTestToolName() + "' can not be used for criteria '" + certResult.getNumber() 
								+ "', as it is a retired tool, and this Certified Product does not carry ICS.");
							}
						}
					}
				}
			}
		}
		
		//now check all the new certs for whatever is required
		for(CertificationResult cert : product.getCertificationResults()) {
			if(cert.isSuccess() != null && cert.isSuccess() == Boolean.TRUE) {
				boolean gapEligibleAndTrue = false;
				if(certRules.hasCertOption(cert.getNumber(), CertificationResultRules.GAP) &&
						cert.isGap() == Boolean.TRUE) {
					gapEligibleAndTrue = true;
				}
				
				if(certRules.hasCertOption(cert.getNumber(), CertificationResultRules.PRIVACY_SECURITY) &&
						StringUtils.isEmpty(cert.getPrivacySecurityFramework())) {
					product.getErrorMessages().add("Privacy and Security Framework is required for certification " + cert.getNumber() + ".");
				}
				if(certRules.hasCertOption(cert.getNumber(), CertificationResultRules.API_DOCUMENTATION) &&
						StringUtils.isEmpty(cert.getApiDocumentation())) {
					product.getErrorMessages().add("API Documentation is required for certification " + cert.getNumber() + ".");
				}
				
				if(!gapEligibleAndTrue && 
						certRules.hasCertOption(cert.getNumber(), CertificationResultRules.TEST_TOOLS_USED) &&
						(cert.getTestToolsUsed() == null || cert.getTestToolsUsed().size() == 0)) {
						product.getErrorMessages().add("Test Tools are required for certification " + cert.getNumber() + ".");
				} else if(certRules.hasCertOption(cert.getNumber(), CertificationResultRules.TEST_TOOLS_USED) &&
						cert.getTestToolsUsed() != null && cert.getTestToolsUsed().size() > 0) {
					for(CertificationResultTestTool toolMap : cert.getTestToolsUsed()) {
						if(toolMap.getTestToolId() == null) {
							TestToolDTO foundTestTool = testToolDao.getByName(toolMap.getTestToolName());
							if(foundTestTool == null || foundTestTool.getId() == null) {
								product.getErrorMessages().add("Certification " + cert.getNumber() + " contains an invalid test tool name: '" + toolMap.getTestToolName() + "'.");
							} 
						}
					}
				}
				
				if(certRules.hasCertOption(cert.getNumber(), CertificationResultRules.FUNCTIONALITY_TESTED) &&
						cert.getTestFunctionality() != null && cert.getTestFunctionality().size() > 0) {
					for(CertificationResultTestFunctionality funcMap : cert.getTestFunctionality()) {
						if(funcMap.getTestFunctionalityId() == null) {
							TestFunctionalityDTO foundTestFunc = testFuncDao.getByNumber(funcMap.getName());
							if(foundTestFunc == null || foundTestFunc.getId() == null) {
								product.getErrorMessages().add("Certification " + cert.getNumber() + " contains invalid test functionality: '" + funcMap.getName() + "'.");
							}
						}
					}
				}
			
				if(certRules.hasCertOption(cert.getNumber(), CertificationResultRules.G1_SUCCESS) &&
						cert.getG1MacraMeasures() != null && cert.getG1MacraMeasures().size() > 0) {
					for(CertificationResultMacraMeasure measureMap : cert.getG1MacraMeasures()) {
						if(measureMap.getMeasure() == null || measureMap.getMeasure().getId() == null) {
							product.getErrorMessages().add("Certification " + cert.getNumber() + " contains invalid G1 Macra Measure.");
						} else {
							//confirm the measure id is valid
							MacraMeasureDTO foundMeasure = macraDao.getById(measureMap.getMeasure().getId());
							if(foundMeasure == null || foundMeasure.getId() == null) {
								product.getErrorMessages().add("Certification " + cert.getNumber() + " contains invalid G1 Macra Measure. No measure found with ID '" + measureMap.getMeasure().getId() + "'.");
							} else if(!foundMeasure.getCriteria().getNumber().equals(cert.getNumber())) {
								product.getErrorMessages().add("Certification " + cert.getNumber() + " contains an invalid G1 Macra Measure. Measure with ID '" + 
										measureMap.getMeasure().getId() + "' is the measure '" + foundMeasure.getName() + 
										"' and is for criteria '" + foundMeasure.getCriteria().getNumber() + "'.");
							} else {								
								measureMap.setMeasure(new MacraMeasure(foundMeasure));
							}
						}
					}
				}
				
				if(certRules.hasCertOption(cert.getNumber(), CertificationResultRules.G2_SUCCESS) &&
						cert.getG2MacraMeasures() != null && cert.getG2MacraMeasures().size() > 0) {
					for(CertificationResultMacraMeasure measureMap : cert.getG2MacraMeasures()) {
						if(measureMap.getMeasure() == null || measureMap.getMeasure().getId() == null) {
							product.getErrorMessages().add("Certification " + cert.getNumber() + " contains invalid G2 Macra Measure.");
						} else {
							//confirm the measure id is valid
							MacraMeasureDTO foundMeasure = macraDao.getById(measureMap.getMeasure().getId());
							if(foundMeasure == null || foundMeasure.getId() == null) {
								product.getErrorMessages().add("Certification " + cert.getNumber() + " contains invalid G2 Macra Measure. No measure found with ID '" + measureMap.getMeasure().getId() + "'.");
							} else if(!foundMeasure.getCriteria().getNumber().equals(cert.getNumber())) {
								product.getErrorMessages().add("Certification " + cert.getNumber() + " contains an invalid G2 Macra Measure. Measure with ID '" + 
										measureMap.getMeasure().getId() + "' is the measure '" + foundMeasure.getName() + 
										"' and is for criteria '" + foundMeasure.getCriteria().getNumber() + "'.");
							} else {								
								measureMap.setMeasure(new MacraMeasure(foundMeasure));
							}
						}
					}
				}
				
				if(!gapEligibleAndTrue && 
					(cert.getNumber().equals("170.315 (g)(1)") || cert.getNumber().equals("170.315 (g)(2)")) &&
					(cert.getTestDataUsed() == null || cert.getTestDataUsed().size() == 0)) {
					product.getErrorMessages().add("Test Data is required for certification " + cert.getNumber() + ".");
				}
			}
		}
	}
	
	private boolean hasCert(String toCheck, List<String> allCerts) {
		boolean hasCert = false;
		for(int i = 0; i < allCerts.size() && !hasCert; i++) {
			if(allCerts.get(i).equals(toCheck)) {
				hasCert = true;
			}
		}
		return hasCert;
	}
	
	/**
	 * look for required complimentary certs when one of the criteria met is a certain
	 * class of cert... such as 170.315 (a)(*)
	 * @param criterionNumberStart
	 * @param allCriteriaMet
	 * @param complimentaryCertNumbers
	 * @return
	 */
	private List<String> checkClassOfCriteriaForErrors(String criterionNumberStart, List<String> allCriteriaMet, List<String> complimentaryCertNumbers) {
		List<String> errors = new ArrayList<String>();
		boolean hasCriterion = false;
		for(String currCriteria : allCriteriaMet) {
			if(currCriteria.startsWith(criterionNumberStart)) {
				hasCriterion = true;
			}
		}	
		if(hasCriterion) {
			for(String currRequiredCriteria : complimentaryCertNumbers) {
				boolean hasComplimentaryCert = false;
				for(String certCriteria : complimentaryCertNumbers) {
					if(certCriteria.equals(currRequiredCriteria)) {
						hasComplimentaryCert = true;
					}
				}
				
				if(!hasComplimentaryCert) {
					errors.add("Certification criterion " + criterionNumberStart + "(*) was found "
							+ "so " + currRequiredCriteria + " is required but was not found.");
				}
			}
		}
		return errors;
	}
	
	/**
	 * Look for a required complimentary criteria when a specific criteria has been met
	 * @param criterionNumber
	 * @param allCriteriaMet
	 * @param complimentaryCertNumbers
	 * @return
	 */
	private List<String> checkSpecificCriteriaForErrors(String criterionNumber, List<String> allCriteriaMet, List<String> complimentaryCertNumbers) {
		List<String> errors = new ArrayList<String>();
		boolean hasCriterion = false;
		for(String currCriteria : allCriteriaMet) {
			if(currCriteria.equals(criterionNumber)) {
				hasCriterion = true;
			}
		}	
		if(hasCriterion) {
			for(String currRequiredCriteria : complimentaryCertNumbers) {
				boolean hasComplimentaryCert = false;
				for(String certCriteria : complimentaryCertNumbers) {
					if(certCriteria.equals(currRequiredCriteria)) {
						hasComplimentaryCert = true;
					}
				}
				
				if(!hasComplimentaryCert) {
					errors.add("Certification criterion " + criterionNumber + "(*) was found "
							+ "so " + currRequiredCriteria + " is required but was not found.");
				}
			}
		}
		return errors;
	}
}

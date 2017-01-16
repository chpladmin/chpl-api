package gov.healthit.chpl.web.controller;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;

import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.caching.UnitTestRules;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.CQMResultCertification;
import gov.healthit.chpl.domain.CQMResultDetails;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { gov.healthit.chpl.CHPLTestConfig.class })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class })
@DatabaseSetup("classpath:data/testData.xml") 
public class CertifiedProductControllerTest {
	private static final Logger logger = LogManager.getLogger(CertifiedProductControllerTest.class);
	@Rule
    @Autowired
    public UnitTestRules cacheInvalidationRule;
	
	@Autowired
	CertifiedProductController certifiedProductController;
	
	private static JWTAuthenticatedUser adminUser;
	
	@BeforeClass
	public static void setUpClass() throws Exception {
		adminUser = new JWTAuthenticatedUser();
		adminUser.setFirstName("Administrator");
		adminUser.setId(-2L);
		adminUser.setLastName("Administrator");
		adminUser.setSubjectName("admin");
		adminUser.getPermissions().add(new GrantedPermission("ROLE_ADMIN"));
	}
	
	/** 
	 * Given that a user with ROLE_ADMIN edits/updates an existing Certified Product
	 * When the UI calls the API at /certified_products/update
	 * When the user tries to update a Certified Product with ics = true and retired test tool = true and set ics = false
	 * Then the API returns an error
	 * @throws IOException 
	 * @throws JSONException 
	 */
	@Transactional
	@Rollback(true)
	@Test
	public void test_updateCertifiedProduct_icsAndRetiredEqualTrue_icsFalseReturnsError() throws EntityRetrievalException, EntityCreationException, IOException {
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		
		CertifiedProductSearchDetails updateRequest = new CertifiedProductSearchDetails();
		updateRequest.setCertificationDate(1440090840000L);
		updateRequest.setId(1L); // Certified_product_id = 1 has icsCode = true and is associated with TestTool with id=2 & id = 3 that have retired = true
		updateRequest.setIcs(false);
		updateRequest.setChplProductNumber("CHP-024050");
		Map<String, Object> certStatus = new HashMap<String, Object>();
		certStatus.put("name", "Active");
		updateRequest.setCertificationStatus(certStatus);
		Map<String, Object> certificationEdition = new HashMap<String, Object>();
		String certEdition = "2015";
		certificationEdition.put("name", certEdition);
		updateRequest.setCertificationEdition(certificationEdition);
		List<CertificationResult> certificationResults = new ArrayList<CertificationResult>();
		CertificationResult cr = new CertificationResult();
		cr.setAdditionalSoftware(null);
		cr.setApiDocumentation(null);
		cr.setG1Success(false);
		cr.setG2Success(false);
		cr.setGap(null);
		cr.setNumber("170.314 (b)(6)");
		cr.setPrivacySecurityFramework(null);
		cr.setSed(null);
		cr.setSuccess(false);
		cr.setTestDataUsed(null);
		cr.setTestFunctionality(null);
		cr.setTestProcedures(null);
		cr.setTestStandards(null);
		cr.setTestTasks(null);
		cr.setTestToolsUsed(null);
		cr.setTitle("Inpatient setting only - transmission of electronic laboratory tests and values/results to ambulatory providers");
		cr.setUcdProcesses(null);
		certificationResults.add(cr);
		updateRequest.setCertificationResults(certificationResults);
		List<CQMResultDetails> cqms = new ArrayList<CQMResultDetails>();
		CQMResultDetails cqm = new CQMResultDetails();
		Set<String> versions = new HashSet<String>();
		versions.add("v0");
		versions.add("v1");
		versions.add("v2");
		versions.add("v3");
		versions.add("v4");
		versions.add("v5");
		cqm.setAllVersions(versions);
		cqm.setCmsId("CMS60");
		List<CQMResultCertification> cqmResultCertifications = new ArrayList<CQMResultCertification>();
		cqm.setCriteria(cqmResultCertifications);
		cqm.setDescription("Acute myocardial infarction (AMI) patients with ST-segment elevation on the ECG closest to arrival time receiving "
				+ "fibrinolytic therapy during the hospital visit"); 
		cqm.setDomain(null);
		cqm.setId(0L);
		cqm.setNqfNumber("0164");
		cqm.setNumber(null);
		cqm.setSuccess(true);
		Set<String> successVersions = new HashSet<String>();
		successVersions.add("v2");
		successVersions.add("v3");
		cqm.setSuccessVersions(successVersions);
		cqm.setTitle("Fibrinolytic Therapy Received Within 30 Minutes of Hospital Arrival");
		cqm.setTypeId(2L);
		cqms.add(cqm);
		updateRequest.setCqmResults(cqms);
		try {
			certifiedProductController.updateCertifiedProduct(updateRequest);
		} catch (InvalidArgumentsException e) {
			e.printStackTrace();
		} catch (ValidationException e) {
			assertNotNull(e);
			assertTrue(e.getErrorMessages().contains("Cannot set ICS to false for a Certified Product "
					+ "with ICS=true and attested criteria that have a retired Test Tool. The following "
					+ "are attested criteria that have a retired Test Tool: [170.314 (a)(1), 170.314 (a)(1)]"));
		}
		
	}
	
	/** 
	 * Given that a user with ROLE_ADMIN selects a Certified Product to view on the CHPL
	 * When the UI calls the API at /certified_products/{certifiedProductId}/details
	 * Then the API returns the Certified Product Details
	 * @throws IOException 
	 * @throws JSONException 
	 */
	@Transactional
	@Test
	public void test_getCertifiedProductById() throws EntityRetrievalException, EntityCreationException, IOException {
		Long cpId = 1L;
		CertifiedProductSearchDetails cpDetails = certifiedProductController.getCertifiedProductById(1L);
		assertNotNull(cpDetails);
	}
	
}

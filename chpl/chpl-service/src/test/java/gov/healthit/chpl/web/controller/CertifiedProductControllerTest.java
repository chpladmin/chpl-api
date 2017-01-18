package gov.healthit.chpl.web.controller;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

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
import gov.healthit.chpl.domain.CertificationResultTestTool;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.MeaningfulUseUser;
import gov.healthit.chpl.dto.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.PendingCertificationResultTestToolDTO;
import gov.healthit.chpl.dto.PendingCertifiedProductDTO;
import gov.healthit.chpl.dto.PendingCqmCriterionDTO;
import gov.healthit.chpl.validation.certifiedProduct.CertifiedProductValidator;
import gov.healthit.chpl.validation.certifiedProduct.CertifiedProductValidatorFactory;
import gov.healthit.chpl.web.controller.results.MeaningfulUseUserResults;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { gov.healthit.chpl.CHPLTestConfig.class })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class })
@DatabaseSetup("classpath:data/testData.xml") 
public class CertifiedProductControllerTest {

	private static final String CSV_SEPARATOR = ",";
	private static final Logger logger = LogManager.getLogger(CertifiedProductControllerTest.class);
	@Rule
    @Autowired
    public UnitTestRules cacheInvalidationRule;
	
	@Autowired
	CertifiedProductController certifiedProductController;
	
	@Autowired
	CertifiedProductValidatorFactory validatorFactory;
	
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
	 * Given that a user with ROLE_ONC_STAFF or ROLE_ADMIN has uploaded a CSV with meaningfulUseUser counts (passed in as MultipartFile file)
	 * When the UI calls the API at /uploadMeaningfulUse
	 * When the CSV contains incorrectly named headers
	 * When the CSV contains some incorrect CHPLProductNumbers
	 * When the CSV contains 2014 & 2015 edition CHPL Product Numbers
	 * When the CSV contains a row with leading and trailing spaces for CHPL Product Number
	 * When the CSV contains a duplicate CHPL Product Number
	 * Then the API parses the contents of the csv
	 * Then the API updates the meaningfulUseUser count for each CHPL Product Number/Certified Product
	 * Then the API returns MeaningfulUseUserResults to the UI as a JSON response
	 * Then the MeaningfulUseUserResults contains an array with unsuccessful updates
	 * Then the API trims leading and trailing spaces and successfully updates meaningfulUseUsers for the CHPLProductNumber
	 * Then the duplicate CHPL Product Number results in an error added to the results errors array
	 * Then the certifiedProductId is updated for non-error results
	 * @throws IOException 
	 * @throws JSONException 
	 */
	@Transactional
	@Rollback(true)
	@Test
	public void test_uploadMeaningfulUseUsers_returnsMeaningfulUseUserResults() throws EntityRetrievalException, EntityCreationException, IOException {
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		logger.info("Running test_uploadMeaningfulUseUsers_returnsMeaningfulUseUserResults");
		
		// Create CSV input for API
		MeaningfulUseUser meaningfulUseUser1 = new MeaningfulUseUser("CHP-024050", 10L); // MeaningfulUseUser 0
		MeaningfulUseUser meaningfulUseUser2 = new MeaningfulUseUser("CHP-024051", 20L); // MeaningfulUseUser 1
		MeaningfulUseUser meaningfulUseUser3 = new MeaningfulUseUser(" CHP-024052 ", 30L); // MeaningfulUseUser 2
		MeaningfulUseUser meaningfulUseUser4 = new MeaningfulUseUser(" 15.01.01.1009.EIC08.36.1.1.160402 ", 40L); // MeaningfulUseUser 3
		MeaningfulUseUser meaningfulUseUser5 = new MeaningfulUseUser("wrongChplProductNumber", 50L); // Errors 0
		MeaningfulUseUser meaningfulUseUser6 = new MeaningfulUseUser(" CHPL-024053 ", 60L); // Errors 1
		MeaningfulUseUser meaningfulUseUser7 = new MeaningfulUseUser("15.02.03.9876.AB01.01.0.1.123456", 70L); // Errors 2
		MeaningfulUseUser meaningfulUseUser8 = new MeaningfulUseUser("15.01.01.1009.EIC08.36.1.1.160402", 70L); // Errors 3 (because duplicate of MeaningfulUseUser 3
		logger.info("Created 8 of MeaningfulUseUser to be updated in the database");
		
		List<MeaningfulUseUser> meaningfulUseUserList = new ArrayList<MeaningfulUseUser>();
		meaningfulUseUserList.add(meaningfulUseUser1);
		meaningfulUseUserList.add(meaningfulUseUser2);
		meaningfulUseUserList.add(meaningfulUseUser3);
		meaningfulUseUserList.add(meaningfulUseUser4);
		meaningfulUseUserList.add(meaningfulUseUser5);
		meaningfulUseUserList.add(meaningfulUseUser6);
		meaningfulUseUserList.add(meaningfulUseUser7);
		meaningfulUseUserList.add(meaningfulUseUser8);
		logger.info("Populated List of MeaningfulUseUser from 8 MeaningfulUseUser");
		
		File file = new File("testMeaningfulUseUsers.csv");
		
		try
        {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file.getName()), "UTF-8"));
            StringBuffer headerLine = new StringBuffer();
            headerLine.append("cipl_product_number");
            headerLine.append(CSV_SEPARATOR);
            headerLine.append("n_meaningful_use");
            bw.write(headerLine.toString());
            bw.newLine();
            for (MeaningfulUseUser muUser : meaningfulUseUserList)
            {
                StringBuffer oneLine = new StringBuffer();
                oneLine.append(muUser.getProductNumber() != null ? muUser.getProductNumber() : "");
                oneLine.append(CSV_SEPARATOR);
                oneLine.append(muUser.getNumberOfUsers() != null ? muUser.getNumberOfUsers() : "");
                bw.write(oneLine.toString());
                bw.newLine();
            }
            bw.flush();
            bw.close();
        }
        catch (UnsupportedEncodingException e) {}
        catch (FileNotFoundException e){}
        catch (IOException e){}
		logger.info("Wrote meaningfulUseUserList to testMeaningfulUseUsers.csv");
	
		FileInputStream input = new FileInputStream(file);
		logger.info("Create FileInputStream from csv file to populate MultipartFile");
		MultipartFile multipartFile = new MockMultipartFile("file", file.getName(), "text/csv", IOUtils.toByteArray(input));
		logger.info("Create & populate MultipartFile from csv file");
		
		MeaningfulUseUserResults apiResult = new MeaningfulUseUserResults();
		try {
			apiResult = certifiedProductController.uploadMeaningfulUseUsers(multipartFile);
		} catch (MaxUploadSizeExceededException e) {
			e.printStackTrace();
		} catch (ValidationException e) {
			e.printStackTrace();
		}
		
		input.close();
		file.delete();
		
		assertTrue("MeaningfulUseUserResults should return 4 but returned " + apiResult.getMeaningfulUseUsers().size(), apiResult.getMeaningfulUseUsers().size() == 4);
		assertTrue("Errors should return 4 but returned " + apiResult.getErrors().size(), apiResult.getErrors().size() == 4);
		assertTrue("MeaningfulUseUserResults returned " + apiResult.getMeaningfulUseUsers().get(0).getProductNumber() + " but should return CHP-024050", 
				apiResult.getMeaningfulUseUsers().get(0).getProductNumber().equals("CHP-024050"));
		assertTrue("MeaningfulUseUserResults returned " + apiResult.getMeaningfulUseUsers().get(0).getNumberOfUsers() + " but should return " + 10L, 
				apiResult.getMeaningfulUseUsers().get(0).getNumberOfUsers().equals(10L));
		assertTrue("MeaningfulUseUserResults returned " + apiResult.getMeaningfulUseUsers().get(0).getCertifiedProductId() + " but should return certifiedProductId 1", 
				apiResult.getMeaningfulUseUsers().get(0).getCertifiedProductId() == 1);
		assertTrue("MeaningfulUseUserResults should return correct product number", apiResult.getMeaningfulUseUsers().get(1).getProductNumber().equals("CHP-024051"));
		assertTrue("MeaningfulUseUserResults should return correct numMeaningfulUseUsers", apiResult.getMeaningfulUseUsers().get(1).getNumberOfUsers().equals(20L));
		assertTrue("MeaningfulUseUserResults should return correct product number", apiResult.getMeaningfulUseUsers().get(2).getProductNumber().equals("CHP-024052"));
		assertTrue("MeaningfulUseUserResults should return correct numMeaningfulUseUsers", apiResult.getMeaningfulUseUsers().get(2).getNumberOfUsers().equals(30L));
		assertTrue("MeaningfulUseUserResults should return correct product number", apiResult.getMeaningfulUseUsers().get(3).getProductNumber().equals("15.01.01.1009.EIC08.36.1.1.160402"));
		assertTrue("MeaningfulUseUserResults should return correct numMeaningfulUseUsers", apiResult.getMeaningfulUseUsers().get(3).getNumberOfUsers().equals(40L));
		assertTrue("MeaningfulUseUserResults returned " + apiResult.getMeaningfulUseUsers().get(3).getProductNumber() + " but should return 15.01.01.1009.EIC08.36.1.1.160402", 
				apiResult.getMeaningfulUseUsers().get(3).getProductNumber().equals("15.01.01.1009.EIC08.36.1.1.160402"));
		assertTrue("MeaningfulUseUserResults returned " + apiResult.getMeaningfulUseUsers().get(3).getNumberOfUsers() + " but should return " + 40L, 
				apiResult.getMeaningfulUseUsers().get(3).getNumberOfUsers().equals(40L));
		assertTrue("MeaningfulUseUserResults errors array should return incorrect CHPL Product Number for row with {wrongChplProductNumber, 50L} but returned " 
		+ apiResult.getErrors().get(0).getError(), apiResult.getErrors().get(0).getError() != null);
		assertTrue("MeaningfulUseUserResults errors array should return incorrect CHPL Product Number for row with {CHPL-024053, 60L} but returned " 
				+ apiResult.getErrors().get(1).getError(), apiResult.getErrors().get(1).getError() != null);
		assertTrue("MeaningfulUseUserResults errors array for row {CHPL-024053, 60L} should have Product Number CHPL-024053 but has " 
				+ apiResult.getErrors().get(1).getProductNumber(), apiResult.getErrors().get(1).getProductNumber().equals("CHPL-024053"));
		assertTrue("MeaningfulUseUserResults errors array for row {CHPL-024053, 60L} should have num_meaningful_use 60L but has " 
				+ apiResult.getErrors().get(1).getNumberOfUsers(), apiResult.getErrors().get(1).getNumberOfUsers() == 60L);
		assertTrue("MeaningfulUseUserResults errors array should return incorrect CHPL Product Number for row with {15.02.03.9876.AB01.01.0.1.123456, 70L} but returned " 
				+ apiResult.getErrors().get(2).getError(), apiResult.getErrors().get(2).getError() != null);
		assertTrue("MeaningfulUseUserResults errors array for row {15.02.03.9876.AB01.01.0.1.123456, 70L} should have Product Number 15.02.03.9876.AB01.01.0.1.123456 but has " 
				+ apiResult.getErrors().get(2).getProductNumber(), apiResult.getErrors().get(2).getProductNumber().equals("15.02.03.9876.AB01.01.0.1.123456"));
		assertTrue("MeaningfulUseUserResults errors array for row {15.02.03.9876.AB01.01.0.1.123456, 70L} should have num_meaningful_use 70L but has " 
				+ apiResult.getErrors().get(2).getNumberOfUsers(), apiResult.getErrors().get(2).getNumberOfUsers() == 70L);
		assertTrue("MeaningfulUseUserResults errors array should return incorrect CHPL Product Number for row with {15.01.01.1009.EIC08.36.1.1.160402, 70L} but returned " 
				+ apiResult.getErrors().get(3).getError(), apiResult.getErrors().get(3).getError() != null);
		assertTrue("MeaningfulUseUserResults errors array for row {12.01.01.1234.AB01.01.0.1.123456, 70L} should have Product Number 15.01.01.1009.EIC08.36.1.1.160402 but has " 
				+ apiResult.getErrors().get(3).getProductNumber(), apiResult.getErrors().get(3).getProductNumber().equals("15.01.01.1009.EIC08.36.1.1.160402"));
		assertTrue("MeaningfulUseUserResults errors array for row {15.01.01.1009.EIC08.36.1.1.160402, 70L} should have num_meaningful_use 70L but has " 
				+ apiResult.getErrors().get(3).getNumberOfUsers(), apiResult.getErrors().get(3).getNumberOfUsers() == 70L);
	}
	
	/** 
	 * Given that a user with ROLE_ONC_STAFF or ROLE_ADMIN has uploaded a CSV with meaningfulUseUser counts (passed in as MultipartFile file)
	 * When the UI calls the API at /uploadMeaningfulUse
	 * When the CSV contains no header
	 * When the CSV contains a 2014 CHPL Product Number with a non-legacy format
	 * Then the API continues with updating the CHPL Product Numbers with their respective num_meaningful_users
	 * @throws IOException 
	 * @throws JSONException 
	 */
	@Transactional
	@Rollback(true)
	@Test
	public void test_uploadMeaningfulUseUsers_noHeader_runsWithoutError() throws EntityRetrievalException, EntityCreationException, IOException {
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		logger.info("Running test_uploadMeaningfulUseUsers_returnsMeaningfulUseUserResults");
		
		// Create CSV input for API
		MeaningfulUseUser meaningfulUseUser1 = new MeaningfulUseUser("CHP-024050", 10L);
		MeaningfulUseUser meaningfulUseUser2 = new MeaningfulUseUser("CHP-024051", 20L);
		MeaningfulUseUser meaningfulUseUser3 = new MeaningfulUseUser("CHP-024052", 30L);
		MeaningfulUseUser meaningfulUseUser4 = new MeaningfulUseUser("15.01.01.1009.EIC08.36.1.1.160402", 40L);
		MeaningfulUseUser meaningfulUseUser5 = new MeaningfulUseUser("14.01.01.1009.EIC08.36.1.1.160402", 50L);
		logger.info("Created 5 of MeaningfulUseUser to be updated in the database");
		
		List<MeaningfulUseUser> meaningfulUseUserList = new ArrayList<MeaningfulUseUser>();
		meaningfulUseUserList.add(meaningfulUseUser1);
		meaningfulUseUserList.add(meaningfulUseUser2);
		meaningfulUseUserList.add(meaningfulUseUser3);
		meaningfulUseUserList.add(meaningfulUseUser4);
		meaningfulUseUserList.add(meaningfulUseUser5);
		logger.info("Populated List of MeaningfulUseUser from 5 MeaningfulUseUser");
		
		File file = new File("testMeaningfulUseUsers.csv");
		
		try
        {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file.getName()), "UTF-8"));
            for (MeaningfulUseUser muUser : meaningfulUseUserList)
            {
                StringBuffer oneLine = new StringBuffer();
                oneLine.append(muUser.getProductNumber() != null ? muUser.getProductNumber() : "");
                oneLine.append(CSV_SEPARATOR);
                oneLine.append(muUser.getNumberOfUsers() != null ? muUser.getNumberOfUsers() : "");
                bw.write(oneLine.toString());
                bw.newLine();
            }
            bw.flush();
            bw.close();
        }
        catch (UnsupportedEncodingException e) {}
        catch (FileNotFoundException e){}
        catch (IOException e){}
		logger.info("Wrote meaningfulUseUserList to testMeaningfulUseUsers.csv");
	
		FileInputStream input = new FileInputStream(file);
		logger.info("Create FileInputStream from csv file to populate MultipartFile");
		MultipartFile multipartFile = new MockMultipartFile("file", file.getName(), "text/csv", IOUtils.toByteArray(input));
		logger.info("Create & populate MultipartFile from csv file");
		
		MeaningfulUseUserResults apiResult = new MeaningfulUseUserResults();
		try {
			apiResult = certifiedProductController.uploadMeaningfulUseUsers(multipartFile);
		} catch (MaxUploadSizeExceededException e) {
			e.printStackTrace();
		} catch (ValidationException e) {
			e.printStackTrace();
		}
		
		input.close();
		file.delete();
		
		assertTrue("MeaningfulUseUserResults returned " + apiResult.getMeaningfulUseUsers().size() + " but should return 5 results", apiResult.getMeaningfulUseUsers().size() == 5);
		assertTrue("MeaningfulUseUserResults returned " + apiResult.getMeaningfulUseUsers().get(0).getProductNumber() + " but should return CHP-024050", 
				apiResult.getMeaningfulUseUsers().get(0).getProductNumber().equals("CHP-024050"));
		assertTrue("MeaningfulUseUserResults returned " + apiResult.getMeaningfulUseUsers().get(0).getNumberOfUsers() + " but should return " + 10L, 
				apiResult.getMeaningfulUseUsers().get(0).getNumberOfUsers().equals(10L));
		assertTrue("MeaningfulUseUserResults returned " + apiResult.getMeaningfulUseUsers().get(0).getCertifiedProductId() + " but should return " + 1L, 
				apiResult.getMeaningfulUseUsers().get(0).getCertifiedProductId() == 1L);
		assertTrue("MeaningfulUseUserResults returned " + apiResult.getMeaningfulUseUsers().get(1).getProductNumber() + " but should return CHP-024051", 
				apiResult.getMeaningfulUseUsers().get(1).getProductNumber().equals("CHP-024051"));
		assertTrue("MeaningfulUseUserResults returned " + apiResult.getMeaningfulUseUsers().get(1).getNumberOfUsers() + " but should return " + 20L, 
				apiResult.getMeaningfulUseUsers().get(1).getNumberOfUsers().equals(20L));
		assertTrue("MeaningfulUseUserResults returned " + apiResult.getMeaningfulUseUsers().get(1).getCertifiedProductId() + " but should return " + 2L, 
				apiResult.getMeaningfulUseUsers().get(1).getCertifiedProductId() == 2L);
		assertTrue("MeaningfulUseUserResults returned " + apiResult.getMeaningfulUseUsers().get(2).getProductNumber() + " but should return CHP-024052", 
				apiResult.getMeaningfulUseUsers().get(2).getProductNumber().equals("CHP-024052"));
		assertTrue("MeaningfulUseUserResults returned " + apiResult.getMeaningfulUseUsers().get(2).getNumberOfUsers() + " but should return " + 30L, 
				apiResult.getMeaningfulUseUsers().get(2).getNumberOfUsers().equals(30L));
		assertTrue("MeaningfulUseUserResults returned " + apiResult.getMeaningfulUseUsers().get(2).getCertifiedProductId() + " but should return " + 3L, 
				apiResult.getMeaningfulUseUsers().get(2).getCertifiedProductId() == 3L);
		assertTrue("MeaningfulUseUserResults returned " + apiResult.getMeaningfulUseUsers().get(3).getProductNumber() + " but should return 15.01.01.1009.EIC08.36.1.1.160402", 
				apiResult.getMeaningfulUseUsers().get(3).getProductNumber().equals("15.01.01.1009.EIC08.36.1.1.160402"));
		assertTrue("MeaningfulUseUserResults returned " + apiResult.getMeaningfulUseUsers().get(3).getNumberOfUsers() + " but should return " + 40L, 
				apiResult.getMeaningfulUseUsers().get(3).getNumberOfUsers().equals(40L));
		assertTrue("MeaningfulUseUserResults returned " + apiResult.getMeaningfulUseUsers().get(3).getCertifiedProductId() + " but should return " + 5L, 
				apiResult.getMeaningfulUseUsers().get(3).getCertifiedProductId() == 5L);
		assertTrue("MeaningfulUseUserResults returned " + apiResult.getMeaningfulUseUsers().get(3).getProductNumber() + " but should return 14.01.01.1009.EIC08.36.1.1.160402", 
				apiResult.getMeaningfulUseUsers().get(4).getProductNumber().equals("14.01.01.1009.EIC08.36.1.1.160402"));
		assertTrue("MeaningfulUseUserResults returned " + apiResult.getMeaningfulUseUsers().get(4).getNumberOfUsers() + " but should return " + 50L, 
				apiResult.getMeaningfulUseUsers().get(4).getNumberOfUsers().equals(50L));
		assertTrue("MeaningfulUseUserResults returned " + apiResult.getMeaningfulUseUsers().get(4).getCertifiedProductId() + " but should return " + 1L, 
				apiResult.getMeaningfulUseUsers().get(4).getCertifiedProductId() == 1L);
	}
	
	/** 
	 * This tests 4 scenarios for CP Update(CertifiedProductSearchDetails) to determine that a warning is returned for mismatched Certification Status + CHPL Product Number ICS. 
	 * An error should be returned when Certification Status + CHPL Product Number ICS are matching Boolean values 
	 * because a Certified Product cannot carry a retired Test Tool when the CP ICS = false.
	 * 
	 * 1. 2015 Certification Edition + false Certification Status + true ICS = returns error (no mismatch)
	 * Given that a user with sufficient privileges edits/updates an existing Certified Product 
	 * (Note: the logged in user must have ROLE_ADMIN or ROLE_ACB_ADMIN and have administrative authority on the ACB that certified the product. 
	 * If a different ACB is passed in as part of the request, an ownership change will take place and the logged in user must have ROLE_ADMIN)
	 * When the UI calls the API at /certified_products/update
	 * When the user tries to update a 2015 Certified Product with the following:
	 * existing ics = true
	 * retired test tool = true
	 * Inherited Certification Status false(unchecked) and the user sets the CHPL Product Number's ICS to 0
	 * Then the API returns an error because there is no mismatch between Certification Status and CHPL Product Number ICS
	 * 
	 * 2. 2015 Certification Edition + true Certification Status + false ICS = returns warning (mismatch)
	 * When the user tries to update a 2015 Certified Product with the following:
	 * existing ics = true
	 * retired test tool = true
	 * Inherited Certification Status true(checked) and the user sets the CHPL Product Number's ICS to 0
	 * Then the API returns a warning because Inherited Certification Status and CHPL Product Number ICS are mismatched
	 * 
	 * 3. 2014 Certification Edition + false Certification Status + true ICS = returns error (no mismatch)
	 * * When the user tries to update a 2014 Certified Product with the following:
	 * existing ics = false
	 * retired test tool = true
	 * Inherited Certification Status false(unchecked) and the user sets the CHPL Product Number's ICS to 0
	 * Then the API returns an error because there is no mismatch between Certification Status and CHPL Product Number ICS
	 * 
	 * 4. 2014 Certification Edition + true Certification Status + false ICS = returns warning (mismatch)
	 * * When the user tries to update a 2014 Certified Product with the following:
	 * existing ics = true
	 * retired test tool = true
	 * Inherited Certification Status true(checked) and the user sets the CHPL Product Number's ICS to 0
	 * Then the API returns a warning because Inherited Certification Status and CHPL Product Number ICS are mismatched
	 * @throws IOException 
	 * @throws JSONException 
	 */
	@Transactional
	@Rollback(true)
	@Test
	public void test_updateCertifiedProductSearchDetails_icsAndRetiredTTs_warningvsError() throws EntityRetrievalException, EntityCreationException, IOException {
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		CertifiedProductSearchDetails updateRequest = new CertifiedProductSearchDetails();
		updateRequest.setCertificationDate(1440090840000L);
		updateRequest.setId(1L); // Certified_product_id = 1 has icsCode = true and is associated with TestTool with id=2 & id = 3 that have retired = true
		Map<String, Object> certStatus = new HashMap<String, Object>();
		certStatus.put("name", "Active");
		updateRequest.setCertificationStatus(certStatus);
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
		List<CertificationResultTestTool> crttList = new ArrayList<CertificationResultTestTool>();
		CertificationResultTestTool crtt = new CertificationResultTestTool();
		crtt.setId(2L);
		crtt.setRetired(true);
		crtt.setTestToolId(2L);
		crtt.setTestToolName("Transport Test Tool");
		crttList.add(crtt);
		cr.setTestToolsUsed(crttList);
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
		Map<String, Object> certificationEdition = new HashMap<String, Object>();
		String certEdition = "2015";
		certificationEdition.put("name", certEdition);
		updateRequest.setCertificationEdition(certificationEdition);
		updateRequest.setIcs(false); // Inherited Status = product.getIcs();
		updateRequest.setChplProductNumber("15.07.07.2642.EIC04.36.0.1.160402");
		try {
			certifiedProductController.updateCertifiedProduct(updateRequest);
		} catch (InvalidArgumentsException e) {
			e.printStackTrace();
		} catch (ValidationException e) {
			assertNotNull(e);
			// ICS is false, 15.07.07.2642.EIC04.36.0.1.160402 shows false ICS. No mismatch = error message
			assertTrue(e.getErrorMessages().contains("Test Tool 'Transport Test Tool' can not be used for criteria '170.314 (b)(6)', "
					+ "as it is a retired tool, and this Certified Product does not carry ICS."));
		}
		
		updateRequest.setIcs(true);
		try {
			certifiedProductController.updateCertifiedProduct(updateRequest);
		} catch (InvalidArgumentsException e) {
			e.printStackTrace();
		} catch (ValidationException e) {
			assertNotNull(e);
			// ICS is true, 15.07.07.2642.EIC04.36.0.1.160402 shows false ICS. Mismatch = warning message
			assertTrue(e.getWarningMessages().contains("Test Tool 'Transport Test Tool' can not be used for criteria '170.314 (b)(6)', "
					+ "as it is a retired tool, and this Certified Product does not carry ICS."));
		}
		Map<String, Object> classificationType = new HashMap<String, Object>();
		classificationType.put("name", "Modular EHR");
		updateRequest.setClassificationType(classificationType);
		Map<String, Object> practiceType = new HashMap<String, Object>();
		practiceType.put("name", "AMBULATORY");
		updateRequest.setPracticeType(practiceType);
		Map<String, Object> certificationEdition2014 = new HashMap<String, Object>();
		String certEdition2014 = "2014";
		certificationEdition2014.put("name", certEdition2014);
		updateRequest.setCertificationEdition(certificationEdition2014);
		updateRequest.setIcs(false);
		try {
			certifiedProductController.updateCertifiedProduct(updateRequest);
		} catch (InvalidArgumentsException e) {
			e.printStackTrace();
		} catch (ValidationException e) {
			assertNotNull(e);
			// 2014 certEdition; ICS is false, 15.07.07.2642.EIC04.36.0.1.160402 shows false ICS. No mismatch = error message
			assertTrue(e.getErrorMessages().contains("Test Tool 'Transport Test Tool' can not be used for criteria '170.314 (b)(6)', "
					+ "as it is a retired tool, and this Certified Product does not carry ICS."));
		}
		
		updateRequest.setIcs(true);
		try {
			certifiedProductController.updateCertifiedProduct(updateRequest);
		} catch (InvalidArgumentsException e) {
			e.printStackTrace();
		} catch (ValidationException e) {
			assertNotNull(e);
			// 2014 certEdition; ICS is true, 15.07.07.2642.EIC04.36.0.1.160402 shows false ICS. Mismatch = warning message
			assertTrue(e.getWarningMessages().contains("Test Tool 'Transport Test Tool' can not be used for criteria '170.314 (b)(6)', "
					+ "as it is a retired tool, and this Certified Product does not carry ICS."));
		}
		
	}
	
	/** 
	 * This tests 4 scenarios for CP Update(PendingCertifiedProductDTO) to determine that a warning is returned for mismatched Certification Status + CHPL Product Number ICS. 
	 * An error should be returned when Certification Status + CHPL Product Number ICS are matching Boolean values 
	 * because a Certified Product cannot carry a retired Test Tool when the CP ICS = false.
	 * 
	 * 1. 2015 Certification Edition + false Certification Status + true ICS = returns error (no mismatch)
	 * Given that a user with sufficient privileges edits/updates an existing Certified Product 
	 * (Note: the logged in user must have ROLE_ADMIN or ROLE_ACB_ADMIN and have administrative authority on the ACB that certified the product. 
	 * If a different ACB is passed in as part of the request, an ownership change will take place and the logged in user must have ROLE_ADMIN)
	 * When the UI calls the API at /certified_products/update
	 * When the user tries to update a 2015 Certified Product with the following:
	 * existing ics = true
	 * retired test tool = true
	 * Inherited Certification Status false(unchecked) and the user sets the CHPL Product Number's ICS to 0
	 * Then the API returns an error because there is no mismatch between Certification Status and CHPL Product Number ICS
	 * 
	 * 2. 2015 Certification Edition + true Certification Status + false ICS = returns warning (mismatch)
	 * When the user tries to update a 2015 Certified Product with the following:
	 * existing ics = true
	 * retired test tool = true
	 * Inherited Certification Status true(checked) and the user sets the CHPL Product Number's ICS to 0
	 * Then the API returns a warning because Inherited Certification Status and CHPL Product Number ICS are mismatched
	 * 
	 * 3. 2014 Certification Edition + false Certification Status + true ICS = returns error (no mismatch)
	 * * When the user tries to update a 2014 Certified Product with the following:
	 * existing ics = false
	 * retired test tool = true
	 * Inherited Certification Status false(unchecked) and the user sets the CHPL Product Number's ICS to 0
	 * Then the API returns an error because there is no mismatch between Certification Status and CHPL Product Number ICS
	 * 
	 * 4. 2014 Certification Edition + true Certification Status + false ICS = returns warning (mismatch)
	 * * When the user tries to update a 2014 Certified Product with the following:
	 * existing ics = true
	 * retired test tool = true
	 * Inherited Certification Status true(checked) and the user sets the CHPL Product Number's ICS to 0
	 * Then the API returns a warning because Inherited Certification Status and CHPL Product Number ICS are mismatched
	 * @throws IOException 
	 * @throws ParseException 
	 * @throws JSONException 
	 */
	@Transactional
	@Rollback(true)
	@Test
	public void test_updatePendingCertifiedProductDTO_icsAndRetiredTTs_warningvsError() throws EntityRetrievalException, EntityCreationException, IOException, ParseException {
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		PendingCertifiedProductDTO pcpDTO = new PendingCertifiedProductDTO();
		String certDateString = "11-09-2016";
		DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
		Date inputDate = dateFormat.parse(certDateString);
		pcpDTO.setCertificationDate(inputDate);
		pcpDTO.setId(1L); // Certified_product_id = 1 has icsCode = true and is associated with TestTool with id=2 & id = 3 that have retired = true
		//Map<String, Object> certStatus = new HashMap<String, Object>();
		//certStatus.put("name", "Active");
		//updateRequest.set(certStatus);
		List<CertificationResultTestTool> crttList = new ArrayList<CertificationResultTestTool>();
		CertificationResultTestTool crtt = new CertificationResultTestTool();
		crtt.setId(1L);
		crtt.setRetired(true);
		crtt.setTestToolId(2L);
		crtt.setTestToolName("Transport Test Tool");
		crttList.add(crtt);
		List<PendingCertificationResultDTO> pcrDTOs = new ArrayList<PendingCertificationResultDTO>();
		PendingCertificationResultDTO pcpCertResultDTO1 = new PendingCertificationResultDTO();
		pcpCertResultDTO1.setPendingCertifiedProductId(1L);
		pcpCertResultDTO1.setId(1L);
		pcpCertResultDTO1.setAdditionalSoftware(null);
		pcpCertResultDTO1.setApiDocumentation(null);
		pcpCertResultDTO1.setG1Success(false);
		pcpCertResultDTO1.setG2Success(false);
		pcpCertResultDTO1.setGap(null);
		pcpCertResultDTO1.setNumber("170.314 (b)(6)");
		pcpCertResultDTO1.setPrivacySecurityFramework(null);
		pcpCertResultDTO1.setSed(null);
		pcpCertResultDTO1.setG1Success(false);
		pcpCertResultDTO1.setG2Success(false);
		pcpCertResultDTO1.setTestData(null);
		pcpCertResultDTO1.setTestFunctionality(null);
		pcpCertResultDTO1.setTestProcedures(null);
		pcpCertResultDTO1.setTestStandards(null);
		pcpCertResultDTO1.setTestTasks(null);
		pcpCertResultDTO1.setMeetsCriteria(true);
		List<PendingCertificationResultTestToolDTO> pcprttdtoList = new ArrayList<PendingCertificationResultTestToolDTO>();
		PendingCertificationResultTestToolDTO pcprttdto1 = new PendingCertificationResultTestToolDTO();
		pcprttdto1.setId(2L);
		pcprttdto1.setName("Transport Test Tool");
		pcprttdto1.setPendingCertificationResultId(1L);
		pcprttdto1.setTestToolId(2L);
		pcprttdto1.setVersion(null);
		PendingCertificationResultTestToolDTO pcprttdto2 = new PendingCertificationResultTestToolDTO();
		pcprttdto2.setId(3L);
		pcprttdto2.setName("Transport Test Tool");
		pcprttdto2.setPendingCertificationResultId(1L);
		pcprttdto2.setTestToolId(3L);
		pcprttdto2.setVersion(null);
		pcprttdtoList.add(pcprttdto1);
		pcprttdtoList.add(pcprttdto2);
		pcpCertResultDTO1.setTestTools(pcprttdtoList);
		pcrDTOs.add(pcpCertResultDTO1);
		PendingCertificationResultDTO pcpCertResultDTO2 = new PendingCertificationResultDTO();
		pcpCertResultDTO2.setId(2L);
		pcpCertResultDTO2.setAdditionalSoftware(null);
		pcpCertResultDTO2.setApiDocumentation(null);
		pcpCertResultDTO2.setG1Success(false);
		pcpCertResultDTO2.setG2Success(false);
		pcpCertResultDTO2.setGap(null);
		pcpCertResultDTO2.setNumber("170.314 (b)(6)");
		pcpCertResultDTO2.setPrivacySecurityFramework(null);
		pcpCertResultDTO2.setSed(null);
		pcpCertResultDTO2.setG1Success(false);
		pcpCertResultDTO2.setG2Success(false);
		pcpCertResultDTO2.setTestData(null);
		pcpCertResultDTO2.setTestFunctionality(null);
		pcpCertResultDTO2.setTestProcedures(null);
		pcpCertResultDTO2.setTestStandards(null);
		pcpCertResultDTO2.setTestTasks(null);
		pcpCertResultDTO2.setMeetsCriteria(true);
		pcpCertResultDTO2.setTestTools(pcprttdtoList);
		pcrDTOs.add(pcpCertResultDTO2);
		pcpDTO.setCertificationCriterion(pcrDTOs);
		List<PendingCqmCriterionDTO> cqmCriterionDTOList = new ArrayList<PendingCqmCriterionDTO>();
		PendingCqmCriterionDTO cqm = new PendingCqmCriterionDTO();
		cqm.setVersion("v0");;
		cqm.setCmsId("CMS60");
		cqm.setCqmCriterionId(0L);
		cqm.setCqmNumber(null); 
		cqm.setDomain(null);
		cqm.setId(0L);
		cqm.setNqfNumber("0164");
		cqm.setTitle("Fibrinolytic Therapy Received Within 30 Minutes of Hospital Arrival");
		cqm.setTypeId(2L);
		cqmCriterionDTOList.add(cqm);
		pcpDTO.setCqmCriterion(cqmCriterionDTOList);
		String certEdition = "2015";
		pcpDTO.setCertificationEdition(certEdition);
		pcpDTO.setCertificationEditionId(3L); // 1 = 2011; 2 = 2014; 3 = 2015
		pcpDTO.setIcs(false); // Inherited Status = product.getIcs();
		pcpDTO.setUniqueId("15.07.07.2642.EIC04.36.0.1.160402");
		CertifiedProductValidator validator = validatorFactory.getValidator(pcpDTO);
		if(validator != null) {
			validator.validate(pcpDTO);
		}
		// test 1
		// ICS is false, 15.07.07.2642.EIC04.36.0.1.160402 shows false ICS. No mismatch = error message
		assertTrue(pcpDTO.getErrorMessages().contains("Test Tool 'Transport Test Tool' can not be used for criteria '170.314 (b)(6)', "
					+ "as it is a retired tool, and this Certified Product does not carry ICS."));
		
		// test 2
		pcpDTO.setIcs(true); // Inherited Status = product.getIcs();
		validator = validatorFactory.getValidator(pcpDTO);
		if(validator != null) {
			validator.validate(pcpDTO);
		}
		// ICS is false, 15.07.07.2642.EIC04.36.0.1.160402 shows false ICS. No mismatch = error message
		assertTrue(pcpDTO.getWarningMessages().contains("Test Tool 'Transport Test Tool' can not be used for criteria '170.314 (b)(6)', "
					+ "as it is a retired tool, and this Certified Product does not carry ICS."));
		
		// test 3
		certEdition = "2014";
		pcpDTO.setCertificationEdition(certEdition);
		pcpDTO.setIcs(false); // Inherited Status = product.getIcs();
		pcpDTO.setPracticeType("AMBULATORY");
		pcpDTO.setProductClassificationName("Modular EHR");
		validator = validatorFactory.getValidator(pcpDTO);
		if(validator != null) {
			validator.validate(pcpDTO);
		}
		// ICS is false, 15.07.07.2642.EIC04.36.0.1.160402 shows false ICS. No mismatch = error message
		assertTrue(pcpDTO.getErrorMessages().contains("Test Tool 'Transport Test Tool' can not be used for criteria '170.314 (b)(6)', "
					+ "as it is a retired tool, and this Certified Product does not carry ICS."));
		
		// test 4
		pcpDTO.setIcs(true);
		validator = validatorFactory.getValidator(pcpDTO);
		if(validator != null) {
			validator.validate(pcpDTO);
		}
		// ICS is false, 15.07.07.2642.EIC04.36.0.1.160402 shows false ICS. No mismatch = error message
		assertTrue(pcpDTO.getWarningMessages().contains("Test Tool 'Transport Test Tool' can not be used for criteria '170.314 (b)(6)', "
					+ "as it is a retired tool, and this Certified Product does not carry ICS."));
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

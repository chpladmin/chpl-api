package gov.healthit.chpl.web.controller;

import static org.junit.Assert.assertTrue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
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
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.MeaningfulUseUser;
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
	 * Given that a user with ROLE_ONC_STAFF or ROLE_ADMIN has uploaded a CSV with meaningfulUseUser counts (passed in as MultipartFile file)
	 * When the UI calls the API at /uploadMeaningfulUse
	 * When the CSV contains incorrectly named headers
	 * When the CSV contains some incorrect CHPLProductNumbers
	 * When the CSV contains 2014 & 2015 edition CHPL Product Numbers
	 * When the CSV contains a row with leading and trailing spaces for CHPL Product Number
	 * Then the API parses the contents of the csv
	 * Then the API updates the meaningfulUseUser count for each CHPL Product Number/Certified Product
	 * Then the API returns MeaningfulUseUserResults to the UI as a JSON response
	 * Then the MeaningfulUseUserResults contains an array with unsuccessful updates
	 * Then the API trims leading and trailing spaces and successfully updates meaningfulUseUsers for the CHPLProductNumber
	 * Then an Activity is added to the activity table in the database
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
		MeaningfulUseUser meaningfulUseUser1 = new MeaningfulUseUser("CHP-024050", 10L);
		MeaningfulUseUser meaningfulUseUser2 = new MeaningfulUseUser("CHP-024051", 20L);
		MeaningfulUseUser meaningfulUseUser3 = new MeaningfulUseUser(" CHP-024052 ", 30L);
		MeaningfulUseUser meaningfulUseUser4 = new MeaningfulUseUser("12.01.01.1234.AB01.01.0.1.123456", 40L);
		MeaningfulUseUser meaningfulUseUser5 = new MeaningfulUseUser("wrongChplProductNumber", 50L);
		MeaningfulUseUser meaningfulUseUser6 = new MeaningfulUseUser(" CHPL-024053 ", 60L);
		MeaningfulUseUser meaningfulUseUser7 = new MeaningfulUseUser("15.01.01.1234.AB01.01.0.1.123456", 70L);
		logger.info("Created 7 of MeaningfulUseUser to be updated in the database");
		
		List<MeaningfulUseUser> meaningfulUseUserList = new ArrayList<MeaningfulUseUser>();
		meaningfulUseUserList.add(meaningfulUseUser1);
		meaningfulUseUserList.add(meaningfulUseUser2);
		meaningfulUseUserList.add(meaningfulUseUser3);
		meaningfulUseUserList.add(meaningfulUseUser4);
		meaningfulUseUserList.add(meaningfulUseUser5);
		meaningfulUseUserList.add(meaningfulUseUser6);
		meaningfulUseUserList.add(meaningfulUseUser7);
		logger.info("Populated List of MeaningfulUseUser from 7 MeaningfulUseUser");
		
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
		
		assertTrue("MeaningfulUseUserResults returns multiple results", apiResult.getMeaningfulUseUsers().size() == 4);
		assertTrue("MeaningfulUseUserResults returned " + apiResult.getMeaningfulUseUsers().get(0).getProductNumber() + " but should return CHP-024050", 
				apiResult.getMeaningfulUseUsers().get(0).getProductNumber().equals("CHP-024050"));
		assertTrue("MeaningfulUseUserResults returned " + apiResult.getMeaningfulUseUsers().get(0).getNumberOfUsers() + " but should return " + 10L, 
				apiResult.getMeaningfulUseUsers().get(0).getNumberOfUsers().equals(10L));
		assertTrue("MeaningfulUseUserResults should return correct product number", apiResult.getMeaningfulUseUsers().get(1).getProductNumber().equals("CHP-024051"));
		assertTrue("MeaningfulUseUserResults should return correct numMeaningfulUseUsers", apiResult.getMeaningfulUseUsers().get(1).getNumberOfUsers().equals(20L));
		assertTrue("MeaningfulUseUserResults should return correct product number", apiResult.getMeaningfulUseUsers().get(2).getProductNumber().equals("CHP-024052"));
		assertTrue("MeaningfulUseUserResults should return correct numMeaningfulUseUsers", apiResult.getMeaningfulUseUsers().get(2).getNumberOfUsers().equals(30L));
		assertTrue("MeaningfulUseUserResults should return correct product number", apiResult.getMeaningfulUseUsers().get(3).getProductNumber().equals("12.01.01.1234.AB01.01.0.1.123456"));
		assertTrue("MeaningfulUseUserResults should return correct numMeaningfulUseUsers", apiResult.getMeaningfulUseUsers().get(3).getNumberOfUsers().equals(40L));
		assertTrue("MeaningfulUseUserResults errors array should return incorrect CHPL Product Number for row with {wrongChplProductNumber, 50L} but returned " 
		+ apiResult.getErrors().get(0).getError(), apiResult.getErrors().get(0).getError() != null);
		assertTrue("MeaningfulUseUserResults errors array should return incorrect CHPL Product Number for row with {CHPL-024053, 60L} but returned " 
				+ apiResult.getErrors().get(1).getError(), apiResult.getErrors().get(1).getError() != null);
		assertTrue("MeaningfulUseUserResults errors array for row {CHPL-024053, 60L} should have Product Number CHPL-024053 but has " 
				+ apiResult.getErrors().get(1).getProductNumber(), apiResult.getErrors().get(1).getProductNumber().equals("CHPL-024053"));
		assertTrue("MeaningfulUseUserResults errors array for row {CHPL-024053, 60L} should have num_meaningful_use 60L but has " 
				+ apiResult.getErrors().get(1).getNumberOfUsers(), apiResult.getErrors().get(1).getNumberOfUsers() == 60L);
		assertTrue("MeaningfulUseUserResults errors array should return incorrect CHPL Product Number for row with {15.01.01.1234.AB01.01.0.1.123456, 70L} but returned " 
				+ apiResult.getErrors().get(2).getError(), apiResult.getErrors().get(2).getError() != null);
		assertTrue("MeaningfulUseUserResults errors array for row {15.01.01.1234.AB01.01.0.1.123456, 70L} should have Product Number 15.01.01.1234.AB01.01.0.1.123456 but has " 
				+ apiResult.getErrors().get(2).getProductNumber(), apiResult.getErrors().get(2).getProductNumber().equals("15.01.01.1234.AB01.01.0.1.123456"));
		assertTrue("MeaningfulUseUserResults errors array for row {15.01.01.1234.AB01.01.0.1.123456, 70L} should have num_meaningful_use 70L but has " 
				+ apiResult.getErrors().get(2).getNumberOfUsers(), apiResult.getErrors().get(2).getNumberOfUsers() == 70L);
		assertTrue("MeaningfulUseUserResults returned " + apiResult.getMeaningfulUseUsers().get(3).getProductNumber() + " but should return 12.01.01.1234.AB01.01.0.1.123456", 
				apiResult.getMeaningfulUseUsers().get(3).getProductNumber().equals("12.01.01.1234.AB01.01.0.1.123456"));
		assertTrue("MeaningfulUseUserResults returned " + apiResult.getMeaningfulUseUsers().get(3).getNumberOfUsers() + " but should return " + 40L, 
				apiResult.getMeaningfulUseUsers().get(3).getNumberOfUsers().equals(40L));
	}
	
	/** 
	 * Given that a user with ROLE_ONC_STAFF or ROLE_ADMIN has uploaded a CSV with meaningfulUseUser counts (passed in as MultipartFile file)
	 * When the UI calls the API at /uploadMeaningfulUse
	 * When the CSV contains no header
	 * Then the API returns an error that the file should contain a header
	 * @throws IOException 
	 * @throws JSONException 
	 */
	@Transactional
	@Rollback(true)
	@Test
	public void test_uploadMeaningfulUseUsers_noHeader_returnsError() throws EntityRetrievalException, EntityCreationException, IOException {
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		logger.info("Running test_uploadMeaningfulUseUsers_returnsMeaningfulUseUserResults");
		
		// Create CSV input for API
		MeaningfulUseUser meaningfulUseUser1 = new MeaningfulUseUser("CHP-024050", 10L);
		MeaningfulUseUser meaningfulUseUser2 = new MeaningfulUseUser("CHP-024051", 20L);
		MeaningfulUseUser meaningfulUseUser3 = new MeaningfulUseUser("CHP-024052", 30L);
		MeaningfulUseUser meaningfulUseUser4 = new MeaningfulUseUser("12.01.01.1234.AB01.01.0.1.123456", 40L);
		logger.info("Created 4 of MeaningfulUseUser to be updated in the database");
		
		List<MeaningfulUseUser> meaningfulUseUserList = new ArrayList<MeaningfulUseUser>();
		meaningfulUseUserList.add(meaningfulUseUser1);
		meaningfulUseUserList.add(meaningfulUseUser2);
		meaningfulUseUserList.add(meaningfulUseUser3);
		meaningfulUseUserList.add(meaningfulUseUser4);
		logger.info("Populated List of MeaningfulUseUser from 4 MeaningfulUseUser");
		
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
		
		assertTrue("MeaningfulUseUserResults returned " + apiResult.getMeaningfulUseUsers().size() + " but should return 4 results", apiResult.getMeaningfulUseUsers().size() == 4);
		assertTrue("MeaningfulUseUserResults returned " + apiResult.getMeaningfulUseUsers().get(0).getProductNumber() + " but should return CHP-024050", 
				apiResult.getMeaningfulUseUsers().get(0).getProductNumber().equals("CHP-024050"));
		assertTrue("MeaningfulUseUserResults returned " + apiResult.getMeaningfulUseUsers().get(0).getNumberOfUsers() + " but should return " + 10L, 
				apiResult.getMeaningfulUseUsers().get(0).getNumberOfUsers().equals(10L));
		assertTrue("MeaningfulUseUserResults returned " + apiResult.getMeaningfulUseUsers().get(1).getProductNumber() + " but should return CHP-024051", 
				apiResult.getMeaningfulUseUsers().get(1).getProductNumber().equals("CHP-024051"));
		assertTrue("MeaningfulUseUserResults returned " + apiResult.getMeaningfulUseUsers().get(1).getNumberOfUsers() + " but should return " + 20L, 
				apiResult.getMeaningfulUseUsers().get(1).getNumberOfUsers().equals(20L));
		assertTrue("MeaningfulUseUserResults returned " + apiResult.getMeaningfulUseUsers().get(2).getProductNumber() + " but should return CHP-024052", 
				apiResult.getMeaningfulUseUsers().get(2).getProductNumber().equals("CHP-024052"));
		assertTrue("MeaningfulUseUserResults returned " + apiResult.getMeaningfulUseUsers().get(2).getNumberOfUsers() + " but should return " + 30L, 
				apiResult.getMeaningfulUseUsers().get(2).getNumberOfUsers().equals(30L));
		assertTrue("MeaningfulUseUserResults returned " + apiResult.getMeaningfulUseUsers().get(3).getProductNumber() + " but should return 12.01.01.1234.AB01.01.0.1.123456", 
				apiResult.getMeaningfulUseUsers().get(3).getProductNumber().equals("12.01.01.1234.AB01.01.0.1.123456"));
		assertTrue("MeaningfulUseUserResults returned " + apiResult.getMeaningfulUseUsers().get(3).getNumberOfUsers() + " but should return " + 40L, 
				apiResult.getMeaningfulUseUsers().get(3).getNumberOfUsers().equals(40L));
	}
	
}

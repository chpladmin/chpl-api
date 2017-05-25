package gov.healthit.chpl.web.controller;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
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
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.AccurateAsOfDate;
import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.MeaningfulUseUser;
import gov.healthit.chpl.web.controller.exception.ValidationException;
import gov.healthit.chpl.web.controller.results.MeaningfulUseUserResults;
import junit.framework.TestCase;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { gov.healthit.chpl.CHPLTestConfig.class })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class })
@DatabaseSetup("classpath:data/testData.xml") 
public class MeaningfulUseControllerTest extends TestCase {
	private static final Logger logger = LogManager.getLogger(MeaningfulUseControllerTest.class);
	
	private static final String CSV_SEPARATOR = ",";
	
	@Autowired MeaningfulUseController meaningfulUseController;
	@Autowired CertifiedProductDAO cpDao;
	
	@Rule
    @Autowired
    public UnitTestRules cacheInvalidationRule;
	
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
		MeaningfulUseUser meaningfulUseUser4 = new MeaningfulUseUser(" 15.01.01.1009.EIC13.36.1.1.160402 ", 40L); // MeaningfulUseUser 3
		MeaningfulUseUser meaningfulUseUser5 = new MeaningfulUseUser("wrongChplProductNumber", 50L); // Errors 0
		MeaningfulUseUser meaningfulUseUser6 = new MeaningfulUseUser(" CHPL-024053 ", 60L); // Errors 1
		MeaningfulUseUser meaningfulUseUser7 = new MeaningfulUseUser("15.02.03.9876.AB01.01.0.1.123456", 70L); // Errors 2
		MeaningfulUseUser meaningfulUseUser8 = new MeaningfulUseUser("15.01.01.1009.EIC13.36.1.1.160402", 70L); // Errors 3 (because duplicate of MeaningfulUseUser 3
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
			apiResult = meaningfulUseController.uploadMeaningfulUseUsers(multipartFile);
		} catch (MaxUploadSizeExceededException e) {
			e.printStackTrace();
		} catch (ValidationException e) {
			e.printStackTrace();
		}
		
		input.close();
		file.delete();
		
		for(MeaningfulUseUser muu : apiResult.getMeaningfulUseUsers()){
			if(muu.getProductNumber().equals("CHP-024050")){
				assertEquals(10, muu.getNumberOfUsers().longValue());
				assertEquals(1, muu.getCertifiedProductId().longValue());
			}
			else if(muu.getProductNumber().equals("CHP-024051")){
				assertEquals(20, muu.getNumberOfUsers().longValue());
				assertEquals(2, muu.getCertifiedProductId().longValue());
			}
			else if(muu.getProductNumber().equals("CHP-024052")){
				assertEquals(30, muu.getNumberOfUsers().longValue());
				assertEquals(3, muu.getCertifiedProductId().longValue());
			}
			else if(muu.getProductNumber().equals("15.01.01.1009.EIC13.36.1.1.160402")){
				assertEquals(40, muu.getNumberOfUsers().longValue());
				assertEquals(6, muu.getCertifiedProductId().longValue());
			}
		}
		
		for(MeaningfulUseUser muu : apiResult.getErrors()){
			Boolean hasError = false;
			if(muu.getProductNumber().equals("wrongChplProductNumber")){
				assertEquals(50L, muu.getNumberOfUsers().longValue());
				assertNotNull(muu.getError());
				hasError=true;
			}
			else if(muu.getProductNumber().equals("CHPL-024053")){
				assertEquals(60, muu.getNumberOfUsers().longValue());
				assertNotNull(muu.getError());
				hasError=true;
			}
			else if(muu.getProductNumber().equals("15.02.03.9876.AB01.01.0.1.123456")){
				assertEquals(70, muu.getNumberOfUsers().longValue());
				assertNotNull(muu.getError());
				hasError=true;
			}
			else if(muu.getProductNumber().equals("15.01.01.1009.EIC13.36.1.1.160402")){
				assertEquals(70, muu.getNumberOfUsers().longValue());
				assertNotNull(muu.getError());
				hasError=true;
			}
			assertTrue(hasError);
		}
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
		//get some 2015 CHPL IDs we can use
		CertifiedProduct cp1 = new CertifiedProduct(cpDao.getDetailsById(17L));
		CertifiedProduct cp2 = new CertifiedProduct(cpDao.getDetailsById(15L));
		MeaningfulUseUser meaningfulUseUser4 = new MeaningfulUseUser(cp1.getChplProductNumber(), 40L);
		MeaningfulUseUser meaningfulUseUser5 = new MeaningfulUseUser(cp2.getChplProductNumber(), 50L);
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
			apiResult = meaningfulUseController.uploadMeaningfulUseUsers(multipartFile);
		} catch (MaxUploadSizeExceededException e) {
			e.printStackTrace();
		} catch (ValidationException e) {
			e.printStackTrace();
		}
		
		input.close();
		file.delete();

		assertEquals(0, apiResult.getErrors().size());
		assertEquals(5, apiResult.getMeaningfulUseUsers().size());
		for(MeaningfulUseUser muu : apiResult.getMeaningfulUseUsers()){
			if(muu.getProductNumber().equals("CHP-024050")){
				assertEquals(10, muu.getNumberOfUsers().longValue());
				assertEquals(1, muu.getCertifiedProductId().longValue());
			}
			else if(muu.getProductNumber().equals("CHP-024051")){
				assertEquals(20, muu.getNumberOfUsers().longValue());
				assertEquals(2, muu.getCertifiedProductId().longValue());
			}
			else if(muu.getProductNumber().equals("CHP-024052")){
				assertEquals(30, muu.getNumberOfUsers().longValue());
				assertEquals(3, muu.getCertifiedProductId().longValue());
			}
			else if(muu.getProductNumber().equals(cp1.getChplProductNumber())){
				assertEquals(40, muu.getNumberOfUsers().longValue());
				assertEquals(cp1.getId().longValue(), muu.getCertifiedProductId().longValue());
			}
			else if(muu.getProductNumber().equals(cp2.getChplProductNumber())){
				assertEquals(50, muu.getNumberOfUsers().longValue());
				assertEquals(cp2.getId().longValue(), muu.getCertifiedProductId().longValue());
			}
		}
	}
	
	/**
	 * Given a user has set the Meaningful Use User Accurate As Of Date on the UI
	 * When the user sends an HTTP.POST
	 * Then the MeaningfulUseDAO updates the database and returns the DTO
	 * @throws ValidationException 
	 */
	@Test
	@Transactional
	@Rollback
	public void updateAccurateAsOfDate() throws EntityRetrievalException, ValidationException {
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		Calendar cal = Calendar.getInstance();
		Long timeInMillis = cal.getTimeInMillis();
		AccurateAsOfDate ad = new AccurateAsOfDate();
		ad.setAccurateAsOfDate(timeInMillis);
		meaningfulUseController.updateMeaningfulUseAccurateAsOf(ad);
		AccurateAsOfDate result = meaningfulUseController.getAccurateAsOfDate();
		assertTrue(result.getAccurateAsOfDate().equals(timeInMillis));
	}
	
}

package gov.healthit.chpl.web.controller;

import static org.junit.Assert.*;

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
import org.json.JSONException;
import org.junit.BeforeClass;
import org.junit.Ignore;
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
	 * Then the API parses the contents of the csv
	 * Then the API updates the meaningfulUseUser count for each CHPL Product Number/Certified Product
	 * Then the API returns MeaningfulUseUserResults to the UI as a JSON response
	 * @throws IOException 
	 * @throws JSONException 
	 */
	@Transactional
	@Rollback(true)
	@Test
	public void test_uploadMeaningfulUseUsers_returnsMeaningfulUseUserResults() throws EntityRetrievalException, EntityCreationException, IOException, JSONException{
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		logger.info("Running test_uploadMeaningfulUseUsers_returnsMeaningfulUseUserResults");
		
		// Create CSV input for API
		MeaningfulUseUser meaningfulUseUser1 = new MeaningfulUseUser("CHP-024050", 10L);
		MeaningfulUseUser meaningfulUseUser2 = new MeaningfulUseUser("CHP-024051", 20L);
		MeaningfulUseUser meaningfulUseUser3 = new MeaningfulUseUser("CHP-024052", 30L);
		logger.info("Created 3 of MeaningfulUseUser to be updated in the database");
		
		List<MeaningfulUseUser> meaningfulUseUserList = new ArrayList<MeaningfulUseUser>();
		meaningfulUseUserList.add(meaningfulUseUser1);
		meaningfulUseUserList.add(meaningfulUseUser2);
		meaningfulUseUserList.add(meaningfulUseUser3);
		logger.info("Populated List of MeaningfulUseUser from 3 MeaningfulUseUser");
		
		File file = new File("testMeaningfulUseUsers.csv");
		
		try
        {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file.getName()), "UTF-8"));
            StringBuffer headerLine = new StringBuffer();
            headerLine.append("chpl_product_number");
            headerLine.append(CSV_SEPARATOR);
            headerLine.append("num_meaningful_use");
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
		
		assertTrue("MeaningfulUseUserResults returns multiple results", apiResult.getMeaningfulUseUsers().size() == 3);
	}
}

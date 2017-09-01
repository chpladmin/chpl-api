package gov.healthit.chpl.web.controller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import org.springframework.web.multipart.MultipartFile;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.auth.dto.UserDTO;
import gov.healthit.chpl.auth.manager.UserManager;
import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.auth.user.UserRetrievalException;
import gov.healthit.chpl.caching.UnitTestRules;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.Job;
import gov.healthit.chpl.domain.MeaningfulUseUser;
import gov.healthit.chpl.dto.ContactDTO;
import gov.healthit.chpl.dto.job.JobDTO;
import gov.healthit.chpl.dto.job.JobTypeDTO;
import gov.healthit.chpl.entity.job.JobStatusType;
import gov.healthit.chpl.job.MeaningfulUseUploadJob;
import gov.healthit.chpl.job.RunnableJobFactory;
import gov.healthit.chpl.manager.JobManager;
import gov.healthit.chpl.web.controller.exception.ValidationException;
import junit.framework.TestCase;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { gov.healthit.chpl.CHPLTestConfig.class })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class })
@DatabaseSetup("classpath:data/testData.xml") 
public class MeaningfulUseJobControllerTest extends TestCase {
	private static final Logger logger = LogManager.getLogger(MeaningfulUseJobControllerTest.class);
	
	private static final String CSV_SEPARATOR = ",";
	
	@Autowired JobController jobController;
	@Autowired JobManager jobManager;
	@Autowired UserManager userManager;
	@Autowired CertifiedProductDAO cpDao;
	@Autowired MeaningfulUseUploadJob muuJob;
	
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

	@Test
	public void testCreateJobWithInvalidType() {
		SecurityContextHolder.getContext().setAuthentication(adminUser);		
		File file = createMuuCsv();
		
		ResponseEntity<Job> response = null;
		try {
			FileInputStream input = new FileInputStream(file);
			MultipartFile multipartFile = new MockMultipartFile("file", file.getName(), "text/csv", IOUtils.toByteArray(input));
			response = jobController.createJob("Bad Name", multipartFile);
			input.close();
			file.delete();
		} catch(IOException ex) {
			fail(ex.getMessage());
		} catch(EntityRetrievalException | EntityCreationException | ValidationException ex) {
			fail(ex.getMessage());
		}
		
		assertNotNull(response);
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
	}
	
	@Test()
	public void testCreateJobAsUnauthenticatedUser() {
		SecurityContextHolder.getContext().setAuthentication(null);		
		File file = createMuuCsv();
		
		ResponseEntity<Job> response = null;
		try {
			FileInputStream input = new FileInputStream(file);
			MultipartFile multipartFile = new MockMultipartFile("file", file.getName(), "text/csv", IOUtils.toByteArray(input));
			response = jobController.createJob("MUU Upload", multipartFile);
			input.close();
			file.delete();
		} catch(IOException ex) {
			fail(ex.getMessage());
		} catch(EntityRetrievalException | EntityCreationException | ValidationException ex) {
			fail(ex.getMessage());
		}
		
		assertNotNull(response);
		assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
	}
	
	@Transactional
	@Rollback(true)
	@Test
	public void testStartMeaningfulUseUploadJobNewThread() {
		SecurityContextHolder.getContext().setAuthentication(adminUser);		
		File file = createMuuCsv();
	
		ResponseEntity<Job> response = null;
		try {
			FileInputStream input = new FileInputStream(file);
			MultipartFile multipartFile = new MockMultipartFile("file", file.getName(), "text/csv", IOUtils.toByteArray(input));
			response = jobController.createJob("MUU Upload", multipartFile);
			input.close();
			file.delete();
		} catch(IOException ex) {
			fail(ex.getMessage());
		} catch(EntityRetrievalException | EntityCreationException | ValidationException ex) {
			fail(ex.getMessage());
		}
		assertNotNull(response);
		assertEquals(HttpStatus.OK, response.getStatusCode());
	}
	
	@Transactional
	@Rollback(false)
	@Test
	public void testStartMeaningfulUseUploadJobSameThread() throws IOException, ValidationException, EntityCreationException, EntityRetrievalException {
		SecurityContextHolder.getContext().setAuthentication(adminUser);		
		File file = createMuuCsv();
		FileInputStream input = new FileInputStream(file);
		MultipartFile multipartFile = new MockMultipartFile("file", file.getName(), "text/csv", IOUtils.toByteArray(input));
		
		//read the file into a string
		StringBuffer data = new StringBuffer();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(multipartFile.getInputStream()));
			String line = null;
			while((line = reader.readLine()) != null) {
				if(data.length() > 0) {
					data.append(System.getProperty("line.separator"));
				}
				data.append(line);
			}
		} catch(IOException ex) {
			String msg = "Could not read file: " + ex.getMessage();
			logger.error(msg);
			throw new ValidationException(msg);
		}
		
		UserDTO currentUser = null;
		try {
			currentUser = userManager.getById(Util.getCurrentUser().getId());
		} catch(UserRetrievalException ex) {
			logger.error("Error finding user with ID " + Util.getCurrentUser().getId() + ": " + ex.getMessage());
			fail("Error finding user with ID " + Util.getCurrentUser().getId() + ": " + ex.getMessage());
		}
		if(currentUser == null) {
			logger.error("No user with ID " + Util.getCurrentUser().getId() + " could be found in the system.");
			fail("No user with ID " + Util.getCurrentUser().getId() + " could be found in the system.");
		}
		
		JobTypeDTO jobType = null;
		List<JobTypeDTO> jobTypes = jobManager.getAllJobTypes();
		for(JobTypeDTO jt : jobTypes) {
			if(jt.getName().equalsIgnoreCase("MUU Upload")) {
				jobType = jt;
			}
		}
		JobDTO toCreate = new JobDTO();
		toCreate.setData(data.toString());
		ContactDTO contact = new ContactDTO();
		contact.setFirstName(currentUser.getFirstName());
		contact.setLastName(currentUser.getLastName());
		contact.setEmail(currentUser.getEmail());
		contact.setPhoneNumber(currentUser.getPhoneNumber());
		contact.setTitle(currentUser.getTitle());
		toCreate.setContact(contact);
		toCreate.setJobType(jobType);
		JobDTO insertedJob = jobManager.createJob(toCreate);
		JobDTO createdJob = jobManager.getJobById(insertedJob.getId());
	
		muuJob.setJob(createdJob);
		muuJob.run();
		
		input.close();
		file.delete();
		
		JobDTO completedJob = jobManager.getJobById(createdJob.getId());
		assertNotNull(completedJob.getStartTime());
		assertNotNull(completedJob.getEndTime());
		assertNotNull(completedJob.getStatus());
		assertEquals(JobStatusType.Complete, completedJob.getStatus().getStatus());
		assertEquals(100, completedJob.getStatus().getPercentComplete().intValue());
		assertNotNull(completedJob.getMessages());
		assertEquals(4, completedJob.getMessages().size());
	}
	
	private File createMuuCsv() {
		MeaningfulUseUser meaningfulUseUser1 = new MeaningfulUseUser("CHP-024050", 10L); // MeaningfulUseUser 0
		MeaningfulUseUser meaningfulUseUser2 = new MeaningfulUseUser("CHP-024051", 20L); // MeaningfulUseUser 1
		MeaningfulUseUser meaningfulUseUser3 = new MeaningfulUseUser(" CHP-024052 ", 30L); // MeaningfulUseUser 2
		MeaningfulUseUser meaningfulUseUser4 = new MeaningfulUseUser(" 15.01.01.1009.IC13.36.02.1.160402 ", 40L); // MeaningfulUseUser 3
		MeaningfulUseUser meaningfulUseUser5 = new MeaningfulUseUser("wrongChplProductNumber", 50L); // Errors 0
		MeaningfulUseUser meaningfulUseUser6 = new MeaningfulUseUser(" CHPL-024053 ", 60L); // Errors 1
		MeaningfulUseUser meaningfulUseUser7 = new MeaningfulUseUser("15.02.03.9876.AB01.01.00.1.123456", 70L); // Errors 2
		MeaningfulUseUser meaningfulUseUser8 = new MeaningfulUseUser("15.01.01.1009.IC13.36.02.1.160402", 70L); // Errors 3 (because duplicate of MeaningfulUseUser 3
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
		return file;
	}
}

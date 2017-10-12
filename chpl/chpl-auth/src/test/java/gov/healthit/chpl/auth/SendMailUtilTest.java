package gov.healthit.chpl.auth;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.mail.MessagingException;

import org.junit.BeforeClass;
import org.junit.Ignore;
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

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { gov.healthit.chpl.auth.CHPLAuthenticationSecurityTestConfig.class })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class,
		TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class })
@DatabaseSetup("classpath:data/testData.xml")
public class SendMailUtilTest {

	private static JWTAuthenticatedUser adminUser;
	@Autowired
	private SendMailUtil sendMailUtil;

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
	 * Description: Tests the sendEmail(String[] toEmail, String subject, String
	 * htmlMessage, String[] filenames) method
	 * 
	 * Expected Result: an email is sent with an attachment
	 * 
	 * Assumptions: N/A
	 * 
	 * @throws MessagingException
	 */
	@Transactional
	@Rollback(true)
	@Test
	@Ignore
	public void test_sendMailUtil_sendEmailWithAttachments() throws MessagingException {
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		String[] emailRecipients = new String[2];
		emailRecipients[0] = "dlucas@ainq.com";
		emailRecipients[1] = "daniel.r.lucas@gmail.com";
		String emailSubject = "Unit Test SendMailUtil_sendEmailWithAttachments";
		String emailMessage = "Message content for unit test SendMailUtil_sendEmailWithAttachments";
		List<File> files = new ArrayList<File>();
		File file1 = new File("C:\\CHPL\\emailTest.csv");
		files.add(file1);

		try {
			sendMailUtil.sendEmail(emailRecipients, emailSubject, emailMessage, files);
		} catch (Exception e) {
			throw e;
		}

	}

}
